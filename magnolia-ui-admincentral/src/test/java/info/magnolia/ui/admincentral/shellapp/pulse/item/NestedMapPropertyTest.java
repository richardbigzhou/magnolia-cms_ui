/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.item;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

/**
 * Test for {@link NestedMapProperty}.
 */
public class NestedMapPropertyTest {

    private Pojo bean;
    private Date date = new Date();

    @Before
    public void setUp() throws Exception {
        bean = new Pojo();
        bean.setContent(
                new HashMap<String, Object>() {{
                    put("property1", "test1");
                    put("property2", 123l);
                    put("property3", 1234);
                    put("property4", date);
                }}
        );
    }

    @Test
    public void testGetPropertyValues() throws Exception {
        // GIVEN
        Property<String> property1 = new NestedMapProperty<String>(bean, "content.property1");
        Property<Long> property2 = new NestedMapProperty<Long>(bean, "content.property2");
        Property<Integer> property3 = new NestedMapProperty<Integer>(bean, "content.property3");
        Property<Date> property4 = new NestedMapProperty<Date>(bean, "content.property4");

        // WHEN
        String value1 = property1.getValue();
        long value2 = property2.getValue();
        int value3 = property3.getValue();
        Date value4 = property4.getValue();

        // THEN
        assertThat(value1, is("test1"));
        assertThat(value2, is(123l));
        assertThat(value3, is(1234));
        assertThat(value4, is(date));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyThrowsExceptionOnWrongNotation() throws Exception {
        // GIVEN
        Property<String> property1 = new NestedMapProperty<String>(bean, "content.bla.property1");

        // WHEN
        property1.getValue();

        // THEN
        // exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyThrowsExceptionOnWrongNotation1() throws Exception {
        // GIVEN
        Property<String> property1 = new NestedMapProperty<String>(bean, "content");

        // WHEN
        property1.getValue();

        // THEN
        // exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyThrowsExceptionOnWrongNotation2() throws Exception {
        // GIVEN
        Property<String> property1 = new NestedMapProperty<String>(bean, "content.property1.");

        // WHEN
        property1.getValue();

        // THEN
        // exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyThrowsExceptionWhenNoGetter() throws Exception {
        // GIVEN

        // WHEN
        Property<String> property = new NestedMapProperty<String>(bean, "data.property");

        // THEN
        // exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertyThrowsExceptionWhenFieldNotMap() throws Exception {
        // GIVEN

        // WHEN
        Property<String> property = new NestedMapProperty<String>(bean, "content.field");

        // THEN
        // exception
    }

    private class Pojo {

        Map<String, Object> content;

        String field;

        public Map<String, Object> getContent() {
            return content;
        }

        private String getField() {
            return field;
        }

        public void setContent(Map<String, Object> content) {
            this.content = content;
        }

        private void setField(String field) {
            this.field = field;
        }
    }
}
