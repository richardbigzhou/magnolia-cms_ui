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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.app.AppLifecycleEventType;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManagerImpl;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroup;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroupEntry;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayout;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.message.MessagesManagerImpl;
import info.magnolia.ui.framework.shell.Shell;

/**
 * Test case for {@link info.magnolia.ui.framework.app.AppController}.
 */
public class AppControllerImplTest {

    private AppLauncherLayoutManager appLauncherLayoutManager = null;
    private GuiceComponentProvider componentProvider = null;
    private AppControllerImpl appController = null;
    private AppEventCollector eventCollector = null;
    private String appName_1 = "app1";
    private String appName_2 = "app2";

    @Before
    public void setUp() throws Exception {
        setAppLayoutManager();
        ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        componentProvider = initComponentProvider();
        Shell shell = mock(MagnoliaShell.class);
        MessagesManager messagesManager = mock(MessagesManagerImpl.class);
        LocationController locationController = mock(LocationController.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        appController = new AppControllerImpl(moduleRegistry, componentProvider, appLauncherLayoutManager, locationController, messagesManager, shell, eventBus);
    }

    @After
    public void tearDown() throws Exception {
        componentProvider.destroy();
        //Reset the static fields
        AppTestImpl.appNumber = 0;
        AppTestImpl.res = new HashMap<String, Object>();
    }

    @Test
    public void testStartIfNotAlreadyRunning_Basic() {
        // GIVEN
        String appName = appName_1 + "_name";

        // WHEN
        appController.startIfNotAlreadyRunning(appName);

        // THEN
        //Check Events
        assertEquals(1, eventCollector.appLifecycleEvent.size());
        assertEquals(AppLifecycleEventType.STARTED, eventCollector.appLifecycleEvent.get(0).getEventType());
        //Check App
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");
        assertEquals(1, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));
        //Check injection
        assertNotNull(pageApp.ctx);
        assertNotNull(pageApp.subApp);
        //Check AppContext
        assertEquals("app:app1_name", pageApp.getDefaultLocation().toString());
    }

    @Test
    public void testStartIfNotAlreadyRunningThenFocus_Basic() {
        // GIVEN
        String appName = appName_1 + "_name";

        // WHEN
        appController.startIfNotAlreadyRunningThenFocus(appName);

        // THEN
        //Check Events
        assertEquals(2, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);
        //Check App
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");
        assertEquals(1, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));
        //Check AppContext
        assertEquals("app:app1_name", pageApp.getDefaultLocation().toString());
    }

    @Test
    public void testStopApp_oneApp() {
        // GIVEN
        String appName = appName_1 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");
        assertEquals(1, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));

        // WHEN
        appController.stopApp(appName);

        // THEN
        assertEquals(3, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STOPPED, 2);
        assertEquals(2, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));
        assertEquals(true, pageApp.events.get(1).startsWith("stop()"));
    }

    @Test
    public void testStopApp_twoApp() {
        // GIVEN
        //Start first App
        String appName1 = appName_1 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName1);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp1 = (AppTestImpl) AppTestImpl.res.get("TestPageApp0");

        //Start second App
        String appName2 = appName_2 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName2);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp1"));
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

        assertEquals(2, pageApp2.events.size());
        assertEquals(true, pageApp2.events.get(0).startsWith("start()"));
        assertEquals(true, pageApp2.events.get(1).startsWith("stop()"));
        assertEquals(1, pageApp1.events.size());
        assertEquals(true, pageApp1.events.get(0).startsWith("start()"));
    }

    @Test
    public void testStopCurrentApp() {
        // GIVEN
        //Start first App
        String appName1 = appName_1 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName1);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl.res.get("TestPageApp0");

        //Start second App
        String appName2 = appName_2 + "_name";
        appController.startIfNotAlreadyRunningThenFocus(appName2);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp1"));
        AppTestImpl pageApp2 = (AppTestImpl) AppTestImpl.res.get("TestPageApp1");

        // WHEN
        appController.stopCurrentApp();

        // THEN
        assertEquals(2, pageApp2.events.size());
        assertEquals(true, pageApp2.events.get(0).startsWith("start()"));
        assertEquals(true, pageApp2.events.get(1).startsWith("stop()"));
    }

    @Test
    public void testIsAppStarted() {
        // GIVEN
        String appName1 = appName_1 + "_name";
        //Check
        assertEquals(false, appController.isAppStarted(appName1));
        //Start App
        appController.startIfNotAlreadyRunningThenFocus(appName1);
        //Check
        assertEquals(true, appController.isAppStarted(appName1));

        // WHEN
        appController.stopCurrentApp();

        // THEN
        assertEquals(false, appController.isAppStarted(appName1));
    }

    /**
     * Init a LayoutManager containing 2 groups (group1 and group2) with
     * one app each (app1 and app2) linket to {TestApp}.
     */
    private void setAppLayoutManager() {

        appLauncherLayoutManager = mock(AppLauncherLayoutManagerImpl.class);
        //Set group1 with App1
        AppDescriptor app1 = AppTestUtility.createAppDescriptor(appName_1, AppTestImpl.class);
        AppLauncherGroup group1 = AppTestUtility.createAppGroup("group1", app1);
        //Set group2 with App2
        AppDescriptor app2 = AppTestUtility.createAppDescriptor("app2", AppTestImpl.class);
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
        components.registerImplementation(AppTestView.class, AppViewTestImpl.class);
        components.registerImplementation(AppTestSubApp.class, AppTestSubApp.class);

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(components);
        builder.exposeGlobally();
        return builder.build();
    }

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
