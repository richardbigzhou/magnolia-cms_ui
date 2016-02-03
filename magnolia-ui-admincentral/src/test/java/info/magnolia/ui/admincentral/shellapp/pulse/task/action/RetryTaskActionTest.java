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
package info.magnolia.ui.admincentral.shellapp.pulse.task.action;

import static org.mockito.Mockito.*;

import info.magnolia.task.Task;
import info.magnolia.task.Task.Status;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.admincentral.shellapp.pulse.task.DefaultTaskDetailPresenter;
import info.magnolia.ui.api.shell.Shell;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

/**
 * Tests for {@link RetryTaskAction}.
 */
public class RetryTaskActionTest extends BaseHumanTaskActionTest {

    private RetryTaskAction action;

    private Task task;
    private TasksManager tasksManager;
    private RetryTaskActionDefinition definition;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        task = new Task();
        tasksManager = mock(TasksManager.class);
        definition = new RetryTaskActionDefinition();
        action = new RetryTaskAction(definition, task, tasksManager, mock(DefaultTaskDetailPresenter.class), mock(Shell.class));
    }

    @Test
    public void testRetryTaskWithPreviousDecision() throws Exception {
        // GIVEN
        task.setId("123");
        task.setStatus(Status.Failed);

        Map<String, Object> results = new HashMap<String, Object>() {{
            put(RetryTaskAction.DECISION, "approve");
        }};
        task.setResults(results);

        // WHEN
        action.execute();

        // THEN
        verify(tasksManager, times(1)).resolve(eq(task.getId()), argThat(new DecisionMatcher()));
    }

    private class DecisionMatcher extends ArgumentMatcher<Map> {

        @Override
        public boolean matches(Object o) {
            Map<String, Object> results = (Map<String, Object>) o;
            return "approve".equals(results.get(ResolveTaskAction.DECISION));
        }
    }
}
