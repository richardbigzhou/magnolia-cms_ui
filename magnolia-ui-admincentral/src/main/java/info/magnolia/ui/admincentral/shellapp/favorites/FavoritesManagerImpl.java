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

import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.JCRSessionOp;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of a Manager for Favorites.
 */
public final class FavoritesManagerImpl implements FavoritesManager {
    private FavoriteStore favoriteStore;

    @Inject
    public FavoritesManagerImpl(final FavoriteStore favoriteStore) {
        this.favoriteStore = favoriteStore;
    }

    @Override
    public AbstractJcrNodeAdapter getFavorites() {
        try {
            Node bookmarksNode = favoriteStore.getBookmarkRoot();
            JcrNodeAdapter favorites = new JcrNodeAdapter(bookmarksNode);

            Iterable<Node> bookmarks = NodeUtil.getNodes(bookmarksNode);
            JcrNodeAdapter currentChild;
            for (Node bookmark : bookmarks) {
                currentChild = new JcrNodeAdapter(bookmark);
                currentChild.addItemProperty(AdmincentralNodeTypes.Favorite.TITLE, DefaultPropertyUtil.newDefaultProperty("", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.TITLE)));

                final String bookmarkNodeType = bookmark.getPrimaryNodeType().getName();

                if (AdmincentralNodeTypes.Favorite.NAME.equals(bookmarkNodeType)) {
                    currentChild.addItemProperty(AdmincentralNodeTypes.Favorite.URL, DefaultPropertyUtil.newDefaultProperty("", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.URL)));
                    currentChild.addItemProperty(AdmincentralNodeTypes.Favorite.ICON, DefaultPropertyUtil.newDefaultProperty("", PropertyUtil.getString(bookmark, AdmincentralNodeTypes.Favorite.ICON)));
                } else if (AdmincentralNodeTypes.FavoriteGroup.NAME.equals(bookmarkNodeType)) {
                    Iterable<Node> bookmarksWithGroup = NodeUtil.getNodes(bookmark, AdmincentralNodeTypes.Favorite.NAME);
                    JcrNodeAdapter favoriteChild;
                    for (Node bookmarkWithGroup : bookmarksWithGroup) {
                        favoriteChild = new JcrNodeAdapter(bookmarkWithGroup);
                        favoriteChild.addItemProperty(AdmincentralNodeTypes.Favorite.TITLE, DefaultPropertyUtil.newDefaultProperty("", PropertyUtil.getString(bookmarkWithGroup, AdmincentralNodeTypes.Favorite.TITLE)));
                        favoriteChild.addItemProperty(AdmincentralNodeTypes.Favorite.URL, DefaultPropertyUtil.newDefaultProperty("", PropertyUtil.getString(bookmarkWithGroup, AdmincentralNodeTypes.Favorite.URL)));
                        favoriteChild.addItemProperty(AdmincentralNodeTypes.Favorite.ICON, DefaultPropertyUtil.newDefaultProperty("", PropertyUtil.getString(bookmarkWithGroup, AdmincentralNodeTypes.Favorite.ICON)));
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
        Map<String, String> groupNames = new TreeMap<String, String>();
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
    public void addFavorite(final JcrNewNodeAdapter favorite) {
        try {
            final String title = (String) favorite.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue();
            favorite.addItemProperty(ModelConstants.JCR_NAME, DefaultPropertyUtil.newDefaultProperty("", Path.getValidatedLabel(title)));
            final Node newFavorite = MgnlContext.doInSystemContext(new JCRSessionOp<Node>(FavoriteStore.WORKSPACE_NAME) {
                @Override
                public Node exec(Session session) throws RepositoryException {
                    return favorite.applyChanges();
                }
            });

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
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.TITLE, DefaultPropertyUtil.newDefaultProperty("", title));
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.URL, DefaultPropertyUtil.newDefaultProperty("", location));
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.GROUP, DefaultPropertyUtil.newDefaultProperty("", ""));
        newFavorite.addItemProperty(AdmincentralNodeTypes.Favorite.ICON, DefaultPropertyUtil.newDefaultProperty("", StringUtils.defaultIfEmpty(icon, "icon-app")));
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
        newGroup.addItemProperty(AdmincentralNodeTypes.Favorite.TITLE, DefaultPropertyUtil.newDefaultProperty("", title));

        return newGroup;
    }

    @Override
    public void addGroup(final JcrNewNodeAdapter newGroup) {
        MgnlContext.doInSystemContext(new MgnlContext.VoidOp() {

            @Override
            public void doExec() {
                final String title = (String) newGroup.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue();
                newGroup.addItemProperty(ModelConstants.JCR_NAME, DefaultPropertyUtil.newDefaultProperty("", Path.getValidatedLabel(title)));
                try {
                    newGroup.applyChanges().getSession().save();
                } catch (RepositoryException e) {
                    throw new RuntimeRepositoryException(e);
                }
            }
        });

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

    @Override
    public void moveFavorite(String relPath, String group) {
        try {
            Node favorite = favoriteStore.getBookmarkRoot().getNode(relPath);
            Node newGroup = null;
            if (StringUtils.isNotEmpty(group)) {
                newGroup = favoriteStore.getBookmarkRoot().getNode(group);
            } else {
                newGroup = favoriteStore.getBookmarkRoot();
            }
            NodeUtil.moveNode(favorite, newGroup);
            newGroup.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public void orderFavoriteBefore(String relPath, String sibling) {
        try {
            Node favoriteToMove = favoriteStore.getBookmarkRoot().getNode(relPath);
            NodeUtil.orderBefore(favoriteToMove, sibling);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public void orderFavoriteAfter(String relPath, String sibling) {
        try {
            Node favoriteToMove = favoriteStore.getBookmarkRoot().getNode(relPath);
            NodeUtil.orderAfter(favoriteToMove, sibling);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public void orderGroupBefore(String relPath, String sibling) {
        try {
            Node groupToMove = favoriteStore.getBookmarkRoot().getNode(relPath);
            NodeUtil.orderBefore(groupToMove, sibling);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public void orderGroupAfter(String relPath, String sibling) {
        try {
            Node groupToMove = favoriteStore.getBookmarkRoot().getNode(relPath);
            NodeUtil.orderAfter(groupToMove, sibling);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }

    }
}
