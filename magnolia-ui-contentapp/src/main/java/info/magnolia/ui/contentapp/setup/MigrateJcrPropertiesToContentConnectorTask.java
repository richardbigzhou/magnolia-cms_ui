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
package info.magnolia.ui.contentapp.setup;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A migration task to move properties <i>workspace</i>, <i>path</i>, <i>includeProperties</i>, <i>includeSystemNodes</i>, <i>defaultOrder</i> and <i>nodeTypes</i>
 * from <i>subapp/workbench</i> or <i>subApp/editor</i> (only the property <i>workspace</i>)  and add them to a new node
 * <i>contentConnector</i> which is added to <i>workbench</i> or <i>editor</i>.<br/>
 * The property path is renamed to rootPath.
 */
public class MigrateJcrPropertiesToContentConnectorTask extends QueryTask {


    private static final String WORKBENCH_NODENAME = "workbench";
    private static final String EDITOR_NODENAME = "editor";
    private static final String CONTENTCONNECTOR_NODENAME = "contentConnector";
    private static final String PATH_PROPERTY = "path";
    private static final String ROOTPATH_PROPERTY = "rootPath";

    private static final String QUERY = " select * from [mgnl:contentNode] as t where name(t) = '" + WORKBENCH_NODENAME + "' or  name(t) = '" + EDITOR_NODENAME + "'";
    public static final String SUB_APP_CLASS_PROPERTY = "subAppClass";


    protected MigrateJcrPropertiesToContentConnectorTask() {
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
                migrateProperty("workspace", node, contentConnectorNode, installContext);
                migrateProperty("path", node, contentConnectorNode, installContext);
                migrateProperty("includeProperties", node, contentConnectorNode, installContext);
                migrateProperty("includeSystemNodes", node, contentConnectorNode, installContext);
                migrateProperty("defaultOrder", node, contentConnectorNode, installContext);
                migrateNode("nodeTypes", node, contentConnectorNode, installContext.getJCRSession(RepositoryConstants.CONFIG));
            }
            // editor
            else {
                migrateProperty("workspace", node, contentConnectorNode, installContext);
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

    private void migrateProperty(String propertyName, Node sourceNode, Node destNode, InstallContext installContext) throws RepositoryException {
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
}
