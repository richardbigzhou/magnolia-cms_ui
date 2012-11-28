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
package info.magnolia.ui.admincentral.content.view;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import info.magnolia.ui.admincentral.app.content.ContentSubAppDescriptor;
import info.magnolia.ui.admincentral.content.view.builder.ContentViewBuilder;
import info.magnolia.ui.admincentral.event.ItemDoubleClickedEvent;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;


/**
 * Tests for ContentPresenter.
 */
public class ContentPresenterTest {

    protected ContentViewBuilder contentViewBuilder;

    protected AppContext context;

    protected EventBus eventBus;

    protected Shell shell;

    protected JcrItemAdapter item;

    protected static final String TEST_WORKSPACE_NAME = "test";

    protected static final String TEST_ITEM_PATH = "2";

    @Before
    public void setUp() {
        contentViewBuilder = mock(ContentViewBuilder.class);
        context = mock(AppContext.class);
        final ContentSubAppDescriptor descr = mock(ContentSubAppDescriptor.class);
        when(context.getDefaultSubAppDescriptor()).thenReturn(descr);
        final WorkbenchDefinition workbench = mock(WorkbenchDefinition.class);
        when(descr.getWorkbench()).thenReturn(workbench);
        when(workbench.getWorkspace()).thenReturn(TEST_WORKSPACE_NAME);
        eventBus = mock(EventBus.class);
        shell = mock(Shell.class);
        item = mock(JcrItemAdapter.class);
        when(item.getPath()).thenReturn(TEST_ITEM_PATH);
    }

    @Test
    public void testOnItemSelectionFiresOnEventBus() {
        // GIVEN see setUp

        // WHEN
        final ContentPresenter presenter = new ContentPresenter(context, contentViewBuilder, eventBus, shell);
        presenter.onItemSelection(item);

        // THEN
        ArgumentCaptor<ItemSelectedEvent> argument = ArgumentCaptor.forClass(ItemSelectedEvent.class);
        verify(eventBus).fireEvent(argument.capture());
        assertEquals(TEST_WORKSPACE_NAME, argument.getValue().getWorkspace());
        assertEquals(TEST_ITEM_PATH, argument.getValue().getPath());
    }

    @Test
    public void testOnDoubleClickFiresOnEventBus() {
        // GIVEN see setUp

        // WHEN
        final ContentPresenter presenter = new ContentPresenter(context, contentViewBuilder, eventBus, shell);
        presenter.onDoubleClick(item);

        // THEN
        ArgumentCaptor<ItemDoubleClickedEvent> argument = ArgumentCaptor.forClass(ItemDoubleClickedEvent.class);
        verify(eventBus).fireEvent(argument.capture());
        assertEquals(TEST_WORKSPACE_NAME, argument.getValue().getWorkspace());
        assertEquals(TEST_ITEM_PATH, argument.getValue().getPath());
    }
}
