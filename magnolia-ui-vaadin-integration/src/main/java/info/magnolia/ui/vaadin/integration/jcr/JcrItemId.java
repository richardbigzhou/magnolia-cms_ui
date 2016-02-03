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
package info.magnolia.ui.vaadin.integration.jcr;

import java.io.Serializable;

/**
 * Generic {@link javax.jcr.Item} item id, holds item uuid and the workspace name.
 */
public class JcrItemId implements Serializable {

    private String uuid;

    private String workspace;

    public JcrItemId(String uuid, String workspace) {
        this.workspace = workspace;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getWorkspace() {
        return workspace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JcrItemId)) return false;

        JcrItemId jcrItemId = (JcrItemId) o;

        if (uuid != null ? !uuid.equals(jcrItemId.uuid) : jcrItemId.uuid != null) return false;
        if (workspace != null ? !workspace.equals(jcrItemId.workspace) : jcrItemId.workspace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (workspace != null ? workspace.hashCode() : 0);
        return result;
    }
}
