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
package info.magnolia.ui.form.field.transformer.basic;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.form.field.definition.OptionGroupFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * .
 */
public class ListToSetTransformerTest extends RepositoryTestCase {
    private Node rootNode;
    private final String propertyName = "propertyName";
    private OptionGroupFieldDefinition definition = new OptionGroupFieldDefinition();

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
    public void testReadFromDataSourceItemNotMultiSelect() throws RepositoryException {
        // GIVEN
        definition.setMultiselect(false);
        rootNode.setProperty(propertyName, "stringValue");
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        ListToSetTransformer<String> handler = new ListToSetTransformer<String>(rootItem, definition, String.class);

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
    public void testReadFromDataSourceItemEmptyMultiSelect() throws RepositoryException {
        // GIVEN
        definition.setMultiselect(true);
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        ListToSetTransformer<String> handler = new ListToSetTransformer<String>(rootItem, definition, String.class);

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNotNull(value);
        assertTrue(value instanceof HashSet);
    }

    @Test
    public void testReadFromDataSourceItemNotEmptyMultiSelect() throws RepositoryException {
        // GIVEN
        definition.setMultiselect(true);
        rootNode.setProperty(propertyName, new String[] { "a", "b", "c" });
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        ListToSetTransformer<String> handler = new ListToSetTransformer<String>(rootItem, definition, String.class);

        // WHEN
        Object value = handler.readFromItem();

        // THEN
        assertNotNull(value);
        assertTrue(value instanceof HashSet);
        assertFalse(((HashSet) value).isEmpty());
        assertTrue(((HashSet) value).contains("a"));
    }

    @Test
    public void testReadWriteFromDataSourceItem() throws RepositoryException {
        // GIVEN
        definition.setMultiselect(true);
        rootNode.setProperty(propertyName, new String[] { "a", "b", "c" });
        JcrNodeAdapter rootItem = new JcrNodeAdapter(rootNode);

        ListToSetTransformer handler = new ListToSetTransformer(rootItem, definition, String.class);
        Object value = handler.readFromItem();
        assertNotNull(value);
        assertTrue(value instanceof HashSet);
        ((HashSet) value).add("d");

        // WHEN
        handler.writeToItem(value);

        // THEN
        assertNotNull(rootItem.getItemProperty(propertyName));
        List<String> property = (List<String>) rootItem.getItemProperty(propertyName).getValue();
        assertTrue(value instanceof HashSet);
        assertTrue(property.contains("a"));
        assertTrue(property.contains("b"));
        assertTrue(property.contains("c"));
        assertTrue(property.contains("d"));
    }

}
