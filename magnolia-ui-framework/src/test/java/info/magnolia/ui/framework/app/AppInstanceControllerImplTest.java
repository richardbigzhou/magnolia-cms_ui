/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.monitoring.SystemMonitor;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.app.App;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.app.stub.FailedAppStub;
import info.magnolia.ui.framework.app.stub.FailedSubAppStub;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.message.MessagesManagerImpl;
import info.magnolia.ui.framework.overlay.ViewAdapter;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.vaadin.ui.CssLayout;

/**
 * {@link info.magnolia.ui.framework.app.AppInstanceControllerImpl} tests.
 */
public class AppInstanceControllerImplTest {

    public static final String TEST_APP = "testApp";
    public static final String TEST_SUB_APP = "testSubApp";
    public static final String USER = "user";
    private MessagesManager messagesManager;
    private AppInstanceControllerImpl appInstanceControllerImpl;
    private SimpleTranslator i18n;
    private MockWebContext ctx = new MockWebContext();
    private ModuleRegistry moduleRegistry;
    private AppController appController;
    private ConfiguredAppDescriptor appDescriptor;
    private AppContext appContext;
    private ConfiguredSubAppDescriptor subAppDescriptor;
    private I18nizer i18nizer = new I18nizer() {
        @Override
        public <C> C decorate(C child) {
            return child;
        }
    };

    @Before
    public void setUp() throws Exception {
        messagesManager = mock(MessagesManager.class);
        i18n = mock(SimpleTranslator.class);
        ctx.setUser(new MgnlUser(USER, "", Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP));

        moduleRegistry = mock(ModuleRegistry.class);
        appController = mock(AppController.class);
        appDescriptor = new ConfiguredAppDescriptor();
        appContext = mock(AppContext.class);

        appDescriptor.setName(TEST_APP);
        subAppDescriptor = new ConfiguredSubAppDescriptor();
        subAppDescriptor.setName(TEST_SUB_APP);
        subAppDescriptor.setSubAppClass(FailingToStartSubApp.class);

        appDescriptor.addSubApp(subAppDescriptor);

        appInstanceControllerImpl = new AppInstanceControllerImpl(
                moduleRegistry,
                appController,
                mock(LocationController.class),
                mock(Shell.class),
                messagesManager,
                appDescriptor,
                mock(AppLauncherLayoutManager.class),
                mock(SystemMonitor.class),
                i18nizer,
                i18n);
        appInstanceControllerImpl.setAppComponentProvider(initComponentProvider());

        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testStubSubAppLaunchedInCaseOfStartUpFailure() throws Exception {
        // GIVEN

        final App app = new BaseApp(appContext, new DefaultAppView());
        appInstanceControllerImpl.setApp(app);

        // WHEN
        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP));

        // THEN
        SubAppContext activeSubAppContext = appInstanceControllerImpl.getActiveSubAppContext();
        assertThat(activeSubAppContext.getSubApp(), instanceOf(FailedSubAppStub.class));
        verify(messagesManager).sendLocalMessage(org.mockito.Matchers.any(Message.class));
    }

    @Test
    public void testStubAppLaunchedInCaseOfStartUpFailure() throws Exception {
        // GIVEN
        appDescriptor.setAppClass(FailingToStartApp.class);

        // WHEN
        appInstanceControllerImpl.start(new DefaultLocation("app", TEST_APP));

        // THEN
        assertThat(appInstanceControllerImpl.getApp(), instanceOf(FailedAppStub.class));
        verify(messagesManager).sendLocalMessage(org.mockito.Matchers.any(Message.class));
    }

    @Test
    public void testSendGroupMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final String testGroup = "test";
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.sendGroupMessage(testGroup, message);

        // THEN
        verify(messagesManager).sendGroupMessage(testGroup, message);
    }

    @Test
    public void testSendUserMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final String testUser = "test";
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.sendUserMessage(testUser, message);

        // THEN
        verify(messagesManager).sendMessage(testUser, message);
    }

    @Test
    public void testSendLocalMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.sendLocalMessage(message);

        // THEN
        verify(messagesManager).sendLocalMessage(message);
    }

    @Test
    public void testBroadcastMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.broadcastMessage(message);

        // THEN
        verify(messagesManager).broadcastMessage(message);
    }

    public GuiceComponentProvider initComponentProvider() {

        ComponentProviderConfiguration components = new ComponentProviderConfiguration();

        components.registerImplementation(AppController.class, AppControllerImpl.class);
        components.registerImplementation(AppView.class, DefaultAppView.class);
        components.registerImplementation(LocationController.class);

        components.registerInstance(ModuleRegistry.class, mock(ModuleRegistry.class));

        components.registerInstance(Shell.class, mock(Shell.class));
        components.registerInstance(MessagesManager.class, mock(MessagesManagerImpl.class));

        components.registerInstance(I18nizer.class, i18nizer);
        components.registerInstance(SimpleTranslator.class, i18n);
        components.registerInstance(AppContext.class, appContext);

        components.registerInstance(TranslationService.class, mock(TranslationService.class));
        components.registerInstance(LocaleProvider.class, mock(LocaleProvider.class));
        components.registerInstance(info.magnolia.cms.i18n.MessagesManager.class, mock(info.magnolia.cms.i18n.MessagesManager.class));

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();

        TestEventBusConfigurer eventBusConfigurer = new TestEventBusConfigurer(new SimpleEventBus());

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
     * Sub-app that fails during the #start() method call.
     */
    public static class FailingToStartSubApp extends BaseSubApp {

        public FailingToStartSubApp(SubAppContext subAppContext) {
            super(subAppContext, new ViewAdapter(new CssLayout()));
        }

        @Override
        public View start(Location location) {
            throw new RuntimeException("Problems during sub-app start");
        }
    }

    /**
     * App that fails during the #start() method call.
     */
    public static class FailingToStartApp extends BaseApp {

        @Inject
        public FailingToStartApp(AppContext appContext) {
            super(appContext, new DefaultAppView());
        }

        @Override
        public void start(Location location) {
            throw new RuntimeException("App failed to start!");
        }
    }
}
