/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.contentapp.setup.for5_3;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.NodeVisitorTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A migration task to move properties <i>workspace</i>, <i>path</i>, <i>includeProperties</i>, <i>includeSystemNodes</i>, <i>defaultOrder</i> and <i>nodeTypes</i>
 * from <i>subapp/workbench</i> or <i>subApp/editor</i> (only the property <i>workspace</i>) and adding them to a new node
 * <i>contentConnector</i> which is added to <i>workbench</i> or <i>editor</i>.<br/>
 * The property path is renamed to rootPath.
 * This task normally is not meant to be used standalone.
 *
 * @see {@link ContentAppMigrationTask}
 */
public class MigrateJcrPropertiesToContentConnectorTask extends NodeVisitorTask {

    private static final Logger log = LoggerFactory.getLogger(MigrateJcrPropertiesToContentConnectorTask.class);

    private static final String WORKBENCH_NODENAME = "workbench";
    private static final String EDITOR_NODENAME = "editor";
    private static final String CONTENTCONNECTOR_NODENAME = "contentConnector";
    private static final String PATH_PROPERTY = "path";
    private static final String ROOTPATH_PROPERTY = "rootPath";

    private final Set<Node> nodesToRemove = new HashSet<Node>();

    public MigrateJcrPropertiesToContentConnectorTask(String path) {
        super("Migrate properties workspace, path from workbench and migrate workspace from editor and add them to a node called contentConnector and add this new node to workbench respectively editor.",
                "Migrating properties workspace, path from workbench and migrating workspace from editor and adding them to a node called contentConnector and adding the new node to workbench respectively editor.",
                RepositoryConstants.CONFIG, path);
    }

    public MigrateJcrPropertiesToContentConnectorTask() {
        this("/");
    }

    @Override
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        super.doExecute(installContext);
        // this task removes some nodes, so we must do this after execution, not to mess with NodeVisitor while it is running
        if (!nodesToRemove.isEmpty()) {
            for (Node node : nodesToRemove) {
                node.remove();
            }
        }
    }

    @Override
    protected boolean nodeMatches(Node node) {
        try {
            return node.getPrimaryNodeType().getName().equals(NodeTypes.ContentNode.NAME) &&
                    (node.getName().equals(WORKBENCH_NODENAME) || node.getName().equals(EDITOR_NODENAME));
        } catch (RepositoryException e) {
            log.error("Couldn't evaluate visited node's name or node-type", e);
        }
        return false;
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {

        try {
            Node subAppNode = node.getParent();
            if (!"subApps".equals(subAppNode.getParent().getName()) && !subAppNode.hasProperty("subAppClass")) {
                return;
            }

            Node contentConnectorNode = subAppNode.hasNode(CONTENTCONNECTOR_NODENAME) ? subAppNode.getNode(CONTENTCONNECTOR_NODENAME) : subAppNode.addNode(CONTENTCONNECTOR_NODENAME, NodeTypes.ContentNode.NAME);

            // workbench
            if (WORKBENCH_NODENAME.equals(node.getName())) {
                migrateProperty("workspace", node, contentConnectorNode);
                migrateProperty("path", node, contentConnectorNode);
                migrateProperty("includeProperties", node, contentConnectorNode);
                migrateProperty("includeSystemNodes", node, contentConnectorNode);
                migrateProperty("defaultOrder", node, contentConnectorNode);
                migrateNode("nodeTypes", node, contentConnectorNode, installContext.getJCRSession(RepositoryConstants.CONFIG));
            }
            // editor
            else {
                migrateProperty("workspace", node, contentConnectorNode);
            }

            boolean nodeCleared = !node.getNodes().hasNext();
            for (final PropertyIterator it = node.getProperties(); nodeCleared && it.hasNext();) {
                final String propertyName = it.nextProperty().getName();
                nodeCleared = propertyName.startsWith(NodeTypes.MGNL_PREFIX) || propertyName.startsWith(NodeTypes.JCR_PREFIX) || propertyName.equals("extends");
            }

            if (nodeCleared) {
                // don't remove node right away, otherwise node-visitor gets confused
                nodesToRemove.add(node);
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException("Failed to migrate JCR-properties to contentConnector.",e);
        }
    }

    private void migrateNode(String nodeName, Node sourceNode, Node destNode, Session jcrSession) throws RepositoryException {
        if (sourceNode.hasNode(nodeName)) {
            if (destNode.hasNode(nodeName)) {
                destNode.getNode(nodeName).remove();
            }

            jcrSession.move(sourceNode.getNode(nodeName).getPath(), destNode.getPath() + "/" + nodeName);
        }
    }

    /*
     * Moving a property by a given name from the source node (workbench or editor) to the destination.-node (contentConnector).
     * If the attribute from the source-node is 'path', it will change its name to 'rootPath'.
     */
    private void migrateProperty(String propertyName, Node sourceNode, Node destNode) throws RepositoryException {
        if (sourceNode.hasProperty(propertyName)) {
            Property sourceNodeProperty = sourceNode.getProperty(propertyName);
            if (!PATH_PROPERTY.equals(propertyName)) {
                destNode.setProperty(propertyName, sourceNodeProperty.getString());
            } else {
                destNode.setProperty(ROOTPATH_PROPERTY, sourceNodeProperty.getString());
            }
            sourceNodeProperty.remove();
        }
    }

    @Override
    protected Predicate getFilteringPredicate() {
        return new AbstractPredicate<Node>() {

            @Override
            public boolean evaluateTyped(Node node) {
                try {
                    // after marking a node for removal, we interrupt visiting further down when we evaluate its direct sub-nodes.
                    if (nodesToRemove.contains(node.getParent())) {
                        return false;
                    }
                } catch (RepositoryException e) {
                    log.warn("Couldn't get parent of visited node, not filtering subtree for NodeVisitor.");
                }
                return true;
            }
        };
    }
}
