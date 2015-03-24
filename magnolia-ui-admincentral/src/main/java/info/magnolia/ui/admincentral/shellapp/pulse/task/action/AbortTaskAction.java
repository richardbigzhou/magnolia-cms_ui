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

import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.task.schedule.TaskSchedulerService;
import info.magnolia.ui.admincentral.shellapp.pulse.task.DefaultTaskDetailPresenter;
import info.magnolia.ui.api.shell.Shell;

import javax.inject.Inject;

/**
 * This action is used to abort a task. It extends the {@link ResolveTaskAction} to check whether the task has been scheduled
 * and if so, it will use the {@link TaskSchedulerService} to un-schedule it.
 */
public class AbortTaskAction extends ResolveTaskAction {

    private final TaskSchedulerService schedulerService;

    @Inject
    public AbortTaskAction(ResolveTaskActionDefinition definition, Task task, TasksManager taskManager, DefaultTaskDetailPresenter taskPresenter, Shell shell, TaskSchedulerService schedulerService) {
        super(definition, task, taskManager, taskPresenter, shell);
        this.schedulerService = schedulerService;
    }

    @Override
    protected void executeTask(TasksManager taskManager, Task task) {

        if (task.getStatus() == Task.Status.Scheduled) {
            schedulerService.unSchedule(task);
        }
        super.executeTask(taskManager, task);
    }
}
