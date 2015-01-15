/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.framework.availability;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecurityConstants;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.AvailabilityRule;
import info.magnolia.ui.api.availability.AvailabilityRuleDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link AvailabilityCheckerImpl}.
 */
public class AvailabilityCheckerImplTest extends MgnlTestCase {

    private static final String TEST_ROLE = "testUserRole";
    private static final String WORKSPACE = "mockWorkspaceName";

    private AvailabilityChecker availabilityChecker;
    private ComponentProvider componentProvider;
    private JcrContentConnector jcrContentConnector;
    private MockSession mockSession;
    private UserManager userManager;
    private RoleManager roleManager;
    private MgnlUser user;
    private Node rootNode;
    private Node testNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        final ArrayList<String> roles = new ArrayList<String>();
        roles.add(TEST_ROLE);
        user = mock(MgnlUser.class);
        when(user.getRoles()).thenReturn(roles);
        when(user.getAllRoles()).thenReturn(roles);

        MockWebContext context = (MockWebContext) MgnlContext.getInstance();
        context.setUser(user);

        MgnlUser anonymousUser = mock(MgnlUser.class);
        when(anonymousUser.getAllRoles()).thenReturn(Arrays.asList("anonymous"));
        userManager = mock(UserManager.class);
        when(userManager.getAnonymousUser()).thenReturn(anonymousUser);
        roleManager = mock(RoleManager.class);
        when(roleManager.getACLs("anonymous")).thenReturn(new HashMap<String, ACL>());
        SecuritySupport securitySupport = mock(SecuritySupport.class);
        when(securitySupport.getUserManager()).thenReturn(userManager);
        when(securitySupport.getRoleManager()).thenReturn(roleManager);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);

        mockSession = new MockSession(WORKSPACE);
        MockUtil.setSessionAndHierarchyManager(mockSession);

        componentProvider = new MockComponentProvider();
        jcrContentConnector = mock(JcrContentConnector.class);
        availabilityChecker = new AvailabilityCheckerImpl(componentProvider, jcrContentConnector);

        rootNode = mockSession.getRootNode();
        Object itemId = JcrItemUtil.getItemId(rootNode);
        doReturn(itemId).when(jcrContentConnector).getDefaultItemId();
        testNode = rootNode.addNode("test");
    }

    @Test
    public void testAccessGrantedRule() throws Exception {
        // GIVEN
        //
        // no rules defined
        ConfiguredAvailabilityDefinition availabilityDefinitionNoRolesDefined = new ConfiguredAvailabilityDefinition();
        // testUser in accessDefinition
        ConfiguredAvailabilityDefinition availabilityDefinitionTestUserRoleRequired = new ConfiguredAvailabilityDefinition();
        ConfiguredAccessDefinition accessDefinition = new ConfiguredAccessDefinition();
        accessDefinition.addRole(TEST_ROLE);
        availabilityDefinitionTestUserRoleRequired.setAccess(accessDefinition);
        // superuser in accessDefinition
        ConfiguredAvailabilityDefinition availabilityDefinitionSuperUserRoleRequired = new ConfiguredAvailabilityDefinition();
        accessDefinition = new ConfiguredAccessDefinition();
        accessDefinition.addRole("superuser");
        availabilityDefinitionSuperUserRoleRequired.setAccess(accessDefinition);

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoRolesDefined, getJcrItemIdsList(testNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionTestUserRoleRequired, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionSuperUserRoleRequired, getJcrItemIdsList(testNode)));
    }

    @Test
    public void testJcrRootAllowedRule() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionForRoot = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionForRoot.setRoot(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionNotForRoot = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionNotForRoot.setRoot(false);

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForRoot, getJcrItemIdsList(rootNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForRoot, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionNotForRoot, getJcrItemIdsList(rootNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNotForRoot, getJcrItemIdsList(testNode)));
    }

    @Test
    public void testJcrNodeTypesAllowedRule() throws Exception {
        // GIVEN
        //
        Node contentNode = rootNode.addNode("testContentNode", NodeTypes.ContentNode.NAME);
        Node folderNode = rootNode.addNode("testFolderNode", NodeTypes.Folder.NAME);
        Node pageNode = rootNode.addNode("testPageNode", NodeTypes.Page.NAME);

        ConfiguredAvailabilityDefinition availabilityDefinitionNoNodesDefined = new ConfiguredAvailabilityDefinition();
        ConfiguredAvailabilityDefinition availabilityDefinitionWithNodeTypes = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionWithNodeTypes.setNodeTypes(Arrays.asList(NodeTypes.ContentNode.NAME, NodeTypes.Folder.NAME));

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoNodesDefined, getJcrItemIdsList(contentNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoNodesDefined, getJcrItemIdsList(folderNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoNodesDefined, getJcrItemIdsList(pageNode)));

        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithNodeTypes, getJcrItemIdsList(contentNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithNodeTypes, getJcrItemIdsList(folderNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWithNodeTypes, getJcrItemIdsList(pageNode)));
    }

    @Test
    public void testJcrNodesAllowedRule() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionNodesAllowed = new ConfiguredAvailabilityDefinition();
        ConfiguredAvailabilityDefinition availabilityDefinitionNodesNotAllowed = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionNodesNotAllowed.setNodes(false);

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNodesAllowed, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionNodesNotAllowed, getJcrItemIdsList(testNode)));
    }

    @Test
    public void testJcrPropertiesAllowedRule() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionPropertiesNotAllowed = new ConfiguredAvailabilityDefinition();
        ConfiguredAvailabilityDefinition availabilityDefinitionPropertiesAllowed = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionPropertiesAllowed.setProperties(true);
        Property testProperty = testNode.setProperty("testProperty", "abcdefg");

        // THEN
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionPropertiesNotAllowed, getJcrPropertyItemIdsList(testProperty)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionPropertiesAllowed, getJcrPropertyItemIdsList(testProperty)));
    }

    @Test
    public void testMultipleItemsAllowedRule() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionForMultiItems = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionForMultiItems.setMultiple(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionForOneItemOnly = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionForOneItemOnly.setMultiple(false);

        List<Object> oneItem = getJcrItemIdsList(testNode);
        List<Object> manyItems = new ArrayList<Object>();
        Node anotherNode = rootNode.addNode("testNode2");
        manyItems.add(new JcrItemId(testNode.getIdentifier(), WORKSPACE));
        manyItems.add(new JcrItemId(anotherNode.getIdentifier(), WORKSPACE));

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForMultiItems, manyItems));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForMultiItems, oneItem));

        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForOneItemOnly, oneItem));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionForOneItemOnly, manyItems));
    }

    @Test
    public void testWritePermissionRequiredRule() throws Exception {
        // GIVEN
        when(userManager.hasAny("superuser", "superuser", SecurityConstants.NODE_ROLES)).thenReturn(false);
        ConfiguredAvailabilityDefinition availabilityDefinitionWritePermissionNotRequired = new ConfiguredAvailabilityDefinition();
        ConfiguredAvailabilityDefinition availabilityDefinitionWritePermissionRequired = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionWritePermissionRequired.setWritePermissionRequired(true);

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWritePermissionNotRequired, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWritePermissionRequired, getJcrItemIdsList(testNode)));
    }

    @Test
    public void testAddingRuleClasses() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionWithMultiAllowRules = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionWithMultiAllowRules.setRules(getAvailabilityRules(DummyRuleAllow.class, DummyRuleAllow2.class));

        ConfiguredAvailabilityDefinition availabilityDefinitionWithOneDenyRule = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionWithOneDenyRule.setRules(getAvailabilityRules(DummyRuleDeny.class));

        ConfiguredAvailabilityDefinition availabilityDefinitionWithMixedRules = new ConfiguredAvailabilityDefinition();
        availabilityDefinitionWithMixedRules.setRules(getAvailabilityRules(DummyRuleDeny.class, DummyRuleAllow.class));

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithMultiAllowRules, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWithOneDenyRule, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWithMixedRules, getJcrItemIdsList(testNode)));
    }

    private List<AvailabilityRuleDefinition> getAvailabilityRules(Class<? extends AvailabilityRule>... ruleClasses) {
        ArrayList<AvailabilityRuleDefinition> list = new ArrayList<AvailabilityRuleDefinition>();
        for (int i = 0; i < ruleClasses.length; i++) {
            ConfiguredAvailabilityRuleDefinition configuredAvailabilityRuleDefinition = new ConfiguredAvailabilityRuleDefinition();
            configuredAvailabilityRuleDefinition.setImplementationClass(ruleClasses[i]);
            list.add(configuredAvailabilityRuleDefinition);
        }
        return list;
    }

    private List<Object> getJcrItemIdsList(Node node) throws Exception {
        return Arrays.asList((Object) JcrItemUtil.getItemId(node));
    }

    private List<Object> getJcrPropertyItemIdsList(Property property) throws Exception {
        return Arrays.asList((Object) JcrItemUtil.getItemId(property));
    }

    /**
     * An {@link AvailabilityRule} dummy implementation that returns true.
     */
    public static class DummyRuleAllow implements AvailabilityRule {

        @Override
        public boolean isAvailable(Collection<?> itemIds) {
            return true;
        }
    }

    /**
     * Another {@link AvailabilityRule} dummy implementation that returns true.
     */
    public static class DummyRuleAllow2 implements AvailabilityRule {

        @Override
        public boolean isAvailable(Collection<?> itemIds) {
            return true;
        }
    }

    /**
     * An {@link AvailabilityRule} dummy implementation that returns false.
     */
    public static class DummyRuleDeny implements AvailabilityRule {

        @Override
        public boolean isAvailable(Collection<?> itemIds) {
            return false;
        }
    }

}
