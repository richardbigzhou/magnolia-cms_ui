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
package info.magnolia.ui.framework.task;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.task.Task;
import info.magnolia.task.event.TaskEvent;
import info.magnolia.task.event.TaskEventHandler;
import info.magnolia.test.mock.MockContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;

/**
 * Test case for {@link LocalTaskDispatcher}.
 */
public class LocalTaskDispatcherTest {

    private Context context;
    private LocalTaskDispatcher dispatcher;
    private ArrayList<TaskEvent> events;
    private EventBus eventBus;
    private ComponentProvider componentProvider;
    private VaadinSession vaadinSession;
    private VaadinService vaadinService;

    @Before
    public void setUp() {
        this.context = mock(Context.class);
        User usr = mock(User.class);
        when(context.getUser()).thenReturn(usr);
        MgnlContext.setInstance(context);
        this.componentProvider = mock(ComponentProvider.class);
        when(componentProvider.getComponent(eq(SystemContext.class))).thenReturn(new MockContext());

        this.eventBus = new SimpleEventBus();
        this.events = new ArrayList<TaskEvent>();
        vaadinService = mock(VaadinService.class);
        doAnswer(new CallsRealMethods()).when(vaadinService).runPendingAccessTasks(Matchers.any(VaadinSession.class));
        doAnswer(new CallsRealMethods()).when(vaadinService).accessSession(Matchers.any(VaadinSession.class), Matchers.any(Runnable.class));


        vaadinSession = spy(new VaadinSession(vaadinService));
        Lock lock = new ReentrantLock();
        doReturn(lock).when(vaadinSession).getLockInstance();
        this.dispatcher = new LocalTaskDispatcher(eventBus, vaadinSession, context, componentProvider);

        eventBus.addHandler(TaskEvent.class, new CollectingTaskEventHandler(events));

    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void sendsEvents() throws InterruptedException {

        // GIVEN


        // WHEN
        Task task = new Task();
        task.setId("0");
        task.setName("foo");
        task.setStatus(Task.Status.Created);

        dispatcher.onTaskEvent(new TaskEvent(task));

        Task claimed = new Task();
        claimed.setId(String.valueOf(1));
        claimed.setName("foo");
        claimed.setStatus(Task.Status.InProgress);
        claimed.setActorId("peter");
        dispatcher.onTaskEvent(new TaskEvent(claimed));

        claimed.setStatus(Task.Status.Removed);
        dispatcher.onTaskEvent(new TaskEvent(claimed));

        vaadinSession.lock();
        vaadinService.runPendingAccessTasks(vaadinSession);
        vaadinSession.unlock();


        Thread.sleep(100);

        // THEN
        assertEquals(3, events.size());
        assertEquals("foo", events.get(0).getTask().getName());
        assertEquals("peter", events.get(1).getTask().getActorId());
        assertEquals("1", events.get(2).getTask().getId());
        assertEquals(Task.Status.Removed, events.get(2).getTask().getStatus());
    }

    @Test
    public void sendsEventsEvenIfHandlersFail() throws InterruptedException {

        // GIVEN
        eventBus.addHandler(TaskEvent.class, new ThrowingMessageEventHandler(events));

        // WHEN
        Task task = new Task();
        task.setId("0");
        task.setName("foo");
        task.setStatus(Task.Status.Created);

        dispatcher.onTaskEvent(new TaskEvent(task));

        Task claimed = new Task();
        claimed.setId("1");
        claimed.setName("foo");
        claimed.setActorId("peter");
        claimed.setStatus(Task.Status.InProgress);

        dispatcher.onTaskEvent(new TaskEvent(claimed));

        vaadinSession.lock();
        vaadinService.runPendingAccessTasks(vaadinSession);
        vaadinSession.unlock();

        // THEN
        assertEquals(4, events.size());
        assertEquals("foo", events.get(0).getTask().getName());
        assertEquals("foo", events.get(1).getTask().getName());
        assertEquals(Task.Status.InProgress, events.get(2).getTask().getStatus());
        assertEquals(Task.Status.InProgress, events.get(3).getTask().getStatus());
    }

    @Test(expected = IllegalStateException.class)
    public void testDispatchingWithEmptyContextCreatesOneOnTheFly() throws Exception {
        // GIVEN
        MgnlContext.setInstance(null);

        // WHEN
        Task task = new Task();
        task.setId("0");
        task.setName("foo");
        task.setStatus(Task.Status.Created);

        dispatcher.onTaskEvent(new TaskEvent(task));

        vaadinSession.lock();
        vaadinService.runPendingAccessTasks(vaadinSession);
        vaadinSession.unlock();

        // THEN
        verify(componentProvider, times(1)).getComponent(eq(SystemContext.class));
        assertThat(MgnlContext.getInstance(), nullValue());
        fail("Intentionally thrown exception.");
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
        public void taskScheduled(TaskEvent taskEvent) {
            events.add(taskEvent);
        }

        @Override
        public void taskFailed(TaskEvent taskEvent) {
            events.add(taskEvent);
        }

        @Override
        public void taskArchived(TaskEvent taskEvent) {
            events.add(taskEvent);
        }

        @Override
        public void taskResolved(TaskEvent taskEvent) {
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
