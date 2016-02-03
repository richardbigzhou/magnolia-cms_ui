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
import static org.hamcrest.CoreMatchers.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.favorite.FavoriteStore;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class FavoritesPresenterTest {

    public static final String TEST_USER = "MickeyMouse";
    public static final String SERVER_NAME = "localhost";
    public static final int SERVER_PORT = 8080;
    public static final String WEBAPP_CONTEXT_PATH = "/myWebApp";
    public static final String FULL_PROTOCOL = "HTTP/1.1";
    public static final String PROTOCOL = "http";
    public static final String FRAGMENT = "/.magnolia/admincentral#app:pages:;";

    public static final String WEB_APP_URL = PROTOCOL + "://" + SERVER_NAME + ":" + SERVER_PORT + WEBAPP_CONTEXT_PATH;

    private MockSession session;
    private MockWebContext ctx;
    private FavoritesPresenter presenter;

    @Before
    public void setUp() {
        session = new MockSession(FavoriteStore.WORKSPACE_NAME);
        ctx = new MockWebContext();
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

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn(FULL_PROTOCOL);
        when(request.getServerName()).thenReturn(SERVER_NAME);
        when(request.getServerPort()).thenReturn(SERVER_PORT);
        ctx.setRequest(request);

        ctx.setContextPath(WEBAPP_CONTEXT_PATH);

        MgnlContext.setInstance(ctx);

        AppDescriptorRegistry registry = mock(AppDescriptorRegistry.class);
        presenter = new FavoritesPresenter(null, null,registry);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testGetWebAppRootURI() throws Exception{
        // GIVEN

        // WHEN
        final String result = presenter.getWebAppRootURI();

        // THEN
        assertThat(result, equalTo(WEB_APP_URL));
    }

    @Test
    public void testGetCompleteURIFromFragment() throws Exception {
        // GIVEN

        // WHEN
        final String result = presenter.getCompleteURIFromFragment(FRAGMENT);

        // THEN
        assertThat("Fragment should have been completed.", result, equalTo(WEB_APP_URL + FRAGMENT));
    }

    @Test
    public void testGetCompleteURIFromFragmentWithAbsoluteURI() {
        // GIVEN
        final String completeUri = "http://www.magnolia-cms.com/magnolia-cms.html";

        // WHEN
        final String result = presenter.getCompleteURIFromFragment(completeUri);

        // THEN
        assertThat("Complete URIs should be returned unchanged.", result, equalTo(completeUri));


    }

    @Test
    public void testGetUrlFragmentFrom() throws Exception {
        // GIVEN

        // WHEN
        final String result = presenter.getUrlFragmentFromURI(new URI(WEB_APP_URL + FRAGMENT));

        // THEN
        assertThat(result, equalTo(FRAGMENT));
    }
}
