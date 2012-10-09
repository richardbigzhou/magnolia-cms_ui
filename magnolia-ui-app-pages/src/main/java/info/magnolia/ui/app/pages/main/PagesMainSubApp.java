/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.AbstractContentSubApp;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.instantpreview.InstantPreviewDispatcher;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * PagesMainSubApp.
 */
public class PagesMainSubApp extends AbstractContentSubApp implements PagesMainView.Listener {

    private static final String CAPTION = "Pages";

    private final PagesMainView view;

    private final InstantPreviewDispatcher dispatcher;

    @Inject
    public PagesMainSubApp(final AppContext appContext, PagesMainView view, ContentWorkbenchPresenter workbench, @Named("subapp") EventBus subAppEventBus, InstantPreviewDispatcher dispatcher) {
        super(appContext,view, workbench, subAppEventBus);
        this.view = view;
        this.view.setListener(this);
        this.dispatcher = dispatcher;
    }
    
    @Override
    public String getCaption() {
        return CAPTION;
    }

    @Override
    public void updateActionbar(final ActionbarPresenter actionbar) {
        super.updateActionbar(actionbar);

        // actions currently always disabled
        actionbar.disable("move");
        actionbar.disable("duplicate");

        // actions disabled based on selection
        if (getWorkbench().getSelectedItemId() == null || getWorkbench().getSelectedItemId().equals("/")) {
            actionbar.disable("preview");
            actionbar.disable("editProperties");
            actionbar.disable("export");
        } else {
            actionbar.enable("preview");
            actionbar.enable("editProperties");
            actionbar.enable("export");
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
}
