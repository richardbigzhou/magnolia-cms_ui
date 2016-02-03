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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.InvocationCountingTestEventHandler;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.event.TestEvent;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.monitoring.SystemMonitor;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.registry.RegistrationException;
import info.magnolia.test.mock.MockWebContext;
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
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.Viewport;
import info.magnolia.ui.framework.app.AppControllerImplTest.AppEventCollector;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.message.MessagesManagerImpl;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * Test case for {@link info.magnolia.ui.api.app.AppController} local
 * App's event.
 */
public class AppEventTest {

    private GuiceComponentProvider componentProvider = null;
    private AppControllerImpl appController = null;
    private AppEventCollector eventCollector = null;
    private String name = "app";
    private String subAppName_1 = "subApp1";
    private SimpleEventBus eventBus;
    private AppDescriptorRegistry appRegistry;
    private MockWebContext ctx;

    @Before
    public void setUp() throws Exception {
        initAppRegistry();

        this.eventBus = new SimpleEventBus();
        componentProvider = initComponentProvider();

        eventCollector = new AppEventCollector();
        eventBus.addHandler(AppLifecycleEvent.class, eventCollector);

        this.appController = (AppControllerImpl) componentProvider.getComponent(AppController.class);
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

        MgnlContext.setInstance(null);
    }

    @Test
    public void testThatEachAppGetsItsOwnEventBus() {

        // GIVEN
        String appName = name + "_name";
        // Start an App that has the AppBuss injected and that also add a dumy
        // handler
        appController
                .startIfNotAlreadyRunningThenFocus(appName,
                        new DefaultLocation(Location.LOCATION_TYPE_APP,
                                appName, "", ""));

        // Initial check
        assertEquals(2, eventCollector.appLifecycleEvent.size());
        AppControllerImplTest.checkAppEvent(eventCollector, appName, AppLifecycleEventType.STARTED, 0);
        AppControllerImplTest.checkAppEvent(eventCollector, appName, AppLifecycleEventType.FOCUSED, 1);

        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp0"));
        AppEventTestImpl firstAppInstance = (AppEventTestImpl) AppTestImpl.res.get("TestPageApp0");
        EventBus firstInstanceBus = firstAppInstance.eventBus;
        InvocationCountingTestEventHandler firstInstanceHandler = firstAppInstance.handler;

        // Send Event to this Bus
        firstInstanceBus.fireEvent(new TestEvent());

        // Check the number of invocation of the handler = 1
        assertEquals(1, firstInstanceHandler.getInvocationCount());

        // Stop the app
        appController.stopCurrentApp();

        // Start app again
        appController
                .startIfNotAlreadyRunningThenFocus(appName,
                        new DefaultLocation(Location.LOCATION_TYPE_APP,
                                appName, "", ""));

        assertEquals(true, AppTestImpl.res.containsKey("TestPageApp1"));
        AppEventTestImpl secondAppInstance = (AppEventTestImpl) AppTestImpl.res.get("TestPageApp1");
        EventBus secondInstanceBus = secondAppInstance.eventBus;
        InvocationCountingTestEventHandler secondInstanceHandler = secondAppInstance.handler;

        // WHEN
        // Send Event to this Bus
        secondInstanceBus.fireEvent(new TestEvent());

        // THEN
        // Check the number of invocation of the handler should stay 1
        assertEquals(1, firstInstanceHandler.getInvocationCount());
        assertEquals(1, secondInstanceHandler.getInvocationCount());
    }

    @Test
    public void appNotifiedOfStoppingThroughAnEvent() throws Exception {
        // GIVEN
        String appName = String.format("%s_name", name);

        appController.startIfNotAlreadyRunningThenFocus(appName, new DefaultLocation(Location.LOCATION_TYPE_APP, appName, "", ""));
        AppEventTestImpl firstAppInstance = (AppEventTestImpl) AppTestImpl.res.get("TestPageApp0");
        final AppLifecycleEventHandler mock = mock(AppLifecycleEventHandler.class);
        firstAppInstance.admincentralEventBus.addHandler(AppLifecycleEvent.class, mock);

        // WHEN
        appController.stopApp(appName);

        // THEN
        verify(mock, only()).onAppStopped(any(AppLifecycleEvent.class));
    }
    

    /**
     * Init a LayoutManager containing 1 group with one app.
     */
    private void initAppRegistry() {

        this.appRegistry = mock(AppDescriptorRegistry.class);

        // create subapps
        Map<String, SubAppDescriptor> subApps = new HashMap<String, SubAppDescriptor>();
        subApps.put(subAppName_1, AppTestUtility.createSubAppDescriptor(
                subAppName_1, AppTestSubApp.class, true));

        AppDescriptor app = AppTestUtility.createAppDescriptorWithSubApps(name,
                AppEventTestImpl.class, subApps);
        try {
            when(appRegistry.getAppDescriptor(name + "_name")).thenReturn(app);
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
        components.registerImplementation(AppLauncherLayoutManager.class, AppLauncherLayoutManagerImpl.class);
        final SystemMonitor systemMonitor = mock(SystemMonitor.class);
        when(systemMonitor.isMemoryLimitReached()).thenReturn(false);
        components.registerInstance(SystemMonitor.class, systemMonitor);

        components.registerInstance(ModuleRegistry.class, mock(ModuleRegistry.class));
        components.registerInstance(AppDescriptorRegistry.class, appRegistry);
        components.registerInstance(Shell.class, mock(Shell.class));
        components.registerInstance(MessagesManager.class, mock(MessagesManagerImpl.class));

        components.registerInstance(I18nizer.class, new I18nizer() {
            @Override
            public <C> C decorate(C child) {
                return child;
            }
        });

        components.registerInstance(TranslationService.class, mock(TranslationService.class));
        components.registerInstance(LocaleProvider.class, mock(LocaleProvider.class));
        components.registerInstance(info.magnolia.cms.i18n.MessagesManager.class, mock(info.magnolia.cms.i18n.MessagesManager.class));

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
}
