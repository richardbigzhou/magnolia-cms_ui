/**
 * This file Copyright (c) 2011-2016 Magnolia International
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

import info.magnolia.ui.vaadin.grid.MagnoliaTable;
import info.magnolia.ui.workbench.ContentView;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Table.TableDragMode;

/**
 * Default Vaadin implementation of the list view.
 */
public class ListViewImpl implements ListView {

    private static final Logger log = LoggerFactory.getLogger(ListViewImpl.class);

    private static final int MINIMUM_COLUMN_WIDTH = 46;
    private static final String DELETED_ROW_STYLENAME = "deleted";

    private Table table;

    private ListView.Listener listener;

    protected void initializeTable(Table table) {
        table.setSizeFull();

        table.setImmediate(true);
        table.setSelectable(true);
        table.setMultiSelect(true);
        table.setNullSelectionAllowed(true);

        table.setDragMode(TableDragMode.NONE);
        table.setEditable(false);
        table.setColumnCollapsingAllowed(false);
        table.setColumnReorderingAllowed(false);
        table.setSortEnabled(true);

        table.setCellStyleGenerator(new Table.CellStyleGenerator() {


            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {
                // icon style is expected on the whole table row, not on a column matching a specific propertyId
                if (propertyId == null && itemId != null) {
                    final Item item = source.getContainerDataSource().getItem(itemId);
                    if (item == null) {
                        return DELETED_ROW_STYLENAME;
                    } else {
                        return listener.getIcon(item);
                    }
                }
                return null;
            }
        });

        // this one was re-added since the check-all worked for tree only but nor for list- and search-view, see MGNLUI-1958
        // TODO fgrilli: a workaround for MGNLUI-1651
        table.addStyleName("no-header-checkbox");
        this.table = table;
        bindHandlers();
    }

    protected void bindHandlers() {
        table.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Object value = event.getProperty().getValue();

                if (listener != null) {
                    Set<Object> items;
                    if (value instanceof Set) {
                        items = (Set) value;
                    } else if (value == null) {
                        items = Collections.emptySet();
                    } else {
                        items = new LinkedHashSet<>();
                        items.add(value);
                    }
                    listener.onItemSelection(items);
                }
            }
        });

        table.addItemClickListener(new ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {

                if (event.getButton() == MouseButton.RIGHT) {
                    if (listener != null) {
                        listener.onRightClick(event.getItemId(), event.getClientX(), event.getClientY());
                    }
                } else if (event.isDoubleClick()) {
                    if (listener != null) {
                        listener.onDoubleClick(event.getItemId());
                    }
                } else {
                    Object value = table.getValue();
                    if (value != null) {
                        Set<Object> items;
                        if (value instanceof Set) {
                            items = (Set<Object>) value;
                        } else {
                            items = new LinkedHashSet<>();
                            items.add(value);
                        }
                        if (items.size() == 1 && ObjectUtils.equals(items.iterator().next(), event.getItemId())) {
                            table.setValue(null);
                        }
                    }
                }
            }
        });

        table.addColumnResizeListener(new Table.ColumnResizeListener() {

            @Override
            public void columnResize(ColumnResizeEvent event) {
                if (event.getCurrentWidth() < MINIMUM_COLUMN_WIDTH) {
                    table.setColumnWidth(event.getPropertyId(), MINIMUM_COLUMN_WIDTH);
                }
            }
        });
    }

    protected ListView.Listener getListener() {
        return listener;
    }

    @Override
    public void setListener(ContentView.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setContainer(Container container) {
        table = createTable(container);
        initializeTable(table);
    }

    protected Table createTable(Container container) {
        return new MagnoliaTable(container);
    }

    @Override
    public void addColumn(String propertyId, String title) {
        if (!table.getContainerPropertyIds().contains(propertyId)) {
            log.warn("Ignoring column '{}', container does not support this propertyId.", propertyId);
            return;
        }
        table.setColumnHeader(propertyId, title);
        List<Object> visibleColumns = new ArrayList<Object>(Arrays.asList(table.getVisibleColumns()));
        if (!visibleColumns.contains(propertyId)) {
            visibleColumns.add(propertyId);
        }
        table.setVisibleColumns(visibleColumns.toArray());
    }

    @Override
    public void addColumn(String propertyId, String title, int width) {
        addColumn(propertyId, title);
        table.setColumnWidth(propertyId, width);
    }

    @Override
    public void addColumn(String propertyId, String title, float expandRatio) {
        addColumn(propertyId, title);
        table.setColumnExpandRatio(propertyId, expandRatio);
    }

    @Override
    public void setColumnFormatter(String propertyId, ColumnFormatter formatter) {
        table.addGeneratedColumn(propertyId, formatter);
    }

    @Override
    public void clearColumns() {
        table.setVisibleColumns(new Object[] {});
    }

    @Override
    public void select(List<Object> itemIds) {
        if (itemIds == null) {
            table.setValue(null);
            return;
        }

        // convert to set before comparing with actual table selection (which is also a set underneath)
        Set<Object> uniqueItemIds = new HashSet<>(itemIds);
        if (uniqueItemIds.equals(table.getValue())) {
            // selection already in sync, nothing to do
            return;
        }

        table.setValue(uniqueItemIds);
        // do not #setCurrentPageFirstItemId because AbstractJcrContainer's index resolution is super slow.
    }

    @Override
    public void expand(Object itemId) {
    }

    @Override
    public Component asVaadinComponent() {
        return table;
    }

    @Override
    public void setMultiselect(boolean multiselect) {
        table.setMultiSelect(multiselect);
    }

    @Override
    public void onShortcutKey(int keyCode, int[] modifierKeys) {
        if (listener != null) {
            listener.onShortcutKey(keyCode, modifierKeys);
        }
    }

}
