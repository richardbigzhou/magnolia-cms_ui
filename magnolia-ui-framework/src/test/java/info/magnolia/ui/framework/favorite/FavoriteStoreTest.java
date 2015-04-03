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
package info.magnolia.ui.framework.favorite;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class FavoriteStoreTest extends MgnlTestCase {
    public static final String TEST_USER = "phantomas";

    private FavoriteStore store;
    private Session session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = new MockSession(FavoriteStore.WORKSPACE_NAME);
        MockUtil.getSystemMockContext().addSession(FavoriteStore.WORKSPACE_NAME, session);
        final User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        final SecuritySupportImpl sec = new SecuritySupportImpl();

        MgnlUserManager userMgr = new MgnlUserManager() {
            {
                setRealmName(Realm.REALM_SYSTEM.getName());
            }
            @Override
            public User getSystemUser() {
                return user;
            }
        };

        sec.addUserManager(Realm.REALM_SYSTEM.getName(), userMgr);
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        MockUtil.getMockContext().setUser(user);

        store = new FavoriteStore();
    }

    @Test
    public void testGetFavoriteRoot() throws Exception {
        // GIVEN
        session.getRootNode().addNode(TEST_USER).addNode(FavoriteStore.FAVORITES_PATH);
        session.save();

        // WHEN
        final Node favoriteRoot = store.getFavoriteRoot();

        // THEN
        assertEquals("/phantomas/favorites", favoriteRoot.getPath());
    }

    @Test
    public void testGetFavoriteRootWhenNotYetExisting() throws Exception {
        // GIVEN

        // WHEN
        final Node favoriteRoot = store.getFavoriteRoot();

        // THEN
        assertEquals("/phantomas/favorites", favoriteRoot.getPath());
    }


    @Test
    public void testGetBookmarkRootWhenNotYetExisting() throws Exception {
        // GIVEN

        // WHEN
        final Node bookmarkRoot = store.getBookmarkRoot();

        // THEN
        assertEquals("/phantomas/favorites/bookmarks", bookmarkRoot.getPath());
    }

}
