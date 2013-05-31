/**
 * This file Copyright (c) 2013 Magnolia International
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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;

import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mock;

import static org.junit.Assert.*;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
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
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.availability.IsDeletedRule;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.framework.app.SubAppContext;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the BrowserSubApp. Mainly for the updateActionbar() method.
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
    private final static String ROOT_AND_NODES = "rootAndNodes";
    private final static String NODES_AND_PROPS = "nodesAndProps";
    private final static String ONLY_PAGES = "onlyPages";
    private final static String PROPS_ONLY = "propsOnly";
    private final static String ROLE_LIMITED = "roleLimited";
    private final static String DELETED_PAGES = "deletedPages";

    private final static String[] ALL_ACTIONS = { ALWAYS, ROOT_ONLY, ROOT_AND_NODES, NODES_AND_PROPS, ONLY_PAGES, PROPS_ONLY, ROLE_LIMITED, DELETED_PAGES };
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
    private BrowserPresenter workbench;
    private TestActionbarPresenter actionbar;
    private ComponentProvider componentProvider;

    // actions
    private ConfiguredActionDefinition actAlways;
    private ConfiguredActionDefinition actRootOnly;
    private ConfiguredActionDefinition actRootAndNodes;
    private ConfiguredActionDefinition actNodesAndProperties;
    private ConfiguredActionDefinition actOnlyPages;
    private ConfiguredActionDefinition actPropertiesOnly;
    private ConfiguredActionDefinition actRoleLimited;
    private ConfiguredActionDefinition actDeletedPages;

    // action availability
    private ConfiguredAvailabilityDefinition availabilityAlways;
    private ConfiguredAvailabilityDefinition availabilityRootOnly;
    private ConfiguredAvailabilityDefinition availabilityRootAndNodes;
    private ConfiguredAvailabilityDefinition availabilityNodesAndProperties;
    private ConfiguredAvailabilityDefinition availabilityPropertiesOnly;
    private ConfiguredAvailabilityDefinition availabilityOnlyPages;
    private ConfiguredAvailabilityDefinition availabilityRoleLimited;
    private ConfiguredAvailabilityDefinition availabilityDeletedPages;

    // section availability
    private ConfiguredAvailabilityDefinition sAvailabilityAlways;
    private ConfiguredAvailabilityDefinition sAvailabilityRootOnly;
    private ConfiguredAvailabilityDefinition sAvailabilityRootAndNodes;
    private ConfiguredAvailabilityDefinition sAvailabilityNodesAndProperties;
    private ConfiguredAvailabilityDefinition sAvailabilityPropertiesOnly;
    private ConfiguredAvailabilityDefinition sAvailabilityOnlyPages;
    private ConfiguredAvailabilityDefinition sAvailabilityRoleLimited;
    private ConfiguredAvailabilityDefinition sAvailabilityDeletedPages;

    // users
    User normalUser;
    User privilegedUser;

    // actionbar definition
    ConfiguredActionbarDefinition actionbarDefinition;
    ConfiguredActionbarGroupDefinition allActionsGroup;
    ConfiguredActionbarGroupDefinition oneActionGroup;
    ConfiguredActionbarSectionDefinition sectionToShow;
    ConfiguredActionbarSectionDefinition sectionToHide;

    // nodes
    Node testContentNode;
    Node testPage;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        componentProvider = mock(ComponentProvider.class);
        doReturn(mock(IsDeletedRule.class)).when(componentProvider).newInstance(any(Class.class), anyVararg());

        initActionAvailabilityDefs();
        initSectionAvailabilityDefs();
        initActions();
        session = new MockSession(WORKSPACE);
        testContentNode = NodeUtil.createPath(session.getRootNode(), TEST_CONTENT_NODE, NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(testContentNode, TEST_PROPERTY, "test");
        testPage = NodeUtil.createPath(session.getRootNode(), TEST_PAGE, NodeTypes.Page.NAME);
        PropertyUtil.setProperty(testPage, TEST_PROPERTY, "test");

        MockContext ctx = new MockContext();
        normalUser = createMockUser("normal");
        privilegedUser = createMockUser("privileged");
        when(privilegedUser.hasRole(REQUIRED_ROLE)).thenReturn(true);

        ctx.addSession(WORKSPACE, session);
        ctx.setUser(normalUser);
        MgnlContext.setInstance(ctx);
        initActionbarGroups();
        initActionbarSections();
        initSubAppComponents();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    // TESTS

    @Test
    public void testAlwaysVisibleSection() throws Exception {
        // GIVEN
        sectionToShow.setAvailability(sAvailabilityAlways);
        sectionToHide.setAvailability(sAvailabilityDeletedPages);
        initActionbar();
        subApp = new BrowserSubApp(actionExecutor, subAppContext, view, workbench, subAppEventBus, componentProvider);

        // WHEN
        // root
        List<String> ids = new ArrayList<String>(1);
        ids.add(JcrItemUtil.getItemId(session.getRootNode()));
        when(workbench.getSelectedItemIds()).thenReturn(ids);
        subApp.updateActionbar(workbench.getActionbarPresenter());

        // THEN
        assertEquals(1, actionbar.visibleSections.size());
        assertTrue(actionbar.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(actionbar.enabledActions.contains(ALWAYS));
        assertTrue(actionbar.enabledActions.contains(ROOT_ONLY));
        assertTrue(actionbar.enabledActions.contains(ROOT_AND_NODES));
        assertFalse(actionbar.enabledActions.contains(NODES_AND_PROPS));
        assertFalse(actionbar.enabledActions.contains(PROPS_ONLY));
        assertFalse(actionbar.enabledActions.contains(ONLY_PAGES));
        assertFalse(actionbar.enabledActions.contains(DELETED_PAGES));
        assertFalse(actionbar.enabledActions.contains(ROLE_LIMITED));

        // WHEN
        // node
        ids = new ArrayList<String>(1);
        ids.add(JcrItemUtil.getItemId(testContentNode));
        when(workbench.getSelectedItemIds()).thenReturn(ids);
        subApp.updateActionbar(workbench.getActionbarPresenter());

        // THEN
        assertEquals(1, actionbar.visibleSections.size());
        assertTrue(actionbar.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(actionbar.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(actionbar.enabledActions.contains(ALWAYS));
        assertFalse(actionbar.enabledActions.contains(ROOT_ONLY));
        assertTrue(actionbar.enabledActions.contains(ROOT_AND_NODES));
        assertTrue(actionbar.enabledActions.contains(NODES_AND_PROPS));
        assertFalse(actionbar.enabledActions.contains(PROPS_ONLY));
        assertFalse(actionbar.enabledActions.contains(ONLY_PAGES));
        assertFalse(actionbar.enabledActions.contains(DELETED_PAGES));
        assertFalse(actionbar.enabledActions.contains(ROLE_LIMITED));

        // WHEN
        // property
        ids = new ArrayList<String>(1);
        ids.add(JcrItemUtil.getItemId(testContentNode.getProperty(TEST_PROPERTY)));
        when(workbench.getSelectedItemIds()).thenReturn(ids);
        subApp.updateActionbar(workbench.getActionbarPresenter());

        // THEN
        assertEquals(1, actionbar.visibleSections.size());
        assertTrue(actionbar.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(actionbar.visibleSections.contains(SECTION_TO_SHOW));
        assertTrue(actionbar.enabledActions.contains(ALWAYS));
        assertFalse(actionbar.enabledActions.contains(ROOT_ONLY));
        assertFalse(actionbar.enabledActions.contains(ROOT_AND_NODES));
        assertTrue(actionbar.enabledActions.contains(NODES_AND_PROPS));
        assertTrue(actionbar.enabledActions.contains(PROPS_ONLY));
        assertFalse(actionbar.enabledActions.contains(ONLY_PAGES));
        assertFalse(actionbar.enabledActions.contains(DELETED_PAGES));
        assertFalse(actionbar.enabledActions.contains(ROLE_LIMITED));
    }

    // HELPER METHODS

    private void initSubAppComponents() {
        // initialize test instance
        actionExecutor = createSimpleActionExecutor();
        actionExecutor.add(actAlways);
        actionExecutor.add(actRootOnly);
        actionExecutor.add(actRootAndNodes);
        actionExecutor.add(actNodesAndProperties);
        actionExecutor.add(actPropertiesOnly);
        actionExecutor.add(actOnlyPages);
        actionExecutor.add(actDeletedPages);
        actionExecutor.add(actRoleLimited);

        subAppEventBus = mock(EventBus.class);
        view = mock(ContentSubAppView.class);
    }

    private void initActionbar() {
        ConfiguredActionbarDefinition definition = new ConfiguredActionbarDefinition();
        definition.addSection(sectionToShow);
        definition.addSection(sectionToHide);
        actionbar = new TestActionbarPresenter();
        workbench = mock(BrowserPresenter.class);
        when(workbench.getActionbarPresenter()).thenReturn(actionbar);

        actionbar.setListener(workbench);
        actionbar.start(definition);

        ConfiguredWorkbenchDefinition wbDef = new ConfiguredWorkbenchDefinition();
        wbDef.setWorkspace(WORKSPACE);
        wbDef.setPath(ROOT_PATH);
        ConfiguredBrowserSubAppDescriptor descriptor = new ConfiguredBrowserSubAppDescriptor();
        descriptor.setWorkbench(wbDef);
        descriptor.setActionbar(definition);

        subAppContext = mock(SubAppContext.class);
        when(subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
    }

    private void initActionbarSections() {
        sectionToShow = new ConfiguredActionbarSectionDefinition();
        sectionToShow.setName(SECTION_TO_SHOW);
        sectionToShow.addGroup(allActionsGroup);

        sectionToHide = new ConfiguredActionbarSectionDefinition();
        sectionToHide.setName(SECTION_TO_HIDE);
        sectionToHide.addGroup(oneActionGroup);
    }

    private void initActionbarGroups() {
        allActionsGroup = new ConfiguredActionbarGroupDefinition();
        allActionsGroup.setName("allActionsGroup");
        allActionsGroup.setItems(createActionbarItemDefinitionList(ALL_ACTIONS));

        oneActionGroup = new ConfiguredActionbarGroupDefinition();
        oneActionGroup.setName("oneActionGroup");
        oneActionGroup.setItems(createActionbarItemDefinitionList(ONE_ACTION));
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

    private void initActionAvailabilityDefs() {
        availabilityAlways = new ConfiguredAvailabilityDefinition();
        availabilityAlways.setRoot(true);
        availabilityAlways.setProperties(true);

        availabilityRootOnly = new ConfiguredAvailabilityDefinition();
        availabilityRootOnly.setRoot(true);
        availabilityRootOnly.setNodes(false);

        availabilityRootAndNodes = new ConfiguredAvailabilityDefinition();
        availabilityRootAndNodes.setRoot(true);

        availabilityNodesAndProperties = new ConfiguredAvailabilityDefinition();
        availabilityNodesAndProperties.setProperties(true);

        availabilityPropertiesOnly = new ConfiguredAvailabilityDefinition();
        availabilityPropertiesOnly.setNodes(false);
        availabilityPropertiesOnly.setProperties(true);

        availabilityOnlyPages = new ConfiguredAvailabilityDefinition();
        availabilityOnlyPages.setNodeTypes(Arrays.asList(new String[] { NodeTypes.Page.NAME }));

        availabilityRoleLimited = new ConfiguredAvailabilityDefinition();
        ConfiguredAccessDefinition access = new ConfiguredAccessDefinition();
        access.setRoles(Arrays.asList(new String[] { REQUIRED_ROLE }));
        availabilityRoleLimited.setAccess(access);

        availabilityDeletedPages = new ConfiguredAvailabilityDefinition();
        availabilityDeletedPages.setNodeTypes(Arrays.asList(new String[] { NodeTypes.Page.NAME }));
        availabilityDeletedPages.setRuleClass(IsDeletedRule.class);
    }

    private void initSectionAvailabilityDefs() {
        sAvailabilityAlways = new ConfiguredAvailabilityDefinition();
        sAvailabilityAlways.setRoot(true);
        sAvailabilityAlways.setProperties(true);

        sAvailabilityRootOnly = new ConfiguredAvailabilityDefinition();
        sAvailabilityRootOnly.setRoot(true);
        sAvailabilityRootOnly.setNodes(false);

        sAvailabilityRootAndNodes = new ConfiguredAvailabilityDefinition();
        sAvailabilityRootAndNodes.setRoot(true);

        sAvailabilityNodesAndProperties = new ConfiguredAvailabilityDefinition();
        sAvailabilityNodesAndProperties.setProperties(true);

        sAvailabilityPropertiesOnly = new ConfiguredAvailabilityDefinition();
        sAvailabilityPropertiesOnly.setNodes(false);
        sAvailabilityPropertiesOnly.setProperties(true);

        sAvailabilityOnlyPages = new ConfiguredAvailabilityDefinition();
        sAvailabilityOnlyPages.setNodeTypes(Arrays.asList(new String[] { NodeTypes.Page.NAME }));

        sAvailabilityRoleLimited = new ConfiguredAvailabilityDefinition();
        ConfiguredAccessDefinition access = new ConfiguredAccessDefinition();
        access.setRoles(Arrays.asList(new String[] { REQUIRED_ROLE }));
        sAvailabilityRoleLimited.setAccess(access);

        sAvailabilityDeletedPages = new ConfiguredAvailabilityDefinition();
        sAvailabilityDeletedPages.setNodeTypes(Arrays.asList(new String[] { NodeTypes.Page.NAME }));
        sAvailabilityDeletedPages.setRuleClass(IsDeletedRule.class);
    }

    private void initActions() {
        actAlways = new ConfiguredActionDefinition();
        actAlways.setName(ALWAYS);
        actAlways.setAvailability(availabilityAlways);

        actRootOnly = new ConfiguredActionDefinition();
        actRootOnly.setName(ROOT_ONLY);
        actRootOnly.setAvailability(availabilityRootOnly);

        actRootAndNodes = new ConfiguredActionDefinition();
        actRootAndNodes.setName(ROOT_AND_NODES);
        actRootAndNodes.setAvailability(availabilityRootAndNodes);

        actNodesAndProperties = new ConfiguredActionDefinition();
        actNodesAndProperties.setName(NODES_AND_PROPS);
        actNodesAndProperties.setAvailability(availabilityNodesAndProperties);

        actPropertiesOnly = new ConfiguredActionDefinition();
        actPropertiesOnly.setName(PROPS_ONLY);
        actPropertiesOnly.setAvailability(availabilityPropertiesOnly);

        actOnlyPages = new ConfiguredActionDefinition();
        actOnlyPages.setName(ONLY_PAGES);
        actOnlyPages.setAvailability(availabilityOnlyPages);

        actDeletedPages = new ConfiguredActionDefinition();
        actDeletedPages.setName(DELETED_PAGES);
        actDeletedPages.setAvailability(availabilityDeletedPages);

        actRoleLimited = new ConfiguredActionDefinition();
        actRoleLimited.setName(ROLE_LIMITED);
        actRoleLimited.setAvailability(availabilityRoleLimited);

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
        builder.withConfiguration(new ComponentProviderConfiguration());
        GuiceComponentProvider componentProvider = builder.build();
        return new SimpleActionExecutor(componentProvider);
    }

    private static class TestActionbarPresenter extends ActionbarPresenter {

        public Set<String> visibleSections;
        public Set<String> enabledActions;
        private ActionbarDefinition definition;

        /**
         *
         */
        public TestActionbarPresenter() {
            super();
        }

        @Override
        public ActionbarView start(ActionbarDefinition def) {
            definition = def;
            visibleSections = new HashSet<String>();
            enabledActions = new HashSet<String>();
            return super.start(def);
        }

        @Override
        public void showSection(String... sectionNames) {
            super.showSection(sectionNames);
            for (String section : sectionNames) {
                visibleSections.add(section);
            }
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
            for (String action : actionNames) {
                enabledActions.add(action);
            }
        }

        @Override
        public void disable(String... actionNames) {
            super.disable(actionNames);
            for (String action : actionNames) {
                enabledActions.remove(action);
            }
        }

    }

}
