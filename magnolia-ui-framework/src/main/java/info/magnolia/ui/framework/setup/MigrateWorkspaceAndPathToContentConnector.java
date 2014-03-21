/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.framework.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A migration task to move.
 * <ul>
 * <li>properties workspace, path from workbench and add the to a new node called contentConnector which is added to workbench</li>
 * <li>property workspace from Editor to a new node called contentConnector which is added to Editor </li>
 * </ul>
 */
public class MigrateWorkspaceAndPathToContentConnector extends QueryTask {


    public static final String WORKBENCH_NODENAME = "workbench";
    public static final String EDITOR_NODENAME = "editor";
    private static final String CONTENTCONNECTOR_NODENAME = "contentConnector";

    private static final String QUERY = " select * from [mgnl:contentNode] as t where name(t) = '" + WORKBENCH_NODENAME + "' or  name(t) = '" + EDITOR_NODENAME + "'";
    public static final String SUB_APP_CLASS_PROPERTY = "subAppClass";

    private Logger log = LoggerFactory.getLogger(getClass());


    public MigrateWorkspaceAndPathToContentConnector() {
        super("Migrate properties workspace, path from workbench and migrate workspace from editor and add them to a node called contentConnector and add this new node to workbench respectively editor.",
                "Migrating properties workspace, path from workbench and migrating workspace from editor and adding them to a node called contentConnector and adding the new node to workbench respectively editor.",
                RepositoryConstants.CONFIG, QUERY);
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {

        try {
            Node subAppNode = node.getParent();
            if (!subAppNode.hasProperty(SUB_APP_CLASS_PROPERTY)) {
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
        } catch (RepositoryException e) {
            log.error("Unable to process app node ", e);
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

    private void migrateProperty(String propertyName, Node sourceNode, Node destNode) throws RepositoryException {
        if (sourceNode.hasProperty(propertyName)) {
            Property sourceNodeProperty = sourceNode.getProperty(propertyName);
            destNode.setProperty(propertyName, sourceNodeProperty.getString());
            sourceNodeProperty.remove();
        } else {
            log.info("Found node in " + RepositoryConstants.CONFIG + ") which is eventually not properly configured: Could not find both path and workspace. Did not migrate this node; configure it manually if required.");
        }
    }
}
