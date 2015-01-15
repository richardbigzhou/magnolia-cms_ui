/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * Main test class for {@link DelegatingMultiValueFieldTransformer}.
 */
public class DelegatingMultiValueFieldTransformerTest {

    private MultiValueFieldDefinition definition = new MultiValueFieldDefinition();
    private Item rootItem;
    private MockNode rootNode;
    private String fieldName = "multi";
    private I18nContentSupport i18nContentSupport;
    private Locale defaultLocal = Locale.ENGLISH;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
        // Init definition
        TextFieldDefinition text = new TextFieldDefinition();
        text.setName("text");
        definition.setName(fieldName);
        definition.setI18n(false);

        // Init rootItem
        MockSession session = new MockSession("test");
        ((MockContext) MgnlContext.getInstance()).addSession("test", session);
        rootNode = new MockNode(session);
        rootNode.setName("root");
        rootNode.setPrimaryType(NodeTypes.ContentNode.NAME);
        rootNode.addNode(new MockNode("multi0", NodeTypes.ContentNode.NAME));
        rootNode.addNode(new MockNode("multi0_de", NodeTypes.ContentNode.NAME));
        rootNode.addNode(new MockNode("titi", NodeTypes.ContentNode.NAME));
        rootItem = new JcrNodeAdapter(rootNode);

        i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getDefaultLocale()).thenReturn(defaultLocal);
        when(i18nContentSupport.isEnabled()).thenReturn(false);

    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void readFromItem() {
        // GIVEN
        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);

        // WHEN
        PropertysetItem res = transformer.readFromItem();

        // THEN
        assertNotNull(res);
        assertEquals(1, res.getItemPropertyIds().size());
        assertNotNull(res.getItemProperty(0));
        Item subItem = (Item) res.getItemProperty(0).getValue();
        assertTrue(subItem instanceof JcrNodeAdapter);
        assertEquals(rootItem, ((JcrNodeAdapter) subItem).getParent());
        assertEquals(subItem, ((JcrNodeAdapter) rootItem).getChild("multi0"));
    }

    @Test
    public void readFromItemTwice() {
        // GIVEN
        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);

        // WHEN
        PropertysetItem res1 = transformer.readFromItem();
        PropertysetItem res2 = transformer.readFromItem();

        // THEN
        assertEquals(res1, res2);
    }

    @Test
    public void readFromItemWithI18nSupport() {
        // GIVEN
        when(i18nContentSupport.isEnabled()).thenReturn(true);
        definition.setI18n(true);
        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);

        // WHEN
        PropertysetItem res = transformer.readFromItem();

        // THEN
        assertNotNull(res);
        assertEquals(1, res.getItemPropertyIds().size());
        assertNotNull(res.getItemProperty(0));
        Item subItem = (Item) res.getItemProperty(0).getValue();
        assertTrue(subItem instanceof JcrNodeAdapter);
        assertEquals(rootItem, ((JcrNodeAdapter) subItem).getParent());
        assertEquals(subItem, ((JcrNodeAdapter) rootItem).getChild("multi0"));
    }

    @Test
    public void readFromItemWithI18nSupportNotDefaultLocal() {
        // GIVEN
        when(i18nContentSupport.isEnabled()).thenReturn(true);
        definition.setI18n(true);

        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);
        transformer.setI18NPropertyName("multi_de");

        // WHEN
        PropertysetItem res = transformer.readFromItem();

        // THEN
        assertNotNull(res);
        assertEquals(1, res.getItemPropertyIds().size());
        assertNotNull(res.getItemProperty(0));
        Item subItem = (Item) res.getItemProperty(0).getValue();
        assertTrue(subItem instanceof JcrNodeAdapter);
        assertEquals(rootItem, ((JcrNodeAdapter) subItem).getParent());
        assertEquals(subItem, ((JcrNodeAdapter) rootItem).getChild("multi0_de"));
    }

    @Test
    public void createNewElement() {
        // GIVEN
        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);
        transformer.readFromItem();
        // WHEN
        Property<?> res = transformer.createProperty();

        // THEN
        assertNotNull(res);
        assertTrue(res.getValue() instanceof JcrNewNodeAdapter);
        assertEquals("multi1", ((JcrNewNodeAdapter) res.getValue()).getNodeName());
        assertEquals(rootItem, ((JcrNodeAdapter) res.getValue()).getParent());
        assertEquals(res.getValue(), ((JcrNodeAdapter) rootItem).getChild("multi1"));
    }

    @Test
    public void createNewElementWithI18nSupport() {
        // GIVEN
        when(i18nContentSupport.isEnabled()).thenReturn(true);
        definition.setI18n(true);

        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);
        transformer.setI18NPropertyName("multi_de");
        transformer.readFromItem();
        // WHEN
        Property<?> res = transformer.createProperty();

        // THEN
        assertNotNull(res);
        assertTrue(res.getValue() instanceof JcrNewNodeAdapter);
        assertEquals("multi1_de", ((JcrNewNodeAdapter) res.getValue()).getNodeName());
        assertEquals(rootItem, ((JcrNodeAdapter) res.getValue()).getParent());
        assertEquals(res.getValue(), ((JcrNodeAdapter) rootItem).getChild("multi1_de"));
    }

    @Test
    public void removeElement() {
        // GIVEN
        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);
        PropertysetItem initialElements = transformer.readFromItem();

        // WHEN
        transformer.removeProperty(initialElements.getItemPropertyIds().iterator().next());

        // THEN
        PropertysetItem finalElements = transformer.readFromItem();
        assertNotNull(finalElements);
        assertEquals(0, finalElements.getItemPropertyIds().size());
    }


    @Test
    public void removeElementAndCreate() {
        // GIVEN
        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);
        PropertysetItem initialElements = transformer.readFromItem();
        transformer.removeProperty(initialElements.getItemPropertyIds().iterator().next());

        // WHEN
        Property<?> res = transformer.createProperty();

        // THEN
        assertNotNull(res);
        assertTrue(res.getValue() instanceof JcrNewNodeAdapter);
        assertEquals("multi1", ((JcrNewNodeAdapter) res.getValue()).getNodeName());
        assertEquals(rootItem, ((JcrNodeAdapter) res.getValue()).getParent());
        assertEquals(res.getValue(), ((JcrNodeAdapter) rootItem).getChild("multi1"));
    }

    @Test
    public void coherenceOfMultisetItemIds() {
        // GIVEN
        DelegatingMultiValueFieldTransformer transformer = new DelegatingMultiValueFieldTransformer(rootItem, definition, PropertysetItem.class, i18nContentSupport);
        PropertysetItem res = transformer.readFromItem();
        // create two elements
        transformer.createProperty();
        transformer.createProperty();
        assertEquals(3, res.getItemPropertyIds().size());
        assertNotNull(res.getItemProperty(0));
        assertNotNull(res.getItemProperty(1));
        assertNotNull(res.getItemProperty(2));
        // WHEN
        transformer.removeProperty(1);

        // THEN
        assertNotNull(res.getItemProperty(0));
        assertNotNull(res.getItemProperty(1));
        assertEquals(2, res.getItemPropertyIds().size());
    }

}
