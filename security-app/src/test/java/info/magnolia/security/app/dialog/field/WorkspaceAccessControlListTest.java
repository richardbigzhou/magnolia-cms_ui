/**
 * This file Copyright (c) 2015-2016 Magnolia International
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

import static info.magnolia.security.app.dialog.field.WorkspaceAccessControlList.*;
import static info.magnolia.jcr.nodebuilder.Ops.*;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import info.magnolia.security.app.dialog.field.WorkspaceAccessControlList.Entry;
import info.magnolia.jcr.nodebuilder.NodeBuilder;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Collection;
import java.util.List;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class WorkspaceAccessControlListTest {

    private WorkspaceAccessControlList acl;
    private Node aclNode;

    @Before
    public void setUp() {
        acl = new WorkspaceAccessControlList();
        MockSession session = new MockSession("acl");
        aclNode = new MockNode(session);
    }

    // Read entries

    @Test
    public void readEntriesExpandsPathIntoPathAndAccessType() throws Exception {
        // GIVEN two ACEs with node paths and two ACEs with sub-node paths (both for root, or for regular node)
        new NodeBuilder(aclNode,
                addNode("0").then(
                        addProperty("path", "/"),
                        addProperty("permissions", "8")),
                addNode("01").then(
                        addProperty("path", "/foo"),
                        addProperty("permissions", "8")),
                addNode("00").then(
                        addProperty("path", "/*"),
                        addProperty("permissions", "63")),
                addNode("02").then(
                        addProperty("path", "/bar/*"),
                        addProperty("permissions", "63"))
        ).exec();

        // WHEN
        acl.readEntries(aclNode);

        // THEN
        Collection<Entry> acl = this.acl.getEntries();
        assertThat(acl, hasSize(4));
        assertThat(acl, containsInAnyOrder(
                new Entry(8L, ACCESS_TYPE_NODE, "/"),
                new Entry(8L, ACCESS_TYPE_NODE, "/foo"),
                new Entry(63L, ACCESS_TYPE_CHILDREN, "/"),
                new Entry(63L, ACCESS_TYPE_CHILDREN, "/bar")
        ));
    }

    @Test
    public void readEntriesDeduplicatesEntries() throws Exception {
        // GIVEN two duplicated ACEs (one for root, one for regular node, even one with trailing slash)
        new NodeBuilder(aclNode,
                addNode("0").then(
                        addProperty("path", "/"),
                        addProperty("permissions", "8")),
                addNode("00").then(
                        addProperty("path", "/"),
                        addProperty("permissions", "8")),
                addNode("01").then(
                        addProperty("path", "/foo"),
                        addProperty("permissions", "63")),
                addNode("02").then(
                        addProperty("path", "/foo"),
                        addProperty("permissions", "63")),
                addNode("03").then(
                        addProperty("path", "/foo/"),
                        addProperty("permissions", "63"))
        ).exec();

        // WHEN
        acl.readEntries(aclNode);

        // THEN
        Collection<Entry> acl = this.acl.getEntries();
        assertThat(acl, hasSize(2));
        assertThat(acl, containsInAnyOrder(
                new Entry(8L, ACCESS_TYPE_NODE, "/"),
                new Entry(63L, ACCESS_TYPE_NODE, "/foo")
        ));
    }

    @Test
    public void readEntriesMergesEntriesByBasePathAndPermission() throws Exception {
        // GIVEN two mergeable ACEs (one for root, one for regular node), plus two additional non-mergeable ACEs
        new NodeBuilder(aclNode,
                addNode("0").then(
                        addProperty("path", "/"),
                        addProperty("permissions", "8")),
                addNode("00").then(
                        addProperty("path", "/*"),
                        addProperty("permissions", "8")),
                addNode("01").then(
                        addProperty("path", "/merge"),
                        addProperty("permissions", "63")),
                addNode("02").then(
                        addProperty("path", "/merge/*"),
                        addProperty("permissions", "63")),
                addNode("03").then(
                        addProperty("path", "/foo"),
                        addProperty("permissions", "8")),
                addNode("04").then(
                        addProperty("path", "/foo/*"),
                        addProperty("permissions", "63"))
        ).exec();

        // WHEN
        acl.readEntries(aclNode);

        // THEN
        Collection<Entry> acl = this.acl.getEntries();
        assertThat(acl, hasSize(4));
        assertThat(acl, containsInAnyOrder(
                new Entry(8L, ACCESS_TYPE_NODE_AND_CHILDREN, "/"),
                new Entry(63L, ACCESS_TYPE_NODE_AND_CHILDREN, "/merge"),
                new Entry(8L, ACCESS_TYPE_NODE, "/foo"),
                new Entry(63L, ACCESS_TYPE_CHILDREN, "/foo")
        ));
    }

    // Save entries

    @Test
    public void saveEntriesWithSeparateAccessTypes() throws Exception {
        // GIVEN two ACEs with node paths and two ACEs with sub-node paths (both for root, or for regular nodes)
        acl.addEntry(new Entry(8L, ACCESS_TYPE_NODE, "/"));
        acl.addEntry(new Entry(63L, ACCESS_TYPE_CHILDREN, "/"));
        acl.addEntry(new Entry(8L, ACCESS_TYPE_NODE, "/foo"));
        acl.addEntry(new Entry(63L, ACCESS_TYPE_CHILDREN, "/bar"));

        // WHEN
        acl.saveEntries(aclNode);

        // THEN
        assertThat(NodeUtil.getNodes(aclNode), containsInAnyOrder(
                allOf(
                        hasProperty("path", "/"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/*"),
                        hasProperty("permissions", 63L)),
                allOf(
                        hasProperty("path", "/foo"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/bar/*"),
                        hasProperty("permissions", 63L))
        ));
    }

    @Test
    public void saveEntriesDeduplicatesEntries() throws Exception {
        // GIVEN two duplicated ACEs (one for root, one for regular node, even one with trailing slash)
        acl.addEntry(new Entry(8L, ACCESS_TYPE_NODE, "/"));
        acl.addEntry(new Entry(8L, ACCESS_TYPE_NODE, "/"));
        acl.addEntry(new Entry(63L, ACCESS_TYPE_NODE, "/foo"));
        acl.addEntry(new Entry(63L, ACCESS_TYPE_NODE, "/foo"));
        acl.addEntry(new Entry(63L, ACCESS_TYPE_NODE, "/foo/"));

        // WHEN
        acl.saveEntries(aclNode);

        // THEN
        assertThat(NodeUtil.getNodes(aclNode), containsInAnyOrder(
                allOf(
                        hasProperty("path", "/"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/foo"),
                        hasProperty("permissions", 63L))
        ));
    }

    @Test
    public void saveEntriesCreatesDoubleEntriesForAccessTypeNodeAndChildren() throws Exception {
        // GIVEN two node-and-children ACEs (one for root, one for regular node), plus two additional non-mergeable ACEs
        acl.addEntry(new Entry(8L, ACCESS_TYPE_NODE_AND_CHILDREN, "/"));
        acl.addEntry(new Entry(63L, ACCESS_TYPE_NODE_AND_CHILDREN, "/merge"));
        acl.addEntry(new Entry(8L, ACCESS_TYPE_NODE, "/foo"));
        acl.addEntry(new Entry(63L, ACCESS_TYPE_CHILDREN, "/foo"));

        // WHEN
        acl.saveEntries(aclNode);

        // THEN
        assertThat(NodeUtil.getNodes(aclNode), containsInAnyOrder(
                allOf(
                        hasProperty("path", "/"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/*"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/merge"),
                        hasProperty("permissions", 63L)),
                allOf(
                        hasProperty("path", "/merge/*"),
                        hasProperty("permissions", 63L)),
                allOf(
                        hasProperty("path", "/foo"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/foo/*"),
                        hasProperty("permissions", 63L))
        ));
    }

    // WorkspaceAccessControlList.Entry low-level tests

    @Test
    public void sanitizeEntriesUponConstructAndUponSetPathIfNeeded() throws Exception {
        // GIVEN an unprocessed entry
        Entry entry = new Entry(8L, "/*");

        // WHEN first sanitation is done upon construct, trigger a second one via #setPath
        assertThat(entry.getPath(), equalTo("/"));
        assertThat(entry.getPermissions(), equalTo(8L));
        assertThat(entry.getAccessType(), equalTo(ACCESS_TYPE_CHILDREN));
        // path alone doesn't grant NODE access type
        entry.setPath("/");

        // THEN
        assertThat(entry.getPath(), equalTo("/"));
        assertThat(entry.getPermissions(), equalTo(8L));
        assertThat(entry.getAccessType(), equalTo(ACCESS_TYPE_CHILDREN));
    }

    @Test
    public void sanitizeTrailingSlashesUponConstructAndUponSetPath() throws Exception {
        // GIVEN an unprocessed entry
        Entry entry = new Entry(8L, ACCESS_TYPE_CHILDREN, "/foo//");

        // WHEN first sanitation is done upon construct, trigger a second one via #setPath
        assertThat(entry.getPath(), equalTo("/foo"));
        assertThat(entry.getPermissions(), equalTo(8L));
        assertThat(entry.getAccessType(), equalTo(ACCESS_TYPE_CHILDREN));
        // path alone doesn't grant NODE access type and trailing slashes are stripped regardless of how many there are
        entry.setPath("/foo///");

        // THEN
        assertThat(entry.getPath(), equalTo("/foo"));
        assertThat(entry.getPermissions(), equalTo(8L));
        assertThat(entry.getAccessType(), equalTo(ACCESS_TYPE_CHILDREN));
    }

    @Test
    public void sanitizeTrailingSlashesAndRedundantWildcards() throws Exception {
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/foo/").getPath(), equalTo("/foo"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/foo/*").getPath(), equalTo("/foo"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/foo///").getPath(), equalTo("/foo"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/foo//*").getPath(), equalTo("/foo"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/foo//*/").getPath(), equalTo("/foo"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/foo//*///**").getPath(), equalTo("/foo"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "//").getPath(), equalTo("/"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "//*").getPath(), equalTo("/"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "////").getPath(), equalTo("/"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "///*").getPath(), equalTo("/"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "///*/").getPath(), equalTo("/"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "///*///**").getPath(), equalTo("/"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/demo-*/*").getPath(), equalTo("/demo-*"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/demo-*/").getPath(), equalTo("/demo-*"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "/////a/*").getPath(), equalTo("/a"));
        assertThat(new Entry(8L, ACCESS_TYPE_NODE, "").getPath(), equalTo(""));
    }

    @Test
    public void updateAccessTypeWhenModifyingPathWithWildcard() throws Exception {
        // GIVEN an unprocessed entry
        Entry entry = new Entry(8L, "/");
        assertThat(entry.getPath(), equalTo("/"));
        assertThat(entry.getPermissions(), equalTo(8L));
        assertThat(entry.getAccessType(), equalTo(ACCESS_TYPE_NODE));

        // WHEN path is set with wild-card
        entry.setPath("/*");

        // THEN 1. we reflect intent by updating access type
        assertThat(entry.getPath(), equalTo("/"));
        assertThat(entry.getPermissions(), equalTo(8L));
        assertThat(entry.getAccessType(), equalTo(ACCESS_TYPE_NODE_AND_CHILDREN));

        // THEN 2. access type still prevails
        entry.setAccessType(ACCESS_TYPE_NODE);
        assertThat(entry.getPath(), equalTo("/"));
        assertThat(entry.getPermissions(), equalTo(8L));
        assertThat(entry.getAccessType(), equalTo(ACCESS_TYPE_NODE));
    }

    @Test
    public void mergeEntries() throws Exception {
        // GIVEN
        List<Entry> entries = Lists.newArrayList(
                new Entry(8L, ACCESS_TYPE_NODE, "/"),
                new Entry(8L, ACCESS_TYPE_CHILDREN, "/"),
                new Entry(63L, ACCESS_TYPE_NODE, "/merge"),
                new Entry(63L, ACCESS_TYPE_CHILDREN, "/merge"),
                new Entry(8L, ACCESS_TYPE_NODE, "/foo"),
                new Entry(63L, ACCESS_TYPE_CHILDREN, "/foo"));

        // WHEN
        Collection<Entry> acl = this.acl.mergeEntries(entries);

        // THEN
        assertThat(acl, hasSize(4));
        assertThat(acl, containsInAnyOrder(
                new Entry(8L, ACCESS_TYPE_NODE_AND_CHILDREN, "/"),
                new Entry(63L, ACCESS_TYPE_NODE_AND_CHILDREN, "/merge"),
                new Entry(8L, ACCESS_TYPE_NODE, "/foo"),
                new Entry(63L, ACCESS_TYPE_CHILDREN, "/foo")
        ));
    }

    @Test
    public void saveEntriesWithNullPath() throws Exception {
        //GIVEN
        acl.addEntry(new Entry(0L, ACCESS_TYPE_NODE, null));

        //WHEN
        acl.saveEntries(aclNode);

        //THEN
        assertFalse(aclNode.hasNodes());
    }

    @Test
    public void saveEntriesWithEmptyPath() throws Exception {
        //GIVEN
        acl.addEntry(new Entry(0L, ACCESS_TYPE_NODE, ""));

        //WHEN
        acl.saveEntries(aclNode);

        //THEN
        assertFalse(aclNode.hasNodes());
    }
}