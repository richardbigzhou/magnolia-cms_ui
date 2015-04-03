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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Base drop constraint for content apps.
 *
 * Compares node types of source and target nodes.
 */
public class BaseDropConstraint extends AlwaysTrueDropConstraint implements DropConstraint {

    private static final Logger log = LoggerFactory.getLogger(BaseDropConstraint.class);
    private final String nodeType;

    public BaseDropConstraint(final String nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public boolean allowedAsChild(Item sourceItem, Item targetItem) {
        try {
            JcrNodeAdapter source = (JcrNodeAdapter) sourceItem;
            JcrNodeAdapter target = (JcrNodeAdapter) targetItem;
            String sourceNodeType = source.applyChanges().getPrimaryNodeType().getName();
            String targetNodeType = target.applyChanges().getPrimaryNodeType().getName();
            // If both nodes are of specific nodeType, return false.
            if (nodeType.equals(sourceNodeType) && nodeType.equals(targetNodeType)) {
                log.debug("Could not move a node type '{}' under a node type '{}'", targetNodeType, nodeType);
                return false;
            }
            // If source is a folder and target of nodeType, return false
            if (NodeTypes.Folder.NAME.equals(sourceNodeType) && nodeType.equals(targetNodeType)) {
                log.debug("Could not move a Folder under a node type '{}'", targetNodeType);
                return false;
            }
            // We do not allow same name siblings (MGNLUI-1292)
            final Node targetNode = target.getJcrItem();
            if (targetNode.hasNode(source.getNodeName())) {
                return false;
            }
        } catch (RepositoryException e) {
            log.warn("Could not check if child is allowed. ", e);
        }
        return true;
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
        return true;
    }
}
