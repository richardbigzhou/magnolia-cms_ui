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

import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLayout;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.app.layout.event.LayoutEvent;
import info.magnolia.ui.framework.app.layout.event.LayoutEventHandler;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SystemEventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.view.ViewPort;

import javax.inject.Inject;

/**
 * Activity for the app launcher.
 * Listen to:
 *  SystemEventBus: LayoutEvent. Reload the Layout by getting the latest available App from the {AppLauncherLayoutManager}.
 *  LocalEventBus : App started and App stop event. In this case, update the App button to indicate if an App is started or stopped.
 *
 * @version $Id$
 */
public class AppLauncherActivity extends AbstractActivity implements AppLauncherView.Presenter {

    private static final long serialVersionUID = 1L;

    private final AppLauncherView view;

    private AppController appController;

    private AppLayoutManager appLauncherLayoutManager;

    private AppLayout layout;

    @Inject
    public AppLauncherActivity(AppLauncherView view, AppController appController, AppLayoutManager appLauncherLayoutManager, EventBus eventBus, SystemEventBus systemEventBus) {
        this.view = view;
        this.appController = appController;
        this.appLauncherLayoutManager = appLauncherLayoutManager;
        this.layout = this.appLauncherLayoutManager.getLayout();

        //Init view
        initView(layout);
        /**
         * Handle ReloadAppEvent.
         */
        systemEventBus.addHandler(LayoutEvent.class, new LayoutEventHandler.Adapter() {
            @Override
            public void onReloadApp(LayoutEvent event) {
                if(isAppRegistered(event.getAppName())) {
                    //Reload Layout
                    reloadLayout();
                }
            }
        });

        /**
         * Add Handler of type AppLifecycleEventHandler in order to catch stop
         * and start App events.
         */
        eventBus.addHandler(AppLifecycleEvent.class,
            new AppLifecycleEventHandler.Adapter() {

                @Override
                /**
                 * Deactivate the visual triangle on the App Icon.
                 */
                public void onAppStopped(AppLifecycleEvent event) {
                    if(isAppPartOftheLayout(event.getAppDescriptor().getName())) {
                        activateButton(false, event.getAppDescriptor().getName());
                    }
                }

                @Override
                /**
                 * Activate the visual triangle on the App Icon.
                 */
                public void onAppStarted(AppLifecycleEvent event) {
                    activateButton(true, event.getAppDescriptor().getName());
                }
        });
    }

    @Override
    public void onAppInvoked(String name) {
        appController.startIfNotAlreadyRunningThenFocus(name);
    }

    @Override
    public void start(ViewPort viewPort, EventBus eventBus, Place place) {
        view.setPresenter(this);
        viewPort.setView(view);
    }

    /**
     * Initialize the view.
     */
    private void initView(AppLayout layout) {
        view.registerApp(layout);
    }

    /**
     * Check if this app is registered For this profile.
     */
    private boolean isAppRegistered(String appName) {
        return this.appLauncherLayoutManager.isAppDescriptionRegistered(appName);
    }

    private boolean isAppPartOftheLayout(String appName) {
        return this.layout.isAppAlreadyRegistered(appName);
    }

    /**
     * Reload Layout and set Icon for running apps.
     */
    private void reloadLayout() {
        this.layout = this.appLauncherLayoutManager.getLayout();
        this.view.clearView();
        initView(this.layout);
        for (AppCategory category : layout.getCategories()) {
            for (AppDescriptor descriptor : category.getApps()) {
                if(this.appController.isAppStarted(descriptor)) {
                    view.activateButton(true, descriptor.getName());
                }
            }
        }
    }

    private void activateButton(boolean activate, String appName) {
        this.view.activateButton(activate, appName);
    }

}
