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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

import com.vaadin.data.Property;

public class BaseItemTest {

    @Test
    public void testConstructionInCaseOfJCRTroubles() throws Exception {
        // GIVEN
        final Node underlyingNode = mock(Node.class);
        when(underlyingNode.getIdentifier()).thenThrow(new RepositoryException("SIMULATED PROBLEM!"));

        // WHEN
        final BaseItem item = new BaseItem(underlyingNode);

        // THEN
        assertEquals(BaseItem.UN_IDENTIFIED, item.getIdentifier());
    }

    @Test
    public void testGetIdentifier() throws Exception {
        // GIVEN
        final Node underlyingNode = mock(Node.class);
        final String jcrIdentifier = "JUST-A_TEST";
        when(underlyingNode.getIdentifier()).thenReturn(jcrIdentifier);
        final BaseItem item = new BaseItem(underlyingNode);

        // WHEN
        final String result = item.getIdentifier();

        // THEN
        assertEquals(jcrIdentifier, result);
    }

    @Test
    public void testAddItemProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode();
        final BaseItem item = new BaseItem(underlyingNode);
        final Property property = new BaseProperty("one", String.class);
        final String propertyId = "FIRST";

        // WHEN
        item.addItemProperty(propertyId, property);

        // THEN
        assertEquals(property, item.getItemProperty(propertyId));
    }

    @Test
    public void testRemoveItemProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode();
        final BaseItem item = new BaseItem(underlyingNode);
        final Property property = new BaseProperty("one", String.class);
        final String firstPropertyId = "FIRST";
        final String secondPropertyId = "SECOND";
        item.addItemProperty(firstPropertyId, property);
        item.addItemProperty(secondPropertyId, property);

        // WHEN
        item.removeItemProperty(firstPropertyId);

        // THEN
        assertNull(item.getItemProperty(firstPropertyId));
        assertEquals(property, item.getItemProperty(secondPropertyId));
    }

    @Test
    public void testGetItemPropertyIds() throws Exception {
        // GIVEN
        final Node underlyingNode = new MockNode();
        final BaseItem item = new BaseItem(underlyingNode);
        final Property property = new BaseProperty("one", String.class);
        final String propertyId = "FIRST";
        item.addItemProperty(propertyId, property);

        // WHEN
        final Collection<Object> result = item.getItemPropertyIds();

        // THEN
        assertTrue(result.contains(propertyId));
    }

}
