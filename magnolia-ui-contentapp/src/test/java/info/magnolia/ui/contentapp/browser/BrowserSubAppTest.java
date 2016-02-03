/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarGroupDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarItemDefinition;
import info.magnolia.ui.actionbar.definition.ConfiguredActionbarSectionDefinition;
import info.magnolia.ui.api.action.AbstractActionExecutor;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.availability.IsDeletedRule;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.overlay.AlertCallback;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.overlay.NotificationCallback;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests.
 */
public class BrowserSubAppTest extends MgnlTestCase {

    private final static String WORKSPACE = "workspace";
    private final static String ROOT_PATH = "/";
    private final static String TEST_CONTENT_NODE = "testContentNode";
    private final static String TEST_PAGE = "testPage";
    private final static String TEST_PROPERTY = "testProperty";

    private final static String REQUIRED_ROLE = "required-role";

    private final static String ALWAYS = "always";
    private final static String ROOT_ONLY = "rootOnly";

    private final static String[] ALL_ACTIONS = { ALWAYS, ROOT_ONLY };
    private final static String[] ONE_ACTION = { ALWAYS };

    private final static String SECTION_TO_SHOW = "sectionToShow";
    private final static String SECTION_TO_HIDE = "sectionToHide";

    // components
    private MockSession session;
    private SubAppContext subAppContext;
    private BrowserSubApp subApp;
    private SimpleActionExecutor actionExecutor;
    private EventBus subAppEventBus;
    private ContentSubAppView view;
    private BrowserPresenter browserPresenter;
    private TestActionbarPresenter testActionbarPresenter;
    private ComponentProvider componentProvider;

    // actions
    private ConfiguredActionDefinition actAlways;
    private ConfiguredActionDefinition actRootOnly;

    // section availability
    private ConfiguredAvailabilityDefinition sAvailabilityAlways;

    // testActionbarPresenter definition
    private ConfiguredActionbarSectionDefinition sectionToShow;
    private ConfiguredActionbarSectionDefinition sectionToHide;

    // nodes
    private Node testContentNode;

    private I18nizer i18nizer;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        componentProvider = mock(ComponentProvider.class);
        i18nizer = mock(I18nizer.class);
        doReturn(mock(IsDeletedRule.class)).when(componentProvider).newInstance(any(Class.class), anyVararg());

        initActions();
        session = new MockSession(WORKSPACE);
        testContentNode = NodeUtil.createPath(session.getRootNode(), TEST_CONTENT_NODE, NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(testContentNode, TEST_PROPERTY, "test");
        Node testPage = NodeUtil.createPath(session.getRootNode(), TEST_PAGE, NodeTypes.Page.NAME);
        PropertyUtil.setProperty(testPage, TEST_PROPERTY, "test");

        MockWebContext ctx = new MockWebContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(createMockUser("normal"));
        MgnlContext.setInstance(ctx);
        initActionbarGroupsAndSections();
        initSubAppComponents();

        sectionToShow.setAvailability(sAvailabilityAlways);
        initBrowser();
        subApp = new BrowserSubApp(actionExecutor, subAppContext, view, browserPresenter, subAppEventBus, componentProvider);
    }

    @Test
    public void testLocationChangedWithUnknownViewType() {
        // GIVEN

        // 1. fake basic viewType configuration for the mock browserPresenter (only knows about defaultView)
        final String defaultViewType = "defaultView";
        when(browserPresenter.getDefaultViewType()).thenReturn(defaultViewType);
        when(browserPresenter.hasViewType(anyString())).thenAnswer(new Answer<Boolean>() {

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                // only return true for our fake defaultViewType, not for the unknownView
                return defaultViewType.equals(args[0]);
            }
        });

        // 2. make sure subAppContext will get updates of current location
        AppContext mockAppContext = mock(AppContext.class);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                subAppContext.setLocation((Location) args[1]);
                return null;
            }
        }).when(mockAppContext).updateSubAppLocation(any(SubAppContext.class), any(Location.class));
        subAppContext.setAppContext(mockAppContext);

        // 3. some location with unknown viewType
        DefaultLocation location = new DefaultLocation("app", "someApp", "someContentApp", "/some/node:unknownView");
        subAppContext.setLocation(location);

        // WHEN
        subApp.locationChanged(location);

        // THEN
        assertNotNull(subApp.getCurrentLocation().getViewType());
        assertEquals(defaultViewType, subApp.getCurrentLocation().getViewType());
    }

    @Test
    public void testAlwaysVisibleSectionOnRoot() throws Exception {
        // GIVEN
        List<String> ids = new ArrayList<String>(1);
        ids.add(JcrItemUtil.getItemId(session.getRootNode()));
        when(browserPresenter.getSelectedItemIds()).thenReturn(ids);

        // WHEN
        subApp.updateActionbar(browserPresenter.getActionbarPresenter());

        // THEN
        assertEquals(1, testActionbarPresenter.visibleSections.size());
        assertTrue(testActionbarPresenter.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(testActionbarPresenter.enabledActions.contains(ALWAYS));
        assertTrue(testActionbarPresenter.enabledActions.contains(ROOT_ONLY));
    }

    @Test
    public void testAlwaysVisibleSectionOnNonRootNode() throws Exception {
        // GIVEN
        List<String> ids = new ArrayList<String>(1);
        ids.add(JcrItemUtil.getItemId(testContentNode));
        when(browserPresenter.getSelectedItemIds()).thenReturn(ids);

        // WHEN
        subApp.updateActionbar(browserPresenter.getActionbarPresenter());

        // THEN
        assertEquals(1, testActionbarPresenter.visibleSections.size());
        assertTrue(testActionbarPresenter.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(testActionbarPresenter.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(testActionbarPresenter.enabledActions.contains(ALWAYS));
        assertFalse(testActionbarPresenter.enabledActions.contains(ROOT_ONLY));
    }

    @Test
    public void testAlwaysVisibleSectionOnProperty() throws Exception {
        // GIVEN
        List<String> ids = new ArrayList<String>(1);
        ids.add(JcrItemUtil.getItemId(testContentNode.getProperty(TEST_PROPERTY)));
        when(browserPresenter.getSelectedItemIds()).thenReturn(ids);

        // WHEN
        subApp.updateActionbar(browserPresenter.getActionbarPresenter());

        // THEN
        assertEquals(1, testActionbarPresenter.visibleSections.size());
        assertTrue(testActionbarPresenter.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(testActionbarPresenter.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(testActionbarPresenter.enabledActions.contains(ALWAYS));
        assertFalse(testActionbarPresenter.enabledActions.contains(ROOT_ONLY));
    }

    // HELPER METHODS

    private void initSubAppComponents() {
        // initialize test instance
        actionExecutor = createSimpleActionExecutor();
        actionExecutor.add(actAlways);
        actionExecutor.add(actRootOnly);

        subAppEventBus = mock(EventBus.class);
        view = mock(ContentSubAppView.class);
    }

    private void initBrowser() {
        ConfiguredActionbarDefinition definition = new ConfiguredActionbarDefinition();
        definition.addSection(sectionToShow);
        definition.addSection(sectionToHide);
        testActionbarPresenter = new TestActionbarPresenter();
        browserPresenter = mock(BrowserPresenter.class);
        when(browserPresenter.getActionbarPresenter()).thenReturn(testActionbarPresenter);

        testActionbarPresenter.setListener(browserPresenter);
        testActionbarPresenter.start(definition);

        ConfiguredWorkbenchDefinition wbDef = new ConfiguredWorkbenchDefinition();
        wbDef.setWorkspace(WORKSPACE);
        wbDef.setPath(ROOT_PATH);
        ConfiguredBrowserSubAppDescriptor descriptor = new ConfiguredBrowserSubAppDescriptor();
        descriptor.setWorkbench(wbDef);
        descriptor.setActionbar(definition);

        subAppContext = new TestSubAppContext(descriptor);
    }

    private void initActionbarGroupsAndSections() {
        ConfiguredActionbarGroupDefinition allActionsGroup = new ConfiguredActionbarGroupDefinition();
        allActionsGroup.setName("allActionsGroup");
        allActionsGroup.setItems(createActionbarItemDefinitionList(ALL_ACTIONS));

        ConfiguredActionbarGroupDefinition oneActionGroup = new ConfiguredActionbarGroupDefinition();
        oneActionGroup.setName("oneActionGroup");
        oneActionGroup.setItems(createActionbarItemDefinitionList(ONE_ACTION));

        sectionToShow = new ConfiguredActionbarSectionDefinition();
        sectionToShow.setName(SECTION_TO_SHOW);
        sectionToShow.addGroup(allActionsGroup);

        sectionToHide = new ConfiguredActionbarSectionDefinition();
        sectionToHide.setName(SECTION_TO_HIDE);
        sectionToHide.addGroup(oneActionGroup);
    }

    private List<ActionbarItemDefinition> createActionbarItemDefinitionList(String[] actions) {
        List<ActionbarItemDefinition> items = new ArrayList<ActionbarItemDefinition>();
        for (String action : actions) {
            ConfiguredActionbarItemDefinition item = new ConfiguredActionbarItemDefinition();
            item.setName(action);
            items.add(item);
        }
        return items;
    }

    private void initActions() {
        sAvailabilityAlways = new ConfiguredAvailabilityDefinition();
        sAvailabilityAlways.setRoot(true);
        sAvailabilityAlways.setProperties(true);

        ConfiguredAvailabilityDefinition availabilityAlways = new ConfiguredAvailabilityDefinition();
        availabilityAlways.setRoot(true);
        availabilityAlways.setProperties(true);

        ConfiguredAvailabilityDefinition availabilityRootOnly = new ConfiguredAvailabilityDefinition();
        availabilityRootOnly.setRoot(true);
        availabilityRootOnly.setNodes(false);

        ConfiguredAvailabilityDefinition availabilityRootAndNodes = new ConfiguredAvailabilityDefinition();
        availabilityRootAndNodes.setRoot(true);

        actAlways = new ConfiguredActionDefinition();
        actAlways.setName(ALWAYS);
        actAlways.setAvailability(availabilityAlways);

        actRootOnly = new ConfiguredActionDefinition();
        actRootOnly.setName(ROOT_ONLY);
        actRootOnly.setAvailability(availabilityRootOnly);

        ConfiguredAccessDefinition access = new ConfiguredAccessDefinition();
        access.setRoles(Arrays.asList(REQUIRED_ROLE));

    }

    private User createMockUser(String name) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);
        return user;
    }

    private static class SimpleActionExecutor extends AbstractActionExecutor {

        private List<ActionDefinition> definitions = new ArrayList<ActionDefinition>();

        @Inject
        public SimpleActionExecutor(ComponentProvider componentProvider) {
            super(componentProvider);
        }

        public boolean add(ActionDefinition actionDefinition) {
            return definitions.add(actionDefinition);
        }

        @Override
        public ActionDefinition getActionDefinition(String actionName) {
            for (ActionDefinition definition : definitions) {
                if (definition.getName() != null && definition.getName().equals(actionName))
                    return definition;
            }
            return null;
        }
    }

    private SimpleActionExecutor createSimpleActionExecutor() {
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        ComponentProviderConfiguration componentProviderConfig = new ComponentProviderConfiguration();
        builder.withConfiguration(componentProviderConfig);
        GuiceComponentProvider componentProvider = builder.build();
        return new SimpleActionExecutor(componentProvider);
    }

    private static class TestActionbarPresenter extends ActionbarPresenter {
        public TestActionbarPresenter() {
            super();
        }

        public Set<String> visibleSections;
        public Set<String> enabledActions;

        @Override
        public ActionbarView start(ActionbarDefinition def) {
            visibleSections = new HashSet<String>();
            enabledActions = new HashSet<String>();
            return super.start(def);
        }

        @Override
        public void showSection(String... sectionNames) {
            super.showSection(sectionNames);
            visibleSections.addAll(Arrays.asList(sectionNames));
        }

        @Override
        public void hideSection(String... sectionNames) {
            super.hideSection(sectionNames);
            for (String section : sectionNames) {
                visibleSections.remove(section);
            }
        }

        @Override
        public void enable(String... actionNames) {
            super.enable(actionNames);
            enabledActions.addAll(Arrays.asList(actionNames));
        }

        @Override
        public void disable(String... actionNames) {
            super.disable(actionNames);
            enabledActions.removeAll(Arrays.asList(actionNames));
        }
    }

    /**
     * Basic Empty implementation of {@link SubAppContext} for test purpose.
     */
    public static class TestSubAppContext implements SubAppContext {

        private Location location;
        private AppContext appContext;
        private SubAppDescriptor descriptor;

        public TestSubAppContext(SubAppDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public OverlayCloser openOverlay(View view) {
            return null;
        }

        @Override
        public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
            return null;
        }

        @Override
        public void openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb) {
        }

        @Override
        public void openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        }

        @Override
        public void openConfirmation(MessageStyleType type, View viewToShow, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        }

        @Override
        public void openConfirmation(MessageStyleType type, String title, String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, View viewToShow) {
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, String title) {
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, NotificationCallback cb) {
        }

        @Override
        public String getSubAppId() {
            return null;
        }

        @Override
        public SubApp getSubApp() {
            return null;
        }

        @Override
        public Location getLocation() {
            return location;
        }

        @Override
        public AppContext getAppContext() {
            return appContext;
        }

        @Override
        public SubAppDescriptor getSubAppDescriptor() {
            return descriptor;
        }

        @Override
        public void setAppContext(AppContext appContext) {
            this.appContext = appContext;
        }

        @Override
        public void setLocation(Location location) {
            this.location = location;
        }

        @Override
        public void setSubApp(SubApp subApp) {
        }

        @Override
        public void setInstanceId(String instanceId) {
        }

        @Override
        public String getInstanceId() {
            return null;
        }

        @Override
        public void close() {
        }

    }

}
