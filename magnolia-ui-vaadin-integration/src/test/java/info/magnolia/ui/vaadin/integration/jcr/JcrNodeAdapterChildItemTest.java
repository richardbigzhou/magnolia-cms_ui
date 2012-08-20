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
package info.magnolia.ui.vaadin.integration.jcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test class {@link JcrNodeAdapter} for the Child Item handling.
 * Add/Remove Existing/New Child Items
 */

public class JcrNodeAdapterChildItemTest {

    private String workspaceName = "workspace";
    private MockSession session;
    private Node baseNode;

    @Before
    public void setUp() throws RepositoryException {
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
        baseNode = session.getRootNode().addNode("baseNode");
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testAddChild() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        // Create a child node
        Node child = baseNode.addNode("child");
        JcrNodeAdapter childItem = new JcrNodeAdapter(child);
        item.addChild( childItem);
        //Create one new child Item
        JcrNewNodeAdapter newChild = new JcrNewNodeAdapter(baseNode, "mgnl:content");
        item.addChild(newChild);

        // WHEN
        Map<String, JcrItemNodeAdapter> res = item.getChildren();

        // THEN
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals(childItem, res.get(childItem.getNodeIdentifier()));
        assertEquals(item, childItem.getParent());
        assertEquals(newChild, res.get(newChild.getNodeIdentifier()));
        assertEquals(item, newChild.getParent());
    }

    @Test
    public void testRemoveChild() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        // Create a child node
        Node child = baseNode.addNode("child");
        JcrNodeAdapter childItem = new JcrNodeAdapter(child);
        item.addChild(childItem);
        //Create one new child Item
        JcrNewNodeAdapter newChild = new JcrNewNodeAdapter(baseNode, "mgnl:content");
        item.addChild(newChild);

        // WHEN
        boolean resBoolean = item.removeChild(childItem);

        // THEN
        Map<String, JcrItemNodeAdapter> res = item.getChildren();
        assertEquals(true, resBoolean);
        assertEquals(1, res.size());
        assertEquals(null, res.get(childItem.getNodeIdentifier()));
        assertEquals(newChild, res.get(newChild.getNodeIdentifier()));
        assertEquals(item, newChild.getParent());
        Map<String, JcrItemNodeAdapter> resRemobed = item.getRemovedChildren();
        assertEquals(1, resRemobed.size());
        assertEquals(childItem, resRemobed.get(childItem.getNodeIdentifier()));
    }

    @Test
    public void testAddNewChildAndStore() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        //Add property to the  Item
        DefaultProperty property = new DefaultProperty("propertyName", "propertyValue");
        item.addItemProperty("propertyName", property);
        //Create one new child Item
        JcrNewNodeAdapter newChild = new JcrNewNodeAdapter(baseNode, "mgnl:content", "childNode");
        item.addChild(newChild);
        //Add property to the child Item
        DefaultProperty childProperty = new DefaultProperty("childPropertyName", "childPropertyValue");
        newChild.addItemProperty("childPropertyName", childProperty);

        // WHEN
        Node res = item.getNode();

        // THEN
        assertNotNull(res);
        assertEquals(baseNode, res);
        assertEquals(true, res.hasProperty("propertyName"));
        assertEquals("propertyValue", res.getProperty("propertyName").getValue().getString());
        assertEquals(true, res.hasNode("childNode"));
        assertEquals(true, res.getNode("childNode").hasProperty("childPropertyName"));
        assertEquals("childPropertyValue", res.getNode("childNode").getProperty("childPropertyName").getValue().getString());
    }

    @Test
    public void testAddExistingChildAndStore() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        //Add property to the  Item
        DefaultProperty property = new DefaultProperty("propertyName", "propertyValue");
        item.addItemProperty("propertyName", property);
        // Create a child node
        Node child = baseNode.addNode("childNode");
        JcrNodeAdapter childItem = new JcrNodeAdapter(child);
        item.addChild(childItem);
        //Add property to the child Item
        DefaultProperty childProperty = new DefaultProperty("childPropertyName", "childPropertyValue");
        childItem.addItemProperty("childPropertyName", childProperty);

        // WHEN
        Node res = item.getNode();

        // THEN
        assertNotNull(res);
        assertEquals(baseNode, res);
        assertEquals(true, res.hasProperty("propertyName"));
        assertEquals("propertyValue", res.getProperty("propertyName").getValue().getString());
        assertEquals(true, res.hasNode("childNode"));
        assertEquals(true, res.getNode("childNode").hasProperty("childPropertyName"));
        assertEquals("childPropertyValue", res.getNode("childNode").getProperty("childPropertyName").getValue().getString());
    }

    @Test
    public void testAddMixedChildAndStore() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        //Add property to the  Item
        DefaultProperty property = new DefaultProperty("propertyName", "propertyValue");
        item.addItemProperty("propertyName", property);
        // Create a child node
        Node child = baseNode.addNode("childNode");
        JcrNodeAdapter childItem = new JcrNodeAdapter(child);
        item.addChild(childItem);
        //Add property to the child Item
        DefaultProperty childProperty = new DefaultProperty("childPropertyName", "childPropertyValue");
        childItem.addItemProperty("childPropertyName", childProperty);
        //Create one new child Item
        JcrNewNodeAdapter newChild = new JcrNewNodeAdapter(baseNode, "mgnl:content", "newChild");
        item.addChild(newChild);
        //Add property to the child Item
        DefaultProperty childPropertyNew = new DefaultProperty("childNewPropertyName", "childNewPropertyValue");
        newChild.addItemProperty("childNewPropertyName", childPropertyNew);

        // WHEN
        Node res = item.getNode();

        // THEN
        assertNotNull(res);
        assertEquals(baseNode, res);
        assertEquals(true, res.hasProperty("propertyName"));
        assertEquals("propertyValue", res.getProperty("propertyName").getValue().getString());
        assertEquals(true, res.hasNode("childNode"));
        assertEquals(true, res.getNode("childNode").hasProperty("childPropertyName"));
        assertEquals("childPropertyValue", res.getNode("childNode").getProperty("childPropertyName").getValue().getString());
        assertEquals(true, res.hasNode("newChild"));
        assertEquals(true, res.getNode("newChild").hasProperty("childNewPropertyName"));
        assertEquals("childNewPropertyValue", res.getNode("newChild").getProperty("childNewPropertyName").getValue().getString());
    }

    @Test
    public void testAddRemoveNewChildAndStore() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        //Add property to the  Item
        DefaultProperty property = new DefaultProperty("propertyName", "propertyValue");
        item.addItemProperty("propertyName", property);
        //Create one new child Item
        JcrNewNodeAdapter newChild = new JcrNewNodeAdapter(baseNode, "mgnl:content", "childNode");
        item.addChild( newChild);
        //Add property to the child Item
        DefaultProperty childProperty = new DefaultProperty("childPropertyName", "childPropertyValue");
        newChild.addItemProperty("childPropertyName", childProperty);

        // WHEN
        item.removeChild(newChild);

        // THEN
        // Get node
        Node res = item.getNode();
        assertNotNull(res);
        assertEquals(baseNode, res);
        assertEquals(true, res.hasProperty("propertyName"));
        assertEquals("propertyValue", res.getProperty("propertyName").getValue().getString());
        assertEquals(false, res.hasNode("childNode"));
    }

    @Test
    public void testAddRemoveExistingChildAndStore() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        //Add property to the  Item
        DefaultProperty property = new DefaultProperty("propertyName", "propertyValue");
        item.addItemProperty("propertyName", property);
        //Create one new child Item
        // Create a child node
        Node child = baseNode.addNode("childNode");
        JcrNodeAdapter childItem = new JcrNodeAdapter(child);
        item.addChild( childItem);
        assertEquals(true, baseNode.hasNode("childNode"));
        //Add property to the child Item
        DefaultProperty childProperty = new DefaultProperty("childPropertyName", "childPropertyValue");
        childItem.addItemProperty("childPropertyName", childProperty);

        // WHEN
        item.removeChild(childItem);

        // THEN
        // Get node
        Node res = item.getNode();
        assertNotNull(res);
        assertEquals(baseNode, res);
        assertEquals(true, res.hasProperty("propertyName"));
        assertEquals("propertyValue", res.getProperty("propertyName").getValue().getString());
        assertEquals(false, res.hasNode("childNode"));
    }

    @Test
    public void testAddRemoveAddChildAndStore() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(baseNode);
        //Add property to the  Item
        DefaultProperty property = new DefaultProperty("propertyName", "propertyValue");
        item.addItemProperty("propertyName", property);
        //Create one new child Item
        // Create a child node
        Node child = baseNode.addNode("childNode");
        JcrNodeAdapter childItem = new JcrNodeAdapter(child);
        assertEquals(true, baseNode.hasNode("childNode"));
        //Add property to the child Item
        DefaultProperty childProperty = new DefaultProperty("childPropertyName", "childPropertyValue");
        childItem.addItemProperty("childPropertyName", childProperty);

        // WHEN
        item.addChild(childItem);
        item.removeChild(childItem);
        item.addChild( childItem);

        // THEN
        // Get node
        Node res = item.getNode();
        assertNotNull(res);
        assertEquals(baseNode, res);
        assertEquals(true, res.hasProperty("propertyName"));
        assertEquals("propertyValue", res.getProperty("propertyName").getValue().getString());
        assertEquals(true, res.hasNode("childNode"));
        assertEquals(true, res.getNode("childNode").hasProperty("childPropertyName"));
        assertEquals("childPropertyValue", res.getNode("childNode").getProperty("childPropertyName").getValue().getString());
        assertEquals(1, item.getChildren().size());
        assertEquals(0, item.getRemovedChildren().size());
    }
}

