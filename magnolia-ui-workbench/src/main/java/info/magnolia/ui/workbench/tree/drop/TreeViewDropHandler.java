/**
 * This file Copyright (c) 2013-2014 Magnolia International
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

import static info.magnolia.jcr.util.NodeUtil.*;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.MoveLocation;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ServerSideCriterion;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.TreeTable;

/**
 * Generic implementation of {@link DropHandler} handling basic {@link Item}.
 * This Handler implementation uses constrained defined in
 * {@link DropConstraint} implemented class.
 */
public class TreeViewDropHandler implements DropHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private TreeTable tree;
    private DropConstraint constraint;
    private AcceptCriterion serverSideCriterion;

    public TreeViewDropHandler() {
        createAcceptCriterion();
    }

    public TreeViewDropHandler(TreeTable tree, DropConstraint constraint) {
        this.tree = tree;
        this.constraint = constraint;
        createAcceptCriterion();
    }

    @Override
    public void drop(DragAndDropEvent dropEvent) {
        // Called whenever a drop occurs on the component

        // Make sure the drag source is the same tree
        Transferable t = dropEvent.getTransferable();

        // First acceptance criteria.
        // Make sure the drag source is the same tree
        if (t.getSourceComponent() != tree || !(t instanceof DataBoundTransferable)) {
            return;
        }

        AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) dropEvent.getTargetDetails();
        // Get ids of the dragged item and the target item
        Object sourceItemId = getSourceId(dropEvent);
        Object targetItemId = target.getItemIdOver();
        // On which side of the target the item was dropped
        VerticalDropLocation location = target.getDropLocation();

        if (location == null) {
            log.debug("DropLocation is null. Do nothing.");
            return;
        }

        moveNode(sourceItemId, targetItemId, location);

    }

    /**
     * Accept dragged Elements.
     */
    @Override
    public AcceptCriterion getAcceptCriterion() {
        return serverSideCriterion;
    }

    /**
     * Move a node within a tree onto, above or below another node depending on
     * the drop location.
     * 
     * @param sourceItemId
     * id of the item to move
     * @param targetItemId
     * id of the item onto which the source node should be moved
     * @param location
     * VerticalDropLocation indicating where the source node was
     * dropped relative to the target node
     */
    private void moveNode(Object sourceItemId, Object targetItemId, VerticalDropLocation location) {
        log.debug("DropLocation: {}", location.name());
        // Get Item from tree
        HierarchicalJcrContainer container = (HierarchicalJcrContainer) tree.getContainerDataSource();
        JcrItemAdapter sourceItem = (JcrItemAdapter) container.getItem(sourceItemId);
        JcrItemAdapter targetItem = (JcrItemAdapter) container.getItem(targetItemId);

        // Sorting goes as
        // - If dropped ON a node, we append it as a child
        // - If dropped on the TOP part of a node, we move/add it before
        // the node
        // - If dropped on the BOTTOM part of a node, we move/add it
        // after the node

        if (location == VerticalDropLocation.MIDDLE) {
            if (constraint.allowedAsChild(sourceItem, targetItem)) {
                // move first in the container
                moveItem(sourceItem.getJcrItem(), targetItem.getJcrItem(), MoveLocation.INSIDE);
                container.setParent(sourceItemId, targetItemId);
            }
        } else {
            Object parentId = container.getParent(targetItemId);
            if (location == VerticalDropLocation.TOP) {
                if (parentId != null && constraint.allowedBefore(sourceItem, targetItem)) {
                // move first in the container
                    moveItem(sourceItem.getJcrItem(), targetItem.getJcrItem(), MoveLocation.BEFORE);
                container.setParent(sourceItemId, parentId);
                }
            } else if (location == VerticalDropLocation.BOTTOM) {
                if (parentId != null && constraint.allowedAfter(sourceItem, targetItem)) {
                    moveItem(sourceItem.getJcrItem(), targetItem.getJcrItem(), MoveLocation.AFTER);
                    container.setParent(sourceItemId, parentId);
                }
            }
        }
    }

    /**
     * Create a serverSide {@link AcceptCriterion} based on the
     * {@link DropConstraint} implementation.
     */
    private void createAcceptCriterion() {
        serverSideCriterion = new ServerSideCriterion() {

            @Override
            public boolean accept(DragAndDropEvent dragEvent) {
                Object sourceItemId = getSourceId(dragEvent);
                HierarchicalJcrContainer container = (HierarchicalJcrContainer) tree.getContainerDataSource();
                JcrItemAdapter sourceItem = (JcrItemAdapter) container.getItem(sourceItemId);

                return constraint.allowedToMove(sourceItem);
            }
        };

    }

    private Object getSourceId(DragAndDropEvent dropEvent) {
        Transferable t = dropEvent.getTransferable();
        return ((DataBoundTransferable) t).getItemId();
    }

    /**
     * Performs check for moving items. Evaluates true when first node is of type Node and source and target are not same.
     */
    public final boolean basicMoveCheck(Item source, Item target) {
        try {
            // target must be node, to allow moving in
            if (!target.isNode()) {
                return false;
            }
            // Source and origin are the same... do nothing
            if (target.getPath().equals(source.getPath()) && target.getSession().getWorkspace().getName().equals(source.getSession().getWorkspace().getName())) {
                return false;
            }
            // Source can not be a child of target.
            return !NodeUtil.isSame((Node) target, source.getParent());
        } catch (RepositoryException re) {
            log.debug("Cannot determine whether drag and drop is possible due to: {}", re.getMessage(), re);
            return false;
        }
    }

    /**
     * Performs move of the source node into target node or next to target node depending on the value of MoveLocation.
     * Move is performed in session-write mode and as such requires explicit call to session.save() after performing the operation.
     */
    public void moveNode(Node nodeToMove, Node newParent, MoveLocation location) throws RepositoryException {
        if (!basicMoveCheck(nodeToMove, newParent)) {
            return;
        }
        switch (location) {
        case INSIDE:
            String newPath = combinePathAndName(newParent.getPath(), nodeToMove.getName());
            nodeToMove.getSession().move(nodeToMove.getPath(), newPath);
            break;
        case BEFORE:
            moveNodeBefore(nodeToMove, newParent);
            break;
        case AFTER:
            moveNodeAfter(nodeToMove, newParent);
            break;
        }
    }

    /**
     * Performs move of the source node or property into target node or next to target node depending on the value of MoveLocation.
     * This method will persist move by calling session.save() explicitly. And will return true/false depending on whether move was successful or not.
     */
    public boolean moveItem(Item source, Item target, MoveLocation location) {
        if (!basicMoveCheck(source, target)) {
            return false;
        }
        try {
            final Node targetNode = (Node) target;
            if (source.isNode()) {
                moveNode((Node) source, targetNode, location);
            } else {
                NodeUtil.moveProperty((Property) source, targetNode);
            }
            source.getSession().save();
            return true;
        } catch (RepositoryException re) {
            log.debug("Cannot execute drag and drop action", re);
            return false;
        }
    }
}
