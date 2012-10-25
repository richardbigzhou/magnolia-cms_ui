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
package info.magnolia.ui.admincentral.app.content;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AbstractContentSubAppTest.
 */
public class AbstractContentSubAppTest {

    private static final String DUMMY_APPNAME = "dummy";
    private static final String path = "/foo/bar";
    private static final String query = "qux*";

    private AppContext appContext;
    private ContentAppView view;
    private ContentWorkbenchPresenter workbench;
    private EventBus subAppEventBus;
    private DummyContentSubApp subApp;

    @Before
    public void setUp() throws Exception {
        appContext = mock(AppContext.class);
        view = mock(ContentAppView.class);
        workbench = mock(ContentWorkbenchPresenter.class);

        ActionbarPresenter actionbar = new ActionbarPresenter(subAppEventBus, appContext);
        when(workbench.getActionbarPresenter()).thenReturn(actionbar);

        subAppEventBus = mock(EventBus.class);
        this.subApp = new DummyContentSubApp(appContext,view, workbench, subAppEventBus);
    }

    private class DummyContentSubApp extends AbstractContentSubApp {
        public int foo = 0;

        public DummyContentSubApp(AppContext appContext, ContentAppView view, ContentWorkbenchPresenter workbench, EventBus subAppEventBus) {
            super(appContext, view, workbench, subAppEventBus);
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


}
