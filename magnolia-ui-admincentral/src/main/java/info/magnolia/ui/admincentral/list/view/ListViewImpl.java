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
package info.magnolia.ui.admincentral.list.view;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.column.ColumnFormatter;
import info.magnolia.ui.admincentral.container.AbstractJcrContainer;
import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.list.container.FlatJcrContainer;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.model.column.definition.ColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.widget.grid.MagnoliaTable;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;


/**
 * Vaadin UI component that displays a list.
 *
 */
public class ListViewImpl implements ListView {

    private ContentView.Listener listener;

    private final Table table;

    private final VerticalLayout margin = new VerticalLayout();

    private final AbstractJcrContainer container;

    private static final Logger log = LoggerFactory.getLogger(ListViewImpl.class);

    public ListViewImpl(WorkbenchDefinition workbenchDefinition, TreeModel treeModel, ComponentProvider componentProvider) {
        table = new MagnoliaTable();
        table.setSizeFull();

        // next two lines are required to make the browser (Table) react on selection change via
        // mouse
        table.setImmediate(true);
        table.setNullSelectionAllowed(false);
        // table.setMultiSelectMode(MultiSelectMode.DEFAULT);
        table.setMultiSelect(false);

        // Important do not set page length and cache ratio on the Table, rather set them by using
        // AbstractJcrContainer corresponding methods. Setting
        // those value explicitly on the Table will cause the same jcr query to be repeated twice
        // thus degrading performance greatly.
        table.addListener(new Table.ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                log.debug("Handle value change Event: {}", event.getProperty().getValue());
                presenterOnItemSelection((String) event.getProperty().getValue());
            }
        });

        table.addListener(new ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                if(event.isDoubleClick()) {
                    presenterOnDoubleClick(String.valueOf(event.getItemId()));
                }
            }
        });

        table.setEditable(false);
        table.setSelectable(true);
        table.setColumnCollapsingAllowed(true);

        table.setColumnReorderingAllowed(false);

        container = new FlatJcrContainer(treeModel, workbenchDefinition);
        table.setContainerDataSource(container);
        // Set Column definition.
        Iterator<ColumnDefinition> iterator = workbenchDefinition.getFilteredColumns().iterator();
        while (iterator.hasNext()) {
            ColumnDefinition column = iterator.next();
//            if(workbenchDefinition.isDialogWorkbench() && ! column.isToDisplayInDialog()) {
//                continue;
//            }
            String columnName = column.getName();
            String columnProperty = "";
            if (column.getPropertyName() != null) {
                columnProperty = column.getPropertyName();
            } else {
                columnProperty = columnName;
            }
            //table.setColumnExpandRatio(columnProperty, column.getWidth() <= 0 ? 1 :column.getWidth());
            table.setColumnHeader(columnProperty, column.getLabel());
            container.addContainerProperty(columnProperty, column.getType(), "");
            //Set Formatter
            if(StringUtils.isNotBlank(column.getFormatterClass())) {
                try {
                    table.addGeneratedColumn(columnName, (ColumnFormatter)componentProvider.newInstance(Class.forName(column.getFormatterClass()),column));
                }
                catch (ClassNotFoundException e) {
                    log.error("Not able to create the Formatter",e);
                }
            }
        }
        //FIXME fgrilli: we have to set the container data source twice. We set it here so
        //that the table actually contains data.
        table.setContainerDataSource(container);

        margin.setStyleName("mgnl-content-view");
        margin.addComponent(table);
    }

    @Override
    public void select(String path) {
        table.select(path);
    }

    @Override
    public void refresh() {
        // This will update the row count and display the newly
        // created items.
        container.refresh();
        container.fireItemSetChange();
    }

    @Override
    public Component asVaadinComponent() {
        return margin;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public AbstractJcrContainer getContainer() {
        return container;
    }

    private void presenterOnItemSelection(String id) {
        if (listener != null) {
            com.vaadin.data.Item item = container.getItem(id);
            listener.onItemSelection(item);
        }
    }

    private void presenterOnDoubleClick(String id) {
        if (listener != null) {
            listener.onDoubleClick(table.getItem(id));
        }
    }

    @Override
    public void refreshItem(Item item) {
        String itemId = ((JcrItemAdapter) item).getItemId();
        if (container.containsId(itemId)) {
            container.fireItemSetChange();
        } else {
            log.warn("No item found for id [{}]", itemId);
        }
    }
}
