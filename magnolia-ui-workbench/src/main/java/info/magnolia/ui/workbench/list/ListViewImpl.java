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
package info.magnolia.ui.workbench.list;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.vaadin.grid.MagnoliaTable;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.ContentView;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;

/**
 * Vaadin UI component that displays a list.
 */
public class ListViewImpl implements ListView {

    private static final String ICON_PROPERTY = "icon-node-data";

    private static final String ICON_TRASH = "icon-trash";

    private static final Logger log = LoggerFactory.getLogger(ListViewImpl.class);

    private final Table table;

    private final Map<String, String> nodeIcons = new HashMap<String, String>();

    private ListView.Listener listener;

    public ListViewImpl() {
        this(new MagnoliaTable());
    }

    public ListViewImpl(Table table) {
        table.setSizeFull();

        table.setImmediate(true);
        table.setSelectable(true);
        table.setMultiSelect(false);
        table.setNullSelectionAllowed(true);

        table.setDragMode(TableDragMode.NONE);
        table.setEditable(false);
        table.setColumnCollapsingAllowed(false);
        table.setColumnReorderingAllowed(false);
        table.setSortEnabled(true);

        table.setCellStyleGenerator(new Table.CellStyleGenerator() {
            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {

                final Item item = source.getContainerDataSource().getItem(itemId);
                if (item instanceof JcrPropertyAdapter) {
                    return ICON_PROPERTY;
                } else if (item instanceof JcrNodeAdapter) {
                    if (isDeletedNode((JcrNodeAdapter) item)) {
                        return ICON_TRASH;
                    }
                    return nodeIcons.get(((JcrNodeAdapter) item).getPrimaryNodeTypeName());
                }
                return null;
            }
        });

        this.table = table;
        bindHandlers();
    }

    private boolean isDeletedNode(JcrNodeAdapter nodeAdapter) {
        Node node = nodeAdapter.getNode();
        try {
            return NodeUtil.hasMixin(node, NodeTypes.Deleted.NAME);
        } catch (RepositoryException re) {
            log.warn("Not able to check if node has MixIn");
            return false;
        }
    }

    protected void bindHandlers() {
        table.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                log.debug("Handle value change Event: {}", event.getProperty().getValue());

                if (listener != null) {
                    listener.onItemSelection(table.getItem(event.getProperty().getValue()));
                }
            }
        });

        table.addItemClickListener(new ItemClickListener() {

            private Object previousSelection;

            @Override
            public void itemClick(ItemClickEvent event) {
                Object currentSelection = event.getItemId();
                if (event.isDoubleClick()) {
                    if (listener != null) {
                        listener.onDoubleClick(event.getItem());
                    }
                } else {
                    // toggle will deselect
                    if (previousSelection == currentSelection) {
                        table.setValue(null);
                    }
                }

                previousSelection = currentSelection;
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
        table.setContainerDataSource(container);
    }

    @Override
    public void addColumn(String propertyId, String title) {
        table.setColumnHeader(propertyId, title);
        List<Object> visibleColumns = new ArrayList<Object>(Arrays.asList(table.getVisibleColumns()));
        visibleColumns.add(propertyId);
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
    public void setNodeIcon(String primaryNodeType, String iconName) {
        nodeIcons.put(primaryNodeType, iconName);
    }

    @Override
    public void select(String path) {
        table.select(path);
    }

    @Override
    public ViewType getViewType() {
        return ViewType.LIST;
    }

    @Override
    public Table asVaadinComponent() {
        return table;
    }

}
