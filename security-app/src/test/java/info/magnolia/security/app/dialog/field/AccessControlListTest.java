/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.security.app.dialog.field;

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.junit.Assert.*;

import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AccessControlList}.
 */
public class AccessControlListTest {

    private AccessControlList accessControlList;
    private Node aclNode;
    private static String ROOT_PATH = "/";
    private static String TEST_PATH = "/random";

    @Before
    public void setUp() {
        accessControlList = new AccessControlList();
        MockSession session = new MockSession("acl");
        aclNode = new MockNode(session);
    }

    @Test
    public void testGetEntryByNodeAccessTypeNodeWithRootPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE, ROOT_PATH));
        accessControlList.saveEntries(aclNode);

        //WHEN
        AccessControlList.Entry entry = accessControlList.getEntryByNode(aclNode.getNode("0"));

        //THEN
        assertEquals(ROOT_PATH, entry.getPath());
        assertEquals(AccessControlList.ACCESS_TYPE_NODE, entry.getAccessType());
    }

    @Test
    public void testGetEntryByNodeAccessTypeNodeWithTestPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE, TEST_PATH));
        accessControlList.saveEntries(aclNode);

        //WHEN
        AccessControlList.Entry entry = accessControlList.getEntryByNode(aclNode.getNode("0"));

        //THEN
        assertEquals(TEST_PATH, entry.getPath());
        assertEquals(AccessControlList.ACCESS_TYPE_NODE, entry.getAccessType());
    }

    @Test
    public void testGetEntryByNodeAccessTypeChildrenWithRootPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_CHILDREN, ROOT_PATH));
        accessControlList.saveEntries(aclNode);

        //WHEN
        AccessControlList.Entry entry = accessControlList.getEntryByNode(aclNode.getNode("0"));

        //THEN
        assertEquals(ROOT_PATH, entry.getPath());
        assertEquals(AccessControlList.ACCESS_TYPE_CHILDREN, entry.getAccessType());
    }

    @Test
    public void testGetEntryByNodeAccessTypeChildrenWithTestPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_CHILDREN, TEST_PATH));
        accessControlList.saveEntries(aclNode);

        //WHEN
        AccessControlList.Entry entry = accessControlList.getEntryByNode(aclNode.getNode("0"));

        //THEN
        assertEquals(TEST_PATH, entry.getPath());
        assertEquals(AccessControlList.ACCESS_TYPE_CHILDREN, entry.getAccessType());
    }

    @Test
    public void testGetEntryByNodeAccessTypeNodeAndChildrenWithRootPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, ROOT_PATH));
        accessControlList.saveEntries(aclNode);

        //WHEN
        AccessControlList.Entry entry = accessControlList.getEntryByNode(aclNode.getNode("0"));

        //THEN
        assertEquals(ROOT_PATH, entry.getPath());
        assertEquals(AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, entry.getAccessType());
    }

    @Test
    public void testGetEntryByNodeAccessTypeNodeAndChildrenWithRTestPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, TEST_PATH));
        accessControlList.saveEntries(aclNode);

        //WHEN
        AccessControlList.Entry entry = accessControlList.getEntryByNode(aclNode.getNode("0"));

        //THEN
        assertEquals(TEST_PATH, entry.getPath());
        assertEquals(AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, entry.getAccessType());
    }

    @Test
    public void testSaveEntriesAccessTypeNodeWithRootPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE, ROOT_PATH));

        //WHEN
        accessControlList.saveEntries(aclNode);

        //THEN
        assertTrue(aclNode.hasNodes());
        assertThat(aclNode.getNode("0"), hasProperty("path", ROOT_PATH));
    }

    @Test
    public void testSaveEntriesAccessTypeNodeWithTestPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE, TEST_PATH));

        //WHEN
        accessControlList.saveEntries(aclNode);

        //THEN
        assertTrue(aclNode.hasNodes());
        assertThat(aclNode.getNode("0"), hasProperty("path", TEST_PATH));
    }

    @Test
    public void testSaveEntriesAccessTypeChildrenWithRootPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_CHILDREN, ROOT_PATH));

        //WHEN
        accessControlList.saveEntries(aclNode);

        //THEN
        assertTrue(aclNode.hasNodes());
        assertThat(aclNode.getNode("0"), hasProperty("path", ROOT_PATH + "*"));
    }

    @Test
    public void testSaveEntriesAccessTypeChildrenWithTestPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_CHILDREN, TEST_PATH));

        //WHEN
        accessControlList.saveEntries(aclNode);

        //THEN
        assertTrue(aclNode.hasNodes());
        assertThat(aclNode.getNode("0"), hasProperty("path", TEST_PATH + "/*"));
    }

    @Test
    public void testSaveEntriesAccessTypeNodeAndChildrenWithRootPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, ROOT_PATH));

        //WHEN
        accessControlList.saveEntries(aclNode);

        //THEN
        assertTrue(aclNode.hasNodes());
        assertThat(aclNode.getNode("0"), hasProperty("path", ROOT_PATH));
        assertThat(aclNode.getNode("00"), hasProperty("path", ROOT_PATH + "*"));
    }

    @Test
    public void testSaveEntriesAccessTypeNodeAndChildrenWithTestPath() throws Exception {
        //GIVEN
        accessControlList.addEntry(new AccessControlList.Entry(0, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, TEST_PATH));

        //WHEN
        accessControlList.saveEntries(aclNode);

        //THEN
        assertTrue(aclNode.hasNodes());
        assertThat(aclNode.getNode("0"), hasProperty("path", TEST_PATH));
        assertThat(aclNode.getNode("00"), hasProperty("path", TEST_PATH + "/*"));
    }
}