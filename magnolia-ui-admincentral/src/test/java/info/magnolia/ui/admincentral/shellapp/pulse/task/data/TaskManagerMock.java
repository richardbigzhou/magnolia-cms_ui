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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.task.Task;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class TaskManagerMock {

    private List<Task> created = Collections.emptyList();
    private List<Task> inProgress = Collections.emptyList();
    private List<Task> resolved = Collections.emptyList();

    private TaskManagerMock(List<Task> created, List<Task> inProgress, List<Task> resolved) {
        this.created = created;
        this.inProgress = inProgress;
        this.resolved = resolved;
    }

    TasksManager createMockMessagesManager() {
        TasksManager tasksManager = mock(TasksManager.class);

        doAnswer(new Answer() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final List<Task.Status> types = (List<Task.Status>) invocation.getArguments()[1];
                final Map<String, Boolean> sortCriteria = (Map<String, Boolean>) invocation.getArguments()[2];
                int limit = (Integer) invocation.getArguments()[3];
                int offset = (Integer) invocation.getArguments()[4];
                return getTaskList(types, sortCriteria).subList(offset, offset + limit);
            }
        }).when(tasksManager).findTasksByUserAndStatus(anyString(), anyListOf(Task.Status.class), anyMapOf(String.class, Boolean.class), anyInt(), anyInt());

        doAnswer(new Answer() {
            @Override
            @SuppressWarnings("unchecked")
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return getTaskList((List<Task.Status>) invocation.getArguments()[1], Collections.<String, Boolean>emptyMap()).size();
            }
        }).when(tasksManager).getTasksAmountByUserAndStatus(anyString(), anyListOf(Task.Status.class));

        return tasksManager;
    }


    private List<Task> getTaskList(List<Task.Status> statuses, Map<String, Boolean> sortCriteria) {
        final List<Task> list = Lists.newArrayList();
        if (statuses.contains(Task.Status.Created)) {
            list.addAll(created);
        }

        if (statuses.contains(Task.Status.InProgress)) {
            list.addAll(inProgress);
        }

        if (statuses.contains(Task.Status.Resolved)) {
            list.addAll(resolved);
        }

        if (sortCriteria.containsKey(MessageConstants.ID)) {
            final boolean asc = sortCriteria.get(MessageConstants.ID);
            Collections.sort(list, new Comparator<Task>() {
                @Override
                public int compare(Task o1, Task o2) {
                    int comparisonResult = o1.getId().compareTo(o2.getId());
                    return asc ? comparisonResult : -comparisonResult;
                }
            });
        }

        return list;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Task> created;
        private List<Task> inProgress;
        private List<Task> resolved;

        public Builder withCreatedWithIds(Object... ids) {
            this.created = Lists.transform(Arrays.asList(ids), new IdToTask(Task.Status.Created));
            return this;
        }

        public Builder withInProgressWithIds(Object... ids) {
            this.inProgress = Lists.transform(Arrays.asList(ids), new IdToTask(Task.Status.InProgress));
            return this;
        }

        public Builder withResolvedWithIds(Object... ids) {
            this.resolved = Lists.transform(Arrays.asList(ids), new IdToTask(Task.Status.Resolved));
            return this;
        }

        public TasksManager build() {
            return new TaskManagerMock(created, inProgress, resolved).createMockMessagesManager();
        }

        private static class IdToTask implements Function<Object, Task> {

            private Task.Status status;

            public IdToTask(Task.Status status) {
                this.status = status;
            }

            @Nullable
            @Override
            public Task apply(@Nullable Object input) {
                final Task task = new Task();
                task.setStatus(status);
                task.setId(String.valueOf(input));
                return task;
            }
        }
    }
}
