/**
 * This file Copyright (c) 2014 Magnolia International
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
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.Before;
import org.junit.Test;

/**
 * This class tests AvailabilityChecker / AvailabilityCheckerImpl.
 * Some tests have been moved from AbstractActionExecutorTest here.
 */
public class AvailabilityCheckerTest extends MgnlTestCase {

    private AvailabilityChecker availabilityChecker;
    private ComponentProvider componentProvider;
    private JcrContentConnector jcrContentConnector;
    private static final String TESTUSERNAME = "testUser";
    private String workspaceName = "mockWorkspaceName";
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
        roles.add(TESTUSERNAME);
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

        mockSession = new MockSession(workspaceName);
        MockUtil.setSessionAndHierarchyManager(mockSession);

        componentProvider = new MockComponentProvider();
        jcrContentConnector = mock(JcrContentConnector.class);
        availabilityChecker = new AvailabilityCheckerImpl(componentProvider, jcrContentConnector);

        rootNode = mockSession.getRootNode();
        List<Object> itemId = getJcrItemIdsList(rootNode);
        doReturn(itemId).when(jcrContentConnector).getDefaultItemId();
        testNode = rootNode.addNode("test");

    }


    /**
     * This methods tests AccessGrantedRule -class.
     */
    @Test
    public void test_AccessGrantedRule() throws Exception {
        // GIVEN
        //
        // no rules defined
        ConfiguredAvailabilityDefinition availabilityDefinitionNoRolesDefined = createBaseAvailabilityDefinition(true);
        // testUser in accessDefinition
        ConfiguredAvailabilityDefinition availabilityDefinitionTestUserRoleRequired = createBaseAvailabilityDefinition(true);
        ConfiguredAccessDefinition accessDefinition = new ConfiguredAccessDefinition();
        accessDefinition.addRole(TESTUSERNAME);
        availabilityDefinitionTestUserRoleRequired.setAccess(accessDefinition);
        // superuser in accessDefinition
        ConfiguredAvailabilityDefinition availabilityDefinitionSuperUserRoleRequired = createBaseAvailabilityDefinition(true);
        accessDefinition = new ConfiguredAccessDefinition();
        accessDefinition.addRole("superuser");
        availabilityDefinitionSuperUserRoleRequired.setAccess(accessDefinition);

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoRolesDefined, getJcrItemIdsList(testNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionTestUserRoleRequired, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionSuperUserRoleRequired, getJcrItemIdsList(testNode)));
    }


    /**
     * This methods tests IsRootItemAllowedRule -class.
     */
    @Test
    public void test_IsRootItemAllowedRule() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionForRoot = createBaseAvailabilityDefinition(true);
        availabilityDefinitionForRoot.setRoot(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionNotForRoot = createBaseAvailabilityDefinition(true);
        availabilityDefinitionNotForRoot.setRoot(false);

        // THEN
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionNotForRoot, getJcrItemIdsList(rootNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNotForRoot, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionNotForRoot, getJcrItemIdsList(rootNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNotForRoot, getJcrItemIdsList(testNode)));
    }

    /**
     * This methods tests JcrItemNodeTypeAllowedRule -class.
     */
    @Test
    public void test_JcrItemNodeTypeAllowedRule() throws Exception {
        // GIVEN
        //
        Node contentNode = rootNode.addNode("testContentNode", NodeTypes.ContentNode.NAME);
        Node folderNode = rootNode.addNode("testFolderNode", NodeTypes.Folder.NAME);
        Node pageNode = rootNode.addNode("testPageNode", NodeTypes.Page.NAME);

        ConfiguredAvailabilityDefinition availabilityDefinitionNoNodesDefined = createBaseAvailabilityDefinition(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionWithNodeTypes = createBaseAvailabilityDefinition(true);
        availabilityDefinitionWithNodeTypes.setNodeTypes(Arrays.asList(NodeTypes.ContentNode.NAME, NodeTypes.Folder.NAME));

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoNodesDefined, getJcrItemIdsList(contentNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoNodesDefined, getJcrItemIdsList(folderNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoNodesDefined, getJcrItemIdsList(pageNode)));

        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithNodeTypes, getJcrItemIdsList(contentNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithNodeTypes, getJcrItemIdsList(folderNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWithNodeTypes, getJcrItemIdsList(pageNode)));
    }

    /**
     * This methods tests JcrNodesAllowedRule -class.
     */
    @Test
    public void test_JcrNodesAllowedRule() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionNodesAllowed = createBaseAvailabilityDefinition(true);
        Property testProperty = testNode.setProperty("testProperty", "abcdefg");

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNodesAllowed, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionNodesAllowed, getJcrPropertyItemIdsList(testProperty)));
    }

    /**
     * This methods tests JcrPropertiesAllowedRule -class.
     */
    @Test
    public void test_JcrPropertiesAllowedRule() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionPropertiesAllowed = createBaseAvailabilityDefinition(false);
        Property testProperty = testNode.setProperty("testProperty", "abcdefg");

        // THEN
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionPropertiesAllowed, getJcrItemIdsList(testNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionPropertiesAllowed, getJcrPropertyItemIdsList(testProperty)));
    }


    /**
     * This methods tests MultipleItemsAllowedRule -class.
     */
    @Test
    public void test_MultipleItemsAllowedRule() throws Exception {
        // GIVEN
        //
        ConfiguredAvailabilityDefinition availabilityDefinitionForMultiItems = createBaseAvailabilityDefinition(true);
        availabilityDefinitionForMultiItems.setMultiple(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionForOneItemOnly = createBaseAvailabilityDefinition(true);
        availabilityDefinitionForOneItemOnly.setMultiple(false);

        List<Object> oneItem = getJcrItemIdsList(testNode);
        List<Object> manyItems = new ArrayList<Object>();
        Node anotherNode = rootNode.addNode("testNode2");
        manyItems.add(new JcrItemId(testNode.getIdentifier(), workspaceName));
        manyItems.add(new JcrItemId(anotherNode.getIdentifier(), workspaceName));

        // THEN
        //
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForMultiItems, manyItems));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForMultiItems, oneItem));

        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForOneItemOnly, oneItem));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionForOneItemOnly, manyItems));
    }

    /**
     * This methods tests WritePermissionsAvailableRule -class.
     */
    @Test
    public void test_WritePermissionsAvailableRule() throws Exception {
        // GIVEN
        when(userManager.hasAny("superuser", "superuser", SecurityConstants.NODE_ROLES)).thenReturn(false);
        ConfiguredAvailabilityDefinition availabilityDefinitionWritePermissionsNotRequired = createBaseAvailabilityDefinition(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionWritePermissionsRequired = createBaseAvailabilityDefinition(true);
        availabilityDefinitionWritePermissionsRequired.setWritePermissionRequired(true);

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWritePermissionsNotRequired, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWritePermissionsRequired, getJcrItemIdsList(testNode)));
    }


    /**
     * Test how the AvailabilityChecker works when adding more availability-rule-classes.
     */
    @Test
    public void test_addingRuleClasses() throws Exception {
        // GIVEN
        ConfiguredAvailabilityDefinition availabilityDefinitionWithMultiAllowRules = createBaseAvailabilityDefinition(true);
        availabilityDefinitionWithMultiAllowRules.setRules(getConfiguredAvailabilityRuleDefinition(new Class[]{DummyRuleAllow.class, DummyRuleAllow2.class}));

        ConfiguredAvailabilityDefinition availabilityDefinitionWithOneDenyRule = createBaseAvailabilityDefinition(true);
        availabilityDefinitionWithOneDenyRule.setRules(getConfiguredAvailabilityRuleDefinition(new Class[]{DummyRuleDeny.class}));

        ConfiguredAvailabilityDefinition availabilityDefinitionWithMixedRules = createBaseAvailabilityDefinition(true);
        availabilityDefinitionWithMixedRules.setRules(getConfiguredAvailabilityRuleDefinition(new Class[]{DummyRuleDeny.class, DummyRuleAllow.class}));

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithMultiAllowRules, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWithOneDenyRule, getJcrItemIdsList(testNode)));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionWithMixedRules, getJcrItemIdsList(testNode)));

    }

    private List<ConfiguredAvailabilityRuleDefinition> getConfiguredAvailabilityRuleDefinition(Class[] classes) {
        ArrayList<ConfiguredAvailabilityRuleDefinition> list = new ArrayList<ConfiguredAvailabilityRuleDefinition>();
        for (int i = 0; i < classes.length; i++) {
            ConfiguredAvailabilityRuleDefinition configuredAvailabilityRuleDefinition = new ConfiguredAvailabilityRuleDefinition();
            configuredAvailabilityRuleDefinition.setImplementationClass(classes[i]);
            list.add(configuredAvailabilityRuleDefinition);
        }
        return list;
    }


    private List<Object> getJcrItemIdsList(Node node) throws Exception {
        return Arrays.asList((Object) new JcrItemId(node.getIdentifier(), workspaceName));
    }

    private List<Object> getJcrPropertyItemIdsList(Property property) throws Exception {
        Item propertyItem = (Item) property;
        Node parentNode = propertyItem.getParent();
        return Arrays.asList((Object) new JcrPropertyItemId(parentNode.getIdentifier(), workspaceName, property.getName()));
    }


    private ConfiguredAvailabilityDefinition createBaseAvailabilityDefinition(boolean nodesAllowed) {
        ConfiguredAvailabilityDefinition configuredAvailabilityDefinition = new ConfiguredAvailabilityDefinition();
        configuredAvailabilityDefinition.setProperties(!nodesAllowed);
        configuredAvailabilityDefinition.setNodes(nodesAllowed);
        configuredAvailabilityDefinition.setWritePermissionRequired(false);
        return configuredAvailabilityDefinition;
    }


}
