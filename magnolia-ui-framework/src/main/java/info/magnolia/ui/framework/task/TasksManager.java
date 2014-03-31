/**
 * This file Copyright (c) 2013 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.framework.task;

import info.magnolia.ui.api.task.Task;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Magnolia TasksManager interface.
 */
public interface TasksManager {
    void claim(long taskId, String userId);

    void addTask(Task task, HashMap<String, Object> content);

    Collection<Task> getAllTasks();

    void complete(long taskId, Map<String, Object> results);

    void removeTask(long id);
}
