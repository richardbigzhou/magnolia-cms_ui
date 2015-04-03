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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.workbench.column.definition.StatusColumnDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Container;

/**
 * Tests.
 */
public class AbstractContentPresenterTest {

    private AbstractContentPresenter presenter;

    private class DummyContentPresenter extends AbstractContentPresenter {
        private DummyContentPresenter(final ComponentProvider componentProvider) {
            super(componentProvider);
        }

        @Override
        protected Container initializeContainer() {
            return null;
        }

        @Override
        public void refresh() {
            // do nothing;
        }
    }

    @Before
    public void setUp() throws Exception {
        final ComponentProvider componentProvider = mock(ComponentProvider.class);
        presenter = new DummyContentPresenter(componentProvider);
    }

    @Test
    public void getAvailableColumns() {
        // GIVEN
        List<ColumnDefinition> allColumns = new ArrayList<ColumnDefinition>();
        StatusColumnDefinition statusColumnDefinition = new StatusColumnDefinition();
        statusColumnDefinition.setEnabled(false);
        allColumns.add(statusColumnDefinition);
        PropertyColumnDefinition propertyColumnDefinition = new PropertyColumnDefinition();
        propertyColumnDefinition.setEnabled(true);
        allColumns.add(propertyColumnDefinition);

        // WHEN
        List<ColumnDefinition> result = presenter.getAvailableColumns(allColumns);

        // THEN
        assertEquals(1, result.size());
        assertEquals(propertyColumnDefinition, result.get(0));
    }

    @Test
    public void rootItemIdIsExcludedFromMultipleSelection() {
        // GIVEN
        Object rootId = new JcrItemId("aaa", "qux");
        Object bar = new JcrItemId("bar", "qux");
        Object baz = new JcrItemId("baz", "qux");

        Set<Object> selectedIds = new HashSet<Object>();

        selectedIds.add(rootId);
        selectedIds.add(bar);
        selectedIds.add(baz);

        ContentConnector contentConnector = mock(ContentConnector.class);
        when(contentConnector.getDefaultItemId()).thenReturn(rootId);

        presenter.start(mock(WorkbenchDefinition.class), mock(EventBus.class), "meh", contentConnector);

        // WHEN
        presenter.onItemSelection(selectedIds);
        List<Object> result = presenter.getSelectedItemIds();

        // THEN
        assertThat(result, not(containsInAnyOrder(rootId)));
        assertThat(result, containsInAnyOrder(bar, baz));
    }

    @Test
    public void rootItemIdIsSelectedIfNoItemIsVisuallySelected() {
        // GIVEN
        Object rootId = new JcrItemId("aaa", "qux");

        Set<Object> selectedIds = null; // no selection in UI

        ContentConnector contentConnector = mock(ContentConnector.class);
        when(contentConnector.getDefaultItemId()).thenReturn(rootId);

        presenter.start(mock(WorkbenchDefinition.class), mock(EventBus.class), "meh", contentConnector);

        // WHEN
        presenter.onItemSelection(selectedIds);
        List<Object> result = presenter.getSelectedItemIds();

        // THEN
        assertThat(result, hasItem(rootId));
    }
}
