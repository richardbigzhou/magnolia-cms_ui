/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class FavoritesManagerImplTest {

    public static final String TEST_USER = "MickeyMouse";
    private MockSession session;
    private FavoritesManagerImpl favoritesManager;
    private FavoriteStore favoriteStore;

    @Before
    public void setUp() {
        session = new MockSession(FavoriteStore.WORKSPACE_NAME);
        MockContext ctx = new MockContext();
        final User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        ctx.setUser(user);
        ctx.addSession(FavoriteStore.WORKSPACE_NAME, session);

        final SecuritySupportImpl sec = new SecuritySupportImpl();

        MgnlUserManager userMgr = new MgnlUserManager() {
            {
                setName(Realm.REALM_SYSTEM.getName());
            }
            @Override
            public User getSystemUser() {
                return user;
            }
        };
        sec.addUserManager(Realm.REALM_SYSTEM.getName(), userMgr);
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        ComponentsTestUtil.setInstance(SystemContext.class, ctx);

        MgnlContext.setInstance(ctx);

        favoriteStore = new FavoriteStore();
        favoritesManager = new FavoritesManagerImpl(favoriteStore);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testAddFavorite() throws Exception {
        // GIVEN
        final String bookmarkTitle = "justATest";
        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/bar", bookmarkTitle, "");

        // WHEN
        favoritesManager.addFavorite(newNodeAdapter);

        // THEN
        final Node newBookmarkNode = favoriteStore.getBookmarkRoot().getNode(bookmarkTitle);
        assertEquals(bookmarkTitle, newBookmarkNode.getName());
        assertEquals(bookmarkTitle, newBookmarkNode.getProperty(AdmincentralNodeTypes.Favorite.TITLE).getString());
    }

    @Test
    public void testEditFavorite() throws Exception {
        // GIVEN
        final String initialTitle = "justATest";
        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/bar", initialTitle, "");
        favoritesManager.addFavorite(newNodeAdapter);

        // WHEN
        final String newTitle = "a new title";
        favoritesManager.editFavorite(initialTitle, newTitle);

        // THEN
        Node bookmarkNode = favoriteStore.getBookmarkRoot().getNode("a-new-title");
        assertEquals("a-new-title", bookmarkNode.getName());
        assertEquals(newTitle, bookmarkNode.getProperty(AdmincentralNodeTypes.Favorite.TITLE).getString());
    }

    @Test(expected = PathNotFoundException.class)
    public void testRemoveFavorite() throws Exception {
        // GIVEN
        final String title = "justATest";
        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/bar", title, "");
        favoritesManager.addFavorite(newNodeAdapter);

        // WHEN
        favoritesManager.removeFavorite(title);

        // THEN
        favoriteStore.getBookmarkRoot().getNode(title);
    }

    @Test
    public void testAddGroup() throws Exception {
        // GIVEN
        final String groupTitle = "justATest";
        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(groupTitle);

        // WHEN
        favoritesManager.addGroup(newNodeAdapter);

        // THEN
        final Node newGroupNode = favoriteStore.getBookmarkRoot().getNode(groupTitle);
        assertEquals(groupTitle, newGroupNode.getName());
        assertEquals(groupTitle, newGroupNode.getProperty(AdmincentralNodeTypes.Favorite.TITLE).getString());
    }

    @Test
    public void testEditGroup() throws Exception {
        // GIVEN
        final String groupTitle = "justATest";
        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(groupTitle);
        favoritesManager.addGroup(newNodeAdapter);

        // WHEN
        final String newTitle = "a new title";
        favoritesManager.editGroup(groupTitle, newTitle);

        // THEN
        final Node groupNode = favoriteStore.getBookmarkRoot().getNode("a-new-title");
        assertEquals("a-new-title", groupNode.getName());
        assertEquals(newTitle, groupNode.getProperty(AdmincentralNodeTypes.Favorite.TITLE).getString());
    }

    @Test(expected = PathNotFoundException.class)
    public void testRemoveGroup() throws Exception {
        // GIVEN
        final String groupTitle = "justATest";
        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(groupTitle);
        favoritesManager.addGroup(newNodeAdapter);

        // WHEN
        favoritesManager.removeGroup(groupTitle);

        // THEN
        favoriteStore.getBookmarkRoot().getNode(groupTitle);
    }

    @Test
    public void testCreateFavoriteSuggestion() throws Exception {
        // GIVEN
        final String location = "/somewhere/over/the/rainbow";
        final String title = "Mr. LoverLover";
        final String icon = "this-is-my-icon-name";

        // WHEN
        JcrNewNodeAdapter suggestion = favoritesManager.createFavoriteSuggestion(location, title, icon);

        // THEN
        assertEquals(title, suggestion.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue());
        assertEquals(location, suggestion.getItemProperty(AdmincentralNodeTypes.Favorite.URL).getValue());
        assertEquals(icon, suggestion.getItemProperty(AdmincentralNodeTypes.Favorite.ICON).getValue());
    }

    @Test
    public void testCreateFavoriteSuggestionWithDefaultIcon() throws Exception {
        // GIVEN
        final String location = "/somewhere/over/the/rainbow";
        final String title = "Mr. LoverLover";

        // WHEN
        JcrNewNodeAdapter suggestion = favoritesManager.createFavoriteSuggestion(location, title, null);

        // THEN
        assertEquals(title, suggestion.getItemProperty(AdmincentralNodeTypes.Favorite.TITLE).getValue());
        assertEquals(location, suggestion.getItemProperty(AdmincentralNodeTypes.Favorite.URL).getValue());
        assertEquals("icon-app", suggestion.getItemProperty(AdmincentralNodeTypes.Favorite.ICON).getValue());
    }

    @Test
    public void testGetGroupsNamesReturnsSortedMap() throws Exception {
        // GIVEN
        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion("zzz");
        favoritesManager.addGroup(newNodeAdapter);

        newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion("Aaa");
        favoritesManager.addGroup(newNodeAdapter);

        newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion("hhh");
        favoritesManager.addGroup(newNodeAdapter);

        // WHEN
        Map<String, String> groups = favoritesManager.getGroupsNames();
        String[] values = groups.values().toArray(new String[] {});

        // THEN
        assertEquals("Aaa", values[0]);
        assertEquals("hhh", values[1]);
        assertEquals("zzz", values[2]);
    }
}
