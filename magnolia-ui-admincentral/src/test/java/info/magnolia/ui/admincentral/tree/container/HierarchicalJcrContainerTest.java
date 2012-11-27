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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
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

    private final String workspace = "config";
    private final String colName1 = "name";
    private final String colName2 = "shortname";

    private Session session;

    private Node rootNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(workspace);
        configuredWorkbench.setPath("/");

        WorkbenchActionRegistry workbenchActionRegistry = mock(WorkbenchActionRegistry.class);
        when(workbenchActionRegistry.getDefinitionToImplementationMappings()).thenReturn(new ArrayList<DefinitionToImplementationMapping<ActionDefinition,Action>>());

        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_" + colName1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_" + colName2);

        configuredWorkbench.addColumn(colDef1);
        configuredWorkbench.addColumn(colDef2);

        ItemTypeDefinition itemType = new ConfiguredItemTypeDefinition();
        ((ConfiguredItemTypeDefinition) itemType).setItemType(MgnlNodeType.NT_CONTENT);
        configuredWorkbench.setMainItemType(itemType);

        workbenchDefinition = configuredWorkbench;

        hierarchicalJcrContainer = new HierarchicalJcrContainer(workbenchDefinition);

        // Init session
        session = MgnlContext.getJCRSession(workspace);
        rootNode = session.getRootNode();
    }

    @Test
    public void testGetItem_NodeType() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        node1.getSession().save();
        String containerItemId = node1.getPath();

        // WHEN
        com.vaadin.data.Item item = hierarchicalJcrContainer.getItem(containerItemId);

        // THEN
        assertEquals(true, item instanceof JcrNodeAdapter);
        assertEquals(node1.getPath(), ((JcrNodeAdapter) item).getJcrItem().getPath());
    }

    @Test
    public void testAreChildrenAllowed_true() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        node1.getSession().save();
        String containerItemId = node1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.areChildrenAllowed(containerItemId);

        // THEN
        assertEquals(true, res);
    }

    @Test
    public void testAreChildrenAllowed_false() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        node1.getSession().save();
        String containerItemId = node1.getProperty("name").getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.areChildrenAllowed(containerItemId);

        // THEN
        assertEquals(false, res);
    }

    @Test
    public void testRootItemIds() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        AbstractJcrContainerTest.createNode(node1, "node1_1", MgnlNodeType.NT_CONTENT, "name", "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", MgnlNodeType.NT_CONTENT, "name", "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", MgnlNodeType.NT_CONTENT, "name", "name2_1");
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
    public void testisRoot_true() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        AbstractJcrContainerTest.createNode(node1, "node1_1", MgnlNodeType.NT_CONTENT, "name", "name1_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.isRoot(containerItemId1);

        // THEN
        assertEquals(true, res);
    }

    @Test
    public void testisRoot_false() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        Node node1_1 = AbstractJcrContainerTest.createNode(node1, "node1_1", MgnlNodeType.NT_CONTENT, "name", "name1_1");
        node1.getSession().save();

        String containerItemId1_1 = node1_1.getPath();

        // WHEN
        boolean res = hierarchicalJcrContainer.isRoot(containerItemId1_1);

        // THEN
        assertEquals(false, res);
    }

    @Test
    public void testGetChildren() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        Node node1_1 = AbstractJcrContainerTest.createNode(node1, "node1_1", MgnlNodeType.NT_CONTENT, "name", "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", MgnlNodeType.NT_CONTENT, "name", "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", MgnlNodeType.NT_CONTENT, "name", "name2_1");
        node1.getSession().save();

        String containerItemId1 = node1.getPath();

        // WHEN
        Collection<String> res = hierarchicalJcrContainer.getChildren(containerItemId1);

        // THEN
        assertEquals(1, res.size());
        assertEquals(true, res.contains(node1_1.getPath()));
    }

    @Test
    public void testGetParent() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        Node node1_1 = AbstractJcrContainerTest.createNode(node1, "node1_1", MgnlNodeType.NT_CONTENT, "name", "name1_1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", MgnlNodeType.NT_CONTENT, "name", "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", MgnlNodeType.NT_CONTENT, "name", "name2_1");
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
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:page", colName1, "name1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", MgnlNodeType.NT_CONTENT, colName1, "name2");
        Node node2_1 = AbstractJcrContainerTest.createNode(node2, "node2_1", MgnlNodeType.NT_CONTENT, colName1, "name2_1");
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
        assertEquals(node2_1.getPath(), ((Node) res.toArray()[0]).getPath());
    }

    @Test
    public void testIsRoot() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", "mgnl:page", colName1, "name1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", MgnlNodeType.NT_CONTENT, colName1, "name2");
        Node node2_1 = AbstractJcrContainerTest.createNode(node2, "node2_1", MgnlNodeType.NT_CONTENT, colName1, "name2_1");
        node1.getSession().save();

        // WHEN
        boolean isRoot_1 = hierarchicalJcrContainer.isRoot(node2);
        boolean isNotRoot_1 = hierarchicalJcrContainer.isRoot(node2_1);
        boolean isNotRoot_2 = hierarchicalJcrContainer.isRoot(node2.getProperty("name"));
        boolean isRoot_2 = hierarchicalJcrContainer.isRoot(rootNode);

        // THEN
        assertEquals(true, isRoot_1);
        assertEquals(true, isRoot_2);
        assertEquals(false, isNotRoot_1);
        assertEquals(false, isNotRoot_2);

    }

    @Test
    public void testGetItemByPath_Node() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, colName1, "name1");
        node1.getSession().save();

        // WHEN
        Item res = hierarchicalJcrContainer.getItemByPath(node1.getPath());

        // THEN
        assertNotNull(res);
        assertTrue(res.isNode());
    }

    @Test
    public void testGetItemByPath_Property() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, colName1, "name1");
        node1.getSession().save();

        // WHEN
        Item res = hierarchicalJcrContainer.getItemByPath(node1.getPath() + "/" + colName1);

        // THEN
        assertNotNull(res);
        assertEquals(false, res.isNode());
    }

    @Test
    public void testGetChildren_OnlyNode_oneNodeType() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", "mgnl:page", "name", "name2");
        AbstractJcrContainerTest.createNode(node2, "node2_1", "mgnl:contentNode", "name", "name2_1");
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
    public void testGetChildren_NodeAndProperty() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, "name", "name1");
        Node node2 = AbstractJcrContainerTest.createNode(rootNode, "node2", MgnlNodeType.NT_CONTENT, "name", "name2");
        rootNode.setProperty("jcr:name", "excluded");
        rootNode.setProperty("name", "included");
        node1.getSession().save();
        ConfiguredItemTypeDefinition type1 = new ConfiguredItemTypeDefinition();
        type1.setItemType(MgnlNodeType.NT_CONTENT);
        workbenchDefinition.setMainItemType(type1);
        workbenchDefinition.setIncludeProperties(true);
        // WHEN
        Collection<Item> res = hierarchicalJcrContainer.getChildren(rootNode);

        // THEN

        assertEquals(3, res.size());
        assertEquals(node1.getPath(), ((Node) res.toArray()[0]).getPath());
        assertEquals(node2.getPath(), ((Node) res.toArray()[1]).getPath());
        assertEquals(rootNode.getProperty("name").getString(), ((Property) res.toArray()[2]).getString());
    }

    @Test
    public void testPathInTree() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, colName1, "name1");
        node1.getSession().save();
        // Initial Check
        assertEquals("/node1", hierarchicalJcrContainer.getPathInTree(node1));

        // WHEN
        workbenchDefinition.setPath("/node1");

        // THEN
        assertEquals("", hierarchicalJcrContainer.getPathInTree(node1));
    }

    @Test
    public void testGetItemByPathWhenWorkbenchPathIsOtherThanRoot() throws RepositoryException {
        // GIVEN
        Node node1 = AbstractJcrContainerTest.createNode(rootNode, "node1", MgnlNodeType.NT_CONTENT, colName1, "name1");
        node1.getSession().save();


        // WHEN
        workbenchDefinition.setPath("/node1");
        Item res = hierarchicalJcrContainer.getItemByPath(node1.getPath());

        // THEN
        assertNotNull(res);
        assertEquals("/node1", res.getPath());
    }

}
