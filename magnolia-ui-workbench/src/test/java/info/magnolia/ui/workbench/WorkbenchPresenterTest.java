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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.thumbnail.ThumbnailPresenterDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.TreePresenter;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;
import info.magnolia.ui.workbench.tree.TreeViewImpl;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Table;

/**
 * Tests for {@link WorkbenchPresenter}.
 */
public class WorkbenchPresenterTest extends MgnlTestCase {

    private final static String WORKSPACE = "workspace";

    private final static String ROOT_PATH = "/";

    private ComponentProvider componentProvider;

    private WorkbenchPresenter presenter;
    private TreePresenter treePresenter;

    @Override
    @Before
    public void setUp() throws Exception {
        this.componentProvider = mock(ComponentProvider.class);
        final HierarchicalJcrContainer mockContainer = mock(HierarchicalJcrContainer.class);
        doReturn(Arrays.asList("p1", "p2")).when(mockContainer).getContainerPropertyIds();
        this.treePresenter = new TreePresenter(new TreeViewImpl(), componentProvider) {

            @Override
            protected HierarchicalJcrContainer createContainer(WorkbenchDefinition workbench) {
                return mockContainer;
            }
        };

        final WorkbenchView view = new WorkbenchViewImpl(mock(SimpleTranslator.class));
        final WorkbenchStatusBarPresenter statusBarPresenter = mock(WorkbenchStatusBarPresenter.class);
        doReturn(new StatusBarViewImpl() {
            @Override
            public Component asVaadinComponent() {
                return new CssLayout();
            }
        }).when(statusBarPresenter).start(any(EventBus.class), any(WorkbenchDefinition.class));

        doReturn(treePresenter).when(componentProvider).newInstance(any(Class.class), anyVararg());

        presenter = new WorkbenchPresenter(view, componentProvider, statusBarPresenter);
        MockUtil.initMockContext();
        MockUtil.setSessionAndHierarchyManager(new MockSession(WORKSPACE));
    }

    @Test
    public void testGetDefaultViewType() {

        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setWorkspace(WORKSPACE);
        workbenchDefinition.setPath(ROOT_PATH);
        workbenchDefinition.getContentViews().add(new TreePresenterDefinition());
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());
        presenter.start(workbenchDefinition, null, null);

        // WHEN
        String viewType = presenter.getDefaultViewType();

        // THEN
        assertEquals(TreePresenterDefinition.VIEW_TYPE, viewType);
    }

    @Test
    public void testHasViewType() {

        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setWorkspace(WORKSPACE);
        workbenchDefinition.setPath(ROOT_PATH);
        workbenchDefinition.getContentViews().add(new TreePresenterDefinition());
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());
        presenter.start(workbenchDefinition, null, null);

        // WHEN
        boolean hasTreeView = presenter.hasViewType(TreePresenterDefinition.VIEW_TYPE);
        boolean hasListView = presenter.hasViewType(ListPresenterDefinition.VIEW_TYPE);
        boolean hasThumbView = presenter.hasViewType(ThumbnailPresenterDefinition.VIEW_TYPE);

        // THEN
        assertTrue(hasTreeView);
        assertTrue(hasListView);
        assertFalse(hasThumbView);
    }

    @Test
    public void testHidingColumnsForChooseDialog() {
        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setWorkspace(WORKSPACE);
        workbenchDefinition.setPath(ROOT_PATH);
        workbenchDefinition.setDialogWorkbench(true);

        final TreePresenterDefinition treePresenterDefinition = new TreePresenterDefinition();
        final PropertyColumnDefinition c1 = new PropertyColumnDefinition();
        c1.setName("c1");
        c1.setPropertyName("p1");
        c1.setDisplayInChooseDialog(true);

        final PropertyColumnDefinition c2 = new PropertyColumnDefinition();
        c2.setName("c2");
        c2.setPropertyName("p2");
        c2.setDisplayInChooseDialog(false);

        treePresenterDefinition.addColumn(c1);
        treePresenterDefinition.addColumn(c2);

        workbenchDefinition.addContentView(treePresenterDefinition);
        treePresenterDefinition.setViewType("tree");
        treePresenterDefinition.setActive(true);

        workbenchDefinition.getContentViews().add(treePresenterDefinition);

        // WHEN
        final View view = presenter.start(workbenchDefinition, null, mock(EventBus.class));
        presenter.onViewTypeChanged("tree");

        // THEN
        Table table = fetchVaadinTable(view.asVaadinComponent());
        assertTrue(Arrays.asList(table.getVisibleColumns()).contains("p1"));
        assertFalse(Arrays.asList(table.getVisibleColumns()).contains("p2"));
    }

    private Table fetchVaadinTable(Component component) {
        if (component instanceof Table) {
            return (Table) component;
        }

        if (component instanceof HasComponents) {
            final Iterator<Component> it = ((HasComponents)component).iterator();
            while (it.hasNext()) {
                Table resolvedTable = fetchVaadinTable(it.next());
                if (resolvedTable != null) {
                    return resolvedTable;
                }
            }
        }

        return null;
    }
}
