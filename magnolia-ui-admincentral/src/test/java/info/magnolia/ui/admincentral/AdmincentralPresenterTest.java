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
package info.magnolia.ui.admincentral;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.admincentral.shellapp.ShellAppController;
import info.magnolia.ui.admincentral.usermenu.UserMenuPresenter;
import info.magnolia.ui.admincentral.usermenu.UserMenuView;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.api.view.Viewport;
import info.magnolia.ui.framework.event.LoginEvent;
import info.magnolia.ui.framework.event.LogoutEvent;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ShellImpl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * This test start and stop Login/Logout events
 */
public class AdmincentralPresenterTest extends MgnlTestCase {

    private EventBus eventBus;
    private ShellImpl shell;
    private AdmincentralPresenter admincentralPresenter;
    private UserMenuPresenter userMenu;
    private ComponentProvider componentProvider;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockWebContext context = new MockWebContext();
        User user = mock(User.class);
        when(user.getName()).thenReturn("testuser");
        context.setUser(user);
        MgnlContext.setInstance(context);

        this.eventBus = mock(EventBus.class);
        this.shell = mock(ShellImpl.class);
        this.userMenu = mock(UserMenuPresenter.class);
        this.componentProvider = mock(ComponentProvider.class);

        when(componentProvider.newInstance(LoginEvent.class, MgnlContext.getUser().getName(), mock(VaadinSession.class), componentProvider)).thenReturn(mock(LoginEvent.class));
        when(shell.getShellAppViewport()).thenReturn(mock(Viewport.class));
        when(userMenu.start()).thenReturn(mock(UserMenuView.class));

        this.admincentralPresenter = new AdmincentralPresenter(shell, mock(EventBus.class), eventBus, mock(AppLauncherLayoutManager.class), mock(LocationController.class),
                mock(AppController.class), mock(ShellAppController.class), mock(MessagesManager.class), userMenu, mock(ComponentProvider.class));
    }

    @Test
    public void testStartLoginEvent() throws Exception {
        // GIVEN
        initializeVaadinUI();

        // WHEN
        admincentralPresenter.start();

        // THEN
        verify(eventBus, times(1)).fireEvent(any(LoginEvent.class));
    }

    @Test
    public void testStopLogoutEvent() throws Exception {
        // GIVEN
        initializeVaadinUI();

        // WHEN
        admincentralPresenter.stop();

        // THEN
        verify(eventBus, times(1)).fireEvent(any(LogoutEvent.class));
    }

    private void initializeVaadinUI(final URI returnURI) {
        UI.setCurrent(new UI() {
            @Override
            protected void init(VaadinRequest request) {
            }

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }

            @Override
            public Page getPage() {
                Page page = mock(Page.class);
                doReturn(returnURI).when(page).getLocation();
                return page;
            }
        });
    }

    private void initializeVaadinUI() throws URISyntaxException {
        initializeVaadinUI(new URI("http://localhost:8080/myWebApp/.magnolia/admincentral#shell:applauncher:;"));
    }
}
