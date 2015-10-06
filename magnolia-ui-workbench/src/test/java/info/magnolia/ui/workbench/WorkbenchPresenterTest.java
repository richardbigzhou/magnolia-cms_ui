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
package info.magnolia.ui.workbench;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.grid.MagnoliaTreeTable;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.workbench.contenttool.search.SearchContentToolPresenter;
import info.magnolia.ui.workbench.contenttool.search.SearchContentToolView;
import info.magnolia.ui.workbench.contenttool.search.SearchContentToolViewImpl;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.thumbnail.ThumbnailPresenterDefinition;
import info.magnolia.ui.workbench.tree.TreePresenter;
import info.magnolia.ui.workbench.tree.TreePresenterDefinition;
import info.magnolia.ui.workbench.tree.TreeView;
import info.magnolia.ui.workbench.tree.TreeViewImpl;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
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
        this.treePresenter = new TreePresenter(new TreeViewImpl(), componentProvider) {
            @Override
            protected Container initializeContainer() {
                IndexedContainer container = new IndexedContainer();
                container.addContainerProperty("p1", String.class, "");
                container.addContainerProperty("p2", String.class, "");
                return container;
            }
        };

        final WorkbenchView view = new WorkbenchViewImpl();
        final WorkbenchStatusBarPresenter statusBarPresenter = mock(WorkbenchStatusBarPresenter.class);
        doReturn(new StatusBarViewImpl() {
            @Override
            public Component asVaadinComponent() {
                return new CssLayout();
            }
        }).when(statusBarPresenter).start(any(EventBus.class), any(ContentPresenter.class));

        SimpleTranslator translator = mock(SimpleTranslator.class);
        SearchContentToolView searchView = new SearchContentToolViewImpl(translator);
        SearchContentToolPresenter searchPresenter = mock(SearchContentToolPresenter.class);

        when(searchPresenter.start()).thenReturn(searchView);

        when(componentProvider.newInstance(any(Class.class), anyVararg())).thenReturn(treePresenter);
        when(componentProvider.newInstance(eq(SearchContentToolPresenter.class), anyVararg())).thenReturn(searchPresenter);

        JcrContentConnector contentConnector = mock(JcrContentConnector.class);
        ConfiguredJcrContentConnectorDefinition connectorDefinition = new ConfiguredJcrContentConnectorDefinition();
        connectorDefinition.setWorkspace(WORKSPACE);
        connectorDefinition.setRootPath(ROOT_PATH);
        doReturn(connectorDefinition).when(contentConnector).getContentConnectorDefinition();
        doReturn(new Object()).when(contentConnector).getDefaultItemId();

        presenter = new WorkbenchPresenter(view, componentProvider, statusBarPresenter, contentConnector);
        MockUtil.initMockContext();
        MockUtil.setSessionAndHierarchyManager(new MockSession(WORKSPACE));
    }

    @Test
    public void testGetDefaultViewType() {
        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.getContentViews().add(new TreePresenterDefinition());
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());
        presenter.start(workbenchDefinition, mock(ImageProviderDefinition.class), mock(EventBus.class));

        // WHEN
        String viewType = presenter.getDefaultViewType();

        // THEN
        assertEquals(TreePresenterDefinition.VIEW_TYPE, viewType);
    }

    /**
     * Tests whether #getDefaultViewType also works without a TreePresenterDefinition (which is "active" per default)
     * and whether workbenchDefinition.getContentViews() doesn't automatically adds the tree view anymore.
     */
    @Test
    public void testGetDefaultViewTypeWithoutTree() {
        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());
        workbenchDefinition.getContentViews().add(new ThumbnailPresenterDefinition());
        presenter.start(workbenchDefinition, mock(ImageProviderDefinition.class), mock(EventBus.class));

        // WHEN
        String defaultViewType = presenter.getDefaultViewType();
        boolean hasTreeDefined = presenter.hasViewType(TreePresenterDefinition.VIEW_TYPE);

        // THEN
        assertThat(defaultViewType, is(ListPresenterDefinition.VIEW_TYPE));
        assertThat(hasTreeDefined , not(true));
    }

    @Test
    public void testHasViewType() {

        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.getContentViews().add(new TreePresenterDefinition());
        workbenchDefinition.getContentViews().add(new ListPresenterDefinition());
        presenter.start(workbenchDefinition, null, mock(EventBus.class));

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
        final View view = presenter.start(workbenchDefinition, mock(ImageProviderDefinition.class), mock(EventBus.class));
        presenter.onViewTypeChanged("tree");

        // THEN
        Table table = fetchVaadinTable(view.asVaadinComponent());
        assertTrue(Arrays.asList(table.getVisibleColumns()).contains("p1"));
        assertFalse(Arrays.asList(table.getVisibleColumns()).contains("p2"));
    }

    /**
     * Workbenches in Dialogs should not provide a UI for multiple selection..
     */
    @Test
    public void testDialogWorkbenchDissallowsMultipleSelection() {
        // GIVEN
        ConfiguredWorkbenchDefinition workbenchDefinition = new ConfiguredWorkbenchDefinition();
        workbenchDefinition.setDialogWorkbench(true);

        final TreePresenterDefinition treePresenterDefinition = new TreePresenterDefinition();

        workbenchDefinition.addContentView(treePresenterDefinition);
        treePresenterDefinition.setViewType("tree");
        treePresenterDefinition.setActive(true);
        workbenchDefinition.getContentViews().add(treePresenterDefinition);

        // WHEN
        final WorkbenchView view = presenter.start(workbenchDefinition, mock(ImageProviderDefinition.class), mock(EventBus.class));
        presenter.onViewTypeChanged("tree");

        // THEN
        TreeView treeView = (TreeView) view.getSelectedView();
        boolean isMultiSelect = ((MagnoliaTreeTable) treeView.asVaadinComponent()).isMultiSelect();
        assertThat(isMultiSelect, equalTo(false));
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
