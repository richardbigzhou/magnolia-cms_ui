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
package info.magnolia.ui.admincentral.shellapp.pulse.item.availability;

import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.api.task.Task;
import info.magnolia.ui.api.task.Task.Status;
import info.magnolia.ui.framework.task.TasksStore;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base abstract action availability rule class based on a task status.
 */
public abstract class AbstractTaskStatusRule extends AbstractAvailabilityRule {

    protected static final Logger log = LoggerFactory.getLogger(AbstractTaskStatusRule.class);

    private TasksStore store;

    @Inject
    public AbstractTaskStatusRule(TasksStore store) {
        this.store = store;
    }

    @Override
    public final boolean isAvailableForItem(Object itemId) {
        Task task = store.getTaskById(Long.valueOf((String) itemId));
        if (task == null) {
            log.warn("Could not get a Task with id [{}]. Availability rule will return true", itemId);
            return true;
        }
        return isAvailableForCurrentStatus(task.getStatus());
    }

    protected abstract boolean isAvailableForCurrentStatus(Status status);
}
