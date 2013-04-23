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

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Implementation of a Manager for Favorites.
 */
public class FavoritesManagerImpl implements FavoritesManager {
    private FavoriteStore bookmarkStore;

    @Inject
    public FavoritesManagerImpl(final FavoriteStore bookmarkStore) {
        this.bookmarkStore = bookmarkStore;
    }
    @Override
    public JcrItemNodeAdapter getFavorites() {
        try {
            Node bookmarksNode = bookmarkStore.getBookmarkRoot();
            JcrNodeAdapter favorites = new JcrNodeAdapter(bookmarksNode);

            Iterable<Node> bookmarks = NodeUtil.getNodes(bookmarksNode, AdmincentralNodeTypes.Favorite.NAME);
            JcrNodeAdapter currentChild;
            for (Node bookmark : bookmarks) {
                currentChild = new JcrNodeAdapter(bookmark);
                currentChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.TITLE)));
                currentChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.URL)));
                currentChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.ICON)));
                favorites.addChild(currentChild);
            }
            return favorites;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
    @Override
    public void addFavorite(JcrNewNodeAdapter favorite) {
        try {
            final String title = (String) favorite.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue();
            favorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(ModelConstants.JCR_NAME, "", Path.getValidatedLabel(title)));
            favorite.getNode().getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public JcrNewNodeAdapter createFavoriteSuggestion(String location, String title, String icon) {
        Node bookmarkRoot;
        try {
            bookmarkRoot = bookmarkStore.getBookmarkRoot();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        JcrNewNodeAdapter newFavorite = new JcrNewNodeAdapter(bookmarkRoot, AdmincentralNodeTypes.Favorite.NAME);
        newFavorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", title));
        newFavorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", location));
        newFavorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", icon));
        return newFavorite;
    }

    @Override
    public void removeFavorite(String id) {
        try {
            Node bookmarkRoot = bookmarkStore.getBookmarkRoot();
            bookmarkRoot.getNode(id).remove();
            bookmarkRoot.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

}
