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

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//import info.magnolia.ui.framework.app.SubAppDescriptor;
//import info.magnolia.ui.framework.location.Location;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import info.magnolia.module.ModuleRegistry;
//import info.magnolia.module.model.ComponentsDefinition;
//import info.magnolia.module.model.ConfigurerDefinition;
//import info.magnolia.module.model.ModuleDefinition;
//import info.magnolia.objectfactory.guice.GuiceComponentProvider;
//import info.magnolia.ui.admincentral.MagnoliaShell;
//import info.magnolia.ui.admincentral.app.simple.AppControllerImplTest.AppEventCollector;
//import info.magnolia.ui.framework.app.AppDescriptor;
//import info.magnolia.ui.framework.app.AppLifecycleEvent;
//import info.magnolia.ui.framework.app.AppLifecycleEventType;
//import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManager;
//import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroup;
//import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroupEntry;
//import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayout;
//import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManagerImpl;
//import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
//import info.magnolia.ui.framework.event.AppEventBusConfigurer;
//import info.magnolia.ui.framework.event.EventBus;
//import info.magnolia.ui.framework.event.InvocationCountingTestEventHandler;
//import info.magnolia.ui.framework.event.SimpleEventBus;
//import info.magnolia.ui.framework.event.TestEvent;
//import info.magnolia.ui.framework.location.DefaultLocation;
//import info.magnolia.ui.framework.location.LocationController;
//import info.magnolia.ui.framework.message.MessagesManager;
//import info.magnolia.ui.framework.message.MessagesManagerImpl;
//import info.magnolia.ui.framework.shell.Shell;
//
///**
// * Test case for {@link info.magnolia.ui.framework.app.AppController} local App's event.
// */
//public class AppEventTest {
//
//    private AppLauncherLayoutManager appLauncherLayoutManager = null;
//    private GuiceComponentProvider componentProvider = null;
//    private AppControllerImpl appController = null;
//    private AppEventCollector eventCollector = null;
//    private String name = "app";
//    private String subAppName_1 = "subApp1";
//
//    @Before
//    public void setUp() throws Exception {
//        setAppLayoutManager();
//
//        // Creates a ModuleRegistry with a single module defining a AppEventBusConfigurer for components 'app'
//        ConfigurerDefinition configurerDefinition = new ConfigurerDefinition();
//        configurerDefinition.setClassName(AppEventBusConfigurer.class.getName());
//        ComponentsDefinition components = new ComponentsDefinition();
//        components.setId("app");
//        components.addConfigurer(configurerDefinition);
//        ModuleDefinition moduleDefinition = new ModuleDefinition();
//        moduleDefinition.addComponents(components);
//        ArrayList<ModuleDefinition> moduleDefinitions = new ArrayList<ModuleDefinition>();
//        moduleDefinitions.add(moduleDefinition);
//        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
//        when(moduleRegistry.getModuleDefinitions()).thenReturn(moduleDefinitions);
//
//        componentProvider = AppControllerImplTest.initComponentProvider();
//        Shell shell = mock(MagnoliaShell.class);
//        MessagesManager messagesManager = mock(MessagesManagerImpl.class);
//        LocationController locationController = mock(LocationController.class);
//        SimpleEventBus eventBus = new SimpleEventBus();
//        eventCollector = new AppEventCollector();
//        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);
//
//        AppDescriptorRegistry appDescriptorRegistry = mock(AppDescriptorRegistry.class);
//
//        appController = new AppControllerImpl(moduleRegistry, componentProvider, appLauncherLayoutManager, locationController, messagesManager, shell, eventBus);
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        componentProvider.destroy();
//        //Reset the static fields
//        AppTestImpl.appNumber = 0;
//        AppTestImpl.res = new HashMap<String, Object>();
//    }
//
//    @Test
//    public void testThatEachAppGetsItsOwnEventBus() {
//
//        // GIVEN
//        String appName = name + "_name";
//        // Start an App that has the AppBuss injected and that also add a dumy handler
//        appController.startIfNotAlreadyRunningThenFocus(appName, new DefaultLocation(Location.LOCATION_TYPE_APP, appName, "", ""));
//
//        // Initial check
//        assertEquals(2, eventCollector.appLifecycleEvent.size());
//        AppControllerImplTest.checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
//        AppControllerImplTest.checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);
//
//        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
//        AppEventTestImpl firstAppInstance = (AppEventTestImpl) AppTestImpl.res.get("TestPageApp0");
//        EventBus firstInstanceBus = firstAppInstance.eventBus;
//        InvocationCountingTestEventHandler firstInstanceHandler = firstAppInstance.handler;
//
//        // Send Event to this Bus
//        firstInstanceBus.fireEvent(new TestEvent());
//
//        // Check the number of invocation of the handler = 1
//        assertEquals(1, firstInstanceHandler.getInvocationCount());
//
//        // Stop the app
//        appController.stopCurrentApp();
//
//        // Start app again
//        appController.startIfNotAlreadyRunningThenFocus(appName, new DefaultLocation(Location.LOCATION_TYPE_APP, appName, "", ""));
//
//        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp1"));
//        AppEventTestImpl secondAppInstance = (AppEventTestImpl) AppTestImpl.res.get("TestPageApp1");
//        EventBus secondInstanceBus = secondAppInstance.eventBus;
//        InvocationCountingTestEventHandler secondInstanceHandler = secondAppInstance.handler;
//
//        // WHEN
//        // Send Event to this Bus
//        secondInstanceBus.fireEvent(new TestEvent());
//
//        // THEN
//        // Check the number of invocation of the handler should stay 1
//        assertEquals(1, firstInstanceHandler.getInvocationCount());
//        assertEquals(1, secondInstanceHandler.getInvocationCount());
//    }
//
//    /**
//     * Init a LayoutManager containing 1 group with one app.
//     */
//    private void setAppLayoutManager() {
//
//        appLauncherLayoutManager = mock(AppLauncherLayoutManagerImpl.class);
//        // create subapps
//        Map<String, SubAppDescriptor> subApps = new HashMap<String, SubAppDescriptor>();
//        subApps.put(subAppName_1, AppTestUtility.createSubAppDescriptor(subAppName_1, AppTestSubApp.class, true));
//
//        //Set cat1 with App1
//        AppDescriptor app = AppTestUtility.createAppDescriptorWithSubApps(name, AppEventTestImpl.class, subApps);
//        AppLauncherGroup cat = AppTestUtility.createAppGroup("cat", app);
//        AppLauncherGroupEntry entry = new AppLauncherGroupEntry();
//        entry.setName(name);
//        entry.setAppDescriptor(app);
//        cat.addApp(entry);
//        AppLauncherLayout appLauncherLayout = new AppLauncherLayout();
//        appLauncherLayout.addGroup(cat);
//        when(appLauncherLayoutManager.getLayoutForCurrentUser()).thenReturn(appLauncherLayout);
//    }
//}
