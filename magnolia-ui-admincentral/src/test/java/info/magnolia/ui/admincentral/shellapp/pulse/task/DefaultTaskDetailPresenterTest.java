/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.task.Task;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailView;
import info.magnolia.ui.admincentral.shellapp.pulse.task.definition.ConfiguredTaskUiDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.task.definition.TaskUiDefinition;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DefaultTaskDetailPresenter}.
 */
public class DefaultTaskDetailPresenterTest {
    private DefaultTaskDetailPresenter presenter;
    private PulseDetailView view;

    @Before
    public void setUp() {
        ConfiguredTaskUiDefinition definition = new ConfiguredTaskUiDefinition();
        definition.setTaskView("pages:publish");
        definition.setTitle("Test title");

        Task task = new Task();

        view = mock(PulseDetailView.class);
        presenter = new DefaultTaskDetailPresenter<TaskUiDefinition, Task>(view, definition, task, null, null, null, null, mock(ActionbarPresenter.class), null);
    }

    @Test
    public void testGetItemViewFromDefinition() throws Exception {
        // Given

        // WHEN
        String taskView = presenter.getItemViewName();

        // THEN
        assertThat(taskView, is("pages:publish"));
    }

    @Test
    public void testViewTitleFromDefinition() throws Exception {
        // Given

        // WHEN
        presenter.setItemViewTitle(view);

        // THEN
        verify(view, times(1)).setTitle(eq("Test title"));
    }
}
