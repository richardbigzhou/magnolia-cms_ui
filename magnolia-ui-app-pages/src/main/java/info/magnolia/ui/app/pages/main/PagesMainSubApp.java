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
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.contentapp.browser.BrowserPresenter;
import info.magnolia.ui.contentapp.browser.BrowserSubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppEventBus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

/**
 * PagesMainSubApp.
 */
public class PagesMainSubApp extends BrowserSubApp {

    @Inject
    public PagesMainSubApp(ActionExecutor actionExecutor, final SubAppContext subappContext, ContentSubAppView view, BrowserPresenter workbench, @Named(SubAppEventBus.NAME) EventBus subAppEventBus) {
        super(actionExecutor, subappContext, view, workbench, subAppEventBus);
    }

    @Override
    public void updateActionbar(ActionbarPresenter actionbar) {

        // actions currently always disabled
        actionbar.disable("move", "duplicate");

        // actions disabled based on selection
        final String[] defaultPageActions = new String[] { "delete", "preview", "edit", "export", "activate", "deactivate", "activateRecursive" };
        final String[] defaultPageDeleteActions = new String[] { "activate", "activateRecursive", "showPreviousVersion", "restorePreviousVersion" };

        if (getBrowser().getSelectedItemId() == null || "/".equals(getBrowser().getSelectedItemId())) {
            actionbar.hideSection("pageDeleteActions");
            actionbar.showSection("pageActions");
            actionbar.disable(defaultPageActions);
        } else {

            final String path = getBrowser().getSelectedItemId();
            final String workspace = getBrowser().getWorkspace();
            final Node page = SessionUtil.getNode(workspace, path);

            // if it's deleted, display the deleted section
            if (isDeleted(page)) {
                actionbar.showSection("pageDeleteActions");
                actionbar.hideSection("pageActions");
                actionbar.disable(defaultPageActions);
                actionbar.disable("add");
                actionbar.enable(defaultPageDeleteActions);

            } else {
                actionbar.hideSection("pageDeleteActions");
                actionbar.showSection("pageActions");
                actionbar.enable(defaultPageActions);
            }
            // if it's a leaf recursive activation should not be available.
            if (isLeaf(page)) {
                actionbar.disable("activateRecursive");
            }
        }
    }

    private boolean isLeaf(final Node node) {
        try {
            return !NodeUtil.getNodes(node, NodeTypes.Page.NAME).iterator().hasNext();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public static boolean isDeleted(final Node node) {
        try {
            for (NodeType nodeType:node.getMixinNodeTypes()) {
                if (NodeTypes.Deleted.NAME.equals(nodeType.getName())) {
                    return true;
                }
            }
            return false;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
