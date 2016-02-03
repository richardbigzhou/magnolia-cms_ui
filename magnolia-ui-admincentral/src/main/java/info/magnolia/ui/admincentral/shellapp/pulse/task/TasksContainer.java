/**
 * This file Copyright (c) 2015-2016 Magnolia International
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

import info.magnolia.context.Context;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseListContainer;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.task.data.TaskQueryDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.task.data.TaskQueryFactory;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Iterables;

/**
 * {@link LazyPulseListContainer} implementation capable of serving {@link Task} objects via
 * {@link TasksManager}. {@link Task.Status} enumeration is used as a grouping criteria.
 */
public class TasksContainer extends LazyPulseListContainer<Task.Status, TaskQueryDefinition, TaskQueryFactory> {

    private TasksManager tasksManager;

    @Inject
    public TasksContainer(TasksManager tasksManager,
                          TaskQueryDefinition taskQueryDefinition,
                          Provider<TaskQueryFactory> taskQueryFactoryProvider,
                          Context ctx) {
        super(taskQueryDefinition, taskQueryFactoryProvider, ctx.getUser().getName());
        this.tasksManager = tasksManager;
    }

    @Override
    public long size() {
        return tasksManager.getTasksAmountByUserAndStatus(getUserName(), getQueryDefinition().types());
    }

    @Override
    public void filterByItemCategory(PulseItemCategory category) {

        Task.Status[] statuses;

        switch (category) {
        case UNCLAIMED:
            statuses = new Task.Status[]{Task.Status.Created};
            break;
        case ONGOING:
            statuses = new Task.Status[]{Task.Status.InProgress};
            break;
        case DONE:
            statuses = new Task.Status[]{Task.Status.Resolved};
            break;
        case FAILED:
            statuses = new Task.Status[]{Task.Status.Failed};
            break;
        default:
            statuses = Task.Status.values();
        }

        List<Task.Status> newStatuses = Arrays.asList(statuses);
        if (!Iterables.elementsEqual(newStatuses, getQueryDefinition().types())) {
            getQueryDefinition().setTypes(newStatuses);
            refresh();
        }
    }
}
