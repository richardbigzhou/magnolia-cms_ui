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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.task.Task;
import info.magnolia.task.definition.TaskDefinition;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link TaskItem}.
 */
public class TaskItemTest {


    @Test
    public void testTaskFields() throws Exception {
        Date today = new Date();

        Task task = new Task();
        task.setComment("test comment");
        task.setActorId("actor");
        task.setCreationDate(today);
        task.setStatus(Task.Status.Created);

        // WHEN
        TaskItem<Task, TaskDefinition> taskItem = new TaskItem<Task, TaskDefinition>(task, mock(TaskDefinition.class));
        String comment = (String) taskItem.getItemProperty("comment").getValue();
        String actorId = (String) taskItem.getItemProperty("actorId").getValue();
        Date creationDate = (Date) taskItem.getItemProperty("creationDate").getValue();
        Task.Status status = (Task.Status) taskItem.getItemProperty("status").getValue();

        // THEN
        assertThat(comment, is("test comment"));
        assertThat(actorId, is("actor"));
        assertThat(creationDate, is(today));
        assertThat(status, is(Task.Status.Created));
    }

    @Test
    public void testTaskFieldsFromProperties() throws Exception {
        Date today = new Date();

        Task task = new Task();
        task.setComment("test comment");
        task.setActorId("actor");
        task.setCreationDate(today);
        task.setStatus(Task.Status.Created);

        // WHEN
        TaskItem<Task, TaskDefinition> taskItem = new TaskItem<Task, TaskDefinition>(task, mock(TaskDefinition.class),
                new String[] {"comment", "actorId", "status"});

        // THEN
        assertThat(taskItem.getItemProperty("creationDate"), nullValue());
    }

    @Test
    public void testTaskFieldsFromNestedProperties() throws Exception {
        final Date today = new Date();

        Task task = new Task();
        task.setComment("test comment");
        task.setActorId("actor");
        task.setCreationDate(today);
        task.setStatus(Task.Status.Created);

        task.setContent(new HashMap<String, Object>() {{

            put("field1", "value1");
            put("field2", 123);
            put("field3", 1234l);
            put("field4", today);
        }});

        // WHEN
        final List<String> nestedProperties = new LinkedList<String>() {{
            add("field1");
            add("field2");
            add("field3");
            add("field4");
        }};

        TaskItem<Task, TaskDefinition> taskItem = new TaskItem<Task, TaskDefinition>(task, mock(TaskDefinition.class),
                new String[] {"comment", "actorId", "status"},
                new HashMap<String, List<String>>() {{
                    put("content", nestedProperties);
                }}
        );

        // THEN
        assertThat((String) taskItem.getItemProperty("content.field1").getValue(), is("value1"));
        assertThat((Integer) taskItem.getItemProperty("content.field2").getValue(), is(123));
        assertThat((Long) taskItem.getItemProperty("content.field3").getValue(), is(1234l));
        assertThat((Date) taskItem.getItemProperty("content.field4").getValue(), is(today));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void customTaskItemContainsAdditionalGetters() throws Exception {
        // GIVEN
        Task task = new MyTask();

        // WHEN
        TaskItem taskItem = new TaskItem(task, null);
        Collection<String> ids = (Collection<String>) taskItem.getItemPropertyIds();

        // THEN
        assertThat(ids, hasItems("qux", "actorId", "comment", "id"));
    }

    private class MyTask extends Task {
        private String qux;

        public MyTask() {
            setContent(new HashMap<String, Object>());
        }

        public String getQux() {
            return qux;
        }

        public void setQux(String qux) {
            this.qux = qux;
        }

    }
}
