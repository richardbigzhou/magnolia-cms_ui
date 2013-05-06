/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.workbench.list;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.workbench.AbstractContentPresenter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.definition.NodeTypeDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

/**
 * The ListPresenter.
 */
public class ListPresenter extends AbstractContentPresenter implements ListView.Listener {

    private final ListView view;

    private AbstractJcrContainer container;

    private final ComponentProvider componentProvider;

    @Inject
    public ListPresenter(ListView view, ComponentProvider componentProvider) {
        this.view = view;
        this.componentProvider = componentProvider;
    }

    @Override
    public ListView start(WorkbenchDefinition workbench, EventBus eventBus) {
        super.start(workbench, eventBus);

        this.container = createContainer(workbench);
        view.setContainer(container);

        // build columns
        List<Object> editableColumns = new ArrayList<Object>();

        final Iterator<ColumnDefinition> it = workbench.getColumns().iterator();
        while (it.hasNext()) {
            ColumnDefinition column = it.next();

            String propertyId = column.getPropertyName() != null ? column.getPropertyName() : column.getName();
            String title = column.getLabel();
            container.addContainerProperty(propertyId, column.getType(), null);

            if (column.getWidth() > 0) {
                view.addColumn(propertyId, title, column.getWidth());
            } else if (column.getExpandRatio() > 0) {
                view.addColumn(propertyId, title, column.getExpandRatio());
            } else {
                view.addColumn(propertyId, column.getLabel());
            }

            if (column.getFormatterClass() != null) {
                view.setColumnFormatter(propertyId, componentProvider.newInstance(column.getFormatterClass(), column));
            }

            if (column.isEditable()) {
                editableColumns.add(propertyId);
            }
        }

        // inplace-editing
        if (workbench.isEditable()) {
            view.setEditable(true);
            // editingDelegate = new InplaceEditingDelegate(view.asVaadinComponent());
            // editingDelegate.setEditableColumns(editableColumns.toArray());
            // editingDelegate.addListener(new ItemEditedEvent.Handler() {
            //
            // @Override
            // public void onItemEdited(ItemEditedEvent event) {
            // presenterOnEditItem(event);
            // }
            // });
        }

        // node icons
        List<NodeTypeDefinition> nodeTypes = workbench.getNodeTypes();
        for (NodeTypeDefinition nodeType : nodeTypes) {
            if (nodeType.getIcon() != null) {
                view.setNodeIcon(nodeType.getName(), nodeType.getIcon());
            }
        }

        // Set Drop Handler
        // if (workbench.getDropConstraintClass() != null) {
        // Class<? extends DropConstraint> dropContainerClass = workbench.getDropConstraintClass();
        // DropConstraint constraint = componentProvider.newInstance(dropContainerClass);
        // DropHandler dropHandler = new TreeViewDropHandler(treeTable, constraint);
        // treeTable.setDropHandler(dropHandler);
        // treeTable.setDragMode(TableDragMode.ROW);
        // log.debug("Set following drop container {} to the treeTable", dropContainerClass.getName());
        // }

        return view;
    }

    @Override
    public void refresh() {
        // This will update the row count and display the newly created items.
        container.refresh();
        container.fireItemSetChange();
    }

    protected AbstractJcrContainer createContainer(WorkbenchDefinition workbench) {
        return new FlatJcrContainer(workbench);
    }

}
