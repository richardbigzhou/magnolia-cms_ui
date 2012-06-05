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
package info.magnolia.ui.framework.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.app.layout.AppLauncherLayout;
import info.magnolia.ui.framework.app.layout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.framework.event.SimpleEventBus;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test case for {@link AppController}.
 *
 * @version $Id$
 */
public class AppControllerImplTest {

    public static class EventCollectingApp implements AppLifecycle {

        List<String> eventsReceived = new ArrayList<String>();

        @Override
        public void start() {
            eventsReceived.add("start");
        }

        @Override
        public void focus() {
            eventsReceived.add("focus");
        }

        @Override
        public void stop() {
            eventsReceived.add("stop");
        }
    }

    public static class TestApp extends EventCollectingApp {

    }

    public static class AnotherTestApp extends EventCollectingApp {

    }

    public static class AppEventCollector implements AppLifecycleEventHandler {

        List<AppLifecycleEvent> events = new ArrayList<AppLifecycleEvent>();

        @Override
        public void onAppFocused(AppLifecycleEvent event) {
            events.add(event);
        }

        @Override
        public void onAppStopped(AppLifecycleEvent event) {
            events.add(event);
        }

        @Override
        public void onAppStarted(AppLifecycleEvent event) {
            events.add(event);
        }

        @Override
        public void onAppRegistered(AppLifecycleEvent event) {
            events.add(event);
        }

        @Override
        public void onAppUnregistered(AppLifecycleEvent event) {
            events.add(event);
        }

        @Override
        public void onAppReRegistered(AppLifecycleEvent event) {
            events.add(event);
        }
    }

    @Test
    public void testStartIfNotAlreadyRunning() {

        // GIVEN
        ConfiguredAppDescriptor appDescriptor = new ConfiguredAppDescriptor();
        appDescriptor.setName("test");
        appDescriptor.setAppClass(TestApp.class);

        TestApp app = new TestApp();

        SimpleEventBus eventBus = new SimpleEventBus();
        AppEventCollector eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        AppLauncherLayoutManager appLauncherLayoutManager = mock(AppLauncherLayoutManager.class);
        AppLauncherLayout appLauncherLayout = mock(AppLauncherLayout.class);
        when(appLauncherLayoutManager.getLayout()).thenReturn(appLauncherLayout);
        when(appLauncherLayout.getAppDescriptor("test")).thenReturn(appDescriptor);

        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(same(TestApp.class))).thenReturn(app);

        AppControllerImpl appController = new AppControllerImpl(appLauncherLayoutManager, componentProvider, eventBus);

        // WHEN
        appController.startIfNotAlreadyRunning("test");

        // THEN
        assertEquals(1, app.eventsReceived.size());
        assertEquals("start", app.eventsReceived.get(0));

        assertEquals(1, eventCollector.events.size());
        assertEquals(AppEventType.STARTED, eventCollector.events.get(0).getEventType());
        assertSame(appDescriptor, eventCollector.events.get(0).getAppDescriptor());
    }

    @Test
    public void testStartIfNotAlreadyRunningThenFocus() {

        // GIVEN
        ConfiguredAppDescriptor appDescriptor = new ConfiguredAppDescriptor();
        appDescriptor.setName("test");
        appDescriptor.setAppClass(TestApp.class);

        TestApp app = new TestApp();

        SimpleEventBus eventBus = new SimpleEventBus();
        AppEventCollector eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        AppLauncherLayoutManager appLauncherLayoutManager = mock(AppLauncherLayoutManager.class);
        when(appLauncherLayoutManager.getLayout().getAppDescriptor("test")).thenReturn(appDescriptor);

        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(same(TestApp.class))).thenReturn(app);

        AppControllerImpl appController = new AppControllerImpl(appLauncherLayoutManager, componentProvider, eventBus);

        // WHEN
        appController.startIfNotAlreadyRunningThenFocus("test");

        // THEN
        assertEquals(2, app.eventsReceived.size());
        assertEquals("start", app.eventsReceived.get(0));
        assertEquals("focus", app.eventsReceived.get(1));

        assertEquals(2, eventCollector.events.size());
        assertEquals(AppEventType.STARTED, eventCollector.events.get(0).getEventType());
        assertSame(appDescriptor, eventCollector.events.get(0).getAppDescriptor());
        assertEquals(AppEventType.FOCUSED, eventCollector.events.get(1).getEventType());
        assertSame(appDescriptor, eventCollector.events.get(1).getAppDescriptor());
    }

    @Test
    public void testStopApplication() {

        // GIVEN
        ConfiguredAppDescriptor appDescriptor = new ConfiguredAppDescriptor();
        appDescriptor.setName("test");
        appDescriptor.setAppClass(TestApp.class);

        TestApp app = new TestApp();

        SimpleEventBus eventBus = new SimpleEventBus();
        AppEventCollector eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        AppLauncherLayoutManager appLauncherLayoutManager = mock(AppLauncherLayoutManager.class);
        when(appLauncherLayoutManager.getLayout().getAppDescriptor("test")).thenReturn(appDescriptor);

        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(same(TestApp.class))).thenReturn(app);

        AppControllerImpl appController = new AppControllerImpl(appLauncherLayoutManager, componentProvider, eventBus);

        appController.startIfNotAlreadyRunning("test");

        app.eventsReceived.clear();
        eventCollector.events.clear();

        // WHEN
        appController.stopApplication("test");

        // THEN
        assertEquals(1, app.eventsReceived.size());
        assertEquals("stop", app.eventsReceived.get(0));

        assertEquals(1, eventCollector.events.size());
        assertEquals(AppEventType.STOPPED, eventCollector.events.get(0).getEventType());
        assertSame(appDescriptor, eventCollector.events.get(0).getAppDescriptor());
    }

    @Test
    public void testStopCurrentApplication() {

        // GIVEN
        ConfiguredAppDescriptor appDescriptor1 = new ConfiguredAppDescriptor();
        appDescriptor1.setName("test1");
        appDescriptor1.setAppClass(TestApp.class);

        TestApp app1 = new TestApp();

        ConfiguredAppDescriptor appDescriptor2 = new ConfiguredAppDescriptor();
        appDescriptor2.setName("test2");
        appDescriptor2.setAppClass(AnotherTestApp.class);

        AnotherTestApp app2 = new AnotherTestApp();

        SimpleEventBus eventBus = new SimpleEventBus();
        AppEventCollector eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        AppLauncherLayoutManager appLauncherLayoutManager = mock(AppLauncherLayoutManager.class);
        when(appLauncherLayoutManager.getLayout().getAppDescriptor("test1")).thenReturn(appDescriptor1);
        when(appLauncherLayoutManager.getLayout().getAppDescriptor("test2")).thenReturn(appDescriptor2);

        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(eq(TestApp.class))).thenReturn(app1);
        when(componentProvider.newInstance(eq(AnotherTestApp.class))).thenReturn(app2);

        AppControllerImpl appController = new AppControllerImpl(appLauncherLayoutManager, componentProvider, eventBus);

        appController.startIfNotAlreadyRunningThenFocus("test1");
        appController.startIfNotAlreadyRunningThenFocus("test2");

        app1.eventsReceived.clear();
        app2.eventsReceived.clear();
        eventCollector.events.clear();

        // WHEN
        appController.stopCurrentApplication();

        // THEN
        assertEquals(1, app2.eventsReceived.size());
        assertEquals("stop", app2.eventsReceived.get(0));

        assertEquals(2, eventCollector.events.size());
        assertEquals(AppEventType.STOPPED, eventCollector.events.get(0).getEventType());
        assertSame(appDescriptor2, eventCollector.events.get(0).getAppDescriptor());
        assertEquals(AppEventType.FOCUSED, eventCollector.events.get(1).getEventType());
        assertSame(appDescriptor1, eventCollector.events.get(1).getAppDescriptor());
    }
}
