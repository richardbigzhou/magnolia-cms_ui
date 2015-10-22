/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.framework.action;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.Arrays;
import java.util.Map;

import javax.jcr.Property;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

public class AbstractCommandActionTest {

    public static final String TEST_USER = "phantomas";

    private static final String WEBSITE_CONTENT =
            "/parent.uuid=1\n" +
                    "/parent/sub.uuid=2";

    private Session session;
    private Scheduler scheduler;
    private CommandActionDefinition definition;
    private CommandsManager commandsManager;
    private SimpleTranslator i18n;
    private MessagesManager messagesManager;

    // current trigger and listener for TriggerListener tests
    private Trigger trigger;
    private TriggerListener triggerListener;

    @Before
    public void setUp() throws Exception {
        // Init context
        session = SessionTestUtil.createSession(RepositoryConstants.WEBSITE, WEBSITE_CONTENT);
        MockContext ctx = new MockContext();
        ctx.addSession(RepositoryConstants.WEBSITE, session);

        User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        ctx.setUser(user);
        MgnlContext.setInstance(ctx);

        // Init scheduler
        ModuleRegistry moduleRegistry = mock(ModuleRegistryImpl.class);
        SchedulerModule schedulerModule = mock(SchedulerModule.class);
        when(moduleRegistry.isModuleRegistered("scheduler")).thenReturn(true);
        when(moduleRegistry.getModuleInstance("scheduler")).thenReturn(schedulerModule);
        scheduler = mock(Scheduler.class);
        when(schedulerModule.getScheduler()).thenReturn(scheduler);

        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);

        definition = new CommandActionDefinition();
        commandsManager = mock(CommandsManager.class);
        QuxCommand quxCommand = new QuxCommand();
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "qux")).thenReturn(quxCommand);

        i18n = mock(SimpleTranslator.class);
    }

    private void setUpForTriggerListenerTests() throws Exception {
        messagesManager = mock(MessagesManager.class);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);

        // mock system context
        MockContext sysCtx = new MockContext();
        User sysUser = mock(User.class);
        doReturn("superuser").when(sysUser).getName();
        sysCtx.setUser(sysUser);
        ComponentsTestUtil.setInstance(SystemContext.class, sysCtx);

        // mock scheduler to get a handle on trigger/triggerListener created by action
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                triggerListener = (TriggerListener) invocation.getArguments()[0];
                return null;
            }
        }).when(scheduler).addTriggerListener(any(TriggerListener.class));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                trigger = (Trigger) invocation.getArguments()[1];
                return null;
            }
        }).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        session = null;
        ComponentsTestUtil.clear();

        trigger = null;
        triggerListener = null;
    }

    @Test
    public void getParamsReturnsBasicContextParamsFromNode() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, null);

        // WHEN
        action.execute();
        Map<String, Object> params = action.getParams();

        // THEN
        assertThat(params, hasEntry(Context.ATTRIBUTE_REPOSITORY, (Object) RepositoryConstants.WEBSITE));
        assertThat(params, hasEntry(Context.ATTRIBUTE_PATH, (Object) "/parent/sub"));
        assertThat(params, hasEntry(Context.ATTRIBUTE_UUID, (Object) "2"));
        assertThat(params, hasEntry(Context.ATTRIBUTE_REQUESTOR, (Object) TEST_USER));
    }

    @Test
    public void getParamsReturnsBasicContextParamsFromProperty() throws Exception {
        // GIVEN
        Property jcrProperty = session.getNode("/parent/sub").setProperty("property1", "value1");
        JcrItemAdapter item = new JcrPropertyAdapter(jcrProperty);
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, null);

        // WHEN
        action.execute();
        Map<String, Object> params = action.getParams();

        // THEN
        assertThat(params, hasEntry(Context.ATTRIBUTE_REPOSITORY, (Object) RepositoryConstants.WEBSITE));
        assertThat(params, hasEntry(Context.ATTRIBUTE_PATH, (Object) "/parent/sub/property1"));
        // In case of property, the Identifier is the parent Node ID
        assertThat(params, hasEntry(Context.ATTRIBUTE_UUID, (Object) "2"));
        assertThat(params, hasEntry(Context.ATTRIBUTE_REQUESTOR, (Object) TEST_USER));
    }

    @Test
    public void getParamsReturnsOtherParamsFromDefinition() throws Exception {
        // GIVEN
        definition.getParams().put("abc", "bar");
        definition.getParams().put("def", "baz");
        definition.getParams().put("ghi", "456");
        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, null);

        // WHEN
        action.execute();
        Map<String, Object> params = action.getParams();

        // THEN
        assertThat(params, hasEntry("abc", (Object) "bar"));
        assertThat(params, hasEntry("def", (Object) "baz"));
        assertThat(params, hasEntry("ghi", (Object) "456"));
        // ensure the default params are returned as well.
        assertThat(params, hasEntry(Context.ATTRIBUTE_REPOSITORY, (Object) RepositoryConstants.WEBSITE));
        assertThat(params, hasEntry(Context.ATTRIBUTE_PATH, (Object) "/parent/sub"));
        assertThat(params, hasEntry(Context.ATTRIBUTE_UUID, (Object) "2"));
        assertThat(params, hasEntry(Context.ATTRIBUTE_REQUESTOR, (Object) TEST_USER));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getParamsReturnsImmutableMap() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, null);

        // WHEN
        action.execute();
        Map<String, Object> params = action.getParams();
        params.put("foo", "bar");
        // THEN exception
    }

    // Do we really need this?? what else could be expected from two separate action instances??
    @Test
    public void getParamsTwiceReturnsNewParamsMap() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, null);
        AbstractCommandAction<CommandActionDefinition> action2 = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, null);

        // WHEN
        action.execute();
        action2.execute();

        // THEN
        assertNotSame(action.getParams(), action2.getParams());
    }

    @Test
    public void executeSynchronously() throws Exception {
        // GIVEN
        definition.setCommand("qux");
        Command quxCommand = commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "qux");

        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, null);

        // WHEN
        action.execute();

        // THEN
        verify(commandsManager, times(1)).executeCommand(quxCommand, action.getParams());
    }

    @Test
    public void executeAsynchronously() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");

        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, i18n);

        // WHEN
        action.execute();

        // THEN
        assertFalse("Stop Processing = false as it's invoke asynchronously", (Boolean) MgnlContext.getAttribute(AbstractCommandAction.COMMAND_RESULT, Context.LOCAL_SCOPE));
    }

    @Test
    public void executeAsynchronouslyAndTakesLongerThanTimeToWait() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");
        definition.setTimeToWait(500);
        definition.setParallel(false); // parallel execution messes up with job names depending on test-order

        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        JobDetail jobDetail = new JobDetail();
        jobDetail.setName("UI Action triggered execution of [default:qux] by user [" + TEST_USER + "].");
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(scheduler.getCurrentlyExecutingJobs()).thenReturn(Arrays.asList(jobExecutionContext));

        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, i18n);

        // WHEN
        action.execute();

        // THEN
        assertEquals("ui-framework.abstractcommand.asyncaction.long", definition.getSuccessMessage());
    }

    @Test
    public void triggerListenerSendsSuccessMessageToCurrentUser() throws Exception {
        // GIVEN
        definition.setAsynchronous(true);
        definition.setCommand("qux");

        setUpForTriggerListenerTests();

        JobExecutionContext jec = mock(JobExecutionContext.class);
        doReturn(1).when(jec).getResult(); // 1 means command job completed successfully

        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, i18n);

        // WHEN
        action.execute();
        // simulate triggerComplete after execute completed, last parameter (-1) is scheduler internals, we don't use it.
        triggerListener.triggerComplete(trigger, jec, -1);

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
        doReturn(0).when(jec).getResult(); // 0 means an error occurred

        JcrItemAdapter item = new JcrNodeAdapter(session.getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(definition, item, commandsManager, null, i18n);

        // WHEN
        action.execute();
        // simulate triggerComplete after execute completed, last parameter (-1) is scheduler internals, we don't use it.
        triggerListener.triggerComplete(trigger, jec, -1);

        // THEN we only want to make sure message is sent with current user (not system user)
        verify(messagesManager).sendMessage(eq(TEST_USER), any(Message.class));
    }

    private static final class QuxCommand extends MgnlCommand {

        @Override
        public boolean execute(Context context) throws Exception {
            return false;
        }
    }

    private class SchedulerModule extends ModuleDefinition {

        public Scheduler getScheduler() {
            return null;
        }
    }
}
