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

import info.magnolia.event.Event;
import info.magnolia.ui.api.task.Task;

/**
 * Task event.
 */
public class TaskEvent implements Event<TaskEventHandler> {

    private Task task;
    private boolean added;
    private boolean claimed;
    private boolean completed;
    private long id;
    private boolean removed;
    private String userId;

    public TaskEvent(Task task, boolean added) {
        this.task = task;
        this.added = added;

    }

    public TaskEvent(long id, boolean claimed, boolean completed, boolean removed, String userId) {
        this.id = id;
        this.removed = removed;
        this.claimed = claimed;
        this.completed = completed;
        this.userId = userId;
    }

    @Override
    public void dispatch(TaskEventHandler handler) {
        if (added) {
            handler.taskAdded(this);
        } else if (removed) {
            handler.taskRemoved(this);
        } else if (claimed) {
            handler.taskClaimed(this);
        } else {
            handler.taskCompleted(this);
        }
    }

    public Task getTask() {
        return task;
    }

    public boolean isAdded() {
        return added;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public boolean isCompleted() {
        return completed;
    }

    public long getId() {
        return id;
    }

    public boolean isRemoved() {
        return removed;
    }

    public String getUserId() {
        return userId;
    }

}
