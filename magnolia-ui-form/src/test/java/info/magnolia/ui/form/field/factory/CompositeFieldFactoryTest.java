/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
import static org.mockito.Mockito.*;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.CompositeField;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.DateFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.fieldType.registry.FieldTypeDefinitionRegistryTest.TestFieldTypeDefinitionProvider;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

public class CompositeFieldFactoryTest extends AbstractFieldFactoryTestCase<CompositeFieldDefinition> {

    private CompositeFieldFactory<CompositeFieldDefinition> factory;
    private FieldFactoryFactory subfieldFactory;
    private I18NAuthoringSupport i18nAuthoringSupport;
    private CompositeField compositeField;
    private TextFieldDefinition textFieldDefinition;
    private DateFieldDefinition dateFieldDefinition;
    private Appender mockAppender;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        i18nAuthoringSupport = mock(I18NAuthoringSupport.class);
        componentProvider.registerInstance(ComponentProvider.class, componentProvider);

        FieldTypeDefinitionRegistry fieldDefinitionRegistry = createFieldTypeRegistry();
        subfieldFactory = new FieldFactoryFactory(componentProvider, fieldDefinitionRegistry, null);

        mockAppender = mock(Appender.class);
        LogManager.getRootLogger().addAppender(mockAppender);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
        compositeField = null;
        factory = null;
        LogManager.getRootLogger().removeAppender(mockAppender);
    }

    @Test
    public void createFieldComponentTest() throws Exception {
        // GIVEN
        factory = new CompositeFieldFactory<>(definition, baseItem, null, i18nAuthoringSupport, subfieldFactory, componentProvider);
        factory.setComponentProvider(componentProvider);

        // WHEN
        Field<PropertysetItem> field = factory.createField();

        // THEN
        assertThat(field, instanceOf(CompositeField.class));
    }

    @Test
    public void validationSuccedsIfRequiredFieldIsNotEmpty() throws Exception {
        // GIVEN
        textFieldDefinition.setRequired(true);

        createField();

        TextField textField = getSubFieldWithType(TextField.class);
        textField.setValue("foo");

        // WHEN
        boolean isValid = compositeField.isValid();

        // THEN
        assertTrue(isValid);
    }

    @Test
    public void validationFailsIfRequiredFieldIsEmpty() throws Exception {
        // GIVEN
        dateFieldDefinition.setRequired(true);

        createField();

        DateField dateField = getSubFieldWithType(DateField.class);
        dateField.setValue(null);

        // WHEN
        boolean isValid = compositeField.isValid();

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void validationOfRequiredCompositeFieldIsNotSupportedAndLogsWarning() throws Exception {
        // GIVEN
        definition.setRequired(true);
        createField();
        // invoke CustomField#getContent() on CustomField (indirectly through component iterators)
        getSubFieldWithType(TextField.class);

        // WHEN
        compositeField.isValid();

        // THEN
        ArgumentCaptor<LoggingEvent> captorLoggingEvent = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        // Check log level
        assertThat(loggingEvent.getLevel(), is(Level.WARN));
        // Check the message being logged
        assertThat(loggingEvent.getRenderedMessage(),
                containsString("Definition of the composite field named [propertyName] is configured as required which is not supported (it is possible to configure the sub-fields as required though)."));
    }

    private void createField() {
        factory = new CompositeFieldFactory<>(definition, baseItem, null, i18NAuthoringSupport, subfieldFactory, componentProvider);
        factory.setComponentProvider(componentProvider);
        compositeField = (CompositeField) factory.createField();
    }

    private <F extends Component> F getSubFieldWithType(final Class<F> fieldClass) {
        AbstractOrderedLayout rootLayout = (AbstractOrderedLayout) compositeField.iterator().next();
        Iterator<Component> filteredIterator = Iterators.filter(rootLayout.iterator(), Predicates.instanceOf(fieldClass));
        return filteredIterator.hasNext() ? (F) filteredIterator.next() : null;
    }

    private FieldTypeDefinitionRegistry createFieldTypeRegistry() {
        FieldTypeDefinitionRegistry registry = new FieldTypeDefinitionRegistry(mock(ModuleRegistry.class));

        ConfiguredFieldTypeDefinition textFieldDefinition = new ConfiguredFieldTypeDefinition();
        textFieldDefinition.setDefinitionClass(TextFieldDefinition.class);
        textFieldDefinition.setFactoryClass(TextFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("text", textFieldDefinition));

        ConfiguredFieldTypeDefinition dateFieldDefinition = new ConfiguredFieldTypeDefinition();
        dateFieldDefinition.setDefinitionClass(DateFieldDefinition.class);
        dateFieldDefinition.setFactoryClass(DateFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("date", dateFieldDefinition));

        return registry;
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        CompositeFieldDefinition definition = new CompositeFieldDefinition();
        definition = (CompositeFieldDefinition) AbstractFieldFactoryTest.createConfiguredFieldDefinition(definition, propertyName);
        definition.setDefaultValue(null);

        // Set fields
        textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition.setRows(0);
        textFieldDefinition.setName("text");

        dateFieldDefinition = new DateFieldDefinition();
        dateFieldDefinition.setName("date");

        List<ConfiguredFieldDefinition> fields = newArrayList(textFieldDefinition, dateFieldDefinition);
        definition.setFields(fields);

        this.definition = definition;
    }
}
