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
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventType;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationChangeRequestedEvent;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.widget.tabsheet.ShellTab;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.ComponentContainer;


/**
 * App controller that manages the lifecycle of running apps and raises callbacks to the app.
 */
@Singleton
public class AppControllerImpl implements AppController, LocationChangedEvent.Handler, LocationChangeRequestedEvent.Handler {

    public static final String COMMON_APP_COMPONENTS_ID = "app";
    public static final String COMMON_SUB_APP_COMPONENTS_ID = "subapp";
    public static final String COMPONENTS_ID_PREFIX = "app-";

    private static final Logger log = LoggerFactory.getLogger(AppControllerImpl.class);

    private final ModuleRegistry moduleRegistry;

    private final ComponentProvider componentProvider;

    private final AppLauncherLayoutManager appLauncherLayoutManager;

    private final LocationController locationController;

    private final MessagesManager messagesManager;

    private final Shell shell;

    private final EventBus eventBus;

    private ViewPort viewPort;

    private final Map<String, AppContextImpl> runningApps = new HashMap<String, AppContextImpl>();

    private final LinkedList<AppContextImpl> appHistory = new LinkedList<AppContextImpl>();

    private AppContextImpl currentApp;

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
        AppContextImpl appContext = doStartIfNotAlreadyRunning(name, location);
        if (appContext != null) {
            doFocus(appContext);
            return appContext.app;
        } else {
           return null;
        }
    }

    @Override
    public App startIfNotAlreadyRunning(String name, Location location) {
        return doStartIfNotAlreadyRunning(name, location).app;
    }

    @Override
    public void stopApp(String name) {
        AppContextImpl appContext = runningApps.get(name);
        if (appContext != null) {
            doStop(appContext);
        }
    }

    @Override
    public void stopCurrentApp() {
        final AppContextImpl appContext = appHistory.peekFirst();
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

    public Location getCurrentLocation(String name) {
        AppContextImpl appContext = runningApps.get(name);
        return appContext == null ? null : appContext.getCurrentLocation();
    }

    private AppContextImpl doStartIfNotAlreadyRunning(String name, Location location) {
        AppContextImpl appContext = runningApps.get(name);
        if (appContext == null) {

            AppDescriptor descriptor = getAppDescriptor(name);
            if (descriptor == null) {
                return null;
            }

            appContext = new AppContextImpl(descriptor);

            if (location == null) {
                location = new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, name, "main");
            }

            appContext.start(location);

            runningApps.put(name, appContext);
            sendEvent(AppLifecycleEventType.STARTED, descriptor);
        }
        return appContext;
    }

    private void doFocus(AppContextImpl appContext) {
        locationController.goTo(appContext.getCurrentLocation());
        appHistory.addFirst(appContext);
        sendEvent(AppLifecycleEventType.FOCUSED, appContext.getAppDescriptor());
    }

    private void doStop(AppContextImpl appContext) {
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

        AppContextImpl nextAppContext = runningApps.get(nextApp.getName());

        if (nextAppContext != null) {
            nextAppContext.onLocationUpdate(newLocation);
        } else {
            nextAppContext = doStartIfNotAlreadyRunning(nextApp.getName(), newLocation);
        }

        if (currentApp != nextAppContext) {
            appHistory.addFirst(nextAppContext);
        }

        viewPort.setView(nextAppContext.getView());
        currentApp = nextAppContext;
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
            return getAppDescriptor(appLocation.getPrefix());
        }
        return null;
    }

    private AppDescriptor getAppDescriptor(String name) {
        return appLauncherLayoutManager.getLayoutForCurrentUser().getAppDescriptor(name);
    }

    private class AppContextImpl implements AppContext, AppFrameView.Listener {

        private class SubAppContext {

            private String subAppId;
            private SubApp subApp;
            private Location location;
            private ShellTab tab;
            private ComponentProvider subAppComponentProvider;
            public View view;
        }

        private Map<String, SubAppContext> subAppContexts = new HashMap<String, SubAppContext>();

        private final AppDescriptor appDescriptor;

        private App app;

        private AppFrameView appFrameView;

        private ComponentProvider appComponentProvider;

        public AppContextImpl(AppDescriptor appDescriptor) {
            this.appDescriptor = appDescriptor;
        }

        @Override
        public String getName() {
            return appDescriptor.getName();
        }

        @Override
        public AppDescriptor getAppDescriptor() {
            return appDescriptor;
        }

        public View getView() {
            return appFrameView;
        }

        /**
         * Called when the app is launched from the app launcher OR a location change event triggers
         * it to start.
         */
        public void start(Location location) {

            this.appComponentProvider = createAppComponentProvider(appDescriptor.getName(), this);

            app = appComponentProvider.newInstance(appDescriptor.getAppClass());

            appFrameView = new AppFrameView();
            appFrameView.setListener(this);

            app.start(location);
        }

        private SubAppContext startSubApp(String name, Class<? extends SubApp> subAppClass, Location location, String subAppId) {

            if (subAppContexts.containsKey(subAppId)) {
                throw new IllegalStateException("Sub app already exists with sub app id " + subAppId);
            }

            ComponentProvider subAppComponentProvider = createSubAppComponentProvider(appDescriptor.getName(), name, appComponentProvider);

            SubApp subApp = subAppComponentProvider.newInstance(subAppClass);

            View view = subApp.start(location);

            ShellTab tab = appFrameView.addTab((ComponentContainer) view.asVaadinComponent(), subApp.getCaption(), false);
            if (!subAppContexts.isEmpty()) {
                tab.setClosable(true);
            }

            SubAppContext subAppContext = new SubAppContext();
            subAppContext.subApp = subApp;
            subAppContext.subAppId = subAppId;
            subAppContext.tab = tab;
            subAppContext.location = location;
            subAppContext.view = view;
            subAppContext.subAppComponentProvider = subAppComponentProvider;

            subAppContexts.put(subAppContext.subAppId, subAppContext);

            return subAppContext;
        }

        /**
         * Called when a location change occurs and the app is already running.
         */
        public void onLocationUpdate(Location location) {

            String subAppId = extractSubAppSubAppId(((DefaultLocation) location).getToken());

            // The location targets the current display state, update the fragment only
            if (subAppId.length() == 0) {
                SubAppContext subAppContext = getActiveSubAppContext();
                if (subAppContext != null) {
                    shell.setFragment(subAppContext.location.toString());
                }
                return;
            }

            // If the location targets an existing sub app then activate it and update its location
            SubAppContext subAppContext = subAppContexts.get(subAppId);
            if (subAppContext != null) {
                subAppContext.location = location;
                subAppContext.subApp.locationChanged(location);
                if (subAppContext.tab != appFrameView.getActiveTab()) {
                    appFrameView.setActiveTab(subAppContext.tab);
                }
                return;
            }

            app.locationChanged(location);
        }

        private String extractSubAppSubAppId(final String token) {
            int i = token.indexOf(':');
            return i != -1 ? token.substring(0, i) : token;
        }

        @Override
        public void onActiveTabSet(ShellTab tab) {
            SubAppContext subAppContext = getSubAppContextForTab(tab);
            if (subAppContext != null) {
                locationController.goTo(subAppContext.location);
            }
        }

        @Override
        public void onTabClosed(ShellTab tab) {
            SubAppContext subAppContext = getSubAppContextForTab(tab);
            if (subAppContext != null) {
                subAppContexts.remove(subAppContext.subAppId);
            }
        }

        public String mayStop() {
            return null;
        }

        public void stop() {
            app.stop();
        }

        public Location getCurrentLocation() {
            SubAppContext subAppContext = getActiveSubAppContext();
            if (subAppContext != null) {
                return subAppContext.location;
            }
            return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, appDescriptor.getName(), "");
        }

        @Override
        public void openSubApp(String name, Class<? extends SubApp> subAppClass, Location location, String subAppId) {
            startSubApp(name, subAppClass, location, subAppId);
        }

        @Override
        public void openSubAppFullScreen(String name, Class<? extends SubApp> subAppClass, Location location) {
            ComponentProvider subAppComponentProvider = createSubAppComponentProvider(appDescriptor.getName(), name, appComponentProvider);

            SubApp subApp = subAppComponentProvider.newInstance(subAppClass);

            View view = subApp.start(location);

            appFrameView.asVaadinComponent().setFullscreen(true);
            //shell.showFullscreen(view);
        }

        @Override
        public void setSubAppLocation(SubApp subApp, Location location) {
            SubAppContext subAppContext = getSubAppContextForSubApp(subApp);
            if (subAppContext != null) {
                subAppContext.location = location;
                if (currentApp == this && getActiveSubAppContext() == subAppContext) {
                    shell.setFragment(location.toString());
                }
            }
        }

        @Override
        public void sendUserMessage(String user, Message message) {
            messagesManager.sendMessage(user, message);
        }

        @Override
        public void sendLocalMessage(Message message) {
            messagesManager.sendLocalMessage(message);
        }

        @Override
        public void broadcastMessage(Message message) {
            messagesManager.broadcastMessage(message);
        }

        @Override
        public void showConfirmationMessage(String message) {
            log.info("If confirmation message was already implemented you'd get a {} now...", message);
        }

        @Override
        public void exitFullScreenMode() {
            appFrameView.asVaadinComponent().setFullscreen(false);
        }

        private SubAppContext getActiveSubAppContext() {
            ShellTab tab = appFrameView.getActiveTab();
            return getSubAppContextForTab(tab);
        }

        private SubAppContext getSubAppContextForTab(ShellTab tab) {
            for (SubAppContext subAppContext : subAppContexts.values()) {
                if (subAppContext.tab.equals(tab)) {
                    return subAppContext;
                }
            }
            return null;
        }

        private SubAppContext getSubAppContextForSubApp(SubApp subApp) {
            for (SubAppContext subAppContext : subAppContexts.values()) {
                if (subAppContext.subApp == subApp)
                    return subAppContext;
            }
            return null;
        }

    }

    /**
     * Creates a ComponentProvider dedicated for the app with the AdminCentral ComponentProvider as its parent. This
     * gives us the ability to inject the AppContext into App components. The components are read from module
     * descriptors using the convention "app-" + name of the app and merged with the components defined for all apps
     * with the id "app".
     */
    private ComponentProvider createAppComponentProvider(String name, AppContext appContext) {

        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();

        // Get components common to all apps
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(COMMON_APP_COMPONENTS_ID, moduleDefinitions);

        // Get components for this specific app
        String componentsId = COMPONENTS_ID_PREFIX + name;
        log.debug("Reading component configurations from module descriptors for " + componentsId);
        ComponentProviderConfiguration appComponents = configurationBuilder.getComponentsFromModules(componentsId, moduleDefinitions);

        configuration.combine(appComponents);

        // Add the AppContext instance into the component provider.
        configuration.addComponent(InstanceConfiguration.valueOf(AppContext.class, appContext));

        log.debug("Creating component provider for app " + name);
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) componentProvider);

        return builder.build();
    }

    private ComponentProvider createSubAppComponentProvider(String appName, String subAppName, ComponentProvider parent) {

        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();

        // Get components common to all apps
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(COMMON_SUB_APP_COMPONENTS_ID, moduleDefinitions);

        // Get components for this specific sub app
        String componentsId = COMPONENTS_ID_PREFIX + appName + "-" + subAppName;
        log.debug("Reading component configurations from module descriptors for " + componentsId);
        ComponentProviderConfiguration subAppComponents = configurationBuilder.getComponentsFromModules(componentsId, moduleDefinitions);

        configuration.combine(subAppComponents);

        log.debug("Creating component provider for sub app " + subAppName);
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) parent);

        return builder.build();
    }
}
