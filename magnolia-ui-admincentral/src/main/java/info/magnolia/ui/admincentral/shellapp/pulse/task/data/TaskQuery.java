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
package info.magnolia.ui.admincentral.shellapp.pulse.task.data;

import info.magnolia.registry.RegistrationException;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.TaskDefinition;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseQuery;
import info.magnolia.ui.admincentral.shellapp.pulse.data.PulseConstants;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;

/**
 * {@link LazyPulseQuery} implementation which serves {@link Task} objects via {@link TasksManager}.
 */
public class TaskQuery extends LazyPulseQuery<Task.Status, Task> {

    private static final Logger log = LoggerFactory.getLogger(TaskQuery.class);

    private TaskDefinitionRegistry taskDefinitionRegistry;

    private TasksManager tasksManager;

    @Inject
    public TaskQuery(TasksManager tasksManager, TaskDefinitionRegistry taskDefinitionRegistry, TaskQueryDefinition queryDefinition) {
        super(queryDefinition);
        this.tasksManager = tasksManager;
        this.taskDefinitionRegistry = taskDefinitionRegistry;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void mapObjectToItem(Task task, Item item) {
        item.addItemProperty(TaskConstants.ID, new ObjectProperty(task.getId()));
        item.addItemProperty(TaskConstants.STATUS_PROPERTY_ID, new ObjectProperty(task.getStatus()));
        item.addItemProperty(TaskConstants.NEW_PROPERTY_ID, new ObjectProperty(task.getStatus() == Task.Status.Created, Boolean.class));
        item.addItemProperty(TaskConstants.TASK_PROPERTY_ID, new ObjectProperty(getTaskTitle(task), String.class));
        item.addItemProperty(TaskConstants.SENDER_PROPERTY_ID, new ObjectProperty(task.getRequestor(), String.class));
        item.addItemProperty(TaskConstants.LAST_CHANGE_PROPERTY_ID, new ObjectProperty(task.getModificationDate(), Date.class));

        item.addItemProperty(TaskConstants.ASSIGNED_TO_PROPERTY_ID, new ObjectProperty(StringUtils.defaultString(task.getActorId()), String.class));

        String sentTo = "";
        if (task.getGroupIds() != null && task.getGroupIds().size() > 0) {
            sentTo += StringUtils.join(task.getGroupIds(), ",");
        }
        if (task.getActorIds() != null && task.getActorIds().size() > 0) {
            sentTo += StringUtils.join(task.getActorIds(), ",");
        }

        item.addItemProperty(TaskConstants.SENT_TO_PROPERTY_ID, new ObjectProperty(sentTo, String.class));
    }

    @Override
    protected long getEntriesAmount(List<Task.Status> types) {
        return tasksManager.getTasksAmountByUserAndStatus(getQueryDefinition().userName(), types);
    }

    @Override
    protected List<Task> getEntries(List<Task.Status> types, int limit, int offset) {
        long timeMs = System.currentTimeMillis();
        List<Task> tasksCollection = tasksManager.findTasksByUserAndStatus(getQueryDefinition().userName(), types, getSortCriteria(), limit, offset);
        log.debug("Fetched {} object list from {} in {}ms", limit, offset, System.currentTimeMillis() - timeMs);
        return tasksCollection;
    }

    @Override
    protected Task createGroupingEntry(Task.Status type) {
        Task groupingTask = new Task();
        groupingTask.setId(PulseConstants.GROUP_PLACEHOLDER_ITEMID + type.name());
        groupingTask.setStatus(type);
        return groupingTask;
    }

    protected String getTaskTitle(Task task) {
        String title = task.getName();
        if (title == null) {
            return null;
        }

        try {
            TaskDefinition definition = taskDefinitionRegistry.get(task.getName());
            if (definition != null) {
                title = definition.getTitle();
            }
        } catch (RegistrationException e) {
            log.error("Could not get task definition for {}.", task.getName(), e);
        }
        String comment = (StringUtils.isNotEmpty(task.getComment())) ? "|" + task.getComment() : "";
        return title + comment;
    }
}
