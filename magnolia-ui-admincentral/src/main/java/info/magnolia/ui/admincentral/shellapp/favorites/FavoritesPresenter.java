/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import javax.inject.Inject;

/**
 * FavoritesPresenter.
 */
public class FavoritesPresenter implements FavoritesView.Listener {

    private FavoritesView view;
    private FavoritesManager favoritesManager;

    @Inject
    public FavoritesPresenter(final FavoritesView view, final FavoritesManager favoritesManager) {
        this.view = view;
        this.favoritesManager = favoritesManager;
    }

    public FavoritesView start() {
        view.setListener(this);
        view.init(favoritesManager.getFavorites(), createNewFavoriteSuggestion("", "", ""));
        return view;
    }

    @Override
    public void removeFavorite(String id) {
        favoritesManager.removeFavorite(id);
        view.init(favoritesManager.getFavoritesForCurrentUser(), createNewFavoriteSuggestion("", "", ""));

    }

    @Override
    public void addFavorite(JcrItemNodeAdapter favorite) {
        favoritesManager.addFavorite(favorite);
        // Give view the updated favorites collection, so that the newly added one is immediately displayed.
        view.init(favoritesManager.getFavorites(), createNewFavoriteSuggestion("", "", ""));
    }

    /**
     * @return a {@link JcrNewNodeAdapter} used to pre-populate a form in the UI with a suggestion for a new favorite.
     */
    public JcrNewNodeAdapter createNewFavoriteSuggestion(String location, String title, String icon) {
        JcrNewNodeAdapter newFavorite = new JcrNewNodeAdapter(favoritesManager.getFavorites().getNode(), AdmincentralNodeTypes.Favorite.NAME);
        newFavorite.addItemProperty(ModelConstants.JCR_NAME, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", title));
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.URL, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", location));
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.ICON, DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", icon));
        return newFavorite;
    }

}
