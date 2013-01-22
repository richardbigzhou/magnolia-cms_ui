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
package info.magnolia.ui.admincentral.tree.container;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.builder.DefinitionToImplementationMapping;
import info.magnolia.ui.model.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.model.workbench.action.WorkbenchActionRegistry;
import info.magnolia.ui.model.workbench.definition.ConfiguredItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.model.workbench.definition.ItemTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.container.AbstractJcrContainerTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for HierarchicalJcrContainer.
 */
public class HierarchicalJcrContainerTest extends RepositoryTestCase {

    private HierarchicalJcrContainer hierarchicalJcrContainer;

    private ConfiguredWorkbenchDefinition workbenchDefinition;

    private static final String WORKSPACE = "config";

    private static final String PROPERTY_1 = "name";

    private static final String PROPERTY_2 = "shortname";

    private Session session;

    private Node rootNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(WORKSPACE);
        configuredWorkbench.setPath("/");

        WorkbenchActionRegistry workbenchActionRegistry = mock(WorkbenchActionRegistry.class);
        when(workbenchActionRegistry.getDefinitionToImplementationMappings()).thenReturn(new ArrayList<DefinitionToImplementationMapping<ActionDefinition, Action>>());

        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(PROPERTY_1);
        colDef1.setLabel("Label_" + PROPERTY_1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(PROPERTY_2);
        colDef2.setLabel("Label_" + PROPERTY_2);

        configuredWorkbench.addColumn(colDef1);
        configuredWorkbench.addColumn(colDef2);

        ItemTypeDefinition itemType = new ConfiguredItemTypeDefinition();
        ((ConfiguredItemTypeDefinition) itemType).setItemType(NodeTypes.Content.NAME);
        configuredWorkbench.setMainItemType(itemType);

        workbenchDefinition = configuredWorkbench;

        hierarchicalJcrContainer = new HierarchicalJcrContainer(workbenchDefinition);

        // Init session
        session = MgnlContext.getJCRSession(WORKSPACE);
        rootNode = session.getRootNode();
    }

    @Test
    public void testGetItemWithNodeType() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.getSession().save();
        String containerItemId = node1.getPath();

        // WHEN
        com.vaadin.data.Item item = hierarchicalJcrContainer.getItem(containerItemId);

        // THEN
        assertTrue(item instanceof JcrNodeAdapter);
        assertEquals(node1.getPath(), ((JcrNodeAdapter) item).getJcrItem().getPath());
    }

    @Test
    public void testAreChildrenAllowedReturnsTrue() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.getSession().save();

        Node node2 = AbstractJcrContainerTest.createNode(node1, "node2", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        node2.getSession().save();
        String containerItemId = node1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.areChildrenAllowed(containerItemId);

        // THEN
        assertTrue(res);
    }

    @Test
    public void testAreChildrenAllowedReturnsFalse() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.getSession().save();
        String containerItemId = node1.getProperty(PROPERTY_1).getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.areChildrenAllowed(containerItemId);

        // THEN
        assertFalse(res);
    }

    @Test
    public void testRootItemIds() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        AbstractJcrContainerTest.createNode(node1, "node1_1", NodeTypes.Content.NAME, PROPERTY_1, "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", NodeTypes.Content.NAME, PROPERTY_1, "name2_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();
        String containerItemId2 = node2.getPath();

        // WHEN
        Collection<String> res = hierarchicalJcrContainer.rootItemIds();

        // THEN
        assertEquals(2, res.size());
        assertTrue(res.contains(containerItemId1));
        assertTrue(res.contains(containerItemId2));
    }

    @Test
    public void testIsRootReturnsTrue() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        AbstractJcrContainerTest.createNode(node1, "node1_1", NodeTypes.Content.NAME, PROPERTY_1, "name1_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.isRoot(containerItemId1);

        // THEN
        assertTrue(res);
    }

    @Test
    public void testIsRootReturnsFalse() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node node11 = AbstractJcrContainerTest.createNode(node1, "node1_1", NodeTypes.Content.NAME, PROPERTY_1, "name1_1");
        node1.getSession().save();

        String containerItemId1_1 = node11.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.isRoot(containerItemId1_1);

        // THEN
        assertFalse(res);
    }

    @Test
    public void testGetChildren() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node node11 = AbstractJcrContainerTest.createNode(node1, "node1_1", NodeTypes.Content.NAME, PROPERTY_1, "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", NodeTypes.Content.NAME, PROPERTY_1, "name2_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        Collection<String> res = hierarchicalJcrContainer.getChildren(containerItemId1);

        // THEN
        assertEquals(1, res.size());
        assertTrue(res.contains(node11.getPath()));
    }

    @Test
    public void testGetParent() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node node1_1 = AbstractJcrContainerTest.createNode(node1, "node1_1", NodeTypes.Content.NAME, PROPERTY_1, "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", NodeTypes.Content.NAME, PROPERTY_1, "name2_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        String res = hierarchicalJcrContainer.getParent(node1_1.getPath());

        // THEN
        assertEquals(containerItemId1, res);
    }

    @Test
    public void testGetRootItemIds() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:page", PROPERTY_1, "name1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        Node node21 = AbstractJcrContainerTest.createNode(node2, "node2_1", NodeTypes.Content.NAME, PROPERTY_1, "name2_1");
        node1.getSession().save();
        // Initial check
        Collection<Item> res = hierarchicalJcrContainer.getRootItemIds();
        assertEquals(1, res.size());
        assertEquals(node2.getPath(), ((Node) res.toArray()[0]).getPath());

        // WHEN
        workbenchDefinition.setPath("/node2");
        res = hierarchicalJcrContainer.getRootItemIds();

        // THEN
        assertEquals(1, res.size());
        assertEquals(node21.getPath(), ((Node) res.toArray()[0]).getPath());
    }

    @Test
    public void testIsRootByItem() throws RepositoryException {
        // GIVEN
        Node node = AbstractJcrContainerTest.createNode(rootNode, "node2", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        Node subnode = AbstractJcrContainerTest.createNode(node, "node2_1", NodeTypes.Content.NAME, PROPERTY_1, "name2_1");
        rootNode.setProperty(PROPERTY_2, "rootLevelProperty");

        // WHEN
        boolean rootNodeIsRoot = hierarchicalJcrContainer.isRoot(rootNode);
        boolean nodeIsRoot = hierarchicalJcrContainer.isRoot(node);
        boolean subnodeIsRoot = hierarchicalJcrContainer.isRoot(subnode);
        boolean rootNodePropertyIsRoot = hierarchicalJcrContainer.isRoot(rootNode.getProperty(PROPERTY_2));
        boolean nodePropertyIsRoot = hierarchicalJcrContainer.isRoot(node.getProperty(PROPERTY_1));

        // THEN
        assertTrue(rootNodeIsRoot);
        assertTrue(nodeIsRoot);
        assertTrue(rootNodePropertyIsRoot);
        assertFalse(subnodeIsRoot);
        assertFalse(nodePropertyIsRoot);
    }

    @Test
    public void testGetItemByPathWithNode() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.getSession().save();

        // WHEN
        Item res = hierarchicalJcrContainer.getItemByPath(node1.getPath());

        // THEN
        assertNotNull(res);
        assertTrue(res.isNode());
    }

    @Test
    public void testGetItemByPathWithProperty() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.getSession().save();

        // WHEN
        Item res = hierarchicalJcrContainer.getItemByPath(node1.getPath() + "/" + PROPERTY_1);

        // THEN
        assertNotNull(res);
        assertFalse(res.isNode());
    }

    @Test
    public void testGetChildrenExcludesOtherNodeTypes() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", "mgnl:page", PROPERTY_1, "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", "mgnl:contentNode", PROPERTY_1, "name2_1");
        node1.getSession().save();

        // WHEN
        Collection<Item> res = hierarchicalJcrContainer.getChildren(rootNode);

        // THEN
        assertEquals(1, res.size());
        // Currently don't get subtypes (mgnl:page is a sub type of mgnl:content but not included as
        // child.
        assertEquals(node1.getPath(), ((Node) res.toArray()[0]).getPath());
    }

    @Test
    public void testGetChildrenExcludesJcrName() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        rootNode.setProperty("jcr:name", "excluded");
        rootNode.setProperty(PROPERTY_1, "included");
        node1.getSession().save();
        ConfiguredItemTypeDefinition type1 = new ConfiguredItemTypeDefinition();
        type1.setItemType(NodeTypes.Content.NAME);
        workbenchDefinition.setMainItemType(type1);
        workbenchDefinition.setIncludeProperties(true);

        // WHEN
        Collection<Item> res = hierarchicalJcrContainer.getChildren(rootNode);

        // THEN
        assertEquals(3, res.size());
        assertEquals(node1.getPath(), ((Node) res.toArray()[0]).getPath());
        assertEquals(node2.getPath(), ((Node) res.toArray()[1]).getPath());
        assertEquals(rootNode.getProperty(PROPERTY_1).getString(), ((Property) res.toArray()[2]).getString());
    }

    @Test
    public void testPathInTree() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.getSession().save();
        // Initial Check
        assertEquals("/node1", hierarchicalJcrContainer.getPathInTree(node1));

        // WHEN
        workbenchDefinition.setPath("/node1");

        // THEN
        assertEquals("", hierarchicalJcrContainer.getPathInTree(node1));
    }

    @Test
    public void testGetItemByPathWhenWorkbenchPathIsNotRoot() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.getSession().save();

        // WHEN
        workbenchDefinition.setPath("/node1");
        Item res = hierarchicalJcrContainer.getItemByPath(node1.getPath());

        // THEN
        assertNotNull(res);
        assertEquals("/node1", res.getPath());
    }

    @Test
    public void testGetChildrenDoesNotIncludeMgnlAndJcrProperties() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        node1.setProperty("jcr:name", "baz");
        node1.setProperty("mgnl:createdBy", "qux");
        node1.setProperty("foo", "meh");
        node1.setProperty("bar", "duh");
        rootNode.getSession().save();
        ConfiguredItemTypeDefinition type1 = new ConfiguredItemTypeDefinition();
        type1.setItemType(NodeTypes.Content.NAME);
        workbenchDefinition.setMainItemType(type1);
        workbenchDefinition.setIncludeProperties(true);

        // WHEN
        Collection<Item> res = hierarchicalJcrContainer.getChildren(node1);

        // THEN
        assertEquals(3, res.size());

    }

    @Test
    public void testGetChildrenRetainsJcrOrder() throws RepositoryException, IOException {
        // GIVEN
        ConfiguredItemTypeDefinition itemTypeDef = new ConfiguredItemTypeDefinition();
        itemTypeDef.setItemType(NodeTypes.ContentNode.NAME);
        workbenchDefinition.setMainItemType(itemTypeDef);
        workbenchDefinition.setIncludeProperties(true);
        Session session = SessionTestUtil.createSession("config", "/server/filters/zzz", "/server/filters/abc", "/server/filters/aaa", "/server/filters/foo", "/server/filters/qux");

        // WHEN
        Collection<Item> res = hierarchicalJcrContainer.getChildren(session.getNode("/server/filters"));

        // THEN
        Item[] items = res.toArray(new Item[]{});
        assertEquals("/server/filters/zzz", items[0].getPath());
        assertEquals("/server/filters/abc", items[1].getPath());
        assertEquals("/server/filters/aaa", items[2].getPath());
        assertEquals("/server/filters/foo", items[3].getPath());
        assertEquals("/server/filters/qux", items[4].getPath());
    }

}
