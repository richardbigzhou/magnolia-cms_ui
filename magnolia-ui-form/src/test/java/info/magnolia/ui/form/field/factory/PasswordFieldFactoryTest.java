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

import static org.junit.Assert.assertEquals;

import info.magnolia.ui.form.field.PasswordFields;
import info.magnolia.ui.form.field.definition.PasswordFieldDefinition;

import org.junit.Test;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.StaticFieldFactory}.
 */
public class PasswordFieldFactoryTest extends AbstractFieldFactoryTestCase<PasswordFieldDefinition> {

    private PasswordFieldFactory passwordFieldFactory;

    @Test
    public void testGetField() {
        // GIVEN
        definition.setVerification(false);
        passwordFieldFactory = new PasswordFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport);
        passwordFieldFactory.setComponentProvider(componentProvider);
        // WHEN
        Field field = passwordFieldFactory.createField();

        // THEN
        assertEquals(true, field instanceof PasswordFields);
    }

    @Test
    public void testVerificationPassword() {
        // GIVEN
        definition.setVerification(true);
        passwordFieldFactory = new PasswordFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport);
        passwordFieldFactory.setComponentProvider(componentProvider);
        // WHEN
        PasswordFields field = (PasswordFields) passwordFieldFactory.createField();

        // THEN
        assertEquals(true, field.getVerticalLayout().getComponent(0) instanceof PasswordField);
        assertEquals(true, field.getVerticalLayout().getComponent(1) instanceof Label);
        assertEquals("Please confirm", ((Label) field.getVerticalLayout().getComponent(1)).getValue().toString());
        assertEquals(true, field.getVerticalLayout().getComponent(2) instanceof PasswordField);
    }

    @Test
    public void testVerificationMatch() {
        // GIVEN
        definition.setVerification(true);
        passwordFieldFactory = new PasswordFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport);
        passwordFieldFactory.setComponentProvider(componentProvider);
        PasswordFields field = (PasswordFields) passwordFieldFactory.createField();
        ((PasswordField) field.getVerticalLayout().getComponent(0)).setValue("aa");
        ((PasswordField) field.getVerticalLayout().getComponent(2)).setValue("aa");

        // WHEN
        field.validate();

        // THEN
        assertEquals(true, field.isValid());

    }

    @Test(expected = InvalidValueException.class)
    public void testVerificationDoNotMatch() {
        // GIVEN
        definition.setVerification(true);
        passwordFieldFactory = new PasswordFieldFactory(definition, baseItem, uiContext, i18NAuthoringSupport);
        passwordFieldFactory.setComponentProvider(componentProvider);
        PasswordFields field = (PasswordFields) passwordFieldFactory.createField();
        ((PasswordField) field.getVerticalLayout().getComponent(0)).setValue("aa");
        ((PasswordField) field.getVerticalLayout().getComponent(2)).setValue("axa");

        // WHEN
        field.validate();

        // THEN
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        PasswordFieldDefinition fieldDefinition = new PasswordFieldDefinition();
        fieldDefinition = (PasswordFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);

        fieldDefinition.setVerificationErrorMessage("Password should match");
        fieldDefinition.setVerificationMessage("Please confirm");
        this.definition = fieldDefinition;
    }

}
