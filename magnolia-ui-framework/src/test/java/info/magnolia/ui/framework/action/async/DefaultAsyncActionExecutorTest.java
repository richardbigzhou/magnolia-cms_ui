/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.scheduler.CommandJob.JobResult;
import info.magnolia.module.scheduler.SchedulerModule;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.hamcrest.Execution;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Map;

import javax.inject.Provider;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

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

    private MessagesManager messagesManager;
    // current trigger and listener for TriggerListener tests
    private Trigger trigger;
    private TriggerListener triggerListener;
    private ListenerManager listenerManager = mock(ListenerManager.class);

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
        given(scheduler.getListenerManager()).willReturn(listenerManager);

        definition = new CommandActionDefinition();
        i18n = mock(SimpleTranslator.class, new ReturnsArgumentAt(0));
        item = new JcrNodeAdapter(session.getRootNode());
    }

    private void setUpForTriggerListenerTests() throws Exception {
        messagesManager = mock(MessagesManager.class);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);

        // mock system context
        MockContext sysCtx = new MockContext();
        User sysUser = mock(User.class);
        doReturn(UserManager.SYSTEM_USER).when(sysUser).getName();
        sysCtx.setUser(sysUser);
        ComponentsTestUtil.setInstance(SystemContext.class, sysCtx);

        // mock scheduler to get a handle on trigger/triggerListener created by action
        doAnswer(new TriggerListenerTracker()).when(listenerManager).addTriggerListener(any(TriggerListener.class));
        doAnswer(new TriggerTracker()).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
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
        JobDetail jobDetail = JobBuilder.newJob(info.magnolia.module.scheduler.CommandJob.class).
                withIdentity("UI Action triggered execution of [default:qux] by user [" + TEST_USER + "].").build();
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

    @Test
    public void triggerListenerSendsSuccessMessageToCurrentUser() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");

        setUpForTriggerListenerTests();

        JobExecutionContext jec = mock(JobExecutionContext.class);
        doReturn(new JobResult(true)).when(jec).getResult();

        AsyncActionExecutor executor = new DefaultAsyncActionExecutor<>(definition, schedulerModuleProvider, ctx, null, i18n);

        // WHEN
        executor.execute(item, null);
        // simulate triggerComplete after execute completed, last parameter (-1) is scheduler internals, we don't use it.
        triggerListener.triggerComplete(trigger, jec, null);

        // THEN we only want to make sure message is sent with current user (not system user)
        verify(messagesManager).sendMessage(eq(TEST_USER), any(Message.class));
    }

    @Test
    public void triggerListenerSendsErrorMessageToCurrentUser() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");

        setUpForTriggerListenerTests();

        JobExecutionContext jec = mock(JobExecutionContext.class);
        doReturn(new JobResult(false)).when(jec).getResult();

        AsyncActionExecutor executor = new DefaultAsyncActionExecutor<>(definition, schedulerModuleProvider, ctx, null, i18n);

        // WHEN
        executor.execute(item, null);
        // simulate triggerComplete after execute completed, last parameter (-1) is scheduler internals, we don't use it.
        triggerListener.triggerComplete(trigger, jec, null);

        // THEN we only want to make sure message is sent with current user (not system user)
        verify(messagesManager).sendMessage(eq(TEST_USER), any(Message.class));
    }

    @Test
    public void executorThrowsExceptionWhenJobCompletedBeforeDelayTimeButTheResultIsError() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");

        setUpForTriggerListenerTests();

        doAnswer(new TriggerTracker() {
                     @Override
                     public Void answer(InvocationOnMock invocation) throws Throwable {
                         Void answer = super.answer(invocation);
                         // simulate triggerComplete immediately with error+exception, last parameter (-1) is scheduler internals, we don't use it.
                         JobExecutionContext jec = mock(JobExecutionContext.class);
                         doReturn(new JobResult(false, new QuxException())).when(jec).getResult();
                         triggerListener.triggerComplete(trigger, jec, null);
                         return answer;
                     }
                 }
        ).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        final AsyncActionExecutor executor = new DefaultAsyncActionExecutor<>(definition, schedulerModuleProvider, ctx, null, i18n);

        // WHEN / THEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                executor.execute(item, null);
            }
        }, throwsAnException(instanceOf(QuxException.class)));
    }

    /**
     * Marker exception to make sure we catch expected exception in tests.
     */
    private static class QuxException extends Exception {
    }

    private class TriggerListenerTracker implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            triggerListener = (TriggerListener) invocation.getArguments()[0];
            return null;
        }
    }

    private class TriggerTracker implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            trigger = (Trigger) invocation.getArguments()[1];
            return null;
        }
    }
}