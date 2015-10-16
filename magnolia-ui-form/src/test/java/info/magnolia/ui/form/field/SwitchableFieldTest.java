/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.form.field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.AbstractFieldFactoryTest;
import info.magnolia.ui.form.field.factory.LinkFieldFactory;
import info.magnolia.ui.form.field.factory.OptionGroupFieldFactory;
import info.magnolia.ui.form.field.factory.SwitchableFieldFactory;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.form.fieldType.registry.FieldTypeDefinitionRegistryTest.TestFieldTypeDefinitionProvider;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.OptionGroup;

public class SwitchableFieldTest extends AbstractFieldTestCase<SwitchableFieldDefinition> {
    private SwitchableField field;
    private List<AbstractField<PropertysetItem>> fields;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = new SwitchableFieldFactory<SwitchableFieldDefinition>(definition, baseItem, subfieldFactory, componentProvider, mock(I18NAuthoringSupport.class));
        factory.setComponentProvider(componentProvider);

        field = (SwitchableField) factory.createField();
        field.initContent();
        fields = field.getFields(field.root, true);
    }

    @Test
    public void validationIsTriggeredOnlyForVisibleField() throws Exception {
        // GIVEN
        AbstractField<PropertysetItem> textField = fields.get(1);
        textField.setRequired(true);
        textField.setVisible(true);
        textField.setConvertedValue("foo");

        AbstractField<?> linkField = fields.get(2);
        linkField.setRequired(true);
        linkField.setVisible(false);

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertTrue(isValid);
    }

    @Test
    public void validationOfSelectedOptionFails() throws Exception {
        // GIVEN
        AbstractField<?> textField = fields.get(1);
        textField.setRequired(true);
        textField.setVisible(false);

        AbstractField<?> linkField = fields.get(2);
        linkField.setRequired(true);
        linkField.setVisible(true);
        linkField.setValue(null);

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationOfRequiredSwitchableFailsIfNoOptionIsSelected() throws Exception {
        // GIVEN
        AbstractField<?> optionGroup = fields.get(0);
        assertTrue(optionGroup instanceof OptionGroup);
        optionGroup.setRequired(true);
        optionGroup.setValue(null);

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationOfRequiredSwitchableSuccedsIfOptionIsSelected() throws Exception {
        // GIVEN
        AbstractField<?> optionGroup = fields.get(0);
        assertTrue(optionGroup instanceof OptionGroup);
        optionGroup.setRequired(true);
        optionGroup.setConvertedValue("text");

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertTrue(isValid);
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        SwitchableFieldDefinition definition = new SwitchableFieldDefinition();
        definition = (SwitchableFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(definition, propertyName);
        definition.setDefaultValue(null);

        // Define options
        SelectFieldOptionDefinition option1 = new SelectFieldOptionDefinition();
        option1.setLabel("Text");
        option1.setValue("text");
        SelectFieldOptionDefinition option2 = new SelectFieldOptionDefinition();
        option2.setLabel("Link");
        option2.setValue("link");

        ArrayList<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();
        options.add(option1);
        options.add(option2);
        definition.setOptions(options);

        // Set fields
        TextFieldDefinition textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition = (TextFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(textFieldDefinition, propertyName);
        textFieldDefinition.setName("text");

        LinkFieldDefinition linkFieldDefinition = new LinkFieldDefinition();
        linkFieldDefinition.setName("link");

        ArrayList<ConfiguredFieldDefinition> fields = new ArrayList<ConfiguredFieldDefinition>();
        fields.add(textFieldDefinition);
        fields.add(linkFieldDefinition);
        definition.setFields(fields);

        this.definition = definition;
    }

    @Override
    protected List<DefinitionProvider<FieldTypeDefinition>> getFieldTypeDefinitions() {
        List<DefinitionProvider<FieldTypeDefinition>> defs = new ArrayList<>();

        ConfiguredFieldTypeDefinition textFieldDefinition = new ConfiguredFieldTypeDefinition();
        textFieldDefinition.setDefinitionClass(TextFieldDefinition.class);
        textFieldDefinition.setFactoryClass(TextFieldFactory.class);
        defs.add(new TestFieldTypeDefinitionProvider("text", textFieldDefinition));

        ConfiguredFieldTypeDefinition linkFieldDefinition = new ConfiguredFieldTypeDefinition();
        linkFieldDefinition.setDefinitionClass(LinkFieldDefinition.class);
        linkFieldDefinition.setFactoryClass(LinkFieldFactory.class);
        defs.add(new TestFieldTypeDefinitionProvider("link", linkFieldDefinition));

        ConfiguredFieldTypeDefinition selectFieldDefinition = new ConfiguredFieldTypeDefinition();
        selectFieldDefinition.setDefinitionClass(OptionGroupFieldDefinition.class);
        selectFieldDefinition.setFactoryClass(OptionGroupFieldFactory.class);
        defs.add(new TestFieldTypeDefinitionProvider("option", selectFieldDefinition));
        return defs;
    }
}
