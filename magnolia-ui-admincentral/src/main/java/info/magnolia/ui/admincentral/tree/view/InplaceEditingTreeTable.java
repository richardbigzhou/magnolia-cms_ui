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
package info.magnolia.ui.admincentral.tree.view;

import info.magnolia.ui.admincentral.event.ItemEditedEvent;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;

/**
 * The Inplace-editing TreeTable, for editing item properties inplace, on double click or via keyboard shortcuts.
 * Additionally, editable columns are configurable
 */
public class InplaceEditingTreeTable extends MagnoliaTreeTable implements ItemClickEvent.ItemClickListener, ItemEditedEvent.Notifier {

    private Object editingItemId;

    private Object editingPropertyId;

    private List<Object> editableColumns;

    private ColumnGenerator bypassedColumnGenerator;

    private final List<ItemEditedEvent.Handler> listeners = new ArrayList<ItemEditedEvent.Handler>();

    public InplaceEditingTreeTable() {
        super();
        setEditable(true);
        setTableFieldFactory(new InplaceEditingFieldFactory());
        addListener(asItemClickListener());
        getActionManager().addActionHandler(new EditingKeyboardHandler());
    }

    // INPLACE EDITING ENTRY POINTS.

    public void setEditableColumns(Object... editablePropertyIds) {
        if (editableColumns != null) {
            this.editableColumns.clear();
        } else {
            editableColumns = new ArrayList<Object>();
        }
        this.editableColumns.addAll(Arrays.asList(editablePropertyIds));
    }

    /**
     * Sets the item and property for inplace editing.
     *
     * @param itemId the item id
     * @param propertyId the property id
     */
    public void setEditing(Object itemId, Object propertyId) {
        if (itemId != null && propertyId != null) {
            if ((bypassedColumnGenerator = getColumnGenerator(propertyId)) != null) {
                removeGeneratedColumn(propertyId);
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
        requestRepaint();
    }

    // INPLACE EDITING FIELD FACTORY

    /**
     * A factory for creating the inplace editing field in the right cell.
     */
    private class InplaceEditingFieldFactory extends DefaultFieldFactory {

        @Override
        public Field createField(Container container, final Object itemId, final Object propertyId, Component uiContext) {

            // add TextField only for selected row/column.
            if (editableColumns.contains(propertyId)
                && itemId.equals(editingItemId)
                && propertyId.equals(editingPropertyId)) {

                Field field = super.createField(container, itemId, propertyId, uiContext);

                // set TextField Focus listeners
                if (field instanceof AbstractTextField) {
                    final AbstractTextField tf = (AbstractTextField) field;
                    tf.addListener(new FieldEvents.FocusListener() {

                        @Override
                        public void focus(FocusEvent event) {
                            tf.setCursorPosition(tf.toString().length());
                        }
                    });

                    tf.addListener(new FieldEvents.BlurListener() {

                        @Override
                        public void blur(BlurEvent event) {
                            fireItemEditedEvent(getItemFromField(tf));
                            setEditing(null, null);
                        }
                    });
                    tf.focus();
                }

                return field;
            }
            return null;
        }
    }

    // FIRING ITEM EDITED EVENTS

    @Override
    public void addListener(ItemEditedEvent.Handler listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ItemEditedEvent.Handler listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private void fireItemEditedEvent(Item item) {
        if (item != null) {
            ItemEditedEvent event = new ItemEditedEvent(item);
            for (ItemEditedEvent.Handler listener : listeners) {
                listener.onItemEdited(event);
            }
        }
    }

    /**
     * Gets the item whose property is currently being edited in the given field. Since the {{Table}} doesn't keep
     * references to its items, the only way to get it back is to ask the property datasource for its listeners and see
     * if the Item is there.
     *
     * @param source the vaadin {{Field}} where the editing occured
     * @return the vaadin {{Item}} if it could be fetched, null otherwise.
     */
    private Item getItemFromField(Field source) {
        if (source != null) {
            Property property = source.getPropertyDataSource();
            if (property != null && property instanceof AbstractProperty) {
                Collection<?> listeners = ((AbstractProperty) property).getListeners(Property.ValueChangeEvent.class);
                Iterator<?> iterator = listeners.iterator();
                while (iterator.hasNext()) {
                    Object listener = iterator.next();
                    if (listener instanceof Item) {
                        return (Item) listener;
                    }
                }
            }
        }
        return null;
    }

    // DOUBLE CLICK

    @Override
    public void itemClick(ItemClickEvent event) {
        if (event.isDoubleClick()) {
            setEditing(event.getItemId(), event.getPropertyId());
        }
    }

    private ItemClickEvent.ItemClickListener asItemClickListener() {
        return this;
    }

    // KEYBOARD SHORTCUTS

    /**
     * The Class EditingKeyboardHandler for keyboard shortcuts with inplace editing.
     */
    private class EditingKeyboardHandler implements Handler {

        private final ShortcutAction enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.ENTER, null);

        private final ShortcutAction tabNext = new ShortcutAction("Tab", ShortcutAction.KeyCode.TAB, null);

        private final ShortcutAction tabPrev = new ShortcutAction("Shift+Tab", ShortcutAction.KeyCode.TAB, new int[]{ShortcutAction.ModifierKey.SHIFT});

        private final ShortcutAction escape = new ShortcutAction("Esc", ShortcutAction.KeyCode.ESCAPE, null);

        @Override
        public Action[] getActions(Object target, Object sender) {
            return new Action[]{enter, tabNext, tabPrev, escape};
        }

        @Override
        public void handleAction(Action action, Object sender, Object target) {
            /*
             * In case of enter the Action needs to be casted back to
             * ShortcutAction because for some reason the object is not same
             * as this.enter object. In that case keycode is used in comparison.
             */
            if (!(action instanceof ShortcutAction)) {
                return;
            }
            ShortcutAction shortcut = (ShortcutAction) action;

            if (target != InplaceEditingTreeTable.this && target instanceof Field) {
                Field field = (Field) target;

                if (shortcut == enter || shortcut.getKeyCode() == enter.getKeyCode()) {
                    fireItemEditedEvent(getItemFromField(field));
                    setEditing(null, null);

                } else if (action == tabNext) {
                    // Saves first
                    fireItemEditedEvent(getItemFromField(field));

                    // Then updates current editingItemId, and asks for next candidate
                    TableCell nextCell = getNextEditableCandidate(editingItemId, editingPropertyId);
                    setEditing(nextCell.getItemId(), nextCell.getPropertyId());

                } else if (action == tabPrev) {
                    // Saves first
                    fireItemEditedEvent(getItemFromField(field));

                    // Then updates current editingItemId, and asks for previous candidate
                    TableCell previousCell = getPreviousEditableCandidate(editingItemId, editingPropertyId);
                    setEditing(previousCell.getItemId(), previousCell.getPropertyId());

                } else if (action == escape) {
                    setEditing(null, null);
                }
            } else if (target == InplaceEditingTreeTable.this) {
                if (getValue() == null) {
                    return;
                }

                if (shortcut == enter || shortcut.getKeyCode() == enter.getKeyCode()) {
                    // get first selected itemId, handles multiple selection mode
                    Object firstSelectedId = getValue();
                    if (firstSelectedId instanceof Set && ((Set<?>) firstSelectedId).size() > 0) {
                        firstSelectedId = ((Set<?>) firstSelectedId).iterator().next();
                    }
                    // Edit selected row at first column
                    Object propertyId = getVisibleColumns()[0];
                    if (!editableColumns.contains(propertyId)) {
                        propertyId = getNextEditableCandidate(firstSelectedId, propertyId).getPropertyId();
                    }
                    setEditing(firstSelectedId, propertyId);
                }
            }
        }
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
