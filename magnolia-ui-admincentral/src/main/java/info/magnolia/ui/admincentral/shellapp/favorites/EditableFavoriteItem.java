/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.favorites;

/**
 * A common interface for both {@code FavoritesEntry} and {@code FavoritesGroup}.
 */
public interface EditableFavoriteItem {

    /**
     * Switching the visibility of the icons of the item.
     *
     * @param visible boolean indicating whether the icons should be visible or not.
     */
    void setIconsVisibility(boolean visible);

    /**
     * Returning a boolean indicating whether the icons of the item are visible.
     */
    boolean iconsAreVisible();

    /**
     * Sets the item to a state where it cannot be edited; text-field goes read-only (and changed but not saved title is lost).
     * Note: Visibility of the icons is not affected by calling this method.
     */
    void setToNonEditableState();

    /**
     * Returning an ID which is unique among all other EditableFavoriteItem within a "session" of a logged-in user in a running instance.
     */
    String getItemId();


}
