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
package info.magnolia.ui.vaadin.integration.jcr;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.PropertyType;

import org.junit.Test;

/**
 * Test of {@link DefaultPropertyUtil}.
 */
public class DefaultPropertyUtilTest {

    @Test
    public void testCreateTypedValueForDate() throws Exception {
        // GIVEN
        final String value = "1970-07-04";

        // WHEN
        Date result = (Date) DefaultPropertyUtil.createTypedValue(PropertyType.TYPENAME_DATE, value);

        // THEN
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(1970, cal.get(Calendar.YEAR));
        assertEquals(6, cal.get(Calendar.MONTH));
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testCreateTypedValueForList() throws Exception {
        // GIVEN
        final String value = "a,b,c";

        // WHEN
        List<String> result = (List<String>) DefaultPropertyUtil.createTypedValue(List.class, value);

        // THEN
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
    }

    @Test
    public void testCreateTypedValueForDateWithoutDefault() throws Exception {
        // WHEN
        Date result = (Date) DefaultPropertyUtil.createTypedValue(PropertyType.TYPENAME_DATE, null);

        // THEN
        assertNull(result);
    }

    @Test
    public void createDefaultPropertyByPropertyType() {
        //WHEN
        DefaultProperty property = DefaultPropertyUtil.newDefaultProperty(PropertyType.LONG, 123l);

        //THEN
        assertEquals(123l, property.getValue());
        assertEquals(Long.class, property.getType());
    }

    @Test
    public void createDefaultPropertyByPropertyTypeWithNullValue() {
        //WHEN
        DefaultProperty property = DefaultPropertyUtil.newDefaultProperty(PropertyType.LONG, null);

        //THEN
        assertEquals(null, property.getValue());
        assertEquals(Long.class, property.getType());
    }

    @Test
    public void testGetFieldTypeClass() {
        assertEquals(String.class, DefaultPropertyUtil.getFieldTypeClass(""));
        assertEquals(String.class, DefaultPropertyUtil.getFieldTypeClass(null));
        assertEquals(String.class, DefaultPropertyUtil.getFieldTypeClass(PropertyType.TYPENAME_STRING));
        assertEquals(Long.class, DefaultPropertyUtil.getFieldTypeClass(PropertyType.TYPENAME_LONG));
        assertEquals(Boolean.class, DefaultPropertyUtil.getFieldTypeClass(PropertyType.TYPENAME_BOOLEAN));
        assertEquals(Date.class, DefaultPropertyUtil.getFieldTypeClass(PropertyType.TYPENAME_DATE));
        assertEquals(BigDecimal.class, DefaultPropertyUtil.getFieldTypeClass(PropertyType.TYPENAME_DECIMAL));
        assertEquals(Double.class, DefaultPropertyUtil.getFieldTypeClass(PropertyType.TYPENAME_DOUBLE));
        assertEquals(Binary.class, DefaultPropertyUtil.getFieldTypeClass(PropertyType.TYPENAME_BINARY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFieldTypeClassForNotSupportedTypeBinary() {
        DefaultPropertyUtil.getFieldTypeClass("SOME_RANDOM_STRING");
    }

}
