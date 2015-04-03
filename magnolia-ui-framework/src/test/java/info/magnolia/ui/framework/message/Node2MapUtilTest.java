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
package info.magnolia.ui.framework.message;

import static org.junit.Assert.*;

import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.jcr.MockNode;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * A test for the Node2MapUtil class.
 * 
 */
public class Node2MapUtilTest extends RepositoryTestCase {

    // entry/property names
    private static final String STRING = "string";
    private static final String LONG = "long";
    private static final String DOUBLE = "double";
    private static final String BOOLEAN = "boolean";
    private static final String DATE = "date";
    private static final String MAP = "map";

    // values
    private static final String STRING_VALUE = "abcdef";
    private static final long LONG_VALUE = (long) 123456;
    private static final double DOUBLE_VALUE = 123.456;
    private static final boolean BOOLEAN_VALUE = true;
    private static final Calendar DATE_VALUE = Calendar.getInstance();
    private static final String ABC = "abc";
    private static final String DEF = "def";
    private static final String GHI = "ghi";

    Node node;
    Map<String, Object> map;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        node = new MockNode();
        map = new HashMap<String, Object>();
    }

    @Test
    public void testStorePrimitives() throws Exception {
        // GIVEN
        map.put(STRING, STRING_VALUE);
        map.put(LONG, LONG_VALUE);
        map.put(DOUBLE, DOUBLE_VALUE);
        map.put(BOOLEAN, BOOLEAN_VALUE);
        map.put(DATE, DATE_VALUE);

        // WHEN
        node = Node2MapUtil.map2node(node, map);

        // THEN
        assertTrue(node.hasProperty(STRING));
        assertTrue(node.hasProperty(LONG));
        assertTrue(node.hasProperty(DOUBLE));
        assertTrue(node.hasProperty(BOOLEAN));
        assertTrue(node.hasProperty(DATE));

        assertEquals(STRING_VALUE, node.getProperty(STRING).getString());
        assertEquals(LONG_VALUE, node.getProperty(LONG).getLong());
        assertEquals(DOUBLE_VALUE, node.getProperty(DOUBLE).getDouble(), 0.0001);
        assertEquals(BOOLEAN_VALUE, node.getProperty(BOOLEAN).getBoolean());
        assertEquals(DATE_VALUE, node.getProperty(DATE).getDate());
    }

    @Test
    public void testStoreSubnode() throws Exception {
        // GIVEN
        Map<String, Object> nested = new HashMap<String, Object>();
        nested.put(STRING, STRING_VALUE);
        nested.put(LONG, LONG_VALUE);
        nested.put(DOUBLE, DOUBLE_VALUE);
        nested.put(BOOLEAN, BOOLEAN_VALUE);
        nested.put(DATE, DATE_VALUE);

        map.put(MAP, nested);

        // WHEN
        node = Node2MapUtil.map2node(node, map);

        // THEN
        assertTrue(node.hasNode(MAP));

        Node nestedNode = node.getNode(MAP);

        assertTrue(nestedNode.hasProperty(STRING));
        assertTrue(nestedNode.hasProperty(LONG));
        assertTrue(nestedNode.hasProperty(DOUBLE));
        assertTrue(nestedNode.hasProperty(BOOLEAN));
        assertTrue(nestedNode.hasProperty(DATE));

        assertEquals(STRING_VALUE, nestedNode.getProperty(STRING).getString());
        assertEquals(LONG_VALUE, nestedNode.getProperty(LONG).getLong());
        assertEquals(DOUBLE_VALUE, nestedNode.getProperty(DOUBLE).getDouble(), 0.0001);
        assertEquals(BOOLEAN_VALUE, nestedNode.getProperty(BOOLEAN).getBoolean());
        assertEquals(DATE_VALUE, nestedNode.getProperty(DATE).getDate());
    }

    @Test
    public void testExceptionOnNonPrimitive() throws Exception {
        // GIVEN
        map.put("failureExpected", new StringBuilder());

        exception.expect(IllegalArgumentException.class);

        // WHEN
        node = Node2MapUtil.map2node(node, map);

        // THEN
        fail("No IllegalArgumentException thrown.");
    }

    @Test
    public void testReadPrimitives() throws Exception {
        // GIVEN
        node.setProperty(STRING, STRING_VALUE);
        node.setProperty(LONG, LONG_VALUE);
        node.setProperty(DOUBLE, DOUBLE_VALUE);
        node.setProperty(BOOLEAN, BOOLEAN_VALUE);
        node.setProperty(DATE, DATE_VALUE);

        // WHEN
        map = Node2MapUtil.node2map(node);

        // THEN
        assertEquals(STRING_VALUE, (String) map.get(STRING));
        long longValue = ((Long) map.get(LONG)).longValue();
        assertEquals(LONG_VALUE, longValue);
        double doubleValue = ((Double) map.get(DOUBLE)).doubleValue();
        assertEquals(DOUBLE_VALUE, doubleValue, 0.0001);
        boolean boolValue = ((Boolean) map.get(BOOLEAN)).booleanValue();
        assertEquals(BOOLEAN_VALUE, boolValue);
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) map.get(DATE));
        assertEquals(DATE_VALUE, cal);
    }


    @Test
    public void testReadSubnodes() throws Exception {
        // GIVEN
        Node nested = node.addNode(MAP);
        nested.setProperty(STRING, STRING_VALUE);
        nested.setProperty(LONG, LONG_VALUE);
        nested.setProperty(DOUBLE, DOUBLE_VALUE);
        nested.setProperty(BOOLEAN, BOOLEAN_VALUE);
        nested.setProperty(DATE, DATE_VALUE);

        // WHEN
        map = Node2MapUtil.node2map(node);

        // THEN
        map = (Map<String, Object>) map.get(MAP);
        assertNotNull(map);
        // check the values in the nested map
        assertEquals(STRING_VALUE, (String) map.get(STRING));
        long longValue = ((Long) map.get(LONG)).longValue();
        assertEquals(LONG_VALUE, longValue);
        double doubleValue = ((Double) map.get(DOUBLE)).doubleValue();
        assertEquals(DOUBLE_VALUE, doubleValue, 0.0001);
        boolean boolValue = ((Boolean) map.get(BOOLEAN)).booleanValue();
        assertEquals(BOOLEAN_VALUE, boolValue);
        Calendar cal = Calendar.getInstance();
        cal.setTime((Date) map.get(DATE));
        assertEquals(DATE_VALUE, cal);
    }

}
