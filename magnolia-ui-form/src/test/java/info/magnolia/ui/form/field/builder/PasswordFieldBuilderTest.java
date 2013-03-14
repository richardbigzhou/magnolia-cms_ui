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
package info.magnolia.ui.form.field.builder;

import static org.junit.Assert.assertEquals;

import info.magnolia.ui.form.field.PasswordFields;
import info.magnolia.ui.form.field.definition.PasswordFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.builder.StaticFieldBuilder}.
 */
public class PasswordFieldBuilderTest extends AbstractBuilderTest<PasswordFieldDefinition> {

    private PasswordFieldBuilder passwordFieldBuilder;

    @Test
    public void simplePasswordFieldBuilderTest() {
        // GIVEN
        definition.setVerification(false);
        passwordFieldBuilder = new PasswordFieldBuilder(definition, baseItem);
        passwordFieldBuilder.setI18nContentSupport(i18nContentSupport);
        // WHEN
        Field field = passwordFieldBuilder.getField();

        // THEN
        assertEquals(true, field instanceof PasswordFields);
    }

    @Test
    public void verificationPasswordFieldBuilderTest() {
        // GIVEN
        definition.setVerification(true);
        passwordFieldBuilder = new PasswordFieldBuilder(definition, baseItem);
        passwordFieldBuilder.setI18nContentSupport(i18nContentSupport);
        // WHEN
        PasswordFields field = (PasswordFields) passwordFieldBuilder.getField();

        // THEN
        assertEquals(true, field.getVerticalLayout().getComponent(0) instanceof PasswordField);
        assertEquals(true, field.getVerticalLayout().getComponent(1) instanceof Label);
        assertEquals("Please confirm", ((Label) field.getVerticalLayout().getComponent(1)).getValue().toString());
        assertEquals(true, field.getVerticalLayout().getComponent(2) instanceof PasswordField);
    }

    @Test
    public void verificationMatchTest() {
        // GIVEN
        definition.setVerification(true);
        passwordFieldBuilder = new PasswordFieldBuilder(definition, baseItem);
        passwordFieldBuilder.setI18nContentSupport(i18nContentSupport);
        PasswordFields field = (PasswordFields) passwordFieldBuilder.getField();
        ((PasswordField) field.getVerticalLayout().getComponent(0)).setValue("aa");
        ((PasswordField) field.getVerticalLayout().getComponent(2)).setValue("aa");

        // WHEN
        field.validate();

        // THEN
        assertEquals(true, field.isValid());

    }

    @Test(expected = InvalidValueException.class)
    public void verificationDoNotMatchTest() {
        // GIVEN
        definition.setVerification(true);
        passwordFieldBuilder = new PasswordFieldBuilder(definition, baseItem);
        passwordFieldBuilder.setI18nContentSupport(i18nContentSupport);
        PasswordFields field = (PasswordFields) passwordFieldBuilder.getField();
        ((PasswordField) field.getVerticalLayout().getComponent(0)).setValue("aa");
        ((PasswordField) field.getVerticalLayout().getComponent(2)).setValue("axa");

        // WHEN
        field.validate();

        // THEN
    }

    @Test
    public void encodingTest() throws RepositoryException {
        // GIVEN
        definition.setVerification(false);
        passwordFieldBuilder = new PasswordFieldBuilder(definition, baseItem);
        passwordFieldBuilder.setI18nContentSupport(i18nContentSupport);
        Field field = passwordFieldBuilder.getField();
        field.setValue("awdYxe?m,483*");
        field.validate();

        // WHEN
        Node res = ((JcrNodeAdapter) baseItem).getNode();

        // THEN
        assertEquals("awdYxe?m,483*", new String(Base64.decodeBase64(res.getProperty(propertyName).getString())));
    }

    @Test
    public void doNotEncodeTest() throws RepositoryException {
        // GIVEN
        definition.setVerification(false);
        definition.setEncode(false);
        passwordFieldBuilder = new PasswordFieldBuilder(definition, baseItem);
        passwordFieldBuilder.setI18nContentSupport(i18nContentSupport);
        Field field = passwordFieldBuilder.getField();
        field.setValue("awdYxe?m,483*");
        field.validate();

        // WHEN
        Node res = ((JcrNodeAdapter) baseItem).getNode();

        // THEN
        assertEquals("awdYxe?m,483*", (res.getProperty(propertyName).getString()));
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        PasswordFieldDefinition fieldDefinition = new PasswordFieldDefinition();
        fieldDefinition = (PasswordFieldDefinition) AbstractFieldBuilderTest.createConfiguredFieldDefinition(fieldDefinition, propertyName);

        fieldDefinition.setVerificationErrorMessage("Password should match");
        fieldDefinition.setVerificationMessage("Please confirm");
        this.definition = fieldDefinition;
    }

}
