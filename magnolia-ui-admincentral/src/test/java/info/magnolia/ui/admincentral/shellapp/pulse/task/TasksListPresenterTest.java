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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.ConfiguredTaskDefinition;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import info.magnolia.ui.admincentral.shellapp.pulse.task.definition.ConfiguredTaskUiDefinition;
import info.magnolia.ui.framework.shell.ShellImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * PulseTasksPresenterTest.
 */
public class TasksListPresenterTest {

    private TasksListPresenter presenter;
    private TaskDefinitionRegistry definitionRegistry;
    private TasksManager tasksManager;
    private ComponentProvider componentProvider;

    @Before
    public void setUp() throws Exception {

        definitionRegistry = mock(TaskDefinitionRegistry.class);
        tasksManager = mock(TasksManager.class);
        componentProvider = mock(ComponentProvider.class);

        presenter = new TasksListPresenter(mock(TasksListView.class), mock(TasksContainer.class), mock(ShellImpl.class),
                tasksManager, definitionRegistry, componentProvider, mock(SimpleTranslator.class));
    }

    @Test
    public void testGetTaskTitle() throws Exception {
        // GIVEN
        String taskName = "testTask";
        String title = "testTitle";

        ConfiguredTaskDefinition taskDefinition = new ConfiguredTaskDefinition();
        taskDefinition.setTitle(title);

        when(definitionRegistry.get(taskName)).thenReturn(taskDefinition);

        // WHEN
        String taskTitle = presenter.getItemTitle(taskName);

        // THEN
        assertThat(taskTitle, is(title));
    }

    @Test
    public void testFallBackToTaskName() throws Exception {
        // GIVEN
        String taskName = "testTask";
        when(definitionRegistry.get(taskName)).thenThrow(new RegistrationException("error"));

        // WHEN
        String taskTitle = presenter.getItemTitle(taskName);

        // THEN
        assertThat(taskTitle, is(taskName));
    }

    @Test
    public void testOpenItem() throws Exception {
        // GIVEN
        String taskName = "testTask";
        Task task = new Task();
        task.setName(taskName);

        TaskDetailPresenter detailPresenter = mock(TaskDetailPresenter.class);

        String title = "testTitle";
        ConfiguredTaskUiDefinition taskDefinition = new ConfiguredTaskUiDefinition();
        taskDefinition.setPresenterClass(TaskDetailPresenter.class);
        taskDefinition.setTitle(title);

        when(definitionRegistry.get(taskName)).thenReturn(taskDefinition);
        when(tasksManager.getTaskById(anyString())).thenReturn(task);
        when(componentProvider.newInstance(eq(TaskDetailPresenter.class), anyVararg())).thenReturn(detailPresenter);

        // WHEN
        presenter.openItem(taskName);

        // THEN
        verify(detailPresenter, times(1)).setListener(eq(presenter));
        verify(detailPresenter, times(1)).start();
    }
}
