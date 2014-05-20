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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.task.Task;

import java.util.Collection;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TaskItemTest.
 */
public class TaskItemTest {

    @Before
    public void setUp() {
        Context ctx = mock(Context.class);
        User user = mock(User.class);

        when(ctx.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("someone");

        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void taskItemResolvesAdditionalContentProperties() throws Exception {
        // GIVEN
        Task task = new Task();
        task.setContent(new HashMap<String, Object>());
        task.getContent().put("foo", 1);
        task.getContent().put("bar", "baz");

        // WHEN
        TaskItem taskItem = new TaskItem(task, null);
        Collection<String> ids = (Collection<String>) taskItem.getItemPropertyIds();

        // THEN
        assertThat(ids, hasItems("foo", "bar", "actorId", "comment", "id"));

        assertEquals("1", taskItem.getItemProperty("foo").getValue());
        assertEquals("baz", taskItem.getItemProperty("bar").getValue());
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
