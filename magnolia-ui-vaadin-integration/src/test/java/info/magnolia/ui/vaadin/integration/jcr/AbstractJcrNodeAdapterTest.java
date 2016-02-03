/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.integration.jcr;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.ModelConstants;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.data.Property;

/**
 * Test of {@link AbstractJcrNodeAdapter}.
 */
public class AbstractJcrNodeAdapterTest {

    private static final String WORKSPACE_NAME = "workspace";

    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(WORKSPACE_NAME);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE_NAME, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testSetCommonAttributes() throws Exception {
        // GIVEN
        String nodeName = "nodeName";
        Node testNode = session.getRootNode().addNode(nodeName);

        // WHEN
        DummyJcrNodeAdapter adapter = new DummyJcrNodeAdapter(testNode);

        // THEN
        assertEquals(testNode.getIdentifier(), adapter.getItemId());
        assertEquals(testNode.getIdentifier(), adapter.getJcrItem().getIdentifier());
        assertEquals(testNode.getPrimaryNodeType().getName(), adapter.getPrimaryNodeTypeName());
    }

    @Test
    public void testGetItemPropertyWithExistingProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        final Property prop = item.getItemProperty(propertyName);

        // THEN
        assertEquals(propertyValue, prop.getValue());

    }

    @Test
    public void testGetItemPropertyWithUnknownProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        underlyingNode.setProperty(propertyName, propertyValue);
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        final Property prop = item.getItemProperty(propertyName + "_1");

        // THEN
        assertNull(prop);
    }

    @Test
    public void testGetItemPropertyWithNewProperty() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);
        Property property = DefaultPropertyUtil.newDefaultProperty(PropertyType.TYPENAME_STRING, propertyValue);
        item.addItemProperty(propertyName, property);

        // WHEN
        final Property prop = item.getItemProperty(propertyName);

        // THEN
        assertEquals(propertyValue, prop.getValue().toString());
    }

    @Test
    public void testValueChangeEventWhenPropertyExists() throws Exception {
        // GIVEN
        Node underlyingNode = session.getRootNode().addNode("underlying");
        String propertyName = "TEST";
        String propertyValue = "value";
        javax.jcr.Property jcrProperty = underlyingNode.setProperty(propertyName, propertyValue);
        DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        Property nodeProperty = item.getItemProperty(propertyName);
        nodeProperty.setValue("newValue");

        // THEN
        assertFalse(item.getChangedProperties().isEmpty());
        assertTrue(item.getChangedProperties().containsKey(propertyName));
        assertEquals(nodeProperty, item.getChangedProperties().get(propertyName));
        assertEquals("newValue", item.getChangedProperties().get(propertyName).getValue());
    }

    @Test
    public void testValueChangeEventWhenPropertyDoesNotExist() throws Exception {
        // GIVEN
        Node underlyingNode = session.getRootNode().addNode("underlying");
        String propertyName = "TEST";
        DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);

        // WHEN
        Property itemProperty = DefaultPropertyUtil.newDefaultProperty(PropertyType.TYPENAME_STRING, propertyName);
        item.addItemProperty(propertyName, itemProperty);
        itemProperty.setValue("newValue");

        // THEN
        assertFalse(item.getChangedProperties().isEmpty());
        assertTrue(item.getChangedProperties().containsKey(propertyName));
        assertEquals(itemProperty, item.getChangedProperties().get(propertyName));
        assertEquals("newValue", item.getChangedProperties().get(propertyName).getValue());
    }

    @Test
    public void testUpdateProperties() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyName = "TEST";
        final String propertyValue = "value";
        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);
        Property property = DefaultPropertyUtil.newDefaultProperty(PropertyType.TYPENAME_STRING, propertyValue);
        item.getChangedProperties().put(propertyName, property);

        // WHEN
        item.applyChanges();

        // THEN
        assertEquals(propertyValue, underlyingNode.getProperty(propertyName).getString());
    }

    @Test
    public void testUpdateNewProperties() throws Exception {
        // GIVEN
        final Node underlyingNode = session.getRootNode().addNode("underlying");
        final String propertyNameNotEmpty = "NOT_EMPTY";
        final String propertyValueNotEmpty = "value";
        final String propertyNameEmpty = "EMPTY";
        final String propertyNameBlank = "BLANK";
        final String propertyNameAlreadyStored = "EXISTING";
        final String propertyValueAlreadyStored = "existing";
        final String propertyNameAlreadyStoredEmpty = "EXISTING_EMPTY";

        underlyingNode.setProperty(propertyNameAlreadyStored, propertyValueAlreadyStored);
        underlyingNode.setProperty(propertyNameAlreadyStoredEmpty, "");
        underlyingNode.getSession().save();

        final DummyJcrNodeAdapter item = new DummyJcrNodeAdapter(underlyingNode);
        Property propertyNotEmpty = DefaultPropertyUtil.newDefaultProperty(PropertyType.TYPENAME_STRING, propertyValueNotEmpty);
        item.getChangedProperties().put(propertyNameNotEmpty, propertyNotEmpty);
        Property propertyEmpty = DefaultPropertyUtil.newDefaultProperty(PropertyType.TYPENAME_STRING, "");
        item.getChangedProperties().put(propertyNameEmpty, propertyEmpty);
        Property propertyBlank = DefaultPropertyUtil.newDefaultProperty(PropertyType.TYPENAME_STRING, " ");
        item.getChangedProperties().put(propertyNameBlank, propertyBlank);

        // WHEN
        item.applyChanges();

        // THEN
        assertEquals(propertyValueNotEmpty, underlyingNode.getProperty(propertyNameNotEmpty).getString());
        assertEquals(propertyValueAlreadyStored, underlyingNode.getProperty(propertyNameAlreadyStored).getString());
        assertEquals(" ", underlyingNode.getProperty(propertyNameBlank).getString());
        assertEquals("", underlyingNode.getProperty(propertyNameEmpty).getString());
        assertTrue(underlyingNode.hasProperty(propertyNameAlreadyStoredEmpty));
    }

    @Test
    public void testUpdatePropertiesNameToAlreadyExisting() throws Exception {

        // MockSession doesn't support move so we use a spy
        final MockSession session = spy(this.session);
        final Node root = new MockNode(session);

        MockContext ctx = (MockContext) MgnlContext.getInstance();
        ctx.addSession(WORKSPACE_NAME, session);

        // mocking rename operation
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                String srcAbsPath = (String) invocation.getArguments()[0];
                String dstAbsPath = (String) invocation.getArguments()[1];
                session.removeItem(srcAbsPath);
                String dstRelPath = StringUtils.substringAfter(dstAbsPath, root.getPath());
                root.addNode(dstRelPath);
                return null;
            }
        }).when(session).move(anyString(), anyString());

        // GIVEN
        String existingName = "existingName";
        String subNodeName = "subNode";

        root.setProperty(existingName, "42");
        Node subNode = root.addNode(subNodeName);
        long nodeCount = root.getNodes().getSize();
        long propertyCount = root.getProperties().getSize();
        DummyJcrNodeAdapter adapter = new DummyJcrNodeAdapter(subNode);

        // WHEN
        adapter.getItemProperty(ModelConstants.JCR_NAME).setValue(existingName);
        adapter.applyChanges();

        // THEN
        assertTrue(root.hasProperty(existingName));
        assertFalse(root.hasNode(existingName));
        assertFalse(root.hasNode(subNodeName));
        assertEquals(nodeCount, root.getNodes().getSize());
        assertEquals(propertyCount, root.getProperties().getSize());
    }

    @Test
    public void hasChangedChildItemsForAddedOne() throws Exception {
        // GIVEN
        Node parentNode = session.getRootNode().addNode("nodeName");
        DummyJcrNodeAdapter parentAdapter = new DummyJcrNodeAdapter(parentNode);
        Node childNode = parentNode.addNode("childNode");
        DummyJcrNodeAdapter childAdapter = new DummyJcrNodeAdapter(childNode);
        assertFalse(parentAdapter.hasChildItemChanges());
        // WHEN
        parentAdapter.addChild(childAdapter);

        // THEN
        assertTrue(parentAdapter.hasChildItemChanges());
    }

    @Test
    public void hasChangedChildItemsForRemovedOne() throws Exception {
        // GIVEN
        Node parentNode = session.getRootNode().addNode("nodeName");
        DummyJcrNodeAdapter parentAdapter = new DummyJcrNodeAdapter(parentNode);
        Node childNode = parentNode.addNode("childNode");
        DummyJcrNodeAdapter childAdapter = new DummyJcrNodeAdapter(childNode);
        assertFalse(parentAdapter.hasChildItemChanges());
        // WHEN
        parentAdapter.removeChild(childAdapter);

        // THEN
        assertTrue(parentAdapter.hasChildItemChanges());
    }

    /**
     * Dummy implementation of the Abstract class.
     */
    public class DummyJcrNodeAdapter extends info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter {

        public DummyJcrNodeAdapter(Node jcrNode) {
            super(jcrNode);
        }

        @Override
        public Collection<?> getItemPropertyIds() {
            return null;
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            return false;
        }

        @Override
        public boolean isNew() {
            return false;
        }

    }

}
