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
package info.magnolia.ui.admincentral.shellapp.favorites;

import java.util.List;

/**
 * Manages the favorites for all users.
 *
 * Needs to persist somewhere, for each user
 *
 * is bookmark a better term for the code?
 *
 * A favorite can be a place in the ui, or a specific action like: add news article
 *  does it need parameters?
 *  is it a named action or a class?
 *    how would a named action be registered and handled
 *
 * what does an invitation to add a favorite look like in the ui?
 *
 * needs ordering so users can rearrange them
 */
public interface FavoriteManager {

    List<Favorite> getFavoritesForUser(String userId);

    void addFavoriteForUser(String userId, Favorite favorite);

    void removeFavorite(String userId, String id);
}
