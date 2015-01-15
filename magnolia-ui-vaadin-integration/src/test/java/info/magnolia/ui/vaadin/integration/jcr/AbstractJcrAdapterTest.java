/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of {@link AbstractJcrAdapter}.
 */
public class AbstractJcrAdapterTest {

    private final String workspaceName = "workspace";

    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testSetCommonAttributesWithNode() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);

        // WHEN
        DummyJcrAdapter adapter = new DummyJcrAdapter(testNode);

        // THEN
        assertTrue(adapter.isNode());
        assertEquals(workspaceName, adapter.getWorkspace());
        assertEquals(testNode.getIdentifier(), adapter.getItemId().getUuid());
        assertEquals(testNode.getPath(), adapter.getJcrItem().getPath());
    }

    @Test
    public void testSetCommonAttributesWithProperty() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        Property testProperty = testNode.setProperty(propertyName, propertyValue);

        // WHEN
        DummyJcrAdapter adapter = new DummyJcrAdapter(testProperty);

        // THEN
        assertFalse(adapter.isNode());
        assertEquals(workspaceName, adapter.getWorkspace());
        assertEquals(JcrItemUtil.getItemId(testProperty), adapter.getItemId());
        assertEquals(testProperty.getPath(), adapter.getJcrItem().getPath());
    }

    @Test
    public void testGetJcrItemWithNodeExisting() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);

        // WHEN
        DummyJcrAdapter adapter = new DummyJcrAdapter(testNode);

        // THEN
        assertNotNull(adapter.getJcrItem());
    }

    @Test
    public void testGetJcrItemWithNodeNotExisting() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);
        testNode.remove();
        // WHEN
        DummyJcrAdapter adapter = new DummyJcrAdapter(testNode);

        // THEN
        assertNull(adapter.getJcrItem());
    }

    @Test
    public void testGetJcrItemWithPropertyExisting() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        Property testProperty = testNode.setProperty(propertyName, propertyValue);

        // WHEN
        DummyJcrAdapter adapter = new DummyJcrAdapter(testProperty);

        // THEN
        assertNotNull(adapter.getJcrItem());
    }

    @Test
    public void testGetJcrItemWithPropertyNotExisting() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);
        String propertyName = "propertyName";
        String propertyValue = "propertyValue";
        Property testProperty = testNode.setProperty(propertyName, propertyValue);
        testNode.remove();

        // WHEN
        DummyJcrAdapter adapter = new DummyJcrAdapter(testProperty);

        // THEN
        assertNull(adapter.getJcrItem());
    }

    /**
     * Dummy implementation of the Abstract class.
     */
    public class DummyJcrAdapter extends AbstractJcrAdapter {

        public DummyJcrAdapter(Item jcrItem) {
            super(jcrItem);
        }

        @Override
        public boolean isNode() {
            return !(getItemId() instanceof JcrPropertyItemId);
        }

        @Override
        public com.vaadin.data.Property getItemProperty(Object id) {
            return null;
        }

        @Override
        public Collection<?> getItemPropertyIds() {
            return null;
        }

        @Override
        public boolean addItemProperty(Object id, com.vaadin.data.Property property) throws UnsupportedOperationException {
            return false;
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            return false;
        }

        @Override
        protected void updateProperty(Item item, String propertyId, com.vaadin.data.Property property) {
        }

        @Override
        public Item applyChanges() throws RepositoryException {
            return null;
        }

        @Override
        public boolean isNew() {
            return false;
        }
    }

}
