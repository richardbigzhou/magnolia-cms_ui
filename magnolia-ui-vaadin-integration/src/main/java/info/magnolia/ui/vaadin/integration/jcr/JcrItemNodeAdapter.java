/**
 * This file Copyright (c) 2012 Magnolia International
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

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Common definition for child handling and corresponding Node access.
 */
public interface JcrItemNodeAdapter extends JcrItemAdapter {

    /**
     * Return the Primary node type Name. This Node type is defined based on the related JCR Node.
     * In case of new Node, the Type is passed during the construction of the new Item or if not
     * defined, the Type is equivalent to the Parent Node Type.
     */
    String getPrimaryNodeTypeName();

    /**
     * Return the corresponding node directly from the JCR repository. <b> The returned Node does
     * not contains all changes done on the current Item, but it's a representation of the current
     * stored Jcr node. </b> To get the Jcr Node including the changes done on the current Item, use
     * getNode().
     */
    Node getJcrItem();

    /**
     * Return the Jcr Node represented by this Item, Including the Modification done on Property and
     * Child Nodes (added or removed).
     */
    Node getModifiedJcrItem() throws RepositoryException;

    /**
     * Return the Jcr Node represented by this Item, Including the Modification done on Property and
     * Child Nodes (added or removed).
     */
    Node getNode();

    /**
     * @param nodeIdentifier AbstractJcrAdapter.getNodeIdentifier().
     * @return child JcrItemNodeAdapter if part of the children, or null if not defined.
     */
    JcrItemNodeAdapter getChild(String nodeIdentifier);

    Map<String, JcrItemNodeAdapter> getChildren();

    /**
     * Add a child JcrItemNodeAdapter to the current Item. <b>Only Child Nodes part of this Map will
     * be persisted into Jcr.</b>
     */
    JcrItemNodeAdapter addChild(JcrItemNodeAdapter child);

    /**
     * Remove a Child Node from the child list. <b>When removing a JcrItemNodeAdapter, this Child
     * will be added to the Remove Child List even if this Item was not part of the current children
     * list. All Item part from the removed list are removed from the Jcr repository.</b>
     */
    boolean removeChild(JcrItemNodeAdapter toRemove);

    /**
     * Return the current Parent Item (If Item is a child). Parent is set by calling addChild(...
     */
    JcrItemNodeAdapter getParent();

    void setParent(JcrItemNodeAdapter parent);

    /**
     * Return the current Node Name. For new Item, this is the name set in the new Item constructor
     * or null if not yet defined.
     */
    String getNodeName();
}
