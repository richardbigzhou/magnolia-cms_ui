/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.framework.app;

import info.magnolia.event.EventBus;
import info.magnolia.event.EventBusProtector;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.app.App;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.AppEventBus;
import info.magnolia.ui.api.app.AppInstanceController;
import info.magnolia.ui.api.app.AppLifecycleEvent;
import info.magnolia.ui.api.app.AppLifecycleEventType;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangeRequestedEvent;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.view.Viewport;
import info.magnolia.ui.framework.message.MessagesManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * Implementation of the {@link info.magnolia.ui.api.app.AppController}.
 *
 * The App controller that manages the lifecycle of running apps and raises callbacks to the app.
 * It provides methods to start, stop and focus already running {@link info.magnolia.ui.api.app.App}s.
 * Registers handlers to the following location change events triggered by the {@link LocationController}:
 * <ul>
 * <li>{@link LocationChangedEvent}</li>
 * <li>{@link LocationChangeRequestedEvent}</li>
 * </ul>
 *
 * @see LocationController
 * @see info.magnolia.ui.api.app.AppContext
 * @see info.magnolia.ui.api.app.App
 */
@Singleton
public class AppControllerImpl implements AppController, LocationChangedEvent.Handler, LocationChangeRequestedEvent.Handler {

    /**
     * Prefix for componentIds for apps.
     */
    private static final String APP_PREFIX = "app";

    private static final Logger log = LoggerFactory.getLogger(AppControllerImpl.class);

    private final ModuleRegistry moduleRegistry;
    private final ComponentProvider componentProvider;
    private final AppDescriptorRegistry appDescriptorRegistry;
    private final LocationController locationController;
    private final EventBus eventBus;
    private final Map<String, AppInstanceController> runningApps = new HashMap<String, AppInstanceController>();
    private final LinkedList<AppInstanceController> appHistory = new LinkedList<AppInstanceController>();
    private final MessagesManager messagesManager;
    private final SimpleTranslator i18n;

    private Viewport viewport;
    private AppInstanceController currentAppInstanceController;
    private EventBusProtector eventBusProtector;

    @Inject
    public AppControllerImpl(ModuleRegistry moduleRegistry, ComponentProvider componentProvider, AppDescriptorRegistry appDescriptorRegistry, LocationController locationController, @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus, MessagesManager messagesManager, SimpleTranslator i18n) {
        this.moduleRegistry = moduleRegistry;
        this.componentProvider = componentProvider;
        this.appDescriptorRegistry = appDescriptorRegistry;
        this.locationController = locationController;
        this.eventBus = admincentralEventBus;
        this.messagesManager = messagesManager;
        this.i18n = i18n;

        admincentralEventBus.addHandler(LocationChangedEvent.class, this);
        admincentralEventBus.addHandler(LocationChangeRequestedEvent.class, this);
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    /**
     * This method is called to create an instance of an app independent from the {@link LocationController} and the {@link AppController} handling.
     * It will not open in the {@link info.magnolia.ui.api.view.Viewport} and will not register itself to the running apps.
     * This is e.g. used to pass the {@link info.magnolia.ui.api.app.App} into a dialog and obtain app-specific information from outside the app.
     *
     * @param appName of the {@link info.magnolia.ui.api.app.App} to instantiate.
     */
    private App getAppWithoutStarting(String appName) {
        AppInstanceController appInstanceController = createNewAppInstance(appName);
        ComponentProvider appComponentProvider = createAppComponentProvider(appInstanceController.getAppDescriptor().getName(), appInstanceController);
        App app = appComponentProvider.newInstance(appInstanceController.getAppDescriptor().getAppClass());

        appInstanceController.setApp(app);
        return app;
    }

    /**
     * This method can be called to launch an {@link App} and then delegate it to the {@link LocationController}.
     * It should have the same effect as calling the {@link LocationController} directly.
     *
     * @param appName of the {@link App} to start.
     * @param location holds information about the subApp to use and the parameters.
     */
    public App startIfNotAlreadyRunningThenFocus(String appName, Location location) {
        AppInstanceController appInstanceController = getAppInstance(appName);
        appInstanceController = doStartIfNotAlreadyRunning(appInstanceController, location);
        doFocus(appInstanceController);
        return appInstanceController.getApp();
    }

    /**
     * This method is called to launch an app independent from the {@link LocationController}.
     * It will not open in the {@link info.magnolia.ui.api.view.Viewport}.
     * This is e.g. used to pass the {@link App} into a dialog and obtain app-specific information from outside the app.
     *
     * See MGNLUI-379.
     *
     * @param appName of the {@link App} to start.
     * @param location holds information about the subApp to use and the parameters.
     */
    @Deprecated
    public App startIfNotAlreadyRunning(String appName, Location location) {
        AppInstanceController appInstanceController = getAppInstance(appName);

        return doStartIfNotAlreadyRunning(appInstanceController, location).getApp();
    }

    @Override
    public void stopApp(String appName) {
        final AppInstanceController appInstanceController = runningApps.get(appName);
        if (appInstanceController != null) {
            doStop(appInstanceController);
        }
    }

    @Override
    public void stopCurrentApp() {
        final AppInstanceController appInstanceController = appHistory.peekFirst();
        if (appInstanceController != null) {
            doStop(appInstanceController);
        }
    }

    @Override
    public boolean isAppStarted(String appName) {
        return runningApps.containsKey(appName);
    }

    @Override
    public void focusCurrentApp() {
        if (currentAppInstanceController != null) {
            doFocus(currentAppInstanceController);
        }
    }

    @Override
    public App getCurrentApp() {
        return currentAppInstanceController == null ? null : currentAppInstanceController.getApp();
    }

    /**
     * Returns the current location of the focused app. This can differ from the actual location of the admin central, e.g. when a shell app is open.
     *
     * @see info.magnolia.ui.api.location.LocationController#getWhere()
     */
    @Override
    public Location getCurrentAppLocation() {
        return currentAppInstanceController == null ? null : currentAppInstanceController.getCurrentLocation();
    }

    /**
     * Returns the current location of a running app instance or null, if it is not running. The App does not have to be focused.
     */
    @Override
    public Location getAppLocation(String appName) {
        AppInstanceController appInstanceController = runningApps.get(appName);
        return appInstanceController == null ? null : appInstanceController.getCurrentLocation();
    }

    /**
     * Delegates the starting of an {@link App} to the {@link info.magnolia.ui.api.app.AppContext}. In
     * case the app is already started, it will update its location.
     */
    private AppInstanceController doStartIfNotAlreadyRunning(AppInstanceController appInstanceController, Location location) {
        if (isAppStarted(appInstanceController.getAppDescriptor().getName())) {
            appInstanceController.onLocationUpdate(location);
            return appInstanceController;
        }

        runningApps.put(appInstanceController.getAppDescriptor().getName(), appInstanceController);
        appInstanceController.start(location);
        sendEvent(AppLifecycleEventType.STARTED, appInstanceController.getAppDescriptor());
        return appInstanceController;
    }

    /**
     * Focuses an already running {@link App} by passing it to the
     * {@link LocationController}.
     */
    private void doFocus(AppInstanceController appInstanceController) {
        locationController.goTo(appInstanceController.getCurrentLocation());
        appHistory.addFirst(appInstanceController);
        sendEvent(AppLifecycleEventType.FOCUSED, appInstanceController.getAppDescriptor());
    }

    private void doStop(AppInstanceController appInstanceController) {
        sendEvent(AppLifecycleEventType.STOPPED, appInstanceController.getAppDescriptor());
        appInstanceController.stop();
        eventBusProtector.resetEventBuses();
        while (appHistory.remove(appInstanceController)) {
            ;
        }

        runningApps.remove(appInstanceController.getAppDescriptor().getName());
        if (currentAppInstanceController == appInstanceController) {
            currentAppInstanceController = null;
            viewport.setView(null);
        }

        if (!appHistory.isEmpty()) {
            doFocus(appHistory.peekFirst());
        }
        locationController.goTo(new DefaultLocation(Location.LOCATION_TYPE_SHELL_APP, "applauncher"));
    }

    private void sendEvent(AppLifecycleEventType appEventType, AppDescriptor appDescriptor) {
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor, appEventType));
    }

    /**
     * Takes care of {@link LocationChangedEvent}s by:
     * <ul>
     * <li>Obtaining the {@link AppDescriptor} associated with the {@link Location}.</li>
     * <li>Creating a new {@link info.magnolia.ui.api.app.AppContext} if not running, otherwise obtain it from the running apps.</li>
     * <li>Updating the {@Link Location} and redirecting in case of missing subAppId.</li>
     * <li>Starting the App.</li>
     * <li>Adding the {@link info.magnolia.ui.api.app.AppContext} to the appHistory.</li>
     * <li>Setting the viewport and updating the current running app.</li>
     * </ul>
     */
    @Override
    public void onLocationChanged(LocationChangedEvent event) {
        Location newLocation = event.getNewLocation();

        if (!newLocation.getAppType().equals(Location.LOCATION_TYPE_APP)) {
            return;
        }

        if (newLocation.equals(getCurrentAppLocation())) {
            return;
        }

        AppDescriptor nextApp = getAppForLocation(newLocation);
        if (nextApp == null) {
            return;
        }

        AppInstanceController nextAppContext = getAppInstance(nextApp.getName());

        // update location
        Location updateLocation = updateLocation(nextAppContext, newLocation);
        if (!updateLocation.equals(newLocation)) {
            locationController.goTo(updateLocation);
            return;
        }

        if (currentAppInstanceController != nextAppContext) {
            appHistory.addFirst(nextAppContext);
            currentAppInstanceController = nextAppContext;
        }

        nextAppContext = doStartIfNotAlreadyRunning(nextAppContext, newLocation);
        viewport.setView(nextAppContext.getApp().getView());
    }

    /**
     * Updates the {@link Location} in case of missing subAppId:
     * <ul>
     * <li>If the app is running, it will fetch the current Location associated with the App.</li>
     * <li>Will fetch the configured default subAppId otherwise.</li>
     * </ul>
     */
    private Location updateLocation(AppInstanceController appInstanceController, Location location) {
        String appType = location.getAppType();
        String appName = location.getAppName();
        String subAppId = location.getSubAppId();
        String params = location.getParameter();

        if (StringUtils.isBlank(subAppId)) {

            if (isAppStarted(appName)) {
                AppInstanceController runningAppContext = runningApps.get(appName);
                subAppId = runningAppContext.getCurrentLocation().getSubAppId();
            } else if (StringUtils.isBlank(subAppId)) {
                Location defaultLocation = appInstanceController.getDefaultLocation();
                if (defaultLocation != null) {
                    subAppId = defaultLocation.getSubAppId();
                } else {
                    log.warn("No default location could be found for the '{}' app, please check subapp configuration.", appName);
                }

            }
        }

        return new DefaultLocation(appType, appName, subAppId, params);
    }

    private AppInstanceController getAppInstance(String appName) {
        if (isAppStarted(appName)) {
            return runningApps.get(appName);
        }
        return createNewAppInstance(appName);
    }

    private AppInstanceController createNewAppInstance(String appName) {
        AppDescriptor descriptor = getAppDescriptor(appName);
        if (descriptor == null) {
            return null;
        }

        AppInstanceController appInstanceController = componentProvider.newInstance(AppInstanceController.class, descriptor);
        createAppComponentProvider(descriptor.getName(), appInstanceController);
        return appInstanceController;
    }

    @Override
    public void onLocationChangeRequested(LocationChangeRequestedEvent event) {
        if (currentAppInstanceController != null) {
            final String message = currentAppInstanceController.mayStop();
            if (message != null) {
                event.setWarning(message);
            }
        }
    }

    @Override
    public void openChooseDialog(String appName, UiContext uiContext, String selectedId, ChooseDialogCallback callback) {
        openChooseDialog(appName, uiContext, null, selectedId, callback);
    }

    @Override
    public void openChooseDialog(String appName, UiContext uiContext, String targetTreeRootPath, String selectedId, ChooseDialogCallback callback) {
        App targetApp = getAppWithoutStarting(appName);
        if (targetApp != null) {
            if (StringUtils.isNotBlank(targetTreeRootPath)) {
                targetApp.openChooseDialog(uiContext, targetTreeRootPath, selectedId, callback);
            } else {
                targetApp.openChooseDialog(uiContext, selectedId, callback);
            }
        }
    }

    private AppDescriptor getAppForLocation(Location location) {
        return getAppDescriptor(location.getAppName());
    }

    private AppDescriptor getAppDescriptor(String name) throws RuntimeException {
        try {
            return appDescriptorRegistry.getAppDescriptor(name);
        } catch (RegistrationException e) {

            Message errorMessage = new Message();
            errorMessage.setType(MessageType.ERROR);
            errorMessage.setSubject(i18n.translate("ui-framework.app.appdescriptorReadError.subject"));
            errorMessage.setMessage(String.format(i18n.translate("ui-framework.app.appdescriptorReadError.message"), name));
            messagesManager.sendLocalMessage(errorMessage);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a ComponentProvider dedicated for the app with the AdminCentral ComponentProvider as its parent. This
     * gives us the ability to inject the AppContext into App components. The components are read from module
     * descriptors using the convention "app-" + name of the app and merged with the components defined for all apps
     * with the id "app".
     */
    private ComponentProvider createAppComponentProvider(String name, AppInstanceController appInstanceController) {

        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();

        // Get components common to all apps
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(APP_PREFIX, moduleDefinitions);

        // Get components for this specific app
        final String componentsId = APP_PREFIX + "-" + name;
        log.debug("Reading component configurations from module descriptors for " + componentsId);
        ComponentProviderConfiguration appComponents = configurationBuilder.getComponentsFromModules(componentsId, moduleDefinitions);

        configuration.combine(appComponents);

        // Add the AppContext instance into the component provider.
        configuration.addComponent(InstanceConfiguration.valueOf(AppContext.class, appInstanceController));
        configuration.addComponent(InstanceConfiguration.valueOf(UiContext.class, appInstanceController));

        configuration.addConfigurer(new AbstractGuiceComponentConfigurer() {

            @Override
            protected void configure() {
                bind(EventBus.class).annotatedWith(Names.named(AppEventBus.NAME)).toProvider(Providers.of(new SimpleEventBus()));
                bind(EventBus.class).annotatedWith(Names.named(ChooseDialogEventBus.NAME)).toProvider(Providers.of(new SimpleEventBus()));
            }
        });

        eventBusProtector = new EventBusProtector();
        configuration.addConfigurer(eventBusProtector);

        log.debug("Creating component provider for app " + name);
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) componentProvider);
        ComponentProvider appComponentProvider = builder.build();

        appInstanceController.setAppComponentProvider(appComponentProvider);

        return appComponentProvider;
    }

}
