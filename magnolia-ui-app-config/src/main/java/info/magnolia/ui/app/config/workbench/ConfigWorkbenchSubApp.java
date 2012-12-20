/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.config.workbench;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.AbstractContentSubApp;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.EventBus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Configuration Workbench SubApp.
 */
public class ConfigWorkbenchSubApp extends AbstractContentSubApp {

    private static final Logger log = LoggerFactory.getLogger(ConfigWorkbenchSubApp.class);

    @Inject
    public ConfigWorkbenchSubApp(final SubAppContext subAppContext, ConfigWorkbenchView view, ContentWorkbenchPresenter workbench, @Named("subapp") EventBus subAppEventBus) {
        super(subAppContext, view, workbench, subAppEventBus);
    }

    @Override
    public void updateActionbar(final ActionbarPresenter actionbar) {
        final String selectedItemId = getWorkbench().getSelectedItemId();

        if (selectedItemId == null || "/".equals(selectedItemId)) {
            actionbar.disable("delete");
        } else {
            actionbar.enable("addFolder", "addNode", "addProperty", "delete");

            try {
                final Session session = MgnlContext.getJCRSession("config");
                final boolean isProperty = session.propertyExists(selectedItemId);

                if(isProperty) {
                    actionbar.disable("addFolder", "addNode", "addProperty");
                    return;
                }
                final Node node = session.getNode(selectedItemId);
                if(NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME)) {
                    actionbar.disable("addFolder");
                }

            }  catch (RepositoryException e) {
                log.warn("An error occurred while trying to determine the node type for item [{}]", selectedItemId, e);
            }
        }
    }
}
