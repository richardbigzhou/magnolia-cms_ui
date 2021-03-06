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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.converter.StringToCalendarConverter;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.basic.ListToSetTransformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.data.util.converter.StringToFloatConverter;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.AbstractFieldFactory}.
 */
public class AbstractFieldFactoryTest extends AbstractFieldFactoryTestCase<ConfiguredFieldDefinition> {

    private AbstractFieldFactory<FieldDefinition, Object> fieldFactory;

    @Test
    public void simpleInitializationTest() {
        // GIVEN
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        // WHEN
        Field<Object> field = fieldFactory.createField();
        // THEN
        assertTrue(TextField.class.isAssignableFrom(field.getClass()));
        assertEquals(definition, fieldFactory.getFieldDefinition());
        assertEquals(false, field.isRequired());
        assertEquals("label", field.getCaption());
        assertEquals(false, field.getPropertyDataSource().isReadOnly());
        assertEquals(true, field.getPropertyDataSource() instanceof TransformedProperty);
    }

    @Test
    public void changePropertyValueTest() throws Exception {
        // GIVEN
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        Field<Object> field = fieldFactory.createField();

        // WHEN
        field.setValue("new Value");

        // THEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();
        assertEquals(true, res.hasProperty(propertyName));
        assertEquals("new Value", res.getProperty(propertyName).getString());
        assertEquals(PropertyType.STRING, res.getProperty(propertyName).getType());
        Property p = baseItem.getItemProperty(propertyName);
        assertEquals(field.getPropertyDataSource().getValue(), p.getValue());
        assertEquals("new Value", p.getValue());
        assertEquals(String.class, p.getValue().getClass());
    }

    @Test
    public void testPropertyValueChangeWithSaveInfoTrue() throws Exception {
        // GIVEN
        baseNode.setProperty(propertyName, "value");
        baseItem = new JcrNodeAdapter(baseNode);
        // Set do not change
        definition.setReadOnly(false);
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        Field<Object> field = fieldFactory.createField();

        // WHEN
        field.setValue("new Value");

        // THEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();
        assertEquals(true, res.hasProperty(propertyName));
        assertEquals("new Value", res.getProperty(propertyName).getString());
        Property p = baseItem.getItemProperty(propertyName);
        assertEquals("new Value", p.getValue());

    }

    @Test
    public void testValueForEmptyNewItem() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName(), "newItem");
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        Field<Object> field = fieldFactory.createField();

        // WHEN
        String value = (String) field.getValue();

        // THEN
        assertNull(value);
    }

    @Test
    public void testValueForNotEmptyNewItem() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName(), "newItem");
        baseItem.addItemProperty(propertyName, DefaultPropertyUtil.newDefaultProperty(String.class, "value"));
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        Field<Object> field = fieldFactory.createField();

        // WHEN
        String value = (String) field.getValue();

        // THEN
        assertNotNull(value);
        assertEquals("value", value);

    }

    @Test
    public void testValueForEmptyNewItemWithDefaultValue() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName(), "newItem");
        definition.setDefaultValue("defaultValue");
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        Field<Object> field = fieldFactory.createField();

        // WHEN
        String value = (String) field.getValue();

        // THEN
        assertNotNull(value);
        assertEquals("defaultValue", value);
    }

    @Test
    public void testValueForNotEmptyNewItemWithDefaultValue() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName(), "newItem");
        baseItem.addItemProperty(propertyName, DefaultPropertyUtil.newDefaultProperty(String.class, "value"));
        definition.setDefaultValue("defaultValue");
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        Field<Object> field = fieldFactory.createField();

        // WHEN
        String value = (String) field.getValue();

        // THEN
        assertNotNull(value);
        assertEquals("value", value);
    }

    @Test
    public void testConversionOfPropertyTypeWithDouble() throws Exception {
        // GIVEN
        // Set property Type
        definition.setType("Double");
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        Field<Object> field = fieldFactory.createField();

        // WHEN
        field.setValue("21.98");

        // THEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();
        assertEquals(true, res.hasProperty(propertyName));
        assertEquals(PropertyType.DOUBLE, res.getProperty(propertyName).getType());
        assertEquals(Double.parseDouble("21.98"), res.getProperty(propertyName).getDouble(), 0);

        Property p = baseItem.getItemProperty(propertyName);
        assertEquals("21.98", p.getValue().toString());
        assertEquals(Double.class, p.getValue().getClass());
    }

    @Ignore
    @Test
    public void simpleI18NTest() {
        // GIVEN
        definition.setLabel("message.label");
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field<Object> field = fieldFactory.createField();
        // THEN

        assertEquals("label", field.getCaption());

    }

    @Test
    public void correctLocaleCodeTest() {
        // GIVEN
        when(i18NAuthoringSupport.deriveLocalisedPropertyName(any(String.class), any(Locale.class))).thenReturn(Locale.CHINA.toString());
        definition.setLabel("label");
        definition.setI18n(true);
        fieldFactory = new TestTextFieldFactory(definition, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setLocale(Locale.CHINA);
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field<Object> field = fieldFactory.createField();

        // THEN
        assertEquals("label (zh_CN)", field.getCaption());
    }

    @Test
    public void supportsBeanItem() throws Exception {
        // GIVEN
        baseItem = new BeanItem<>(new TestBean("bar"));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        // WHEN
        Field<?> field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals("bar", p.getValue());
    }

    @Test
    public void supportsBeanItemWithEnumMemberAndDefaultValue() throws Exception {
        // GIVEN
        baseItem = new BeanItem<>(new TestBean(null));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "breakfast");
        def.setType("info.magnolia.ui.form.field.factory.AbstractFieldFactoryTest$Breakfast");
        def.setDefaultValue(Breakfast.BAKED_BEANS.name());
        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals(Breakfast.BAKED_BEANS, p.getValue());
    }

    @Test
    public void supportsBeanItemWithEnumMemberViaListToSetTransformer() throws Exception {
        // GIVEN
        baseItem = new BeanItem<>(new TestBean(null));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "breakfast");
        def.setType("info.magnolia.ui.form.field.factory.AbstractFieldFactoryTest$Breakfast");
        def.setDefaultValue(Breakfast.BAKED_BEANS.name());
        def.setTransformerClass((Class<? extends Transformer<?>>) (Object) ListToSetTransformer.class);
        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals(Breakfast.BAKED_BEANS, p.getValue());
    }

    @Test
    public void supportsPropertysetItem() throws Exception {
        // GIVEN
        baseItem = new PropertysetItem();
        baseItem.addItemProperty("foo", new ObjectProperty<>("fooValue"));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        // WHEN
        Field<?> field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals("fooValue", p.getValue());
    }

    @Test
    public void supportsPropertysetItemWithNonExistingProperty() throws Exception {
        // GIVEN
        baseItem = new PropertysetItem();
        baseItem.addItemProperty("foo", new ObjectProperty<>("fooValue"));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "bar");
        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);
        // WHEN
        Field<?> field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertNotNull(p);
    }

    @Test
    public void supportsDefaultValueWithAssignableType() throws Exception {
        // GIVEN a factory/field backed by a Number property, assigning concrete default value of -1 (int)
        baseItem = new PropertysetItem();
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "number");
        def.setType(null);
        AbstractFieldFactory<FieldDefinition, Number> fieldFactory = new AbstractFieldFactory<FieldDefinition, Number>(def, baseItem, null, i18NAuthoringSupport) {

            @Override
            protected Class<?> getDefaultFieldType() {
                return Number.class;
            }

            @Override
            protected Number createDefaultValue(Property property) {
                return -1;
            }

            @Override
            protected Field<Number> createFieldComponent() {
                return new AbstractField<Number>() {
                    @Override
                    public Class<? extends Number> getType() {
                        return Number.class;
                    }
                };
            }
        };

        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field<Number> field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals(-1, p.getValue());

        // WHEN
        field.setValue(6d);

        // THEN
        assertEquals(6d, p.getValue());
    }

    @Test
    public void supportsDefaultValueWithConfiguredConverter() throws Exception {
        // GIVEN a factory/field backed by a Float property, with a converter explicitly configured
        baseItem = new PropertysetItem();
        ObjectProperty<Float> preTypedProperty = new ObjectProperty<>(null, Float.class);
        baseItem.addItemProperty("floaty", preTypedProperty);
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "floaty");
        def.setType(null);
        def.setDefaultValue("0.86");
        def.setConverterClass(StringToFloatConverter.class);
        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field<?> field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals(0.86f, p.getValue());
    }

    @Test
    public void supportsEnumPropertyAndDefaultValue() throws Exception {
        // GIVEN
        baseItem = new PropertysetItem();
        ObjectProperty<Breakfast> preTypedProperty = new ObjectProperty<>(Breakfast.EGGS_AND_BACON);
        preTypedProperty.setValue(null); // resetting actual value, so that defaultValue mechanism kicks in
        baseItem.addItemProperty("breakfast", preTypedProperty);
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "breakfast");
        def.setType("info.magnolia.ui.form.field.factory.AbstractFieldFactoryTest$Breakfast");
        def.setDefaultValue(Breakfast.BAKED_BEANS.name());
        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field field = fieldFactory.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals(Breakfast.BAKED_BEANS, p.getValue());
    }

    @Test
    public void testEmptyOrNullLabelSetsNoCaption() throws Exception {
        // GIVEN
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        def.setLabel("");

        fieldFactory = new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport);
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        Field<?> field = fieldFactory.createField();

        // THEN
        assertNull(field.getCaption());

        // GIVEN
        def.setLabel(null);

        // WHEN
        field = fieldFactory.createField();

        // THEN
        assertNull(field.getCaption());
    }

    @Test
    public void testFieldConverterInitialized() throws Exception {
        // GIVEN
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        def.setConverterClass(StringToCalendarConverter.class);

        fieldFactory = spy(new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport));
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        fieldFactory.createField();

        // THEN
        verify(fieldFactory, times(1)).initializeConverter(eq(StringToCalendarConverter.class));
    }

    @Test
     public void testViewConverterInitialized() throws Exception {
        // GIVEN
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new TextFieldDefinition(), "foo");
        def.setConverterClass(StringToCalendarConverter.class);

        fieldFactory = spy(new TestTextFieldFactory(def, baseItem, null, i18NAuthoringSupport));
        fieldFactory.setComponentProvider(this.componentProvider);

        // WHEN
        fieldFactory.getView();

        // THEN
        verify(fieldFactory, times(1)).initializeConverter(eq(StringToCalendarConverter.class));
    }

    public static ConfiguredFieldDefinition createConfiguredFieldDefinition(ConfiguredFieldDefinition configureFieldDefinition, String propertyName) {
        configureFieldDefinition.setDescription("description");
        configureFieldDefinition.setI18nBasename("i18nBasename");
        configureFieldDefinition.setLabel("label");
        configureFieldDefinition.setName(propertyName);
        configureFieldDefinition.setRequired(false);
        configureFieldDefinition.setReadOnly(false);
        configureFieldDefinition.setType("String");
        return configureFieldDefinition;
    }

    /**
     * Simplified test implementation of a {@link info.magnolia.ui.form.field.factory.TextFieldFactory}.
     */
    public static class TestTextFieldFactory extends AbstractFieldFactory<FieldDefinition, Object> {

        public TestTextFieldFactory(FieldDefinition definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport) {
            super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
        }

        @Override
        protected Field createFieldComponent() {
            return new TestTextField();
        }

        /**
         * {@link com.vaadin.data.util.converter.ConverterFactory} is bound to the {@link VaadinSession}. To get The default converters to work we need to mock the VaadinSession.
         *
         * @see AbstractFieldFactoryTestCase where we add the {@link com.vaadin.data.util.converter.DefaultConverterFactory} to the {@link info.magnolia.objectfactory.ComponentProvider}.
         */
        private class TestTextField extends TextField {

            @Override
            protected VaadinSession getSession() {
                return Components.getComponentProvider().getComponent(VaadinSession.class);
            }

            @Override
            public void setLocale(Locale locale) {
                // override this since locale-backing Vaadin components are not available here
            }
        }
    }

    public class TestBean {
        private String foo;
        private Breakfast breakfast;

        public TestBean(String foo) {
            this.foo = foo;
        }

        public String getFoo() {
            return foo;
        }

        public Breakfast getBreakfast() {
            return breakfast;
        }

        public void setBreakfast(Breakfast breakfast) {
            this.breakfast = breakfast;
        }
    }

    enum Breakfast {
        EGGS_AND_BACON, BAKED_BEANS
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        ConfiguredFieldDefinition configureFieldDefinition = new ConfiguredFieldDefinition();
        this.definition = createConfiguredFieldDefinition(configureFieldDefinition, propertyName);
    }
}
