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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link AppController}.
 *
 * The App controller that manages the lifecycle of running apps and raises callbacks to the app.
 * It provides methods to start, stop and focus already running {@link App}s.
 * Registers handlers to the following location change events triggered by the {@link LocationController}:
 * <ul>
 * <li>{@link LocationChangedEvent}</li>
 * <li>{@link LocationChangeRequestedEvent}</li>
 * </ul>
 *
 * @see LocationController
 * @see AppContext
 * @see App
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
    public AppControllerImpl(ModuleRegistry moduleRegistry, ComponentProvider componentProvider,
                             AppLauncherLayoutManager appLauncherLayoutManager, LocationController locationController,
                             MessagesManager messagesManager, Shell shell, @Named("admincentral") EventBus admincentralEventBus) {
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

    /**
     * This method is called to create an instant of an app independent from the {@link LocationController} and the {@link AppController} handling.
     * It will not open in the {@link ViewPort} and will not register itself to the running apps.
     * This is e.g. used to pass the {@link App} into a dialog and obtain app-specific information from outside the app.
     *
     * @param appId of the {@link App} to instantiate.
     */
    @Override
    public App getAppWithoutStarting(String appId) {
        AppContext appContext = getAppContext(appId);
        ComponentProvider appComponentProvider = appContext.createAppComponentProvider(appContext.getName(), appContext);
        App app = appComponentProvider.newInstance(appContext.getAppDescriptor().getAppClass());

        appContext.setApp(app);
        return app;
    }

    /**
     * This method can be called to launch an {@link App} and then delegate it to the {@link LocationController}.
     * It should have the same effect as calling the {@link LocationController} directly.
     *
     * @param appId of the {@link App} to start.
     * @param location holds information about the subApp to use and the parameters.
     */
    @Override
    public App startIfNotAlreadyRunningThenFocus(String appId, Location location) {
        AppContext appContext = getAppContext(appId);
        appContext = doStartIfNotAlreadyRunning(appContext, location);
        if (appContext != null) {
            doFocus(appContext);
            return appContext.getApp();
        } else {
            return null;
        }
    }

    /**
     * This method is called to launch an app independent from the {@link LocationController}.
     * It will not open in the {@link ViewPort}.
     * This is e.g. used to pass the {@link App} into a dialog and obtain app-specific information from outside the app.
     *
     * See MGNLUI-379.
     *
     * @param appId of the {@link App} to start.
     * @param location holds information about the subApp to use and the parameters.
     * @deprecated since introduction of {@link #getAppWithoutStarting(String appId) getAppWithoutStarting}
     */
    @Deprecated
    @Override
    public App startIfNotAlreadyRunning(String appId, Location location) {
        AppContext appContext = getAppContext(appId);

        return doStartIfNotAlreadyRunning(appContext, location).getApp();
    }

    @Override
    public void stopApp(String appId) {
        AppContext appContext = runningApps.get(appId);
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
    public boolean isAppStarted(String appId) {
        return runningApps.containsKey(appId);
    }

    @Override
    public void focusCurrentApp() {
        doFocus(currentApp);
    }

    @Override
    public Location getCurrentLocation(String appId) {
        AppContext appContext = runningApps.get(appId);
        return appContext == null ? null : appContext.getCurrentLocation();
    }

    @Override
    public AppContext getCurrentApp() {
        return currentApp;
    }

    /**
     * Delegates the starting of an {@link App} to the {@link AppContext}. In
     * case the app is already started, it will update its location.
     */
    private AppContext doStartIfNotAlreadyRunning(AppContext appContext, Location location) {
        if (isAppStarted(appContext.getName())) {
            appContext.onLocationUpdate(location);
            return appContext;
        }

        runningApps.put(appContext.getName(), appContext);
        appContext.start(location);
        sendEvent(AppLifecycleEventType.STARTED, appContext.getAppDescriptor());
        return appContext;
    }

    /**
     * Focuses an already running {@link App} by passing it to the
     * {@link LocationController}.
     */
    private void doFocus(AppContext appContext) {
        locationController.goTo(appContext.getCurrentLocation());
        appHistory.addFirst(appContext);
        sendEvent(AppLifecycleEventType.FOCUSED, appContext.getAppDescriptor());
    }

    private void doStop(AppContext appContext) {
        appContext.stop();
        while (appHistory.remove(appContext)) {
            ;
        }

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

    /**
     * Takes care of {@link LocationChangedEvent}s by:
     * <ul>
     * <li>Obtaining the {@link AppDescriptor} associated with the {@link Location}.</li>
     * <li>Creating a new {@link AppContext} if not running, otherwise obtain it from the running apps.</li>
     * <li>Updating the {@Link Location} and redirecting in case of missing subAppId.</li>
     * <li>Starting the App.</li>
     * <li>Adding the {@link AppContext} to the appHistory.</li>
     * <li>Setting the viewPort and updating the current running app.</li>
     * </ul>
     */
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
        // focusCurrentApp();
    }

    /**
     * Updates the {@link Location} in case of missing subAppId:
     * <ul>
     * <li>If the app is running, it will fetch the current Location associated with the App.</li>
     * <li>Will fetch the configured default subAppId otherwise.</li>
     * </ul>
     */
    private Location updateLocation(AppContext appContext, Location location) {
        String appType = location.getAppType();
        String appId = location.getAppId();
        String subAppId = location.getSubAppId();
        String params = location.getParameter();

        if (StringUtils.isBlank(subAppId)) {

            if (isAppStarted(appId)) {
                AppContext runningAppContext = runningApps.get(appId);
                subAppId = runningAppContext.getCurrentLocation().getSubAppId();
            } else if (StringUtils.isBlank(subAppId)) {
                subAppId = appContext.getDefaultLocation().getSubAppId();

            }
        }

        return new DefaultLocation(appType, appId, subAppId, params);
    }


    private AppContext getAppContext(String appId) {
        if (isAppStarted(appId)) {
            return runningApps.get(appId);
        } else {
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
        return getAppDescriptor(newLocation.getAppId());
    }

    private AppDescriptor getAppDescriptor(String name) {
        return appLauncherLayoutManager.getLayoutForCurrentUser().getAppDescriptor(name);
    }

}
