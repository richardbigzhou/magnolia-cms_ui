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
package info.magnolia.ui.vaadin.integration.contentconnector;

import com.vaadin.data.Item;

/**
 * Simple stateless component used to bridge arbitrary object by their identifier to a Vaadin {@link Item}
 * and vice-versa.
 */
public interface ContentConnector {

    /**
     * Convert an item id (arbitrary object) to its string representation which can be
     * appended to a URL fragment and later be used to fetch the item id back.
     * @see ContentConnector#getItemIdByUrlFragment(java.lang.String).
     *
     * @param itemId id of an item to be converted to a string representation.
     * @return string representation of an item id.
     */
    String getItemUrlFragment(Object itemId);

    /**
     * Fetch item id from its string representation. Used primarily for restoring selection in views
     * from URL fragments.
     *
     * @param urlFragment URL fragment that points to an item.
     * @return item id that corresponds to the URL fragment.
     */
    Object getItemIdByUrlFragment(String urlFragment);

    /**
     * Get the default item id which for instance could be used as a view selection if
     * no actual item is selected. Most common example of such an item is a root node of
     * the tree hierarchy.
     *
     * @return default item id.
     */
    Object getDefaultItemId();

    /**
     * Fetch Vaadin {@link Item} by its id. Such item is not bound to any container and
     * can eventually be used in actions for editing.
     *
     * @param itemId item id.
     * @return Vaadin {@link Item} that corresponds to the id.
     */
    Item getItem(Object itemId);

    /**
     * Get item id.
     * @param item Item id of which is to be returned.
     * @return id of an item.
     */
    Object getItemId(Item item);

    /**
     * Check whether current {@link ContentConnector} is capable of fetching a Vaadin {@link Item}
     * with a specific id.
     *
     * @param itemId id of a Vaadin {@link Item} to look up.
     * @return true if such a Vaadin {@link Item} exists, false - otherwise.
     */
    boolean canHandleItem(Object itemId);
}
