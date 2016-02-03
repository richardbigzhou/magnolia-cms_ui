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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.monitoring.SystemMonitor;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.registry.RegistrationException;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.app.App;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.AppInstanceController;
import info.magnolia.ui.api.app.AppLifecycleEvent;
import info.magnolia.ui.api.app.AppLifecycleEventHandler;
import info.magnolia.ui.api.app.AppLifecycleEventType;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManagerImpl;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.Viewport;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.message.MessagesManagerImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * Test case for {@link info.magnolia.ui.api.app.AppController}.
 */
public class AppControllerImplTest {

    private static final String APP_NAME_1 = "app1";
    private static final String APP_NAME_2 = "app2";
    private static final String APP_NAME_THEMED = "appThemed";

    private static final String SUBAPP_NAME_1 = "subApp1";
    private static final String SUBAPP_NAME_2 = "subApp2";

    private AppDescriptorRegistry appRegistry = null;
    private GuiceComponentProvider componentProvider = null;
    private AppControllerImpl appController = null;

    private LocationController locationController = null;

    private AppEventCollector eventCollector = null;
    private EventBus eventBus;

    private MockWebContext ctx;

    @Before
    public void setUp() throws Exception {
        initAppRegistry();

        this.eventBus = new SimpleEventBus();
        this.componentProvider = initComponentProvider();
        this.locationController = componentProvider.getComponent(LocationController.class);

        eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        appController = (AppControllerImpl) componentProvider.getComponent(AppController.class);
        appController.setViewport(mock(Viewport.class));

        ctx = new MockWebContext();

        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        componentProvider.destroy();

        // Reset the static fields
        AppTestImpl.appNumber = 0;
        AppTestImpl.res = new HashMap<String, Object>();
        AppTestSubApp.subAppNumber = 0;
        AppTestSubApp.subApps = new HashMap<String, AppTestSubApp>();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testStartIfNotAlreadyRunningBasic() {
        // GIVEN
        String appName = APP_NAME_1 + "_name";

        // WHEN
        App app = appController.startIfNotAlreadyRunning(appName, new DefaultLocation(Location.LOCATION_TYPE_APP, appName, "", ""));

        // THEN
        // Check Events
        assertNotNull(app);
        assertEquals(AppTestImpl.class.getName(), app.getClass().getName());
        assertEquals(1, eventCollector.appLifecycleEvent.size());
        assertEquals(AppLifecycleEventType.STARTED, eventCollector.appLifecycleEvent.get(0).getEventType());
        // Check App
        assertTrue(AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");
        assertEquals(1, pageApp.events.size());
        assertTrue(pageApp.events.get(0).startsWith("start()"));
        // Check injection
        assertNotNull(pageApp.getAppContext());
    }

    @Test
    public void testStartIfNotAlreadyRunningThenFocusBasic() {
        // GIVEN
        String appName = APP_NAME_1 + "_name";

        // WHEN
        appController.startIfNotAlreadyRunningThenFocus(appName, new DefaultLocation(Location.LOCATION_TYPE_APP, appName, "", ""));

        // THEN
        // Check Events
        assertEquals(2, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);
        // Check App
        assertTrue(AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");

        // We expect two events here. One for starting the app, and one for the focus.
        assertEquals(2, pageApp.events.size());
        assertTrue(pageApp.events.get(0).startsWith("start()"));
        assertTrue(pageApp.events.get(1).startsWith("locationChanged()"));
    }

    @Test
    public void testStopAppOneApp() {
        // GIVEN
        String appName = APP_NAME_1 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName, new DefaultLocation(Location.LOCATION_TYPE_APP, appName, "", ""));
        // Check
        assertTrue(AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");
        assertEquals(2, pageApp.events.size());
        assertTrue(pageApp.events.get(0).startsWith("start()"));

        // WHEN
        appController.stopApp(appName);

        // THEN
        assertEquals(3, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STOPPED, 2);
        assertEquals(3, pageApp.events.size());
        assertTrue(pageApp.events.get(0).startsWith("start()"));
        assertTrue(pageApp.events.get(1).startsWith("locationChanged()"));
        assertTrue(pageApp.events.get(2).startsWith("stop()"));

    }

    @Test
    public void testStopAppTwoAppsWithOneFocused() {
        // GIVEN
        // Start first App
        String appName1 = APP_NAME_1 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName1, new DefaultLocation(Location.LOCATION_TYPE_APP, appName1, "", ""));
        // Check
        assertTrue(AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp1 = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");

        // Start second App
        String appName2 = APP_NAME_2 + "_name";
        appController.startIfNotAlreadyRunning(appName2, new DefaultLocation(Location.LOCATION_TYPE_APP, appName2, "", ""));
        // Check
        assertTrue(AppTestImpl.res.containsKey("TestPageApp1"));
        AppTestImpl pageApp2 = (AppTestImpl) AppTestImpl.res.get("TestPageApp1");

        // WHEN
        appController.stopApp(appName2);

        // THEN
        assertEquals(5, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName1, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName1, AppLifecycleEventType.FOCUSED, 1);
        checkAppEvent(eventCollector, appName2, AppLifecycleEventType.STARTED, 2);
        checkAppEvent(eventCollector, appName2, AppLifecycleEventType.STOPPED, 3);
        checkAppEvent(eventCollector, appName1, AppLifecycleEventType.FOCUSED, 4);

        assertEquals(2, pageApp2.events.size());
        assertTrue(pageApp2.events.get(0).startsWith("start()"));
        assertTrue(pageApp2.events.get(1).startsWith("stop()"));
        assertEquals(2, pageApp1.events.size());
        assertTrue(pageApp1.events.get(0).startsWith("start()"));
        assertTrue(pageApp1.events.get(1).startsWith("locationChanged()"));

    }

    @Test
    public void testStopAppTwoAppsWithBothFocused() {
        // GIVEN
        // Start first App
        String appName1 = APP_NAME_1 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName1, new DefaultLocation(Location.LOCATION_TYPE_APP, appName1, "", ""));
        // Check
        assertTrue(AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp1 = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");

        // Start second App
        String appName2 = APP_NAME_2 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName2, new DefaultLocation(Location.LOCATION_TYPE_APP, appName2, "", ""));
        // Check
        assertTrue(AppTestImpl.res.containsKey("TestPageApp1"));
        AppTestImpl pageApp2 = (AppTestImpl) AppTestImpl.res.get("TestPageApp1");

        // WHEN
        appController.stopApp(appName2);

        // THEN
        assertEquals(6, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName1, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName1, AppLifecycleEventType.FOCUSED, 1);
        checkAppEvent(eventCollector, appName2, AppLifecycleEventType.STARTED, 2);
        checkAppEvent(eventCollector, appName2, AppLifecycleEventType.FOCUSED, 3);
        checkAppEvent(eventCollector, appName2, AppLifecycleEventType.STOPPED, 4);
        checkAppEvent(eventCollector, appName1, AppLifecycleEventType.FOCUSED, 5);

        assertEquals(3, pageApp2.events.size());
        assertTrue(pageApp2.events.get(0).startsWith("start()"));
        assertTrue(pageApp2.events.get(1).startsWith("locationChanged()"));
        assertTrue(pageApp2.events.get(2).startsWith("stop()"));
        assertEquals(3, pageApp1.events.size());
        assertTrue(pageApp1.events.get(0).startsWith("start()"));
        assertTrue(pageApp1.events.get(1).startsWith("locationChanged()"));
        assertTrue(pageApp1.events.get(2).startsWith("locationChanged()"));

    }

    @Test
    public void testStopCurrentApp() {
        // GIVEN
        // Start first App
        String appName1 = APP_NAME_1 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName1, new DefaultLocation(Location.LOCATION_TYPE_APP, appName1, "", ""));
        // Check
        assertTrue(AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl.res.get("TestPageApp0");

        // Start second App
        String appName2 = APP_NAME_2 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName2, new DefaultLocation(Location.LOCATION_TYPE_APP, appName2, "", ""));
        // Check
        assertTrue(AppTestImpl.res.containsKey("TestPageApp1"));
        AppTestImpl pageApp2 = (AppTestImpl) AppTestImpl.res.get("TestPageApp1");

        // WHEN
        appController.stopCurrentApp();

        // THEN
        assertEquals(3, pageApp2.events.size());
        assertTrue(pageApp2.events.get(0).startsWith("start()"));
        assertTrue(pageApp2.events.get(1).startsWith("locationChanged()"));
        assertTrue(pageApp2.events.get(2).startsWith("stop()"));
    }

    @Test
    public void testIsAppStarted() {
        // GIVEN
        String appName1 = APP_NAME_1 + "_name";
        // Check
        assertFalse(appController.isAppStarted(appName1));
        // Start App
        appController.startIfNotAlreadyRunningThenFocus(appName1, new DefaultLocation(Location.LOCATION_TYPE_APP, appName1, "", ""));
        // Check
        assertTrue(appController.isAppStarted(appName1));

        // WHEN
        appController.stopCurrentApp();

        // THEN
        assertFalse(appController.isAppStarted(appName1));
    }

    @Test
    public void testLocationHandler() {

        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");

        // WHEN
        locationController.goTo(location);
        // THEN
        assertTrue(appController.isAppStarted(APP_NAME_1 + "_name"));
    }

    @Test
    public void testOpenSubApp() {
        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");

        // WHEN
        locationController.goTo(location);

        // THEN
        assertTrue(appController.isAppStarted(APP_NAME_1 + "_name"));
        assertEquals(location, appController.getCurrentAppLocation());
    }

    @Test
    public void testOpenTwoSubApps() {

        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");
        locationController.goTo(location);

        // WHEN
        Location location2 = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_2 + "_name");
        locationController.goTo(location2);

        // THEN
        assertTrue(appController.isAppStarted(APP_NAME_1 + "_name"));
        assertNotSame(location, appController.getCurrentAppLocation());
        assertEquals(location2, appController.getCurrentAppLocation());

    }

    @Test
    public void testOpenTwoAppsWithSubApps() {

        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");
        locationController.goTo(location);

        Location location2 = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_2 + "_name");
        locationController.goTo(location2);

        Location location3 = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_2 + "_name");
        locationController.goTo(location3);

        // WHEN
        // switch to current location of first app
        Location location4 = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", "");
        locationController.goTo(location4);

        // THEN
        assertTrue(appController.isAppStarted(APP_NAME_1 + "_name"));
        assertNotSame(location, appController.getCurrentAppLocation());
        assertNotSame(location3, appController.getCurrentAppLocation());
        assertNotSame(location4, appController.getCurrentAppLocation());
        assertEquals(location2, appController.getCurrentAppLocation());

    }

    @Test
    public void testOnLocationChangeChecksForMissingSubAppId() {
        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");
        locationController.goTo(location);

        Location newLocation = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_2 + "_name", "");
        LocationChangedEvent newLocationEvent = new LocationChangedEvent(newLocation);

        // WHEN
        appController.onLocationChanged(newLocationEvent);

        // THEN
        assertNotNull(appController.getCurrentApp());
        assertEquals(APP_NAME_2 + "_name", appController.getCurrentAppLocation().getAppName());
        assertNotNull(appController.getCurrentAppLocation());
        assertNotEquals(newLocation, appController.getCurrentAppLocation());
        assertNotNull(appController.getCurrentAppLocation().getSubAppId());
    }

    @Test
    public void testOnLocationChangePreservesParameters() {
        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");
        locationController.goTo(location);

        String parameter = "/:param:test";
        Location newLocation = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", "", parameter);
        LocationChangedEvent newLocationEvent = new LocationChangedEvent(newLocation);

        // WHEN
        appController.onLocationChanged(newLocationEvent);

        // THEN
        assertNotNull(appController.getCurrentApp());
        assertEquals(APP_NAME_1 + "_name", appController.getCurrentAppLocation().getAppName());
        assertNotNull(appController.getCurrentAppLocation().getParameter());
        assertEquals(parameter, appController.getCurrentAppLocation().getParameter());
    }

    @Test
    public void testStartThemedApp() {
        // GIVEN
        String appName = APP_NAME_THEMED + "_name";

        // WHEN
        appController.startIfNotAlreadyRunningThenFocus(appName, new DefaultLocation(Location.LOCATION_TYPE_APP, appName, "", ""));

        // THEN
        assertTrue(appController.getCurrentApp().getView().asVaadinComponent().getStyleName().contains("testtheme"));
    }

    @Test
    public void testLocationChanged() {

        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");
        locationController.goTo(location);

        // WHEN
        Location location2 = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name", "testParameter");
        locationController.goTo(location2);

        // THEN
        assertTrue(AppTestSubApp.subApps.get("subApp0").isLocationChanged());
    }

    /**
     * Init a LayoutManager containing 2 groups (group1 and group2) with
     * one app each (app1 and app2) linket to {TestApp}.
     */
    private void initAppRegistry() {

        this.appRegistry = mock(AppDescriptorRegistry.class);

        // create subapps
        Map<String, SubAppDescriptor> subApps = new LinkedHashMap<String, SubAppDescriptor>();
        subApps.put(SUBAPP_NAME_1, AppTestUtility.createSubAppDescriptor(SUBAPP_NAME_1, AppTestSubApp.class, true));
        subApps.put(SUBAPP_NAME_2, AppTestUtility.createSubAppDescriptor(SUBAPP_NAME_2, AppTestSubApp.class, false));

        AppDescriptor app1 = AppTestUtility.createAppDescriptorWithSubApps(APP_NAME_1, AppTestImpl.class, subApps);
        AppDescriptor app2 = AppTestUtility.createAppDescriptorWithSubApps(APP_NAME_2, AppTestImpl.class, subApps);
        ConfiguredAppDescriptor appThemed = (ConfiguredAppDescriptor) AppTestUtility.createAppDescriptorWithSubApps(APP_NAME_THEMED, AppTestImpl.class, subApps);
        appThemed.setTheme("testtheme");

        try {
            when(appRegistry.getAppDescriptor(APP_NAME_1 + "_name")).thenReturn(app1);
            when(appRegistry.getAppDescriptor(APP_NAME_2 + "_name")).thenReturn(app2);
            when(appRegistry.getAppDescriptor(APP_NAME_THEMED + "_name")).thenReturn(appThemed);
        } catch (RegistrationException e) {
            // won't happen
        }
    }

    public GuiceComponentProvider initComponentProvider() {

        ComponentProviderConfiguration components = new ComponentProviderConfiguration();

        components.addTypeMapping(AppTestImpl.class, AppTestImpl.class);
        components.addTypeMapping(AppEventTestImpl.class, AppEventTestImpl.class);
        components.addTypeMapping(AppTestSubApp.class, AppTestSubApp.class);
        components.addTypeMapping(AppInstanceController.class, AppInstanceControllerImpl.class);

        components.registerImplementation(AppController.class, AppControllerImpl.class);
        components.registerImplementation(AppTestView.class, AppViewTestImpl.class);
        components.registerImplementation(AppView.class, DefaultAppView.class);
        components.registerImplementation(LocationController.class);
        components.registerImplementation(ModuleRegistry.class, ModuleRegistryImpl.class);
        components.registerImplementation(AppLauncherLayoutManager.class, AppLauncherLayoutManagerImpl.class);

        components.registerInstance(I18nizer.class, new I18nizer() {
            @Override
            public <C> C decorate(C child) {
                return child;
            }
        });

        components.registerInstance(AppDescriptorRegistry.class, appRegistry);
        components.registerInstance(Shell.class, mock(Shell.class));
        components.registerInstance(MessagesManager.class, mock(MessagesManagerImpl.class));

        components.registerInstance(TranslationService.class, mock(TranslationService.class));
        components.registerInstance(LocaleProvider.class, mock(LocaleProvider.class));
        components.registerInstance(info.magnolia.cms.i18n.MessagesManager.class, mock(info.magnolia.cms.i18n.MessagesManager.class));

        final SystemMonitor systemMonitor = mock(SystemMonitor.class);
        when(systemMonitor.isMemoryLimitReached()).thenReturn(false);
        components.registerInstance(SystemMonitor.class, systemMonitor);

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        TestEventBusConfigurer eventBusConfigurer = new TestEventBusConfigurer(eventBus);

        builder.withConfiguration(components);
        builder.exposeGlobally();
        return builder.build(eventBusConfigurer);
    }

    private class TestEventBusConfigurer extends AbstractGuiceComponentConfigurer {

        private final EventBus eventBus;

        private TestEventBusConfigurer(EventBus eventbus) {
            this.eventBus = eventbus;
        }

        @Override
        protected void configure() {
            bind(EventBus.class).annotatedWith(Names.named(AdmincentralEventBus.NAME)).toProvider(Providers.of(eventBus));
            bind(EventBus.class).annotatedWith(Names.named(SystemEventBus.NAME)).toProvider(Providers.of(new SimpleEventBus()));
        }
    }

    /**
     * App event collector.
     */
    public static class AppEventCollector implements AppLifecycleEventHandler {

        List<AppLifecycleEvent> appLifecycleEvent = new ArrayList<AppLifecycleEvent>();

        @Override
        public void onAppFocused(AppLifecycleEvent event) {
            appLifecycleEvent.add(event);
        }

        @Override
        public void onAppStopped(AppLifecycleEvent event) {
            appLifecycleEvent.add(event);
        }

        @Override
        public void onAppStarted(AppLifecycleEvent event) {
            appLifecycleEvent.add(event);
        }
    }

    public static void checkAppEvent(AppEventCollector eventCollector, String appName, AppLifecycleEventType eventType, int position) {
        assertEquals(eventType, eventCollector.appLifecycleEvent.get(position).getEventType());
        assertEquals(appName, eventCollector.appLifecycleEvent.get(position).getAppDescriptor().getName());
    }
}
