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
package info.magnolia.ui.admincentral.dialog.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import info.magnolia.ui.model.dialog.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.dialog.definition.EmailValidatorDefinition;
import info.magnolia.ui.model.dialog.definition.FieldDefinition;
import info.magnolia.ui.model.dialog.definition.RegexpValidatorDefinition;

import org.junit.Test;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

public class FieldBuilderTest {

    @Test
    public void testBuildingTextField() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType(FieldDefinition.TEXT_FIELD_TYPE);

        // WHEN
        final Field result = FieldBuilder.build(def);

        // THEN
        assertEquals(TextField.class, result.getClass());
        assertEquals(FieldBuilder.TEXTFIELD_STYLE_NAME, result.getStyleName());
    }

    @Test
    public void testBuildingCheckBox() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType(FieldDefinition.CHECKBOX_FIELD_TYPE);

        // WHEN
        final Field result = FieldBuilder.build(def);

        // THEN
        assertEquals(CheckBox.class, result.getClass());
        assertEquals(FieldBuilder.TEXTFIELD_STYLE_NAME, result.getStyleName());
    }

    @Test
    public void testBuildingRequiredField() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();
        def.setRequired(true);
        def.setType(FieldDefinition.TEXT_FIELD_TYPE);

        // WHEN
        final Field result = FieldBuilder.build(def);

        // THEN
        assertTrue(result.isRequired());
        assertEquals(FieldBuilder.REQUIRED_ERROR, result.getRequiredError());
    }

    @Test
    public void testBuildingNullField() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();
        def.setType("<unkown>");

        // WHEN
        final Field result = FieldBuilder.build(def);

        // THEN
        assertNull(result);
    }

    @Test
    public void testAddValidatorsWithNotValidatorConfigured() {
        // GIVEN
        final FieldDefinition def = new ConfiguredFieldDefinition();

        final Field field = new TextField();

        // WHEN
        FieldBuilder.addValidators(def, field);

        // THEN
        assertNull(field.getValidators());
    }

    @Test
    public void testAddValidatorsWithRegExpValidatorDef() {
        // GIVEN
        final String errorMessage = "Must be diggets - from 4 to 5...";
        final FieldDefinition def = new ConfiguredFieldDefinition();
        final RegexpValidatorDefinition validatorDef = new RegexpValidatorDefinition();
        validatorDef.setErrorMessage(errorMessage);
        validatorDef.setPattern("^\\d{4,5}$");
        def.addValidator(validatorDef);
        final Field field = new TextField();

        // WHEN
        FieldBuilder.addValidators(def, field);

        // THEN
        assertEquals(1,field.getValidators().size());
        final RegexpValidator validator = (RegexpValidator) field.getValidators().iterator().next();
        // check error message gets transferred - can't check for pattern as RegexpValidator offeres no easy way to acces it...
        assertEquals(errorMessage, validator.getErrorMessage());
    }

    @Test
    public void testAddValidatorsWithEmailValidatorDef() {
        // GIVEN
        final String errorMessage = "Incorrect format for an email adress!";
        final FieldDefinition def = new ConfiguredFieldDefinition();
        final EmailValidatorDefinition validatorDef = new EmailValidatorDefinition();
        validatorDef.setErrorMessage(errorMessage);
        def.addValidator(validatorDef);
        final Field field = new TextField();

        // WHEN
        FieldBuilder.addValidators(def, field);

        // THEN
        assertEquals(1,field.getValidators().size());
        final RegexpValidator validator = (RegexpValidator) field.getValidators().iterator().next();
        assertEquals(errorMessage, validator.getErrorMessage());
    }
}
