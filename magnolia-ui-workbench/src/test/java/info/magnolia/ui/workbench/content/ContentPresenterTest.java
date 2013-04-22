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
package info.magnolia.ui.workbench.content;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.event.EventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.workbench.ContentPresenter;
import info.magnolia.ui.workbench.ContentViewBuilder;
import info.magnolia.ui.workbench.WorkbenchView;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for ContentPresenter.
 */
public class ContentPresenterTest {
    protected ContentViewBuilder contentViewBuilder;

    protected EventBus eventBus;

    protected JcrItemAdapter item;

    protected static final String TEST_WORKSPACE_NAME = "test";

    protected static final String TEST_ITEM_PATH = "2";

    private static final String TEST_WORKBENCHDEF_PATH = "/path/to/somewhere";

    private WorkbenchDefinition workbench;
    @Before
    public void setUp() {
        this.workbench = mock(WorkbenchDefinition.class);
        contentViewBuilder = mock(ContentViewBuilder.class);
        when(workbench.getWorkspace()).thenReturn(TEST_WORKSPACE_NAME);
        when(workbench.getPath()).thenReturn(TEST_WORKBENCHDEF_PATH);
        eventBus = mock(EventBus.class);
        item = mock(JcrItemAdapter.class);
        when(item.getPath()).thenReturn(TEST_ITEM_PATH);

    }

    @Test
    public void testOnItemSelectionFiresOnEventBus() {
        // GIVEN
        final ContentPresenter presenter = new ContentPresenter(contentViewBuilder);
        presenter.start(mock(WorkbenchView.class), workbench, null, eventBus);
        // WHEN
        presenter.onItemSelection(item);

        // THEN
        ArgumentCaptor<ItemSelectedEvent> argument = ArgumentCaptor.forClass(ItemSelectedEvent.class);
        verify(eventBus).fireEvent(argument.capture());
        assertEquals(TEST_WORKSPACE_NAME, argument.getValue().getWorkspace());
        assertEquals(TEST_ITEM_PATH, argument.getValue().getPath());
    }

    @Test
    public void testOnDoubleClickFiresOnEventBus() {
        // GIVEN
        final ContentPresenter presenter = new ContentPresenter(contentViewBuilder);
        presenter.start(mock(WorkbenchView.class), workbench, null, eventBus);

        // WHEN
        presenter.onDoubleClick(item);

        // THEN
        ArgumentCaptor<ItemDoubleClickedEvent> argument = ArgumentCaptor.forClass(ItemDoubleClickedEvent.class);
        verify(eventBus).fireEvent(argument.capture());
        assertEquals(TEST_WORKSPACE_NAME, argument.getValue().getWorkspace());
        assertEquals(TEST_ITEM_PATH, argument.getValue().getPath());
    }

    @Test
    public void testOnItemSelectionWithNullItemSetSelectedPath() {
        // GIVEN
        ContentPresenter presenter = new ContentPresenter(contentViewBuilder);
        presenter.start(mock(WorkbenchView.class), workbench, null, eventBus);
        // WHEN
        presenter.onItemSelection(null);

        // THEN
        assertEquals(TEST_WORKBENCHDEF_PATH, presenter.getSelectedItemPath());
    }
}
