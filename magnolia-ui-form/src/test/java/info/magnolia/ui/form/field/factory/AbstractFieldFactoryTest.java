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

import info.magnolia.objectfactory.Components;
import info.magnolia.test.mock.MockComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.AbstractFieldFactory}.
 */
public class AbstractFieldFactoryTest extends AbstractFieldFactoryTestCase<ConfiguredFieldDefinition> {

    private AbstractFieldFactory<FieldDefinition, Object> abstractDialogField;

    @Test
    public void simpleInitializationTest() {
        // GIVEN
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        // WHEN
        Field<Object> field = abstractDialogField.createField();
        // THEN
        assertTrue(TextField.class.isAssignableFrom(field.getClass()));
        assertEquals(definition, abstractDialogField.getFieldDefinition());
        assertEquals(false, field.isRequired());
        assertEquals("label", field.getCaption());
        assertEquals(false, field.getPropertyDataSource().isReadOnly());
        assertEquals(true, field.getPropertyDataSource() instanceof TransformedProperty);
    }

    @Test
    public void changePropertyValueTest() throws Exception {
        // GIVEN
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        Field<Object> field = abstractDialogField.createField();

        // WHEN
        field.setValue("new Value");

        // THEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();
        assertEquals(true, res.hasProperty(propertyName));
        assertEquals("new Value", res.getProperty(propertyName).getString());
        assertEquals(PropertyType.STRING, res.getProperty(propertyName).getType());
        Property p = baseItem.getItemProperty(propertyName);
        assertEquals(field.getPropertyDataSource().getValue(), p.getValue());
        assertEquals("new Value", p.getValue().toString());
        assertEquals(String.class, p.getValue().getClass());
    }

    @Test
    public void testPropertyValueChangeWithSaveInfoTrue() throws Exception {
        // GIVEN
        baseNode.setProperty(propertyName, "value");
        baseItem = new JcrNodeAdapter(baseNode);
        // Set do not change
        definition.setReadOnly(false);
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        Field<Object> field = abstractDialogField.createField();

        // WHEN
        field.setValue("new Value");

        // THEN
        Node res = ((JcrNodeAdapter) baseItem).applyChanges();
        assertEquals(true, res.hasProperty(propertyName));
        assertEquals("new Value", res.getProperty(propertyName).getString());
        Property p = baseItem.getItemProperty(propertyName);
        assertEquals("new Value", p.getValue().toString());

    }

    @Test
    public void testValueForEmptyNewItem() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName(), "newItem");
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        Field<Object> field = abstractDialogField.createField();

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
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        Field<Object> field = abstractDialogField.createField();

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
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        Field<Object> field = abstractDialogField.createField();

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
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        Field<Object> field = abstractDialogField.createField();

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
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        Field<Object> field = abstractDialogField.createField();

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
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());

        // WHEN
        Field<Object> field = abstractDialogField.createField();
        // THEN

        assertEquals("label", field.getCaption());

    }

    @Test
    public void requiredFieldIsMarkedByAsteriskTest() {
        // GIVEN
        definition.setRequired(true);
        abstractDialogField = new TestTextFieldFactory(definition, baseItem);
        abstractDialogField.setI18nContentSupport(i18nContentSupport);
        abstractDialogField.setComponentProvider(new MockComponentProvider());

        // WHEN
        Field<Object> field = abstractDialogField.createField();
        field.setRequired(definition.isRequired());

        // THEN
        assertEquals(true, field.isRequired());
        assertTrue(field.getCaption().contains("*"));
    }

    @Test
    public void testPlainBeanItemSupport() throws Exception {
        // GIVEN
        baseItem = new BeanItem<TestBean>(new TestBean("bar"));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        abstractDialogField = new TestTextFieldFactory(def, baseItem);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        // WHEN
        Field<?> field = abstractDialogField.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals("bar", p.getValue().toString());
    }

    @Test
    public void testPropertysetItemSupport() throws Exception {
        // GIVEN
        baseItem = new PropertysetItem();
        baseItem.addItemProperty("foo", new ObjectProperty<String>("fooValue"));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        abstractDialogField = new TestTextFieldFactory(def, baseItem);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        // WHEN
        Field<?> field = abstractDialogField.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertEquals("fooValue", p.getValue().toString());
    }

    @Test
    public void testPropertysetItemSupportNonExistingProperty() throws Exception {
        // GIVEN
        baseItem = new PropertysetItem();
        baseItem.addItemProperty("foo", new ObjectProperty<String>("fooValue"));
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "bar");
        abstractDialogField = new TestTextFieldFactory(def, baseItem);
        abstractDialogField.setComponentProvider(new MockComponentProvider());
        // WHEN
        Field<?> field = abstractDialogField.createField();

        // THEN
        Property<?> p = field.getPropertyDataSource();
        assertNotNull(p);
    }

    @Test
    public void testLabelRequired() throws Exception {
        // GIVEN
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        def.setRequired(true);

        abstractDialogField = new TestTextFieldFactory(def, baseItem);
        abstractDialogField.setComponentProvider(new MockComponentProvider());

        // WHEN
        Field<?> field = abstractDialogField.createField();

        // THEN
        assertTrue(field.getCaption().contains("<span class=\"requiredfield\">*</span>"));
    }

    @Test
    public void testEmptyOrNullLabelSetsNoCaption() throws Exception {
        // GIVEN
        ConfiguredFieldDefinition def = createConfiguredFieldDefinition(new ConfiguredFieldDefinition(), "foo");
        def.setLabel("");

        abstractDialogField = new TestTextFieldFactory(def, baseItem);
        abstractDialogField.setComponentProvider(new MockComponentProvider());

        // WHEN
        Field<?> field = abstractDialogField.createField();

        // THEN
        assertNull(field.getCaption());

        // GIVEN
        def.setLabel(null);

        // WHEN
        field = abstractDialogField.createField();

        // THEN
        assertNull(field.getCaption());
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

        public TestTextFieldFactory(FieldDefinition definition, Item relatedFieldItem) {
            super(definition, relatedFieldItem);
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
        }
    }

    public static class TestBean {
        private String foo;

        public TestBean(String foo) {
            this.foo = foo;
        }

        public String getFoo() {
            return foo;
        }
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        ConfiguredFieldDefinition configureFieldDefinition = new ConfiguredFieldDefinition();
        this.definition = createConfiguredFieldDefinition(configureFieldDefinition, propertyName);
    }

}
