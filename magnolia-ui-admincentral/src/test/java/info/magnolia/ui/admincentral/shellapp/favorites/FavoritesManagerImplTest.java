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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import javax.jcr.Node;

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
        User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        ctx.setUser(user);
        ctx.addSession(FavoriteStore.WORKSPACE_NAME, session);
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
        JcrNewNodeAdapter newNodeAdapter = new JcrNewNodeAdapter(favoriteStore.getBookmarkRoot(), AdmincentralNodeTypes.Favorite.NAME);
        final String bookmarkTitle = "justATest";
        newNodeAdapter.addItemProperty(DefaultPropertyUtil.newDefaultProperty(AdmincentralNodeTypes.Favorite.TITLE, "", bookmarkTitle));

        // WHEN
        favoritesManager.addFavorite(newNodeAdapter);

        // THEN
        final Node newBookmarkNode = favoriteStore.getBookmarkRoot().getNode(bookmarkTitle);
        assertEquals(bookmarkTitle, newBookmarkNode.getName());
        assertEquals(bookmarkTitle, newBookmarkNode.getProperty(AdmincentralNodeTypes.Favorite.TITLE).getString());
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
}
