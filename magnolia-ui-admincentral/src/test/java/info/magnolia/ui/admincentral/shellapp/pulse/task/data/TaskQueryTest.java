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
package info.magnolia.ui.admincentral.shellapp.pulse.task.data;


import static info.magnolia.ui.admincentral.shellapp.pulse.PulseMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.ConfiguredTaskDefinition;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageConstants;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.vaadin.data.Item;

public class TaskQueryTest extends MgnlTestCase {

    private static final String USER = "user";

    private TasksManager tasksManager;

    private TaskDefinitionRegistry taskDefinitionRegistry;
    private TaskQuery taskQueryWithAllStatutesAllowed;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ((MockContext) MgnlContext.getInstance()).setUser(mock(User.class));

        taskDefinitionRegistry = mock(TaskDefinitionRegistry.class);
        tasksManager = TaskManagerMock.builder().
                withCreatedWithIds(1, 2, 3).
                withInProgressWithIds(4, 5, 6).
                withResolvedWithIds(7, 8, 9).
                build();

        taskQueryWithAllStatutesAllowed = new TaskQuery(tasksManager, taskDefinitionRegistry, new TestTaskQueryDefinition());
    }

    @Test
    public void testSize() throws Exception {
        // GIVEN
        final TaskQueryDefinition defCreatedAndInProgress = new TestTaskQueryDefinition();
        defCreatedAndInProgress.setTypes(ImmutableList.of(Task.Status.Created, Task.Status.InProgress));

        final TaskQueryDefinition defResolvedWithGrouping = new TestTaskQueryDefinition();
        defResolvedWithGrouping.setTypes(ImmutableList.of(Task.Status.Resolved));
        defResolvedWithGrouping.setGroupingByType(true);

        // WHEN
        int sizeWithAllTypeAllowed = taskQueryWithAllStatutesAllowed.size();
        int sizeWithCreatedAndInProgress = new TaskQuery(tasksManager, taskDefinitionRegistry, defCreatedAndInProgress).size();
        int sizeWithInfoWithGrouping = new TaskQuery(tasksManager, taskDefinitionRegistry, defResolvedWithGrouping).size();

        // THEN
        // Nine altogether
        assertEquals(sizeWithAllTypeAllowed, 9);
        // Three errors and three warnings
        assertEquals(sizeWithCreatedAndInProgress, 6);
        // Three info messages plus one grouping entry
        assertEquals(sizeWithInfoWithGrouping, 4);
    }

    @Test
    public void testGetItems() throws Exception {
        // GIVEN
        final TaskQueryDefinition defCreatedAndInProgressDesc = new TestTaskQueryDefinition();

        defCreatedAndInProgressDesc.setTypes(Arrays.asList(Task.Status.Created, Task.Status.InProgress));
        defCreatedAndInProgressDesc.setSortPropertyIds(new Object[]{MessageConstants.ID});
        defCreatedAndInProgressDesc.setSortPropertyAscendingStates(new boolean[]{false});

        final TaskQuery query2 = new TaskQuery(tasksManager, taskDefinitionRegistry, defCreatedAndInProgressDesc);

        // WHEN
        final List<Item> items = taskQueryWithAllStatutesAllowed.loadItems(0, 9);
        final List<Item> itemsSorted = query2.loadItems(0, 6);

        // THEN
        assertThat(items, containsAmountOfTaskItems(9));
        assertThat(items, containsIdsInOrder(1, 2, 3, 4, 5, 6, 7, 8, 9));

        assertThat(itemsSorted, containsAmountOfTaskItems(6));
        assertThat(itemsSorted, containsIdsInOrder(6, 5, 4, 3, 2, 1));
    }

    @Test
    public void testGetTaskTitle() throws Exception {
        // GIVEN
        String title = "testTitle";
        final Task task = createSampleTask();

        final TaskQuery query = taskQueryWithAllStatutesAllowed;

        final ConfiguredTaskDefinition taskDefinition = new ConfiguredTaskDefinition();
        taskDefinition.setTitle(title);
        when(taskDefinitionRegistry.get(task.getName())).thenReturn(taskDefinition);

        // WHEN
        String taskTitle = query.getTaskTitle(task);

        // THEN
        assertThat(taskTitle, is(title));
    }

    private Task createSampleTask() {
        final Task task = new Task();
        task.setName("testTask");
        return task;
    }

    @Test
    public void testGetTaskTitleWithComment() throws Exception {
        // GIVEN
        final Task task = createSampleTask();
        String comment = "comment bla";
        String title = "testTitle";

        task.setComment(comment);

        ConfiguredTaskDefinition taskDefinition = new ConfiguredTaskDefinition();
        taskDefinition.setTitle(title);
        when(taskDefinitionRegistry.get(task.getName())).thenReturn(taskDefinition);

        // WHEN
        String taskTitle = taskQueryWithAllStatutesAllowed.getTaskTitle(task);

        // THEN
        String expectedTitle = title + "|" + comment;
        assertThat(taskTitle, is(expectedTitle));
    }

    @Test
    public void testFallBackToTaskName() throws Exception {
        // GIVEN
        final Task task = createSampleTask();
        when(taskDefinitionRegistry.get(task.getName())).thenThrow(new RegistrationException("Intentionally thrown exception."));

        // WHEN
        String taskTitle = taskQueryWithAllStatutesAllowed.getTaskTitle(task);

        // THEN
        assertThat(taskTitle, is(task.getName()));
    }

    @Test
    public void testFallBackToTaskNameWithComment() throws Exception {
        // GIVEN
        String comment = "comment bla";
        final Task task = createSampleTask();
        task.setComment(comment);

        when(taskDefinitionRegistry.get(task.getName())).thenThrow(new RegistrationException("Intentionally thrown exception."));

        // WHEN
        String taskTitle = taskQueryWithAllStatutesAllowed.getTaskTitle(task);

        // THEN
        String expectedTitle = task.getName() + "|" + comment;
        assertThat(taskTitle, is(expectedTitle));
    }

    private class TestTaskQueryDefinition extends TaskQueryDefinition {

        public TestTaskQueryDefinition() {
            setUserName(USER);
        }
    }
}
