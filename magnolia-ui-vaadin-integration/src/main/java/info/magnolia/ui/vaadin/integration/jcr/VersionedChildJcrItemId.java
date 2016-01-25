/**
 * This file Copyright (c) 2016 Magnolia International
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

/**
 * An {@link JcrItemId} used for {@link info.magnolia.cms.core.version.VersionedNodeChild}. Keeps a reference to it's parent
 * {@link VersionedJcrItemId} and the relative path from the parent to this child.
 * It's created in {@link JcrItemUtil#getItemId(javax.jcr.Item)} and used to retrieve the wrapped version child-node in {@link JcrItemUtil#getJcrItem(JcrItemId)}.
 */
public class VersionedChildJcrItemId extends VersionedJcrItemId {

    private VersionedJcrItemId parent;
    private String relPath;

    public VersionedChildJcrItemId(VersionedJcrItemId parent, String relPath) {
        super(parent.getUuid(), parent.getWorkspace(), parent.getVersionName());
        this.parent = parent;
        this.relPath = relPath;
    }

    public String getRelPath() {
        return relPath;
    }

    public VersionedJcrItemId getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = super.equals(o);

        VersionedChildJcrItemId jcrItemId = (VersionedChildJcrItemId) o;
        if (relPath != null ? !relPath.equals(jcrItemId.relPath) : jcrItemId.relPath != null) return false;

        return equals;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 41 * result + (relPath != null ? relPath.hashCode() : 0);
        return result;
    }
}
