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

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Map;

import com.vaadin.ui.Component;

/**
 * View for favorites.
 */
public interface FavoritesView extends View {

    static final String FAVORITES_BASENAME = "info.magnolia.ui.admincentral.favorites.messages";

    void setListener(Listener listener);

    void init(AbstractJcrNodeAdapter favoritesRoot, JcrNewNodeAdapter favoriteSuggestion, JcrNewNodeAdapter groupSuggestion, Map<String, String> availableGroups);

    void setFavoriteLocation(JcrNewNodeAdapter location, JcrNewNodeAdapter groupSuggestion, Map<String, String> availableGroups);

    /**
     * Unselect the currently selected component, as only one can be selected at any point in time.
     */
    void updateSelection(Component newSelection);

    /**
     * Listener.
     */
    interface Listener {
        void addFavorite(JcrNewNodeAdapter newFavorite);

        void editFavorite(String relPath, String newTitle);

        void removeFavorite(String relPath);

        void goToLocation(String location);

        void addGroup(JcrNewNodeAdapter newGroup);

        void editGroup(String relPath, String newTitle);

        void removeGroup(String relPath);

        void moveFavorite(String relPath, String group);

        void orderFavoriteBefore(String relPath, String sibling);

        void orderFavoriteAfter(String relPath, String sibling);

        void orderGroupBefore(String relPath, String sibling);

        void orderGroupAfter(String groupToMove, String relPath);
    }

}
