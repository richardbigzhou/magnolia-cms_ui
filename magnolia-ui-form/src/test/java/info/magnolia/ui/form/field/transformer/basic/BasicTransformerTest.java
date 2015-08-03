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
package info.magnolia.ui.form.field.transformer.basic;

import static info.magnolia.test.hamcrest.ExceptionMatcher.instanceOf;
import static info.magnolia.test.hamcrest.ExecutionMatcher.throwsAnException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.hamcrest.Execution;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property.ReadOnlyException;

/**
 * .
 */
public class BasicTransformerTest extends RepositoryTestCase {
    private Node rootNode;
    private final String propertyName = "propertyName";
    private ConfiguredFieldDefinition definition = new ConfiguredFieldDefinition();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        definition.setName(propertyName);
        rootNode = session.getRootNode().addNode("parent", NodeTypes.Content.NAME);
        session.save();
    }

    @Test
    public void testReadFromDataSourceItemStringWithoutDefault() throws RepositoryException {
        // GIVEN
        definition.setType("String");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<String> handler = new BasicTransformer<String>(rootItem, definition, String.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNull(value);
    }

    @Test
    public void testReadFromDataSourceItemString() throws RepositoryException {
        // GIVEN
        definition.setType("String");
        definition.setDefaultValue("defaultStringValue");
        rootNode.setProperty(propertyName, "stringValue");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<String> handler = new BasicTransformer<String>(rootItem, definition, String.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals(rootNode.getProperty(propertyName).getString(), value);
        assertNotNull(rootItem.getItemProperty(propertyName));
        assertEquals(String.class, rootItem.getItemProperty(propertyName).getType());
        assertEquals(rootNode.getProperty(propertyName).getString(), rootItem.getItemProperty(propertyName).getValue());
    }


    @Test
    public void testReadFromDataSourceItemLongWithoutDefault() throws RepositoryException {
        // GIVEN
        definition.setType("Long");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Long> handler = new BasicTransformer<Long>(rootItem, definition, Long.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNull(value);
    }

    @Test
    public void testReadFromDataSourceItemLong() throws RepositoryException {
        // GIVEN
        definition.setType("Long");
        definition.setDefaultValue("100");
        rootNode.setProperty(propertyName, 200l);
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Long> handler = new BasicTransformer<Long>(rootItem, definition, Long.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNotNull(value);
        assertTrue(value instanceof Long);
        assertEquals(rootNode.getProperty(propertyName).getLong(), value);
        assertNotNull(rootItem.getItemProperty(propertyName));
        assertEquals(Long.class, rootItem.getItemProperty(propertyName).getType());
        assertEquals(rootNode.getProperty(propertyName).getLong(), rootItem.getItemProperty(propertyName).getValue());
    }

    @Test
    public void testReadFromDataSourceItemReadOnly() throws RepositoryException {
        // GIVEN
        definition.setType("String");
        definition.setDefaultValue("defaultStringValue");
        definition.setReadOnly(true);
        rootNode.setProperty(propertyName, "stringValue");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<String> handler = new BasicTransformer<String>(rootItem, definition, String.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNotNull(value);
        assertTrue(value instanceof String);
        assertEquals(rootNode.getProperty(propertyName).getString(), value);
        assertNotNull(rootItem.getItemProperty(propertyName));
        assertEquals(String.class, rootItem.getItemProperty(propertyName).getType());
        assertEquals(rootNode.getProperty(propertyName).getString(), rootItem.getItemProperty(propertyName).getValue());
        assertTrue(new TransformedProperty(handler).isReadOnly());
    }

    @Test
    public void testWriteToDataSourceItemNewString() throws RepositoryException {
        // GIVEN
        definition.setType("String");
        definition.setDefaultValue("defaultStringValue");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<String> handler = new BasicTransformer<String>(rootItem, definition, String.class, mock(I18NAuthoringSupport.class));
        handler.readFromItem();

        // WHEN
        handler.writeToItem("newValue");

        // THEN
        Node res = rootItem.applyChanges();
        assertTrue(res.hasProperty(propertyName));
        assertEquals("newValue", res.getProperty(propertyName).getString());
    }

    @Test
    public void testWriteToDataSourceItemString() throws RepositoryException {
        // GIVEN
        definition.setType("String");
        definition.setDefaultValue("defaultStringValue");
        rootNode.setProperty(propertyName, "stringValue");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<String> handler = new BasicTransformer<String>(rootItem, definition, String.class, mock(I18NAuthoringSupport.class));
        handler.readFromItem();

        // WHEN
        handler.writeToItem("newValue");

        // THEN
        Node res = rootItem.applyChanges();
        assertTrue(res.hasProperty(propertyName));
        assertEquals("newValue", res.getProperty(propertyName).getString());
    }

    @Test
    public void testWriteToDataSourceItemNewLong() throws RepositoryException {
        // GIVEN
        definition.setType("Long");
        definition.setDefaultValue("100");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Long> handler = new BasicTransformer<Long>(rootItem, definition, Long.class, mock(I18NAuthoringSupport.class));
        handler.readFromItem();

        // WHEN
        handler.writeToItem(200l);

        // THEN
        Node res = rootItem.applyChanges();
        assertTrue(res.hasProperty(propertyName));
        assertEquals(200l, res.getProperty(propertyName).getLong());
    }

    @Test
    public void testWriteToDataSourceItemLong() throws RepositoryException {
        // GIVEN
        definition.setType("Long");
        definition.setDefaultValue("100");
        rootNode.setProperty(propertyName, 120l);
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Long> handler = new BasicTransformer<Long>(rootItem, definition, Long.class, mock(I18NAuthoringSupport.class));
        handler.readFromItem();

        // WHEN
        handler.writeToItem(200l);

        // THEN
        Node res = rootItem.applyChanges();
        assertTrue(res.hasProperty(propertyName));
        assertEquals(200l, res.getProperty(propertyName).getLong());
    }

    @Test
    public void testWriteToDataSourceReadOnly() throws RepositoryException {
        // GIVEN
        definition.setType("String");
        definition.setDefaultValue("defaultStringValue");
        definition.setReadOnly(true);
        rootNode.setProperty(propertyName, "stringValue");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        final BasicTransformer<String> transformer = new BasicTransformer<String>(rootItem, definition, String.class, mock(I18NAuthoringSupport.class));
        transformer.readFromItem();

        // WHEN/THEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                new TransformedProperty(transformer).setValue("newValue");
            }
        }, throwsAnException(instanceOf(ReadOnlyException.class)));
    }

    @Test
    public void testReadFromDataSourceWrongType() throws RepositoryException {
        // GIVEN
        definition.setType("Boolean");
        rootNode.setProperty(propertyName, "false");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Boolean> handler = new BasicTransformer<Boolean>(rootItem, definition, Boolean.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNotNull(value);
        assertTrue("The value is of the definition type", value instanceof Boolean);
        assertEquals("Keep the original JCR value", Boolean.FALSE, value);
        assertEquals(Boolean.class, rootItem.getItemProperty(propertyName).getType());
        assertEquals(Boolean.FALSE, rootItem.getItemProperty(propertyName).getValue());

    }

    @Test
    public void testReadFromDataSourceWrongTypeEmptyValue() throws RepositoryException {
        // GIVEN
        definition.setType("Boolean");
        rootNode.setProperty(propertyName, "");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Boolean> handler = new BasicTransformer<Boolean>(rootItem, definition, Boolean.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNull(value);
        assertEquals(Boolean.class, rootItem.getItemProperty(propertyName).getType());
        assertEquals(null, rootItem.getItemProperty(propertyName).getValue());
    }

    @Test
    public void testReadFromDataSourceWrongTypeIncompatibleValue() throws RepositoryException {
        // GIVEN
        definition.setType("Long");
        rootNode.setProperty(propertyName, "titi");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Long> handler = new BasicTransformer<Long>(rootItem, definition, Long.class, mock(I18NAuthoringSupport.class));

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNull(value);
        assertEquals(Long.class, rootItem.getItemProperty(propertyName).getType());
        assertEquals(null, rootItem.getItemProperty(propertyName).getValue());
    }

    @Test
    public void testWriteToDataSourceWrongType() throws RepositoryException {
        // GIVEN
        definition.setType("Boolean");
        rootNode.setProperty(propertyName, "false");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Boolean> handler = new BasicTransformer<Boolean>(rootItem, definition, Boolean.class, mock(I18NAuthoringSupport.class));
        handler.readFromItem();

        // WHEN
        handler.writeToItem(true);

        // THEN
        Node res = rootItem.applyChanges();
        assertTrue(res.hasProperty(propertyName));
        assertEquals("Property was String and is now Boolean", PropertyType.BOOLEAN, res.getProperty(propertyName).getType());
        assertEquals(true, res.getProperty(propertyName).getBoolean());
    }

    @Test
    public void testWriteToDataSourceWrongTypeIncompatibleValue() throws RepositoryException {
        // GIVEN
        definition.setType("Long");
        rootNode.setProperty(propertyName, "titi");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        BasicTransformer<Long> handler = new BasicTransformer<Long>(rootItem, definition, Long.class, mock(I18NAuthoringSupport.class));
        handler.readFromItem();

        // WHEN
        handler.writeToItem(10l);

        // THEN
        Node res = rootItem.applyChanges();
        assertTrue(res.hasProperty(propertyName));
        assertEquals("Property was String and is now Long", PropertyType.LONG, res.getProperty(propertyName).getType());
        assertEquals(10l, res.getProperty(propertyName).getLong());
    }

}
