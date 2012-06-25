/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.view;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.column.EditHandler;
import info.magnolia.ui.admincentral.tree.container.HierarchicalJcrContainer;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.TreeTable;

/**
 * User interface component that extends TreeTable and uses a WorkbenchDefinition for layout and invoking command callbacks.
 *
 */
@SuppressWarnings("serial")
public class MagnoliaTreeTable extends TreeTable {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private HierarchicalJcrContainer container;

    private final TreeModel treeModel;

    public MagnoliaTreeTable(WorkbenchDefinition workbenchDefinition, TreeModel treeModel) {
        this.treeModel = treeModel;

        setSizeUndefined();
        setHeight(100, UNITS_PERCENTAGE);
        setEditable(false);
        setSelectable(true);
        setColumnCollapsingAllowed(true);
        setMultiSelect(false);
        setImmediate(true);
        addStyleName("striped");
        setColumnReorderingAllowed(false);

        addDragAndDrop();

        container = new HierarchicalJcrContainer(treeModel, workbenchDefinition);

        for (Column<?> treeColumn : treeModel.getColumns().values()) {
            String columnName = treeColumn.getDefinition().getName();
            // super.setColumnExpandRatio(columnName, treeColumn.getWidth() <= 0 ? 1 :
            // treeColumn.getWidth());
            container.addContainerProperty(columnName, Component.class, "");
            super.setColumnHeader(columnName, treeColumn.getLabel());
        }

        setContainerDataSource(container);

        new EditHandler(this);
    }


    /**
     * Add Drag and Drop functionality to the provided TreeTable.
     */
    private void addDragAndDrop() {
        setDragMode(TableDragMode.ROW);
        setDropHandler(new DropHandler() {
            @Override
            public void drop(DragAndDropEvent event) {

                try {

                    // Wrapper for the object that is dragged
                    Transferable t = event.getTransferable();

                    // Make sure the drag source is the same tree
                    if (t.getSourceComponent() != MagnoliaTreeTable.this) {
                        return;
                    }

                    AbstractSelectTargetDetails target = (AbstractSelectTargetDetails) event.getTargetDetails();
                    // Get ids of the dragged item and the target item
                    Object sourceItemId = t.getData("itemId");
                    Object targetItemId = target.getItemIdOver();
                    // On which side of the target the item was dropped
                    VerticalDropLocation location = target.getDropLocation();

                    log.debug("DropLocation: " + location.name());

                    HierarchicalJcrContainer containerWrapper = (HierarchicalJcrContainer) getContainerDataSource();
                    // Drop right on an item -> make it a child -
                    if (location == VerticalDropLocation.MIDDLE) {
                        JcrItemAdapter sourceItem = (JcrItemAdapter)container.getItem((String) sourceItemId);
                        JcrItemAdapter targetItem = (JcrItemAdapter)container.getItem((String) targetItemId);
                        if (treeModel.moveItem(sourceItem.getJcrItem(), targetItem.getJcrItem())) {
                            setParent(sourceItemId, targetItemId);
                        }
                    }
                    // Drop at the top of a subtree -> make it previous
                    else if (location == VerticalDropLocation.TOP) {
                        Object parentId = containerWrapper.getParent(targetItemId);
                        if (parentId != null) {
                            log.debug("Parent:" + containerWrapper.getItem(parentId));
                            JcrItemAdapter sourceItem = (JcrItemAdapter)container.getItem((String) sourceItemId);
                            JcrItemAdapter targetItem = (JcrItemAdapter)container.getItem((String) targetItemId);
                            if (treeModel.moveItemBefore(sourceItem.getJcrItem(), targetItem.getJcrItem())) {
                                setParent(sourceItemId, targetItemId);
                            }
                        }
                    }

                    // Drop below another item -> make it next
                    else if (location == VerticalDropLocation.BOTTOM) {
                        Object parentId = containerWrapper.getParent(targetItemId);
                        if (parentId != null) {
                            JcrItemAdapter sourceItem = (JcrItemAdapter)container.getItem((String) sourceItemId);
                            JcrItemAdapter targetItem = (JcrItemAdapter)container.getItem((String) targetItemId);
                            if (treeModel.moveItemAfter(sourceItem.getJcrItem(), targetItem.getJcrItem())) {
                                setParent(sourceItemId, targetItemId);
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {

                // FIXME: Use com.vaadin.ui.Table.TableDropCriterion

                return AcceptAll.get();
            }
        });
    }

    public void select(String itemId) {

        if(!container.isRoot(itemId)){
            String parent = container.getParent(itemId);
            while (!container.isRoot(parent)) {
                setCollapsed(parent, false);
                parent = container.getParent(parent);
            }
            //finally expand the root else children won't be visibile.
            setCollapsed(parent, false);
        }

        // Select the item
        select((Object)itemId);

        // Make sure its in view
        // TODO commented out to avoid flicker on selection via place controller while this should definitely be called when navigated by the history
        // setCurrentPageFirstItemId(itemId);
    }

    public void refresh() {
        container.fireItemSetChange();
    }

    public void updateItem(final Item item) {
        String itemId = ((JcrItemAdapter)item).getItemId();
        if (container.containsId(itemId)) {
            container.fireItemSetChange();
        } else {
            log.warn("No item found for Id: "+itemId);
        }

    }

//    @Override
//    public Resource getItemIcon(Object itemId) {
//
//        // FIXME this is not the best place to do it, ideally we could set it when we create a new item (investigate, might not make a difference)
//        try {
//
//            // TODO should getItemIcon be available on JcrContainerSource ?
//
//            String itemIcon = treeModel.getItemIcon(container.getJcrItem((ContainerItemId) itemId));
//            if (itemIcon != null) {
//                String tmp = MgnlContext.getContextPath() + (!itemIcon.startsWith(JCRUtil.PATH_SEPARATOR) ? JCRUtil.PATH_SEPARATOR + itemIcon : itemIcon);
//                return new ExternalResource(tmp);
//            }
//
//        } catch (RepositoryException e) {
//            throw new RuntimeRepositoryException(e);
//        }
//        return super.getItemIcon(itemId);
//    }
}
