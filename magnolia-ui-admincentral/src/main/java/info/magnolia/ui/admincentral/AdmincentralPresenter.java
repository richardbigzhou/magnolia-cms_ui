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
import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.app.simple.DefaultLocationHistoryMapper;
import info.magnolia.ui.admincentral.shellapp.ShellAppController;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherShellApp;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesShellApp;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseShellApp;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.event.AdmincentralEventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.location.LocationHistoryHandler;
import info.magnolia.ui.framework.message.LocalMessageDispatcher;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.model.overlay.View;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.server.ErrorEvent;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * Presenter which starts up the components that make up Admincentral.
 */
public class AdmincentralPresenter {

    private final ShellImpl shell;

    private final MessagesManager messagesManager;

    @Inject
    public AdmincentralPresenter(final ShellImpl shell, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, final AppLauncherLayoutManager appLauncherLayoutManager, final LocationController locationController, final AppController appController, final ShellAppController shellAppController, final LocalMessageDispatcher messageDispatcher, MessagesManager messagesManager) {
        this.shell = shell;
        this.messagesManager = messagesManager;

        shellAppController.setViewport(this.shell.getShellAppViewport());
        shellAppController.addShellApp("applauncher", AppLauncherShellApp.class);
        shellAppController.addShellApp("pulse", PulseShellApp.class);
        shellAppController.addShellApp("favorite", FavoritesShellApp.class);

        appController.setViewport(shell.getAppViewport());

        DefaultLocationHistoryMapper locationHistoryMapper = new DefaultLocationHistoryMapper(appLauncherLayoutManager);
        LocationHistoryHandler locationHistoryHandler = new LocationHistoryHandler(locationHistoryMapper, shell);
        locationHistoryHandler.register(locationController, eventBus, new DefaultLocation(Location.LOCATION_TYPE_SHELL_APP, "applauncher", "", ""));

        messagesManager.registerMessagesListener(MgnlContext.getUser().getName(), messageDispatcher);

        UI.getCurrent().setErrorHandler(new ErrorHandler() {

            @Override
            public void error(ErrorEvent event) {
                Throwable e = event.getThrowable();
                String subject = e.getClass().getSimpleName();
                StringBuilder message = new StringBuilder(subject);
                if (e.getMessage() != null) {
                    message.append(": ");
                    message.append(e.getMessage());
                }
                addMessageDetails(message, e);
                AdmincentralPresenter.this.messagesManager.sendLocalMessage(new Message(MessageType.ERROR, subject, message.toString()));
            }

            private void addMessageDetails(StringBuilder message, Throwable e) {
                if (e == null || e == e.getCause()) {
                    return;
                }
                Throwable cause = e.getCause();

                if (e.getCause() instanceof InvocationTargetException) {
                    // add details for RPC exceptions
                    cause = ((InvocationTargetException) cause).getTargetException();
                }
                if (cause != null) {
                    message.append("\n");
                    message.append("caused by ");
                    message.append(cause.getClass().getSimpleName());
                    if (cause.getMessage() != null) {
                        message.append(": ");
                        message.append(cause.getMessage());
                    }
                }
                addMessageDetails(message, cause);
            }
        });
        VaadinSession.getCurrent().setErrorHandler(null);
    }

    public View start() {
        return shell.getMagnoliaShell();
    }
}
