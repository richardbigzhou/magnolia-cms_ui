/**
 * This file Copyright (c) 2014-2016 Magnolia International
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

import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.admincentral.shellapp.pulse.task.definition.ConfiguredTaskUiDefinition;
import info.magnolia.ui.framework.shell.ShellImpl;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link TasksListPresenter}.
 */
public class TasksListPresenterTest {

    private TasksListPresenter presenter;
    private TaskDefinitionRegistry definitionRegistry;
    private TasksManager tasksManager;
    private ComponentProvider componentProvider;
    private Task task;

    @Before
    public void setUp() throws Exception {
        MockContext context = new MockContext();
        User user = mock(User.class);
        when(user.getName()).thenReturn("testuser");
        context.setUser(user);

        this.task = new Task();
        this.definitionRegistry = mock(TaskDefinitionRegistry.class);
        this.tasksManager = mock(TasksManager.class);
        this.componentProvider = mock(ComponentProvider.class);
        this.presenter = new TasksListPresenter(mock(TasksListView.class), mock(TasksContainer.class), mock(ShellImpl.class),
                tasksManager, definitionRegistry, componentProvider, mock(SimpleTranslator.class), context);

        task.setName("testTask");
    }

    @Test
    public void testOpenItem() throws Exception {
        // GIVEN
        TaskDetailPresenter detailPresenter = mock(TaskDetailPresenter.class);

        String title = "testTitle";
        ConfiguredTaskUiDefinition taskDefinition = new ConfiguredTaskUiDefinition();
        taskDefinition.setPresenterClass(TaskDetailPresenter.class);
        taskDefinition.setTitle(title);

        when(definitionRegistry.get(task.getName())).thenReturn(taskDefinition);
        when(tasksManager.getTaskById(anyString())).thenReturn(task);
        when(componentProvider.newInstance(eq(TaskDetailPresenter.class), anyVararg())).thenReturn(detailPresenter);

        // WHEN
        presenter.openItem(task.getName());

        // THEN
        verify(detailPresenter, times(1)).setListener(eq(presenter));
        verify(detailPresenter, times(1)).start();
    }
}
