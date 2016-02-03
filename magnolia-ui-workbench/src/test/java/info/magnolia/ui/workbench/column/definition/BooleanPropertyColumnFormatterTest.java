/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.workbench.column.definition;

import static info.magnolia.ui.workbench.column.definition.BooleanPropertyColumnDefinition.DisplayMode.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.ui.workbench.column.definition.BooleanPropertyColumnDefinition.BooleanPropertyColumnFormatter;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Table;

public class BooleanPropertyColumnFormatterTest {

    private BooleanPropertyColumnDefinition definition;
    private BooleanPropertyColumnFormatter formatter;
    private Table table;

    @Before
    public void setUp() throws Exception {
        definition = new BooleanPropertyColumnDefinition();
        formatter = new BooleanPropertyColumnFormatter(definition);
        table = mock(Table.class);
        doAnswer(new Answer<Property<?>>() {
            @Override
            public Property<?> answer(InvocationOnMock invocation) throws Throwable {
                FooItem foo = invocation.getArgumentAt(0, FooItem.class);
                Object value = foo.getPropertyValue();
                return value != null ? new ObjectProperty<>(value) : null;
            }
        }).when(table).getContainerProperty(any(FooItem.class), any());
    }

    @Test
    public void generateCellWithDefaults() throws Exception {
        assertThat(formatter.generateCell(table, itemId(true), null), isIcon("icon-tick"));
        assertNull(formatter.generateCell(table, itemId(false), null));
        assertNull(formatter.generateCell(table, itemId(null), null));
    }

    @Test
    public void generateCellWithDefaultsFromStringProperty() throws Exception {
        assertThat(formatter.generateCell(table, itemId("true"), null), isIcon("icon-tick"));
        assertNull(formatter.generateCell(table, itemId("false"), null));
        assertNull(formatter.generateCell(table, itemId("nada"), null));
    }

    @Test
    public void generateCellBasedOnDefinition() throws Exception {
        // GIVEN only icons and icon mode
        definition.setDisplayMode(ICON_ONLY);
        definition.setTrueIcon("icon-true");
        definition.setFalseIcon("icon-false");
        assertThat(formatter.generateCell(table, itemId(true), null), isIcon("icon-true"));
        assertThat(formatter.generateCell(table, itemId(false), null), isIcon("icon-false"));

        // GIVEN texts (and still icons) but only text mode
        definition.setDisplayMode(TEXT_ONLY);
        definition.setTrueLabel("VRAI");
        definition.setFalseLabel("FAUX");
        assertThat(formatter.generateCell(table, itemId(true), null), isText("VRAI"));
        assertThat(formatter.generateCell(table, itemId(false), null), isText("FAUX"));

        // GIVEN icon and text mode
        definition.setDisplayMode(ICON_AND_TEXT);
        assertThat(formatter.generateCell(table, itemId(true), null), isIconAndText("icon-true", "VRAI"));
        assertThat(formatter.generateCell(table, itemId(false), null), isIconAndText("icon-false", "FAUX"));

        // GIVEN still icon and text mode, but with mixed icon or text depending on value
        definition.setFalseIcon(null);
        definition.setTrueLabel(null);
        assertThat(formatter.generateCell(table, itemId(true), null), isIcon("icon-true"));
        assertThat(formatter.generateCell(table, itemId(false), null), isText("FAUX"));
    }

    @Test
    public void generateCellDoesntReturnEmptyStringButNull() throws Exception {
        // WHEN object property is null
        assertNull(formatter.generateCell(table, itemId(null), null));
        // WHEN definition has e.g. no falseIcon configured and object property is false
        assertNull(formatter.generateCell(table, itemId(false), null));
        // WHEN definition configures empty or blank string
        definition.setTrueIcon(" ");
        definition.setFalseIcon("");
        assertNull(formatter.generateCell(table, itemId(true), null));
        assertNull(formatter.generateCell(table, itemId(false), null));
        // WHEN definition has e.g. no falseIcon nor falseLabel configured and object property is false
        definition.setDisplayMode(ICON_AND_TEXT);
        assertNull(formatter.generateCell(table, itemId(false), null));
    }

    @Test
    public void generateCellEscapesHtml() throws Exception {
        // GIVEN silly xss strategies
        definition.setDisplayMode(ICON_AND_TEXT);
        definition.setTrueIcon("\"><script>...</script>");
        definition.setTrueLabel("<script>rock & roll</script>");
        definition.setFalseIcon("\\\" onmouseup=\"alert('XSS')\">");
        definition.setFalseLabel("</span>");
        assertThat(formatter.generateCell(table, itemId(true), null), isIconAndText("&quot;&gt;&lt;script&gt;...&lt;/script&gt;", "&lt;script&gt;rock &amp; roll&lt;/script&gt;"));
        assertThat(formatter.generateCell(table, itemId(false), null), isIconAndText("\\&quot; onmouseup=&quot;alert('XSS')&quot;&gt;", "&lt;/span&gt;"));
    }

    /**
     * Keep tests readable and realistic, while at the same time being convenient for test cases.
     */
    private static Object itemId(Object withUnderlyingPropertyValue) {
        return new FooItem(withUnderlyingPropertyValue);
    }

    private static class FooItem {

        private final Object propertyValue;

        public FooItem(Object propertyValue) {
            this.propertyValue = propertyValue;
        }

        public Object getPropertyValue() {
            return propertyValue;
        }
    }

    private static Matcher<String> isIcon(String iconClass) {
        return Matchers.equalTo(String.format("<span class=\"%s\"></span>", iconClass));
    }

    private static Matcher<String> isText(String text) {
        return Matchers.equalTo(String.format("<span>%s</span>", text));
    }

    private static Matcher<String> isIconAndText(String iconClass, String text) {
        return Matchers.equalTo(String.format("<span class=\"%s\"></span><span>%s</span>", iconClass, text));
    }

}
