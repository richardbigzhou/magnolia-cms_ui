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
import info.magnolia.ui.framework.app.AppInstanceController;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.AppView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements both - the controlling of an app instance as well as the housekeeping of the context for an app.
 */
public class AppInstanceControllerImpl implements AppContext, AppInstanceController {

    private static final Logger log = LoggerFactory.getLogger(AppInstanceControllerImpl.class);

    private List<SubAppContext> subAppContexts = new LinkedList<SubAppContext>();

    private ModuleRegistry moduleRegistry;

    private AppController appController;

    private LocationController locationController;

    private Shell shell;

    private MessagesManager messagesManager;

    private ComponentProvider componentProvider;

    private App app;

    private AppDescriptor appDescriptor;

    private SubAppContext currentSubAppContext;

    @Inject
    public AppInstanceControllerImpl(ModuleRegistry moduleRegistry, AppController appController, LocationController locationController, Shell shell, MessagesManager messagesManager, AppDescriptor appDescriptor) {
        this.moduleRegistry = moduleRegistry;
        this.appController = appController;
        this.locationController = locationController;
        this.shell = shell;
        this.messagesManager = messagesManager;
        this.appDescriptor = appDescriptor;
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
        Map<String, SubAppDescriptor> subAppDescriptors = getAppDescriptor().getSubApps();

        SubAppDescriptor defaultSubAppDescriptor = null;
        for (SubAppDescriptor subAppDescriptor : subAppDescriptors.values()) {
            if (subAppDescriptor.isDefault()) {
                defaultSubAppDescriptor = subAppDescriptor;
                break;
            }
        }
        return defaultSubAppDescriptor;
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
        for (SubAppContext subAppContext : subAppContexts) {
            if (subAppContext.getInstanceId().equals(instanceId)) {
                locationController.goTo(subAppContext.getLocation());
            }
        }
    }

    @Override
    public void onClose(String instanceId) {
        subAppContexts.remove(instanceId);
        onFocus(app.getView().getActiveSubAppView());
    }

    @Override
    public String mayStop() {
        return null;
    }

    @Override
    public void stop() {
        app.stop();
    }

    @Override
    public Location getCurrentLocation() {
        SubAppContext subAppContext = getActiveSubAppContext();
        if (subAppContext != null) {
            return subAppContext.getLocation();
        }
        return new DefaultLocation(Location.LOCATION_TYPE_APP, appDescriptor.getName(), "", "");
    }

    @Override
    public Location getDefaultLocation() {
        SubAppDescriptor subAppDescriptor = getDefaultSubAppDescriptor();
        if (subAppDescriptor != null) {
            return new DefaultLocation(Location.LOCATION_TYPE_APP, appDescriptor.getName(), subAppDescriptor.getName(), "");
        } else {
            return null;
        }
    }

    @Override
    public void openSubApp(Location location) {
        // If the location targets an existing sub app then activate it and update its location
        // launch running subapp
        SubAppContext subAppContext = getSupportingSubAppContext(location);
        if (subAppContext != null) {
            subAppContext.setLocation(location);
            subAppContext.getSubApp().locationChanged(location);

            currentSubAppContext = subAppContext;

            if (subAppContext.getInstanceId() != app.getView().getActiveSubAppView()) {
                app.getView().setActiveSubAppView(subAppContext.getInstanceId());
            }
        } else {
            // else start new subApp
            // startSubApp

            subAppContext = startSubApp(location);
            subAppContexts.add(subAppContext);
            currentSubAppContext = subAppContext;
        }

    }

    private SubAppContext startSubApp(Location location) {

        SubAppDescriptor subAppDescriptor = getSubAppDescriptorById(location.getSubAppId());

        if (subAppDescriptor == null) {
            subAppDescriptor = getDefaultSubAppDescriptor();
        }
        SubAppContext subAppContext = new SubAppContextImpl(subAppDescriptor);

        ComponentProvider subAppComponentProvider = createSubAppComponentProvider(appDescriptor.getName(), subAppContext.getSubAppId(), subAppContext, componentProvider);

        SubApp subApp = subAppComponentProvider.newInstance(subAppDescriptor.getSubAppClass());

        subAppContext.setAppContext(this);
        subAppContext.setLocation(location);
        subAppContext.setSubApp(subApp);

        String instanceId = app.getView().addSubAppView(subApp.start(location), subApp.getCaption(), !subAppContexts.isEmpty());

        subAppContext.setInstanceId(instanceId);

        return subAppContext;
    }

    @Override
    public void setSubAppLocation(SubAppContext subAppContext, Location location) {
        subAppContext.setLocation(location);
        if (appController.getCurrentAppInstanceController() == this && getActiveSubAppContext() == subAppContext) {
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

    private SubAppContext getActiveSubAppContext() {
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
        for (SubAppContext context : subAppContexts) {
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


    private ComponentProvider createSubAppComponentProvider(String appName, String subAppName, SubAppContext subAppContext, ComponentProvider parent) {

        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();

        // Get components common to all sub apps
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(AppController.COMMON_SUB_APP_COMPONENTS_ID, moduleDefinitions);

        // Get components for this specific sub app
        String componentsId = AppController.COMPONENTS_ID_PREFIX + appName + "-" + subAppName;
        log.debug("Reading component configurations from module descriptors for " + componentsId);
        ComponentProviderConfiguration subAppComponents = configurationBuilder.getComponentsFromModules(componentsId, moduleDefinitions);

        configuration.combine(subAppComponents);

        // Add the SubAppContext instance into the component provider.
        configuration.addComponent(InstanceConfiguration.valueOf(SubAppContext.class, subAppContext));

        log.debug("Creating component provider for sub app " + subAppName);
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) parent);

        return builder.build();
    }

}
