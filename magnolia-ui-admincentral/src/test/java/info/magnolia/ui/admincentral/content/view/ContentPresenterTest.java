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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import info.magnolia.ui.admincentral.app.content.ContentAppDescriptor;
import info.magnolia.ui.admincentral.content.view.builder.ContentViewBuilderProvider;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for ContentPresenter.
 */
public class ContentPresenterTest {

    @Test
    public void testOnItemSelectionFiresOnEventBus() {
        // GIVEN
        final String testWorkspaceName = "test";
        final String testItemId = "2";
        final ContentViewBuilderProvider contentViewBuilderProvider = mock(ContentViewBuilderProvider.class);
        final AppContext context = mock(AppContext.class);
        final ContentAppDescriptor descr = mock(ContentAppDescriptor.class);
        when(context.getAppDescriptor()).thenReturn(descr);
        final WorkbenchDefinition workbench = mock(WorkbenchDefinition.class);
        when(descr.getWorkbench()).thenReturn(workbench);
        when(workbench.getWorkspace()).thenReturn(testWorkspaceName);
        final EventBus eventBus = mock(EventBus.class);
        final Shell shell = mock(Shell.class);
        final JcrItemAdapter item = mock(JcrItemAdapter.class);
        when(item.getItemId()).thenReturn(testItemId);

        // WHEN
        final ContentPresenter presenter = new ContentPresenter(contentViewBuilderProvider, context, eventBus, shell);
        presenter.onItemSelection(item);

        // THEN
        ArgumentCaptor<ItemSelectedEvent> argument = ArgumentCaptor.forClass(ItemSelectedEvent.class);
        verify(eventBus).fireEvent(argument.capture());
        assertEquals(testWorkspaceName, argument.getValue().getWorkspace());
        assertEquals(testItemId, argument.getValue().getPath());
    }

}
