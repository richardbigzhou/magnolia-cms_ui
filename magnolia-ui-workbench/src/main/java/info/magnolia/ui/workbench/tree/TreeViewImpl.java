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
package info.magnolia.ui.workbench.tree;

import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.list.ListViewImpl;

import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TreeTable;

/**
 * Vaadin UI component that displays a tree.
 */
public class TreeViewImpl extends ListViewImpl implements TreeView {

    private final TreeTable treeTable;

    private final ItemEditedEvent.Handler itemEditedListener = new ItemEditedEvent.Handler() {

        @Override
        public void onItemEdited(ItemEditedEvent event) {
            if (getListener() != null) {
                getListener().onItemEdited(event.getItem());
            }
        }
    };

    // private Set<?> defaultValue = null;

    public TreeViewImpl() {
        this(new InplaceEditingTreeTable());
    }

    public TreeViewImpl(TreeTable tree) {
        super(tree);
        tree.setSortEnabled(false);

        tree.setCollapsed(tree.firstItemId(), false);

        this.treeTable = tree;
    }

    // @Override
    // protected void bindHandlers() {
    //
    // treeTable.addValueChangeListener(new TreeTable.ValueChangeListener() {
    //
    // @Override
    // public void valueChange(ValueChangeEvent event) {
    // if (defaultValue == null && event.getProperty().getValue() instanceof Set) {
    // defaultValue = (Set<?>) event.getProperty().getValue();
    // }
    // final Object value = event.getProperty().getValue();
    // if (value instanceof String) {
    // // presenterOnItemSelection(String.valueOf(value));
    // } else if (value instanceof Set) {
    // final Set<?> set = new HashSet<Object>((Set<?>) value);
    // set.removeAll(defaultValue);
    // if (set.size() == 1) {
    // // presenterOnItemSelection(String.valueOf(set.iterator().next()));
    // } else if (set.size() == 0) {
    // // presenterOnItemSelection(null);
    // treeTable.setValue(null);
    // }
    // }
    // }
    // });
    // }

    @Override
    public void setEditable(boolean editable) {
        treeTable.setEditable(editable);
        if (editable && treeTable instanceof InplaceEditingTreeTable) {
            ((InplaceEditingTreeTable) treeTable).addItemEditedListener(itemEditedListener);
        } else {
            ((InplaceEditingTreeTable) treeTable).removeItemEditedListener(itemEditedListener);
        }
    }

    @Override
    public void setEditableColumns(Object... propertyIds) {
        ((InplaceEditingTreeTable) treeTable).setEditableColumns(propertyIds);
    }

    @Override
    public void deactivateDragAndDrop() {
        treeTable.setDragMode(TableDragMode.NONE);
    }

    @Override
    public void select(String path) {
        if (!"/".equals(path)) {
            expandTreeToNode(path);
            treeTable.setCurrentPageFirstItemId(path);
        }
        treeTable.select(path);
    }

    private void expandTreeToNode(String path) {
        String[] segments = path.split("/");
        String segmentPath = "";
        // Expand each parent node in turn.
        for (int s = 0; s < segments.length - 1; s++) {
            if (!"".equals(segments[s])) {
                segmentPath += "/" + segments[s];
                treeTable.setCollapsed(segmentPath, false);
            }
        }
    }

    @Override
    protected TreeView.Listener getListener() {
        return (TreeView.Listener) super.getListener();
    }

    @Override
    public ViewType getViewType() {
        return ViewType.TREE;
    }

    @Override
    public TreeTable asVaadinComponent() {
        return treeTable;
    }

}
