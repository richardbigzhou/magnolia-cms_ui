/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.dialog.setup;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.dialog.setup.migration.CheckBoxRadioControlMigration;
import info.magnolia.ui.dialog.setup.migration.CheckBoxSwitchControlMigration;
import info.magnolia.ui.dialog.setup.migration.ControlMigration;
import info.magnolia.ui.dialog.setup.migration.DamControlMigration;
import info.magnolia.ui.dialog.setup.migration.DateControlMigration;
import info.magnolia.ui.dialog.setup.migration.EditControlMigration;
import info.magnolia.ui.dialog.setup.migration.FckEditControlMigration;
import info.magnolia.ui.dialog.setup.migration.FileControlMigration;
import info.magnolia.ui.dialog.setup.migration.HiddenControlMigration;
import info.magnolia.ui.dialog.setup.migration.LinkControlMigration;
import info.magnolia.ui.dialog.setup.migration.MultiSelectControlMigration;
import info.magnolia.ui.dialog.setup.migration.SelectControlMigration;
import info.magnolia.ui.dialog.setup.migration.StaticControlMigration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
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
public class DialogMigrationTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(DialogMigrationTask.class);
    private final String moduleName;
    private final HashSet<Property> extendsAndReferenceProperty = new HashSet<Property>();
    private HashMap<String, ControlMigration> controlMigrationMap;

    public DialogMigrationTask(String moduleName) {
        super("Dialog Migration for 5.x", "Migrate dialog for the following module: " + moduleName);
        this.moduleName = moduleName;
        this.controlMigrationMap = new HashMap<String, ControlMigration>();
    }

    /**
     * Handle all Dialogs registered and migrate them.
     */
    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            this.controlMigrationMap = getCustomMigrationTask();
            String dialogNodeName = "dialogs";
            String dialogPath = "/modules/" + moduleName + "/" + dialogNodeName;

            Session session = installContext.getJCRSession(RepositoryConstants.CONFIG);
            Node dialog = session.getNode(dialogPath);
            // Copy to Dialog50
            String newDialogPath = dialog.getPath() + "50";
            copyInSession(dialog, newDialogPath);
            NodeUtil.visit(dialog, new NodeVisitor() {
                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node dialogNode : NodeUtil.getNodes(current, NodeTypes.ContentNode.NAME)) {
                        performDialogMigration(dialogNode);
                    }
                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME));
            session.removeItem(dialogPath);
            session.move(newDialogPath, dialogPath);

            // Try to resolve references for extends.
            postProcessForExtendsAndReference();

        } catch (Exception e) {
            log.error("", e);
            installContext.warn("Could not Migrate Dialog for the following module " + moduleName);
        }
    }

    /**
     * Initialize the map used to migrate controls to Fields.<br>
     * Each control is link to a specific {@link ControlMigration}.
     * <b>Override this class in order to add your own controls migration task.</b>
     */
    protected HashMap<String, ControlMigration> getCustomMigrationTask() {
        HashMap<String, ControlMigration> customMigrationTask = new HashMap<String, ControlMigration>();
        customMigrationTask.put("edit", new EditControlMigration());
        customMigrationTask.put("fckEdit", new FckEditControlMigration());
        customMigrationTask.put("date", new DateControlMigration());
        customMigrationTask.put("select", new SelectControlMigration());
        customMigrationTask.put("checkbox", new CheckBoxRadioControlMigration(true));
        customMigrationTask.put("checkboxSwitch", new CheckBoxSwitchControlMigration());
        customMigrationTask.put("radio", new CheckBoxRadioControlMigration(false));
        customMigrationTask.put("dam", new DamControlMigration());
        customMigrationTask.put("uuidLink", new LinkControlMigration());
        customMigrationTask.put("link", new LinkControlMigration());
        customMigrationTask.put("categorizationUUIDMultiSelect", new MultiSelectControlMigration(true));
        customMigrationTask.put("multiselect", new MultiSelectControlMigration(false));
        customMigrationTask.put("file", new FileControlMigration());
        customMigrationTask.put("static", new StaticControlMigration());
        customMigrationTask.put("hidden", new HiddenControlMigration());

        return customMigrationTask;
    }

    /**
     * Handle and Migrate a Dialog node.
     */
    private void performDialogMigration(Node dialog) throws RepositoryException {
        // Get child Nodes (should be Tab)
        Iterable<Node> tabNodes = NodeUtil.getNodes(dialog, DIALOG_FILTER);
        if (tabNodes.iterator().hasNext()) {
            // Check if it's a tab definition
            if (dialog.hasProperty("controlType") && dialog.getProperty("controlType").getString().equals("tab")) {
                handleTab(dialog);
            } else {
                // Handle action
                if (!dialog.hasProperty("controlType") && !dialog.hasProperty("extends") && !dialog.hasProperty("reference")) {
                    handleAction(dialog);
                }
                // Handle tab
                handleTabs(dialog, tabNodes.iterator());
            }
            // Remove class property defined on Dialog level
            if (dialog.hasProperty("class")) {
                dialog.getProperty("class").remove();
            }
        } else {
            // Handle as a field.
            handleField(dialog);
        }

        handleExtendsAndReference(dialog);
    }

    /**
     * Add action to node.
     */
    private void handleAction(Node dialog) throws RepositoryException {
        dialog.addNode("actions", NodeTypes.ContentNode.NAME);

        Node node = dialog.getNode("actions");

        node.addNode("commit", NodeTypes.ContentNode.NAME).setProperty("label", "save changes");
        node.addNode("cancel", NodeTypes.ContentNode.NAME).setProperty("label", "cancel");
        node.getNode("commit").setProperty("class", "info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition");
        node.getNode("cancel").setProperty("class", "info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition");

    }

    /**
     * Handle Tabs.
     */
    private void handleTabs(Node dialog, Iterator<Node> tabNodes) throws RepositoryException {
        Node form = dialog.addNode("form", NodeTypes.ContentNode.NAME);
        handleFormLabels(dialog, form);
        Node dialogTabs = form.addNode("tabs", NodeTypes.ContentNode.NAME);
        while (tabNodes.hasNext()) {
            Node tab = tabNodes.next();
            // Handle Fields Tab
            handleTab(tab);
            // Move tab
            NodeUtil.moveNode(tab, dialogTabs);
        }
    }

    /**
     * Move the label property from dialog to form node.
     */
    private void handleFormLabels(Node dialog, Node form) throws RepositoryException {
        moveAndRenameLabelProperty(dialog, form, "label");
        moveAndRenameLabelProperty(dialog, form, "i18nBasename");
        moveAndRenameLabelProperty(dialog, form, "description");
    }

    /**
     * Move the desired property if present from the source to the target node.
     */
    private void moveAndRenameLabelProperty(Node source, Node target, String propertyName) throws RepositoryException {
        if (source.hasProperty(propertyName)) {
            Property dialogProperty = source.getProperty(propertyName);
            target.setProperty(propertyName, dialogProperty.getString());
            dialogProperty.remove();
        }
    }

    /**
     * Handle a Tab.
     */
    private void handleTab(Node tab) throws RepositoryException {
        if ((tab.hasProperty("controlType") && StringUtils.equals(tab.getProperty("controlType").getString(), "tab")) || (tab.getParent().hasProperty("extends"))) {
            if (tab.hasProperty("controlType") && StringUtils.equals(tab.getProperty("controlType").getString(), "tab")) {
                // Remove controlType Property
                tab.getProperty("controlType").remove();
            }
            // get all controls to be migrated
            Iterator<Node> controls = NodeUtil.getNodes(tab, NodeTypes.ContentNode.NAME).iterator();
            // create a fields Node
            Node fields = tab.addNode("fields", NodeTypes.ContentNode.NAME);

            while (controls.hasNext()) {
                Node control = controls.next();
                // Handle fields
                handleField(control);
                // Move to fields
                NodeUtil.moveNode(control, fields);
            }
        } else if (tab.hasNode("inheritable")) {
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
        if (fieldNode.hasProperty("controlType")) {
            String controlTypeName = fieldNode.getProperty("controlType").getString();

            if (controlMigrationMap.containsKey(controlTypeName)) {
                ControlMigration controlMigration = controlMigrationMap.get(controlTypeName);
                controlMigration.migrate(fieldNode);
            } else {
                fieldNode.setProperty("class", "info.magnolia.ui.form.field.definition.StaticFieldDefinition");
                if (!fieldNode.hasProperty("value")) {
                    fieldNode.setProperty("value", "Field not yet supported");
                }
                log.warn("No dialog define for control '{}' for node '{}'", controlTypeName, fieldNode.getPath());
            }
        } else {
            // Handle Field Extends/Reference
            handleExtendsAndReference(fieldNode);
        }
    }


    private void handleExtendsAndReference(Node node) throws RepositoryException {
        if (node.hasProperty("extends")) {
            // Handle Field Extends
            extendsAndReferenceProperty.add(node.getProperty("extends"));
        } else if (node.hasProperty("reference")) {
            // Handle Field Extends
            extendsAndReferenceProperty.add(node.getProperty("reference"));
        }
    }

    /**
     * Create a specific node filter.
     */
    private static AbstractPredicate<Node> DIALOG_FILTER = new AbstractPredicate<Node>() {
        @Override
        public boolean evaluateTyped(Node node) {
            try {
                return !node.getName().startsWith(NodeTypes.JCR_PREFIX)
                        && !NodeUtil.isNodeType(node, NodeTypes.MetaData.NAME) &&
                        NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME);
            } catch (RepositoryException e) {
                return false;
            }
        }
    };

    /**
     * Check if the extends and reference are correct. If not try to do the best
     * to found a correct path.
     */
    private void postProcessForExtendsAndReference() throws RepositoryException {
        for (Property p : extendsAndReferenceProperty) {
            String path = p.getString();
            if (path.equals("override")) {
                continue;
            }
            if (!p.getSession().nodeExists(path)) {

                String newPath = insertBeforeLastSlashAndTest(p.getSession(), path, "/tabs", "/fields", "/tabs/fields", "/form/tabs");
                if (newPath != null) {
                    p.setValue(newPath);
                    continue;
                }

                // try to add a tabs before the 2nd last /
                String beging = path.substring(0, path.lastIndexOf("/"));
                String end = path.substring(beging.lastIndexOf("/"));
                beging = beging.substring(0, beging.lastIndexOf("/"));
                newPath = beging + "/form/tabs" + end;
                if (p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                    continue;
                }
                // try with a fields before the last / with a tabs before the 2nd last /
                newPath = insertBeforeLastSlash(newPath, "/fields");
                if (p.getSession().nodeExists(newPath)) {
                    p.setValue(newPath);
                } else {
                    log.warn("reference to " + path + " not found");
                }
            }
        }
    }

    /**
     * Test insertBeforeLastSlash() for all toInserts.
     * If newPath exist as a node return it.
     */
    private String insertBeforeLastSlashAndTest(Session session, String reference, String... toInserts) throws RepositoryException {
        String res = null;
        for (String toInsert : toInserts) {
            String newPath = insertBeforeLastSlash(reference, toInsert);
            if (session.nodeExists(newPath)) {
                return newPath;
            }
        }
        return res;
    }

    /**
     * Insert the toInsert ("/tabs") before the last /.
     */
    private String insertBeforeLastSlash(String reference, String toInsert) {
        String beging = reference.substring(0, reference.lastIndexOf("/"));
        String end = reference.substring(reference.lastIndexOf("/"));
        return beging + toInsert + end;
    }

    /**
     * Session based copy operation. As JCR only supports workspace based copies this operation is performed.
     * by using export import operations.
     */
    private void copyInSession(Node src, String dest) throws RepositoryException {
        final String destParentPath = StringUtils.defaultIfEmpty(StringUtils.substringBeforeLast(dest, "/"), "/");
        final String destNodeName = StringUtils.substringAfterLast(dest, "/");
        final Session session = src.getSession();
        try {
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
            if (!StringUtils.equals(src.getName(), destNodeName)) {
                String currentPath;
                if (destParentPath.equals("/")) {
                    currentPath = "/" + src.getName();
                } else {
                    currentPath = destParentPath + "/" + src.getName();
                }
                session.move(currentPath, dest);
            }
        } catch (IOException e) {
            throw new RepositoryException("Can't copy node " + src + " to " + dest, e);
        }
    }
}
