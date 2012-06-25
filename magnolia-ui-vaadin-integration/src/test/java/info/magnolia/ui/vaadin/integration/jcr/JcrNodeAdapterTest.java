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
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

public class JcrNodeAdapterTest {

    private String sessionName = "test";
    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(sessionName);
        MockContext ctx = new MockContext();
        ctx.addSession("test", session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }



    @Test
    public void testGetIdentifier() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode(new MockSession("test"));
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        final String result = item.getNodeIdentifier();

        // THEN
        assertEquals(underlyingNode.getIdentifier(), result);
    }

    @Test
    public void testAddItemProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        DefaultProperty property = new DefaultProperty(propertyName, propertyValue);
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        final boolean b = item.addItemProperty(propertyName,property);

        // THEN
        assertEquals(true, b);
        assertEquals(property.getValue().toString(), item.getItemProperty(propertyName).getValue().toString());
    }

    @Test
    public void testAddItemProperty_alreadyExist() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);

        DefaultProperty property = new DefaultProperty(propertyName, "newValue");
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        final boolean b = item.addItemProperty(propertyName,property);

        // THEN
        assertEquals(false, b);
        assertEquals(propertyValue, item.getItemProperty(propertyName).getValue().toString());
    }

    @Test
    public void testRemoveItemProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);
        assertEquals(true, underlyingNode.hasProperty(propertyName));

        // WHEN
        final boolean b = item.removeItemProperty(propertyName);

        // THEN
        assertEquals(true, b);
        assertEquals(false, underlyingNode.hasProperty(propertyName));
    }

    @Test
    public void testRemoveItemProperty_DoNotExist() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);
        assertEquals(false, underlyingNode.hasProperty(propertyName));

        // WHEN
        final boolean b = item.removeItemProperty(propertyName);

        // THEN
        assertEquals(false, b);
        assertEquals(false, underlyingNode.hasProperty(propertyName));
    }

    @Test
    public void testGetItemProperties() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        final Property prop = item.getItemProperty(propertyName);

        // THEN
        assertEquals(propertyValue, prop.getValue());
    }

    @Test
    public void testGetNode() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        final Node result = item.getNode();

        // THEN
        assertEquals(underlyingNode, result);
    }


    @Test
    public void testValueChangeEvent_PropertyExist() throws Exception {
        // GIVEN
        Node underlyingNode = session.getRootNode().addNode("underlying");
        String propertyName = "TEST";
        String propertyValue = "value";
        javax.jcr.Property jcrProperty = underlyingNode.setProperty(propertyName, propertyValue);
        JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        Property nodePorperty = item.getItemProperty(propertyName);
        nodePorperty.setValue("newValue");

        // THEN
        assertEquals("newValue", jcrProperty.getString());
    }


    @Test
    public void testValueChangeEvent_PropertyDoNotExist() throws Exception {
        // GIVEN
        Node underlyingNode = session.getRootNode().addNode("underlying");
        String propertyName = "TEST";
        JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        Property nodePorperty = item.getItemProperty(propertyName);
        nodePorperty.setValue("newValue");

        // THEN
        assertEquals("newValue", underlyingNode.getProperty(propertyName).getString());
    }



    @Test(expected=UnsupportedOperationException.class)
    public void testGetItemPropertyIds() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode(new MockSession("test"));
        final JcrNodeAdapter item = new JcrNodeAdapter(underlyingNode);

        // WHEN
        item.getItemPropertyIds();
    }

}
