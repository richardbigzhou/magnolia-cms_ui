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
package info.magnolia.ui.workbench.tree;

import info.magnolia.ui.workbench.event.ItemEditedEvent;
import info.magnolia.ui.workbench.list.ListViewImpl;

import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.event.dd.DropHandler;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TreeTable;

/**
 * Vaadin UI component that displays a tree.
 */
public class TreeViewImpl extends ListViewImpl implements TreeView {

    private static final Logger log = LoggerFactory.getLogger(TreeViewImpl.class);

    private final TreeTable treeTable;

    private final ItemEditedEvent.Handler itemEditedListener = new ItemEditedEvent.Handler() {

        @Override
        public void onItemEdited(ItemEditedEvent event) {
            if (getListener() != null) {
                getListener().onItemEdited(event.getItem());
            }
        }
    };

    public TreeViewImpl() {
        this(new InplaceEditingTreeTable());
    }

    public TreeViewImpl(TreeTable tree) {
        super(tree);
        tree.setSortEnabled(false);

        tree.setCollapsed(tree.firstItemId(), false);

        this.treeTable = tree;
    }

    @Override
    public void setEditable(boolean editable) {
        treeTable.setEditable(editable);
        if (editable && treeTable instanceof InplaceEditingTreeTable) {
            ((InplaceEditingTreeTable) treeTable).addItemEditedListener(itemEditedListener);
            ((InplaceEditingTreeTable) treeTable).addKeyboardHandlers();
        } else {
            ((InplaceEditingTreeTable) treeTable).removeItemEditedListener(itemEditedListener);
        }
    }

    @Override
    public void setEditableColumns(Object... propertyIds) {
        ((InplaceEditingTreeTable) treeTable).setEditableColumns(propertyIds);
    }

    @Override
    public void setDragAndDropHandler(DropHandler dropHandler) {
        if (dropHandler != null) {
            treeTable.setDragMode(TableDragMode.ROW);
            treeTable.setDropHandler(dropHandler);
        } else {
            treeTable.setDragMode(TableDragMode.NONE);
            treeTable.setDropHandler(null);
        }
    }

    @Override
    public void select(List<String> itemIds) {
        String firstItemId = itemIds.get(0);
        expandTreeToNode(firstItemId);

        treeTable.setValue(null);
        for (String id : itemIds) {
            treeTable.select(id);
        }
        // do not #setCurrentPageFirstItemId because AbstractJcrContainer's index resolution is super slow.
    }

    private void expandTreeToNode(String nodeId) {
        HierarchicalJcrContainer container = (HierarchicalJcrContainer) treeTable.getContainerDataSource();
        String workbenchPath = container.getWorkbenchDefinition().getPath();

        try {
            Item item = container.getJcrItem(nodeId);
            if (item == null || !item.getPath().contains(workbenchPath)) {
                return;
            }

            Node node = (item instanceof Property) ? item.getParent() : (Node) item;

            // as long as parent is within the scope of the workbench
            while (node != null && !StringUtils.equals(node.getPath(), workbenchPath)) {
                treeTable.setCollapsed(node.getIdentifier(), false);
                node = node.getParent();
            }

        } catch (RepositoryException e) {
            log.warn("Could not collect the parent hierarchy of node {}", nodeId, e);
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
