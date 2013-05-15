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
package info.magnolia.ui.app.pages.main;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.contentapp.browser.BrowserPresenter;
import info.magnolia.ui.contentapp.browser.BrowserSubApp;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main SubApp for Pages - the Browser.
 */
public class PagesMainSubApp extends BrowserSubApp {

    private static final Logger log = LoggerFactory.getLogger(PagesMainSubApp.class);

    @Inject
    public PagesMainSubApp(ActionExecutor actionExecutor, final SubAppContext subappContext, ContentSubAppView view, BrowserPresenter workbench, @Named(SubAppEventBus.NAME) EventBus subAppEventBus) {
        super(actionExecutor, subappContext, view, workbench, subAppEventBus);
    }

    @Override
    public void updateActionbar(ActionbarPresenter actionbar) {

        final String selectedItemId = getBrowser().getSelectedItemId();

        try {
            Item jcrItem = null;
            BrowserSubAppDescriptor subAppDescriptor = (BrowserSubAppDescriptor) getSubAppContext().getSubAppDescriptor();
            WorkbenchDefinition workbench = subAppDescriptor.getWorkbench();
            String workbenchRootItemId = JcrItemUtil.getItemId(JcrItemUtil.getNode(workbench.getWorkspace(), workbench.getPath()));
            if (selectedItemId != null && !selectedItemId.equals(workbenchRootItemId)) {
                jcrItem = JcrItemUtil.getJcrItem(workbench.getWorkspace(), selectedItemId);
            }

            // actions currently always disabled
            actionbar.disable("move", "duplicate");

            // actions disabled based on selection
            final String[] defaultActions = new String[] { "delete", "preview", "edit", "export", "activate", "deactivate", "activateRecursive" };

            if (jcrItem == null) {
                actionbar.disable(defaultActions);
            } else {
                actionbar.enable(defaultActions);
                final Node page = (Node) jcrItem;
                // if it's a leaf recursive activation should not be available.
                if (isLeaf(page)) {
                    actionbar.disable("activateRecursive");
                }
            }
        } catch (RepositoryException e) {
            log.warn("Unable to determine node type of {}", selectedItemId);
        }
    }

    private boolean isLeaf(final Node node) {
        try {
            return !NodeUtil.getNodes(node, NodeTypes.Page.NAME).iterator().hasNext();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
