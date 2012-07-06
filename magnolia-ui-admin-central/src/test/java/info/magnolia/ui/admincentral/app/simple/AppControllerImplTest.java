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
import info.magnolia.ui.framework.app.AppLifecycleEventType;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.app.AppView;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLayout;
import info.magnolia.ui.framework.app.layout.AppLayoutImpl;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.app.layout.AppLayoutManagerImpl;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.location.LocationController;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.message.MessagesManagerImpl;
import info.magnolia.ui.framework.shell.Shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import com.google.gwt.editor.client.Editor.Ignore;

/**
 * Test case for {@link info.magnolia.ui.framework.app.AppController}.
 */
public class AppControllerImplTest {

    private AppLayoutManager appLayoutManager = null;
    private GuiceComponentProvider componentProvider = null;
    private AppControllerImpl appControler = null;
    private AppEventCollector eventCollector = null;
    private String appName_1 = "app1";
    private String appName_2 = "app2";

    @Before
    public void setUp() throws Exception{
        setAppLayoutManager();
        ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        componentProvider = initComponentProvider();
        Shell shell = mock(MagnoliaShell.class);
        MessagesManager messagesManager = mock(MessagesManagerImpl.class);
        LocationController locationController = mock(LocationController.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        appControler = new AppControllerImpl(moduleRegistry, componentProvider, appLayoutManager, locationController, shell, eventBus, messagesManager);
    }

    @After
    public void tearDown() throws Exception{
        componentProvider.destroy();
        //Reset the static fields
        AppTestImpl.appNumber = 0;
        AppTestImpl.res = new HashMap<String, Object>();
    }



    @Ignore
    public void TestStartIfNotAlreadyRunning_Basic() {
        // GIVEN
        String appName = appName_1+"_name";

        // WHEN
        appControler.startIfNotAlreadyRunning(appName);

        // THEN
        //Check Events
        assertEquals(1, eventCollector.appLifecycleEvent.size());
        assertEquals(AppLifecycleEventType.STARTED, eventCollector.appLifecycleEvent.get(0).getEventType());
        //Check App
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl)AppTestImpl.res.get("TestPageApp0");
        assertEquals(1, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));
        //Check injection
        assertNotNull(pageApp.ctx);
        assertNotNull(pageApp.view);
        //Check AppContext
        assertEquals("app:app1_name", pageApp.getDefaultLocation().toString());
    }

    @Ignore
    public void TestStartIfNotAlreadyRunningThenFocus_Basic() {
        // GIVEN
        String appName = appName_1+"_name";

        // WHEN
        appControler.startIfNotAlreadyRunningThenFocus(appName);

        // THEN
        //Check Events
        assertEquals(2, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);
        //Check App
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl)AppTestImpl.res.get("TestPageApp0");
        assertEquals(1, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));
        //Check AppContext
        assertEquals("app:app1_name", pageApp.getDefaultLocation().toString());
    }

    @Ignore
    public void TestStopApp_oneApp() {
        // GIVEN
        String appName = appName_1+"_name";
        appControler.startIfNotAlreadyRunningThenFocus(appName);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp = (AppTestImpl)AppTestImpl.res.get("TestPageApp0");
        assertEquals(1, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));

        // WHEN
        appControler.stopApp(appName);

        // THEN
        assertEquals(3, eventCollector.appLifecycleEvent.size());
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);
        checkAppEvent(eventCollector, appName, AppLifecycleEventType.STOPPED, 2);
        assertEquals(2, pageApp.events.size());
        assertEquals(true, pageApp.events.get(0).startsWith("start()"));
        assertEquals(true, pageApp.events.get(1).startsWith("stop()"));
    }

    @Ignore
    public void TestStopApp_twoApp() {
        // GIVEN
        //Start first App
        String appName1 = appName_1+"_name";
        appControler.startIfNotAlreadyRunningThenFocus(appName1);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl pageApp1 = (AppTestImpl)AppTestImpl.res.get("TestPageApp0");

        //Start second App
        String appName2 = appName_2+"_name";
        appControler.startIfNotAlreadyRunningThenFocus(appName2);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp1"));
        AppTestImpl pageApp2 = (AppTestImpl)AppTestImpl.res.get("TestPageApp1");

        // WHEN
        appControler.stopApp(appName2);

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


    @Ignore
    public void TestStopCurrentApp() {
        // GIVEN
        //Start first App
        String appName1 = appName_1+"_name";
        appControler.startIfNotAlreadyRunningThenFocus(appName1);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppTestImpl.res.get("TestPageApp0");

        //Start second App
        String appName2 = appName_2+"_name";
        appControler.startIfNotAlreadyRunningThenFocus(appName2);
        //Check
        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp1"));
        AppTestImpl pageApp2 = (AppTestImpl)AppTestImpl.res.get("TestPageApp1");

        // WHEN
        appControler.stopCurrentApp();

        // THEN
        assertEquals(2, pageApp2.events.size());
        assertEquals(true, pageApp2.events.get(0).startsWith("start()"));
        assertEquals(true, pageApp2.events.get(1).startsWith("stop()"));
    }

    @Ignore
    public void TestIsAppStarted() {
        // GIVEN
        String appName1 = appName_1+"_name";
        //Check
        assertEquals(false , appControler.isAppStarted(appName1));
        //Start App
        appControler.startIfNotAlreadyRunningThenFocus(appName1);
        //Check
        assertEquals(true , appControler.isAppStarted(appName1));

        // WHEN
        appControler.stopCurrentApp();

        // THEN
        assertEquals(false , appControler.isAppStarted(appName1));
    }

    /**
     * Init a LayoutManager containing 2 category (cat1 and cat2) with
     * one app each (app1 and app2) linket to {TestApp}.
     */
    private void setAppLayoutManager() {

        appLayoutManager = mock(AppLayoutManagerImpl.class);
        //Set cat1 with App1
        AppDescriptor app1 = AppTestUtility.createAppDescriptor(appName_1, AppTestImpl.class);
        AppCategory cat1 = AppTestUtility.createAppCategory("cat1", app1);
        //Set cat2 with App2
        AppDescriptor app2 = AppTestUtility.createAppDescriptor("app2", AppTestImpl.class);
        AppCategory cat2 =AppTestUtility.createAppCategory("cat2", app2);
        cat2.addApp(app2);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put("cat1",cat1);
        categories.put("cat2",cat2);

        AppLayout appLayout = new AppLayoutImpl(categories);

        when(appLayoutManager.getLayout()).thenReturn(appLayout);
    }


    public static GuiceComponentProvider initComponentProvider() {
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        ComponentProviderConfiguration components = new ComponentProviderConfiguration();
        //Register PagesView
        components.registerImplementation(AppView.class, AppViewTestImpl.class);
        builder.withConfiguration(components);
        builder.exposeGlobally();
        return  builder.build();
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
        assertEquals(eventType,  eventCollector.appLifecycleEvent.get(position).getEventType());
        assertEquals(appName, eventCollector.appLifecycleEvent.get(position).getAppDescriptor().getName());
    }
}
