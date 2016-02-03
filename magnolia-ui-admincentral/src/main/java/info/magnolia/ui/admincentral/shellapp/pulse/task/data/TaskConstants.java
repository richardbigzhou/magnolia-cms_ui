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

/**
 * Common constants used for arranging the access to {@link info.magnolia.task.Task} objects.
 *
 * @see TaskQueryDefinition
 * @see TaskQuery
 */
public class TaskConstants {
    public static final String ID = "id";
    public static final String ASSIGNED_TO_PROPERTY_ID = "assignedTo";
    public static final String SENT_TO_PROPERTY_ID = "sentTo";
    public static final String LAST_CHANGE_PROPERTY_ID = "mgnl:lastModified";
    public static final String SENDER_PROPERTY_ID = "sender";
    public static final String STATUS_PROPERTY_ID = "status";
    public static final String TASK_PROPERTY_ID = "task";
    public static final String NEW_PROPERTY_ID = "new";
}
