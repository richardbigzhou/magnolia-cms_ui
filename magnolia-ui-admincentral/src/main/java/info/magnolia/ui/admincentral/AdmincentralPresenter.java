/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import info.magnolia.event.EventBus;
import info.magnolia.ui.framework.app.DefaultLocationHistoryMapper;
import info.magnolia.ui.admincentral.shellapp.ShellAppController;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherShellApp;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesShellApp;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseShellApp;
import info.magnolia.ui.admincentral.usermenu.UserMenuPresenter;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.location.LocationHistoryHandler;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ShellImpl;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * Presenter which starts up the components that make up Admincentral.
 */
public class AdmincentralPresenter {

    private final ShellImpl shell;

    @Inject
    public AdmincentralPresenter(final ShellImpl shell, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, final AppLauncherLayoutManager appLauncherLayoutManager, final LocationController locationController, final AppController appController, final ShellAppController shellAppController, MessagesManager messagesManager, UserMenuPresenter userMenu) {
        this.shell = shell;

        shell.setUserMenu(userMenu.start());

        shellAppController.setViewport(this.shell.getShellAppViewport());
        shellAppController.addShellApp("applauncher", AppLauncherShellApp.class);
        shellAppController.addShellApp("pulse", PulseShellApp.class);
        shellAppController.addShellApp("favorite", FavoritesShellApp.class);

        appController.setViewport(shell.getAppViewport());

        DefaultLocationHistoryMapper locationHistoryMapper = new DefaultLocationHistoryMapper(appLauncherLayoutManager);
        LocationHistoryHandler locationHistoryHandler = new LocationHistoryHandler(locationHistoryMapper, shell);
        locationHistoryHandler.register(locationController, eventBus, new DefaultLocation(Location.LOCATION_TYPE_SHELL_APP, "applauncher", "", ""));

        UI.getCurrent().setErrorHandler(new AdmincentralErrorHandler(messagesManager));
        VaadinSession.getCurrent().setErrorHandler(null);
    }

    public View start() {
        return shell.getMagnoliaShell();
    }
}
