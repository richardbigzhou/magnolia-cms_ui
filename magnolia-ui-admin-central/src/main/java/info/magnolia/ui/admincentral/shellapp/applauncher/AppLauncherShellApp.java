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
package info.magnolia.ui.admincentral.shellapp.applauncher;

import info.magnolia.ui.framework.app.ShellApp;
import info.magnolia.ui.framework.app.ShellAppContext;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.app.ShellView;
import info.magnolia.ui.framework.app.layout.AppGroup;
import info.magnolia.ui.framework.app.layout.AppGroupEntry;
import info.magnolia.ui.framework.app.layout.AppLayout;
import info.magnolia.ui.framework.app.layout.AppLayoutChangedEvent;
import info.magnolia.ui.framework.app.layout.AppLayoutChangedEventHandler;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SystemEventBus;
import info.magnolia.ui.framework.location.Location;

import javax.inject.Inject;

/**
 * Activity for the app launcher.
 * Listen to:
 *  SystemEventBus: LayoutEvent. Reload the Layout by getting the latest available App from the {AppLauncherLayoutManager}.
 *  LocalEventBus : App started and App stop event. In this case, update the App button to indicate if an App is started or stopped.
 */
public class AppLauncherShellApp implements ShellApp, AppLauncherView.Presenter {

    private static final long serialVersionUID = 1L;

    private final AppLauncherView view;

    private AppController appController;

    private AppLayoutManager appLayoutManager;

    @Inject
    public AppLauncherShellApp(AppLauncherView view, AppController appController, AppLayoutManager appLayoutManager, EventBus eventBus, SystemEventBus systemEventBus) {
        this.view = view;
        this.appController = appController;
        this.appLayoutManager = appLayoutManager;

        //Init view
        initView(appLayoutManager.getLayoutForCurrentUser());
        /**
         * Handle ReloadAppEvent.
         */
        systemEventBus.addHandler(AppLayoutChangedEvent.class, new AppLayoutChangedEventHandler() {

            @Override
            public void onAppLayoutChanged(AppLayoutChangedEvent event) {
                //Reload Layout
                reloadLayout();
            }
        });

        /**
         * Add Handler of type AppLifecycleEventHandler in order to catch stop
         * and start App events.
         */
        eventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

                /**
                 * Deactivate the visual triangle on the App Icon.
                 */
                @Override
                public void onAppStopped(AppLifecycleEvent event) {
                    AppLayout layout = AppLauncherShellApp.this.appLayoutManager.getLayoutForCurrentUser();
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
    }

    @Override
    public ShellView start(ShellAppContext context) {
        view.setPresenter(this);
        return view;
    }

    @Override
    public void locationChanged(Location location) {
    }

    @Override
    public void onAppInvoked(String name) {
        appController.startIfNotAlreadyRunningThenFocus(name);
    }

    /**
     * Initialize the view.
     */
    private void initView(AppLayout layout) {
        view.registerApp(layout);
    }

    /**
     * Reload Layout and set Icon for running apps.
     */
    private void reloadLayout() {
        AppLayout layout = this.appLayoutManager.getLayoutForCurrentUser();
        this.view.clearView();
        initView(layout);
        for (AppGroup group : layout.getGroups()) {
            for (AppGroupEntry entry : group.getApps()) {
                if(this.appController.isAppStarted(entry.getName())) {
                    view.activateButton(true, entry.getName());
                }
            }
        }
    }

    private void activateButton(boolean activate, String appName) {
        this.view.activateButton(activate, appName);
    }

}
