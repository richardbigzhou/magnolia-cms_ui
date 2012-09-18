/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.editor.gwt.client.dom;

import java.util.LinkedList;
import java.util.List;

/**
 * CmsNode.
 */
public class CmsNode {
    protected CmsNode parent;
    private LinkedList<CmsNode> children = new LinkedList<CmsNode>();
    private boolean isPage = false;
    private boolean isArea = false;
    private boolean isComponent = false;

    public CmsNode(CmsNode parent) {
        this.parent = parent;
    }

    public CmsNode getParent() {
        return parent;
    }

    public void setParent(CmsNode parent) {
        this.parent = parent;
    }

    public LinkedList<CmsNode> getChildren() {
        return children;
    }

    public List<CmsNode> getDescendants() {

        List<CmsNode> descendants = new LinkedList<CmsNode>();

        for (CmsNode element : getChildren()) {
            descendants.add(element);
            descendants.addAll(element.getDescendants());
        }
        return descendants;
    }

    public List<CmsNode> getAscendants() {
        List<CmsNode> ascendants = new LinkedList<CmsNode>();
        CmsNode ascendant = parent;
        while (ascendant != null) {
            ascendants.add(ascendant);
            ascendant = ascendant.getParent();
        }
        return ascendants;
    }

    public CmsNode getParentArea() {
        CmsNode parentArea = null;
        for (CmsNode parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent.isArea()) {
                parentArea = parent;
                break;
            }
        }
        return parentArea;
    }

    public List<CmsNode> getComponents() {
        List<CmsNode> components = new LinkedList<CmsNode>();
        for (CmsNode element : getChildren()) {
            if (element.isComponent()) {
                components.add(element);
            }
        }
        return components;
    }

    public List<CmsNode> getAreas() {
        List<CmsNode> areas = new LinkedList<CmsNode>();
        for (CmsNode element : getChildren()) {
            if (element.isArea()) {
                areas.add(element);
            }
        }
        return areas;
    }

    public CmsNode getRoot() {
        CmsNode root = null;
        for (CmsNode parent = this; parent != null; parent = parent.getParent()) {
                root = parent;
        }
        return root;
    }

    public boolean isRelated(CmsNode relative) {

        return relative != null && this.getRoot() == relative.getRoot();
    }

    public void delete() {
        for (CmsNode child : getChildren()) {
            if (getParent() != null) {
                getParent().getChildren().add(child);
            }
            child.setParent(getParent());
        }
    }

    public void setPage(boolean isPage) {
        this.isPage = isPage;
    }

    public void setArea(boolean isArea) {
        this.isArea = isArea;
    }

    public void setComponent(boolean isComponent) {
        this.isComponent = isComponent;
    }

    public boolean isPage() {
        return isPage;
    }

    public boolean isArea() {
        return isArea;
    }

    public boolean isComponent() {
        return isComponent;
    }

    public MgnlElement asMgnlElement() {
        return (MgnlElement) this;
    }
}
