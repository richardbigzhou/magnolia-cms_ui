/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.workbench.tree;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredNodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.NodeTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.ui.Table;

/**
 * Testing the default {@link TreeViewImpl} logic.
 */
public class TreeViewImplTest extends RepositoryTestCase {

    private static final String WORKSPACE = "config";
    private static final String NODE_ROOT_ITEM_ID = "root-depth1";
    private static final String NODE_PARENT = "parent-depth2";
    private static final String NODE = "node-depth3";
    private static final String NODE_CHILD = "child-depth4";
    private static final String NODE_PROPERTY = "property-depth4";

    private Session session;

    private TreeViewImpl view;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(WORKSPACE);

        ConfiguredJcrContentConnectorDefinition connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        view = new TreeViewImpl();

        ConfiguredNodeTypeDefinition nodeTypeDefinition = new ConfiguredNodeTypeDefinition();
        nodeTypeDefinition.setStrict(false);
        nodeTypeDefinition.setName("nt:unstructured");

        connectorDefinition.setNodeTypes(Arrays.asList((NodeTypeDefinition) nodeTypeDefinition));
        connectorDefinition.setRootPath("/");
        connectorDefinition.setWorkspace(WORKSPACE);

        Table table = view.createTable(new HierarchicalJcrContainer(connectorDefinition));
        view.initializeTable(table);
    }

    @Test
    public void testSelectExpandsTreeToNode() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node visibleRoot = root.addNode(NODE_ROOT_ITEM_ID);
        Node parent = visibleRoot.addNode(NODE_PARENT);
        Node node = parent.addNode(NODE);
        Node child = node.addNode(NODE_CHILD);
        node.setProperty(NODE_PROPERTY, "112");

        // initial state
        assertTrue(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(visibleRoot)));
        assertTrue(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(parent)));
        assertTrue(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(node)));
        assertTrue(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(child)));

        // WHEN
        view.select(Arrays.asList((Object)JcrItemUtil.getItemId(node)));

        // THEN
        assertFalse(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(visibleRoot)));
        assertFalse(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(parent)));
    }

    @Test
    public void testSelectDoesNotExpandNodeItself() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node visibleRoot = root.addNode(NODE_ROOT_ITEM_ID);
        Node parent = visibleRoot.addNode(NODE_PARENT);
        Node node = parent.addNode(NODE);
        node.addNode(NODE_CHILD);
        node.setProperty(NODE_PROPERTY, "112");

        // WHEN
        Object itemId = JcrItemUtil.getItemId(node);
        view.select(Arrays.asList(itemId));

        // THEN
        assertTrue(view.asVaadinComponent().isCollapsed(node.getIdentifier()));
    }

    @Ignore("See ticket MGNLUI-2078 - Need to verify if test is still relevant.")
    @Test
    public void testSelectExpandsNodeAtRootLevel() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node visibleRoot = root.addNode(NODE_ROOT_ITEM_ID);
        visibleRoot.addNode(NODE_PARENT);

        // WHEN
        view.select(Arrays.asList((Object)visibleRoot.getIdentifier()));

        // THEN
        assertFalse(view.asVaadinComponent().isCollapsed(visibleRoot.getIdentifier()));
    }

    @Test
    public void testSelectExpandsTreeToProperty() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node visibleRoot = root.addNode(NODE_ROOT_ITEM_ID);
        Node parent = visibleRoot.addNode(NODE_PARENT);
        Node node = parent.addNode(NODE);
        node.addNode(NODE_CHILD);
        node.setProperty(NODE_PROPERTY, "112");

        Object propertyFakeId = new JcrPropertyItemId(node.getIdentifier(), WORKSPACE, NODE_PROPERTY);

        // WHEN
        view.select(Arrays.asList((Object)propertyFakeId));

        // THEN
        assertFalse(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(visibleRoot)));
        assertFalse(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(parent)));
        assertFalse(view.asVaadinComponent().isCollapsed(JcrItemUtil.getItemId(node)));
    }

    @Test
    public void testCollapseNodeWithSelectedChildUnselectsChild() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node visibleRoot = root.addNode(NODE_ROOT_ITEM_ID);
        Node parent = visibleRoot.addNode(NODE_PARENT);
        Node node = parent.addNode(NODE);
        Node child = node.addNode(NODE_CHILD);

        Object visibleRootId = JcrItemUtil.getItemId(visibleRoot);
        Object childId = JcrItemUtil.getItemId(child);

        view.asVaadinComponent().setCollapsed(visibleRootId, false);
        view.select(Arrays.asList((Object) visibleRootId, childId));

        // WHEN
        view.asVaadinComponent().setCollapsed(visibleRootId, true);

        // THEN
        assertTrue(view.asVaadinComponent().isCollapsed(visibleRootId));
        assertTrue(view.asVaadinComponent().isSelected(visibleRootId));
        assertFalse(view.asVaadinComponent().isSelected(childId));
    }
}
