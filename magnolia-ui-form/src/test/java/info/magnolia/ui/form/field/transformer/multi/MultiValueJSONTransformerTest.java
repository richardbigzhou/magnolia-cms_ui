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
package info.magnolia.ui.form.field.transformer.multi;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.Property;
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
public class MultiValueJSONTransformerTest extends RepositoryTestCase {
    private Node rootNode;
    private final String propertyName = "propertyName";
    private MultiValueFieldDefinition definition = new MultiValueFieldDefinition();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init parent Node
        String nodeProperties =
                "/parent.@type=mgnl:content\n" +
                        "/parent.propertyString=hello\n" +
                        "/parent/child.@type=mgnl:content\n" +
                        "/parent/child.propertyString=chield1\n";
        ComponentsTestUtil.setInstance(I18NAuthoringSupport.class, mock(I18NAuthoringSupport.class));
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
        MultiValueJSONTransformer delegate = new MultiValueJSONTransformer(parent, definition, PropertysetItem.class);
        PropertysetItem input = new PropertysetItem();
        input.addItemProperty(0, new ObjectProperty<String>("Jav"));
        input.addItemProperty(1, new ObjectProperty<String>("ta"));

        // WHEN
        delegate.writeToItem(input);

        // THEN
        assertTrue(parent.getItemProperty(propertyName) != null);
        assertTrue(parent.getItemProperty(propertyName).getValue() instanceof String);
        Node parentNode = parent.applyChanges();
        assertTrue(parentNode.hasProperty(propertyName));
        assertTrue(!parentNode.getProperty(propertyName).isMultiple());
        assertEquals("Jav,ta", parentNode.getProperty(propertyName).getValue().getString());
    }

    @Test
    public void testReadMultiProperty() throws RepositoryException {
        // GIVEN
        String values = "Art,Dan,Jen";
        rootNode.setProperty(propertyName, values);
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueJSONTransformer delegate = new MultiValueJSONTransformer(parent, definition, PropertysetItem.class);

        // WHEN
        PropertysetItem res = delegate.readFromItem();

        // THEN
        assertEquals(3, res.getItemPropertyIds().size());
        assertEquals("Art", res.getItemProperty(0).getValue());
        assertEquals("Dan", res.getItemProperty(1).getValue());
        assertEquals("Jen", res.getItemProperty(2).getValue());
    }

    @Test
    public void testReadEmptyMultiProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueJSONTransformer delegate = new MultiValueJSONTransformer(parent, definition, PropertysetItem.class);

        // WHEN
        PropertysetItem res = delegate.readFromItem();

        // THEN
        assertEquals(0, res.getItemPropertyIds().size());
    }

    @Test
    public void testUpdateMultiProperty() throws RepositoryException {
        // GIVEN
        String initialValues = "Art,Dan,Jen";
        PropertysetItem newValues = new PropertysetItem();
        newValues.addItemProperty(0, new ObjectProperty<String>("Pig"));
        newValues.addItemProperty(1, new ObjectProperty<String>("Ph"));

        rootNode.setProperty(propertyName, initialValues);
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueJSONTransformer delegate = new MultiValueJSONTransformer(parent, definition, PropertysetItem.class);

        // WHEN
        delegate.writeToItem(newValues);

        // THEN
        assertTrue(parent.getItemProperty(propertyName) != null);
        assertTrue(parent.getItemProperty(propertyName).getValue() instanceof String);
        Node parentNode = parent.applyChanges();
        assertTrue(parentNode.hasProperty(propertyName));
        Property p = parentNode.getProperty(propertyName);
        assertTrue(!p.isMultiple());
        assertEquals("Pig,Ph", p.getValue().getString());
    }
}
