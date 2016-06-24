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
package info.magnolia.security.app.dialog.field;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;

/**
 * A simple ACL representation reading entries from, and saving them to a given JCR node.
 * Entries simply consist of a permission value and a path.
 *
 * @param <E> the type of Access Control Entries (ACEs) managed by this list.
 *
 * @see WebAccessFieldFactory
 */
public class AccessControlList<E extends AccessControlList.Entry> {

    /** @deprecated since 5.4.8, intended for internal usage only (package visibility) */
    @Deprecated
    public static final String PATH_PROPERTY_NAME = "path";

    /** @deprecated since 5.4.8, intended for internal usage only (package visibility) */
    @Deprecated
    public static final String PERMISSIONS_PROPERTY_NAME = "permissions";

    /** @deprecated since 5.4.8, moved to {@link WorkspaceAccessControlList#ACCESS_TYPE_NODE} */
    @Deprecated
    public static final long ACCESS_TYPE_NODE = 1;

    /** @deprecated since 5.4.8, moved to {@link WorkspaceAccessControlList#ACCESS_TYPE_CHILDREN} */
    @Deprecated
    public static final long ACCESS_TYPE_CHILDREN = 2;

    /** @deprecated since 5.4.8, moved to {@link WorkspaceAccessControlList#ACCESS_TYPE_NODE_AND_CHILDREN} */
    @Deprecated
    public static final long ACCESS_TYPE_NODE_AND_CHILDREN = ACCESS_TYPE_NODE | ACCESS_TYPE_CHILDREN;

    private List<E> entries = new ArrayList<>();

    public Collection<E> getEntries() {
        return Collections.unmodifiableCollection(entries);
    }

    public void addEntry(E entry) {
        entries.add(entry);
    }

    public void removeEntry(E entry) {
        entries.remove(entry);
    }

    /**
     * Initializes this {@link AccessControlList} by reading existing {@linkplain AccessControlList.Entry entries} from the given ACL node.
     *
     * @param aclNode the JCR node corresponding to the given ACL, usually named <code>acl_&lt;foobar&gt;</code>
     */
    public void readEntries(Node aclNode) throws RepositoryException {
        this.entries.clear();
        Collection<E> entries = createEntries(aclNode);
        this.entries.addAll(entries);
    }

    protected Collection<E> createEntries(Node aclNode) throws RepositoryException {
        List<E> entries = new ArrayList<>();
        for (Node entryNode : NodeUtil.getNodes(aclNode)) {
            E entry = createEntry(entryNode);
            entries.add(entry);
        }
        return entries;
    }

    /**
     * Default implementation simply retrieves the path and permissions values from corresponding JCR properties,
     * and delegates entry construction to {@link #doCreateRawEntry(long, String)}.
     */
    protected E createEntry(Node entryNode) throws RepositoryException {
        long permissions = entryNode.getProperty(PERMISSIONS_PROPERTY_NAME).getLong();
        String path = entryNode.getProperty(PATH_PROPERTY_NAME).getString();
        return doCreateRawEntry(permissions, path);
    }

    /**
     * Subclasses of {@link AccessControlList} may override this to create their own specialized {@link AccessControlList.Entry Entries}.
     */
    protected E doCreateRawEntry(long permissions, String path) {
        return (E) new Entry(permissions, path);
    }

    /**
     * Saves {@linkplain AccessControlList.Entry entries} of this {@link AccessControlList} (back) to the given ACL node.
     * @param aclNode the JCR node corresponding to the given ACL, usually named <code>acl_&lt;foobar&gt;</code>
     */
    public void saveEntries(Node aclNode) throws RepositoryException {
        for (E entry : entries) {
            // Ignore entries with empty path
            if (StringUtils.isNotEmpty(entry.getPath())) {
                Node entryNode = aclNode.addNode(Path.getUniqueLabel(aclNode, "0"), NodeTypes.ContentNode.NAME);
                entryNode.setProperty(PATH_PROPERTY_NAME, entry.getPath());
                entryNode.setProperty(PERMISSIONS_PROPERTY_NAME, entry.getPermissions());
            }
        }
    }

    /** @deprecated since 5.4.8, replaced by {@link #addEntry(Entry)} and {@link #createEntry(Node)} */
    @Deprecated
    public void readEntry(Node entryNode) throws RepositoryException {
        addEntry(createEntry(entryNode));
    }

    /** @deprecated since 5.4.8, replaced by {@link #createEntry(Node)} and {@link #doCreateRawEntry(long, String)}*/
    @Deprecated
    public Entry getEntryByNode(Node entryNode) throws RepositoryException {
        return createEntry(entryNode);
    }

    /**
     * An entry in the access control list.
     */
    public static class Entry {

        private long permissions;
        private String path;

        public Entry(long permissions, String path) {
            this.permissions = permissions;
            this.path = path;
        }

        /** @deprecated since 5.4.8, accessType was moved to {@link WorkspaceAccessControlList.Entry#merge(WorkspaceAccessControlList.Entry)} */
        @Deprecated
        public Entry(long permissions, long accessType, String path) {
            this(permissions, path);
        }

        public long getPermissions() {
            return permissions;
        }

        public void setPermissions(long permissions) {
            this.permissions = permissions;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Deprecated
        public long getAccessType() {
            return 0;
        }

        @Deprecated
        public void setAccessType(long accessType) {
        }

        /** @deprecated since 5.4.8, moved to {@link WorkspaceAccessControlList.Entry#merge(WorkspaceAccessControlList.Entry)} */
        @Deprecated
        public void merge(Entry entry) {
        }

        /** @deprecated since 5.4.8, keys are not needed any more on simple entries without access type */
        @Deprecated
        public EntryKey createKey() {
            return new EntryKey(permissions, path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (permissions != entry.permissions) return false;
            return path.equals(entry.path);

        }

        @Override
        public int hashCode() {
            int result = (int) (permissions ^ (permissions >>> 32));
            result = 31 * result + path.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("AccessControlList.Entry: %s\t\"%s\"", PermissionImpl.getPermissionAsName(permissions), path);
        }
    }

    /**
     * Used for testing equality of entries.
     *
     * @deprecated since 5.4.8, equality is implemented on the entry directly, while same-key merging logic was moved
     * to the more specific {@link WorkspaceAccessControlList}
     */
    @Deprecated
    public static final class EntryKey extends Entry {
        public EntryKey(long permissions, String path) {
            super(permissions, path);
        }
    }
}
