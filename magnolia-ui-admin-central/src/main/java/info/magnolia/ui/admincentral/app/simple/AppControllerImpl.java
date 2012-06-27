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

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppEventType;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppView;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationChangeRequestedEvent;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.ComponentContainer;

/**
 * App controller that manages the lifecycle of running apps and raises callbacks to the app.
 *
 * @version $Id$
 */
@Singleton
public class AppControllerImpl implements AppController, LocationChangedEvent.Handler, LocationChangeRequestedEvent.Handler {

    private static final Logger log = LoggerFactory.getLogger(AppControllerImpl.class);

    private ComponentProvider componentProvider;
    private AppLayoutManager appLayoutManager;
    private LocationController locationController;
    private MessagesManager messagesManager;
    private Shell shell;
    private EventBus eventBus;
    private ViewPort viewPort;

    private final Map<String, AppContextImpl> runningApps = new HashMap<String, AppContextImpl>();
    private final LinkedList<AppContextImpl> appHistory = new LinkedList<AppContextImpl>();


    private AppContextImpl currentApp;

    @Inject
    public AppControllerImpl(ComponentProvider componentProvider, AppLayoutManager appLayoutManager, LocationController locationController, Shell shell, EventBus eventBus, MessagesManager messagesManager) {
        this.locationController = locationController;
        this.componentProvider = componentProvider;
        this.appLayoutManager = appLayoutManager;
        this.messagesManager = messagesManager;
        this.eventBus = eventBus;
        this.shell = shell;

        eventBus.addHandler(LocationChangedEvent.class, this);
        eventBus.addHandler(LocationChangeRequestedEvent.class, this);
    }

    @Override
    public void setViewPort(ViewPort viewPort) {
        this.viewPort = viewPort;
    }

    @Override
    public void startIfNotAlreadyRunningThenFocus(String name) {
        AppContextImpl appContext = doStartIfNotAlreadyRunning(name, null);
        doFocus(appContext);
    }

    @Override
    public void startIfNotAlreadyRunning(String name) {
        doStartIfNotAlreadyRunning(name, null);
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

    private AppContextImpl doStartIfNotAlreadyRunning(String name, Location location) {
        AppContextImpl appContext = runningApps.get(name);
        if (appContext == null) {
            AppDescriptor descriptor = getAppDescriptor(name);
            appContext = new AppContextImpl(descriptor);

            if (location == null) {
                location = appContext.getDefaultLocation();
            }

            appContext.start(eventBus, location);

            runningApps.put(name, appContext);
            sendEvent(AppEventType.STARTED, descriptor);
        }
        return appContext;
    }

    private void doFocus(AppContextImpl appContext) {
        appContext.focus();
        appHistory.addFirst(appContext);
        sendEvent(AppEventType.FOCUSED, appContext.getAppDescriptor());
    }

    private void doStop(AppContextImpl appContext) {
        appContext.stop();
        while (appHistory.remove(appContext)) {
        }
        runningApps.remove(appContext.getName());
        if (currentApp == appContext) {
            currentApp = null;
            viewPort.setView(null);
        }
        sendEvent(AppEventType.STOPPED, appContext.getAppDescriptor());
        if (!appHistory.isEmpty()) {
            doFocus(appHistory.peekFirst());
        }
    }

    private void sendEvent(AppEventType appEventType, AppDescriptor appDescriptor) {
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor, appEventType));
    }

    @Override
    public void onLocationChanged(LocationChangedEvent event) {

        Location newLocation = event.getNewLocation();
        AppDescriptor nextApp = getAppForLocation(newLocation);

        if (nextApp == null) {
            return;
        }

        AppContextImpl nextAppContext = runningApps.get(nextApp.getName());

        if (nextAppContext != null) {
            nextAppContext.onLocationUpdate(newLocation);
        } else {
            nextAppContext = doStartIfNotAlreadyRunning(nextApp.getName(), newLocation);
        }

        nextAppContext.display(viewPort);
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
        for (AppCategory category : appLayoutManager.getLayout().getCategories()) {
            for (AppDescriptor descriptor : category.getApps()) {
                if (descriptor.getName().equals(name)) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    private class AppContextImpl implements AppContext {

        private AppDescriptor appDescriptor;
        private App app;
        private AppFrameView appFrameView;
        private Location currentLocation;
        private ComponentProvider appProvider;

        public AppContextImpl(AppDescriptor appDescriptor) {
            this.appDescriptor = appDescriptor;
            this.appProvider = setAppComponentProvider(appDescriptor.getName(),this);
        }

        public String getName() {
            return appDescriptor.getName();
        }

        public AppDescriptor getAppDescriptor() {
            return appDescriptor;
        }

        /**
         * Called when the app is launched from the app launcher OR a location change event triggers it to start.
         */
        public void start(EventBus eventBus, Location location) {

            DefaultLocation appLocation = (DefaultLocation) location;

            app = appProvider.newInstance(appDescriptor.getAppClass());

            appFrameView = new AppFrameView();
            //TODO ehe: Remove this from app. start and use injection instead.
            AppView view = app.start(new DefaultLocation("app", appDescriptor.getName(), appLocation.getToken()));

            currentLocation = location;

            appFrameView.addTab((ComponentContainer) ((IsVaadinComponent) view).asVaadinComponent(), view.getCaption());
        }

        /**
         * Called when the app is launched from the app launcher OR if another app is closed and this is to show itself.
         */
        public void focus() {
            locationController.goTo(currentLocation);
        }

        /**
         * Called when a location change occurs and the app is already running.
         */
        public void onLocationUpdate(Location location) {
            app.locationChanged(new DefaultLocation("app", appDescriptor.getName(), ((DefaultLocation) location).getToken()));
        }

        public void display(ViewPort viewPort) {
            viewPort.setView(appFrameView);
        }

        public String mayStop() {
            return null;
        }

        public void stop() {
            app.stop();
        }

        public Location getDefaultLocation() {
            return new DefaultLocation("app", appDescriptor.getName(), "");
        }

        @Override
        public void openAppView(AppView view) {
            appFrameView.addTab((ComponentContainer) ((IsVaadinComponent) view).asVaadinComponent(), view.getCaption());
        }

        @Override
        public void setAppLocation(Location location) {
            currentLocation = location;
            shell.setFragment(location.toString());
        }

        @Override
        public void sendLocalMessage(Message message) {
            messagesManager.sendMessage(MgnlContext.getUser().getName(), message);
        }

        @Override
        public void broadcastMessage(Message message) {
            messagesManager.sendMessageToAllUsers(message);
        }

        @Override
        public void showConfirmationMessage(String message) {
        }
    }

    /**
     * Create an App Provider child of admin-central and dedicated to the app.
     * This gives us the ability to inject the AppContext into App components.
     * In the module configuration file, for app definition, the
     * components id name must be:
     * app-'appname' : like app-pages.
     */
    private ComponentProvider setAppComponentProvider(String name, AppContext appContext) {

        String componentIdName = "app-"+name;

        log.debug("Read component configurations from module descriptors for "+componentIdName);
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = Components.getComponent(ModuleRegistry.class).getModuleDefinitions();
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(componentIdName, moduleDefinitions);
        //Add the related App AppContext into the component provider.
        configuration.addComponent(InstanceConfiguration.valueOf(AppContext.class, appContext));

        log.debug("Creating the component provider...");
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) componentProvider);
        GuiceComponentProvider componentProvider = builder.build();

        return componentProvider;
   }
}
