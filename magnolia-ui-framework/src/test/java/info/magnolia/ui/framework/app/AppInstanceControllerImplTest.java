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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.monitoring.SystemMonitor;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.app.App;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.app.SubAppLifecycleEvent;
import info.magnolia.ui.api.app.SubAppLifecycleEventHandler;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.app.stub.FailedAppStub;
import info.magnolia.ui.framework.app.stub.FailedSubAppStub;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.overlay.ViewAdapter;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;

import com.google.common.collect.Maps;
import com.google.inject.TypeLiteral;
import com.vaadin.ui.CssLayout;

public class AppInstanceControllerImplTest {

    public static final String TEST_APP = "testApp";
    public static final String FAILING_SUB_APP = "testSubApp";

    public static final String SUB_APP_FOO = "foo";
    public static final String SUB_APP_BAR = "bar";


    private MessagesManager messagesManager;
    private AppInstanceControllerImpl appInstanceControllerImpl;
    private SimpleTranslator i18n;

    private ConfiguredAppDescriptor appDescriptor;
    private AppContext appContext;
    private ConfiguredSubAppDescriptor failingSubAppDescriptor;

    private ComponentProvider componentProvider;

    private Map<String, SubAppContext> subAppNamesToContexts = Maps.newHashMap();

    @Before
    public void setUp() throws Exception {
        messagesManager = mock(MessagesManager.class);
        i18n = mock(SimpleTranslator.class);

        final MockWebContext ctx = new MockWebContext();
        ctx.setUser(mock(User.class));

        appDescriptor = new ConfiguredAppDescriptor();
        appContext = mock(AppContext.class);

        appDescriptor.setName(TEST_APP);

        failingSubAppDescriptor = new ConfiguredSubAppDescriptor();
        failingSubAppDescriptor.setName(FAILING_SUB_APP);
        failingSubAppDescriptor.setSubAppClass(FailingToStartSubApp.class);

        final ConfiguredSubAppDescriptor fooDescriptor = new ConfiguredSubAppDescriptor();
        fooDescriptor.setSubAppClass(SelfContextRegisteringSubApp.class);
        fooDescriptor.setName(SUB_APP_FOO);

        final ConfiguredSubAppDescriptor barDescriptor = new ConfiguredSubAppDescriptor();
        barDescriptor.setSubAppClass(SelfContextRegisteringSubApp.class);
        barDescriptor.setName(SUB_APP_BAR);

        appDescriptor.addSubApp(fooDescriptor);
        appDescriptor.addSubApp(barDescriptor);
        appDescriptor.addSubApp(failingSubAppDescriptor);

        MgnlContext.setInstance(ctx);

        appInstanceControllerImpl = new AppInstanceControllerImpl(
                mock(ModuleRegistry.class),
                mock(AppController.class),
                mock(LocationController.class),
                mock(Shell.class),
                messagesManager,
                appDescriptor,
                mock(AppLauncherLayoutManager.class),
                mock(SystemMonitor.class),
                mock(I18nizer.class, new ReturnsArgumentAt(0)),
                i18n,
                ctx);

        componentProvider = initComponentProvider();
        appInstanceControllerImpl.setAppComponentProvider(componentProvider);

        final App app = new BaseApp(appContext, new DefaultAppView() {
            @Override
            public String addSubAppView(View view, String caption, boolean closable) {
                return StringUtils.isBlank(caption) ? super.addSubAppView(view, caption, closable) : caption;
            }
        });

        appInstanceControllerImpl.setApp(app);
    }

    @Test
    public void subAppStartedEventIsFiredOnSubAppCreation() throws Exception {
        // WHEN
        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP, SUB_APP_FOO));

        final SelfContextRegisteringSubApp foo = getSubAppByName(SUB_APP_FOO);

        // THEN
        verify(foo.subAppLifecycleEventHandler, only()).onSubAppStarted(any(SubAppLifecycleEvent.class));
    }

    @Test
    public void subAppStoppedEventFiredOnSubAppTermination() throws Exception {
        // WHEN
        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP, SUB_APP_FOO));
        appInstanceControllerImpl.onClose(SUB_APP_FOO);

        final SelfContextRegisteringSubApp foo = getSubAppByName(SUB_APP_FOO);

        // THEN
        verify(foo.subAppLifecycleEventHandler).onSubAppStarted(any(SubAppLifecycleEvent.class));
        verify(foo.subAppLifecycleEventHandler).onSubAppStopped(any(SubAppLifecycleEvent.class));
        verifyNoMoreInteractions(foo.subAppLifecycleEventHandler);
    }

    @Test
    public void switchingBetweenSubAppsTriggersBlurAndFocusEventsForCorrespondingSubApps() throws Exception {
        // WHEN sub-apps are created
        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP, SUB_APP_FOO));
        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP, SUB_APP_BAR));

        final SelfContextRegisteringSubApp foo = getSubAppByName(SUB_APP_FOO);
        final SelfContextRegisteringSubApp bar = getSubAppByName(SUB_APP_BAR);

        // THEN listeners of each of the sub-apps get notified of corresponding sub-app start
        verify(foo.subAppLifecycleEventHandler).onSubAppStarted(any(SubAppLifecycleEvent.class));
        verify(bar.subAppLifecycleEventHandler).onSubAppStarted(any(SubAppLifecycleEvent.class));

        // WHEN a sub-apps which is not active is focused
        appInstanceControllerImpl.onFocus(SUB_APP_FOO);

        // THEN the previous active sub-app listeners get notified with 'blur' event and the new
        // sub-apps listeners get notified with 'focus' event
        verify(bar.subAppLifecycleEventHandler).onSubAppBlurred(any(SubAppLifecycleEvent.class));
        verify(foo.subAppLifecycleEventHandler).onSubAppFocused(any(SubAppLifecycleEvent.class));

        // and after that no other interactions actually happen
        verifyNoMoreInteractions(foo.subAppLifecycleEventHandler);
        verifyNoMoreInteractions(bar.subAppLifecycleEventHandler);

    }

    @Test
    public void subAppLifecycleEventsAreDispatchedAccordingly() throws Exception {

        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP, SUB_APP_FOO));
        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP, SUB_APP_BAR));


        appInstanceControllerImpl.onFocus(SUB_APP_FOO);
        appInstanceControllerImpl.onClose(SUB_APP_FOO);

        final SelfContextRegisteringSubApp foo = getSubAppByName(SUB_APP_FOO);
        final SelfContextRegisteringSubApp bar = getSubAppByName(SUB_APP_BAR);

        verify(foo.subAppLifecycleEventHandler).onSubAppStarted(any(SubAppLifecycleEvent.class));
        verify(bar.subAppLifecycleEventHandler).onSubAppBlurred(any(SubAppLifecycleEvent.class));
        verify(foo.subAppLifecycleEventHandler).onSubAppFocused(any(SubAppLifecycleEvent.class));
        verify(foo.subAppLifecycleEventHandler).onSubAppStopped(any(SubAppLifecycleEvent.class));
    }

    private SelfContextRegisteringSubApp getSubAppByName(String name) {
        return (SelfContextRegisteringSubApp) subAppNamesToContexts.get(name).getSubApp();
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testStubSubAppLaunchedInCaseOfStartUpFailure() throws Exception {
        // WHEN
        appInstanceControllerImpl.openSubApp(new DefaultLocation("app", TEST_APP, FAILING_SUB_APP));

        // THEN
        SubAppContext activeSubAppContext = appInstanceControllerImpl.getActiveSubAppContext();
        assertThat(activeSubAppContext.getSubApp(), instanceOf(FailedSubAppStub.class));
        verify(messagesManager).sendLocalMessage(any(Message.class));
    }

    @Test
    public void testStubAppLaunchedInCaseOfStartUpFailure() throws Exception {
        // GIVEN
        appDescriptor.setAppClass(FailingToStartApp.class);

        // WHEN
        appInstanceControllerImpl.start(new DefaultLocation("app", TEST_APP, FAILING_SUB_APP));

        // THEN
        assertThat(appInstanceControllerImpl.getApp(), instanceOf(FailedAppStub.class));
        verify(messagesManager).sendLocalMessage(any(Message.class));
    }

    @Test
    public void testSendGroupMessageForwardsToMessagesManager() {
        // GIVEN
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
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.sendLocalMessage(message);

        // THEN
        verify(messagesManager).sendLocalMessage(message);
    }

    @Test
    public void testBroadcastMessageForwardsToMessagesManager() {
        // GIVEN
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.broadcastMessage(message);

        // THEN
        verify(messagesManager).broadcastMessage(message);
    }

    public GuiceComponentProvider initComponentProvider() {
        ComponentProviderConfiguration components = new ComponentProviderConfiguration();
        components.registerInstance(SimpleTranslator.class, i18n);
        components.registerInstance(AppContext.class, appContext);

        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(components);
        builder.exposeGlobally();
        return builder.build(new AbstractGuiceComponentConfigurer() {
            @Override
            protected void configure() {
                // Allow the sub-apps to register themselves se that their contexts can be retrieved by name
                bind(new TypeLiteral<Map<String, SubAppContext>>() {}).toInstance(subAppNamesToContexts);
            }
        });
    }

    public static class SelfContextRegisteringSubApp extends BaseSubApp {

        SubAppLifecycleEventHandler subAppLifecycleEventHandler;

        @Inject
        public SelfContextRegisteringSubApp(SubAppContext subAppContext, @Named(SubAppEventBus.NAME) EventBus eventBus, Map<String, SubAppContext> subAppNamesToContext) {
            super(subAppContext, mock(View.class));
            subAppLifecycleEventHandler = mock(SubAppLifecycleEventHandler.class);
            eventBus.addHandler(SubAppLifecycleEvent.class, subAppLifecycleEventHandler);
            subAppNamesToContext.put(subAppContext.getSubAppDescriptor().getName(), subAppContext);
        }

        @Override
        public String getCaption() {
            return getSubAppContext().getSubAppDescriptor().getName();
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
