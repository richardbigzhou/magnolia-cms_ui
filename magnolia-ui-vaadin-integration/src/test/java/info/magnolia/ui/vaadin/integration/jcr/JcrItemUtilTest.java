/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.Test;

/**
 * Tests.
 */
public class JcrItemUtilTest {

    @Test
    public void testGetNodeIdentifierFrom() throws Exception {
        // GIVEN
        final String nodeUuid = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2";
        final String propertyId = nodeUuid + "@" + "name";

        // WHEN
        final String result = JcrItemUtil.parseNodeIdentifier(propertyId);

        // THEN
        assertThat(result, equalTo(nodeUuid));
    }

    @Test
    public void testGetPropertyName() throws Exception {
        // GIVEN
        final String propertyName = "specialProp";
        final String propertyId = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2" + "@" + propertyName;

        // WHEN
        final String result = JcrItemUtil.parsePropertyName(propertyId);

        // THEN
        assertThat(result, equalTo(propertyName));
    }

    @Test
    public void testIsPropertyIdWithPropertyId() throws Exception {
        // GIVEN
        final String propertyId = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2" + "@" + "specialProp";

        // WHEN
        final boolean result = JcrItemUtil.isPropertyItemId(propertyId);

        // THEN
        assertTrue("Result should be considered to be a propertyId", result);
    }

    @Test
    public void testIsPropertyIdWithNodeId() throws Exception {
        // GIVEN
        final String nodeId = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2";

        // WHEN
        final boolean result = JcrItemUtil.isPropertyItemId(nodeId);

        // THEN
        assertFalse("Result should not be considered to be a propertyId", result);
    }

    @Test
    public void testGetItemPathForNode() throws Exception {
        // GIVEN
        MockUtil.initMockContext();
        MockSession session = new MockSession("test");
        MockUtil.setSessionAndHierarchyManager(session);
        Node rootNode = session.getRootNode().addNode("new");


        // WHEN
        String res = JcrItemUtil.getItemPath(rootNode);

        // THEN
        assertEquals("/new", res);
    }

    @Test
    public void testGetItemPathForProperty() throws Exception {
        // GIVEN
        MockUtil.initMockContext();
        MockSession session = new MockSession("test");
        MockUtil.setSessionAndHierarchyManager(session);
        Node rootNode = session.getRootNode().addNode("new");
        Property property = rootNode.setProperty("someProperty", "propertyValue");

        // WHEN
        String res = JcrItemUtil.getItemPath(property);

        // THEN
        assertEquals("/new@someProperty", res);
    }

    @Test
    public void testGetItemIdWithProperty() throws Exception {
        // Given
        final String nodeUuid = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2";
        final MockNode node = new MockNode();
        node.setIdentifier(nodeUuid);
        final String propertyName = "name";
        final Property property = new MockProperty(propertyName, "theName", node);

        // WHEN
        final String result = JcrItemUtil.getItemId(property);

        // THEN
        assertThat(result, equalTo(nodeUuid + JcrItemUtil.PROPERTY_NAME_AND_IDENTIFIER_SEPARATOR + propertyName));
    }

    @Test
    public void testGetItemIdWithNode() throws Exception {
        // Given
        final String nodeUuid = "ccb8ae64-3ad2-4ffd-93ce-367926f3bcd2";
        final MockNode node = new MockNode();
        node.setIdentifier(nodeUuid);

        // WHEN
        final String result = JcrItemUtil.getItemId(node);

        // THEN
        assertThat(result, equalTo(nodeUuid));
    }

    @Test
    public void testGetJrcItems() throws Exception {
        // GIVEN
        MockUtil.initMockContext();
        MockSession session = new MockSession("test");
        MockUtil.setSessionAndHierarchyManager(session);
        Node rootNode = session.getRootNode();

        Node first = NodeUtil.createPath(rootNode, "first", NodeTypes.Content.NAME);
        String firstNodeId = JcrItemUtil.getItemId(first);
        PropertyUtil.setProperty(first, "prop1", "value1");
        Property prop1 = PropertyUtil.getProperty(first, "prop1");
        String propertyId = JcrItemUtil.getItemId(prop1);
        Node second = NodeUtil.createPath(rootNode, "second", NodeTypes.Content.NAME);
        String secondNodeId = JcrItemUtil.getItemId(second);
        String brokenId = "foo";

        String[] ids = { firstNodeId, secondNodeId, propertyId, brokenId };

        // WHEN
        List<Item> items = JcrItemUtil.getJcrItems("test", Arrays.asList(ids));

        // THEN
        assertEquals(3, items.size());
        assertTrue(items.contains(first));
        assertTrue(items.contains(second));
        assertTrue(items.contains(prop1));
    }
}
