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
package info.magnolia.ui.form.field.transformer.multi;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * .
 */
public class MultiValueChildrenNodeTransformerTest extends RepositoryTestCase {
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
                        "/parent/00.@type=mgnl:content\n" +
                        "/parent/00.property=value1\n" +
                        "/parent/11.@type=mgnl:content\n" +
                        "/parent/11.property=value2\n";

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
        MultiValueChildrenNodeTransformer delegate = new MultiValueChildrenNodeTransformer(parent, definition, PropertysetItem.class);
        PropertysetItem properties = new PropertysetItem();
        properties.addItemProperty("0", new ObjectProperty<String>("value1"));
        properties.addItemProperty("1", null);
        // WHEN
        delegate.writeToItem(properties);

        // THEN
        assertTrue(parent.getItemProperty(propertyName) == null);
        assertTrue(!parent.applyChanges().hasNode(propertyName));
    }

    @Test
    public void testReadMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueChildrenNodeTransformer delegate = new MultiValueChildrenNodeTransformer(parent, definition, PropertysetItem.class);

        // WHEN
        PropertysetItem res = delegate.readFromItem();

        // THEN
        assertEquals(2, res.getItemPropertyIds().size());
        assertEquals("value1", res.getItemProperty(0).getValue());
        assertEquals("value2", res.getItemProperty(1).getValue());

    }

    @Test
    public void testUpdateMultiPropertyWithoutChanges() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueChildrenNodeTransformer delegate = new MultiValueChildrenNodeTransformer(parent, definition, PropertysetItem.class);
        // Set the same values
        PropertysetItem input = new PropertysetItem();
        input.addItemProperty(0, new ObjectProperty<String>("value1"));
        input.addItemProperty(1, new ObjectProperty<String>("value2"));
        delegate.writeToItem(input);

        // WHEN
        parent.applyChanges();

        // THEN
        NodeIterator iterator = parent.getJcrItem().getNodes();
        assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            assertTrue(node.hasProperty(propertyName));
            assertTrue(node.getProperty(propertyName).getString().startsWith("value"));
        }
    }

    @Test
    public void testUpdateMultiProperty() throws RepositoryException {
        // GIVEN
        String[] newValues = { "Pig", "Ph", "Po" };
        PropertysetItem input = new PropertysetItem();
        input.addItemProperty(0, new ObjectProperty<String>("Pig"));
        input.addItemProperty(1, new ObjectProperty<String>("Ph"));
        input.addItemProperty(2, new ObjectProperty<String>("Po"));

        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueChildrenNodeTransformer delegate = new MultiValueChildrenNodeTransformer(parent, definition, PropertysetItem.class);

        // WHEN
        delegate.writeToItem(input);
        parent.applyChanges();

        // THEN
        NodeIterator iterator = parent.getJcrItem().getNodes();
        assertTrue(iterator.hasNext());
        assertEquals("Pig", iterator.nextNode().getProperty(propertyName).getString());
        assertEquals("Ph", iterator.nextNode().getProperty(propertyName).getString());
        assertEquals("Po", iterator.nextNode().getProperty(propertyName).getString());
    }
}
