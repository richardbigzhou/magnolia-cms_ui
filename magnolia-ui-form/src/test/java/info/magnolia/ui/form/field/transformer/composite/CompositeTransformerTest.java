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
package info.magnolia.ui.form.field.transformer.composite;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.PropertysetItem;

/**
 * .
 */
public class CompositeTransformerTest extends RepositoryTestCase {
    private Node rootNode;
    private final String fieldName = "fieldName";
    private List<String> fieldsName = Arrays.asList("field1", "field2", "field3");
    private CompositeFieldDefinition definition = new CompositeFieldDefinition();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init parent Node
        String nodeProperties =
                "/parent.@type=mgnl:content\n" +
                        "/parent.fieldNamefield2=hello2\n";

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(nodeProperties));
        session.save();
        definition.setName(fieldName);

        rootNode = session.getRootNode().getNode("parent");
    }

    @Test
    public void testWriteMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        CompositeTransformer delegate = new CompositeTransformer(parent, definition, PropertysetItem.class, fieldsName, mock(I18NAuthoringSupport.class));
        PropertysetItem itemSet = new PropertysetItem();
        itemSet.addItemProperty("field1", new DefaultProperty<String>("hello1"));

        // WHEN
        delegate.writeToItem(itemSet);
        parent.applyChanges();

        // THEN
        assertTrue(rootNode.hasProperty("fieldNamefield1"));
        assertEquals("hello1", rootNode.getProperty("fieldNamefield1").getString());
        assertTrue(rootNode.hasProperty("fieldNamefield2"));
        assertEquals("hello2", rootNode.getProperty("fieldNamefield2").getString());
    }

    @Test
    public void testWriteMultiPropertyWithNullValue() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        CompositeTransformer delegate = new CompositeTransformer(parent, definition, PropertysetItem.class, fieldsName, mock(I18NAuthoringSupport.class));
        PropertysetItem itemSet = new PropertysetItem();
        itemSet.addItemProperty("field1", null);

        // WHEN
        delegate.writeToItem(itemSet);
        parent.applyChanges();

        // THEN
        assertFalse(rootNode.hasProperty("fieldNamefield1"));
        assertTrue(rootNode.hasProperty("fieldNamefield2"));
        assertEquals("hello2", rootNode.getProperty("fieldNamefield2").getString());
    }

    @Test
    public void testReadMultiProperty() throws RepositoryException {
        // GIVEN

        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        CompositeTransformer delegate = new CompositeTransformer(parent, definition, PropertysetItem.class, fieldsName, mock(I18NAuthoringSupport.class));

        // WHEN
        PropertysetItem res = delegate.readFromItem();

        // THEN
        assertNotNull(res.getItemProperty("field2"));
        assertEquals("hello2", res.getItemProperty("field2").getValue());

    }

    @Test
    public void testReadWriteMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        CompositeTransformer delegate = new CompositeTransformer(parent, definition, PropertysetItem.class, fieldsName, mock(I18NAuthoringSupport.class));

        // WHEN
        PropertysetItem itemSet = delegate.readFromItem();
        itemSet.addItemProperty("field1", new DefaultProperty<String>("hello1"));
        itemSet.getItemProperty("field2").setValue(null);
        delegate.writeToItem(itemSet);
        parent.applyChanges();

        // THEN
        assertTrue(rootNode.hasProperty("fieldNamefield1"));
        assertEquals("hello1", rootNode.getProperty("fieldNamefield1").getString());
        assertFalse(rootNode.hasProperty("fieldNamefield2"));
    }

}
