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
package info.magnolia.ui.workbench.column;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import info.magnolia.ui.workbench.column.definition.ColumnDefinition;

import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

/**
 * Test for AbstractColumnFormatter.
 */
public class AbstractColumnFormatterTest {

    @Test
    public void testGetJcrItem() {
        // GIVEN
        AbstractColumnFormatter<ColumnDefinition> dummy = new AbstractColumnFormatter<ColumnDefinition>(null) {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                return null;
            }
        };

        Table table = mock(Table.class);
        Item value = mock(Item.class);
        when(table.getItem("someItemId")).thenReturn(value);

        // WHEN
        javax.jcr.Item item = dummy.getJcrItem(table, "someItemId");
        // THEN - don't fail w/ ClassCastException
        assertNull(item);
    }

}
