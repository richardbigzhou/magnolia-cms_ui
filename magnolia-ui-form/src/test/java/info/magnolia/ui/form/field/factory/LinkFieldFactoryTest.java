/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import static org.mockito.Mockito.*;

import info.magnolia.ui.form.field.LinkField;
import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.validator.definition.FieldValidatorDefinition;
import info.magnolia.ui.form.validator.definition.RegexpValidatorDefinition;
import info.magnolia.ui.form.validator.factory.RegexpFieldValidatorFactory;
import info.magnolia.ui.form.validator.registry.FieldValidatorFactoryFactory;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.Field;

/**
 * Main testcase for {@link info.magnolia.ui.form.field.factory.LinkFieldFactory}.
 */
public class LinkFieldFactoryTest extends AbstractFieldFactoryTestCase<LinkFieldDefinition> {

    private LinkFieldFactory<LinkFieldDefinition> linkFieldFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        componentProvider.setInstance(ContentConnector.class, mock(JcrContentConnector.class));
    }

    @Test
    public void simpleLinkFieldTest() throws Exception {
        // GIVEN
        setFactory(null);
        // WHEN
        Field<String> field = linkFieldFactory.createField();

        // THEN
        assertEquals(true, field instanceof LinkField);
    }

    @Test
    public void simpleLinkFieldUuidTest() throws Exception {
        // GIVEN
        definition.setIdentifierToPathConverter(new BaseIdentifierToPathConverter());
        definition.setName(propertyName);
        definition.setTargetWorkspace(workspaceName);
        baseNode.setProperty(propertyName, baseNode.getIdentifier());
        baseItem = new JcrNodeAdapter(baseNode);
        setFactory(null);

        // WHEN
        Field<String> field = linkFieldFactory.createField();

        // THEN
        assertEquals(true, field instanceof LinkField);
        // Propert way set to the UUID baseNode.getIdentifier() and we display the path
        assertEquals(baseNode.getPath(), field.getValue());
    }


    @Test
    public void linkField_SetFieldPropagation() throws Exception {
        // GIVEN
        definition.setName(propertyName);
        baseNode.setProperty(propertyName, "notChanged");
        baseItem = new JcrNodeAdapter(baseNode);
        setFactory(null);
        Field<String> field = linkFieldFactory.createField();
        assertEquals("notChanged", ((LinkField) field).getTextField().getValue());
        // WHEN
        ((LinkField) field).getTextField().setValue("Changed");

        // THEN
        assertEquals("Changed", baseItem.getItemProperty(propertyName).getValue());
    }

    @Test
    public void linkFieldRequired() throws Exception {
        // GIVEN
        definition.setName(propertyName);
        definition.setRequired(true);
        baseNode.setProperty(propertyName, "notChanged");
        baseItem = new JcrNodeAdapter(baseNode);
        setFactory(null);
        Field<String> field = linkFieldFactory.createField();
        assertEquals("notChanged", ((LinkField) field).getTextField().getValue());
        // WHEN
        ((LinkField) field).setValue(null);
        ErrorMessage errorMessage = ((LinkField) field).getErrorMessage();

        // THEN
        assertNotNull(errorMessage);
    }

    @Test
    public void linkFieldUsesValidator() throws Exception {
        // GIVEN
        baseNode.setProperty(propertyName, "notChanged");
        baseItem = new JcrNodeAdapter(baseNode);

        FieldValidatorFactoryFactory validatorFactory = mock(FieldValidatorFactoryFactory.class);
        RegexpValidatorDefinition regexpValidator = new RegexpValidatorDefinition();
        regexpValidator.setPattern("/demo-project(.*)");
        regexpValidator.setErrorMessage("error {}");
        RegexpFieldValidatorFactory regExpValidator = new RegexpFieldValidatorFactory(regexpValidator);
        when(validatorFactory.createFieldValidatorFactory(regexpValidator, baseItem)).thenReturn(regExpValidator);

        definition.setName(propertyName);
        definition.setValidators(Arrays.asList((FieldValidatorDefinition) regexpValidator));
        definition.setRequired(true);

        setFactory(validatorFactory);
        Field<String> field = linkFieldFactory.createField();
        assertEquals("notChanged", ((LinkField) field).getTextField().getValue());
        // WHEN
        ((LinkField) field).setValue("someValue");
        ErrorMessage errorMessage = ((LinkField) field).getErrorMessage();

        // THEN
        assertNotNull(errorMessage);
    }

    /**
     * @param validatorFactory
     */
    protected void setFactory(FieldValidatorFactoryFactory validatorFactory) {
        linkFieldFactory = new LinkFieldFactory<LinkFieldDefinition>(definition, baseItem, null, null, null);
        linkFieldFactory.setFieldValidatorFactoryFactory(validatorFactory);
        linkFieldFactory.setComponentProvider(componentProvider);
    }

    @Override
    protected void createConfiguredFieldDefinition() {
        LinkFieldDefinition fieldDefinition = new LinkFieldDefinition();
        fieldDefinition.setName(propertyName);
        fieldDefinition.setFieldEditable(true);
        this.definition = fieldDefinition;
    }


}
