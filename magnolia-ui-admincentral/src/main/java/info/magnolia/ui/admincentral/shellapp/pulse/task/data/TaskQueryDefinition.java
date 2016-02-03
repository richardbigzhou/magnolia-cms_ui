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
package info.magnolia.ui.admincentral.shellapp.pulse.task.data;

import info.magnolia.task.Task;
import info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseQueryDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.data.PulseQueryDefinition;

import java.util.Arrays;
import java.util.Date;

/**
 * Definition of the {@link TaskQuery}. Pre-sets the properties described in {@link TaskConstants}.
 */
public class TaskQueryDefinition extends LazyPulseQueryDefinition<Task.Status> implements PulseQueryDefinition<Task.Status> {

    public TaskQueryDefinition() {
        setTypes(Arrays.asList(Task.Status.Created, Task.Status.InProgress, Task.Status.Resolved, Task.Status.Failed));

        addProperty(TaskConstants.NEW_PROPERTY_ID, Boolean.class, true, true, false);
        addProperty(TaskConstants.TASK_PROPERTY_ID, String.class, null, true, false);
        addProperty(TaskConstants.STATUS_PROPERTY_ID, Task.Status.class, Task.Status.Created, true, false);
        addProperty(TaskConstants.SENDER_PROPERTY_ID, String.class, null, true, false);
        addProperty(TaskConstants.ASSIGNED_TO_PROPERTY_ID, String.class, null, true, false);
        addProperty(TaskConstants.SENT_TO_PROPERTY_ID, String.class, null, true, false);
        addProperty(TaskConstants.LAST_CHANGE_PROPERTY_ID, Date.class, null, true, true);
        addProperty(TaskConstants.STATUS_PROPERTY_ID, Task.Status.class, null, true, false);
    }
}
