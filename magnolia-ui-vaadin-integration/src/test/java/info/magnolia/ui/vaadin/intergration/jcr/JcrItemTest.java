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
package info.magnolia.ui.vaadin.intergration.jcr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

import com.vaadin.data.Property;

public class JcrItemTest {

    @Test
    public void testConstructionInCaseOfJCRTroubles() throws Exception {
        // GIVEN
        final Node underlyingNode = mock(Node.class);
        when(underlyingNode.getIdentifier()).thenThrow(new RepositoryException("SIMULATED PROBLEM!"));

        // WHEN
        final JcrItem item = new JcrItem(underlyingNode);

        // THEN
        assertEquals(JcrItem.UN_IDENTIFIED, item.getIdentifier());
    }

    @Test
    public void testGetIdentifier() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode(new MockSession("test"));
        final JcrItem item = new JcrItem(underlyingNode);

        // WHEN
        final String result = item.getIdentifier();

        // THEN
        assertEquals(underlyingNode.getIdentifier(), result);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testAddItemProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode(new MockSession("test"));
        final JcrItem item = new JcrItem(underlyingNode);

        // WHEN
        item.addItemProperty("a", new BaseProperty("b"));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRemoveItemProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode(new MockSession("test"));
        final JcrItem item = new JcrItem(underlyingNode);

        // WHEN
        item.removeItemProperty("a");
    }

    @Test
    public void testGetItemProperties() throws Exception {
        // GIVEN
        final MockSession session = new MockSession("test");
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final MockContext ctx = new MockContext();
        ctx.addSession("test", session);
        MgnlContext.setInstance(ctx);
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrItem item = new JcrItem(underlyingNode);

        // WHEN
        final Property prop = item.getItemProperty(propertyName);

        // THEN
        assertEquals(propertyValue, prop.getValue());
    }

    @Test
    public void testGetNode() throws Exception {
        // GIVEN
        final MockSession session = new MockSession("test");
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final MockContext ctx = new MockContext();
        ctx.addSession("test", session);
        MgnlContext.setInstance(ctx);
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final JcrItem item = new JcrItem(underlyingNode);

        // WHEN
        final Node result = item.getNode();

        // THEN
        assertEquals(underlyingNode, result);
    }


    @Test(expected=UnsupportedOperationException.class)
    public void testGetItemPropertyIds() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode(new MockSession("test"));
        final JcrItem item = new JcrItem(underlyingNode);

        // WHEN
        item.getItemPropertyIds();
    }

}
