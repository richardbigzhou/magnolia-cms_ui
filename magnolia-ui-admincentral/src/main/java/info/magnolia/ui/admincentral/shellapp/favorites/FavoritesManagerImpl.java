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
import info.magnolia.ui.api.ModelConstants;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

/**
 * Implementation of a Manager for Favorites.
 */
public class FavoritesManagerImpl implements FavoritesManager {
    private FavoriteStore favoriteStore;

    @Inject
    public FavoritesManagerImpl(final FavoriteStore favoriteStore) {
        this.favoriteStore = favoriteStore;
    }

    @Override
    public JcrItemNodeAdapter getFavorites() {
        try {
            Node bookmarksNode = favoriteStore.getBookmarkRoot();
            JcrNodeAdapter favorites = new JcrNodeAdapter(bookmarksNode);

            Iterable<Node> bookmarks = NodeUtil.getNodes(bookmarksNode);
            JcrNodeAdapter currentChild;
            for (Node bookmark : bookmarks) {
                currentChild = new JcrNodeAdapter(bookmark);
                currentChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.TITLE)));

                final String bookmarkNodeType = bookmark.getPrimaryNodeType().getName();

                if (AdmincentralNodeTypes.Favorite.NAME.equals(bookmarkNodeType)) {
                    currentChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.URL)));
                    currentChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.ICON)));
                } else if (AdmincentralNodeTypes.FavoriteGroup.NAME.equals(bookmarkNodeType)) {
                    Iterable<Node> bookmarksWithGroup = NodeUtil.getNodes(bookmark, AdmincentralNodeTypes.Favorite.NAME);
                    JcrNodeAdapter favoriteChild;
                    for (Node bookmarkWithGroup : bookmarksWithGroup) {
                        favoriteChild = new JcrNodeAdapter(bookmarkWithGroup);
                        favoriteChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", PropertyUtil.getString(bookmarkWithGroup, AdmincentralNodeTypes.Favorite.TITLE)));
                        favoriteChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", PropertyUtil.getString(bookmarkWithGroup, AdmincentralNodeTypes.Favorite.URL)));
                        favoriteChild.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", PropertyUtil.getString(bookmarkWithGroup, AdmincentralNodeTypes.Favorite.ICON)));
                        currentChild.addChild(favoriteChild);
                    }
                }
                favorites.addChild(currentChild);
            }
            return favorites;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public Map<String, String> getGroupsNames() {
        Map<String, String> groupNames = new HashMap<String, String>();
        Iterable<Node> groups;
        try {
            Node bookmarksNode = favoriteStore.getBookmarkRoot();
            groups = NodeUtil.getNodes(bookmarksNode, AdmincentralNodeTypes.FavoriteGroup.NAME);
            for (Node group : groups) {
                groupNames.put(group.getName(), PropertyUtil.getString(group, AdmincentralNodeTypes.Favorite.TITLE));
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        return groupNames;
    }

    @Override
    public void addFavorite(JcrNewNodeAdapter favorite) {
        try {
            final String title = (String) favorite.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue();
            favorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(ModelConstants.JCR_NAME, "", Path.getValidatedLabel(title)));
            Node newFavorite = favorite.getNode();
            Session session = newFavorite.getSession();
            final String group = (String) favorite.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).getValue();
            if (StringUtils.isNotBlank(group)) {
                Node parent = session.getNode(newFavorite.getParent().getPath() + "/" + group);
                NodeUtil.moveNode(newFavorite, parent);
            }
            session.save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public JcrNewNodeAdapter createFavoriteSuggestion(String location, String title, String icon) {
        Node bookmarkRoot;
        try {
            bookmarkRoot = favoriteStore.getBookmarkRoot();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        JcrNewNodeAdapter newFavorite = new JcrNewNodeAdapter(bookmarkRoot, AdmincentralNodeTypes.Favorite.NAME);
        newFavorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", title));
        newFavorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.URL, "", location));
        newFavorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.GROUP, "", ""));
        newFavorite.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.ICON, "", StringUtils.defaultIfEmpty(icon, "icon-app")));
        return newFavorite;
    }

    @Override
    public void removeFavorite(String path) {
        try {
            Node bookmarkRoot = favoriteStore.getBookmarkRoot();
            bookmarkRoot.getNode(path).remove();
            bookmarkRoot.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public void editFavorite(String path, String title) {
        try {
            // we get the props we need from the node being edited, then delete it and re-create it anew. This to ensure that title and jcr node name are kept in "sync",
            // the latter being the title with jcr invalid characters replaced with dashes.
            Node bookmarkRoot = favoriteStore.getBookmarkRoot();
            Node staleFavorite = bookmarkRoot.getNode(path);
            Node parent = staleFavorite.getParent();
            String url = staleFavorite.getProperty(AdmincentralNodeTypes.Favorite.URL).getString();
            String icon = staleFavorite.getProperty(AdmincentralNodeTypes.Favorite.ICON).getString();
            String group = "";
            if (staleFavorite.hasProperty(AdmincentralNodeTypes.Favorite.GROUP)) {
                group = staleFavorite.getProperty(AdmincentralNodeTypes.Favorite.GROUP).getString();
            }
            staleFavorite.remove();

            Node editedFavorite = parent.addNode(Path.getValidatedLabel(title), AdmincentralNodeTypes.Favorite.NAME);
            editedFavorite.setProperty(AdmincentralNodeTypes.Favorite.TITLE, title);
            editedFavorite.setProperty(AdmincentralNodeTypes.Favorite.URL, url);
            editedFavorite.setProperty(AdmincentralNodeTypes.Favorite.ICON, icon);
            editedFavorite.setProperty(AdmincentralNodeTypes.Favorite.GROUP, group);

            bookmarkRoot.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public JcrNewNodeAdapter createFavoriteGroupSuggestion(String title) {
        Node bookmarkRoot;
        try {
            bookmarkRoot = favoriteStore.getBookmarkRoot();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

        JcrNewNodeAdapter newGroup = new JcrNewNodeAdapter(bookmarkRoot, AdmincentralNodeTypes.FavoriteGroup.NAME);
        newGroup.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", title));

        return newGroup;
    }

    @Override
    public void addGroup(JcrNewNodeAdapter newGroup) {
        try {
            final String title = (String) newGroup.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue();
            newGroup.addItemProperty(DefaultPropertyUtil.newDefaultProperty(ModelConstants.JCR_NAME, "", Path.getValidatedLabel(title)));
            newGroup.getNode().getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }


    @Override
    public void editGroup(String path, String newTitle) {
        try {
            // we get the props we need from the node being edited, then delete it and re-create it anew. This to ensure that title and jcr node name are kept in "sync",
            // the latter being the title with jcr invalid characters replaced with dashes.
            Node bookmarkRoot = favoriteStore.getBookmarkRoot();
            Node oldGroup = bookmarkRoot.getNode(path);
            NodeIterator favorites = oldGroup.getNodes();

            Node editedGroup = bookmarkRoot.addNode(Path.getValidatedLabel(newTitle), AdmincentralNodeTypes.FavoriteGroup.NAME);
            editedGroup.setProperty(AdmincentralNodeTypes.Favorite.TITLE, newTitle);
            while (favorites.hasNext()) {
                Node favorite = favorites.nextNode();
                favorite.setProperty(AdmincentralNodeTypes.Favorite.GROUP, editedGroup.getName());
                NodeUtil.moveNode(favorite, editedGroup);
            }
            oldGroup.remove();
            bookmarkRoot.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

    }

    @Override
    public void removeGroup(String path) {
        try {
            Node bookmarkRoot = favoriteStore.getBookmarkRoot();
            Node groupToBeRemoved = bookmarkRoot.getNode(path);
            // These ones will remain orphans :(
            // NodeIterator favorites = groupToBeRemoved.getNodes(AdmincentralNodeTypes.Favorite.NAME);
            groupToBeRemoved.remove();
            bookmarkRoot.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

    }
}
