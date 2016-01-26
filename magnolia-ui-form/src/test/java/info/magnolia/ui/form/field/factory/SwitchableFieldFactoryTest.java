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

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.*;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.aceeditor.AceEditor;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

public class SwitchableFieldFactoryTest extends AbstractFieldFactoryTestCase<SwitchableFieldDefinition> {

    private SwitchableFieldFactory<SwitchableFieldDefinition> factory;
    private FieldFactoryFactory subfieldFactory;
    private I18NAuthoringSupport i18nAuthoringSupport;
    private SwitchableField field;
    private TextFieldDefinition textFieldDefinition;
    private CodeFieldDefinition codeFieldDefinition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        i18nAuthoringSupport = mock(I18NAuthoringSupport.class);
        componentProvider.registerInstance(ComponentProvider.class, componentProvider);

        FieldTypeDefinitionRegistry fieldDefinitionRegistry = createFieldTypeRegistry();
        subfieldFactory = new FieldFactoryFactory(componentProvider, fieldDefinitionRegistry, null);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        field = null;
        factory = null;
    }

    @Test
    public void createFieldComponentTest() {
        // GIVEN
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18nAuthoringSupport);
        factory.setComponentProvider(componentProvider);

        // WHEN
        Field<PropertysetItem> field = factory.createField();

        // THEN
        assertThat(field, instanceOf(SwitchableField.class));
    }

    @Test
    public void selectHasNoDefaultValueIfNotConfigured() {
        // GIVEN

        // WHEN
        createField();

        // THEN
        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);
        assertTrue(select.isNullSelectionAllowed());
        assertNull(select.getValue());
    }

    @Test
    public void selectHasDefaultValueIfConfigured() throws Exception {
        // GIVEN
        definition.getOptions().get(1).setSelected(true);
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());

        // WHEN
        createField();

        // THEN
        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);
        assertTrue(select.isNullSelectionAllowed());
        assertEquals("code", select.getValue());
    }

    @Test
    public void setI18nForDelegatingCompositeFieldTransformer() throws Exception {
        // GIVEN
        definition.setTransformerClass(DelegatingCompositeFieldTransformer.class);
        definition.setI18n(true);

        // WHEN
        createField();

        // THEN
        assertTrue(definition.getFields().get(3).isI18n());
    }

    @Test
    public void setI18nForSwitchableTransformer() throws Exception {
        // GIVEN
        definition.setTransformerClass(SwitchableTransformer.class);
        definition.setI18n(true);

        // WHEN
        createField();

        // THEN
        assertFalse(definition.getFields().get(2).isI18n());
    }

    @Test
    public void switchingWritesToItem() throws Exception {
        // GIVEN
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName()) {
            @Override
            public boolean addItemProperty(Object propertyId, Property property) throws UnsupportedOperationException {
                return super.addItemProperty(propertyId, property);
            }
        };
        createField();
        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);

        // WHEN
        select.setValue("text");
        baseNode = ((JcrNewNodeAdapter) baseItem).applyChanges();

        // THEN
        assertEquals("text", baseItem.getItemProperty(propertyName).getValue());
        assertEquals("text", baseNode.getProperty(propertyName).getString());
        assertFalse(baseNode.hasProperty(propertyName + "text"));
    }

    @Test
    public void switchingWritesToItemWithDefaultValue() throws Exception {
        // GIVEN
        definition.getFields().get(0).setDefaultValue("hop!");
        baseItem = new JcrNewNodeAdapter(baseNode, baseNode.getPrimaryNodeType().getName());
        createField();
        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);

        // WHEN
        select.setValue("text");
        baseNode = ((JcrNewNodeAdapter) baseItem).applyChanges();

        // THEN
        assertEquals("text", baseNode.getProperty(propertyName).getString());
        assertTrue(baseNode.hasProperty(propertyName + "text"));
        assertEquals("hop!", baseNode.getProperty(propertyName + "text").getString());
    }

    @Test
    public void fieldNamesAndFieldsStayInSynch() {
        // GIVEN
        createField();
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
        createField();
        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);

        // WHEN
        select.setValue("text");
        assertEquals("has the select field and the selected textfield", 2, getVisibleFieldNb());

        // WHEN
        select.setValue("hidden");

        // THEN
        assertEquals("has only the select field as the hidden is not visible", 1, getVisibleFieldNb());
    }

    @Test
    public void validationIsTriggeredOnlyForVisibleField() {
        // GIVEN
        textFieldDefinition.setRequired(true);
        codeFieldDefinition.setRequired(true);

        createField();

        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);
        select.setValue("text");
        TextField textField = getSubFieldWithType(TextField.class);
        textField.setValue("foo");

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertTrue(isValid);
    }

    @Test
    public void validationOfSelectedOptionFails() {
        // GIVEN
        textFieldDefinition.setRequired(true);
        codeFieldDefinition.setRequired(true);

        createField();

        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);
        select.setValue("code");
        // default value for AceEditor is empty string, and AceEditor doesn't consider this an empty value in #isEmpty(), as it should (see AbstractTextField#isEmpty)
        AceEditor codeField = getSubFieldWithType(AceEditor.class);
        codeField.setValue(null);

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationOfRequiredSwitchableFailsIfNoOptionIsSelected() {
        // GIVEN
        definition.setRequired(true);
        createField();
        // invoke CustomField#getContent() on CustomField (indirectly through component iterators)
        getSubFieldWithType(AbstractSelect.class);

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationOfRequiredSwitchableSucceedsIfOptionIsSelected() {
        // GIVEN
        definition.setRequired(true);
        createField();
        AbstractSelect select = getSubFieldWithType(AbstractSelect.class);
        select.setValue("text");

        // WHEN
        boolean isValid = field.isValid();

        // THEN
        assertTrue(isValid);
    }

    private void createField() {
        factory = new SwitchableFieldFactory<>(definition, baseItem, subfieldFactory, componentProvider, i18NAuthoringSupport);
        factory.setComponentProvider(componentProvider);
        field = (SwitchableField) factory.createField();
    }

    private <F extends Component> F getSubFieldWithType(final Class<F> fieldClass) {
        AbstractOrderedLayout rootLayout = (AbstractOrderedLayout) field.iterator().next();
        Iterator<Component> filteredIterator = Iterators.filter(rootLayout.iterator(), Predicates.instanceOf(fieldClass));
        return filteredIterator.hasNext() ? (F) filteredIterator.next() : null;
    }

    private int getVisibleFieldNb() {
        AbstractOrderedLayout rootLayout = (AbstractOrderedLayout) field.iterator().next();
        Iterator<Component> filteredIterator = Iterators.filter(rootLayout.iterator(), new Predicate<Component>() {
            @Override
            public boolean apply(Component component) {
                return component.isVisible();
            }
        });
        return Iterators.size(filteredIterator);
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
        textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition.setRows(0);
        textFieldDefinition.setName("text");

        codeFieldDefinition = new CodeFieldDefinition();
        codeFieldDefinition.setLanguage("java");
        codeFieldDefinition.setName("code");

        HiddenFieldDefinition hiddenFieldDefinition = new HiddenFieldDefinition();
        hiddenFieldDefinition.setName("hidden");

        List<ConfiguredFieldDefinition> fields = newArrayList(textFieldDefinition, codeFieldDefinition, hiddenFieldDefinition);
        definition.setFields(fields);

        this.definition = definition;
    }

}
