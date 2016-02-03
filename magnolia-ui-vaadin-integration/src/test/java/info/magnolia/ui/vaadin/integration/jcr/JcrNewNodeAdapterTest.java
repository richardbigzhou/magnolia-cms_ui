/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import static org.junit.Assert.*;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.ModelConstants;

import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

/**
 * Test class for JcrNewNodeAdapter.
 */
public class JcrNewNodeAdapterTest {

    private final String worksapceName = "workspace";
    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(worksapceName);
        MockContext ctx = new MockContext();
        ctx.setUser(new MgnlUser("test", "admin", Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP, null, null));
        ctx.addSession(worksapceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testIsNew() throws Exception {
        // GIVEN
        String nodeName = "rootNode";
        String id = "propertyName";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);

        // WHEN
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        // THEN
        assertTrue(adapter.isNew());
    }

    @Test
    public void testGetItemPropertyReturnsModified() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyName";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        Property propertyInitial = adapter.getItemProperty(id);
        // New property --> null.
        assertEquals(true, propertyInitial == null);
        propertyInitial = DefaultPropertyUtil.newDefaultProperty(String.class, "test");
        adapter.addItemProperty(id, propertyInitial);

        propertyInitial.setValue("new");

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
    }

    @Test
    public void testApplyChangesUpdatesNode() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        Property notModifyProperty = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("notModify", notModifyProperty);

        Property notNotModifyRemovedProperty = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("notModifyRemoved", notNotModifyRemovedProperty);
        adapter.removeItemProperty("notModifyRemoved");

        Property notNewModifyProperty = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("modify", notNewModifyProperty);
        notNewModifyProperty.setValue("newModify");

        Property modifyRemovedProperty = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("modifyRemoved", modifyRemovedProperty);
        modifyRemovedProperty.setValue("newModifyRemoved");
        adapter.removeItemProperty("modifyRemoved");

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        assertNotNull(res);
        assertSame(res, parentNode.getNode(res.getName()));
        assertTrue(res.hasProperty("notModify"));
        assertFalse(res.hasProperty("notModifyRemoved"));
        assertTrue(res.hasProperty("modify"));
        assertEquals("newModify", res.getProperty("modify").getString());
        assertFalse(res.hasProperty("modifyRemoved"));
        assertEquals(nodeType, res.getPrimaryNodeType().getName());
    }

    @Test
    public void testApplyChangesTwice() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        Property propertyModified = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("id", propertyModified);
        Node node1 = adapter.applyChanges();

        // WHEN no duplicate call
        Node node2 = adapter.applyChanges();

        // THEN
        assertEquals(node1, node2);
    }

    @Test
    public void testBeforeAndAfterApplyChanges() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        Property propertyModified = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("id", propertyModified);

        Node nodeBefore = adapter.getJcrItem();

        // WHEN
        Node nodeAfter = adapter.applyChanges();

        // THEN
        assertEquals("We expect the getJcrItem() method returning the parent node before applying changes", parentNode, nodeBefore);
        assertEquals("We expect the getJcrItem() method returning the actual node after applying changes", nodeAfter, adapter.getJcrItem());
    }

    @Test
    public void testAddingPropertiesAfterApplyingChanges() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        Property propertyModified = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("id", propertyModified);

        Node nodeBefore = adapter.applyChanges();

        // WHEN
        Property propertyModifiedAfter = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("di", propertyModifiedAfter);

        Node nodeAfter = adapter.applyChanges();

        // THEN
        assertEquals("We expect the getJcrItem() method returning the actual node after applying changes", nodeAfter, adapter.getJcrItem());
        assertTrue("We expect the node have a property 'id'", nodeBefore.hasProperty("id"));
        assertTrue("We expect the node have a property 'id' after adding another one", nodeAfter.hasProperty("id"));
        assertTrue("We expect the node have a property 'di' after adding it", nodeAfter.hasProperty("di"));
    }

    @Test
    public void testRemovingPropertiesAfterApplyingChanges() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        Property propertyModified = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("id", propertyModified);

        Node nodeBefore = adapter.applyChanges();

        assertTrue("We expect the node to have a property 'id'", nodeBefore.hasProperty("id"));

        // WHEN
        adapter.removeItemProperty("id");

        Node nodeAfter = adapter.applyChanges();

        // THEN
        assertEquals("We expect the getJcrItem() method returning the actual node after applying changes", nodeAfter, adapter.getJcrItem());
        assertFalse("We expect the node not to have a property 'id' after removing it", nodeAfter.hasProperty("id"));
    }

    @Test
    public void testGettingPropertyAfterApplyingChanges() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String nodeType = "mgnl:content";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNewNodeAdapter adapter = new JcrNewNodeAdapter(parentNode, nodeType);

        assertNull(adapter.getItemProperty("id"));

        Property propertyModified = DefaultPropertyUtil.newDefaultProperty(String.class, "");
        adapter.addItemProperty("id", propertyModified);

        // WHEN
        adapter.applyChanges();

        // THEN
        // After applying changes the adapter should behave like a JcrNodeAdapter,
        // thus returning null for an non-existing property.
        assertNull("We expect to get null for non-existing properties", adapter.getItemProperty("di"));
        // This should simulate a call to super().super().getItemProperty()
        assertTrue(adapter.getItemProperty(ModelConstants.JCR_NAME) instanceof DefaultProperty);
        assertEquals("We expect to get jcrName of the node", "0", adapter.getItemProperty(ModelConstants.JCR_NAME).getValue());
    }


    @Test
    public void testReturnedPropertiesAreInSync() throws RepositoryException {

        // GIVEN
        Node parentNode = session.getRootNode().addNode("node");
        parentNode.setProperty("name", "");
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        Property itemProperty1 = adapter.getItemProperty("name");
        Property itemProperty2 = adapter.getItemProperty("name");

        // WHEN
        itemProperty1.setValue("changed");

        // THEN
        assertTrue(itemProperty2.getValue().equals("changed"));
    }

    @Test
    public void testReturnsPropertiesWithChangedValues() throws RepositoryException {

        Node parentNode = session.getRootNode().addNode("node");
        parentNode.setProperty("name", "");
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        Property itemProperty1 = adapter.getItemProperty("name");

        itemProperty1.setValue("changed");

        Property itemProperty2 = adapter.getItemProperty("name");

        assertTrue(itemProperty2.getValue().equals("changed"));
    }
}
