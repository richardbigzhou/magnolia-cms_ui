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
package info.magnolia.ui.workbench;

import info.magnolia.ui.api.view.View;

import java.util.List;
import java.util.Set;

import com.vaadin.data.Item;

/**
 * UI component that displays content in one of the supported view types (list, tree etc).
 */
public interface ContentView extends View {

    void setListener(ContentView.Listener listener);

    /**
     * Selects the items with given IDs in the content view.
     *
     * @param itemIds IDs
     */
    void select(List<Object> itemIds);

    /**
     * Expands an item if the view supports it.
     *
     * @param itemId ID
     */
    void expand(Object itemId);

    void onShortcutKey(int keyCode, int[] modifierKeys);

    /**
     * Decides whether the user can select multiple items.
     */
    void setMultiselect(boolean multiselect);

    /**
     * Listener for the ContentView.
     */
    public interface Listener {

        void onItemSelection(Set<Object> items);

        void onDoubleClick(Object itemId);

        void onRightClick(Object itemId, int clickX, int clickY);

        void onShortcutKey(int keyCode, int... modifierKeys);

        String getIcon(Item item);
    }
}
