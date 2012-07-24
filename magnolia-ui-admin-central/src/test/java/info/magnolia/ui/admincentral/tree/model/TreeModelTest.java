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
package info.magnolia.ui.admincentral.tree.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.column.PropertyTypeColumn;
import info.magnolia.ui.admincentral.container.JcrContainerTest;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactoryImpl;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.model.workbench.definition.ItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Main test class for {TreeModel}
 */
public class TreeModelTest extends RepositoryTestCase {

    private TreeModel treeModel;

    private final String workspace = "config";

    private final String colName1 = "name";

    private final String colName2 = "shortname";

    private Session session;

    private Node rootNode;

    private WorkbenchDefinition workbenchDefinition;

    @SuppressWarnings("deprecation")
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        WorkbenchActionFactory workbenchActionFactory = new WorkbenchActionFactoryImpl();
        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();
        configuredWorkbench.setWorkspace(workspace);
        configuredWorkbench.setPath("/");

        ConfiguredItemTypeDefinition type1 = new ConfiguredItemTypeDefinition();
        type1.setItemType("mgnl:content");
        type1.setIcon("icone1");
        configuredWorkbench.addItemType(type1);
        // Init col

        Map<String, Column< ? >> columns = new LinkedHashMap<String, Column< ? >>();
        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_" + colName1);
        colDef1.setPropertyName("PropertyName_" + colName1);
        Column<AbstractColumnDefinition> col1 = new PropertyTypeColumn(colDef1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_" + colName2);
        colDef2.setPropertyName("PropertyName_" + colName2);
        Column<AbstractColumnDefinition> col2 = new PropertyTypeColumn(colDef2);

        columns.put(colName1, col1);
        columns.put(colName2, col2);
        configuredWorkbench.addColumn(colDef1);
        configuredWorkbench.addColumn(colDef2);

        treeModel = new TreeModel(configuredWorkbench, workbenchActionFactory);
        workbenchDefinition = configuredWorkbench;

        // Init session
        session = MgnlContext.getSystemContext().getJCRSession(workspace);
        rootNode = session.getRootNode();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        treeModel = null;
    }

    @Test
    public void testGetRootItemIds() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:page", colName1, "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:content", colName1, "name2");
        Node node2_1 = JcrContainerTest.createNode(node2, "node2_1", "mgnl:content", colName1, "name2_1");
        node1.getSession().save();
        // Initial check
        Collection<Item> res = treeModel.getRootItemIds();
        assertEquals(1, res.size());
        assertEquals(node2.getPath(), ((Node) res.toArray()[0]).getPath());

        // WHEN
        ((ConfiguredWorkbenchDefinition) workbenchDefinition).setPath("/node2");
        res = treeModel.getRootItemIds();

        // THEN
        assertEquals(1, res.size());
        assertEquals(node2_1.getPath(), ((Node) res.toArray()[0]).getPath());
    }

    @Test
    public void testIsRoot() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:page", colName1, "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:content", colName1, "name2");
        Node node2_1 = JcrContainerTest.createNode(node2, "node2_1", "mgnl:content", colName1, "name2_1");
        node1.getSession().save();

        // WHEN
        boolean isRoot_1 = treeModel.isRoot(node2);
        boolean isNotRoot_1 = treeModel.isRoot(node2_1);
        boolean isNotRoot_2 = treeModel.isRoot(node2.getProperty("name"));
        boolean isRoot_2 = treeModel.isRoot(rootNode);

        // THEN
        assertEquals(true, isRoot_1);
        assertEquals(true, isRoot_2);
        assertEquals(false, isNotRoot_1);
        assertEquals(false, isNotRoot_2);

    }


    @Test
    public void testSetColumnComponent() {
        // TODO
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    public void testGetItemIcon_Node() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", colName1, "name1");
        node1.getSession().save();

        // WHEN
        String icone = treeModel.getItemIcon(node1);

        // THEN
        assertNotNull(icone);
        assertEquals("icone1", icone);
    }

    @Test
    public void testGetItemIcon_Property() {
        // TODO
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    public void testGetNodeByIdentifier() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", colName1, "name1");
        node1.getSession().save();

        // WHEN
        Node res = treeModel.getNodeByIdentifier(node1.getIdentifier());

        // THEN
        assertNotNull(res);
        assertEquals(node1.getPath(), res.getPath());
    }

    @Test
    public void testGetItemByPath_Node() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", colName1, "name1");
        node1.getSession().save();

        // WHEN
        Item res = treeModel.getItemByPath(node1.getPath());

        // THEN
        assertNotNull(res);
        assertEquals(true, res.isNode());
    }

    @Test
    public void testGetItemByPath_Property() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", colName1, "name1");
        node1.getSession().save();

        // WHEN
        Item res = treeModel.getItemByPath(node1.getPath() + "/" + colName1);

        // THEN
        assertNotNull(res);
        assertEquals(false, res.isNode());
    }

    @Test
    public void testMoveItem() {
        // TODO
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    public void testMoveItemBefore() {
        // TODO
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    public void testMoveItemAfter() {
        // TODO
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    public void testGetChildren_OnlyNode_oneNodeType() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:page", "name", "name2");
        JcrContainerTest.createNode(node2, "node2_1", "mgnl:contentNode", "name", "name2_1");
        node1.getSession().save();

        // WHEN
        Collection<Item> res = treeModel.getChildren(rootNode);

        // THEN
        assertEquals(1, res.size());
        // Currently don't get subtypes (mgnl:page is a sub type of mgnl:content but not included as
        // child.
        assertEquals(node1.getPath(), ((Node) res.toArray()[0]).getPath());
    }

    @Test
    public void testGetChildren_OnlyNode_twoNodeType() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:contentNode", "name", "name2");
        JcrContainerTest.createNode(node2, "node2_1", "mgnl:contentNode", "name", "name2_1");
        node1.getSession().save();
        ConfiguredItemTypeDefinition type1 = new ConfiguredItemTypeDefinition();
        type1.setItemType("mgnl:contentNode");
        ((ConfiguredWorkbenchDefinition) workbenchDefinition).addItemType(type1);
        // WHEN
        Collection<Item> res = treeModel.getChildren(rootNode);

        // THEN

        assertEquals(2, res.size());
        assertEquals(node1.getPath(), ((Node) res.toArray()[0]).getPath());
        assertEquals(node2.getPath(), ((Node) res.toArray()[1]).getPath());
    }

    @Test
    public void testGetChildren_NodeAndProperty() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        rootNode.setProperty("jcr:name", "excluded");
        rootNode.setProperty("name", "included");
        node1.getSession().save();
        ConfiguredItemTypeDefinition type1 = new ConfiguredItemTypeDefinition();
        type1.setItemType(ItemTypeDefinition.ITEM_TYPE_PROPERTY);
        ((ConfiguredWorkbenchDefinition) workbenchDefinition).addItemType(type1);
        // WHEN
        Collection<Item> res = treeModel.getChildren(rootNode);

        // THEN

        assertEquals(3, res.size());
        assertEquals(node1.getPath(), ((Node) res.toArray()[0]).getPath());
        assertEquals(node2.getPath(), ((Node) res.toArray()[1]).getPath());
        assertEquals(rootNode.getProperty("name").getString(), ((Property) res.toArray()[2]).getString());
    }

    @Test
    public void testPathInTree() throws RepositoryException {
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", colName1, "name1");
        node1.getSession().save();
        // Initial Check
        assertEquals("/node1", treeModel.getPathInTree(node1));

        // WHEN
        ((ConfiguredWorkbenchDefinition) workbenchDefinition).setPath("/node1");

        // THEN
        assertEquals("", treeModel.getPathInTree(node1));
    }

}
