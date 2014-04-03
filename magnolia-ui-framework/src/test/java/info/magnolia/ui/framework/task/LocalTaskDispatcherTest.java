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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.ui.api.task.Task;
import info.magnolia.ui.api.task.Task.Status;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link LocalTaskDispatcher}.
 */
public class LocalTaskDispatcherTest {
    @Before
    public void setUp() {
        Context ctx = mock(Context.class);
        User usr = mock(User.class);
        when(ctx.getUser()).thenReturn(usr);
        when(usr.getName()).thenReturn("peter");
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void sendsEvents() throws InterruptedException {

        // GIVEN
        EventBus eventBus = new SimpleEventBus();
        LocalTaskDispatcher dispatcher = new LocalTaskDispatcher(eventBus, null);
        ArrayList<TaskEvent> events = new ArrayList<TaskEvent>();
        eventBus.addHandler(TaskEvent.class, new CollectingTaskEventHandler(events));

        // WHEN
        Task task = new Task();
        task.setId(0);
        task.setName("foo");
        task.setStatus(Status.Created);

        dispatcher.taskAdded(task);
        Thread.sleep(100);

        Task claimed = new Task();
        claimed.setId(1);
        claimed.setName("foo");
        claimed.setStatus(Status.InProgress);

        dispatcher.taskClaimed(claimed.getId(), "peter");
        Thread.sleep(100);

        dispatcher.taskRemoved(1);
        Thread.sleep(100);

        // THEN
        assertEquals(3, events.size());
        assertEquals("foo", events.get(0).getTask().getName());
        assertTrue(events.get(1).isClaimed());
        assertEquals(1, events.get(2).getId());
        assertTrue(events.get(2).isRemoved());
    }

    @Test
    public void sendsEventsEvenIfHandlersFail() throws InterruptedException {

        // GIVEN
        EventBus eventBus = new SimpleEventBus();
        LocalTaskDispatcher dispatcher = new LocalTaskDispatcher(eventBus, null);
        ArrayList<TaskEvent> events = new ArrayList<TaskEvent>();
        eventBus.addHandler(TaskEvent.class, new ThrowingMessageEventHandler(events));
        eventBus.addHandler(TaskEvent.class, new ThrowingMessageEventHandler(events));

        // WHEN
        Task task = new Task();
        task.setId(0);
        task.setName("foo");
        task.setStatus(Status.Created);

        dispatcher.taskAdded(task);
        Thread.sleep(100);

        Task claimed = new Task();
        claimed.setId(1);
        claimed.setName("foo");
        claimed.setStatus(Status.InProgress);

        dispatcher.taskClaimed(claimed.getId(), "peter");
        Thread.sleep(100);

        // THEN
        assertEquals(4, events.size());
        assertEquals("foo", events.get(0).getTask().getName());
        assertEquals("foo", events.get(1).getTask().getName());
        assertTrue(events.get(2).isClaimed());
        assertTrue(events.get(3).isClaimed());
    }

    private static class CollectingTaskEventHandler implements TaskEventHandler {

        private final List<TaskEvent> events;

        public CollectingTaskEventHandler(List<TaskEvent> events) {
            this.events = events;
        }

        @Override
        public void taskClaimed(TaskEvent taskEvent) {
            events.add(taskEvent);
        }

        @Override
        public void taskAdded(TaskEvent taskEvent) {
            events.add(taskEvent);
        }

        @Override
        public void taskRemoved(TaskEvent taskEvent) {
            events.add(taskEvent);
        }

        @Override
        public void taskCompleted(TaskEvent taskEvent) {
            events.add(taskEvent);
        }
    }

    private static class ThrowingMessageEventHandler extends CollectingTaskEventHandler {

        private ThrowingMessageEventHandler(List<TaskEvent> events) {
            super(events);
        }

        @Override
        public void taskClaimed(TaskEvent taskEvent) {
            super.taskClaimed(taskEvent);
            throw new RuntimeException("Intentionally thrown exception for testing");
        }

        @Override
        public void taskAdded(TaskEvent taskEvent) {
            super.taskAdded(taskEvent);
            throw new RuntimeException("Intentionally thrown exception for testing");
        }
    }
}
