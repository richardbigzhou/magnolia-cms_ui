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
package info.magnolia.ui.workbench.tree.drop;

import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Allows only nodes to be moved and prevents nodes from becoming children of properties.
 */
public class NodesAndPropsDropConstraint extends AlwaysTrueDropConstraint implements DropConstraint {

    private static final Logger log = LoggerFactory.getLogger(NodesAndPropsDropConstraint.class);

    @Override
    public boolean allowedAsChild(Item sourceItem, Item targetItem) {
        JcrItemAdapter jcrItem = (JcrItemAdapter) targetItem;
        if (!jcrItem.isNode()) {
            return false;
        }
        Node jcrNode = (Node) jcrItem.getJcrItem();
        JcrItemAdapter sourceJcrItem = (JcrItemAdapter) sourceItem;
        try {
            if (sourceJcrItem.isNode()) {
                Node sourceNode = (Node) sourceJcrItem.getJcrItem();
                boolean isAllowed = jcrNode.getPrimaryNodeType().canAddChildNode(sourceNode.getName(), sourceNode.getPrimaryNodeType().getName());
                boolean wouldBeSameNameSibling = jcrNode.hasNode(sourceNode.getName());
                return isAllowed && !wouldBeSameNameSibling;
            } else {
                Property sourceProperty = (Property) sourceJcrItem.getJcrItem();
                if (sourceProperty.isMultiple()) {
                    return jcrNode.getPrimaryNodeType().canSetProperty(sourceProperty.getName(), sourceProperty.getValues());
                } else {
                    return jcrNode.getPrimaryNodeType().canSetProperty(sourceProperty.getName(), sourceProperty.getValue());
                }
            }
        } catch (RepositoryException e) {
            log.error("Failed to read item (node/property) or it's new parent node while moving item to new location with {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean allowedBefore(Item sourceItem, Item targetItem) {
        return true;
    }

    @Override
    public boolean allowedAfter(Item sourceItem, Item targetItem) {
        return true;
    }

    @Override
    public boolean allowedToMove(Item sourceItem) {
        JcrItemAdapter jcrSourceItem = (JcrItemAdapter) sourceItem;
        return jcrSourceItem != null;
    }

}