/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.task.action;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.task.schedule.TaskSchedulerService;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.admincentral.shellapp.pulse.task.DefaultTaskDetailPresenter;
import info.magnolia.ui.api.shell.Shell;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link AbortTaskAction}.
 */
public class AbortTaskActionTest extends MgnlTestCase {

    private Task task;
    private ResolveTaskActionDefinition definition;
    private AbortTaskAction action;
    private TasksManager tasksManager;
    private TaskSchedulerService schedulerService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.task = new Task();
        this.tasksManager = mock(TasksManager.class);
        this.definition = new ResolveTaskActionDefinition();
        this.schedulerService = mock(TaskSchedulerService.class);
        this.action = new AbortTaskAction(definition, task, tasksManager, mock(DefaultTaskDetailPresenter.class), mock(Shell.class), schedulerService);
    }

    @Test
    public void testAbortUnschedulesTask() throws Exception {
        task.setId("123");
        task.setStatus(Task.Status.Scheduled);

        definition.setDecision("abort");

        // WHEN
        action.execute();

        // THEN
        verify(schedulerService, times(1)).unSchedule(task);

    }
}
