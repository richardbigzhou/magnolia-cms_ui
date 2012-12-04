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

import com.google.common.collect.HashMultimap;
import com.vaadin.ui.ComponentContainer;
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
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AppContextimpl.
 */
public class AppContextImpl implements AppContext, AppFrameView.Listener {

    private static final Logger log = LoggerFactory.getLogger(AppContextImpl.class);

    public static final String COMMON_APP_COMPONENTS_ID = "app";
    public static final String COMMON_SUB_APP_COMPONENTS_ID = "subapp";
    public static final String COMPONENTS_ID_PREFIX = "app-";

    private HashMultimap<String, SubAppContext> subAppContexts = HashMultimap.create();

    private ComponentProvider componentProvider;
    private AppController appController;
    private LocationController locationController;
    private Shell shell;
    private MessagesManager messagesManager;
    private final AppDescriptor appDescriptor;

    private App app;

    private AppFrameView appFrameView;

    private ComponentProvider appComponentProvider;
    private ModuleRegistry moduleRegistry;

    public AppContextImpl(ModuleRegistry moduleRegistry, ComponentProvider componentProvider, AppController appController, LocationController locationController, Shell shell, MessagesManager messagesManager, AppDescriptor appDescriptor) {
        this.moduleRegistry = moduleRegistry;
        this.componentProvider = componentProvider;
        this.appController = appController;
        this.locationController = locationController;
        this.shell = shell;
        this.messagesManager = messagesManager;
        this.appDescriptor = appDescriptor;
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
    public AppDescriptor getAppDescriptor() {
        return appDescriptor;
    }

    @Override
    public SubAppDescriptor getDefaultSubAppDescriptor()  {
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
    public View getView() {
        return appFrameView;
    }

    /**
     * Called when the app is launched from the app launcher OR a location change event triggers
     * it to start.
     */
    @Override
    public void start(Location location) {

        this.appComponentProvider = createAppComponentProvider(appDescriptor.getName(), this);

        app = appComponentProvider.newInstance(appDescriptor.getAppClass());

        appFrameView = new AppFrameView();
        appFrameView.setListener(this);

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
    public void onActiveTabSet(String tabId) {
        SubAppContext subAppContext = getSubAppContextForTab(tabId);
        if (subAppContext != null) {
            locationController.goTo(subAppContext.getLocation());
        }
    }

    @Override
    public void onTabClosed(String tabId) {
        SubAppContext subAppContext = getSubAppContextForTab(tabId);
        if (subAppContext != null) {
            subAppContexts.remove(subAppContext.getSubAppId(), subAppContext);
        }
        onActiveTabSet(this.appFrameView.getActiveTabId());
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
        return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, appDescriptor.getName(), "", "");
    }

    @Override
    public void openSubApp(Location location) {
        // If the location targets an existing sub app then activate it and update its location
        // launch running subapp
        SubAppContext subAppContext = getSupportingSubAppContext(location);
        if (subAppContext != null) {
            // check if location actually changed
            //if (!subAppContext.getLocation().equals(location)) {
            subAppContext.setLocation(location);
            subAppContext.getSubApp().locationChanged(location);
            //}
            if (subAppContext.getTabId() != appFrameView.getActiveTab().getTabId()) {
                appFrameView.setActiveTabId(subAppContext.getTabId());
            }
        }
        else {
            // else start new subApp
            // startSubApp

            subAppContext = startSubApp(location);
            subAppContexts.put(subAppContext.getSubAppId(), subAppContext);

        }

    }

    private SubAppContext startSubApp(Location location) {

        DefaultLocation l = (DefaultLocation) location;

        SubAppDescriptor subAppDescriptor = getSubAppDescriptorById(l.getSubAppId());

        if (subAppDescriptor == null) {
            subAppDescriptor = getDefaultSubAppDescriptor();
        }
        SubAppContext subAppContext = new SubAppContextImpl(subAppDescriptor);


        ComponentProvider subAppComponentProvider = createSubAppComponentProvider(appDescriptor.getName(), subAppContext.getSubAppId(), subAppContext, appComponentProvider);

        SubApp subApp = subAppComponentProvider.newInstance(subAppDescriptor.getSubAppClass());



        subAppContext.setAppContext(this);
        subAppContext.setLocation(location);
        subAppContext.setSubApp(subApp);
        subAppContext.setSubAppComponentProvider(subAppComponentProvider);

        View view = subApp.start(location);
        String tabId = appFrameView.addTab((ComponentContainer) view.asVaadinComponent(), subApp.getCaption(), !subAppContexts.isEmpty());
        subAppContext.setTabId(tabId);

        return subAppContext;
    }

    @Override
    public void setSubAppLocation(SubApp subApp, Location location) {
        SubAppContext subAppContext = getSubAppContextForSubApp(subApp);
        if (subAppContext != null) {
            subAppContext.setLocation(location);
            if (appController.getCurrentApp() == this && getActiveSubAppContext() == subAppContext) {
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
    public void enterFullScreenMode() {
        appFrameView.asVaadinComponent().setFullscreen(true);
        //shell.showFullscreen(view);
    }

    @Override
    public void exitFullScreenMode() {
        appFrameView.asVaadinComponent().setFullscreen(false);
    }

    private SubAppContext getActiveSubAppContext() {
        return getSubAppContextForTab(appFrameView.getActiveTabId());
    }

    private SubAppContext getSubAppContextForTab(String tabId) {
        for (SubAppContext subAppContext : subAppContexts.values()) {
            if (subAppContext.getTabId().equals(tabId)) {
                return subAppContext;
            }
        }
        return null;
    }

    // Same instance?!
    private SubAppContext getSubAppContextForSubApp(SubApp subApp) {
        for (SubAppContext subAppContext : getSubAppContexts(subApp.getSubAppId())) {
            if (subAppContext.getSubApp() == subApp)
                return subAppContext;
        }
        return null;
    }

    private Set<SubAppContext> getSubAppContexts(String subAppId) {
        return subAppContexts.get(subAppId);
    }

    private SubAppContext getSupportingSubAppContext(Location location) {
        DefaultLocation l = (DefaultLocation) location;

        // If the location has no subAppId defined, get default
        String subAppId = (l.getSubAppId().isEmpty()) ? getDefaultSubAppDescriptor().getName() : l.getSubAppId();

        SubAppContext supportingContext = null;
        Set<SubAppContext> subApps = subAppContexts.get(subAppId);
        for (SubAppContext context : subApps) {
            if (context.getSubApp().supportsLocation(l)) {
                supportingContext = context;
                break;
            }
        }
        return supportingContext;
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

    private ComponentProvider createSubAppComponentProvider(String appName, String subAppName, SubAppContext subAppContext, ComponentProvider parent) {

        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();

        // Get components common to all sub apps
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(COMMON_SUB_APP_COMPONENTS_ID, moduleDefinitions);

        // Get components for this specific sub app
        String componentsId = COMPONENTS_ID_PREFIX + appName + "-" + subAppName;
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

