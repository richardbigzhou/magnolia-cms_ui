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
package info.magnolia.ui.workbench.container;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Tests.
 */
public class AbstractContainerTest {

    private ContainerTestImpl container;

    @Before
    public void setUp() throws Exception {
        container = new ContainerTestImpl();
    }

    @Test
    public void testAddContainerProperty() throws Exception {
        // GIVEN
        final String id = "id";
        assertFalse(container.getContainerPropertyIds().contains(id));

        // WHEN
        container.addContainerProperty(id, String.class, "STRING");

        // THEN
        assertTrue(container.getContainerPropertyIds().contains(id));
    }

    @Test
    public void testRemoveContainerProperty() throws Exception {
        // GIVEN
        final String id = "id";
        container.addContainerProperty(id, String.class, "STRING");
        assertTrue(container.getContainerPropertyIds().contains(id));

        // WHEN
        container.removeContainerProperty(id);

        // THEN
        assertFalse(container.getContainerPropertyIds().contains(id));
    }

    @Test
    public void testGetType() throws Exception {
        // GIVEN
        final String id = "id";

        // WHEN
        container.addContainerProperty(id, String.class, "STRING");

        // THEN
        assertEquals(String.class, container.getType(id));
    }

    public class ContainerTestImpl extends AbstractContainer {
        @Override
        public Item getItem(Object itemId) {
            return null;
        }

        @Override
        public Collection<?> getItemIds() {
            return null;
        }

        @Override
        public Property getContainerProperty(Object itemId, Object propertyId) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean containsId(Object itemId) {
            return false;
        }

        @Override
        public Item addItem(Object itemId) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("TestImpl doesn't support this operation.");
        }

        @Override
        public Object addItem() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("TestImpl doesn't support this operation.");
        }

        @Override
        public boolean removeItem(Object itemId) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("TestImpl doesn't support this operation.");
        }

        @Override
        public boolean removeAllItems() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("TestImpl doesn't support this operation.");
        }
    }
}
