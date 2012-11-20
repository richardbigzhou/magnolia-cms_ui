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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.column.ColumnFormatter;
import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.tree.container.HierarchicalJcrContainer;
import info.magnolia.ui.model.column.definition.ColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.vaadin.integration.jcr.container.AbstractJcrContainer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TreeTable;


/**
 * Vaadin UI component that displays a tree.
 */
@SuppressWarnings("serial")
public class TreeViewImpl implements TreeView {

    private static final Logger log = LoggerFactory.getLogger(TreeViewImpl.class);

    private final Layout layout;

    private final TreeTable treeTable;

    private final HierarchicalJcrContainer container;

    private ContentView.Listener listener;

    private Set< ? > defaultValue = null;

    /**
     * Instantiates a new content tree view.
     * 
     * @param workbench the workbench definition
     * @param componentProvider the component provider
     * @param container the container data source
     */
    public TreeViewImpl(WorkbenchDefinition workbench, ComponentProvider componentProvider, HierarchicalJcrContainer container) {
        this.container = container;

        treeTable = buildTreeTable(container, workbench, componentProvider);
        layout = buildLayout();
        layout.addComponent(treeTable);

        // if (workbenchDefinition.isEditable()) {
        // tree.addListener(new ItemClickListener() {
        //
        // private Object previousSelection;
        //
        // @Override
        // public void itemClick(ItemClickEvent event) {
        // if (event.isDoubleClick()) {
        // ((InplaceEditingTreeTable) tree).setEditing(event.getItemId(), event.getPropertyId());
        // } else {
        // // toggle will deselect
        // if (previousSelection == event.getItemId()) {
        // tree.setValue(null);
        // }
        // }
        //
        // previousSelection = event.getItemId();
        // }
        // });
        // } else {

        treeTable.addListener(new ItemClickEvent.ItemClickListener() {

            private Object previousSelection;

            @Override
            public void itemClick(ItemClickEvent event) {
                Object currentSelection = event.getItemId();
                if (event.isDoubleClick()) {
                    presenterOnDoubleClick(String.valueOf(event.getItemId()));
                } else {
                    // toggle will deselect
                    if (previousSelection == currentSelection) {
                        treeTable.setValue(null);
                    }
                }

                previousSelection = currentSelection;
            }
        });
        // }

        treeTable.addListener(new TreeTable.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                if (defaultValue == null && event.getProperty().getValue() instanceof Set) {
                    defaultValue = (Set< ? >) event.getProperty().getValue();
                }
                final Object value = event.getProperty().getValue();
                if (value instanceof String) {
                    presenterOnItemSelection(String.valueOf(value));
                } else if (value instanceof Set) {
                    final Set< ? > set = new HashSet<Object>((Set< ? >) value);
                    set.removeAll(defaultValue);
                    if (set.size() == 1) {
                        presenterOnItemSelection(String.valueOf(set.iterator().next()));
                    } else if (set.size() == 0) {
                        presenterOnItemSelection(null);
                        treeTable.setValue(null);
                    }
                }
            }
        });

    }

    // CONFIGURE TREE TABLE

    private TreeTable buildTreeTable(Container container, WorkbenchDefinition workbench, ComponentProvider componentProvider) {

        TreeTable treeTable = workbench.isEditable() ? new InplaceEditingTreeTable() : new MagnoliaTreeTable();

        // basic widget configuration
        treeTable.setNullSelectionAllowed(true);
        treeTable.setColumnCollapsingAllowed(false);
        treeTable.setColumnReorderingAllowed(false);
        treeTable.setCollapsed(treeTable.firstItemId(), false);
        treeTable.setSizeFull();

        // data model
        treeTable.setContainerDataSource(container);
        buildColumns(treeTable, container, workbench.getColumns(), componentProvider);

        // listeners

        return treeTable;
    }

    /**
     * Sets the columns for the vaadin TreeTable, based on workbench columns configuration.
     * 
     * @param treeTable the TreeTable vaadin component
     * @param container the container data source
     * @param columns the list of ColumnDefinitions
     * @param componentProvider the component provider
     */
    protected void buildColumns(TreeTable treeTable, Container container, List<ColumnDefinition> columns, ComponentProvider componentProvider) {
        final Iterator<ColumnDefinition> iterator = columns.iterator();
        final List<String> visibleColumns = new ArrayList<String>();
        final List<String> editableColumns = new ArrayList<String>();

        while (iterator.hasNext()) {
            final ColumnDefinition column = iterator.next();
            final String columnProperty = column.getPropertyName() != null ? column.getPropertyName() : column.getName();

            // Add data column
            container.addContainerProperty(columnProperty, column.getType(), "");
            visibleColumns.add(columnProperty);

            // Set appearance
            treeTable.setColumnHeader(columnProperty, column.getLabel());
            if (column.getWidth() > 0) {
                treeTable.setColumnWidth(columnProperty, column.getWidth());
            } else {
                treeTable.setColumnExpandRatio(columnProperty, column.getExpandRatio());
            }

            // Generated columns
            String formatterClass = column.getFormatterClass();
            if (StringUtils.isNotBlank(formatterClass)) {
                try {
                    ColumnFormatter formatter = (ColumnFormatter) componentProvider.newInstance(Class.forName(formatterClass), column);
                    treeTable.addGeneratedColumn(columnProperty, formatter);
                } catch (ClassNotFoundException e) {
                    log.error("Formatter class could not be found.", e);
                }
            }

            // Inplace editing
            if (column.isEditable()) {
                editableColumns.add(columnProperty);
            }
        }

        treeTable.setVisibleColumns(visibleColumns.toArray());
        if (treeTable instanceof InplaceEditingTreeTable) {
            ((InplaceEditingTreeTable) treeTable).setEditableColumns(editableColumns.toArray());
        }
    }

    // CONTENT VIEW IMPL

    @Override
    public void select(String path) {
        treeTable.select(path);
    }

    @Override
    public void refresh() {
        container.refresh();
        container.fireItemSetChange();
    }

    @Override
    public AbstractJcrContainer getContainer() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support this operation");
    }

    @Override
    public ViewType getViewType() {
        return ViewType.TREE;
    }

    @Override
    public void setListener(ContentView.Listener listener) {
        this.listener = listener;
    }

    private void presenterOnItemSelection(String id) {
        if (listener != null) {
            listener.onItemSelection(treeTable.getItem(id));
        }
    }

    private void presenterOnDoubleClick(String id) {
        if (listener != null) {
            listener.onDoubleClick(treeTable.getItem(id));
        }
    }

    // VAADIN VIEW

    private Layout buildLayout() {
        CssLayout layout = new CssLayout();
        layout.setStyleName("mgnl-content-view");
        layout.setSizeFull();
        return layout;
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

}
