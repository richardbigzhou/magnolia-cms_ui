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

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.shellapp.favorites.Favorite.FavoriteType;
import info.magnolia.ui.framework.favorite.bookmark.BookmarkStore;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;

/**
 * FavoritesManagerImpl.
 */
public class FavoritesManagerImpl implements FavoritesManager {
    private BookmarkStore bookmarkStore;

    @Inject
    public FavoritesManagerImpl(final BookmarkStore bookmarkStore) {
        this.bookmarkStore = bookmarkStore;
    }
    @Override
    public List<Item> getFavoritesForCurrentUser() {
        List<Item> favorites = new ArrayList<Item>();
        try {
            Node bookmarksNode = bookmarkStore.getBookmarkRoot();
            Iterable<Node> bookmarks = NodeUtil.getNodes(bookmarksNode, BookmarkStore.BOOKMARK_NODETYPE);
            for (Node bookmark : bookmarks) {
                favorites.add(new JcrNodeAdapter(bookmark));
            }
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return favorites;

    }
    @Override
    public void addFavoriteForCurrentUser(Item favorite) {
        if (FavoriteType.BOOKMARK == favorite.getItemProperty("type").getValue()) {
            addBookmark(favorite);
        }

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

    private void addBookmark(Item favorite) {
        try {
            Node bookmarkRoot = bookmarkStore.getBookmarkRoot();
            JcrNewNodeAdapter newBookmark = new JcrNewNodeAdapter(bookmarkRoot, BookmarkStore.BOOKMARK_NODETYPE);
            newBookmark.addItemProperty(BookmarkStore.BOOKMARK_URL_PROPERTY_NAME, favorite.getItemProperty(BookmarkStore.BOOKMARK_URL_PROPERTY_NAME));
            newBookmark.addItemProperty("icon", favorite.getItemProperty("icon"));
            newBookmark.addItemProperty("title", favorite.getItemProperty("title"));
            newBookmark.getNode().getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }
}
