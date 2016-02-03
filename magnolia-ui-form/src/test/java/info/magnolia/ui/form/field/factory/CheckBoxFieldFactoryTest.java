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

import static org.junit.Assert.*;

import info.magnolia.ui.form.field.CheckBoxField;
import info.magnolia.ui.form.field.definition.CheckboxFieldDefinition;

import org.junit.Test;

import com.vaadin.ui.Field;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.CheckBoxFieldFactory}.
 */
public class CheckBoxFieldFactoryTest extends AbstractFieldFactoryTestCase<CheckboxFieldDefinition> {

    private static final String CHECKBOX_FIELD_LABEL = "CheckBox Field";
    private static final String CHECKBOX_CAPTION = "Turn me on";

    private CheckBoxFieldFactory checkBoxField;

    @Test
    public void simpleCheckBoxFieldTest() throws Exception {
        // GIVEN
        checkBoxField = new CheckBoxFieldFactory(definition, baseItem);
        checkBoxField.setI18nContentSupport(i18nContentSupport);

        // WHEN
        Field<Boolean> field = checkBoxField.createField();

        // THEN
        assertTrue(field instanceof CheckBoxField);
        assertEquals(CHECKBOX_FIELD_LABEL, field.getCaption());
        assertEquals(CHECKBOX_CAPTION, ((CheckBoxField) field).iterator().next().getCaption());
    }

    @Test
    public void checkBoxField_SetSelectedTest() throws Exception {
        // GIVEN
        checkBoxField = new CheckBoxFieldFactory(definition, baseItem);
        checkBoxField.setI18nContentSupport(i18nContentSupport);
        definition.setDefaultValue("false");

        // WHEN
        Field<Boolean> field = checkBoxField.createField();

        // THEN
        assertEquals(true, field instanceof CheckBoxField);
        assertEquals(false, field.getValue());
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        CheckboxFieldDefinition fieldDefinition = new CheckboxFieldDefinition();
        fieldDefinition.setName(propertyName);
        fieldDefinition.setLabel(CHECKBOX_FIELD_LABEL);
        fieldDefinition.setButtonLabel(CHECKBOX_CAPTION);
        this.definition = fieldDefinition;
    }

}
