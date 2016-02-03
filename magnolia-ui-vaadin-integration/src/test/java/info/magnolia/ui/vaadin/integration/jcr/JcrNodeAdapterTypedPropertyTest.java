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

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.value.BinaryValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Property;

/**
 * Test of {@link JcrNodeAdapter} property handling.
 */
public class JcrNodeAdapterTypedPropertyTest {

    private final String workspaceName = "workspace";
    private MockSession session;

    @Before
    public void setUp() {
        session = new MockSession(workspaceName);
        MockContext ctx = new MockContext();
        ctx.addSession(workspaceName, session);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testGetItemPropertyWithNonTypedProperty() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        String value = "test";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty(String.class, value);
        adapter.addItemProperty(id, propertyInitial);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
        assertEquals(PropertyType.nameFromValue(PropertyType.STRING), property.getType().getSimpleName());
        assertEquals(value, property.getValue().toString());
    }

    @Test
    public void testGetItemPropertyWithStringProperty() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        String value = "test";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty("String", value);
        adapter.addItemProperty(id, propertyInitial);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
        assertEquals(PropertyType.nameFromValue(PropertyType.STRING), property.getType().getSimpleName());
        assertEquals(value, property.getValue().toString());
    }

    @Test
    public void testGetItemPropertyWithLongProperty() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        String value = "10000";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty("Long", value);
        adapter.addItemProperty(id, propertyInitial);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
        assertEquals(PropertyType.nameFromValue(PropertyType.LONG), property.getType().getSimpleName());
        assertEquals(Long.decode(value), property.getValue());
    }

    @Test
    public void testGetItemPropertyWithDoubleProperty() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        String value = "10000.99";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty("Double", value);
        adapter.addItemProperty(id, propertyInitial);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
        assertEquals(PropertyType.nameFromValue(PropertyType.DOUBLE), property.getType().getSimpleName());
        assertEquals(Double.valueOf(value), property.getValue());
    }

    @Test
    public void testGetItemPropertyWithDateProperty() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        String value = "1970-07-04";
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 6);
        calendar.set(Calendar.DAY_OF_MONTH, 4);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty("Date", value);
        adapter.addItemProperty(id, propertyInitial);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
        assertEquals(PropertyType.nameFromValue(PropertyType.DATE), property.getType().getSimpleName());
        assertEquals(calendar.getTime(), property.getValue());
    }

    @Test
    public void testGetItemPropertyWithBooleanProperty() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        String value = "true";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty("Boolean", value);
        adapter.addItemProperty(id, propertyInitial);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
        assertEquals(PropertyType.nameFromValue(PropertyType.BOOLEAN), property.getType().getSimpleName());
        assertEquals(Boolean.TRUE, property.getValue());
    }

    @Test
    public void testGetItemPropertyWithDecimalProperty() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        String value = "1111111";
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty("Decimal", value);
        adapter.addItemProperty(id, propertyInitial);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertSame(property, propertyInitial);
        assertEquals("BigDecimal", property.getType().getSimpleName());
        assertEquals(new BigDecimal(value), property.getValue());
    }

    @Test
    public void testGetItemPropertyWithBooleanPropertyFromJcr() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        Boolean value = Boolean.TRUE;
        Node parentNode = session.getRootNode().addNode(nodeName);
        // Create the JCR property
        parentNode.setProperty(id, value);
        session.save();

        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // WHEN
        Property property = adapter.getItemProperty(id);

        // THEN
        assertEquals(PropertyType.nameFromValue(PropertyType.BOOLEAN), property.getType().getSimpleName());
        assertEquals(value, property.getValue());
    }

    @Test
    public void testGetItemPropertyWithBooleanPropertyFromJcrModifiedByVaadin() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        Boolean value = Boolean.TRUE;
        Node parentNode = session.getRootNode().addNode(nodeName);
        // Create the JCR property
        parentNode.setProperty(id, value);
        session.save();
        // Get the Jcr Property as a Vaadin props
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);
        Property property = adapter.getItemProperty(id);
        // Modify
        property.setValue(Boolean.FALSE);

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        assertEquals(PropertyType.nameFromValue(PropertyType.BOOLEAN), property.getType().getSimpleName());
        assertEquals(res.getProperty(id).getType(), PropertyType.BOOLEAN);
        assertEquals(res.getProperty(id).getBoolean(), property.getValue());
    }

    @Test
    public void testGetItemPropertyWithBinaryPropertyCreatedByVaadinStoredIntoJcr() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        BinaryValue value = new BinaryValue("text");
        Node parentNode = session.getRootNode().addNode(nodeName);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Create the property
        Property propertyInitial = DefaultPropertyUtil.newDefaultProperty("Binary", null);
        adapter.addItemProperty(id, propertyInitial);
        propertyInitial.setValue(value.getBinary());

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        assertEquals(res.getProperty(id).getType(), PropertyType.BINARY);
    }

    @Test
    public void testGetAndStoreBinaryPropertyCreatedByVaadinStoredIntoJcr() throws Exception {
        // GIVEN
        // Create a NewNodeAdapter
        String nodeName = "rootNode";
        String id = "propertyID";
        BinaryValue value = new BinaryValue("text");
        Node parentNode = session.getRootNode().addNode(nodeName);
        parentNode.setProperty(id, value);
        JcrNodeAdapter adapter = new JcrNodeAdapter(parentNode);

        // Get the property
        Property propertyInitial = adapter.getItemProperty(id);
        propertyInitial.setValue(new BinaryValue("newText").getBinary());

        // WHEN
        Node res = adapter.applyChanges();

        // THEN
        assertEquals(res.getProperty(id).getType(), PropertyType.BINARY);
        assertEquals(IOUtils.toString(res.getProperty(id).getBinary().getStream(), "UTF-8"), "newText");
    }
}
