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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.app.AppLifecycleEventType;
import info.magnolia.ui.framework.app.SubAppDescriptor;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroup;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroupEntry;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayout;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManagerImpl;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.location.LocationChangedEvent;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.message.MessagesManagerImpl;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.AppView;
import info.magnolia.ui.framework.view.ViewPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link info.magnolia.ui.framework.app.AppController}.
 */
public class AppControllerImplTest {

    private static final String APP_NAME_1 = "app1";
    private static final String APP_NAME_2 = "app2";

    private static final String SUBAPP_NAME_1 = "subApp1";
    private static final String SUBAPP_NAME_2 = "subApp2";

    private AppLauncherLayoutManager appLauncherLayoutManager = null;
    private GuiceComponentProvider componentProvider = null;
    private AppControllerImpl appController = null;

    private LocationController locationController = null;

    private AppEventCollector eventCollector = null;

    @Before
    public void setUp() throws Exception {
        setAppLayoutManager();
        ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        componentProvider = initComponentProvider();
        Shell shell = mock(MagnoliaShell.class);
        MessagesManager messagesManager = mock(MessagesManagerImpl.class);

        SimpleEventBus eventBus = new SimpleEventBus();

        eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        this.locationController = new LocationController(eventBus, mock(Shell.class));

        appController = new AppControllerImpl(moduleRegistry, componentProvider, appLauncherLayoutManager, locationController, messagesManager, shell, eventBus);
        appController.setViewPort(mock(ViewPort.class));
    }

    @After
    public void tearDown() throws Exception {
        componentProvider.destroy();

        // Reset the static fields
        AppTestImpl.appNumber = 0;
        AppTestImpl.res = new HashMap<String, Object>();
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
    public void testOpenTwoSubApps() {

        // GIVEN
        Location location = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_1 + "_name");
        locationController.goTo(location);

        // WHEN
        Location location2 = new DefaultLocation(Location.LOCATION_TYPE_APP, APP_NAME_1 + "_name", SUBAPP_NAME_2 + "_name");
        locationController.goTo(location2);

        // THEN
        assertTrue(appController.isAppStarted(APP_NAME_1 + "_name"));
        assertNotSame(location, appController.getCurrentApp().getCurrentLocation());
        assertEquals(location2, appController.getCurrentApp().getCurrentLocation());

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
        assertNotSame(location, appController.getCurrentApp().getCurrentLocation());
        assertNotSame(location3, appController.getCurrentApp().getCurrentLocation());
        assertNotSame(location4, appController.getCurrentApp().getCurrentLocation());
        assertEquals(location2, appController.getCurrentApp().getCurrentLocation());

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
        assertEquals(APP_NAME_2 + "_name", appController.getCurrentApp().getName());
        assertNotNull(appController.getCurrentApp().getCurrentLocation());
        assertNotEquals(newLocation, appController.getCurrentApp().getCurrentLocation());
        assertNotNull(appController.getCurrentApp().getCurrentLocation().getSubAppId());
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
        assertEquals(APP_NAME_1 + "_name", appController.getCurrentApp().getName());
        assertNotNull(appController.getCurrentApp().getCurrentLocation().getParameter());
        assertEquals(parameter, appController.getCurrentApp().getCurrentLocation().getParameter());
    }

    /**
     * Init a LayoutManager containing 2 groups (group1 and group2) with
     * one app each (app1 and app2) linket to {TestApp}.
     */
    private void setAppLayoutManager() {

        appLauncherLayoutManager = mock(AppLauncherLayoutManagerImpl.class);

        // create subapps
        Map<String, SubAppDescriptor> subApps = new HashMap<String, SubAppDescriptor>();
        subApps.put(SUBAPP_NAME_1, AppTestUtility.createSubAppDescriptor(SUBAPP_NAME_1, AppTestSubApp.class, true));
        subApps.put(SUBAPP_NAME_2, AppTestUtility.createSubAppDescriptor(SUBAPP_NAME_2, AppTestSubApp.class, true));

        // Set group1 with App1
        AppDescriptor app1 = AppTestUtility.createAppDescriptorWithSubApps(APP_NAME_1, AppTestImpl.class, subApps);
        AppLauncherGroup group1 = AppTestUtility.createAppGroup("group1", app1);
        // Set group2 with App2
        AppDescriptor app2 = AppTestUtility.createAppDescriptorWithSubApps("app2", AppTestImpl.class, subApps);
        AppLauncherGroup group2 = AppTestUtility.createAppGroup("group2", app2);

        AppLauncherGroupEntry entry1 = new AppLauncherGroupEntry();
        entry1.setName(app1.getName());
        entry1.setAppDescriptor(app1);
        group1.addApp(entry1);

        AppLauncherGroupEntry entry2 = new AppLauncherGroupEntry();
        entry2.setName(app2.getName());
        entry2.setAppDescriptor(app2);
        group2.addApp(entry2);

        AppLauncherLayout appLauncherLayout = new AppLauncherLayout();
        appLauncherLayout.addGroup(group1);
        appLauncherLayout.addGroup(group2);

        when(appLauncherLayoutManager.getLayoutForCurrentUser()).thenReturn(appLauncherLayout);
    }

    public static GuiceComponentProvider initComponentProvider() {

        ComponentProviderConfiguration components = new ComponentProviderConfiguration();

        components.addTypeMapping(AppTestImpl.class, AppTestImpl.class);
        components.addTypeMapping(AppEventTestImpl.class, AppEventTestImpl.class);
        components.addTypeMapping(AppTestSubApp.class, AppTestSubApp.class);
        components.registerImplementation(AppTestView.class, AppViewTestImpl.class);
        components.registerImplementation(AppView.class, AppFrameView.class);

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(components);
        builder.exposeGlobally();
        return builder.build();
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

