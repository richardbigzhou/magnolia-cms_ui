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
import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.column.PropertyTypeColumn;
import info.magnolia.ui.admincentral.container.JcrContainerTest;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactory;
import info.magnolia.ui.admincentral.workbench.action.WorkbenchActionFactoryImpl;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyTypeColumnDefinition;
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
public class TreeModelTest extends RepositoryTestCase{

    private TreeModel treeModel;
    private String workspace = "config";
    private String colName1 = "name";
    private String colName2 = "shortname";
    private Session session;
    private Node rootNode;
    private WorkbenchDefinition workbenchDefinition;


    @Override
    @Before
    public void setUp() throws Exception{
        super.setUp();

        WorkbenchActionFactory workbenchActionFactory = new WorkbenchActionFactoryImpl();
        workbenchDefinition = new WorkbenchDefinition();
        workbenchDefinition.setWorkspace(workspace);
        workbenchDefinition.setPath("/");

        ItemTypeDefinition type1 = new ItemTypeDefinition();
        type1.setItemType("mgnl:content");
        workbenchDefinition.addItemType(type1);
        //Init col

        Map<String, Column<?>> columns = new LinkedHashMap<String, Column<?>>();
        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(colName1);
        colDef1.setLabel("Label_"+colName1);
        Column<AbstractColumnDefinition> col1 = new PropertyTypeColumn(colDef1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(colName2);
        colDef2.setLabel("Label_"+colName2);
        Column<AbstractColumnDefinition> col2 = new PropertyTypeColumn(colDef2);

        columns.put(colName1, col1);
        columns.put(colName2, col2);
        workbenchDefinition.addColumn(colDef1);
        workbenchDefinition.addColumn(colDef2);

        treeModel = new TreeModel(workbenchDefinition, columns, workbenchActionFactory);

        //Init session
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
    public void testGetColumnComponent() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }


    @Test
    public void testSetColumnComponent() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }


    @Test
    public void testGetItemIcon() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }


    @Test
    public void testGetNodeByIdentifier() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }


    @Test
    public void testGetItemByPath() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    public void testMoveItem() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }


    @Test
    public void testMoveItemBefore() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }


    @Test
    public void testMoveItemAfter() {
        //TODO
        // GIVEN

        // WHEN

        // THEN
    }


    @Test
    public void testGetChildren_OnlyNode_oneNodeType() throws RepositoryException{
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:page", "name", "name2");
        Node node2_1 = JcrContainerTest.createNode(node2, "node2_1", "mgnl:contentNode", "name", "name2_1");
        node1.getSession().save();

        // WHEN
        Collection<Item> res = treeModel.getChildren(rootNode);

        // THEN
        assertEquals(1, res.size());
        // Currently don't get subtypes (mgnl:page is a sub type of mgnl:content but not included as child.
        assertEquals(node1.getPath(), ((Node)res.toArray()[0]).getPath());
    }

    @Test
    public void testGetChildren_OnlyNode_twoNodeType() throws RepositoryException{
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:contentNode", "name", "name2");
        Node node2_1 = JcrContainerTest.createNode(node2, "node2_1", "mgnl:contentNode", "name", "name2_1");
        node1.getSession().save();
        ItemTypeDefinition type1 = new ItemTypeDefinition();
        type1.setItemType("mgnl:contentNode");
        workbenchDefinition.addItemType(type1);
        // WHEN
        Collection<Item> res = treeModel.getChildren(rootNode);

        // THEN

        assertEquals(2, res.size());
        assertEquals(node1.getPath(), ((Node)res.toArray()[0]).getPath());
        assertEquals(node2.getPath(), ((Node)res.toArray()[1]).getPath());
    }

    @Test
    public void testGetChildren_NodeAndProperty() throws RepositoryException{
        // GIVEN
        Node node1 = JcrContainerTest.createNode(rootNode, "node1", "mgnl:content", "name", "name1");
        Node node2 = JcrContainerTest.createNode(rootNode, "node2", "mgnl:content", "name", "name2");
        rootNode.setProperty( "jcr:name", "excluded");
        rootNode.setProperty( "name", "included");
        node1.getSession().save();
        ItemTypeDefinition type1 = new ItemTypeDefinition();
        type1.setItemType(ItemTypeDefinition.ITEM_TYPE_NODE_DATA);
        workbenchDefinition.addItemType(type1);
        // WHEN
        Collection<Item> res = treeModel.getChildren(rootNode);

        // THEN

        assertEquals(3, res.size());
        assertEquals(node1.getPath(), ((Node)res.toArray()[0]).getPath());
        assertEquals(node2.getPath(), ((Node)res.toArray()[1]).getPath());
        assertEquals(rootNode.getProperty("name").getString(), ((Property)res.toArray()[2]).getString());
    }

    @Test
    public void testPathInTree() {
      //TODO
        // GIVEN

        // WHEN

        // THEN
    }

    @Test
    public void testGetColumns() {
      //TODO
        // GIVEN

        // WHEN

        // THEN
    }


}
