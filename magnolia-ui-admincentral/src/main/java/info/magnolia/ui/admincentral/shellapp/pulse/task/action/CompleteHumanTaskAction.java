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
package info.magnolia.ui.admincentral.shellapp.pulse.task.action;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.shellapp.pulse.task.TaskPresenter;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.api.task.Task;
import info.magnolia.ui.api.task.Task.Status;
import info.magnolia.ui.framework.task.TasksManager;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Action for completing a human task.
 */
public class CompleteHumanTaskAction extends AbstractHumanTaskAction<CompleteHumanTaskActionDefinition> {

    public CompleteHumanTaskAction(CompleteHumanTaskActionDefinition definition, Task task, TaskPresenter taskPresenter, TasksManager taskManager, Shell shell) {
        super(definition, task, taskManager, taskPresenter, shell);
    }

    @Override
    protected void executeTask(TasksManager taskManager, Task task) {
        log.debug("About to complete human task named [{}]", task.getName());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(DECISION, getDefinition().getDecision());
        long taskId = task.getId();

        taskManager.complete(taskId, result);
        getTaskPresenter().onUpdateDetailView(String.valueOf(taskId));

        getShell().openNotification(MessageStyleTypeEnum.INFO, true, getDefinition().getSuccessMessage());
    }

    @Override
    protected void canExecuteTask(Task task) throws IllegalStateException {
        final String currentUser = MgnlContext.getUser().getName();
        if (task.getStatus() != Status.InProgress || !currentUser.equals(task.getActorId())) {
            throw new IllegalStateException("Task status is [" + task.getStatus() + "] and is assigned to user [" + task.getActorId() + "]. Only in progress tasks assigned to yourself can be completed.");
        }
    }
}
