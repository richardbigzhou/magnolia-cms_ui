/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.SwitchableField;
import info.magnolia.ui.form.field.definition.CodeFieldDefinition;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.HiddenFieldDefinition;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.transformer.composite.DelegatingCompositeFieldTransformer;
import info.magnolia.ui.form.field.transformer.composite.SwitchableTransformer;
import info.magnolia.ui.form.fieldType.registry.FieldTypeDefinitionRegistryTest.TestFieldTypeDefinitionProvider;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

/**
 * Test class.
 */
public class SwitchableFieldFactoryTest extends AbstractFieldFactoryTestCase<SwitchableFieldDefinition> {

    private SwitchableFieldFactory<SwitchableFieldDefinition> factory;
    private FieldFactoryFactory subfieldFactory;
    private I18NAuthoringSupport i18nAuthoringSupport;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        i18nAuthoringSupport = mock(I18NAuthoringSupport.class);
        componentProvider.registerInstance(ComponentProvider.class, componentProvider);
        FieldTypeDefinitionRegistry fieldDefinitionRegistry = createFieldTypeRegistry();
        subfieldFactory = new FieldFactoryFactory(componentProvider, fieldDefinitionRegistry, null);
    }

    @Test
    public void createFieldComponentTest() {
        // GIVEN
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
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
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
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
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
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
    public void setI18nForDelegatingCompositeFieldTransformer() throws Exception {
        // GIVEN
        definition.setTransformerClass(DelegatingCompositeFieldTransformer.class);
        definition.setI18n(true);
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
        factory.setComponentProvider(componentProvider);

        // WHEN
        factory.createField();

        // THEN
        assertTrue(definition.getFields().get(3).isI18n());
    }

    @Test
    public void setI18nForSwitchableTransformer() throws Exception {
        // GIVEN
        definition.setTransformerClass(SwitchableTransformer.class);
        definition.setI18n(true);
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
        factory.setComponentProvider(componentProvider);

        // WHEN
        factory.createField();

        // THEN
        assertFalse(definition.getFields().get(2).isI18n());
    }

    @Test
    public void testSwitchingWritesToItem() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName()) {
            @Override
            public boolean addItemProperty(Object propertyId, Property property) throws UnsupportedOperationException {
                return super.addItemProperty(propertyId, property);
            }
        };
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
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
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
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

    @Test
    public void fieldNamesAndFieldsStayInSynch() {
        // GIVEN
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
        factory.setComponentProvider(componentProvider);
        factory.createField();
        assertEquals(4, definition.getFields().size());
        assertEquals(4, definition.getFieldNames().size());
        assertTrue(definition.getFieldNames().contains(definition.getName()));

        // WHEN
        factory.createField();

        // THEN
        assertEquals(4, definition.getFields().size());
        assertEquals(4, definition.getFieldNames().size());
        assertTrue(definition.getFieldNames().contains(definition.getName()));
    }

    /**
     * Ensure that calling CompositeFieldDefinition#getFields before CompositeFieldDefinition#setFields does not impact the result of getFieldNames()
     * This was the key problem in MGNLUI-3402.
     */
    @Test
    public void fieldNamesAndFieldsStayInSynchWhenGetFieldsIsCalledBeforeSetFields() {
        // GIVEN
        ConfiguredFieldDefinition fieldA = new ConfiguredFieldDefinition();
        fieldA.setName("a");

        ConfiguredFieldDefinition fieldB = new ConfiguredFieldDefinition();
        fieldB.setName("b");

        List<ConfiguredFieldDefinition> fields = newArrayList(fieldA, fieldB);

        CompositeFieldDefinition compositeFieldDefinition = new CompositeFieldDefinition();
        compositeFieldDefinition.setFields(fields);

        // WHEN
        List<String> names = compositeFieldDefinition.getFieldNames();

        // THEN
        assertThat(names, hasItems("a", "b"));
    }

    @Test
    public void doNotAddNonVisibleField() {
        // GIVEN
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
        factory.setComponentProvider(componentProvider);
        SwitchableField field = (SwitchableField) factory.createField();
        AbstractOrderedLayout layout = (AbstractOrderedLayout) field.iterator().next();
        AbstractSelect select = (AbstractSelect) layout.iterator().next();

        // WHEN
        select.setValue("text");
        assertEquals("has the select field and the selected textfield", 2, getVisibleFieldNb(layout));

        // WHEN
        select.setValue("hidden");

        // THEN
        assertEquals("has only the select field as the hidden is not visible", 1, getVisibleFieldNb(layout));
    }

    private int getVisibleFieldNb(AbstractOrderedLayout layout) {
        Iterator<Component> iterator = layout.iterator();
        int res = 0;
        while (iterator.hasNext()) {
            Field<?> field = (Field<?>) iterator.next();
            if (field.isVisible()) {
                res += 1;
            }
        }
        return res;
    }

    private FieldTypeDefinitionRegistry createFieldTypeRegistry() {
        FieldTypeDefinitionRegistry registry = new FieldTypeDefinitionRegistry();

        ConfiguredFieldTypeDefinition textFieldDefinition = new ConfiguredFieldTypeDefinition();
        textFieldDefinition.setDefinitionClass(TextFieldDefinition.class);
        textFieldDefinition.setFactoryClass(TextFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("text", textFieldDefinition));

        ConfiguredFieldTypeDefinition codeFieldDefinition = new ConfiguredFieldTypeDefinition();
        codeFieldDefinition.setDefinitionClass(CodeFieldDefinition.class);
        codeFieldDefinition.setFactoryClass(CodeFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("code", codeFieldDefinition));

        ConfiguredFieldTypeDefinition selectFieldDefinition = new ConfiguredFieldTypeDefinition();
        selectFieldDefinition.setDefinitionClass(OptionGroupFieldDefinition.class);
        selectFieldDefinition.setFactoryClass(OptionGroupFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("option", selectFieldDefinition));

        ConfiguredFieldTypeDefinition hiddenFieldDefinition = new ConfiguredFieldTypeDefinition();
        hiddenFieldDefinition.setDefinitionClass(HiddenFieldDefinition.class);
        hiddenFieldDefinition.setFactoryClass(HiddenFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("hidden", hiddenFieldDefinition));

        return registry;
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
        SelectFieldOptionDefinition option3 = new SelectFieldOptionDefinition();
        option3.setLabel("Hidden");
        option3.setValue("hidden");

        List<SelectFieldOptionDefinition> options = newArrayList(option1, option2, option3);
        definition.setOptions(options);

        // Set fields
        TextFieldDefinition textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition = (TextFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(textFieldDefinition, propertyName);
        textFieldDefinition.setRows(0);
        textFieldDefinition.setName("text");

        CodeFieldDefinition codeFieldDefinition = new CodeFieldDefinition();
        codeFieldDefinition.setLanguage("java");
        codeFieldDefinition.setName(propertyName);
        codeFieldDefinition.setName("code");

        HiddenFieldDefinition hiddenFieldDefinition = new HiddenFieldDefinition();
        hiddenFieldDefinition.setName("hidden");

        List<ConfiguredFieldDefinition> fields = newArrayList(textFieldDefinition, codeFieldDefinition, hiddenFieldDefinition);
        definition.setFields(fields);

        this.definition = definition;
    }

}
