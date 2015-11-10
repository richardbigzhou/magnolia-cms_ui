/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.framework.action.async;

import static com.google.common.collect.Maps.newHashMap;
import static info.magnolia.test.hamcrest.ExceptionMatcher.instanceOf;
import static info.magnolia.test.hamcrest.ExecutionMatcher.throwsAnException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.scheduler.SchedulerModule;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.hamcrest.Execution;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Map;

import javax.inject.Provider;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.google.common.collect.Lists;

public class DefaultAsyncActionExecutorTest {

    private static final String TEST_USER = "phantomas";

    private Session session;
    private CommandActionDefinition definition;

    private Scheduler scheduler;
    private final Map<String, Object> params = newHashMap();
    private SimpleTranslator i18n;
    private Provider<SchedulerModule> schedulerModuleProvider;
    private MockContext ctx;
    private JcrItemAdapter item;

    @Before
    public void setUp() throws Exception {
        // Init context
        session = new MockSession(RepositoryConstants.WEBSITE);
        ctx = new MockContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);

        User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        ctx.setUser(user);
        MgnlContext.setInstance(ctx);

        // Mock scheduler
        schedulerModuleProvider = mock(Provider.class);
        SchedulerModule schedulerModule = mock(SchedulerModule.class);
        given(schedulerModuleProvider.get()).willReturn(schedulerModule);
        scheduler = mock(Scheduler.class);
        given(schedulerModule.getScheduler()).willReturn(scheduler);

        definition = new CommandActionDefinition();
        i18n = mock(SimpleTranslator.class, new ReturnsArgumentAt(0));
        item = new JcrNodeAdapter(session.getRootNode());
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        session = null;
        ComponentsTestUtil.clear();
    }

    @Test
    public void executeCompletesBeforeTimeToWait() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");
        // no mocking of scheduler#getCurrentlyExecutingJobs => does as if job had completed already

        AsyncActionExecutor executor = new DefaultAsyncActionExecutor<>(definition, schedulerModuleProvider, ctx, null, i18n);

        // WHEN
        boolean isRunningInTheBackground = executor.execute(item, null);

        // THEN
        assertThat(isRunningInTheBackground, is(false));
    }

    @Test
    public void executeTakesLongerThanTimeToWait() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");
        definition.setTimeToWait(500);
        definition.setParallel(false); // parallel execution messes up with job names depending on test-order

        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        JobDetail jobDetail = new JobDetail();
        jobDetail.setName("UI Action triggered execution of [default:qux] by user [" + TEST_USER + "].");
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(scheduler.getCurrentlyExecutingJobs()).thenReturn(Lists.newArrayList(jobExecutionContext));

        AsyncActionExecutor executor = new DefaultAsyncActionExecutor<>(definition, schedulerModuleProvider, ctx, null, i18n);

        // WHEN
        boolean isRunningInTheBackground = executor.execute(item, null);

        // THEN
        assertThat(isRunningInTheBackground, is(true));
    }

    @Test
    public void executeCatchesSchedulerException() throws Exception {
        // GIVEN
        doThrow(SchedulerException.class).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
        final AsyncActionExecutor executor = new DefaultAsyncActionExecutor<>(definition, schedulerModuleProvider, ctx, null, i18n);

        // WHEN / THEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                executor.execute(item, params);
            }
        }, throwsAnException(instanceOf(AsyncActionExecutor.ParallelExecutionException.class)));
    }
}