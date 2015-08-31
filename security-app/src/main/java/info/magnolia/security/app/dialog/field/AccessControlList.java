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
package info.magnolia.security.app.dialog.field;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;

import java.util.Collection;
import java.util.LinkedHashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents an access control list as visualized in the UI with the access type separated from the path.
 */
public class AccessControlList {

    public static final String PERMISSIONS_PROPERTY_NAME = "permissions";
    public static final String PATH_PROPERTY_NAME = "path";

    public static final long ACCESS_TYPE_NODE = 1;
    public static final long ACCESS_TYPE_CHILDREN = 2;
    public static final long ACCESS_TYPE_NODE_AND_CHILDREN = ACCESS_TYPE_NODE | ACCESS_TYPE_CHILDREN;

    /**
     * Used for testing equality of entries.
     */
    public static final class EntryKey {

        private long permissions;
        private String path;

        public EntryKey(long permissions, String path) {
            this.permissions = permissions;
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EntryKey entryKey = (EntryKey) o;

            if (permissions != entryKey.permissions) return false;
            if (!path.equals(entryKey.path)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (permissions ^ (permissions >>> 32));
            result = 31 * result + path.hashCode();
            return result;
        }
    }

    /**
     * An entry in the access control list.
     */
    public static class Entry {

        private long permissions;
        private long accessType;
        private String path;

        public Entry(long permissions, long accessType, String path) {
            this.permissions = permissions;
            this.accessType = accessType;
            this.path = path;
        }

        public long getPermissions() {
            return permissions;
        }

        public void setPermissions(long permissions) {
            this.permissions = permissions;
        }

        public long getAccessType() {
            return accessType;
        }

        public void setAccessType(long accessType) {
            this.accessType = accessType;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void merge(Entry entry) {
            accessType |= entry.getAccessType();
        }

        public EntryKey createKey() {
            return new EntryKey(permissions, path);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;

            return createKey().equals(((Entry) o).createKey());
        }

        @Override
        public int hashCode() {
            return createKey().hashCode();
        }
    }

    private LinkedHashMap<EntryKey, Entry> entries = new LinkedHashMap<EntryKey, Entry>();

    public Collection<Entry> getEntries() {
        return entries.values();
    }

    public void addEntry(Entry entry) {
        EntryKey key = entry.createKey();
        Entry existingEntry = entries.get(key);
        if (existingEntry == null) {
            entries.put(key, entry);
        } else {
            existingEntry.merge(entry);
        }
    }

    public void readEntries(Node aclNode) throws RepositoryException {
        for (Node entryNode : NodeUtil.getNodes(aclNode)) {
            readEntry(entryNode);
        }
    }

    public void readEntry(Node entryNode) throws RepositoryException {
        addEntry(doGetEntryFromNode(entryNode));
    }

    public Entry getEntryByNode(Node entryNode) throws RepositoryException {
        Entry entry = doGetEntryFromNode(entryNode);
        Entry existingEntry = entries.get(entry.createKey());
        if (existingEntry != null) {
            return existingEntry;
        }
        return entry;
    }

    private Entry doGetEntryFromNode(Node entryNode) throws RepositoryException {
        long permissions = entryNode.getProperty(PERMISSIONS_PROPERTY_NAME).getLong();
        String path = entryNode.getProperty(PATH_PROPERTY_NAME).getString();

        long accessType;

        if (path.endsWith("/*")) {
            accessType = ACCESS_TYPE_CHILDREN;
            path = path.equals("/*") ?  "/" : StringUtils.substringBeforeLast(path, "/*");
        } else {
            accessType = ACCESS_TYPE_NODE;
            path = path.equals("/") ?  path : StringUtils.removeEnd(path, "/");
        }

        return new Entry(permissions, accessType, path);
    }

    public void saveEntries(Node aclNode) throws RepositoryException {
        for (Entry entry : entries.values()) {

            Node entryNode = aclNode.addNode(Path.getUniqueLabel(aclNode.getSession(), aclNode.getPath(), "0"), NodeTypes.ContentNode.NAME);

            String path = entry.getPath();
            long permissions = entry.getPermissions();
            long accessType = entry.getAccessType();

            String suffixForChildren = path.equals("/") ? "*" : "/*";
            switch ((int) accessType) {
            case (int) ACCESS_TYPE_CHILDREN:
                path += suffixForChildren;
                break;
            case (int) ACCESS_TYPE_NODE_AND_CHILDREN:
                String nodeName = Path.getUniqueLabel(aclNode.getSession(), aclNode.getPath(), "0");
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
