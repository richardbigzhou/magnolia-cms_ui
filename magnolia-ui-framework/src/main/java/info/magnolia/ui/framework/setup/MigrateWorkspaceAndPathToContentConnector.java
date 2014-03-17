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

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A migration task to move
 * <ul>
 * <li>properties workspace, path from workbench and add the to a new node called contentConnector which is added to workbench</li>
 * <li>property workspace from Editor to a new node called contentConnector which is added to Editor </li>
 * </ul>
 */
public class MigrateWorkspaceAndPathToContentConnector extends QueryTask {


    private static final String WORKBENCH_NODENAME = "workbench";
    private static final String EDITOR_NODENAME = "editor";
    private static final String CONTENTCONNECTOR_NODENAME = "contentConnector";

    private static final String WORSPACE_PROPERTY = "workspace";
    private static final String PATH_PROPERTY = "path";
    private static final String QUERY = " select * from [mgnl:contentNode] as t where name(t) = '" + WORKBENCH_NODENAME + "' or  name(t) = '" + EDITOR_NODENAME + "'";

    private Logger log = LoggerFactory.getLogger(getClass());


    public MigrateWorkspaceAndPathToContentConnector() {
        super("Migrate properties workspace, path from workbench and migrate workspace from editor and add them to a node called contentConnector and add this new node to workbench respectively editor.",
                "Migrating properties workspace, path from workbench and migrating workspace from editor and adding them to a node called contentConnector and adding the new node to workbench respectively editor.",
                RepositoryConstants.CONFIG, QUERY);
    }

    @Override
    protected void operateOnNode(InstallContext installContext, Node node) {

        try {
            String workspace = null;
            String path = null;

            // workbench
            if (node.hasProperty(WORSPACE_PROPERTY)) {
                workspace = node.getProperty(WORSPACE_PROPERTY).getString();
            }

            if (WORKBENCH_NODENAME.equals(node.getName())) {
                if (node.hasProperty(PATH_PROPERTY)) {
                    path = node.getProperty(PATH_PROPERTY).getString();
                }
                if (StringUtils.isNotBlank(path) && StringUtils.isNotBlank(workspace)) {
                    Node contentConnector = node.getParent().addNode(CONTENTCONNECTOR_NODENAME, "mgnl:contentNode");
                    contentConnector.setProperty(WORKBENCH_NODENAME, workspace);
                    contentConnector.setProperty(PATH_PROPERTY, path);
                } else {
                    log.info("Found " + WORKBENCH_NODENAME + "-node (" + node.getPath() + "@" + node.getSession().getWorkspace() + ") which is eventually not properly configured: Could not find both path and workspace. Did not migrate this node; configure it manually if required.");
                }

            }

            // editor
            else {
                if (StringUtils.isNotBlank(workspace)) {
                    Node contentConnector = node.getParent().addNode(CONTENTCONNECTOR_NODENAME, "mgnl:contentNode");
                    contentConnector.setProperty(WORKBENCH_NODENAME, workspace);
                } else {
                    log.info("Found " + EDITOR_NODENAME + "-node (" + node.getPath() + "@" + node.getSession().getWorkspace() + ") which is eventually not properly configured: missing workbench. Did not migrate this node; configure it manually; configure it manually if required.");
                }
            }


        } catch (RepositoryException e) {
            log.error("Unable to process app node ", e);
        }

    }
}
