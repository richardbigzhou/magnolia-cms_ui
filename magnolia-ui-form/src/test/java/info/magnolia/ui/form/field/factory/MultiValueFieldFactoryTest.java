/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import static org.mockito.Mockito.mock;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.ui.form.field.MultiField;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.fieldType.registry.FieldTypeDefinitionRegistryTest.TestFieldTypeDefinitionProvider;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class MultiValueFieldFactoryTest extends AbstractFieldFactoryTestCase<MultiValueFieldDefinition> {

    private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator("Not a valid email");
    private MultiValueFieldFactory<MultiValueFieldDefinition> factory;
    private FieldFactoryFactory subfieldFactory;
    private MultiField multiField;
    private TextFieldDefinition textFieldDefinition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        FieldTypeDefinitionRegistry fieldDefinitionRegistry = createFieldTypeRegistry();
        subfieldFactory = new FieldFactoryFactory(componentProvider, fieldDefinitionRegistry, null);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        multiField = null;
        factory = null;
    }

    @Test
    public void createFieldComponentTest() throws Exception {
        // GIVEN
        factory = new MultiValueFieldFactory<>(definition, baseItem, null, i18NAuthoringSupport, subfieldFactory, componentProvider);
        factory.setComponentProvider(componentProvider);

        // WHEN
        Field<PropertysetItem> field = factory.createField();

        // THEN
        assertThat(field, instanceOf(MultiField.class));
    }

    @Test
    public void validationFailsIfRequiredMultiFieldIsEmpty() throws Exception {
        // GIVEN
        definition.setRequired(true);

        createField();

        // WHEN
        boolean isValid = multiField.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationFailsIfRequiredSubFieldIsEmpty() throws Exception {
        // GIVEN
        definition.setRequired(true);

        createField();

        // add new mandatory text field
        TextField textField = new TextField();
        textField.setValue("");
        addSubField(textField);

        // WHEN
        boolean isValid = multiField.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationSuccedsIfRequiredSubFieldIsNotEmpty() throws Exception {
        // GIVEN
        definition.setRequired(true);

        createField();

        // add new mandatory text field
        TextField textField = new TextField();
        textField.setValue("foo");
        addSubField(textField);

        // WHEN
        boolean isValid = multiField.isValid();

        // THEN
        assertTrue(isValid);
    }

    @Test
    public void validationSuccedsIfNotRequiredSubFieldIsEmpty() throws Exception {
        // GIVEN
        createField();

        // add new non mandatory text field
        TextField textField = new TextField();
        textField.setValue("");
        addSubField(textField);

        // WHEN
        boolean isValid = multiField.isValid();

        // THEN
        assertTrue(isValid);
    }

    @Test
    public void validationFailsIfSubFieldValidatorFails() throws Exception {
        // GIVEN
        createField();

        // add required text field with invalid email text
        TextField textField = new TextField();
        textField.setRequired(true);
        textField.addValidator(EMAIL_VALIDATOR);
        textField.setValue("foo");
        addSubField(textField);

        // WHEN
        boolean isValid = multiField.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationSuccedsIfSubFieldValidatorPasses() throws Exception {
        // GIVEN
        createField();

        // add text field with valid email text
        TextField textField = new TextField();
        textField.addValidator(EMAIL_VALIDATOR);
        textField.setValue("foo@magnolia-cms.com");
        addSubField(textField);

        // WHEN
        boolean isValid = multiField.isValid();

        // THEN
        assertTrue(isValid);
    }

    private void createField() {
        factory = new MultiValueFieldFactory<>(definition, baseItem, null, i18NAuthoringSupport, subfieldFactory, componentProvider);
        factory.setComponentProvider(componentProvider);
        multiField = (MultiField) factory.createField();
    }

    private void addSubField(final Component component) {
        AbstractOrderedLayout rootLayout = (AbstractOrderedLayout) multiField.iterator().next();
        rootLayout.addComponent(component);
    }

    private FieldTypeDefinitionRegistry createFieldTypeRegistry() {
        FieldTypeDefinitionRegistry registry = new FieldTypeDefinitionRegistry(mock(ModuleRegistry.class));

        ConfiguredFieldTypeDefinition textFieldDefinition = new ConfiguredFieldTypeDefinition();
        textFieldDefinition.setDefinitionClass(TextFieldDefinition.class);
        textFieldDefinition.setFactoryClass(TextFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("text", textFieldDefinition));

        return registry;
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        MultiValueFieldDefinition definition = new MultiValueFieldDefinition();
        definition = (MultiValueFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(definition, propertyName);
        definition.setDefaultValue(null);

        // Set fields
        textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition.setRows(0);
        textFieldDefinition.setName("text");

        definition.setField(textFieldDefinition);

        this.definition = definition;
    }

    @Test
    public void areButtonsInvisibleWhenReadOnly() {
        // GIVEN
        definition.setReadOnly(true);

        // WHEN
        createField();

        // THEN
        assertTrue(multiField.isReadOnly());
        assertTrue(definition.getField().isReadOnly());
        assertFalse(buttonsExist(multiField));
    }

    @Test
    public void areButtonsVisibleWhenEditable() {
        // GIVEN
        definition.setReadOnly(false);

        // WHEN
        createField();

        // THEN
        assertFalse(multiField.isReadOnly());
        assertFalse(definition.getField().isReadOnly());
        assertTrue(buttonsExist(multiField));
    }

    private boolean buttonsExist(MultiField multiField) {
        VerticalLayout root = (VerticalLayout) multiField.iterator().next();
        Iterator<Component> it = root.iterator();
        boolean buttonsExist = false;
        while (it.hasNext()) {
            Component component = it.next();
            if (component instanceof Button) {
                buttonsExist = true;
            }
        }
        return buttonsExist;
    }
}
