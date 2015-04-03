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
package info.magnolia.ui.form.field.transformer;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.util.converter.Converter.ConversionException;

/**
 * Test class based on {@link BasicTransformer}.
 */
public class TransformedPropertyTest extends RepositoryTestCase {

    private Node relatedFormNode;
    private final String propertyName = "propertyName";
    private Item relatedFormItem;
    private ConfiguredFieldDefinition definition = new ConfiguredFieldDefinition();
    private BasicTransformer transformer;
    private TransformedProperty property;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        definition.setName(propertyName);
        relatedFormNode = session.getRootNode().addNode("form", NodeTypes.Content.NAME);
        session.save();
        relatedFormItem = new JcrNodeAdapter(relatedFormNode);
    }

    @Test
    public void testInitializationNullValueAndTypeSetByFactory() {
        // GIVEN
        transformer = new BasicTransformer(relatedFormItem, definition, Long.class);

        // WHEN
        property = new TransformedProperty(transformer);

        // THEN
        // Set value with the Type --> ok
        assertEquals(Long.class, property.getType());
    }

    @Test(expected = ConversionException.class)
    public void testInitializationNullValueAndTypeSetByFactoryException() {
        // GIVEN
        transformer = new BasicTransformer(relatedFormItem, definition, Long.class);

        // WHEN
        property = new TransformedProperty(transformer);

        // THEN
        // Set a value of another type --> exception
        property.setValue("titi");
    }

    @Test
    public void testInitializationNullValueTypeNotSetByFactory() {
        // GIVEN
        transformer = new BasicTransformer(relatedFormItem, definition, UndefinedPropertyType.class);

        // WHEN
        property = new TransformedProperty(transformer);

        // THEN
        // Type is of the default Transformer type 'String'
        assertEquals(String.class, property.getType());
    }

    @Test
    public void testInitializationNotNullValueTypeNotSetByFactory() throws RepositoryException {
        // GIVEN
        relatedFormNode.setProperty(propertyName, 200l);
        relatedFormItem = new JcrNodeAdapter(relatedFormNode);
        transformer = new BasicTransformer(relatedFormItem, definition, UndefinedPropertyType.class);

        // WHEN
        property = new TransformedProperty(transformer);

        // THEN
        // Type equal of the value type
        assertEquals(Long.class, property.getType());
        assertEquals(200l, property.getValue());
    }

    @Test
    public void testGetValue() throws RepositoryException {
        // GIVEN
        relatedFormNode.setProperty(propertyName, "propertyValue");
        relatedFormItem = new JcrNodeAdapter(relatedFormNode);
        transformer = new BasicTransformer(relatedFormItem, definition, UndefinedPropertyType.class);

        // WHEN
        property = new TransformedProperty(transformer);

        // THEN
        // Value = Transformer.readFromItem
        assertEquals("propertyValue", property.getValue());
        assertEquals(transformer.readFromItem(), property.getValue());
    }

    @Test
    public void testSetValue() throws RepositoryException {
        // GIVEN
        relatedFormNode.setProperty(propertyName, Boolean.TRUE);
        relatedFormItem = new JcrNodeAdapter(relatedFormNode);
        transformer = new BasicTransformer(relatedFormItem, definition, UndefinedPropertyType.class);
        property = new TransformedProperty(transformer);

        // WHEN
        property.setValue(Boolean.FALSE);

        // THEN
        // Item.getProperty() = newValue
        assertEquals(Boolean.FALSE, relatedFormItem.getItemProperty(propertyName).getValue());
        // getValue() = newValue
        assertEquals(Boolean.FALSE, property.getValue());
        // Transformer.readFromItem = newValue
        assertEquals(Boolean.FALSE, transformer.readFromItem());
    }

    @Test
    public void testFireI18NValueChange() throws RepositoryException {
        // GIVEN
        relatedFormNode.setProperty(propertyName, "enPropertyName");
        relatedFormItem = new JcrNodeAdapter(relatedFormNode);
        definition.setI18n(true);
        transformer = new BasicTransformer(relatedFormItem, definition, UndefinedPropertyType.class);
        property = new TransformedProperty(transformer);
        property.getTransformer().setI18NPropertyName(propertyName + "_de");
        property.getTransformer().setLocale(new Locale("de"));

        // WHEN
        property.fireI18NValueChange();

        // THEN
        property.setValue("dePropertyName");

        assertNotNull(relatedFormItem.getItemProperty(propertyName + "_de"));
        assertEquals("dePropertyName", property.getValue());
        assertNotNull(relatedFormItem.getItemProperty(propertyName));
        assertEquals("enPropertyName", relatedFormItem.getItemProperty(propertyName).getValue());
    }

    @Test
    public void testFireI18NValueChangeWithReadOnlyTrue() throws RepositoryException {
        // GIVEN
        relatedFormNode.setProperty(propertyName, "enPropertyName");
        relatedFormNode.setProperty(propertyName + "_de", "dePropertyName");
        relatedFormItem = new JcrNodeAdapter(relatedFormNode);
        definition.setI18n(true);
        definition.setReadOnly(true);
        transformer = new BasicTransformer(relatedFormItem, definition, UndefinedPropertyType.class);
        property = new TransformedProperty(transformer);
        property.getTransformer().setI18NPropertyName(propertyName + "_de");
        property.getTransformer().setLocale(new Locale("de"));
        property.setReadOnly(true);

        // WHEN
        property.fireI18NValueChange();

        // THEN
        assertEquals("dePropertyName", property.getValue());
    }
}
