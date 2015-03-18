/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.framework.event;

import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;

/**
 * Tests for {@link LoginEvent}.
 */
public class LoginEventTest extends MgnlTestCase {

    private VaadinSession vaadinSession;
    private LoginEvent loginEvent;
    private LoginEventHandler loginEventHandler;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        MockContext context = new MockContext();
        User user = mock(User.class);
        when(user.getName()).thenReturn("testuser");
        context.setUser(user);

        MgnlContext.setInstance(context);
        VaadinService vaadinService = mock(VaadinService.class);
        vaadinSession = spy(new VaadinSession(vaadinService));
    }

    @Test
    public void testDispatchOnLogin() throws Exception {

        // GIVEN
        loginEventHandler = mock(LoginEventHandler.class);
        loginEvent = new LoginEvent(MgnlContext.getUser().getName(), vaadinSession, mock(ComponentProvider.class));

        // WHEN
        loginEvent.dispatch(loginEventHandler);

        // THEN
        verify(loginEventHandler).onLogin(loginEvent);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

}
