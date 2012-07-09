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
package info.magnolia.ui.admincentral;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.app.simple.DefaultLocationHistoryMapper;
import info.magnolia.ui.framework.message.LocalMessageDispatcher;
import info.magnolia.ui.admincentral.app.simple.ShellAppController;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.location.LocationHistoryHandler;
import info.magnolia.ui.framework.message.MessagesManager;

import javax.inject.Inject;

import com.vaadin.ui.Window;

/**
 * Presenter meant to bootstrap the MagnoliaShell.
 */
public class MagnoliaShellPresenter implements MagnoliaShellView.Presenter {
    
    private final MagnoliaShellView view;

    @Inject
    public MagnoliaShellPresenter(final MagnoliaShellView view, final EventBus eventBus, final AppLayoutManager appLayoutManager, final LocationController locationController, final AppController appController, final ShellAppController shellAppController, final LocalMessageDispatcher messageDispatcher, MessagesManager messagesManager) {
        this.view = view;
        this.view.setPresenter(this);

        shellAppController.setViewPort(view.getRoot().getShellAppViewport());

        appController.setViewPort(view.getRoot().getAppViewport());

        DefaultLocationHistoryMapper locationHistoryMapper = new DefaultLocationHistoryMapper(appLayoutManager);
        LocationHistoryHandler locationHistoryHandler = new LocationHistoryHandler(locationHistoryMapper, view.getRoot());
        locationHistoryHandler.register(locationController, eventBus, new DefaultLocation(DefaultLocation.LOCATION_TYPE_SHELL_APP, "applauncher", ""));
        messagesManager.registerMessagesListener(MgnlContext.getUser().getName(), messageDispatcher);
    }

    public void start(final Window window) {
        final MagnoliaShell shell = view.getRoot();
        shell.setSizeFull();
        window.addComponent(shell);
    }
}
