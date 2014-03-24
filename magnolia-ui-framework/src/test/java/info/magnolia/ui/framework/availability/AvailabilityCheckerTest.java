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
import static org.mockito.Mockito.mock;

import info.magnolia.cms.security.MgnlUser;
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
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

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
 * This class tests AvailabilityChecker / AvailabilityCheckerImpl.
 * Some tests hev been moved from AbstractActionExecutorTest here.
 */
public class AvailabilityCheckerTest extends MgnlTestCase {

    private AvailabilityChecker availabilityChecker;
    private ComponentProvider componentProvider;
    private JcrContentConnector jcrContentConnector;
    private static final String TESTUSERNAME = "testUser";
    private String workspaceName = "mockWorkspaceName";
    private MockSession mockSession;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        ArrayList<String> roles = new ArrayList<String>();
        roles.add("testRole");

        MgnlUser user = new MgnlUser(TESTUSERNAME, null, new ArrayList<String>(), roles, new HashMap<String, String>()) {
            // Overridden to avoid querying the group manager in test
            @Override
            public Collection<String> getAllRoles() {
                return Arrays.asList(TESTUSERNAME);
            }
        };
        MockWebContext context = (MockWebContext) MgnlContext.getInstance();
        context.setUser(user);

        mockSession = new MockSession(workspaceName);
        MockUtil.setSessionAndHierarchyManager(mockSession);

        componentProvider = new MockComponentProvider();
        jcrContentConnector = mock(JcrContentConnector.class);
        availabilityChecker = new AvailabilityCheckerImpl(componentProvider, jcrContentConnector);
    }

    /**
     * This methods tests AccessGrantedRule -class.
     */
    @Test
    public void test_AccessGrantedRule() {
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

        List<Object> items = Arrays.asList(new Object());

        // THEN
        //
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNoRolesDefined, items));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionTestUserRoleRequired, items));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionSuperUserRoleRequired, items));
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
        // contains an item which mocks a root-item
        List<Object> rootItem = Arrays.asList((Object) null);
        List<Object> nonRootItem = Arrays.asList(new Object());

        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForRoot, rootItem));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForRoot, nonRootItem));
        // TODO find out why the line below fails ...
        //assertFalse(availabilityChecker.isAvailable(availabilityDefinitionForRoot, rootItem));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionNotForRoot, nonRootItem));
    }

    /**
     * This methods tests JcrItemNodeTypeAllowedRule -class.
     */
    @Test
    public void test_JcrItemNodeTypeAllowedRule() throws Exception {
        // GIVEN
        //
        Node parentNode = mockSession.getRootNode().addNode("TEST", NodeTypes.ContentNode.NAME);
        Node contentNode = parentNode.addNode("testContentNode", NodeTypes.ContentNode.NAME);
        Node folderNode = parentNode.addNode("testFolderNode", NodeTypes.Folder.NAME);
        Node pageNode = parentNode.addNode("testPageNode", NodeTypes.Page.NAME);

        ConfiguredAvailabilityDefinition availabilityDefinitionNoNodesDefined = createBaseAvailabilityDefinition(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionWithNodeTypes = createBaseAvailabilityDefinition(true);
        availabilityDefinitionWithNodeTypes.setNodeTypes(Arrays.asList(NodeTypes.ContentNode.NAME, NodeTypes.Folder.NAME));

        // THEN
        //
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
        Node parentNode = mockSession.getRootNode().addNode("TEST", NodeTypes.ContentNode.NAME);
        Node testNode = parentNode.addNode("testContentNode", NodeTypes.ContentNode.NAME);
        Property testProperty = testNode.setProperty("testProperty", "abcdefg");

        // THEN
        //
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
        Node parentNode = mockSession.getRootNode().addNode("TEST", NodeTypes.ContentNode.NAME);
        Node testNode = parentNode.addNode("testContentNode", NodeTypes.ContentNode.NAME);
        Property testProperty = testNode.setProperty("testProperty", "abcdefg");

        // THEN
        //
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionPropertiesAllowed, getJcrItemIdsList(testNode)));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionPropertiesAllowed, getJcrPropertyItemIdsList(testProperty)));
    }


    /**
     * This methods tests MultipleItemsAllowedRule -class.
     */
    @Test
    public void test_MultipleItemsAllowedRule() {
        // GIVEN
        //
        ConfiguredAvailabilityDefinition availabilityDefinitionForMultiItems = createBaseAvailabilityDefinition(true);
        availabilityDefinitionForMultiItems.setMultiple(true);
        ConfiguredAvailabilityDefinition availabilityDefinitionForOneItemOnly = createBaseAvailabilityDefinition(true);
        availabilityDefinitionForOneItemOnly.setMultiple(false);

        JcrItemAdapter itemAdapter1 = mock(JcrItemAdapter.class);
        List<Object> oneItem = new ArrayList<Object>();
        oneItem.add(itemAdapter1);
        JcrItemAdapter itemAdapter2 = mock(JcrItemAdapter.class);
        List<Object> manyItems = new ArrayList<Object>(oneItem);
        manyItems.add(itemAdapter2);

        // THEN
        //
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForMultiItems, manyItems));
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForMultiItems, oneItem));

        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionForOneItemOnly, oneItem));
        assertFalse(availabilityChecker.isAvailable(availabilityDefinitionForOneItemOnly, manyItems));
    }


    /**
     * Test how the AvailabilityChecker works when adding more availability-rule-classes.
     */
    @Test
    public void test_addingRuleClasses() throws Exception{
        // GIVEN
        //
        Node parentNode = mockSession.getRootNode().addNode("TEST", NodeTypes.ContentNode.NAME);
        Node testNode = parentNode.addNode("testContentNode", NodeTypes.ContentNode.NAME);

        ConfiguredAvailabilityDefinition availabilityDefinitionWithMultiAllowRules = createBaseAvailabilityDefinition(true);
        List<ConfiguredAvailabilityRuleDefinition> rulesAllAllow = getConfiguredAvailabilityRuleDefinition(new Class[]{DummyAccessRuleAllow.class, DummyAccessRuleAllow2.class});
        availabilityDefinitionWithMultiAllowRules.setRules(rulesAllAllow);

        ConfiguredAvailabilityDefinition availabilityDefinitionWithMixedRules = createBaseAvailabilityDefinition(true);
        List<ConfiguredAvailabilityRuleDefinition> rulesMixedAllowDeny = getConfiguredAvailabilityRuleDefinition(new Class[]{DummyAccessRuleAllow.class, DummyAccessRuleDeny.class});
        availabilityDefinitionWithMixedRules.setRules(rulesMixedAllowDeny);


        // THEN
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithMultiAllowRules, getJcrItemIdsList(testNode)));
        // TODO ??? check ...
        assertTrue(availabilityChecker.isAvailable(availabilityDefinitionWithMixedRules, getJcrItemIdsList(testNode)));


    }

    private List<ConfiguredAvailabilityRuleDefinition> getConfiguredAvailabilityRuleDefinition(Class[] classes){
        ArrayList<ConfiguredAvailabilityRuleDefinition> list = new ArrayList<ConfiguredAvailabilityRuleDefinition>();
        for (int i=0; i<classes.length;i++){
            ConfiguredAvailabilityRuleDefinition configuredAvailabilityRuleDefinition = new ConfiguredAvailabilityRuleDefinition();
            configuredAvailabilityRuleDefinition.setImplementationClass(classes[i]);
        }
        return list;
    }



    private List<Object> getJcrItemIdsList(Node node) throws Exception {
        return Arrays.asList((Object) new JcrItemId(node.getIdentifier(), workspaceName));
    }

    private List<Object> getJcrPropertyItemIdsList(Property property) throws Exception {
        javax.jcr.Item propertyItem = (javax.jcr.Item) property;
        Node parentNode = propertyItem.getParent();
        return Arrays.asList((Object) new JcrPropertyItemId(parentNode.getIdentifier(), workspaceName, property.getName()));
    }


    private ConfiguredAvailabilityDefinition createBaseAvailabilityDefinition(boolean nodesAllowed) {
        ConfiguredAvailabilityDefinition configuredAvailabilityDefinition = new ConfiguredAvailabilityDefinition();
        configuredAvailabilityDefinition.setRoot(true);
        configuredAvailabilityDefinition.setProperties(!nodesAllowed);
        configuredAvailabilityDefinition.setNodes(nodesAllowed);
        configuredAvailabilityDefinition.setWritePermissionRequired(false);
        return configuredAvailabilityDefinition;
    }

    private class DummyAccessRuleAllow implements AvailabilityRule {
        @Override
        public boolean isAvailable(Collection<?> itemIds) {
            return true;
        }
    }

    private class DummyAccessRuleAllow2 implements AvailabilityRule {
        @Override
        public boolean isAvailable(Collection<?> itemIds) {
            return true;
        }
    }


    private class DummyAccessRuleDeny implements AvailabilityRule {
        @Override
        public boolean isAvailable(Collection<?> itemIds) {
            return false;
        }
    }


}
