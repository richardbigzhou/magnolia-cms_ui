/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.form.field.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.converter.Converter;

/**
 * Tests for {@link StringToCalendarConverter}.
 */
public class StringToCalendarConverterTest {

    private Converter<String, Calendar> converter;
    private long timeMs = 1401093721628L;
    private Calendar model;
    private TimeZone defaultTimeZone;

    @Before
    public void setUp() throws Exception {
        converter = new StringToCalendarConverter();
        model = Calendar.getInstance();
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+2"));
    }

    @After
    public void tearDown() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    public void testCalendarToPresentationDe() throws Exception {
        // GIVEN
        model.setTimeInMillis(timeMs);

        // WHEN
        Locale locale = Locale.GERMAN;
        String presentation = converter.convertToPresentation(model, String.class, locale);

        // THEN
        assertThat(presentation, containsString("26.05.2014"));
        assertThat(presentation, containsString("10:42"));
    }

    @Test
    public void testCalendarToPresentationEn() throws Exception {
        // GIVEN
        model.setTimeInMillis(timeMs);
        // WHEN
        Locale locale = Locale.ENGLISH;
        String presentation = converter.convertToPresentation(model, String.class, locale);

        // THEN
        assertThat(presentation, containsString("May 26, 2014"));
        assertThat(presentation, containsString("10:42"));
        assertThat(presentation, containsString("AM"));
    }

    @Test
    public void testPresentationToCalendarEn() throws Exception {
        // GIVEN
        String presentation = "May 23, 2014 2:11:00 PM";

        // WHEN
        Locale locale = Locale.ENGLISH;
        Calendar model = converter.convertToModel(presentation, Calendar.class, locale);

        // THEN
        assertThat(model.get(Calendar.YEAR), is(2014));
        assertThat(model.get(Calendar.MONTH), is(4));
        assertThat(model.get(Calendar.DAY_OF_MONTH), is(23));
    }

    @Test
    public void testStringToCalendarDe() throws Exception {
        // GIVEN
        String presentation = "26.05.2014 10:42:01";

        // WHEN
        Locale locale = Locale.GERMAN;
        Calendar calendar = converter.convertToModel(presentation, Calendar.class, locale);

        // THEN
        assertThat(calendar.get(Calendar.YEAR), is(2014));
        assertThat(calendar.get(Calendar.MONTH), is(4));
        assertThat(calendar.get(Calendar.DAY_OF_MONTH), is(26));
    }

    @Test
    public void testCalendarToPresentationForNullValue() throws Exception {
        // GIVEN
        Locale locale = Locale.ENGLISH;

        // WHEN
        String presentation = converter.convertToPresentation(null, String.class, locale);

        // THEN
        assertThat(presentation, isEmptyOrNullString());
    }

    @Test
    public void testPresentationToCalendarForNullValue() throws Exception {
        // GIVEN
        String presentation = null;

        // WHEN
        Locale locale = Locale.ENGLISH;
        Calendar model = converter.convertToModel(presentation, Calendar.class, locale);

        // THEN
        assertThat(model, nullValue());
    }
}
