/**
 * This file Copyright (c) 2014-2016 Magnolia International
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.task.Task;
import info.magnolia.task.Task.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TaskAvailabilityRuleTest.
 */
public class TaskAvailabilityRuleTest {

    @Before
    public void setUp() {
        User user = mock(User.class);
        when(user.getName()).thenReturn("foo");

        Context context = mock(Context.class);
        when(context.getUser()).thenReturn(user);

        MgnlContext.setInstance(context);

    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void taskIsAvailableWhenRuleDefinitionStatusIsSameAsTaskStatus() throws Exception {
        // GIVEN
        TaskAvailabilityRuleDefinition definition = new TaskAvailabilityRuleDefinition();
        definition.setStatus(Status.Created);

        Task task = new Task();
        task.setStatus(Status.Created);

        TaskAvailabilityRule rule = new TaskAvailabilityRule(definition);

        // WHEN
        boolean available = rule.isAvailableForItem(task);

        // THEN
        assertTrue(available);
    }

    @Test
    public void taskIsNotAvailableWhenRuleDefinitionStatusIsNotSameAsTaskStatus() throws Exception {
        // GIVEN
        TaskAvailabilityRuleDefinition definition = new TaskAvailabilityRuleDefinition();
        definition.setStatus(Status.InProgress);

        Task task = new Task();
        task.setStatus(Status.Created);

        TaskAvailabilityRule rule = new TaskAvailabilityRule(definition);

        // WHEN
        boolean available = rule.isAvailableForItem(task);

        // THEN
        assertFalse(available);
    }

    @Test
    public void taskIsNotAvailableWhenAssigneeIsNotCurrentUser() throws Exception {
        // GIVEN
        TaskAvailabilityRuleDefinition definition = new TaskAvailabilityRuleDefinition();
        definition.setStatus(Status.InProgress);

        Task task = new Task();
        task.setStatus(Status.InProgress);
        task.setActorId("qux");

        TaskAvailabilityRule rule = new TaskAvailabilityRule(definition);

        // WHEN
        boolean available = rule.isAvailableForItem(task);

        // THEN
        assertFalse(available);
    }

    @Test
    public void taskIsNotAvailableIfNull() throws Exception {
        // GIVEN
        TaskAvailabilityRuleDefinition definition = new TaskAvailabilityRuleDefinition();
        definition.setStatus(Status.InProgress);

        TaskAvailabilityRule rule = new TaskAvailabilityRule(definition);

        // WHEN
        boolean available = rule.isAvailableForItem(null);

        // THEN
        assertFalse(available);
    }

}
