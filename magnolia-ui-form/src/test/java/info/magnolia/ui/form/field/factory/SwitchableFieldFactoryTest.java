/**
 * This file Copyright (c) 2013-2014 Magnolia International
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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.ui.form.field.SwitchableField;
import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.fieldType.registry.FieldTypeDefinitionRegistryTest.TestFieldTypeDefinitionProvider;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Field;

/**
 * Test class.
 */
public class SwitchableFieldFactoryTest extends AbstractFieldFactoryTestCase<SwitchableFieldDefinition> {

    private SwitchableFieldFactory<SwitchableFieldDefinition> factory;
    private MockComponentProvider componentProvider;
    private FieldFactoryFactory subfieldFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        componentProvider = new MockComponentProvider();
        componentProvider.registerInstance(ComponentProvider.class, componentProvider);
        FieldTypeDefinitionRegistry fieldDefinitionRegistery = createFieldTypeRegistery();
        subfieldFactory = new FieldFactoryFactory(componentProvider, fieldDefinitionRegistery, null);
    }

    @Test
    public void createFieldComponentTest() {
        // GIVEN
        factory = new SwitchableFieldFactory<SwitchableFieldDefinition>(definition, baseItem, subfieldFactory, i18nContentSupport, componentProvider);
        factory.setComponentProvider(componentProvider);

        // WHEN
        Field<PropertysetItem> field = factory.createField();

        // THEN
        assertNotNull(field);
        assertTrue(field instanceof SwitchableField);
    }

    @Test
    public void testSelectHasNoDefaultValueIfNotConfigured() {
        // GIVEN
        factory = new SwitchableFieldFactory<SwitchableFieldDefinition>(definition, baseItem, subfieldFactory, i18nContentSupport, componentProvider);
        factory.setComponentProvider(componentProvider);

        // WHEN
        SwitchableField field = (SwitchableField) factory.createField();

        // THEN
        AbstractOrderedLayout layout = (AbstractOrderedLayout) field.iterator().next();
        AbstractSelect select = (AbstractSelect) layout.iterator().next();
        assertTrue(select.isNullSelectionAllowed());
        assertNull(select.getValue());
    }

    @Test
    public void testSelectHasDefaultValueIfConfigured() throws Exception {
        // GIVEN
        definition.getOptions().get(1).setSelected(true);
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        factory = new SwitchableFieldFactory<SwitchableFieldDefinition>(definition, baseItem, subfieldFactory, i18nContentSupport, componentProvider);
        factory.setComponentProvider(componentProvider);

        // WHEN
        SwitchableField field = (SwitchableField) factory.createField();

        // THEN
        AbstractOrderedLayout layout = (AbstractOrderedLayout) field.iterator().next();
        AbstractSelect select = (AbstractSelect) layout.iterator().next();
        assertTrue(select.isNullSelectionAllowed());
        assertEquals("code", select.getValue());
    }

    @Test
    public void testSwitchingWritesToItem() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        factory = new SwitchableFieldFactory<SwitchableFieldDefinition>(definition, baseItem, subfieldFactory, i18nContentSupport, componentProvider);
        factory.setComponentProvider(componentProvider);
        SwitchableField field = (SwitchableField) factory.createField();
        AbstractOrderedLayout layout = (AbstractOrderedLayout) field.iterator().next();
        AbstractSelect select = (AbstractSelect) layout.iterator().next();

        // WHEN
        select.setValue("text");
        baseNode = ((JcrNewNodeAdapter) baseItem).applyChanges();

        // THEN
        assertEquals("text", baseItem.getItemProperty(propertyName).getValue());
        assertEquals("text", baseNode.getProperty(propertyName).getString());
        assertFalse(baseNode.hasProperty(propertyName + "text"));
        assertTrue(baseNode.hasProperty(propertyName + "code"));
    }

    @Test
    public void testSwitchingWritesToItemWithDefaultValue() throws Exception {
        // GIVEN
        definition.getFields().get(0).setDefaultValue("hop!");
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        factory = new SwitchableFieldFactory<SwitchableFieldDefinition>(definition, baseItem, subfieldFactory, i18nContentSupport, componentProvider);
        factory.setComponentProvider(componentProvider);
        SwitchableField field = (SwitchableField) factory.createField();
        AbstractOrderedLayout layout = (AbstractOrderedLayout) field.iterator().next();
        AbstractSelect select = (AbstractSelect) layout.iterator().next();

        // WHEN
        select.setValue("text");
        baseNode = ((JcrNewNodeAdapter) baseItem).applyChanges();

        // THEN
        assertEquals("text", baseNode.getProperty(propertyName).getString());
        assertTrue(baseNode.hasProperty(propertyName + "text"));
        assertEquals("hop!", baseNode.getProperty(propertyName + "text").getString());
        assertTrue(baseNode.hasProperty(propertyName + "code"));
    }

    private FieldTypeDefinitionRegistry createFieldTypeRegistery() {
        FieldTypeDefinitionRegistry registery = new FieldTypeDefinitionRegistry();

        ConfiguredFieldTypeDefinition textFieldDefinition = new ConfiguredFieldTypeDefinition();
        textFieldDefinition.setDefinitionClass(TextFieldDefinition.class);
        textFieldDefinition.setFactoryClass(TextFieldFactory.class);
        registery.register(new TestFieldTypeDefinitionProvider("text", textFieldDefinition));

        ConfiguredFieldTypeDefinition codeFieldDefinition = new ConfiguredFieldTypeDefinition();
        codeFieldDefinition.setDefinitionClass(BasicTextCodeFieldDefinition.class);
        codeFieldDefinition.setFactoryClass(BasicTextCodeFieldFactory.class);
        registery.register(new TestFieldTypeDefinitionProvider("code", codeFieldDefinition));

        ConfiguredFieldTypeDefinition selectFieldDefinition = new ConfiguredFieldTypeDefinition();
        selectFieldDefinition.setDefinitionClass(OptionGroupFieldDefinition.class);
        selectFieldDefinition.setFactoryClass(OptionGroupFieldFactory.class);
        registery.register(new TestFieldTypeDefinitionProvider("option", selectFieldDefinition));

        return registery;
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
        option2.setLabel("Code");
        option2.setValue("code");
        ArrayList<SelectFieldOptionDefinition> options = new ArrayList<SelectFieldOptionDefinition>();
        options.add(option1);
        options.add(option2);
        definition.setOptions(options);

        // Set fields
        TextFieldDefinition textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition = (TextFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(textFieldDefinition, propertyName);
        textFieldDefinition.setRows(0);
        textFieldDefinition.setName("text");
        BasicTextCodeFieldDefinition codeFieldDefinition = new BasicTextCodeFieldDefinition();
        codeFieldDefinition.setLanguage("java");
        codeFieldDefinition.setName(propertyName);
        codeFieldDefinition.setName("code");
        ArrayList<ConfiguredFieldDefinition> fields = new ArrayList<ConfiguredFieldDefinition>();
        fields.add(textFieldDefinition);
        fields.add(codeFieldDefinition);
        definition.setFields(fields);

        this.definition = definition;
    }

}
