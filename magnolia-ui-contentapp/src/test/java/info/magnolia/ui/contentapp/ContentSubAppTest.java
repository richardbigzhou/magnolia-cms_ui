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
package info.magnolia.ui.contentapp;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.contentapp.browser.BrowserPresenter;
import info.magnolia.ui.contentapp.browser.BrowserSubApp;

import javax.jcr.Item;

import org.junit.Before;

/**
 * ContentSubAppTest.
 */
public class ContentSubAppTest {

    private static final String DUMMY_APPNAME = "dummy";
    private static final String path = "/foo/bar";
    private static final String query = "qux*";

    private AppContext appContext;
    private ContentSubAppView view;
    private BrowserPresenter workbench;
    private EventBus subAppEventBus;
    private DummyContentSubApp subApp;
    private SubAppContext subAppContext;
    private ComponentProvider componentProvider;

    @Before
    public void setUp() throws Exception {
        appContext = mock(AppContext.class);
        subAppContext = mock(SubAppContext.class);

        componentProvider = mock(ComponentProvider.class);
        doReturn(mock(DummyRule.class)).when(componentProvider).newInstance(any(Class.class), anyVararg());

        view = mock(ContentSubAppView.class);
        workbench = mock(BrowserPresenter.class);

        ActionbarPresenter actionbar = new ActionbarPresenter();
        when(workbench.getActionbarPresenter()).thenReturn(actionbar);

        subAppEventBus = mock(EventBus.class);
        this.subApp = new DummyContentSubApp(null, subAppContext, view, workbench, subAppEventBus);
    }

    private class DummyContentSubApp extends BrowserSubApp {
        public int foo = 0;

        public DummyContentSubApp(ActionExecutor actionExecutor, SubAppContext subAppContext, ContentSubAppView view, BrowserPresenter workbench, EventBus subAppEventBus) {
            super(actionExecutor, subAppContext, view, workbench, subAppEventBus, componentProvider);
        }

        @Override
        public void updateActionbar(ActionbarPresenter actionbar) {

        }

        @Override
        public String getCaption() {
            return DUMMY_APPNAME;
        }

        @Override
        protected void onSubAppStart() {
            foo++;
        }
    }

    private class DummyRule extends AbstractAvailabilityRule {
        @Override
        public boolean isAvailableForItem(Item item) {
            return true;
        }
    }
}
