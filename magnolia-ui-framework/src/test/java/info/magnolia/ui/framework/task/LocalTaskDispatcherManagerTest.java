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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.MgnlGroup;
import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.event.EventBus;
import info.magnolia.task.Task;
import info.magnolia.task.event.TaskEvent;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.util.Providers;

public class LocalTaskDispatcherManagerTest extends MgnlTestCase {

    private SecuritySupport securitySupport;

    private UserManager userManager;

    private User alice;
    private User bob;
    private User charlie;

    private Group group;
    private Group subGroup1;
    private Group subGroup2;


    @Before
    @Override
    public void setUp() throws Exception {

        this.securitySupport = mock(SecuritySupport.class);

        this.userManager = mock(UserManager.class);

        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);

        doReturn(userManager).when(securitySupport).getUserManager();

        this.group = new MgnlGroup("1", "group", Arrays.asList("subGroup1"), Collections.EMPTY_LIST);
        this.subGroup1 = new MgnlGroup("2", "subGroup1", Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        this.subGroup2 = new MgnlGroup("3", "subGroup2", Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        this.alice = new MgnlUser("alice", "admin", Arrays.<String>asList("group"), Collections.EMPTY_LIST, Collections.EMPTY_MAP);
        this.bob = new MgnlUser("bob", "admin", Arrays.<String>asList("subGroup1"), Collections.EMPTY_LIST, Collections.EMPTY_MAP);
        this.charlie = new MgnlUser("charlie", "admin", Arrays.<String>asList("subGroup2"), Collections.EMPTY_LIST, Collections.EMPTY_MAP);

        when(userManager.getUsersWithGroup("group", true)).thenReturn(Arrays.asList("alice"));
        when(userManager.getUsersWithGroup("subGroup1", true)).thenReturn(Arrays.asList(new String[]{"alice", "bob"}));
        when(userManager.getUsersWithGroup("subGroup2", true)).thenReturn(Arrays.asList("charlie"));
    }

    @Test
    public void testTaskRecipientsAreCorrect() throws Exception {
        // GIVEN
        final TrackingTaskEventDispatcher aliceListener = new TrackingTaskEventDispatcher();
        final TrackingTaskEventDispatcher bobListener = new TrackingTaskEventDispatcher();
        final TrackingTaskEventDispatcher charlieListener = new TrackingTaskEventDispatcher();

        final LocalTaskDispatcherManager manager = new LocalTaskDispatcherManager(mock(EventBus.class), Providers.of(securitySupport));
        manager.registerLocalTasksListener("alice", aliceListener);
        manager.registerLocalTasksListener("bob", bobListener);
        manager.registerLocalTasksListener("charlie", charlieListener);

        final Task task1 = new Task();
        task1.setGroupIds(Arrays.asList("group"));

        final Task task2 = new Task();
        task2.setGroupIds(Arrays.asList("subGroup1"));

        // WHEN
        manager.taskAdded(new TaskEvent(task1));
        manager.taskAdded(new TaskEvent(task2));

        // THEN

        // Alice gets both tasks since she belongs to the broad group
        assertTrue(aliceListener.trackedTasks.contains(task1));
        assertTrue(aliceListener.trackedTasks.contains(task2));

        // Bob gets only second task since he belongs to the sub-group1
        assertFalse(bobListener.trackedTasks.contains(task1));
        assertTrue(bobListener.trackedTasks.contains(task2));

        // Charlie gets no tasks
        assertFalse(charlieListener.trackedTasks.contains(task1));
        assertFalse(charlieListener.trackedTasks.contains(task2));
    }

    /**
     * Simply tracks the dispatched tasks without additional actions.
     */
    private static class TrackingTaskEventDispatcher implements TaskEventDispatcher {

        List<Task> trackedTasks = new ArrayList<Task>();

        @Override
        public void onTaskEvent(TaskEvent task) {
            trackedTasks.add(task.getTask());
        }
    }
}
