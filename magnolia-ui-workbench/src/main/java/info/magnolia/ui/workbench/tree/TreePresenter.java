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
package info.magnolia.ui.workbench.tree;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.list.ListPresenter;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;
import info.magnolia.ui.workbench.tree.drop.TreeViewDropHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.TreeTable;

/**
 * The TreePresenter is responsible for creating, configuring and updating a tree of items according to the workbench definition.
 */
public class TreePresenter extends ListPresenter implements TreeView.Listener {

    private static final Logger log = LoggerFactory.getLogger(TreePresenter.class);

    @Inject
    public TreePresenter(TreeView view, ComponentProvider componentProvider) {
        super(view, componentProvider);
    }

    @Override
    public TreeView start(WorkbenchDefinition workbench, EventBus eventBus, String viewTypeName) {
        TreeView view = (TreeView) super.start(workbench, eventBus, viewTypeName);

        // inplace-editing
        if (workbench.isEditable()) {

            List<Object> editableColumns = new ArrayList<Object>();

            Iterator<ColumnDefinition> it = getColumnsIterator();
            while (it.hasNext()) {
                ColumnDefinition column = it.next();

                String propertyId = column.getPropertyName() != null ? column.getPropertyName() : column.getName();
                if (column.isEditable()) {
                    editableColumns.add(propertyId);
                }
            }
            view.setEditableColumns(editableColumns.toArray());
            view.setEditable(true);
        }

        // Drag and Drop
        Class<? extends DropConstraint> dropConstraintClass = workbench.getDropConstraintClass();
        if (dropConstraintClass != null) {
            DropConstraint constraint = getComponentProvider().newInstance(dropConstraintClass);
            DropHandler dropHandler = new TreeViewDropHandler((TreeTable) view.asVaadinComponent(), constraint);
            view.setDragAndDropHandler(dropHandler);
            log.debug("Set following drop container {} to the treeTable", dropConstraintClass.getName());
        }

        return view;
    }


    public void disableDragAndDrop() {
        ((TreeView) view).setDragAndDropHandler(null);
    }

    @Override
    protected HierarchicalJcrContainer createContainer(WorkbenchDefinition workbench) {
        return new HierarchicalJcrContainer(workbench);
    }

    // TREE VIEW LISTENER IMPL

    @Override
    public void onItemEdited(Item item) {
        try {
            if (item != null) {
                log.debug("com.vaadin.data.Item edited. Firing ItemEditedEvent...");
                eventBus.fireEvent(new ItemEditedEvent(item));
            } else {
                log.warn("Null item edited");
            }
        } catch (Exception e) {
            log.error("An error occurred while double clicking on a row in the data grid", e);
        }

        // Clear preOrder cache of itemIds in case node was renamed
        getContainer().fireItemSetChange();
    }

    @Override
    public void expand(String itemId) {
        view.expand(itemId);
    }

}
