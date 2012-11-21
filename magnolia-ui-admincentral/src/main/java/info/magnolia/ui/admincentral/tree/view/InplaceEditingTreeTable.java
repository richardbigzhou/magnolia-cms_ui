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

import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.container.AbstractJcrContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.RepositoryException;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;


/**
 * An inplace editing tree table, taking care of partial updates when editing one item.
 */
@SuppressWarnings("serial")
public class InplaceEditingTreeTable extends MagnoliaTreeTable implements ItemClickEvent.ItemClickListener {

    private Object editingItemId;

    private Object editingPropertyId;

    private List<Object> editableColumns;

    private ColumnGenerator bypassedColumnGenerator;

    public InplaceEditingTreeTable() {
        super();
        setEditable(true);
        setTableFieldFactory(new InplaceEditingFieldFactory());
        addListener(asItemClickListener());
    }

    // @Override
    // public void attach() {
    // super.attach();
    // Component parent = getParent();
    //
    // // get first ancestor that can register action handlers
    // while (parent != null) {
    // if (parent instanceof com.vaadin.event.Action.Container) {
    // ((com.vaadin.event.Action.Container) parent).addActionHandler(new
    // InplaceEditingKeyboardHandler());
    // break;
    // }
    // parent = parent.getParent();
    // }
    // }

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

    // /**
    // * Saves the currently editing item.
    // */
    // private void save(AbstractField field) {
    // System.out.println("SAVEY!");
    // field.commit();
    //
    // // get jcr item in use, not a new one from container
    // Collection< ? > listeners = ((AbstractProperty)
    // field.getPropertyDataSource()).getListeners(ValueChangeEvent.class);
    // for (Object listener : listeners) {
    // if (listener instanceof JcrItemAdapter) {
    // try {
    // if (listener instanceof AbstractJcrNodeAdapter) {
    // ((AbstractJcrNodeAdapter) listener).getNode().getSession().save();
    // } else if (listener instanceof JcrPropertyAdapter) {
    // ((JcrPropertyAdapter) listener).getJcrItem().getSession().save();
    // }
    // } catch (RepositoryException e) {
    // }
    // }
    // }
    //
    // if (JcrItemAdapter.JCR_NAME.equals(editingPropertyId)) {
    // ((HierarchicalJcrContainer) getContainerDataSource()).fireItemSetChange();
    // }
    //
    // // request partial update
    //
    // // setEditing(null, null);
    // }

    // /**
    // * Cancel changes on currently editing item.
    // */
    // private void cancel() {
    // System.out.println("CANCELLEY!");
    // setEditing(null, null);
    // }

    // PARTIAL UPDATE ENABLEMENT

    // @Override
    // protected int getFirstUpdatedItemIndex() {
    // if (editingItemId != null) {
    // return indexOfId(editingItemId);
    // }
    // return super.getFirstUpdatedItemIndex();
    // }
    //
    // @Override
    // protected int getUpdatedRowCount() {
    // if (editingItemId != null) {
    // return 1;
    // }
    // return super.getUpdatedRowCount();
    // }

    // @Override
    // protected int getAddedRowCount() {
    // if (editingItemId != null) {
    // return 0;
    // }
    // return super.getAddedRowCount();
    // }

    // @Override
    // protected boolean isPartialRowUpdate() {
    // // provide same logic as treetable partial update on expand
    // boolean containerSupportsPartialUpdates = (getContainerDataSource() instanceof
    // ItemSetChangeNotifier);
    // boolean treeTableHasPartialUpdate = super.isPartialRowUpdate();
    // boolean editingHasPartialUpdate = editingItemId != null
    // && containerSupportsPartialUpdates
    // && !isRowCacheInvalidated();
    // // mutually exclude behaviors - they should never occur simultaneously though.
    // return treeTableHasPartialUpdate ^ editingHasPartialUpdate;
    // }

    // INPLACE EDITING FIELD FACTORY

    /**
     * A factory for creating inplace editing fields.
     */
    private class InplaceEditingFieldFactory extends DefaultFieldFactory {

        @Override
        public Field createField(Container container, final Object itemId, final Object propertyId, Component uiContext) {

            // add textfield only for selected row/column.
            if (editableColumns.contains(propertyId)
                && itemId.equals(editingItemId)
                && propertyId.equals(editingPropertyId)) {

                Field field = super.createField(container, itemId, propertyId, uiContext);

                // set focus at end of textfield
                ((AbstractComponent) field).setImmediate(false);
                if (field instanceof AbstractTextField) {
                    final AbstractTextField tf = (AbstractTextField) field;
                    tf.addListener(new FieldEvents.FocusListener() {

                        @Override
                        public void focus(FocusEvent event) {
                            // set cursor at end of field
                            tf.setCursorPosition(tf.toString().length());
                            tf.commit();

                            tf.addListener(new Property.ValueChangeListener() {

                                @Override
                                public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                                    System.out.println("CHANGEY!!");
                                }

                            });
                        }
                    });

                    tf.addListener(new FieldEvents.BlurListener() {

                        @Override
                        public void blur(BlurEvent event) {
                            System.out.println("BLUREY SAVEY!!");
                            tf.commit();
                            // save item on blur (e.g. click elsewhere, window change)
                            // TODO: put saving logic fucking OUT!

                            DefaultProperty prop = (DefaultProperty) tf.getPropertyDataSource();
                            JcrItemAdapter item = (JcrItemAdapter) prop.getListeners(Property.ValueChangeEvent.class).toArray()[0];

                            if (item instanceof JcrItemNodeAdapter) {
                                // saving node
                                try {
                                    System.out.println("Node detected!!");
                                    ((JcrItemNodeAdapter) item).getNode().getSession().save();
                                    ((AbstractJcrContainer) getContainerDataSource()).fireItemSetChange();
                                } catch (RepositoryException e) {
                                    // log.error(e);
                                }

                            } else if (item instanceof AbstractJcrAdapter) {
                                System.out.println("Property detected!!");
                                // saving property

                            }

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

    // /**
    // * The Class InplaceEditingKeyboardHandler.
    // */
    // private class InplaceEditingKeyboardHandler implements Handler {
    //
    // Action enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.ENTER, null);
    //
    // Action tabNext = new ShortcutAction("Tab", ShortcutAction.KeyCode.TAB, null);
    //
    // Action tabPrev = new ShortcutAction("Shift+Tab", ShortcutAction.KeyCode.TAB, new
    // int[]{ShortcutAction.ModifierKey.SHIFT});
    //
    // Action escape = new ShortcutAction("Esc", ShortcutAction.KeyCode.ESCAPE, null);
    //
    // Action add = new ShortcutAction("Add item", ShortcutAction.KeyCode.N, new
    // int[]{ShortcutAction.ModifierKey.META});
    //
    // Action delete = new ShortcutAction("Delete", ShortcutAction.KeyCode.DELETE, null);
    //
    // @Override
    // public Action[] getActions(Object target, Object sender) {
    // return new Action[]{enter, tabNext, tabPrev, escape, add, delete};
    // }
    //
    // @Override
    // @SuppressWarnings("unchecked")
    // public void handleAction(Action action, Object sender, Object target) {
    //
    // if (target instanceof AbstractTextField) {
    // if (action == enter) {
    // System.out.println("TF:KEY:ENTER");
    // save((AbstractField) target);
    // setEditing(null, null);
    // } else if (action == tabNext) {
    // System.out.println("TF:KEY:TAB");
    // save((AbstractField) target);
    // TableCell nextCell = getNextEditableCandidate(editingItemId, editingPropertyId);
    // setEditing(nextCell.getItemId(), nextCell.getPropertyId());
    // } else if (action == tabPrev) {
    // System.out.println("TF:KEY:SHIFT+TAB");
    // save((AbstractField) target);
    // TableCell previousCell = getPreviousEditableCandidate(editingItemId, editingPropertyId);
    // setEditing(previousCell.getItemId(), previousCell.getPropertyId());
    // } else if (action == escape) {
    // System.out.println("TF:KEY:ESCAPE");
    // ((AbstractField) target).discard();
    // cancel();
    // setEditing(null, null);
    // }
    //
    // } else if (target == InplaceEditingTreeTable.this) {
    // if (getValue() == null) {
    // return;
    // }
    // Object selectedId = ((Set<Object>) getValue()).iterator().next();
    //
    // if (action == enter) {
    // System.out.println("TT:KEY:ENTER");
    // // edit selected row at first column
    // Object propertyId = getVisibleColumns()[0];
    // if (!editableColumns.contains(propertyId)) {
    // propertyId = getNextEditableCandidate(selectedId, propertyId).getPropertyId();
    // }
    // setEditing(selectedId, propertyId);
    // } else if (action == add) {
    // System.out.println("TT:KEY:CMD+N");
    // } else if (action == delete) {
    // System.out.println("TT:KEY:DELETE");
    // if (selectedId != null) {
    // // Change selection
    // Object newSelectedId = nextItemId(selectedId);
    // if (newSelectedId == null) {
    // newSelectedId = prevItemId(selectedId);
    // }
    // select(newSelectedId);
    //
    // // Remove the item from the container
    // getContainerDataSource().removeItem(selectedId);
    // }
    // }
    // }
    // }
    // }
    //
    // // NEXT/PREVIOUS EDITABLE PROPERTY CANDIDATES
    //
    // private TableCell getNextEditableCandidate(Object itemId, Object propertyId) {
    //
    // List<Object> visibleColumns = Arrays.asList(getVisibleColumns());
    // Object newItemId = itemId;
    // int newColumn = visibleColumns.indexOf(propertyId);
    // do {
    // if (newColumn == visibleColumns.size() - 1) {
    // newItemId = nextItemId(newItemId);
    // }
    // newColumn = (newColumn + 1) % visibleColumns.size();
    // } while (!editableColumns.contains(visibleColumns.get(newColumn)) && newItemId != null);
    //
    // return new TableCell(newItemId, visibleColumns.get(newColumn));
    // }
    //
    // private TableCell getPreviousEditableCandidate(Object itemId, Object propertyId) {
    //
    // List<Object> visibleColumns = Arrays.asList(getVisibleColumns());
    // Object newItemId = itemId;
    // int newColumn = visibleColumns.indexOf(propertyId);
    // do {
    // if (newColumn == 0) {
    // newItemId = prevItemId(newItemId);
    // }
    // newColumn = (newColumn + visibleColumns.size() - 1) % visibleColumns.size();
    // } while (!editableColumns.contains(visibleColumns.get(newColumn)) && newItemId != null);
    //
    // return new TableCell(newItemId, visibleColumns.get(newColumn));
    // }
    //
    // /**
    // * The TableCell supporting class.
    // */
    // private class TableCell {
    //
    // private final Object itemId;
    //
    // private final Object propertyId;
    //
    // public TableCell(Object itemId, Object propertyId) {
    // this.itemId = itemId;
    // this.propertyId = propertyId;
    // }
    //
    // public Object getItemId() {
    // return itemId;
    // }
    //
    // public Object getPropertyId() {
    // return propertyId;
    // }
    // }

}
