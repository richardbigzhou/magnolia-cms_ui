/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.workbench.event.ItemEditedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.AbstractStringToNumberConverter;
import com.vaadin.event.ActionManager;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;

/**
 * The Inplace-editing TreeTable, for editing item properties inplace, on double click or via keyboard shortcuts.
 * Additionally, editable columns are configurable
 */
public class InplaceEditingTreeTable extends MagnoliaTreeTable implements ItemClickEvent.ItemClickListener, ItemEditedEvent.Notifier {

    private Object editingItemId;

    private Object editingPropertyId;

    private List<Object> editableColumns = new ArrayList<Object>();

    private InplaceEditingFieldFactory fieldFactory;

    private ColumnGenerator bypassedColumnGenerator;

    private final List<ItemEditedEvent.Handler> listeners = new ArrayList<ItemEditedEvent.Handler>();

    private ActionManager shortcutActionManager;

    public InplaceEditingTreeTable() {
        super();
        fieldFactory = new InplaceEditingFieldFactory();
        setTableFieldFactory(fieldFactory);
        addItemClickListener(asItemClickListener());

        addExpandListener(new ExpandListener() {

            @Override
            public void nodeExpand(ExpandEvent event) {
                if (editingItemId != null) {
                    setEditing(null, null);
                }
            }
        });

        addCollapseListener(new CollapseListener() {

            @Override
            public void nodeCollapse(CollapseEvent event) {
                if (editingItemId != null) {
                    setEditing(null, null);
                }
            }
        });
    }

    // INPLACE EDITING ENTRY POINTS.

    public void setEditableColumns(Object... editablePropertyIds) {
        editableColumns.clear();
        editableColumns.addAll(Arrays.asList(editablePropertyIds));
    }

    /**
     * Sets the item and property for inplace editing.
     *
     * @param itemId the item id
     * @param propertyId the property id
     */
    public void setEditing(Object itemId, Object propertyId) {
        // ensure we don't keep outdated itemIds
        if (getItem(itemId) == null) {
            itemId = null;
            propertyId = null;
        }

        if (itemId != null && propertyId != null) {
            Item item = getItem(itemId);
            Property<?> property = item.getItemProperty(propertyId);

            // The previous call can return null, i.e. when clicking on an empty cell of a node row (i.e. /config/server and then the "value" cell)
            // Do not allow editing for multi-value property.
            if (property == null || property.getValue() instanceof List) {
                return;
            } else {
                if ((bypassedColumnGenerator = getColumnGenerator(propertyId)) != null) {
                    removeGeneratedColumn(propertyId);
                }
                fieldFactory.createFieldAndRegister(getContainerDataSource(), itemId, propertyId, this);
            }
        } else {
            if (bypassedColumnGenerator != null) {
                addGeneratedColumn(editingPropertyId, bypassedColumnGenerator);
                bypassedColumnGenerator = null;
            }
            focus();
        }

        this.editingItemId = itemId;
        this.editingPropertyId = propertyId;

        refreshRowCache();
    }

    // PARTIAL UPDATES
    // MGNLUI-282 partial updates are disabled for inplace-editing to prevent tree from turning unstable.

    @Override
    protected int getFirstUpdatedItemIndex() {
        // if (editingItemId != null) {
        // return indexOfId(editingItemId);
        // }
        return super.getFirstUpdatedItemIndex();
    }

    @Override
    protected int getUpdatedRowCount() {
        // if (editingItemId != null) {
        // return 1;
        // }
        return super.getUpdatedRowCount();
    }

    @Override
    protected int getFirstAddedItemIndex() {
        // if (editingItemId != null) {
        // return indexOfId(editingItemId);
        // }
        return super.getFirstAddedItemIndex();
    }

    @Override
    protected int getAddedRowCount() {
        // if (editingItemId != null) {
        // return 0;
        // }
        return super.getAddedRowCount();
    }

    @Override
    protected boolean shouldHideAddedRows() {
        // if (editingItemId != null) {
        // return false;
        // }
        return super.shouldHideAddedRows();
    }

    @Override
    protected boolean isPartialRowUpdate() {
        return
        // editingItemId != null ||
        super.isPartialRowUpdate();
    }

    // INPLACE EDITING FIELD FACTORY

    /**
     * A factory for creating the inplace editing field in the right cell.
     */
    private class InplaceEditingFieldFactory implements TableFieldFactory {

        private Field<?> inplaceEditingField;

        /**
         * For partial updates to work, we need to perform a dry run to attach the component to the table beforehand,
         * i.e. before it is actually requested at paint phase by the table.
         */
        public void createFieldAndRegister(Container container, Object itemId, Object propertyId, Component uiContext) {

            Property<?> containerProperty = container.getContainerProperty(itemId, propertyId);
            // the previous call can return null, i.e. when clicking on an empty cell of a node row (i.e. /config/server and then the "value" cell)
            if (containerProperty == null) {
                return;
            }
            Class<?> type = containerProperty.getType();
            final Field<?> field = createFieldByPropertyType(type);
            if (field != null) {
                field.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
                field.setSizeFull();
            }

            // set TextField listeners
            if (field instanceof AbstractTextField) {
                final AbstractTextField tf = (AbstractTextField) field;
                tf.addBlurListener(new FieldEvents.BlurListener() {

                    @Override
                    public void blur(BlurEvent event) {
                        fireItemEditedEvent(tf.getPropertyDataSource());
                        setEditing(null, null);
                    }
                });
                tf.focus();

                tf.addValueChangeListener(new ValueChangeListener() {

                    @Override
                    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                        final Object text = event.getProperty().getValue();
                        if (text instanceof String) {
                            tf.selectAll();
                        }
                        tf.removeValueChangeListener(this);
                    }
                });
            }

            // register component on the table
            InplaceEditingTreeTable.this.registerComponent(field);

            inplaceEditingField = field;
        }

        @Override
        public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {

            // add TextField only for selected row/column.
            if (editableColumns.contains(propertyId) && itemId.equals(editingItemId) && propertyId.equals(editingPropertyId)) {
                return inplaceEditingField;
            }

            return null;
        }

        private Field<?> createFieldByPropertyType(Class<?> type) {
            if (type == null) {
                return null;
            }
            Field<?> field = new TextField();
            // FIXME MGNLUI-1855 To remove once Vaadin 7.2 will be used. Currently we need to assign converter for properties with type Long because otherwise Vaadin assigns incompatible StringToNumberConverter.
            if (Long.class.equals(type)) {
                ((AbstractTextField) field).setConverter(new StringToLongConverter());
            }
            return field;
        }

    }

    /**
     * The StringToLongConverter.<br>
     * MGNLUI-1855 This should be handled by vaadin, but StringToNumberConverter throws conversion exception when used
     * with a Long property in Vaadin 7.1. This should be fixed, unfortunately not before 7.2, so we need that converter
     * for the time being.<br>
     * As a result, this class will have a short life span, this is why it is kept private and deprecated.
     */
    @Deprecated
    private static class StringToLongConverter extends AbstractStringToNumberConverter<Long> {
        // FIXME MGNLUI-1855 To remove once Vaadin 7.2 will be used.
        @Override
        public Long convertToModel(String value, Class<? extends Long> targetType, Locale locale) throws ConversionException {
            Number n = convertToNumber(value, targetType, locale);
            return n == null ? null : n.longValue();
        }

        @Override
        public Class<Long> getModelType() {
            return Long.class;
        }
    }


    // FIRING ITEM EDITED EVENTS

    @Override
    public void addItemEditedListener(ItemEditedEvent.Handler listener) {
        listeners.add(listener);
    }

    @Override
    public void removeItemEditedListener(ItemEditedEvent.Handler listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    /**
     * Fires an {@link ItemEditedEvent} to all listeners. Since the property does not contain a reference to the item
     * it came from we need to fetch the item from the container and change the property before we send the item in the
     * event.
     */
    public void fireItemEditedEvent(Property property) {

        Item item = getContainerDataSource().getItem(editingItemId);
        if (item == null) {
            return;
        }

        Property itemProperty = item.getItemProperty(editingPropertyId);
        if (itemProperty == null) {
            return;
        }

        itemProperty.setValue(property.getValue());

        ItemEditedEvent event = new ItemEditedEvent(item);
        for (ItemEditedEvent.Handler listener : listeners) {
            listener.onItemEdited(event);
        }
    }

    // DOUBLE CLICK

    @Override
    public void itemClick(ItemClickEvent event) {
        if (event.isDoubleClick() && editableColumns.contains(event.getPropertyId())) {
            setEditing(event.getItemId(), event.getPropertyId());
        }
    }

    private ItemClickEvent.ItemClickListener asItemClickListener() {
        return this;
    }


    // EDITING API

    public void editNextCell(Field<?> field) {
        // First gets a reference to next candidate
        TableCell nextCell = getNextEditableCandidate(editingItemId, editingPropertyId);
        // Then saves
        fireItemEditedEvent(field.getPropertyDataSource());

        setEditing(nextCell.getItemId(), nextCell.getPropertyId());
    }

    public void editPreviousCell(Field<?> field) {
        // First gets a reference to previous candidate
        TableCell previousCell = getPreviousEditableCandidate(editingItemId, editingPropertyId);
        // Then saves
        fireItemEditedEvent(field.getPropertyDataSource());

        setEditing(previousCell.getItemId(), previousCell.getPropertyId());
    }

    public void editFirstCellofFirstSelectedRow() {

        // get first selected itemId, handles multiple selection mode
        Object firstSelectedId = getValue();
        if (firstSelectedId instanceof Collection) {
            if (((Collection<?>) firstSelectedId).size() > 0) {
                firstSelectedId = ((Set<?>) firstSelectedId).iterator().next();
            } else {
                firstSelectedId = null;
            }
        }
        // Edit selected row at first column
        Object propertyId = getVisibleColumns()[0];
        if (!editableColumns.contains(propertyId)) {
            propertyId = getNextEditableCandidate(firstSelectedId, propertyId).getPropertyId();
        }
        setEditing(firstSelectedId, propertyId);
    }

    // NEXT/PREVIOUS EDITABLE PROPERTY CANDIDATES

    private TableCell getNextEditableCandidate(Object itemId, Object propertyId) {

        List<Object> visibleColumns = Arrays.asList(getVisibleColumns());
        Object newItemId = itemId;
        int newColumn = visibleColumns.indexOf(propertyId);
        do {
            if (newColumn == visibleColumns.size() - 1) {
                newItemId = nextItemId(newItemId);
            }
            newColumn = (newColumn + 1) % visibleColumns.size();
        } while (!editableColumns.contains(visibleColumns.get(newColumn)) && newItemId != null);

        return new TableCell(newItemId, visibleColumns.get(newColumn));
    }

    private TableCell getPreviousEditableCandidate(Object itemId, Object propertyId) {

        List<Object> visibleColumns = Arrays.asList(getVisibleColumns());
        Object newItemId = itemId;
        int newColumn = visibleColumns.indexOf(propertyId);
        do {
            if (newColumn == 0) {
                newItemId = prevItemId(newItemId);
            }
            newColumn = (newColumn + visibleColumns.size() - 1) % visibleColumns.size();
        } while (!editableColumns.contains(visibleColumns.get(newColumn)) && newItemId != null);

        return new TableCell(newItemId, visibleColumns.get(newColumn));
    }

    /**
     * The TableCell supporting class.
     */
    private class TableCell {

        private final Object itemId;

        private final Object propertyId;

        public TableCell(Object itemId, Object propertyId) {
            this.itemId = itemId;
            this.propertyId = propertyId;
        }

        public Object getItemId() {
            return itemId;
        }

        public Object getPropertyId() {
            return propertyId;
        }
    }

}
