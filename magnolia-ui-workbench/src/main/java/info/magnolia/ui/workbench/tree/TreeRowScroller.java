/**
 * This file Copyright (c) 2014-2016 Magnolia International
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

import com.vaadin.data.Item;
import com.vaadin.ui.TreeTable;

/**
 * Just like {@link RowScroller} - brings tree table rows into view and also expands the parent
 * nodes if needed.
 */
public class TreeRowScroller extends RowScroller {

    public TreeRowScroller(TreeTable table) {
        super(table);
    }

    @Override
    public void bringRowIntoView(Object rowId) {
        expandTreeToNode(rowId, false);
        super.bringRowIntoView(rowId);
    }

    @Override
    public TreeTable getParent() {
        return (TreeTable) super.getParent();
    }

    @Override
    protected int getItemIndex(Object itemId) {
        if (getParent() instanceof MagnoliaTreeTable) {
            return ((MagnoliaTreeTable)getParent()).indexOfId(itemId);
        }
        return super.getItemIndex(itemId);
    }

    public void expandTreeToNode(Object id, boolean expandNode) {
        com.vaadin.data.Container.Hierarchical container = getParent().getContainerDataSource();
        Item item = container.getItem(id);

        if (item == null) {
            return;
        }

        // Determine node to expand.
        Object node = null;
        if (!container.areChildrenAllowed(id)) {
            node = container.getParent(id);
        } else {
            if (expandNode) {
                node = id;
            } else {
                Object parent = container.getParent(id);
                // Check if item is root.
                if (parent != null) {
                    node = parent;
                }
            }
        }

        // as long as parent is within the scope of the workbench
        while (node != null) {
            getParent().setCollapsed(node, false);
            node = container.getParent(node);
        }

    }
}
