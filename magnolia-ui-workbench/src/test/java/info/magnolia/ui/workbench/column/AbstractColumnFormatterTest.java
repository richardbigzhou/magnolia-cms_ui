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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.jcr.decoration.ContentDecorator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.wrapper.HTMLEscapingContentDecorator;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;

import javax.jcr.Property;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

/**
 * Test for AbstractColumnFormatter.
 */
public class AbstractColumnFormatterTest {

    private MockNode node;
    private MockProperty property;
    private Table table;
    private Item item;
    private Object itemId;

    private AbstractColumnFormatter<ColumnDefinition> abstractColumnFormatter;
    private ColumnDefinition definition;

    private final String PROPERTY = "name";

    @Before
    public void setUp() {
        table = mock(Table.class);
        item = mock(Item.class);
        itemId = item;

        abstractColumnFormatter = new TestAbstractColumnFormatter(definition);
        definition = mock(ColumnDefinition.class);

        final String unEscapedHTML = "<A onmouseover=alert('XSS_by_Vishal_V_Sonar_&_Akash_Chavan')> XSS </A>";
        node = new MockNode(unEscapedHTML, NodeTypes.NodeData.NAME);
        property = new MockProperty(PROPERTY, unEscapedHTML, node);
    }

    @Test
    public void testGetJcrItem() {
        // GIVEN
        when(table.getItem("someItemId")).thenReturn(item);

        // WHEN
        javax.jcr.Item item = abstractColumnFormatter.getJcrItem(table, "someItemId");
        // THEN - don't fail w/ ClassCastException
        assertNull(item);
    }

    @Test
    public void testGetJcrItemIsEscaped() throws Exception {

        // GIVEN
        ContentDecorator decorator = new HTMLEscapingContentDecorator(false);
        JcrNodeAdapter nodeAdapter = mock(JcrNodeAdapter.class);

        when(nodeAdapter.getJcrItem()).thenReturn(node);
        when(nodeAdapter.isNode()).thenReturn(true);
        when(table.getItem(itemId)).thenReturn(nodeAdapter);

        // WHEN
        javax.jcr.Item value = abstractColumnFormatter.getJcrItem(table, itemId);

        // THEN
        assertThat(decorator.wrapNode(node).getName(), is(value.getName()));
    }

    @Test
    public void testGetJcrItemPropertyIsEscaped() throws Exception {

        // GIVEN
        ContentDecorator decorator = new HTMLEscapingContentDecorator(false);
        JcrItemAdapter nodeAdapter = mock(JcrItemAdapter.class);

        when(nodeAdapter.getJcrItem()).thenReturn(property);
        when(nodeAdapter.isNode()).thenReturn(false);
        when(table.getItem(itemId)).thenReturn(nodeAdapter);

        // WHEN
        javax.jcr.Item value = abstractColumnFormatter.getJcrItem(table, itemId);

        // THEN
        assertThat(decorator.wrapProperty(property).getString(), is(((Property) value).getString()));
    }

    private class TestAbstractColumnFormatter extends AbstractColumnFormatter<ColumnDefinition> {

        public TestAbstractColumnFormatter(ColumnDefinition definition) {
            super(definition);
        }

        @Override
        protected javax.jcr.Item getJcrItem(Table source, Object itemId) {
            return super.getJcrItem(source, itemId);
        }

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            return null;
        }
    }
}
