/**
 * This file Copyright (c) 2011-2014 Magnolia International
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
import java.util.List;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;

/**
 * Default Vaadin implementation of the tree view.
 */
public class TreeViewImpl extends ListViewImpl implements TreeView {

    private final MagnoliaTreeTable tree;

    private boolean editable;
    private final List<Object> editableColumns = new ArrayList<Object>();
    private InplaceEditingFieldFactory fieldFactory;
    private ExpandListener expandListener;
    private CollapseListener collapseListener;
    private ColumnGenerator bypassedColumnGenerator;

    public TreeViewImpl() {
        this(new MagnoliaTreeTable());
    }

    public TreeViewImpl(MagnoliaTreeTable tree) {
        super(tree);
        tree.setSortEnabled(false);
        tree.setCollapsed(tree.firstItemId(), false);
        this.tree = tree;
    }

    @Override
    public void select(List<Object> itemIds) {
        Object firstItemId = itemIds == null || itemIds.isEmpty() ? null : itemIds.get(0);
        if (firstItemId == null || tree.isSelected(firstItemId)) {
            return;
        }
        tree.focus();
        // expandTreeToNode(firstItemId, false);

        tree.setValue(null);
        for (Object id : itemIds) {
            tree.select(id);
        }
        tree.setCurrentPageFirstItemId(firstItemId);
    }

    @Override
    public void expand(Object itemId) {
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

            // expanding and collapsing tree must turn off editing
            expandListener = new ExpandListener() {

                @Override
                public void nodeExpand(ExpandEvent event) {
                    setEditing(null, null);
                }
            };
            collapseListener = new CollapseListener() {

                @Override
                public void nodeCollapse(CollapseEvent event) {
                    setEditing(null, null);
                }
            };
            tree.addExpandListener(expandListener);
            tree.addCollapseListener(collapseListener);

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

        } else {
            tree.setTableFieldFactory(null);
            fieldFactory = null;
            tree.removeExpandListener(expandListener);
            tree.removeCollapseListener(collapseListener);
            expandListener = null;
            collapseListener = null;
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

        // if (itemId != null && propertyId != null) {
        // Item item = getItem(itemId);
        // Property<?> property = item.getItemProperty(propertyId);
        // } else {
        // if ((bypassedColumnGenerator = tree.getColumnGenerator(propertyId)) != null) {
        // tree.removeGeneratedColumn(propertyId);
        // }
        // }
        // } else {
        // if (bypassedColumnGenerator != null) {
        // addGeneratedColumn(editingPropertyId, bypassedColumnGenerator);
        // bypassedColumnGenerator = null;
        // }
        // }

        if (editable && editableColumns.contains(propertyId)) {
            fieldFactory.setEditing(itemId, propertyId);
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
        // TODO
    }

}
