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

        /**
         * Adding instantly a new group and a new favorite (and add the latter to the former).
         * @param newFavorite The JcrNewNodeAdapter for the new favorite.
         * @param newGroup The JcrNewNodeAdapter for the new group.
         */
        void addFavoriteAndGroup(JcrNewNodeAdapter newFavorite, JcrNewNodeAdapter newGroup);

        /**
         * Adds a favorite.
         * @param newFavorite The JcrNewNodeAdapter for the new favorite.
         */
        void addFavorite(JcrNewNodeAdapter newFavorite);

        /**
         * Edit a favorite.
         * @param relPath  The relative path of the favorite to edit.
         * @param newTitle The new title.
         */
        void editFavorite(String relPath, String newTitle);

        /**
         * Removes a favorite by the given path.
         * @param relPath of the favorite to remove.
         */
        void removeFavorite(String relPath);

        /**
         * To go to the given location.
         * @param location The location to go to.
         */
        void goToLocation(String location);

        /**
         * Add a group.
         * @param newGroup The JcrNewNodeAdapter for the new group.
         */
        void addGroup(JcrNewNodeAdapter newGroup);

        /**
         * Edit a group.
         * @param relPath The relative path of the group to edit.
         * @param newTitle The new title.
         */
        void editGroup(String relPath, String newTitle);

        /**
         * Remove a group.
         * @param relPath The relative path of the group to remove.
         */
        void removeGroup(String relPath);

        /**
         * Moves a favorite.
         * @param relPath The relative path to move the node.
         * @param group The node-name of the group.
         */
        void moveFavorite(String relPath, String group);

        /**
         * Orders a favorite before a sibling.
         * @param relPath The path of the node to move.
         * @param sibling The node-name of the sibling.
         */
        void orderFavoriteBefore(String relPath, String sibling);

        /**
         * Orders a favorite after a sibling.
         * @param relPath The path of the node to move.
         * @param sibling The node-name of the sibling.
         */
        void orderFavoriteAfter(String relPath, String sibling);

        /**
         * Orders a group before a sibling.
         * @param relPath The path of the node to move.
         * @param sibling The node-name of the sibling.
         */
        void orderGroupBefore(String relPath, String sibling);

        /**
         * Orders a group after a sibling.
         * @param relPath The path of the node to move.
         * @param groupToMove The node-name of the sibling.
         */
        void orderGroupAfter(String groupToMove, String relPath);
    }

}
