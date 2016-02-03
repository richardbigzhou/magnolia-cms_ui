/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.dom;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a node inside the tree structure built by the {@link info.magnolia.ui.vaadin.gwt.client.editor.dom.processor.CommentProcessor}.
 * Holds a reference to its parent node and a list to its children and allows navigating inside the tree structure.
 *
 * @see MgnlPage
 * @see MgnlArea
 * @see MgnlComponent
 */
public class CmsNode {
    protected CmsNode parent;
    private LinkedList<CmsNode> children = new LinkedList<CmsNode>();

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

    public MgnlArea getParentArea() {
        MgnlArea parentArea = null;
        for (CmsNode parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof MgnlArea) {
                parentArea = (MgnlArea) parent;
                break;
            }
        }
        return parentArea;
    }

    public MgnlArea getRootArea() {
        MgnlArea parentArea = (this instanceof MgnlArea) ? (MgnlArea) this : null;
        for (CmsNode parent = getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof MgnlArea) {
                parentArea = (MgnlArea) parent;
            }
        }
        return parentArea;
    }

    public List<MgnlComponent> getComponents() {
        List<MgnlComponent> components = new LinkedList<MgnlComponent>();
        for (CmsNode element : getChildren()) {
            if (element instanceof MgnlComponent) {
                components.add((MgnlComponent) element);
            }
        }
        return components;
    }

    public List<MgnlArea> getAreas() {
        List<MgnlArea> areas = new LinkedList<MgnlArea>();
        for (CmsNode element : getChildren()) {
            if (element instanceof MgnlArea) {
                areas.add((MgnlArea) element);
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
        return relative != null && this.getRootArea() == relative.getRootArea();
    }

    public void delete() {
        for (CmsNode child : getChildren()) {
            if (getParent() != null) {
                getParent().getChildren().add(child);
            }
            child.setParent(getParent());
        }
    }

    public MgnlElement asMgnlElement() {
        return (MgnlElement) this;
    }

    public int getLevel() {
        int level = 0;
        for (CmsNode parent = getParent(); parent != null; parent = parent.getParent()) {
            level++;
        }
        return level;
    }

}
