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

import info.magnolia.ui.api.task.Task;

import java.util.List;
import java.util.Map;

/**
 * Magnolia TasksManager interface.
 */
public interface TasksManager {
    /**
     * TaskListener.
     */
    public interface TaskListener {

        void taskClaimed(long id, String userId);

        void taskAdded(Task task);

        void taskRemoved(long id);

        void taskCompleted(long id);
    }

    void claim(long taskId, String userId);

    void addTask(Task task, Map<String, Object> content);

    List<Task> getAllTasks();

    void complete(long taskId, Map<String, Object> results);

    void removeTask(long id);

    /**
     * Beware: this method is for registering task listeners and should only be used by the entry point of our application AdmincentralUI where we register a dispatcher.
     * If you'll use it to register your own TaskListeners this is likely to introduce a memory leak. You should listen to the TaskEvent instead.
     */
    void registerTasksListener(String userName, TaskListener listener);

    void unregisterTasksListener(String userName, TaskListener listener);

}
