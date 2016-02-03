/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WorkbenchPresenter}.
 */
public class WorkbenchPresenterTest extends MgnlTestCase {

    private final static String WORKSPACE = "workspace";

    private final static String ROOT_PATH = "/";

    private ComponentProvider componentProvider;

    private WorkbenchPresenter presenter;

    @Override
    @Before
    public void setUp() throws Exception {
        WorkbenchView view = mock(WorkbenchView.class);
        WorkbenchStatusBarPresenter statusBarPresenter = mock(WorkbenchStatusBarPresenter.class);

        componentProvider = mock(ComponentProvider.class);
        doReturn(mock(ContentPresenter.class)).when(componentProvider).newInstance(any(Class.class), anyVararg());

        presenter = new WorkbenchPresenter(view, componentProvider, statusBarPresenter);
    }

    @Test
    public void testGetDefaultViewType() {

        MockUtil.initMockContext();
        MockUtil.setSessionAndHierarchyManager(new MockSession(WORKSPACE));


        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setWorkspace(WORKSPACE);
        workbenchDefinition.setPath(ROOT_PATH);
        workbenchDefinition.getContentViews().add(new TreePresenterDefinition());
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());
        presenter.start(workbenchDefinition, null, null);

        // WHEN
        ContentView.ViewType viewType = presenter.getDefaultViewType();

        // THEN
        assertEquals(ContentView.ViewType.TREE, viewType);
    }
}
