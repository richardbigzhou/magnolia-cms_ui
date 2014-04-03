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
package info.magnolia.ui.framework.task;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.ui.api.task.Task;
import info.magnolia.ui.api.task.Task.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link TasksStore}.
 */
@Singleton
public class TasksStoreImpl implements TasksStore {

    private static Map<Long, Task> tasks = new HashMap<Long, Task>();

    private SecuritySupport securitySupport;

    @Inject
    public TasksStoreImpl(SecuritySupport securitySupport) {
        this.securitySupport = securitySupport;
    }

    @Override
    public void claim(long taskId, String userId) {
        tasks.get(taskId).setStatus(Task.Status.InProgress);
        tasks.get(taskId).setActorId(userId);
        // persist
        // event for ui, possibly not part of spike
    }

    @Override
    public void complete(long taskId) {
        tasks.get(taskId).setStatus(Task.Status.Completed);
    }

    @Override
    public void addTask(Task task) {
        task.setStatus(Task.Status.Created);
        long id = tasks.size();
        task.setId(id);
        tasks.put(id, task);
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<Task>(tasks.values());
    }

    @Override
    public List<Task> findAllTasksByUser(String userId) {
        List<Task> userTasks = new LinkedList<Task>();
        for (Task task : tasks.values()) {
            if (userId.equals(task.getActorId())) {
                userTasks.add(task);
            } else if (task.getGroupIds() != null && isUserMember(userId, task.getGroupIds())) {
                userTasks.add(task);
            } else if (task.getActorIds() != null) {

                List<String> actors = Arrays.asList(StringUtils.split(task.getActorIds(), ","));

                if (actors.contains(userId)) {
                    userTasks.add(task);
                }
            }
        }
        return userTasks;
    }

    @Override
    public List<Task> findTasksByUserAndStatus(String userId, List<Status> status) {
        List<Task> userTasks = findAllTasksByUser(userId);
        List<Task> tasks = new LinkedList<Task>();

        for (Task task : userTasks) {
            // filter out claimed tasks assigned to a different user
            if (!userId.equals(task.getActorId()) && task.getStatus() != Status.Created) {
                continue;
            }
            if (status.contains(task.getStatus())) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    @Override
    public Task getTaskById(long taskId) {
        return tasks.get(taskId);
    }

    @Override
    public void removeTask(long taskId) {
        tasks.remove(taskId);
    }

    private boolean isUserMember(String userId, String groupIds) {
        for (String groupId : Arrays.asList(StringUtils.split(groupIds))) {
            if (securitySupport.getUserManager().getUser(userId).inGroup(groupId)) {
                return true;
            }
        }
        return false;
    }
}
