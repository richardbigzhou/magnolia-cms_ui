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

/**
 * Special type of {@link JcrItemId} which identifies Vaadin {@link com.vaadin.data.Item}
 * associated with a new {@link javax.jcr.Node}.
 */
public class JcrNewNodeItemId extends JcrNodeItemId {

    private String name;

    private String primaryNodeType;

    public JcrNewNodeItemId(String uuid, String workspace, String primaryNodeType, String name) {
        super(uuid, workspace);
        this.name = name;
        this.primaryNodeType = primaryNodeType;
    }

    public JcrNewNodeItemId(String uuid, String workspace, String primaryNodeType) {
        this(uuid, workspace, primaryNodeType, null);
    }

    public String getName() {
        return name;
    }

    public String getPrimaryNodeType() {
        return primaryNodeType;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JcrNewNodeItemId)) return false;
        if (!super.equals(o)) return false;

        JcrNewNodeItemId that = (JcrNewNodeItemId) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
