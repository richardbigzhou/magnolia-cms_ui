/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.ui.admincentral.shellapp.pulse.PulseMessageCategoryNavigator.CategoryChangedEvent;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseMessageCategoryNavigator.MessageCategoryChangedListener;
import info.magnolia.ui.vaadin.integration.widget.grid.MagnoliaTreeTable;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.time.FastDateFormat;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Table.GeneratedRow;

/**
 * Implementation of {@link PulseMessagesView}.
 */
@SuppressWarnings("serial")
public class PulseMessagesViewImpl extends CustomComponent implements PulseMessagesView {

    private static final String[] headers = new String[]{
        "New", "Type", "Message Text", "Sender", "Date", "Quick Do"
    };

    private final TreeTable messageTable = new MagnoliaTreeTable();

    private final VerticalLayout root = new VerticalLayout();

    private final PulseMessageCategoryNavigator navigator = new PulseMessageCategoryNavigator();

    private final PulseMessagesPresenter presenter;

    private final FastDateFormat dateFormatter = FastDateFormat.getInstance();
    
    private boolean grouping = false;

    @Inject
    public PulseMessagesViewImpl(final PulseMessagesPresenter presenter) {
        this.presenter = presenter;
        setSizeFull();
        root.setSizeFull();
        setCompositionRoot(root);
        construct();
    }

    private void construct() {
        root.addComponent(navigator);
        navigator.addCategoryChangeListener(new MessageCategoryChangedListener() {

            @Override
            public void messageCategoryChanged(CategoryChangedEvent event) {
                presenter.filterByMessageCategory(event.getCategory());
            }
        });
        constructTable();
    }

    private void constructTable() {
        root.addComponent(messageTable);
        root.setExpandRatio(messageTable, 1f);
        messageTable.setSizeFull();
        messageTable.addGeneratedColumn("date", new Table.ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Property prop = source.getItem(itemId).getItemProperty(columnId);
                if (prop.getType().equals(Date.class) && prop.getValue() != null) {
                    return dateFormatter.format(prop.getValue());
                }
                return null;
            }

        });
        
        messageTable.setRowGenerator(groupingRowGenerator);
        messageTable.setCellStyleGenerator(cellStyleGenerator);
        navigator.addGroupingListener(groupingListener);
        
        messageTable.setContainerDataSource(presenter.getMessageDataSource());
        messageTable.setVisibleColumns(presenter.getColumnOrder());
        messageTable.setSortContainerPropertyId("date");
        messageTable.setSortAscending(false);
        messageTable.setColumnHeaders(headers);

        messageTable.addListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                Object itemId = event.getItemId();
                presenter.onMessageClicked((String) itemId);
            }
        });
    }
    
    private ValueChangeListener groupingListener = new ValueChangeListener() {
        
        @Override
        public void valueChange(ValueChangeEvent event) {
            boolean checked = event.getProperty().getValue().equals(Boolean.TRUE);
            presenter.setGrouping(checked);
            grouping = checked;
            
            if(checked) {
                for(Object itemId: messageTable.getItemIds()) {
                    messageTable.setCollapsed(itemId, false);
                }
            }
        }
    };
    
    private Table.CellStyleGenerator cellStyleGenerator = new Table.CellStyleGenerator() {
        
        @Override
        public String getStyle(Object itemId, Object propertyId) {

            if( grouping &&
                    propertyId != null && 
                    propertyId.equals(PulseMessagesPresenter.GROUP_COLUMN)) {
                return "v-cell-invisible";
            }
            return "";
        }
    };

    
    /* Row generator draws grouping headers if such are present in container
     */
    private Table.RowGenerator groupingRowGenerator = new Table.RowGenerator() {

        @Override
        public GeneratedRow generateRow(Table table, Object itemId) {
           
            /*
             * When sorting by type special items are inserted into
             * Container to acts as a placeholder for grouping
             * sub section. This row generator must render those
             * special items.
             */
            if(itemId.toString().startsWith(PulseMessagesPresenter.GROUP_PLACEHOLDER_ITEMID)) {
                Item item = table.getItem(itemId);
                Property property = item.getItemProperty(PulseMessagesPresenter.GROUP_COLUMN);

                GeneratedRow generated = new GeneratedRow("", "", property.getValue().toString());
                generated.setSpanColumns(false);
                return generated;
            }

            return null;
        }
    };

    @Override
    public ComponentContainer asVaadinComponent() {
        return this;
    }

    @Override
    public void update(List<String> params) {
        if (params != null && !params.isEmpty()) {
            final Set<Object> values = new HashSet<Object>();
            String messageId = params.get(0);
            values.add(messageId);
            // This is a multi-select table, calling select would just add this message to the
            // current
            // set of selected rows so setting the value explicitly this way makes only this one
            // selected.
            messageTable.setValue(values);
        }
    }
}
