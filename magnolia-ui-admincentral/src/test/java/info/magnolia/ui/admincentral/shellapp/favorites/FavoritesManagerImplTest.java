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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class FavoritesManagerImplTest extends RepositoryTestCase {

    public static final String TEST_USER = "MickeyMouse";

    private static final String FAVORITE_NODE_TYPES = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<nodeTypes xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\" >"
            + "<nodeType name=\"mgnl:favorite\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">"
            + "  <supertypes><supertype>nt:base</supertype></supertypes>"
            + "  <propertyDefinition name=\"*\" requiredType=\"undefined\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" multiple=\"false\"/>"
            + "  <propertyDefinition name=\"*\" requiredType=\"undefined\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" multiple=\"true\"/>"
            + "</nodeType>"
            + "<nodeType name=\"mgnl:favoriteGroup\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\">"
            + "  <supertypes><supertype>mgnl:folder</supertype></supertypes>"
            + "</nodeType>"
            + "</nodeTypes>";

    private Session session;
    private FavoritesManagerImpl favoritesManager;
    private FavoriteStore favoriteStore;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        repositoryManager.loadWorkspace("magnolia", FavoriteStore.WORKSPACE_NAME);
        repositoryManager.getRepositoryProvider("magnolia").registerNodeTypes(new ByteArrayInputStream(FAVORITE_NODE_TYPES.getBytes()));
        session = MgnlContext.getJCRSession(FavoriteStore.WORKSPACE_NAME);

        MockContext ctx = new MockContext();
        final User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        ctx.setUser(user);
        ctx.addSession("magnolia-" + RepositoryConstants.VERSION_STORE, MgnlContext.getJCRSession("magnolia-" + RepositoryConstants.VERSION_STORE));
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

    @Override
    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
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

    @Test
    public void testMoveFavoriteToGroup() throws Exception {
        // GIVEN
        final String fromGroupName = "fromGroup";
        final String toGroupName = "toGroup";
        final String favoriteName = "testFavorite";

        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(fromGroupName);
        favoritesManager.addGroup(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(toGroupName);
        favoritesManager.addGroup(newNodeAdapter);

        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/bar", favoriteName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(fromGroupName);
        favoritesManager.addFavorite(newNodeAdapter);

        // WHEN
        favoritesManager.moveFavorite(fromGroupName + "/" + favoriteName, toGroupName);

        // THEN
        assertFalse(favoriteStore.getBookmarkRoot().getNode(fromGroupName).hasNode(favoriteName));
        assertTrue(favoriteStore.getBookmarkRoot().getNode(toGroupName).hasNode(favoriteName));
    }

    @Test
    public void testMoveFavoriteToNoGroup() throws Exception {
        // GIVEN
        final String fromGroupName = "fromGroup";
        final String toGroupName = null;
        final String favoriteName = "testFavorite";

        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(fromGroupName);
        favoritesManager.addGroup(newNodeAdapter);

        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/bar", favoriteName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(fromGroupName);
        favoritesManager.addFavorite(newNodeAdapter);

        // WHEN
        favoritesManager.moveFavorite(fromGroupName + "/" + favoriteName, toGroupName);

        // THEN
        assertFalse(favoriteStore.getBookmarkRoot().getNode(fromGroupName).hasNode(favoriteName));
        assertTrue(favoriteStore.getBookmarkRoot().hasNode(favoriteName));

    }

    @Test
    public void testOrderFavoriteBefore() throws Exception {
        // GIVEN
        final String groupName = "testGroup";
        final String firstNodeName = "first";
        final String secondNodeName = "second";
        final String thirdNodeName = "third";

        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(groupName);
        favoritesManager.addGroup(newNodeAdapter);

        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/first", firstNodeName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(groupName);
        favoritesManager.addFavorite(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/second", secondNodeName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(groupName);
        favoritesManager.addFavorite(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/third", thirdNodeName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(groupName);
        favoritesManager.addFavorite(newNodeAdapter);

        // WHEN
        favoritesManager.orderFavoriteBefore(groupName + "/" + thirdNodeName, firstNodeName);

        // THEN
        Iterator<Node> favorites = NodeUtil.getNodes(favoriteStore.getBookmarkRoot().getNode(groupName)).iterator();
        assertEquals(thirdNodeName, favorites.next().getName());
        assertEquals(firstNodeName, favorites.next().getName());
        assertEquals(secondNodeName, favorites.next().getName());
    }

    @Test
    public void testOrderFavoriteAfter() throws Exception {
        // GIVEN
        final String groupName = "testGroup";
        final String firstNodeName = "first";
        final String secondNodeName = "second";
        final String thirdNodeName = "third";

        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(groupName);
        favoritesManager.addGroup(newNodeAdapter);

        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/first", firstNodeName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(groupName);
        favoritesManager.addFavorite(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/second", secondNodeName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(groupName);
        favoritesManager.addFavorite(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteSuggestion("/foo/third", thirdNodeName, "");
        newNodeAdapter.getItemProperty(AdmincentralNodeTypes.Favorite.GROUP).setValue(groupName);
        favoritesManager.addFavorite(newNodeAdapter);

        // WHEN
        favoritesManager.orderFavoriteAfter(groupName + "/" + firstNodeName, thirdNodeName);

        // THEN
        Iterator<Node> favorites = NodeUtil.getNodes(favoriteStore.getBookmarkRoot().getNode(groupName)).iterator();
        assertEquals(secondNodeName, favorites.next().getName());
        assertEquals(thirdNodeName, favorites.next().getName());
        assertEquals(firstNodeName, favorites.next().getName());
    }

    @Test
    public void testOrderGroupBefore() throws Exception {
        // GIVEN
        final String firstNodeName = "first";
        final String secondNodeName = "second";
        final String thirdNodeName = "third";

        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(firstNodeName);
        favoritesManager.addGroup(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(secondNodeName);
        favoritesManager.addGroup(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(thirdNodeName);
        favoritesManager.addGroup(newNodeAdapter);

        // WHEN
        favoritesManager.orderGroupBefore(thirdNodeName, firstNodeName);

        // THEN
        Iterator<Node> favorites = NodeUtil.getNodes(favoriteStore.getBookmarkRoot()).iterator();
        assertEquals(thirdNodeName, favorites.next().getName());
        assertEquals(firstNodeName, favorites.next().getName());
        assertEquals(secondNodeName, favorites.next().getName());
    }

    @Test
    public void testOrderGroupAfter() throws Exception {
        // GIVEN
        final String firstNodeName = "first";
        final String secondNodeName = "second";
        final String thirdNodeName = "third";

        JcrNewNodeAdapter newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(firstNodeName);
        favoritesManager.addGroup(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(secondNodeName);
        favoritesManager.addGroup(newNodeAdapter);
        newNodeAdapter = favoritesManager.createFavoriteGroupSuggestion(thirdNodeName);
        favoritesManager.addGroup(newNodeAdapter);

        // WHEN
        favoritesManager.orderGroupAfter(firstNodeName, thirdNodeName);

        // THEN
        Iterator<Node> favorites = NodeUtil.getNodes(favoriteStore.getBookmarkRoot()).iterator();
        assertEquals(secondNodeName, favorites.next().getName());
        assertEquals(thirdNodeName, favorites.next().getName());
        assertEquals(firstNodeName, favorites.next().getName());
    }
}
