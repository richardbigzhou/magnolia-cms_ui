/**
 * This file Copyright (c) 2012-2014 Magnolia International
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
package info.magnolia.ui.workbench.tree.drop;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.column.definition.PropertyTypeColumnDefinition;
import info.magnolia.ui.workbench.container.AbstractJcrContainerTest;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.MoveLocation;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;

/**
 * Tests for HierarchicalJcrContainer.
 */
public class TreeViewDropHandlerTest extends RepositoryTestCase {

    private HierarchicalJcrContainer hierarchicalJcrContainer;

    private ConfiguredJcrContentConnectorDefinition connectorDefinition;

    private static final String WORKSPACE = "config";

    private static final String PROPERTY_1 = "name";

    private static final String PROPERTY_2 = "shortname";

    private Session session;

    private Node rootNode;
    private String workspace;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ConfiguredWorkbenchDefinition configuredWorkbench = new ConfiguredWorkbenchDefinition();

        // Add view
        ConfiguredContentPresenterDefinition contentView = new TreePresenterDefinition();
        configuredWorkbench.addContentView(contentView);

        PropertyTypeColumnDefinition colDef1 = new PropertyTypeColumnDefinition();
        colDef1.setSortable(true);
        colDef1.setName(PROPERTY_1);
        colDef1.setLabel("Label_" + PROPERTY_1);
        PropertyTypeColumnDefinition colDef2 = new PropertyTypeColumnDefinition();
        colDef2.setSortable(false);
        colDef2.setName(PROPERTY_2);
        colDef2.setLabel("Label_" + PROPERTY_2);

        contentView.addColumn(colDef1);
        contentView.addColumn(colDef2);

        ConfiguredNodeTypeDefinition nodeTypeDefinition = new ConfiguredNodeTypeDefinition();
        nodeTypeDefinition.setName(NodeTypes.Content.NAME);

        // workbenchDefinition = configuredWorkbench;
        connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.setRootPath("/");
        connectorDefinition.setWorkspace(WORKSPACE);
        connectorDefinition.addNodeType(nodeTypeDefinition);

        // Init session
        session = MgnlContext.getJCRSession(WORKSPACE);
        rootNode = session.getRootNode();
        workspace = WORKSPACE;
    }

    @Test
    public void testMoveItem() throws RepositoryException {
        // GIVEN
        Node source = AbstractJcrContainerTest.createNode(rootNode, "source", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node target = AbstractJcrContainerTest.createNode(rootNode, "target", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        target.getSession().save();

        // WHEN
        boolean res = new TreeViewDropHandler(null, null).moveItem(new JcrNodeAdapter(source), new JcrNodeAdapter(target), MoveLocation.INSIDE);

        // THEN
        assertTrue(res);
        assertTrue(target.hasNode("source"));
    }

    @Test
    public void testMoveItemFalseChildCanNotBeSetAsParent() throws RepositoryException {
        // GIVEN
        Node source = AbstractJcrContainerTest.createNode(rootNode, "source", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node target = AbstractJcrContainerTest.createNode(source, "target", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        target.getSession().save();

        // WHEN
        boolean res = new TreeViewDropHandler(null, null).moveItem(new JcrNodeAdapter(source), new JcrNodeAdapter(target), MoveLocation.INSIDE);

        // THEN
        assertFalse(res);
    }

    @Test
    public void testMoveItemFalseNoOperationOnProperty() throws RepositoryException {
        // GIVEN
        Node source = AbstractJcrContainerTest.createNode(rootNode, "source", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Property sourceProperty = source.setProperty("property", "property");
        source.getSession().save();

        // WHEN
        boolean res = new TreeViewDropHandler(null, null).moveItem(new JcrPropertyAdapter(sourceProperty), new JcrNodeAdapter(source), MoveLocation.INSIDE);

        // THEN
        assertFalse(res);
    }

    @Test
    public void testMoveItemBefore() throws RepositoryException {
        // GIVEN
        Node first = AbstractJcrContainerTest.createNode(rootNode, "first", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node second = AbstractJcrContainerTest.createNode(rootNode, "second", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        second.getSession().save();
        NodeIterator nodeIterator = rootNode.getNodes();
        nodeIterator.nextNode();
        assertEquals("first", nodeIterator.nextNode().getName());

        // WHEN
        boolean res = new TreeViewDropHandler(null, null).moveItem(new JcrNodeAdapter(second), new JcrNodeAdapter(first), MoveLocation.BEFORE);

        // THEN
        assertTrue(res);
        nodeIterator = rootNode.getNodes();
        nodeIterator.nextNode();
        assertEquals("second", nodeIterator.nextNode().getName());
    }

    @Test
    public void testMoveItemAfter() throws RepositoryException {
        // GIVEN
        Node first = AbstractJcrContainerTest.createNode(rootNode, "first", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node second = AbstractJcrContainerTest.createNode(rootNode, "second", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        second.getSession().save();
        NodeIterator nodeIterator = rootNode.getNodes();
        nodeIterator.nextNode();
        assertEquals("first", nodeIterator.nextNode().getName());

        // WHEN
        boolean res = new TreeViewDropHandler(null, null).moveItem(new JcrNodeAdapter(first), new JcrNodeAdapter(second), MoveLocation.AFTER);

        // THEN
        assertTrue(res);
        nodeIterator = rootNode.getNodes();
        nodeIterator.nextNode();
        assertEquals("second", nodeIterator.nextNode().getName());
    }

    @Test
    public void moveMultipleItem() throws RepositoryException {
        // GIVEN
        // create source/target nodes/items
        Node source1 = AbstractJcrContainerTest.createNode(rootNode, "source1", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node source2 = AbstractJcrContainerTest.createNode(rootNode, "source2", NodeTypes.Content.NAME, PROPERTY_1, "name1");
        Node target = AbstractJcrContainerTest.createNode(rootNode, "target", NodeTypes.Content.NAME, PROPERTY_1, "name2");
        target.getSession().save();
        JcrNodeAdapter sourceItem1 = new JcrNodeAdapter(source1);
        JcrNodeAdapter sourceItem2 = new JcrNodeAdapter(source2);
        JcrNodeAdapter targetItem = new JcrNodeAdapter(target);
        Set<Object> sources = new HashSet<Object>(Arrays.asList(sourceItem1, sourceItem2));

        // Mock the TreeTable
        MagnoliaTreeTable treeTable = mock(MagnoliaTreeTable.class);
        HierarchicalJcrContainer jcrContainer = mock(HierarchicalJcrContainer.class);
        when(treeTable.getContainerDataSource()).thenReturn(jcrContainer);
        when(jcrContainer.getItem(sourceItem1)).thenReturn(sourceItem1);
        when(jcrContainer.getItem(sourceItem2)).thenReturn(sourceItem2);
        when(jcrContainer.getItem(targetItem)).thenReturn(targetItem);

        // Mock the DDEvent and Transferable and Target Detail
        DragAndDropEvent event = mock(DragAndDropEvent.class);
        AbstractSelectTargetDetails targetDetail = mock(AbstractSelectTargetDetails.class);
        when(targetDetail.getItemIdOver()).thenReturn(targetItem);
        when(targetDetail.getDropLocation()).thenReturn(VerticalDropLocation.MIDDLE);
        DataBoundTransferable transferable = mock(DataBoundTransferable.class);
        when(transferable.getSourceComponent()).thenReturn(treeTable);
        when(treeTable.getValue()).thenReturn(sources);
        when(event.getTargetDetails()).thenReturn(targetDetail);
        when(event.getTransferable()).thenReturn(transferable);

        // Mock the constrain
        DropConstraint constrain = mock(DropConstraint.class);
        when(constrain.allowedAsChild(sourceItem1, targetItem)).thenReturn(true);
        when(constrain.allowedAsChild(sourceItem2, targetItem)).thenReturn(true);

        // WHEN
        new TreeViewDropHandler(treeTable, constrain).drop(event);

        // THEN
        assertTrue(target.hasNode("source1"));
        assertTrue(target.hasNode("source2"));
    }
}
