/**
 * This file Copyright (c) 2012-2013 Magnolia International
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

import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.framework.event.EventBusProtector;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.overlay.AlertCallback;
import info.magnolia.ui.model.overlay.ConfirmationCallback;
import info.magnolia.ui.model.overlay.MessageStyleType;
import info.magnolia.ui.model.overlay.NotificationCallback;
import info.magnolia.ui.model.overlay.OverlayCloser;
import info.magnolia.ui.model.overlay.OverlayLayer;
import info.magnolia.ui.model.overlay.View;
import info.magnolia.ui.vaadin.overlay.OverlayPresenter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements both - the controlling of an app instance as well as the housekeeping of the context for an app.
 */
public class AppInstanceControllerImpl implements AppContext, AppInstanceController {

    private static final Logger log = LoggerFactory.getLogger(AppInstanceControllerImpl.class);

    private static class SubAppDetails {
        private SubAppContext context;
        private EventBusProtector eventBusProtector;
        private GuiceComponentProvider componentProvider;
    }

    private Map<String, SubAppDetails> subApps = new ConcurrentHashMap<String, SubAppDetails>();

    private ModuleRegistry moduleRegistry;

    private AppController appController;

    private LocationController locationController;

    private Shell shell;

    private MessagesManager messagesManager;

    private ComponentProvider componentProvider;

    private App app;

    private AppDescriptor appDescriptor;

    private SubAppContext currentSubAppContext;

    private OverlayLayer overlayPresenter;

    @Inject
    public AppInstanceControllerImpl(ModuleRegistry moduleRegistry, AppController appController, LocationController locationController, Shell shell,
            MessagesManager messagesManager, AppDescriptor appDescriptor) {
        this.moduleRegistry = moduleRegistry;
        this.appController = appController;
        this.locationController = locationController;
        this.shell = shell;
        this.messagesManager = messagesManager;
        this.appDescriptor = appDescriptor;

        overlayPresenter = new OverlayPresenter() {

            @Override
            public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
                View overlayParent = getView();
                return AppInstanceControllerImpl.this.shell.openOverlayOnView(view, overlayParent, OverlayLayer.ModalityDomain.APP, modalityLevel);
            }

        };
    }

    @Override
    public void setAppComponentProvider(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public void setApp(App app) {
        this.app = app;
    }

    @Override
    public App getApp() {
        return app;
    }

    @Override
    public String getName() {
        return appDescriptor.getName();
    }

    @Override
    public String getLabel() {
        return appDescriptor.getLabel();
    }

    @Override
    public AppDescriptor getAppDescriptor() {
        return appDescriptor;
    }

    @Override
    public SubAppDescriptor getDefaultSubAppDescriptor() {
        Collection<SubAppDescriptor> subAppDescriptors = getAppDescriptor().getSubApps().values();
        return subAppDescriptors.isEmpty() ? null : subAppDescriptors.iterator().next();
    }

    private SubAppDescriptor getSubAppDescriptorById(String subAppId) {
        Map<String, SubAppDescriptor> subAppDescriptors = getAppDescriptor().getSubApps();
        return subAppDescriptors.get(subAppId);
    }

    @Override
    public AppView getView() {
        return app.getView();
    }





    /**
     * Called when the app is launched from the app launcher OR a location change event triggers
     * it to start.
     */
    @Override
    public void start(Location location) {

        app = componentProvider.newInstance(appDescriptor.getAppClass());
        app.start(location);

        if (StringUtils.isNotBlank(appDescriptor.getTheme())) {
            app.getView().setTheme(appDescriptor.getTheme());
        }
    }

    /**
     * Called when a location change occurs and the app is already running.
     */
    @Override
    public void onLocationUpdate(Location location) {
        app.locationChanged(location);
    }

    @Override
    public void onFocus(String instanceId) {
        if (subApps.containsKey(instanceId)) {
            SubAppContext subAppContext = subApps.get(instanceId).context;
            locationController.goTo(subAppContext.getLocation());
        }
    }

    @Override
    public void onClose(String instanceId) {
        stopSubAppInstance(instanceId);
        onFocus(app.getView().getActiveSubAppView());
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void stop() {
        for (String instanceId : subApps.keySet()) {
            stopSubAppInstance(instanceId);
        }
        currentSubAppContext = null;
        app.stop();
    }

    private void stopSubAppInstance(String instanceId) {
        SubAppDetails subAppDetails = subApps.get(instanceId);
        subAppDetails.context.getSubApp().stop();
        subAppDetails.componentProvider.destroy();
        subAppDetails.eventBusProtector.resetEventBuses();
        subApps.remove(instanceId);
    }

    @Override
    public Location getCurrentLocation() {
        SubAppContext subAppContext = getActiveSubAppContext();
        if (subAppContext != null) {
            return subAppContext.getLocation();
        }
        return new DefaultLocation(Location.LOCATION_TYPE_APP, appDescriptor.getName());
    }

    @Override
    public Location getDefaultLocation() {
        SubAppDescriptor subAppDescriptor = getDefaultSubAppDescriptor();
        if (subAppDescriptor != null) {
            return new DefaultLocation(Location.LOCATION_TYPE_APP, appDescriptor.getName(), subAppDescriptor.getName());
        } else {
            return null;
        }
    }

    @Override
    public void openSubApp(Location location) {
        // main sub app has always to be there - open it if not yet running
        final Location defaultLocation = getDefaultLocation();
        boolean isDefaultSubApp = defaultLocation.getSubAppId().equals(location.getSubAppId());
        if (!isDefaultSubApp) {
            SubAppContext subAppContext = getSupportingSubAppContext(defaultLocation);
            if (subAppContext == null) {
                startSubApp(defaultLocation, false);
            }
        }
        // If the location targets an existing sub app then activate it and update its location
        // launch running subapp
        SubAppContext subAppContext = getSupportingSubAppContext(location);
        if (subAppContext != null) {
            if (!location.equals(subAppContext.getLocation())) {
                subAppContext.setLocation(location);
                subAppContext.getSubApp().locationChanged(location);
            }
            // update the caption
            getView().updateCaption(subAppContext.getInstanceId(), subAppContext.getSubApp().getCaption());

            // set active subApp view if it isn't already active
            if (!subAppContext.getInstanceId().equals(app.getView().getActiveSubAppView())) {
                app.getView().setActiveSubAppView(subAppContext.getInstanceId());
            }
        } else {
            subAppContext = startSubApp(location, !isDefaultSubApp);
        }
        currentSubAppContext = subAppContext;

    }

    /**
     * Used to close a running subApp from server side. Delegates to {@link AppView#closeSubAppView(String)}.
     * The actual closing and cleaning up, will be handled by the callback {@link AppView.Listener#onClose(String)}
     * implemented in {@link #onClose(String)}.
     */
    @Override
    public void closeSubApp(String instanceId) {
       getView().closeSubAppView(instanceId);
    }

    private SubAppContext startSubApp(Location location, boolean isClosable) {

        SubAppDescriptor subAppDescriptor = getSubAppDescriptorById(location.getSubAppId());

        if (subAppDescriptor == null) {
            subAppDescriptor = getDefaultSubAppDescriptor();
        }
        SubAppContext subAppContext = new SubAppContextImpl(subAppDescriptor, shell);

        subAppContext.setAppContext(this);
        subAppContext.setLocation(location);

        SubAppDetails subAppDetails = createSubAppComponentProvider(appDescriptor.getName(), subAppContext.getSubAppId(), subAppContext, componentProvider);
        subAppDetails.context = subAppContext;

        SubApp subApp = subAppDetails.componentProvider.newInstance(subAppDescriptor.getSubAppClass());
        subAppContext.setSubApp(subApp);

        String instanceId = app.getView().addSubAppView(subApp.start(location), subApp.getCaption(), isClosable);

        subAppContext.setInstanceId(instanceId);

        subApps.put(instanceId, subAppDetails);
        return subAppContext;
    }

    /**
     * Used to update the framework about changes to locations inside the app and circumventing the {@link info.magnolia.ui.framework.location.LocationController} mechanism.
     * Example Usages:
     * <pre>
     *     <ul>
     *         <li>Inside ContentApp framework to update {@link info.magnolia.ui.framework.app.SubAppContext#getLocation()} and the {@link Shell} fragment</li>
     *         <li>In the Pages App when navigating pages inside the PageEditor</li>
     *     </ul>
     * </pre>
     * When ever possible use the {@link info.magnolia.ui.framework.location.LocationController} to not have to do this.
     *
     * @param subAppContext The subAppContext to be updated.
     * @param location The new {@link Location}.
     */
    @Override
    public void updateSubAppLocation(SubAppContext subAppContext, Location location) {
        subAppContext.setLocation(location);

        // the restoreBrowser() method in the BrowserSubApp is not initialized at this point
        if (subAppContext.getInstanceId() != null) {
            getView().updateCaption(subAppContext.getInstanceId(), subAppContext.getSubApp().getCaption());
        }

        if (appController.getCurrentApp() == getApp() && getActiveSubAppContext() == subAppContext) {
            shell.setFragment(location.toString());
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
    public void enterFullScreenMode() {
        app.getView().setFullscreen(true);
    }

    @Override
    public void exitFullScreenMode() {
        app.getView().setFullscreen(false);
    }

    @Override
    public SubAppContext getActiveSubAppContext() {
        return currentSubAppContext;
    }

    /**
     * Will return a running subAppContext which will handle the current location.
     * Only subApps with matching subAppId will be asked whether they support the location.
     */
    private SubAppContext getSupportingSubAppContext(Location location) {
        // If the location has no subAppId defined, get default
        String subAppId = (location.getSubAppId().isEmpty()) ? getDefaultSubAppDescriptor().getName() : location.getSubAppId();

        SubAppContext supportingContext = null;
        for (SubAppDetails subAppDetails : subApps.values()) {
            SubAppContext context = subAppDetails.context;
            if (!subAppId.equals(context.getSubAppId())) {
                continue;
            }
            if (context.getSubApp().supportsLocation(location)) {
                supportingContext = context;
                break;
            }
        }
        return supportingContext;
    }


    private SubAppDetails createSubAppComponentProvider(String appName, String subAppName, SubAppContext subAppContext, ComponentProvider parent) {

        SubAppDetails subAppDetails = new SubAppDetails();

        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();

        // Get components common to all sub apps
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(AppController.SUBAPP_PREFIX, moduleDefinitions);

        // Get components for this specific sub app
        String componentsId = AppController.APP_PREFIX + "-" + appName + "-" + subAppName;
        log.debug("Reading component configurations from module descriptors for " + componentsId);
        ComponentProviderConfiguration subAppComponents = configurationBuilder.getComponentsFromModules(componentsId, moduleDefinitions);

        configuration.combine(subAppComponents);

        // Add the SubAppContext instance into the component provider.
        configuration.addComponent(InstanceConfiguration.valueOf(SubAppContext.class, subAppContext));

        EventBusProtector eventBusProtector = new EventBusProtector();
        configuration.addConfigurer(eventBusProtector);
        subAppDetails.eventBusProtector = eventBusProtector;

        log.debug("Creating component provider for sub app " + subAppName);
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) parent);

        subAppDetails.componentProvider = builder.build();

        return subAppDetails;
    }

    @Override
    public OverlayCloser openOverlay(View view) {
        return overlayPresenter.openOverlay(view);
    }

    @Override
    public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
        return overlayPresenter.openOverlay(view, modalityLevel);
    }

    @Override
    public void openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb) {
        overlayPresenter.openAlert(type, viewToShow, confirmButtonText, cb);
    }

    @Override
    public void openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        overlayPresenter.openAlert(type, title, body, confirmButtonText, cb);
    }

    @Override
    public void openConfirmation(MessageStyleType type, View viewToShow, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        overlayPresenter.openConfirmation(type, viewToShow, confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }

    @Override
    public void openConfirmation(MessageStyleType type, String title, String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        overlayPresenter.openConfirmation(type, title, body, confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, View viewToShow) {
        overlayPresenter.openNotification(type, doesTimeout, viewToShow);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, String title) {
        overlayPresenter.openNotification(type, doesTimeout, title);
    }

    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, NotificationCallback cb) {
        overlayPresenter.openNotification(type, doesTimeout, title, linkText, cb);
    }

}
