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

import static info.magnolia.jcr.util.NodeUtil.*;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.MoveHandler;
import info.magnolia.ui.workbench.tree.MoveLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
 * Vaadin {@link DropHandler} for moving JCR nodes and properties in trees, with Drag and Drop.
 * <p>
 * Additionally, the dropping conditions can be restricted, by implementing a {@link DropConstraint}
 * and configuring it in the {@link info.magnolia.ui.workbench.definition.WorkbenchDefinition WorkbenchDefinition}.
 */
public class TreeViewDropHandler implements MoveHandler, DropHandler {

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
        // Get id's of the target item
        Object targetItemId = target.getItemIdOver();
        // On which side of the target the item was dropped
        VerticalDropLocation location = target.getDropLocation();
        if (location == null) {
            log.debug("DropLocation is null. Do nothing.");
            return;
        }
        // Get id's of the dragged items
        Iterator<Object> selected = getItemIdsToMove(dropEvent).iterator();
        while (selected.hasNext()) {
            Object sourceItemId = selected.next();
            moveNode(sourceItemId, targetItemId, location);
        }
    }

    /**
     * Returns a collection of itemIds to move:
     * <ul>
     * <li>all <em>selected</em> itemIds if and only if the dragging node is <em>also</em> selected</li>
     * <li>only the dragging itemId if it's not selected</li>.
     * </ul>
     */
    private Collection<Object> getItemIdsToMove(DragAndDropEvent dropEvent) {
        Transferable t = dropEvent.getTransferable();
        Object draggingItemId = ((DataBoundTransferable) t).getItemId();

        // all selected itemIds if and only if the dragging node is also selected
        Set<Object> selectedItemIds = (Set<Object>) ((TreeTable) t.getSourceComponent()).getValue();
        if (selectedItemIds.contains(draggingItemId)) {
            return selectedItemIds;
        }

        // only the dragging itemId if it's not selected
        return Arrays.asList(draggingItemId);
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
                moveItem(sourceItem, targetItem, MoveLocation.INSIDE);
                container.setParent(sourceItemId, targetItemId);
            }
        } else {
            Object parentId = container.getParent(targetItemId);
            if (location == VerticalDropLocation.TOP) {
                if (parentId != null && constraint.allowedBefore(sourceItem, targetItem)) {
                    // move first in the container
                    moveItem(sourceItem, targetItem, MoveLocation.BEFORE);
                    container.setParent(sourceItemId, parentId);
                }
            } else if (location == VerticalDropLocation.BOTTOM) {
                if (parentId != null && constraint.allowedAfter(sourceItem, targetItem)) {
                    moveItem(sourceItem, targetItem, MoveLocation.AFTER);
                    container.setParent(sourceItemId, parentId);
                }
            }
        }
    }

    /**
     * Create a serverSide {@link AcceptCriterion} based on the {@link DropConstraint} implementation.
     */
    private void createAcceptCriterion() {
        serverSideCriterion = new ServerSideCriterion() {

            @Override
            public boolean accept(DragAndDropEvent dragEvent) {
                boolean res = true;
                Iterator<Object> selected = getItemIdsToMove(dragEvent).iterator();
                while (selected.hasNext()) {
                    Object sourceItemId = selected.next();
                    HierarchicalJcrContainer container = (HierarchicalJcrContainer) tree.getContainerDataSource();
                    JcrItemAdapter sourceItem = (JcrItemAdapter) container.getItem(sourceItemId);
                    res &= constraint.allowedToMove(sourceItem);
                }
                return res;
            }
        };

    }

    /**
     * Performs check for moving items. Evaluates true when first node is of type Node and source and target are not same.
     */
    public boolean basicMoveCheck(Item source, Item target) {
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
    @Override
    public boolean moveItem(com.vaadin.data.Item source, com.vaadin.data.Item target, MoveLocation location) {
        Item sourceItem = ((JcrItemAdapter) source).getJcrItem();
        Item targetItem = ((JcrItemAdapter) target).getJcrItem();
        if (!basicMoveCheck(sourceItem, targetItem)) {
            return false;
        }
        try {
            final Node targetNode = (Node) targetItem;
            if (sourceItem.isNode()) {
                moveNode((Node) sourceItem, targetNode, location);
            } else {
                NodeUtil.moveProperty((Property) sourceItem, targetNode);
            }
            sourceItem.getSession().save();
            return true;
        } catch (RepositoryException re) {
            log.debug("Cannot execute drag and drop action", re);
            return false;
        }
    }

    @Override
    public DropHandler asDropHandler() {
        return this;
    }
}
