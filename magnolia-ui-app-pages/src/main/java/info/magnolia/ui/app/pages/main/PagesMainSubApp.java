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

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.AbstractContentSubApp;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.instantpreview.InstantPreviewDispatcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * PagesMainSubApp.
 */
public class PagesMainSubApp extends AbstractContentSubApp implements PagesMainView.Listener {

    private final PagesMainView view;

    private final InstantPreviewDispatcher dispatcher;

    @Inject
    public PagesMainSubApp(final SubAppContext subappContext, PagesMainView view, ContentWorkbenchPresenter workbench, @Named("subapp") EventBus subAppEventBus, InstantPreviewDispatcher dispatcher) {
        super(subappContext, view, workbench, subAppEventBus);
        this.view = view;
        this.view.setListener(this);
        this.dispatcher = dispatcher;
    }

    @Override
    public void updateActionbar(ActionbarPresenter actionbar) {

        // actions currently always disabled
        actionbar.disable("move", "duplicate");

        // actions disabled based on selection
        final String[] defaultActions = new String[] { "delete", "preview", "edit", "export", "activate", "deactivate", "activateRecursive" };
        if (getWorkbench().getSelectedItemId() == null || "/".equals(getWorkbench().getSelectedItemId())) {
            actionbar.disable(defaultActions);
        } else {
            actionbar.enable(defaultActions);
            final String path = getWorkbench().getSelectedItemId();
            final String workspace = getWorkbench().getWorkspace();
            final Node page = SessionUtil.getNode(workspace, path);
            updateActivationActions(page, actionbar);
        }
    }

    @Override
    public void share() {
        dispatcher.share();
    }

    @Override
    public void subscribe(String hostId) {
        dispatcher.subscribeTo(hostId);
    }

    private void updateActivationActions(final Node node, final ActionbarPresenter actionbarPresenter) {
        try {
            // if it's a leaf recursive activation makes no sense
            if (!NodeUtil.getNodes(node, NodeTypes.Page.NAME).iterator().hasNext()) {
                actionbarPresenter.disable("activateRecursive");
            }

            int status = NodeTypes.Activatable.getActivationStatus(node);

            switch (status) {
            case NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED:
                actionbarPresenter.disable("activate");
                break;
            case NodeTypes.Activatable.ACTIVATION_STATUS_MODIFIED:
                // TODO fgrilli what do we do in this case?
                break;
            case NodeTypes.Activatable.ACTIVATION_STATUS_NOT_ACTIVATED:
                actionbarPresenter.disable("deactivate");
                break;
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

    }
}
