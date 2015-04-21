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
package info.magnolia.ui.form.field.transformer.composite;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Main test class for {@link DelegatingCompositeFieldTransformer}.
 */
public class DelegatingCompositeFieldTransformerTest {

    private CompositeFieldDefinition definition = new CompositeFieldDefinition();
    private Item rootItem;

    @Before
    public void setUp() throws Exception {
        List<ConfiguredFieldDefinition> fields = new ArrayList<ConfiguredFieldDefinition>();
        TextFieldDefinition text1 = new TextFieldDefinition();
        text1.setName("text1");
        fields.add(text1);
        TextFieldDefinition text2 = new TextFieldDefinition();
        text2.setName("text2");
        fields.add(text2);
        definition.setFields(fields);
        MockSession session = new MockSession("test");
        Node node = new MockNode(session);
        rootItem = new JcrNodeAdapter(node);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void readFromItemOnce() {
        // GIVEN
        DelegatingCompositeFieldTransformer transformer = new DelegatingCompositeFieldTransformer(rootItem, definition, PropertysetItem.class, definition.getFieldNames());

        // WHEN
        PropertysetItem res = transformer.readFromItem();

        // THEN
        assertNotNull(res);
        assertEquals(2, res.getItemPropertyIds().size());
        assertNotNull(res.getItemProperty("text1"));
        assertEquals(rootItem, res.getItemProperty("text1").getValue());
        assertNotNull(res.getItemProperty("text2"));
        assertEquals(rootItem, res.getItemProperty("text2").getValue());
    }

    public void readFromItemTwice() {
        // GIVEN
        DelegatingCompositeFieldTransformer transformer = new DelegatingCompositeFieldTransformer(rootItem, definition, PropertysetItem.class, definition.getFieldNames());

        // WHEN
        PropertysetItem res1 = transformer.readFromItem();
        PropertysetItem res2 = transformer.readFromItem();

        // THEN
        assertEquals(res1, res2);
    }

}
