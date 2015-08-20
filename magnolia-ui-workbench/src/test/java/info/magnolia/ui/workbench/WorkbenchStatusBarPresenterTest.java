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
package info.magnolia.ui.workbench;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;


public class WorkbenchStatusBarPresenterTest {

    private final static String WORKSPACE = "workspace";
    private final static String USER = "testUser";


    private WorkbenchStatusBarPresenter barPresenter;
    private Session session;
    private SimpleEventBus eventBus;
    private ContentConnector contentConnector;
    private ContentPresenter activeContentPresenter;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(createMockUser(USER));
        MgnlContext.setInstance(ctx);

        initWorkbenchPresenter();
    }

    private User createMockUser(String name) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);
        return user;
    }

    private void initWorkbenchPresenter() throws Exception {
        SimpleTranslator i18n = mock(SimpleTranslator.class);
        contentConnector = mock(ContentConnector.class);
        StatusBarView view = mock(StatusBarViewImpl.class);
        activeContentPresenter = mock(ContentPresenter.class);

        eventBus = new SimpleEventBus();
        barPresenter = new WorkbenchStatusBarPresenter(view, contentConnector, i18n);

        barPresenter.start(eventBus, activeContentPresenter);
    }

    @Test(expected = Test.None.class)
    public void deletedItemSelectionDoesNotThrowException() {
        // GIVEN
        given(activeContentPresenter.getSelectedItemIds()).willReturn(Lists.<Object>newArrayList("/b"));
        given(contentConnector.canHandleItem("/b")).willReturn(false);
        given(contentConnector.getItemUrlFragment("/b")).willThrow(Exception.class);

        // WHEN
        barPresenter.refresh();
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }
}