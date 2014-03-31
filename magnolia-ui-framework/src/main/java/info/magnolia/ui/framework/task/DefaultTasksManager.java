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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultTasksManager.
 */
public class DefaultTasksManager implements TasksManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void claim(long taskId, String userId) {
        log.warn("Not implemented.");
    }

    @Override
    public void addTask(Task task, HashMap<String, Object> content) {
        log.warn("Not implemented.");
    }

    @Override
    public Collection<Task> getAllTasks() {
        log.warn("Not implemented.");
        return null;
    }

    @Override
    public void complete(long taskId, Map<String, Object> results) {
        log.warn("Not implemented.");
    }

    @Override
    public void removeTask(long id) {
        log.warn("Not implemented.");
    }
}
