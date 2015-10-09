/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.task.action.availability;

import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.task.Task;
import info.magnolia.task.Task.Status;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.NullItem;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Availability rule used for task actions.
 */
public class TaskAvailabilityRule extends AbstractAvailabilityRule {

    private static final Logger log = LoggerFactory.getLogger(TaskAvailabilityRule.class);

    private final TaskAvailabilityRuleDefinition definition;
    private final TasksManager tasksManager;

    /**
     * @deprecated since 5.4.3.
     */
    @Deprecated
    public TaskAvailabilityRule(TaskAvailabilityRuleDefinition definition) {
        this(definition, Components.getComponent(TasksManager.class));
    }

    @Inject
    public TaskAvailabilityRule(TaskAvailabilityRuleDefinition definition, TasksManager tasksManager) {
        this.definition = definition;
        this.tasksManager = tasksManager;
    }

    @Override
    public final boolean isAvailableForItem(Object itemId) {
        if (itemId == null || itemId instanceof NullItem) {
            log.warn("Got a null task. Availability rule will return false");
            return false;
        }

        Task task = null;
        if (itemId instanceof Task) {
            task = (Task) itemId;
        } else {
            task = tasksManager.getTaskById(itemId.toString());
        }
        if (task != null) {
            boolean statusMatches = false;

            for (Status status : definition.getStatus()) {
                statusMatches = statusMatches | status.equals(task.getStatus());
            }

            return isVisibleToUser(task) && statusMatches;
        }

        return false;
    }

    protected boolean isVisibleToUser(Task task) {
        return !definition.isAssignee() || MgnlContext.getUser().getName().equals(task.getActorId());
    }

}
