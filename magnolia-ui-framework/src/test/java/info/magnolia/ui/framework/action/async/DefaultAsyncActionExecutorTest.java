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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.scheduler.SchedulerModule;
import info.magnolia.test.hamcrest.Execution;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import java.util.Map;

import javax.inject.Provider;
import javax.jcr.Item;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public class DefaultAsyncActionExecutorTest {

    private DefaultAsyncActionExecutor<CommandActionDefinition> executor;
    private JcrItemAdapter item;
    private Scheduler scheduler;
    private final Map<String, Object> params = newHashMap();

    @Before
    public void setUp() throws Exception {
        CommandActionDefinition definition = mock(CommandActionDefinition.class);
        given(definition.getCommand()).willReturn("TestCommand");

        Provider<SchedulerModule> provider = mock(Provider.class);
        Context context = mock(Context.class);
        User user = mock(User.class);
        given(context.getUser()).willReturn(user);
        given(user.getName()).willReturn("TestUser");

        UiContext uiContext = mock(UiContext.class);
        SimpleTranslator simpleTranslator = mock(SimpleTranslator.class);

        Trigger trigger = mock(Trigger.class);
        JobDetail jobDetail = mock(JobDetail.class);

        SchedulerModule schedulerModule = mock(SchedulerModule.class);
        given(provider.get()).willReturn(schedulerModule);
        scheduler = mock(Scheduler.class);
        scheduler.scheduleJob(jobDetail, trigger);
        given(schedulerModule.getScheduler()).willReturn(scheduler);

        item = mock(JcrItemAdapter.class);

        Item jcrItem = mock(Item.class);

        given(item.getJcrItem()).willReturn(jcrItem);
        given(jcrItem.getPath()).willReturn("/foo/bar");

        executor = new DefaultAsyncActionExecutor<>(definition, provider, context, uiContext, simpleTranslator);
    }

    @Test
    public void schedulerExceptionIsCaught() throws Exception {
        // GIVEN
        doThrow(SchedulerException.class).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));


        // WHEN / THEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                executor.execute(item, params);
            }
        }, throwsAnException(instanceOf(AsyncActionExecutor.ParallelExecutionException.class)));
    }

    @Test
    public void objectAlreadyExistsExceptionIsCaught() throws Exception {
        // GIVEN
        doThrow(ObjectAlreadyExistsException.class).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // WHEN / THEN
        assertThat(new Execution() {
            @Override
            public void evaluate() throws Exception {
                executor.execute(item, params);
            }
        }, throwsAnException(instanceOf(AsyncActionExecutor.ParallelExecutionException.class)));
    }
}