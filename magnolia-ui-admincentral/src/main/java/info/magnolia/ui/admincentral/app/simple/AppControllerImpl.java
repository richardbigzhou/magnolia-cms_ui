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
package info.magnolia.ui.admincentral.app.simple;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventType;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationChangeRequestedEvent;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * App controller that manages the lifecycle of running apps and raises callbacks to the app.
 */
@Singleton
public class AppControllerImpl implements AppController, LocationChangedEvent.Handler, LocationChangeRequestedEvent.Handler {



    private static final Logger log = LoggerFactory.getLogger(AppControllerImpl.class);

    private final ModuleRegistry moduleRegistry;

    private final ComponentProvider componentProvider;

    private final AppLauncherLayoutManager appLauncherLayoutManager;

    private final LocationController locationController;

    private final MessagesManager messagesManager;

    private final Shell shell;

    private final EventBus eventBus;

    private ViewPort viewPort;

    private final Map<String, AppContext> runningApps = new HashMap<String, AppContext>();

    private final LinkedList<AppContext> appHistory = new LinkedList<AppContext>();

    private AppContext currentApp;

    @Inject
    public AppControllerImpl(ModuleRegistry moduleRegistry, ComponentProvider componentProvider, AppLauncherLayoutManager appLauncherLayoutManager, LocationController locationController, MessagesManager messagesManager, Shell shell, @Named("admincentral") EventBus admincentralEventBus) {
        this.moduleRegistry = moduleRegistry;
        this.componentProvider = componentProvider;
        this.appLauncherLayoutManager = appLauncherLayoutManager;
        this.locationController = locationController;
        this.messagesManager = messagesManager;
        this.shell = shell;
        this.eventBus = admincentralEventBus;

        admincentralEventBus.addHandler(LocationChangedEvent.class, this);
        admincentralEventBus.addHandler(LocationChangeRequestedEvent.class, this);
    }

    @Override
    public void setViewPort(ViewPort viewPort) {
        this.viewPort = viewPort;
    }

    @Override
    public App startIfNotAlreadyRunningThenFocus(String name, Location location) {
        AppContext appContext = getAppContext(name);
        appContext = doStartIfNotAlreadyRunning(appContext, location);
        if (appContext != null) {
            doFocus(appContext);
            return appContext.getApp();
        } else {
           return null;
        }
    }

    @Override
    public App startIfNotAlreadyRunning(String name, Location location) {
        AppContext appContext = getAppContext(name);

        return doStartIfNotAlreadyRunning(appContext, location).getApp();
    }

    @Override
    public void stopApp(String name) {
        AppContext appContext = runningApps.get(name);
        if (appContext != null) {
            doStop(appContext);
        }
    }

    @Override
    public void stopCurrentApp() {
        final AppContext appContext = appHistory.peekFirst();
        if (appContext != null) {
            stopApp(appContext.getName());
        }
    }

    @Override
    public boolean isAppStarted(String name) {
        return runningApps.containsKey(name);
    }

    @Override
    public void focusCurrentApp(){
        doFocus(currentApp);
    }

    @Override
    public Location getCurrentLocation(String name) {
        AppContext appContext = runningApps.get(name);
        return appContext == null ? null : appContext.getCurrentLocation();
    }

    @Override
    public AppContext getCurrentApp() {
        return currentApp;
    }

    private AppContext doStartIfNotAlreadyRunning(AppContext appContext, Location location) {

        if (isAppStarted(appContext.getName())) {
            appContext.onLocationUpdate(location);
            return appContext;
        }

        appContext.start(location);

        runningApps.put(appContext.getName(), appContext);
        sendEvent(AppLifecycleEventType.STARTED, appContext.getAppDescriptor());

        return appContext;
    }

    private void doFocus(AppContext appContext) {
        locationController.goTo(appContext.getCurrentLocation());
        appHistory.addFirst(appContext);
        sendEvent(AppLifecycleEventType.FOCUSED, appContext.getAppDescriptor());
    }

    private void doStop(AppContext appContext) {
        appContext.stop();
        while (appHistory.remove(appContext))
            ;

        runningApps.remove(appContext.getName());
        if (currentApp == appContext) {
            currentApp = null;
            viewPort.setView(null);
        }
        sendEvent(AppLifecycleEventType.STOPPED, appContext.getAppDescriptor());
        if (!appHistory.isEmpty()) {
            doFocus(appHistory.peekFirst());
        }
    }

    private void sendEvent(AppLifecycleEventType appEventType, AppDescriptor appDescriptor) {
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor, appEventType));
    }

    @Override
    public void onLocationChanged(LocationChangedEvent event) {
        Location newLocation = event.getNewLocation();
        AppDescriptor nextApp = getAppForLocation(newLocation);

        if (nextApp == null) {
            return;
        }

        if (currentApp != null) {
            currentApp.exitFullScreenMode();
        }

        AppContext nextAppContext = getAppContext(nextApp.getName());

        // update location
        Location updateLocation = updateLocation(nextAppContext, newLocation);
        if (!updateLocation.equals(newLocation)) {
            locationController.goTo(updateLocation);
            return;
        }

        nextAppContext = doStartIfNotAlreadyRunning(nextAppContext, newLocation);


        if (currentApp != nextAppContext) {
            appHistory.addFirst(nextAppContext);
        }

        viewPort.setView(nextAppContext.getView());
        currentApp = nextAppContext;
        // focus on locationChanged?
        //focusCurrentApp();
    }

    private Location updateLocation(AppContext appContext, Location location) {

        if (location instanceof DefaultLocation) {
            DefaultLocation l = (DefaultLocation) location;
            String appId = l.getAppId();
            String subAppId = l.getSubAppId();

            if (subAppId == null || subAppId.isEmpty()) {

                if (isAppStarted(appId)) {
                    AppContext runningAppContext = runningApps.get(appId);
                    return runningAppContext.getCurrentLocation();
                }
                else {
                    return appContext.getDefaultLocation();
                }
            }

        }
        return location;
    }

    private AppContext getAppContext(String appId) {
        if (isAppStarted(appId)) {
            return runningApps.get(appId);
        }
        else {
            AppDescriptor descriptor = getAppDescriptor(appId);
            if (descriptor == null) {
                return null;
            }

            return new AppContextImpl(moduleRegistry, componentProvider, this, locationController, shell, messagesManager, descriptor);

        }
    }

    @Override
    public void onLocationChangeRequested(LocationChangeRequestedEvent event) {
        if (currentApp != null) {
            final String message = currentApp.mayStop();
            if (message != null) {
                event.setWarning(message);
            }
        }
    }

    private AppDescriptor getAppForLocation(Location newLocation) {
        if (newLocation instanceof DefaultLocation) {
            DefaultLocation appLocation = (DefaultLocation) newLocation;
            return getAppDescriptor(appLocation.getAppId());
        }
        return null;
    }

    private AppDescriptor getAppDescriptor(String name) {
        return appLauncherLayoutManager.getLayoutForCurrentUser().getAppDescriptor(name);
    }

}
