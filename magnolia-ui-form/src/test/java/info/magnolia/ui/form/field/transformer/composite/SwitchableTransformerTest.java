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
package info.magnolia.ui.form.field.transformer.composite;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.HiddenFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.definition.SwitchableFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.util.PropertysetItem;

/**
 * Tests for the {@link SwitchableTransformer}.
 * Considering a simple switchable field configured with two sub-fields: one TextField and one HiddenField.
 */
public class SwitchableTransformerTest {

    private Session session;
    private SwitchableFieldDefinition definition;
    private TextFieldDefinition text;
    private HiddenFieldDefinition hidden;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(RepositoryConstants.WEBSITE);
        MockContext ctx = new MockContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);

        definition = new SwitchableFieldDefinition();
        definition.setName("switch");

        text = new TextFieldDefinition();
        text.setName("text");
        hidden = new HiddenFieldDefinition();
        hidden.setName("hidden");
        hidden.setType("Boolean");
        hidden.setDefaultValue("true");
        List<ConfiguredFieldDefinition> fields = new ArrayList<ConfiguredFieldDefinition>();
        fields.add(text);
        fields.add(hidden);
        definition.setFields(fields);

        SelectFieldOptionDefinition textOption = new SelectFieldOptionDefinition();
        textOption.setValue("text");
        SelectFieldOptionDefinition hiddenOption = new SelectFieldOptionDefinition();
        hiddenOption.setValue("hidden");
        definition.setOptions(Arrays.asList(textOption, hiddenOption));
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testWriteSelectedField() throws RepositoryException {
        // GIVEN
        Node parent = session.getRootNode();
        JcrNodeAdapter node = new JcrNewNodeAdapter(parent, NodeTypes.ContentNode.NAME, "node");
        SwitchableTransformer delegate = new SwitchableTransformer(node, definition, PropertysetItem.class, definition.getFieldNames(), mock(I18NAuthoringSupport.class));

        // WHEN
        PropertysetItem itemSet = new PropertysetItem();
        itemSet.addItemProperty("switch", new DefaultProperty<String>("text"));
        itemSet.addItemProperty("text", new DefaultProperty<String>("hop!"));
        delegate.writeToItem(itemSet);
        node.applyChanges();

        // THEN
        Node jcrNode = parent.getNode("node");
        assertTrue(jcrNode.hasProperty("switch"));
        assertEquals("text", jcrNode.getProperty("switch").getString());
        assertTrue(jcrNode.hasProperty("switchtext"));
        assertEquals("hop!", jcrNode.getProperty("switchtext").getString());
        assertFalse(jcrNode.hasProperty("switchhidden"));
    }

    @Test
    public void testWriteBothFields() throws RepositoryException {
        // GIVEN
        Node parent = session.getRootNode();
        JcrNodeAdapter node = new JcrNewNodeAdapter(parent, NodeTypes.ContentNode.NAME, "node");
        SwitchableTransformer delegate = new SwitchableTransformer(node, definition, PropertysetItem.class, definition.getFieldNames(), mock(I18NAuthoringSupport.class));

        // WHEN
        PropertysetItem itemSet = new PropertysetItem();
        itemSet.addItemProperty("text", new DefaultProperty<String>("hop!"));
        itemSet.addItemProperty("switch", new DefaultProperty<String>("hidden"));
        itemSet.addItemProperty("hidden", new DefaultProperty<Boolean>(true));
        delegate.writeToItem(itemSet);
        node.applyChanges();

        // THEN
        Node jcrNode = parent.getNode("node");
        assertTrue(jcrNode.hasProperty("switch"));
        assertEquals("hidden", jcrNode.getProperty("switch").getString());
        assertTrue(jcrNode.hasProperty("switchhidden"));
        assertTrue(jcrNode.getProperty("switchhidden").getBoolean());
    }

    @Test
    public void testWriteSelectedFieldWithoutValue() throws RepositoryException {
        // GIVEN
        Node parent = session.getRootNode();
        JcrNodeAdapter node = new JcrNewNodeAdapter(parent, NodeTypes.ContentNode.NAME, "node");
        SwitchableTransformer delegate = new SwitchableTransformer(node, definition, PropertysetItem.class, definition.getFieldNames(), mock(I18NAuthoringSupport.class));

        // WHEN
        PropertysetItem itemSet = new PropertysetItem();
        itemSet.addItemProperty("switch", new DefaultProperty<String>("text"));
        delegate.writeToItem(itemSet);
        node.applyChanges();

        // THEN
        Node jcrNode = parent.getNode("node");
        assertTrue(jcrNode.hasProperty("switch"));
        assertEquals("text", jcrNode.getProperty("switch").getString());
        assertFalse(jcrNode.hasProperty("switchtext"));
        assertFalse(jcrNode.hasProperty("switchhidden"));
    }

}
