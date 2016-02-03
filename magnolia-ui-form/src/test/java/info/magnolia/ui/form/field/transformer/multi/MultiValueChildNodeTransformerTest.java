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
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.jcr.wrapper.JCRPropertiesFilteringNodeWrapper;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
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
 * Test.
 */
public class MultiValueChildNodeTransformerTest extends RepositoryTestCase {
    private Node rootNode;
    private final String propertyName = "sites";
    private MultiValueFieldDefinition definition = new MultiValueFieldDefinition();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init parent Node
        String nodeProperties =
                "/parent.@type=mgnl:content\n" +
                        "/parent.propertyString=hello\n" +
                        "/parent/sites.@type=mgnl:content\n" +
                        "/parent/sites.0=/demo-project/about\n" +
                        "/parent/sites.1=/demo-features/content-templates\n";

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        new PropertiesImportExport().createNodes(session.getRootNode(), IOUtils.toInputStream(nodeProperties));
        session.save();
        definition.setName(propertyName);

        rootNode = session.getRootNode().getNode("parent");

        assertTrue(rootNode.getNode("sites").hasProperty("jcr:createdBy"));
    }

    @Test
    public void testWriteToItem() throws RepositoryException {
        // GIVEN
        rootNode.getNode(propertyName).remove();
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);

        PropertysetItem newValue = new PropertysetItem();
        newValue.addItemProperty(0, new ObjectProperty<String>("/xx/xxx"));
        newValue.addItemProperty(1, new ObjectProperty<String>("/yy"));

        MultiValueChildNodeTransformer transformer = new MultiValueChildNodeTransformer(parent, definition, PropertysetItem.class, mock(I18NAuthoringSupport.class));

        // WHEN
        transformer.writeToItem(newValue);

        // THEN
        assertNotNull(parent.getChild(propertyName));
        JcrNodeAdapter child = (JcrNodeAdapter) parent.getChild(propertyName);
        assertEquals(NodeTypes.ContentNode.NAME, child.getPrimaryNodeTypeName());
        assertEquals(2, child.getItemPropertyIds().size());
        assertNotNull(child.getItemProperty("0"));
        assertEquals("/xx/xxx", child.getItemProperty("0").getValue().toString());
        assertNotNull(child.getItemProperty("1"));
        assertEquals("/yy", child.getItemProperty("1").getValue().toString());
    }

    @Test
    public void testWriteToItemUpdateOfExistingProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);

        PropertysetItem newValue = new PropertysetItem();
        newValue.addItemProperty(0, new ObjectProperty<String>("/xx/xxx"));

        MultiValueChildNodeTransformer transformer = new MultiValueChildNodeTransformer(parent, definition, PropertysetItem.class, mock(I18NAuthoringSupport.class));

        // WHEN
        transformer.writeToItem(newValue);

        // THEN
        parent.applyChanges();
        assertNotNull(rootNode.hasNode(propertyName));
        Node child = new JCRPropertiesFilteringNodeWrapper(rootNode.getNode(propertyName));
        assertTrue(child.hasProperty("0"));
        assertEquals("/xx/xxx", child.getProperty("0").getString());
    }

    @Test
    public void testReadFromItem() throws RepositoryException {
        // GIVEN
        rootNode.getNode(propertyName).remove();
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);

        MultiValueChildNodeTransformer transformer = new MultiValueChildNodeTransformer(parent, definition, PropertysetItem.class, mock(I18NAuthoringSupport.class));

        // WHEN
        PropertysetItem res = transformer.readFromItem();

        // THEN
        assertNotNull(res);
        assertTrue(res.getItemPropertyIds().isEmpty());
    }

    @Test
    public void testReadFromItemExistingProperty() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);

        MultiValueChildNodeTransformer transformer = new MultiValueChildNodeTransformer(parent, definition, PropertysetItem.class, mock(I18NAuthoringSupport.class));

        // WHEN
        PropertysetItem res = transformer.readFromItem();

        // THEN
        assertNotNull(res);
        assertFalse(res.getItemPropertyIds().isEmpty());
        assertEquals(2, res.getItemPropertyIds().size());
        assertNotNull(res.getItemProperty(0));
        assertEquals("/demo-project/about", res.getItemProperty(0).getValue().toString());
        assertNotNull(res.getItemProperty(1));
        assertEquals("/demo-features/content-templates", res.getItemProperty(1).getValue().toString());
    }

    @Test
    public void testReadFromItemWriteToItem() throws RepositoryException {
        // GIVEN
        JcrNodeAdapter parent = new JcrNodeAdapter(rootNode);
        MultiValueChildNodeTransformer transformer = new MultiValueChildNodeTransformer(parent, definition, PropertysetItem.class, mock(I18NAuthoringSupport.class));

        // Read
        PropertysetItem res = transformer.readFromItem();
        assertEquals(2, res.getItemPropertyIds().size());
        // Add a new one
        res.addItemProperty(3, new ObjectProperty<String>("/yy"));
        // Remove one
        res.removeItemProperty(0);

        // WHEN
        transformer.writeToItem(res);

        // THEN
        parent.applyChanges();
        assertNotNull(rootNode.hasNode(propertyName));
        Node child = new JCRPropertiesFilteringNodeWrapper(rootNode.getNode(propertyName));
        // in the meantime mgnl:created, mgnl:createdBy, mgnl:lastUpdate & mgnl:lastUpdateBy have been set
        assertEquals(6, child.getProperties().getSize());
        assertFalse(child.hasProperty("0"));
        assertTrue(child.hasProperty("3"));
        assertEquals("/yy", child.getProperty("3").getString());
        assertTrue(child.hasProperty("1"));
        assertEquals("/demo-features/content-templates", child.getProperty("1").getString());
    }
}
