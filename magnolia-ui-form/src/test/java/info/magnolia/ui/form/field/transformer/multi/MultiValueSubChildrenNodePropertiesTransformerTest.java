/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.form.field.transformer.multi;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * Main test class for {@link MultiValueSubChildrenNodePropertiesTransformer}.
 */
public class MultiValueSubChildrenNodePropertiesTransformerTest extends RepositoryTestCase {
    private Node rootNode;
    private final String propertyName = "property";
    private MultiValueFieldDefinition definition = new MultiValueFieldDefinition();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init parent Node
        String nodeProperties =
                "/parent.@type=mgnl:content\n" +
                        "/parent.propertyString=hello\n" +
                        "/parent/property.@type=mgnl:content\n" +
                        "/parent/property/00.@type=mgnl:content\n" +
                        "/parent/property/00.property1=value11\n" +
                        "/parent/property/00.property2=value12\n" +
                        "/parent/property/11.@type=mgnl:content\n" +
                        "/parent/property/11.property1=value21\n" +
                        "/parent/property/11.property2=value22\n";

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(nodeProperties));
        session.save();
        definition.setName(propertyName);

        rootNode = session.getRootNode().getNode("parent");
    }

    @Test
    public void testCreateMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueSubChildrenNodePropertiesTransformer delegate = new MultiValueSubChildrenNodePropertiesTransformer(parent, definition, PropertysetItem.class);
        PropertysetItem mainProperties = new PropertysetItem();
        PropertysetItem values1 = new PropertysetItem();
        values1.addItemProperty("property1", new ObjectProperty<String>("value1"));
        values1.addItemProperty("property2", null);
        mainProperties.addItemProperty("00", new ObjectProperty<PropertysetItem>(values1));
        // WHEN
        delegate.writeToItem(mainProperties);

        // THEN
        assertTrue(parent.getItemProperty(propertyName) == null);
        Node res = parent.applyChanges();
        assertTrue(res.hasNode("property"));
        assertTrue(res.hasNode("property/00"));
        assertTrue(res.getNode("property/00").hasProperty("property1"));
        assertEquals("value1", res.getNode("property/00").getProperty("property1").getString());
        assertTrue(res.getNode("property/00").hasProperty("property2"));
        assertEquals("value12", res.getNode("property/00").getProperty("property2").getString());
    }

    @Test
    public void testReadMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueSubChildrenNodePropertiesTransformer delegate = new MultiValueSubChildrenNodePropertiesTransformer(parent, definition, PropertysetItem.class);

        // WHEN
        PropertysetItem res = delegate.readFromItem();

        // THEN
        assertEquals(2, res.getItemPropertyIds().size());
        assertEquals("value11", ((DefaultProperty<PropertysetItem>) res.getItemProperty(0)).getValue().getItemProperty("property1").getValue());
        assertEquals("value12", ((DefaultProperty<PropertysetItem>) res.getItemProperty(0)).getValue().getItemProperty("property2").getValue());
        assertEquals("value21", ((DefaultProperty<PropertysetItem>) res.getItemProperty(1)).getValue().getItemProperty("property1").getValue());
        assertEquals("value22", ((DefaultProperty<PropertysetItem>) res.getItemProperty(1)).getValue().getItemProperty("property2").getValue());

    }

    @Test
    public void testUpdateMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueSubChildrenNodePropertiesTransformer delegate = new MultiValueSubChildrenNodePropertiesTransformer(parent, definition, PropertysetItem.class);
        // Set the same values
        PropertysetItem mainProperties = new PropertysetItem();
        PropertysetItem values1 = new PropertysetItem();
        values1.addItemProperty("property1", new ObjectProperty<String>("value11Modified"));
        values1.addItemProperty("property2", new ObjectProperty<String>(""));
        mainProperties.addItemProperty("00", new ObjectProperty<PropertysetItem>(values1));
        PropertysetItem values2 = new PropertysetItem();
        values2.addItemProperty("property1", new ObjectProperty<String>("value21Modified"));
        values2.addItemProperty("property2", new ObjectProperty<String>("value22Modified"));
        mainProperties.addItemProperty("11", new ObjectProperty<PropertysetItem>(values2));

        delegate.writeToItem(mainProperties);

        // WHEN
        Node res = parent.applyChanges();

        // THEN
        assertTrue(res.hasNode("property"));
        assertTrue(res.hasNode("property/00"));
        assertTrue(res.getNode("property/00").hasProperty("property1"));
        assertEquals("value11Modified", res.getNode("property/00").getProperty("property1").getString());
        assertTrue(res.getNode("property/00").hasProperty("property2"));
        assertEquals("", res.getNode("property/00").getProperty("property2").getString());
        assertFalse(res.hasNode("property/11"));
        assertTrue(res.hasNode("property/01"));
        assertTrue(res.getNode("property/01").hasProperty("property1"));
        assertEquals("value21Modified", res.getNode("property/01").getProperty("property1").getString());
        assertTrue(res.getNode("property/01").hasProperty("property2"));
        assertEquals("value22Modified", res.getNode("property/01").getProperty("property2").getString());
    }


}
