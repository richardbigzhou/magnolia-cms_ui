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
package info.magnolia.ui.admincentral.shellapp.pulse.task.action;

import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.admincentral.shellapp.pulse.task.DefaultTaskDetailPresenter;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.shell.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract action for a task action.
 * 
 * @param <D> {@link info.magnolia.ui.api.action.ActionDefinition}.
 */
public abstract class AbstractTaskAction<D extends ActionDefinition> extends AbstractAction<D> {
    protected static final Logger log = LoggerFactory.getLogger(AbstractTaskAction.class);

    protected static final String DECISION = "decision";
    protected static final String ACTOR_ID = "actorId";

    private Task task;
    private TasksManager tasksManager;
    private DefaultTaskDetailPresenter taskPresenter;
    private Shell shell;

    public AbstractTaskAction(D definition, Task task, TasksManager tasksManager, DefaultTaskDetailPresenter taskPresenter, Shell shell) {
        super(definition);
        this.task = task;
        this.tasksManager = tasksManager;
        this.taskPresenter = taskPresenter;
        this.shell = shell;
    }

    @Override
    public final void execute() throws ActionExecutionException {
        log.debug("About to execute Task [{}]", task);
        try {
            canExecuteTask(task);
            executeTask(tasksManager, task);
        } catch (Exception ex) {
            log.error("An error occurred while trying to execute task [{}]", task, ex);
            shell.showError("Error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Subclasses need to implement this method to actually execute the task.
     */
    protected abstract void executeTask(TasksManager tasksManager, Task task);

    /**
     * Subclasses can override this method to check if the current task can actually be executed, e.g. by checking the current Task status.
     * Default implementation does nothing.
     * 
     * @throws IllegalStateException if a task status doesn't allow this action to be executed (e.g. trying to complete a task which is not in progress). In general, subclasses can throw this exception for any reason
     * they deem should cause the current task execution to be aborted.
     * @See {@link Task.Status}
     *
     * @deprecated since 5.4, use availability rules to check the allowed statuses.
     * @see {@link info.magnolia.ui.admincentral.shellapp.pulse.task.action.availability.TaskAvailabilityRule}
     */
    @Deprecated
    protected void canExecuteTask(Task task) throws IllegalStateException {
        // no-op
    }

    /**
     * Subclasses can use the shell e.g. to display success notifications.
     */
    protected Shell getShell() {
        return shell;
    }

    /**
     * Subclasses can use the TaskPresenter to interact back with it and its parent presenter, i.e. PulsePresenter.
     */
    protected DefaultTaskDetailPresenter getTaskPresenter() {
        return taskPresenter;
    }
}
