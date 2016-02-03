/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.workbench.tree.drop;


import info.magnolia.ui.workbench.tree.MoveLocation;

import com.vaadin.data.Item;

/**
 * Define methods used to configure a
 * {@link com.vaadin.event.dd.DropHandler.DropHandler}. DropConstraint
 * implemented class are defined in the
 * {@link info.magnolia.ui.workbench.definition.WorkbenchDefinition}, <br>
 * and used in by {@link TreeViewDropHandler},<br>
 * in order to configure drag and drop ability in
 * {@link info.magnolia.ui.workbench.tree.TreeView}.
 */
public interface DropConstraint {

    /**
     * Checks whether source item is allowed to be moved relative (based on the move location) to target item.
     */
    public boolean isAllowedAt(Item sourceItem, Item targetItem, MoveLocation location);

    /**
     * @param sourceItem
     *            Moved Item.
     * @param targetItem
     *            Target Item .
     * @return true if the sourceItem is allowed to be set as a child of
     *         targetItem.<br>
     *         false otherwise.
     */
    public boolean allowedAsChild(Item sourceItem, Item targetItem);

    /**
     * @param sourceItem
     *            Moved Item.
     * @param targetItem
     *            Target Item .
     * @return true if the sourceItem is allowed to be put before the
     *         targetItem.<br>
     *         false otherwise.
     */
    public boolean allowedBefore(Item sourceItem, Item targetItem);

    /**
     * @param sourceItem
     *            Moved Item.
     * @param targetItem
     *            Target Item .
     * @return true if the sourceItem is allowed to be put after the targetItem.<br>
     *         false otherwise.
     */
    public boolean allowedAfter(Item sourceItem, Item targetItem);

    /**
     * @return true if the Item is allowed to be moved <br>
     *         false otherwise.
     */
    public boolean allowedToMove(Item sourceItem);
}
