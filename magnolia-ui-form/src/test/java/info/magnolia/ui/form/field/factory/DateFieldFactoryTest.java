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
package info.magnolia.ui.form.field.factory;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.form.field.definition.DateFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Field;
import com.vaadin.ui.PopupDateField;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.DateFieldFactory}.
 */
public class DateFieldFactoryTest extends AbstractFieldFactoryTestCase<DateFieldDefinition> {

    private DateFieldFactory dialogDate;
    private final Context context = mock(Context.class);
    private final User user = mock(User.class);
    private final SimpleTranslator simpleTranslator = mock(SimpleTranslator.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(context.getUser()).thenReturn(user);
        when(context.getLocale()).thenReturn(Locale.ENGLISH);
    }

    @Test
    public void simpleDateFieldTest() throws Exception {
        // GIVEN
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.YEAR, 2012);
        baseNode.setProperty(propertyName, cal);
        baseItem = new JcrNodeAdapter(baseNode);
        dialogDate = new DateFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport, simpleTranslator, context);
        dialogDate.setComponentProvider(componentProvider);
        // WHEN
        Field field = dialogDate.createField();

        // THEN
        assertThat(field, instanceOf(PopupDateField.class));
        assertEquals("yyyy-MM-dd", ((PopupDateField) field).getDateFormat());
        SimpleDateFormat sdf = new SimpleDateFormat(((PopupDateField) field).getDateFormat());
        assertEquals("2012-03-02", sdf.format(field.getValue()));
    }

    @Test
    public void simpleDateFieldTest_ChangedValue() throws Exception {
        // GIVEN
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.YEAR, 2012);
        baseNode.setProperty(propertyName, cal);
        baseItem = new JcrNodeAdapter(baseNode);
        dialogDate = new DateFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport, simpleTranslator, context);
        dialogDate.setComponentProvider(componentProvider);
        Calendar calNew = Calendar.getInstance();
        calNew.set(Calendar.DAY_OF_MONTH, 20);
        calNew.set(Calendar.MONTH, 2);
        calNew.set(Calendar.YEAR, 2010);
        // WHEN
        Field field = dialogDate.createField();
        field.setValue(calNew.getTime());

        // THEN
        assertThat(field, instanceOf(PopupDateField.class));
        assertEquals("yyyy-MM-dd", ((PopupDateField) field).getDateFormat());
        SimpleDateFormat sdf = new SimpleDateFormat(((PopupDateField) field).getDateFormat());
        assertEquals("2010-03-20", sdf.format(field.getValue()));
    }

    @Test
    public void dateFieldTest_Time() throws Exception {
        // GIVEN
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 2);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.YEAR, 2012);
        cal.set(Calendar.HOUR_OF_DAY, 5);
        cal.set(Calendar.MINUTE, 55);
        definition.setTime(true);
        definition.setTimeFormat("HH:mm");
        baseNode.setProperty(propertyName, cal);
        baseItem = new JcrNodeAdapter(baseNode);
        dialogDate = new DateFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport, simpleTranslator, context);
        dialogDate.setComponentProvider(componentProvider);
        // WHEN
        Field field = dialogDate.createField();

        // THEN
        assertThat(field, instanceOf(PopupDateField.class));
        assertEquals("yyyy-MM-dd HH:mm", ((PopupDateField) field).getDateFormat());
        SimpleDateFormat sdf = new SimpleDateFormat(((PopupDateField) field).getDateFormat());
        assertEquals("2012-03-02 05:55", sdf.format(field.getValue()));
    }

    @Test
    public void timeZone() throws Exception {
        // GIVEN
        baseItem = new JcrNodeAdapter(baseNode);
        dialogDate = new DateFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport, new SimpleTranslator(null, null) {
            @Override
            public String translate(String key, Object... args) {
                return Arrays.asList(args).toString();
            }
        }, context);
        dialogDate.setComponentProvider(componentProvider);
        when(user.getProperty(MgnlUserManager.PROPERTY_TIMEZONE)).thenReturn(TimeZone.getDefault().getID());

        // WHEN
        Field field = dialogDate.createField();

        // THEN
        assertThat(field.getDescription(), containsString(TimeZone.getDefault().getDisplayName()));
        assertTrue(field instanceof PopupDateField);
        assertThat(((PopupDateField) field).getInputPrompt(), containsString(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT)));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        DateFieldDefinition fieldDefinition = new DateFieldDefinition();
        fieldDefinition = (DateFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);
        fieldDefinition.setType(null);
        fieldDefinition.setTime(false);
        fieldDefinition.setDateFormat("yyyy-MM-dd");
        fieldDefinition.setTimeFormat("HH:mm:ss");
        this.definition = fieldDefinition;
    }

}
