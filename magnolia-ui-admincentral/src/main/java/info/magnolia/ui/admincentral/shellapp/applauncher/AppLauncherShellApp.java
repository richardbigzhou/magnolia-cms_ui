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
package info.magnolia.ui.admincentral.shellapp.applauncher;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.event.SystemEventBus;
import info.magnolia.ui.admincentral.shellapp.ShellApp;
import info.magnolia.ui.admincentral.shellapp.ShellAppContext;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.AppLifecycleEvent;
import info.magnolia.ui.api.app.AppLifecycleEventHandler;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherGroup;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherGroupEntry;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayout;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutChangedEvent;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutChangedEventHandler;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.server.ClientConnector;

/**
 * App launcher shell app. Listen to: system EventBus: LayoutEvent. Reload
 * the Layout by getting the latest available App from the
 * {AppLauncherLayoutManager}. LocalEventBus : App started and App stop event.
 * In this case, update the App button to indicate if an App is started or
 * stopped.
 */
public class AppLauncherShellApp implements ShellApp, AppLauncherView.Presenter {

    private final AppLauncherView view;
    private final AppLauncherLayoutChangedEventHandler handler;

    private AppLauncherLayoutManager appLauncherLayoutManager;

    private AppController appController;

    private Context context;

    private Shell shell;

    /**
     * @deprecated since 5.4 - use the c-tor which injects the {@link Context} instead.
     */
    @Deprecated
    public AppLauncherShellApp(Shell shell, AppLauncherView view, AppController appController, AppLauncherLayoutManager appLauncherLayoutManager, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, @Named(SystemEventBus.NAME) EventBus systemEventBus) {
        this(shell, view, appController, appLauncherLayoutManager, admincentralEventBus, systemEventBus, MgnlContext.getInstance());
    }

    @Inject
    public AppLauncherShellApp(Shell shell, AppLauncherView view, AppController appController, AppLauncherLayoutManager appLauncherLayoutManager, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, @Named(SystemEventBus.NAME) EventBus systemEventBus, Context context) {
        this.view = view;
        this.shell = shell;
        this.appController = appController;
        this.appLauncherLayoutManager = appLauncherLayoutManager;
        this.context = context;

        // Init view
        initView(appLauncherLayoutManager.getLayoutForCurrentUser());
        /**
         * Handle ReloadAppEvent.
         */
        handler = new AppLauncherLayoutChangedEventHandler() {

            @Override
            public void onAppLayoutChanged(AppLauncherLayoutChangedEvent event) {
                // Reload Layout
                reloadLayout();
            }
        };

        final HandlerRegistration systemRegistration = systemEventBus.addHandler(AppLauncherLayoutChangedEvent.class, handler);

        /**
         * Add Handler of type AppLifecycleEventHandler in order to catch stop
         * and start App events.
         */
        admincentralEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            /**
             * Deactivate the visual triangle on the App Icon.
             */
            @Override
            public void onAppStopped(AppLifecycleEvent event) {
                AppLauncherLayout layout = AppLauncherShellApp.this.appLauncherLayoutManager.getLayoutForCurrentUser();
                if (layout.containsApp(event.getAppDescriptor().getName())) {
                    activateButton(false, event.getAppDescriptor().getName());
                }
            }

            /**
             * Activate the visual triangle on the App Icon.
             */
            @Override
            public void onAppStarted(AppLifecycleEvent event) {
                activateButton(true, event.getAppDescriptor().getName());
            }
        });
        // Remove handler once the view is detached.
        view.asVaadinComponent().addDetachListener(new ClientConnector.DetachListener() {
            @Override
            public void detach(ClientConnector.DetachEvent event) {
                systemRegistration.removeHandler();
            }
        });
    }

    @Override
    public View start(ShellAppContext context) {
        view.setPresenter(this);
        return view;
    }

    @Override
    public void locationChanged(Location location) {
    }

    /**
     * Initialize the view.
     */
    private void initView(AppLauncherLayout layout) {
        view.registerApp(layout);
        final List<String> appNames = new LinkedList<String>();
        for (AppLauncherGroup group : layout.getGroups()) {
            for (AppLauncherGroupEntry entry : group.getApps()) {
                appNames.add(entry.getName());
            }
        }
        shell.registerApps(appNames);
    }

    /**
     * Reload Layout and set Icon for running apps.
     */
    private void reloadLayout() {
        final AppLauncherLayout layout = this.appLauncherLayoutManager.getLayoutForUser(context.getUser());
        this.view.clearView();
        initView(layout);
        for (AppLauncherGroup group : layout.getGroups()) {
            for (AppLauncherGroupEntry entry : group.getApps()) {
                if (this.appController.isAppStarted(entry.getName())) {
                    view.activateButton(true, entry.getName());
                }
            }
        }
    }

    private void activateButton(boolean activate, String appName) {
        this.view.activateButton(activate, appName);
    }

}
