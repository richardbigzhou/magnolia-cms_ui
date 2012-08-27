/**
 * This file Copyright (c) 2010-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.setup;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.core.Path;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Dialog migration main task.
 * Migrate dialogs for the specified moduleName.
 */
public class DialogMigrationTask  extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(DialogMigrationTask.class);
    private final String moduleName;
    private HashSet<Property> extendsAndReferenceProperty = new HashSet<Property>();

    public DialogMigrationTask(String moduleName) {
        super("Dialog Migration for 5.x","Migrate dialog for the following module: "+moduleName);
        this.moduleName = moduleName;
    }

    /**
     * Handle all Dialogs registered and migrate them.
     */
    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            Node dialog = installContext.getJCRSession(RepositoryConstants.CONFIG).getNode("/modules/"+moduleName+"/dialogs");
            // Copy to Dialog50
            copyInSession(dialog, dialog.getPath()+"50");
            NodeUtil.visit(dialog, new NodeVisitor() {
                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node dialogNode : NodeUtil.getNodes(current, MgnlNodeType.NT_CONTENTNODE)) {
                        performDialogMigration(dialogNode);
                    }
                }
            }, new NodeTypePredicate(MgnlNodeType.NT_CONTENT));
            // Try to resolve references for extends.
            postProcessForExtendsAndReference();
        } catch (Exception e) {
            installContext.warn("Could not Migrate Dialod for the following module "+moduleName);
        }
    }


    /**
     * Handle and Migrate a Dialog node.
     */
    private void performDialogMigration(Node dialog) throws RepositoryException {
        // Get child Nodes (should be Tab)
        Iterable<Node> tabNodes = NodeUtil.getNodes(dialog, DIALOG_FILTER);
        if(tabNodes.iterator().hasNext()) {
            //Check if it's a tab definition
            if(dialog.hasProperty("controlType") && dialog.getProperty("controlType").getString().equals("tab")) {
                handleTab(dialog);
            }else {
                //Handle action
                if(!dialog.hasProperty("controlType") &&!dialog.hasProperty("extends") && !dialog.hasProperty("reference")) {
                    handleAction(dialog);
                }
                //Handle tab
                handleTabs(dialog, tabNodes.iterator());
            }
        }else {
            //Handle as a field.
            handleField(dialog);
        }
        handleExtendsAndReference(dialog);
    }

    /**
     * Add action to node.
     */
    private void handleAction(Node dialog) throws RepositoryException {
        dialog.addNode("actions", MgnlNodeType.NT_CONTENTNODE);

        Node node = dialog.getNode("actions");

        node.addNode("commit", MgnlNodeType.NT_CONTENTNODE).setProperty("label", "save changes");
        node.addNode("cancel", MgnlNodeType.NT_CONTENTNODE).setProperty("label", "cancel");
        node.getNode("commit").addNode("actionDefinition", MgnlNodeType.NT_CONTENTNODE).setProperty("class", "info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition");
        node.getNode("cancel").addNode("actionDefinition", MgnlNodeType.NT_CONTENTNODE).setProperty("class", "info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition");

    }


    /**
     * Handle Tabs.
     */
    private void handleTabs(Node dialog, Iterator<Node> tabNodes) throws RepositoryException {
        Node dialogTabs = dialog.addNode("tabs", MgnlNodeType.NT_CONTENTNODE);
        while(tabNodes.hasNext()){
            Node tab = tabNodes.next();
            // Handle Fields Tab
            handleTab(tab);
            //Move tab
            NodeUtil.moveNode(tab, dialogTabs);
        }
    }

    /**
     * Handle a Tab.
     */
    private void handleTab(Node tab) throws RepositoryException{
        if((tab.hasProperty("controlType") && StringUtils.equals(tab.getProperty("controlType").getString(), "tab")) || (tab.getParent().hasProperty("extends"))){
            if(tab.hasProperty("controlType") && StringUtils.equals(tab.getProperty("controlType").getString(), "tab")){
                //Remove controlType Property
                tab.getProperty("controlType").remove();
            }
            //get all controls to be migrated
            Iterator<Node> controls = NodeUtil.getNodes(tab,MgnlNodeType.NT_CONTENTNODE).iterator();
            //create a fields Node
            Node fields = tab.addNode("fields", MgnlNodeType.NT_CONTENTNODE);

            while(controls.hasNext()){
                Node control = controls.next();
                //Handle fields
                handleField(control);
                //Move to fields
                NodeUtil.moveNode(control, fields);
            }
        }else if(tab.hasNode("inheritable")) {
            // Handle inheritable
            Node inheritable = tab.getNode("inheritable");
            handleExtendsAndReference(inheritable);
        } else {
            handleExtendsAndReference(tab);
        }
    }

    /**
     * Change controlType to the equivalent class.
     * Change the extend path.
     */
    private void handleField(Node fieldNode) throws RepositoryException {
        if(fieldNode.hasProperty("controlType")){
            if(fieldNode.getProperty("controlType").getString().equals("edit")){
                fieldNode.getProperty("controlType").remove();
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.TextFieldDefinition");
            }else if(fieldNode.getProperty("controlType").getString().equals("fckEdit")){
                fieldNode.getProperty("controlType").remove();
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.RichTextFieldDefinition");
            }else if(fieldNode.getProperty("controlType").getString().equals("date")){
                fieldNode.getProperty("controlType").remove();
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.DateFieldDefinition");
            }else if(fieldNode.getProperty("controlType").getString().equals("select")){
                fieldNode.getProperty("controlType").remove();
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.SelectFieldDefinition");
            }else if(fieldNode.getProperty("controlType").getString().equals("checkbox")){
                fieldNode.getProperty("controlType").remove();
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.OptionGroupFieldDefinition");
            }else if(fieldNode.getProperty("controlType").getString().equals("radio")){
                fieldNode.getProperty("controlType").remove();
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.OptionGroupFieldDefinition");
            }else if(fieldNode.getProperty("controlType").getString().equals("dam")){
                fieldNode.getProperty("controlType").remove();
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.FileUploadFieldDefinition");
            }else if(fieldNode.getProperty("controlType").getString().equals("uuidLink")){
                if(fieldNode.hasProperty("repository")) {
                    fieldNode.getProperty("controlType").remove();
                    fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.LinkFieldDefinition");
                    fieldNode.setProperty("dialogName", "ui-admincentral:link");
                    fieldNode.setProperty("uuid", "true");
                    if(fieldNode.getProperty("repository").getString().equals("website")) {
                        fieldNode.setProperty("appName", "pages");
                    }else if (fieldNode.getProperty("repository").getString().equals("data")) {
                        // Handle contacts
                        if(fieldNode.hasProperty("tree") && fieldNode.getProperty("tree").getString().equals("Contact")) {
                            fieldNode.setProperty("appName", "contacts");
                            fieldNode.setProperty("workspace", "contacts");
                        }
                    }
                }
            }else {
                fieldNode.setProperty("class", "info.magnolia.ui.model.field.definition.StaticFieldDefinition");
            }
        }else  {
            // Handle Field Extends/Reference
            handleExtendsAndReference(fieldNode);
        }
    }

    private void handleExtendsAndReference(Node node) throws RepositoryException {
        if (node.hasProperty("extends")) {
            // Handle Field Extends
            node.setProperty("extends",renameExtendsPath(node, "extends"));
        }else if (node.hasProperty("reference")) {
            // Handle Field Extends
            node.setProperty("reference",renameExtendsPath(node, "reference"));
        }
    }

    private String renameExtendsPath(Node fieldNode, String property) throws RepositoryException {
        extendsAndReferenceProperty.add(fieldNode.getProperty(property));
        return StringUtils.replace(fieldNode.getProperty(property).getString(), "/dialogs/", "/dialogs50/");
    }

    /**
     * Create a specific node filter.
     */
    private static AbstractPredicate<Node> DIALOG_FILTER = new AbstractPredicate<Node>() {
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return !node.getName().startsWith(MgnlNodeType.JCR_PREFIX)
                && !NodeUtil.isNodeType(node, MgnlNodeType.NT_METADATA) &&
                NodeUtil.isNodeType(node, MgnlNodeType.NT_CONTENTNODE);
            } catch (RepositoryException e) {
                return false;
            }
        }
    };


    /**
     * Check if the extends and reference are correct. If not try to do the best
     * to found a correct path.
     * @throws RepositoryException
     * @throws ValueFormatException
     */
    private void postProcessForExtendsAndReference() throws RepositoryException  {
        for(Property p:extendsAndReferenceProperty) {
            String path = p.getString();
            if(path.equals("override")) {
                continue;
            }
            if(!p.getSession().nodeExists(path)) {
                // Referred path do not exist.
                // add /tabs before the last /
                String newPath  = insertBeforeLastSlash(path, "/tabs");
                if(p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                    continue;
                }
                // add /fields before the last /
                newPath  = insertBeforeLastSlash(path, "/fields");
                if(p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                    continue;
                }

                //try with a fields before the last / with a tabs before the last /
                newPath  = insertBeforeLastSlash(path, "/tabs");
                newPath  = insertBeforeLastSlash(newPath, "/fields");
                if(p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                    continue;
                }
                // try to add a tabs before the 2nd last /
                String beging = path.substring(0, path.lastIndexOf("/"));
                String end = path.substring(beging.lastIndexOf("/"));
                beging = beging.substring(0, beging.lastIndexOf("/"));
                newPath = beging+"/tabs"+end;
                if(p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                    continue;
                }
                //try with a fields before the last / with a tabs before the 2nd last /
                newPath  = insertBeforeLastSlash(newPath, "/fields");
                if(p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                }else {
                    log.warn("reference to "+path+" not found");
                }
            }
        }
    }

    /**
     * Insert the toInsert ("/tabs") before the last /.
     */
    private String insertBeforeLastSlash(String reference, String toInsert) {
        String beging = reference.substring(0, reference.lastIndexOf("/"));
        String end = reference.substring(reference.lastIndexOf("/"));
        return  beging+toInsert+end;
    }

    /**
     * Session based copy operation. As JCR only supports workspace based copies this operation is performed.
     * by using export import operations.
     */
    private void copyInSession(Node src, String dest) throws RepositoryException {
        final String destParentPath = StringUtils.defaultIfEmpty(StringUtils.substringBeforeLast(dest, "/"), "/");
        final String destNodeName = StringUtils.substringAfterLast(dest, "/");
        final Session session = src.getSession();
        try{
            final File file = File.createTempFile("mgnl", null, Path.getTempDirectory());
            final FileOutputStream outStream = new FileOutputStream(file);
            session.exportSystemView(src.getPath(), outStream, false, false);
            outStream.flush();
            IOUtils.closeQuietly(outStream);
            FileInputStream inStream = new FileInputStream(file);
            session.importXML(
                    destParentPath,
                    inStream,
                    ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            IOUtils.closeQuietly(inStream);
            file.delete();
            if(!StringUtils.equals(src.getName(), destNodeName)){
                String currentPath;
                if(destParentPath.equals("/")){
                    currentPath = "/" + src.getName();
                }
                else{
                    currentPath = destParentPath + "/" + src.getName();
                }
                session.move(currentPath, dest);
            }
        }
        catch (IOException e) {
            throw new RepositoryException("Can't copy node " + src + " to " + dest, e);
        }
    }
}
