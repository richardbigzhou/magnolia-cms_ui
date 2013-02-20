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

import static org.mockito.Mockito.*;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.event.EventBus;

import org.junit.Before;

/**
 * ContentSubAppTest.
 */
public class ContentSubAppTest {

    private static final String DUMMY_APPNAME = "dummy";
    private static final String path = "/foo/bar";
    private static final String query = "qux*";

    private AppContext appContext;
    private WorkbenchSubAppView view;
    private ContentWorkbenchPresenter workbench;
    private EventBus subAppEventBus;
    private DummyContentSubApp subApp;
    private SubAppContext subAppContext;

    @Before
    public void setUp() throws Exception {
        appContext = mock(AppContext.class);
        subAppContext = mock(SubAppContext.class);

        view = mock(WorkbenchSubAppView.class);
        workbench = mock(ContentWorkbenchPresenter.class);

        ActionbarPresenter actionbar = new ActionbarPresenter(subAppEventBus, appContext);
        when(workbench.getActionbarPresenter()).thenReturn(actionbar);

        subAppEventBus = mock(EventBus.class);
        this.subApp = new DummyContentSubApp(subAppContext, view, workbench, subAppEventBus);
    }

    private class DummyContentSubApp extends ContentSubApp {
        public int foo = 0;

        public DummyContentSubApp(SubAppContext subAppContext, WorkbenchSubAppView view, ContentWorkbenchPresenter workbench, EventBus subAppEventBus) {
            super(subAppContext, view, workbench, subAppEventBus);
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

}
