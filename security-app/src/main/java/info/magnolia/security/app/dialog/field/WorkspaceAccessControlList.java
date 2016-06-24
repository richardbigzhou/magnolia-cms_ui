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

import static java.util.stream.Collectors.toMap;

import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.jcr.util.NodeTypes;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;

/**
 * An extended ACL representation for use with workspace permissions.
 *
 * Workspace ACLs are edited with a simplified list of entries. The access type value (selected node and/or sub-nodes)
 * is derived from the stored paths of entries which share common "base path" (without wildcard) and permission.
 *
 * @see WorkspaceAccessFieldFactory
 */
public class WorkspaceAccessControlList extends AccessControlList<WorkspaceAccessControlList.Entry> {

    static final String ACCESS_TYPE_PROPERTY_NAME = "accessType";

    public static final long ACCESS_TYPE_NODE = 1;
    public static final long ACCESS_TYPE_CHILDREN = 2;
    public static final long ACCESS_TYPE_NODE_AND_CHILDREN = ACCESS_TYPE_NODE | ACCESS_TYPE_CHILDREN;

    /**
     * {@linkplain WorkspaceAccessControlList.Entry Entries} are read and eventually merged with combined access type
     * if two entries are found with same base path and permission.
     */
    @Override
    protected Collection<Entry> createEntries(Node aclNode) throws RepositoryException {
        Collection<Entry> rawEntries = super.createEntries(aclNode);
        return mergeEntries(rawEntries);
    }

    /**
     * Create specialized ACL {@linkplain WorkspaceAccessControlList.Entry entries}, with access type.
     */
    @Override
    protected Entry doCreateRawEntry(long permissions, String path) {
        // use subclass' two-arg ctor, let access type be computed on the fly
        return new Entry(permissions, path);
    }

    /**
     * {@linkplain WorkspaceAccessControlList.Entry Entries} are saved as follows, according to their access types:
     * <ul>
     * <li>{@link #ACCESS_TYPE_NODE}: creates a single node per entry, with base path and permission value</li>
     * <li>{@link #ACCESS_TYPE_CHILDREN}: creates a single node per entry, with path (wildcard appended) and permission value</li>
     * <li>{@link #ACCESS_TYPE_NODE_AND_CHILDREN}: creates two nodes per entry, one for each of the above cases, with same permission value</li>
     * </ul>
     * <p>Therefore, access type value is not persisted directly to ACL entry nodes.</p>
     * <p>This also performs additional cleanup upfront, in case the list contains redundant entries.</p>
     */
    @Override
    public void saveEntries(Node aclNode) throws RepositoryException {

        Collection<Entry> mergedEntries = mergeEntries(getEntries());

        for (Entry entry : mergedEntries) {

            // Ignore entries with empty path
            if (StringUtils.isNotEmpty(entry.getPath())) {
                Node entryNode = aclNode.addNode(Path.getUniqueLabel(aclNode, "0"), NodeTypes.ContentNode.NAME);

                String path = entry.getPath();
                long permissions = entry.getPermissions();
                long accessType = entry.getAccessType();

                String suffixForChildren = path.equals("/") ? "*" : "/*";
                switch ((int) accessType) {
                case (int) ACCESS_TYPE_CHILDREN:
                    path += suffixForChildren;
                    break;
                case (int) ACCESS_TYPE_NODE_AND_CHILDREN:
                    String nodeName = Path.getUniqueLabel(aclNode, "0");
                    Node extraEntry = aclNode.addNode(nodeName, NodeTypes.ContentNode.NAME);
                    extraEntry.setProperty(PATH_PROPERTY_NAME, path + suffixForChildren);
                    extraEntry.setProperty(PERMISSIONS_PROPERTY_NAME, permissions);
                    break;
                }

                entryNode.setProperty(PERMISSIONS_PROPERTY_NAME, permissions);
                entryNode.setProperty(PATH_PROPERTY_NAME, path);
            }
        }
    }

    Collection<Entry> mergeEntries(Collection<Entry> entries) {
        // Group entries by "key", i.e. by common base path (without wildcard) and permission value
        // Map values are the input elements (hence the identity), while collisions are handled by merging entries' access type
        Map<AccessControlList.Entry, Entry> mergedEntriesByKey = entries.stream()
                .distinct()
                .collect(toMap(Entry::getKey, Function.identity(), Entry::merge));

        // Drop the keys, return only merged entries
        return mergedEntriesByKey.values();
    }

    /**
     * An extended {@linkplain AccessControlList.Entry entry} for the workspace ACLs, with notion of access type and sanitized path.
     */
    public static class Entry extends AccessControlList.Entry {
        private long accessType;

        /**
         * Creates a workspace access control entry by decoding access type and base path from the given path.
         */
        public Entry(long permissions, String path) {
            super(permissions, path);
            setPathAndAccessType(path);
        }

        /**
         * Creates a workspace access control entry by decoding base path from the given path.
         * Given access type is applied as is.
         *
         * @throws IllegalArgumentException if access type is different from supported values {@link #ACCESS_TYPE_NODE} (1), {@link #ACCESS_TYPE_CHILDREN} (2) or {@link #ACCESS_TYPE_NODE_AND_CHILDREN} (3)
         */
        public Entry(long permissions, long accessType, String path) throws IllegalArgumentException {
            this(permissions, path);
            if (accessType == 0) {
                throw new IllegalArgumentException("Access type should be one of ACCESS_TYPE_NODE (1), ACCESS_TYPE_CHILDREN (2) or ACCESS_TYPE_NODE_AND_CHILDREN (3)");
            }
            this.accessType = accessType;
        }

        /**
         * Decorates the path setter with sanitation of the path given as input.
         */
        @Override
        public void setPath(String path) {
            setPathAndAccessType(path);
        }

        /**
         * Sanitizes the given path and decodes the access type from it.
         * <p>Implementation is lenient towards malformed paths with repeated consecutive slashes or stars,
         * and will reduce path to its simplest form if there are redundant wildcards.</p>
         * <p>Besides, setting path with a wildcard will tentatively update access type.</p>
         */
        void setPathAndAccessType(String path) {

            if(StringUtils.isEmpty(path)){
                path = StringUtils.EMPTY;
            }

            // First remove repeated consecutive slashes and stars, then trailing slash if any
            path = deduplicateSlashesAndStars(path);
            if (!path.equals("/") && path.endsWith("/")) {
                path = StringUtils.removeEnd(path, "/");
            }

            if (path.endsWith("/*")) {
                // Reduce path to simplest form if there are redundant wildcards
                do {
                    path = StringUtils.removeEnd(path, "/*");
                } while (path.endsWith("/*"));
                if (path.isEmpty()) {
                    path = "/";
                }
                this.accessType |= ACCESS_TYPE_CHILDREN;
            } else {
                // do not add NODE access type if already sanitized
                if (this.accessType == 0) {
                    this.accessType = ACCESS_TYPE_NODE;
                }
            }

            super.setPath(path);
        }

        public long getAccessType() {
            return accessType;
        }

        public void setAccessType(long accessType) {
            this.accessType = accessType;
        }

        /**
         * Creates and returns a new {@link Entry.Key Entry.Key} representing this entry's grouping.
         */
        Entry.Key getKey() {
            return new Entry.Key(getPermissions(), getPath());
        }

        /**
         * Merges two entries together by combining their respective access type values.
         */
        Entry merge(Entry entry) {
            // only base path and permission need to be equal in order to merge
            if (!super.equals(entry)) {
                throw new IllegalArgumentException("Can only merge ACL entries with same base path (without wildcard) and permission");
            }
            accessType |= entry.getAccessType();
            return this;
        }

        private String deduplicateSlashesAndStars(String path) {
            final StringBuilder builder = new StringBuilder();
            char[] chars = path.toCharArray();
            int i = 0;
            char prevChar = 0;
            while (i < chars.length) {
                char c = chars[i];
                if (i <= 0 || c != prevChar || (c != '*' && c != '/')) {
                    prevChar = c;
                    builder.append(c);
                }
                i++;
            }
            return builder.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Entry entry = (Entry) o;
            return accessType == entry.accessType;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (int) (accessType ^ (accessType >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return String.format("WorkspaceAccessControlList.Entry: %s\t%s\t\"%s\"", PermissionImpl.getPermissionAsName(getPermissions()), getAccessTypeName(accessType), getPath());
        }

        /**
         * Entry key is a simplified—incomplete—representation of an {@link Entry}, intended for grouping entries which share common base path (without wildcard) and permission.
         * If two entries have equal keys, then they can be merged together with combined access type.
         */
        class Key extends AccessControlList.Entry {
            Key(long permissions, String path) {
                super(permissions, path);
            }
        }
    }

    private static String getAccessTypeName(long accessType) {
        if (accessType == ACCESS_TYPE_NODE) {
            return "Node";
        } else if (accessType == ACCESS_TYPE_CHILDREN) {
            return "Sub-nodes";
        } else if (accessType == ACCESS_TYPE_NODE_AND_CHILDREN) {
            return "Node and sub-nodes";
        } else {
            return String.format("Undefined (%d)", accessType);
        }
    }
}
