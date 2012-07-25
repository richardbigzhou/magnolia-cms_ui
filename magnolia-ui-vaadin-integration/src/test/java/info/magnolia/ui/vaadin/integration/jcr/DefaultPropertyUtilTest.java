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

import org.junit.Test;

import javax.jcr.PropertyType;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

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
    public void testCreateTypedValueForDateWithoutDefault() throws Exception {
        // WHEN
        Date result = (Date) DefaultPropertyUtil.createTypedValue(PropertyType.TYPENAME_DATE, null);

        // THEN
        Calendar today = Calendar.getInstance();
        
        Calendar resultsCalendar = Calendar.getInstance();
        resultsCalendar.setTime(result);
        
        assertEquals(today.get(Calendar.YEAR), resultsCalendar.get(Calendar.YEAR));
        assertEquals(today.get(Calendar.MONTH), resultsCalendar.get(Calendar.MONTH));
        assertEquals(today.get(Calendar.DAY_OF_MONTH), resultsCalendar.get(Calendar.DAY_OF_MONTH));
    }

}
