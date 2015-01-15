/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.item.list;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategoryNavigator;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.GeneratedRow;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract view implementation for displaying a list of items in pulse.
 */
public abstract class AbstractPulseListView implements PulseListView {

    public static final String GROUP_PLACEHOLDER_ITEMID = "##SUBSECTION##";

    private final String[] order;

    private final String[] headers;

    private final TreeTable itemTable = new MagnoliaTreeTable();

    private final VerticalLayout root = new VerticalLayout();

    private final PulseItemCategoryNavigator navigator;

    private final SimpleTranslator i18n;

    private PulseListView.Listener listener;

    private Label emptyPlaceHolder;

    private PulseListFooter footer;

    private PulseItemCategory currentlySelectedCategory = PulseItemCategory.UNCLAIMED;

    private boolean categoryFilterAlreadyApplied;

    private Property.ValueChangeListener selectionListener = new Property.ValueChangeListener() {

        private Set<Object> prevSelected = new HashSet<Object>();

        @Override
        public void valueChange(ValueChangeEvent event) {
            /*
             * selecting/unselecting cause valueChange events and it is not
             * preferred that an event handler generates more events.
             */
            itemTable.removeValueChangeListener(this);

            @SuppressWarnings("unchecked")
            Set<Object> currSelected = new HashSet<Object>((Set<Object>) event.getProperty().getValue());
            Set<Object> added = new HashSet<Object>(currSelected);
            Set<Object> removed = new HashSet<Object>(prevSelected);

            added.removeAll(prevSelected);
            removed.removeAll(currSelected);

            prevSelected = currSelected;

            /*
             * if group line was added/removed then select/unselect all it's
             * children
             */
            selectChildren(added, true);
            selectChildren(removed, false);

            // Item deselection will always deselect group
            for (Object child : removed) {
                Object parent = listener.getParent(child);
                if (parent != null) {
                    itemTable.unselect(parent);
                    prevSelected.remove(parent);
                }
            }

            /*
             * Selecting item must check that all siblings are also selected
             */
            for (Object child : added) {
                Object parent = listener.getParent(child);
                if (isAllChildrenSelected(parent)) {
                    itemTable.select(parent);
                    prevSelected.add(parent);
                } else {
                    itemTable.unselect(parent);
                    prevSelected.remove(parent);
                }
            }

            itemTable.addValueChangeListener(this);
            footer.updateStatus();
        }

        private boolean isAllChildrenSelected(Object parent) {
            if (parent == null) {
                return false;
            }

            Collection<?> siblings = listener.getGroup(parent);
            boolean allSelected = true;

            if (siblings != null) {
                for (Object sibling : siblings) {
                    if (!itemTable.isSelected(sibling)) {
                        allSelected = false;
                    }
                }
            } else {
                return false;
            }

            return allSelected;
        }

        private void selectChildren(Set<Object> parents, boolean check) {
            for (Object parent : parents) {
                Collection<?> group = listener.getGroup(parent);
                if (group != null) {
                    for (Object child : group) {
                        if (check) {
                            itemTable.select(child);
                            prevSelected.add(child);
                        } else {
                            itemTable.unselect(child);
                            prevSelected.remove(child);
                        }
                    }
                }
            }
        }
    };

    private ValueChangeListener groupingListener = new ValueChangeListener() {

        @Override
        public void valueChange(ValueChangeEvent event) {
            boolean checked = event.getProperty().getValue().equals(Boolean.TRUE);
            doGrouping(checked);
        }
    };

    /*
     * Row generator draws grouping headers if such are present in container
     */
    private Table.RowGenerator groupingRowGenerator = new Table.RowGenerator() {

        @Override
        public GeneratedRow generateRow(Table table, Object itemId) {

            /*
             * When sorting by type special items are inserted into Container to
             * acts as a placeholder for grouping sub section. This row
             * generator must render those special items.
             */
            if (itemId.toString().startsWith(GROUP_PLACEHOLDER_ITEMID)) {
                Item item = table.getItem(itemId);
                return generateGroupingRow(item);
            }

            return null;
        }
    };

    @Inject
    public AbstractPulseListView(Shell shell, SimpleTranslator i18n, String[] order, String[] headers, String emptyMessage, PulseItemCategory... categories) {
        this.i18n = i18n;
        this.order = order;
        this.headers = headers;
        navigator = new PulseItemCategoryNavigator(i18n, true, false, categories);
        root.setSizeFull();
        construct(emptyMessage);
    }

    @Override
    public void refresh() {
        // skip this if we're displaying all messages or if the category filter has just been applied (i.e. after clicking on a different tab)
        if ((currentlySelectedCategory != PulseItemCategory.ALL_MESSAGES || currentlySelectedCategory != PulseItemCategory.ALL_TASKS) && !categoryFilterAlreadyApplied) {
            listener.filterByItemCategory(currentlySelectedCategory);
        }
        // now this can be reset to its initial value
        categoryFilterAlreadyApplied = false;
        footer.updateStatus();
        itemTable.sort();
        doGrouping(false);
    }

    @Override
    public void setDataSource(Container dataSource) {
        itemTable.setContainerDataSource(dataSource);
        itemTable.setVisibleColumns(order);
        itemTable.setColumnHeaders(headers);
    }

    @Override
    public void setListener(PulseListView.Listener listener) {
        this.listener = listener;
        // message listener can use the generic listener
        // as the only thing it does now is deleting items
        this.footer.setMessagesListener(listener);
    }

    @Override
    public void updateCategoryBadgeCount(PulseItemCategory category, int count) {
        navigator.updateCategoryBadgeCount(category, count);
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

    public void setFooter(PulseListFooter footer) {
        this.footer = footer;
        root.addComponent(footer);
        footer.setHeight("60px");
    }

    private void construct(String emptyMessage) {
        root.addComponent(navigator);
        navigator.addCategoryChangeListener(new PulseItemCategoryNavigator.ItemCategoryChangedListener() {

            @Override
            public void itemCategoryChanged(PulseItemCategoryNavigator.CategoryChangedEvent event) {
                final PulseItemCategory category = event.getCategory();
                onItemCategoryChanged(category);
            }
        });

        constructTable();

        emptyPlaceHolder = new Label();
        emptyPlaceHolder.setContentMode(ContentMode.HTML);
        emptyPlaceHolder.setValue(String.format("<span class=\"icon-pulse\"></span><div class=\"message\">%s</div>", emptyMessage));
        emptyPlaceHolder.addStyleName("emptyplaceholder");

        root.addComponent(emptyPlaceHolder);

        // create an initial default footer to avoid NPE exception. This will be replaced later on by specific implementations
        footer = new PulseListFooter(itemTable, i18n, false);
        root.addComponent(footer);

        setComponentVisibility(itemTable.getContainerDataSource());
    }

    private void constructTable() {
        root.addComponent(itemTable);
        root.setExpandRatio(itemTable, 1f);
        itemTable.setSizeFull();
        itemTable.addStyleName("message-table");
        itemTable.setSelectable(true);
        itemTable.setMultiSelect(true);
        itemTable.setRowGenerator(groupingRowGenerator);

        navigator.addGroupingListener(groupingListener);

        itemTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                onItemClicked(event, event.getItemId());
            }
        });

        itemTable.addValueChangeListener(selectionListener);
        itemTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {
            @Override
            public void containerItemSetChange(ItemSetChangeEvent event) {
                setComponentVisibility(event.getContainer());
            }
        });
    }

    private void doGrouping(boolean checked) {
        listener.setGrouping(checked);

        if (checked) {
            for (Object itemId : itemTable.getItemIds()) {
                itemTable.setCollapsed(itemId, false);
            }
        }
    }

    private void setComponentVisibility(Container container) {
        boolean isEmptyList = container.getItemIds().size() == 0;
        if (isEmptyList) {
            root.setExpandRatio(emptyPlaceHolder, 1f);
        } else {
            root.setExpandRatio(emptyPlaceHolder, 0f);
        }

        itemTable.setVisible(!isEmptyList);
        footer.setVisible(!isEmptyList);
        emptyPlaceHolder.setVisible(isEmptyList);
    }

    /**
     * A row generator draws grouping headers if such are present in the container. Default implementation returns null.
     */
    abstract protected GeneratedRow generateGroupingRow(Item item);

    protected SimpleTranslator getI18n() {
        return i18n;
    }

    protected TreeTable getItemTable() {
        return itemTable;
    }

    @Override
    public void setTabActive(PulseItemCategory category) {
        navigator.setActive(category);
        onItemCategoryChanged(category);
    }

    protected PulseListFooter getFooter() {
        return footer;
    }

    protected void onItemClicked(ClickEvent event, final Object itemId) {
        String itemIdAsString = String.valueOf(itemId);
        // clicking on the group type header does nothing.
        if (itemIdAsString.startsWith(GROUP_PLACEHOLDER_ITEMID)) {
            return;
        }
        if (event.isDoubleClick()) {
            listener.onItemClicked(itemIdAsString);
        } else {
            if (itemTable.isSelected(itemIdAsString)) {
                itemTable.unselect(itemIdAsString);
            }
        }
    }

    private void onItemCategoryChanged(final PulseItemCategory category) {
        currentlySelectedCategory = category;
        listener.filterByItemCategory(category);
        categoryFilterAlreadyApplied = true;
        // TODO fgrilli Unselect all when switching categories or nasty side effects will happen. See MGNLUI-1447
        for (String id : (Set<String>) itemTable.getValue()) {
            itemTable.unselect(id);
        }

        switch (category) {
        case ALL_TASKS:
        case ALL_MESSAGES:
            navigator.enableGroupBy(true);
            break;
        default:
            navigator.enableGroupBy(false);
        }

        refresh();
    }

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for denoting new messages or tasks in the Pulse list views.
     */
    protected class PulseNewItemColumnGenerator implements Table.ColumnGenerator {

        // void public constructor to instantiate from subclasses in different packages
        public PulseNewItemColumnGenerator() {
        }

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            Property<Boolean> newProperty = source.getContainerProperty(itemId, columnId);
            boolean isNew = newProperty != null && newProperty.getValue();
            if (isNew) {
                return "<span class=\"icon icon-tick new-message\"></span>";
            }
            return null;
        }
    }
}
