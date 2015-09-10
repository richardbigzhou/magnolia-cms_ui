/**
 * This file Copyright (c) 2011-2015 Magnolia International
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
import info.magnolia.ui.workbench.list.ListViewImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Container;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;

/**
 * Default Vaadin implementation of the tree view.
 */
public class TreeViewImpl extends ListViewImpl implements TreeView {

    private MagnoliaTreeTable tree;

    private boolean editable;
    private final List<Object> editableColumns = new ArrayList<Object>();
    private InplaceEditingFieldFactory fieldFactory;
    private ExpandListener expandListener;
    private CollapseListener collapseListener;
    private Container shortcutActionManager;
    private EditingKeyboardHandler editingKeyboardHandler;
    private ColumnGenerator bypassedColumnGenerator;
    private TreeRowScroller rowScroller;

    @Override
    protected TreeTable createTable(com.vaadin.data.Container container) {
        return new MagnoliaTreeTable(container);
    }

    @Override
    protected void initializeTable(Table table) {
        super.initializeTable(table);
        this.tree = (MagnoliaTreeTable) table;
        rowScroller = new TreeRowScroller(tree);
        collapseListener = new CollapsedNodeListener();
        tree.addCollapseListener(collapseListener);
    }

    @Override
    public void select(List<Object> itemIds) {
        tree.setValue(null);

        Object firstItemId = itemIds == null || itemIds.isEmpty() ? null : itemIds.get(0);
        if (firstItemId == null) {
            return;
        }

        for (Object id : itemIds) {
            tree.select(id);
        }

        rowScroller.bringRowIntoView(firstItemId);
    }

    @Override
    public void expand(Object itemId) {
        rowScroller.expandTreeToNode(itemId, true);
    }

    @Override
    protected TreeView.Listener getListener() {
        return (TreeView.Listener) super.getListener();
    }

    @Override
    public TreeTable asVaadinComponent() {
        return tree;
    }

    @Override
    public void setEditable(boolean editable) {
        if (editable) {
            // field factory
            fieldFactory = new InplaceEditingFieldFactory();
            fieldFactory.setFieldBlurListener(new BlurListener() {

                @Override
                public void blur(BlurEvent event) {
                    Object source = event.getSource();
                    if (source instanceof Field<?>) {
                        saveItemProperty(((Field<?>) source).getPropertyDataSource());
                    }
                    setEditing(null, null);
                }
            });
            tree.setTableFieldFactory(fieldFactory);

            // expanding tree must turn off editing
            expandListener = new ExpandListener() {

                @Override
                public void nodeExpand(ExpandEvent event) {
                    setEditing(null, null);
                }
            };

            tree.addExpandListener(expandListener);

            // double-click listener
            ItemClickListener clickListener = new ItemClickListener() {

                @Override
                public void itemClick(ItemClickEvent event) {
                    if (event.isDoubleClick()) {
                        setEditing(event.getItemId(), event.getPropertyId());
                    }
                }
            };
            tree.addItemClickListener(clickListener);

            // keyboard shortcuts
            editingKeyboardHandler = new EditingKeyboardHandler(tree);
            if (shortcutActionManager != null) {
                shortcutActionManager.addActionHandler(editingKeyboardHandler);
            }

        } else {
            tree.setTableFieldFactory(null);
            fieldFactory = null;
            tree.removeExpandListener(expandListener);
            expandListener = null;
            if (shortcutActionManager != null) {
                shortcutActionManager.removeActionHandler(editingKeyboardHandler);
            }
            editingKeyboardHandler = null;
        }

        tree.setEditable(editable);
        this.editable = editable;
    }

    @Override
    public void setEditableColumns(Object... editablePropertyIds) {
        editableColumns.clear();
        editableColumns.addAll(Arrays.asList(editablePropertyIds));
    }

    private void setEditing(Object itemId, Object propertyId) {

        // restore generated column if it was disabled for editing
        if (bypassedColumnGenerator != null) {
            tree.addGeneratedColumn(fieldFactory.getEditingPropertyId(), bypassedColumnGenerator);
            bypassedColumnGenerator = null;
        }

        if (editable && editableColumns.contains(propertyId)) {
            if (itemId == null || propertyId == null) {
                tree.focus();
                fieldFactory.setEditing(null, null);
            } else {
                // disable generated column for editing
                if ((bypassedColumnGenerator = tree.getColumnGenerator(propertyId)) != null) {
                    tree.removeGeneratedColumn(propertyId);
                }
                fieldFactory.setEditing(itemId, propertyId);
            }
        } else {
            fieldFactory.setEditing(null, null);
        }
        tree.refreshRowCache();
    }

    private void saveItemProperty(Property<?> propertyDataSource) {
        getListener().onItemEdited(fieldFactory.getEditingItemId(), fieldFactory.getEditingPropertyId(), propertyDataSource);
    }

    @Override
    public void setDragAndDropHandler(DropHandler dropHandler) {
        if (dropHandler != null) {
            tree.setDragMode(TableDragMode.MULTIROW);
            tree.setDropHandler(dropHandler);
        } else {
            tree.setDragMode(TableDragMode.NONE);
            tree.setDropHandler(null);
        }
    }

    // KEYBOARD SHORTCUTS

    @Override
    public void setActionManager(Container shortcutActionManager) {
        if (editable) {
            shortcutActionManager.addActionHandler(editingKeyboardHandler);
        }
        this.shortcutActionManager = shortcutActionManager;
    }

    public void setSortable(boolean sortable) {
        if (tree.getContainerDataSource() instanceof HierarchicalJcrContainer) {
            tree.setSortEnabled(sortable);
            ((HierarchicalJcrContainer) tree.getContainerDataSource()).setSortable(sortable);
        }
    }

    private final class CollapsedNodeListener implements CollapseListener {

        @Override
        public void nodeCollapse(CollapseEvent event) {
            // collapsing tree must turn off editing
            if (editable) {
                setEditing(null, null);
            }
            Object collapsedNodeId = event.getItemId();
            // collapsed node descendants should be unselected as they're not visible, yet any ops affect them
            unselectDescendants(collapsedNodeId);
        }
    }

    private void unselectDescendants(final Object parentId) {
        if (tree.isMultiSelect()) {
            Set<Object> selectedIds = (Set<Object>) tree.getValue();
            for (Object id : selectedIds) {
                if (tree.isDescendantOf(id, parentId)) {
                    tree.unselect(id);
                }
            }
        }
    }

    /**
     * The Class EditingKeyboardHandler for keyboard shortcuts with inplace editing.
     */
    private class EditingKeyboardHandler implements Handler {

        private final ShortcutAction enter = new ShortcutAction("Enter", ShortcutAction.KeyCode.ENTER, null);

        private final ShortcutAction tabNext = new ShortcutAction("Tab", ShortcutAction.KeyCode.TAB, null);

        private final ShortcutAction tabPrev = new ShortcutAction("Shift+Tab", ShortcutAction.KeyCode.TAB, new int[] { ShortcutAction.ModifierKey.SHIFT });

        private final ShortcutAction escape = new ShortcutAction("Esc", ShortcutAction.KeyCode.ESCAPE, null);

        private final TreeTable tree;

        public EditingKeyboardHandler(TreeTable tree) {
            this.tree = tree;
        }

        @Override
        public Action[] getActions(Object target, Object sender) {
            // TODO: Find a better solution for handling tab key events: MGNLUI-1384
            return new Action[] { enter, tabNext, tabPrev, escape };
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

            // Because shortcutActionManager is typically the workbench's keyboardEventPanel, this handler might be called from other content views
            if (tree == null || !tree.isAttached()) {
                return;
            }

            if (target != tree && target instanceof Field) {
                Field<?> field = (Field<?>) target;

                if (shortcut == enter || shortcut.getKeyCode() == enter.getKeyCode()) {
                    saveItemProperty(fieldFactory.getField().getPropertyDataSource());
                    setEditing(null, null);

                } else if (action == tabNext) {
                    saveItemProperty(fieldFactory.getField().getPropertyDataSource());
                    editNextCell(fieldFactory.getEditingItemId(), fieldFactory.getEditingPropertyId());

                } else if (action == tabPrev) {
                    saveItemProperty(fieldFactory.getField().getPropertyDataSource());
                    editPreviousCell(fieldFactory.getEditingItemId(), fieldFactory.getEditingPropertyId());

                } else if (action == escape) {
                    setEditing(null, null);
                }
            } else if (target == tree) {
                if (tree.getValue() == null) {
                    return;
                }

                if (shortcut == enter || shortcut.getKeyCode() == enter.getKeyCode()) {
                    editFirstCell();

                }
            }
        }
    }

    // EDITING API

    private void editNextCell(Object itemId, Object propertyId) {

        List<Object> visibleColumns = Arrays.asList(tree.getVisibleColumns());
        Object newItemId = itemId;
        int newColumn = visibleColumns.indexOf(propertyId);
        do {
            if (newColumn == visibleColumns.size() - 1) {
                newItemId = tree.nextItemId(newItemId);
            }
            newColumn = (newColumn + 1) % visibleColumns.size();
        } while (!editableColumns.contains(visibleColumns.get(newColumn)) && newItemId != null);

        setEditing(newItemId, visibleColumns.get(newColumn));
    }

    public void editPreviousCell(Object itemId, Object propertyId) {

        List<Object> visibleColumns = Arrays.asList(tree.getVisibleColumns());
        Object newItemId = itemId;
        int newColumn = visibleColumns.indexOf(propertyId);
        do {
            if (newColumn == 0) {
                newItemId = tree.prevItemId(newItemId);
            }
            newColumn = (newColumn + visibleColumns.size() - 1) % visibleColumns.size();
        } while (!editableColumns.contains(visibleColumns.get(newColumn)) && newItemId != null);

        setEditing(newItemId, visibleColumns.get(newColumn));
    }

    public void editFirstCell() {

        // get first selected itemId, handles multiple selection mode
        Object firstSelectedId = tree.getValue();
        if (firstSelectedId instanceof Collection) {
            if (((Collection<?>) firstSelectedId).size() > 0) {
                firstSelectedId = ((Set<?>) firstSelectedId).iterator().next();
            } else {
                firstSelectedId = null;
            }
        }

        // Edit selected row at first column
        Object propertyId = tree.getVisibleColumns()[0];
        if (!editableColumns.contains(propertyId)) {
            editNextCell(firstSelectedId, propertyId);
        } else {
            setEditing(firstSelectedId, propertyId);
        }
    }
}
