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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.i18nsystem.ContextLocaleProvider;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Tests.
 */
public class AbstractCommandActionTest {
    private String website =
            "/parent.uuid=1\n" +
                    "/parent/sub.uuid=2";

    private MockSession session;

    private CommandsManager commandsManager;
    private Scheduler scheduler;
    private SimpleTranslator i18n;

    public static final String TEST_USER = "phantomas";

    @Before
    public void setUp() throws Exception {
        session = SessionTestUtil.createSession("website", website);
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);
        // Init scheduler
        ModuleRegistry moduleRegistry = mock(ModuleRegistryImpl.class);
        SchedulerModule schedulerModule = mock(SchedulerModule.class);
        when(moduleRegistry.isModuleRegistered("scheduler")).thenReturn(true);
        when(moduleRegistry.getModuleInstance("scheduler")).thenReturn(schedulerModule);
        scheduler = mock(Scheduler.class);
        when(schedulerModule.getScheduler()).thenReturn(scheduler);

        ComponentsTestUtil.setInstance(ModuleRegistry.class, moduleRegistry);

        MockWebContext webContext = new MockWebContext();
        webContext.setContextPath("/foo");
        webContext.addSession("website", session);
        final User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        final SecuritySupportImpl sec = new info.magnolia.cms.security.SecuritySupportImpl();
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        webContext.setUser(user);
        MgnlContext.setInstance(webContext);

        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getJCRSession("website")).thenReturn(session);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);



        commandsManager = mock(CommandsManager.class);
        i18n = mock(SimpleTranslator.class);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        session = null;
        ComponentsTestUtil.clear();
    }

    @Test
    public void testGetParamsReturnsBasicContextParamsFromNode() throws Exception {

        // GIVEN
        AbstractCommandAction<CommandActionDefinition> action =
                new AbstractCommandAction<CommandActionDefinition>(
                        new CommandActionDefinition(),
                        new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent/sub")),
                        commandsManager,
                        null,
                        null);

        // WHEN
        action.setCurrentItem(action.getItems().get(0));
        action.onPreExecute();
        Map<String, Object> params = action.getParams();

        // THEN
        assertTrue(params.containsKey(Context.ATTRIBUTE_REPOSITORY));
        assertTrue(params.containsKey(Context.ATTRIBUTE_PATH));
        assertTrue(params.containsKey(Context.ATTRIBUTE_UUID));
        assertTrue(params.containsKey(Context.ATTRIBUTE_REQUESTOR));

        assertEquals("website", params.get(Context.ATTRIBUTE_REPOSITORY));
        assertEquals("/parent/sub", params.get(Context.ATTRIBUTE_PATH));
        assertEquals("2", params.get(Context.ATTRIBUTE_UUID));
        assertEquals(TEST_USER, params.get(Context.ATTRIBUTE_REQUESTOR));
    }

    @Test
    public void testGetParamsReturnsBasicContextParamsFromProperty() throws Exception {
        // GIVEN
        Property jcrProperty = MgnlContext.getJCRSession("website").getNode("/parent/sub").setProperty("property1", "property1");
        AbstractCommandAction<CommandActionDefinition> action =
                new AbstractCommandAction<CommandActionDefinition>(
                        new CommandActionDefinition(),
                        new JcrPropertyAdapter(jcrProperty),
                        commandsManager,
                        null,
                        null);

        // WHEN
        action.setCurrentItem(action.getItems().get(0));
        action.onPreExecute();
        Map<String, Object> params = action.getParams();

        // THEN
        assertTrue(params.containsKey(Context.ATTRIBUTE_REPOSITORY));
        assertTrue(params.containsKey(Context.ATTRIBUTE_PATH));
        assertTrue(params.containsKey(Context.ATTRIBUTE_UUID));
        assertTrue(params.containsKey(Context.ATTRIBUTE_REQUESTOR));

        assertEquals("website", params.get(Context.ATTRIBUTE_REPOSITORY));
        assertEquals(jcrProperty.getPath(), params.get(Context.ATTRIBUTE_PATH));
        // In case of property, the Identifier is the parent Node ID
        assertEquals("2", params.get(Context.ATTRIBUTE_UUID));
        assertEquals(TEST_USER, params.get(Context.ATTRIBUTE_REQUESTOR));
    }

    @Test
    public void testGetParamsReturnsOtherParamsFromDefinition() throws Exception {

        // GIVEN
        CommandActionDefinition definition = new CommandActionDefinition();
        definition.getParams().put("abc", "bar");
        definition.getParams().put("def", "baz");
        definition.getParams().put("ghi", "456");

        AbstractCommandAction<CommandActionDefinition> action =
                new AbstractCommandAction<CommandActionDefinition>(
                        definition,
                        new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent/sub")),
                        commandsManager,
                        null,
                        i18n);

        // WHEN
        action.setCurrentItem(action.getItems().get(0));
        action.onPreExecute();
        Map<String, Object> params = action.getParams();

        // THEN
        assertEquals("bar", params.get("abc"));
        assertEquals("baz", params.get("def"));
        assertEquals("456", params.get("ghi"));

        // ensure the default params are returned as well.
        assertEquals("website", params.get(Context.ATTRIBUTE_REPOSITORY));
        assertEquals("/parent/sub", params.get(Context.ATTRIBUTE_PATH));
        assertEquals("2", params.get(Context.ATTRIBUTE_UUID));
        assertEquals(TEST_USER, params.get(Context.ATTRIBUTE_REQUESTOR));
    }

    @Test
    public void testExecute() throws Exception {
        // GIVEN
        CommandActionDefinition definition = new CommandActionDefinition();
        definition.setCommand("qux");

        QuxCommand quxCommand = new QuxCommand();
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "qux")).thenReturn(quxCommand);
        when(commandsManager.getCommand("qux")).thenReturn(quxCommand);

        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(
                definition,
                new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent")),
                commandsManager,
                null,
                i18n);

        // WHEN
        action.execute();

        // THEN
        verify(commandsManager, times(1)).executeCommand(quxCommand, action.getParams());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetParamsReturnsImmutableMap() throws Exception {
        // GIVEN
        JcrNodeAdapter item = new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent/sub"));
        AbstractCommandAction<CommandActionDefinition> action = new AbstractCommandAction<CommandActionDefinition>(
                new CommandActionDefinition(), item, commandsManager, null, null);
        action.setCurrentItem(action.getItems().get(0));
        action.buildParams(item.getJcrItem());

        action.onPreExecute();
        Map<String, Object> params = action.getParams();

        // WHEN
        params.put("foo", "bar");

        // THEN exception
    }

    @Test
    public void testExecuteTwiceReturnsNewMap() throws Exception {
        // GIVEN
        CommandActionDefinition definition = new CommandActionDefinition();
        definition.setCommand("qux");
        Map<String, Object> params1 = new HashMap<String, Object>();
        params1.put("param1", "some parameter1");

        Map<String, Object> params2 = new HashMap<String, Object>();
        params2.put("param2", "some parameter2");

        QuxCommand quxCommand = new QuxCommand();
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "qux")).thenReturn(quxCommand);
        when(commandsManager.getCommand("qux")).thenReturn(quxCommand);

        AbstractCommandAction<CommandActionDefinition> action = new TestAbstractCommandAction(
                definition,
                new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent")),
                commandsManager,
                null, i18n, params1);

        action.execute();
        // WHEN
        AbstractCommandAction<CommandActionDefinition> action2 = new TestAbstractCommandAction(
                definition,
                new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent")),
                commandsManager,
                null, i18n, params2);

        action2.execute();

        // THEN
        assertNull(action2.getParams().get("param1"));
        assertEquals(action2.getParams().get("param2"), params2.get("param2"));
    }

    @Test
    public void testInvokeAsynchronously() throws RepositoryException, ActionExecutionException {
        // GIVEN
        CommandActionDefinition definition = new CommandActionDefinition();
        definition.setCommand("asynchronous");
        QuxCommand quxCommand = new QuxCommand();
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "asynchronous")).thenReturn(quxCommand);

        definition.setAsynchronous(true);

        JcrNodeAdapter item = new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent/sub"));

        AbstractCommandAction<CommandActionDefinition> action = new TestAbstractCommandAction(
                definition,
                item,
                commandsManager,
                null, i18n, new HashMap<String, Object>());

        action.setCurrentItem(item);

        // WHEN
        action.executeOnItem(item);

        // THEN
        assertFalse("Stop Processing = false as it's invoke asynchronously", (Boolean) MgnlContext.getAttribute(AbstractCommandAction.COMMAND_RESULT, Context.LOCAL_SCOPE));
    }

    @Test
    public void testInvokeAsynchronouslyActionThatTakesLongerThanFiveSeconds() throws RepositoryException, ActionExecutionException, SchedulerException {
        // GIVEN
        JobExecutionContext jobExecutionContext = mock(JobExecutionContext.class);
        JobDetail jobDetail = new JobDetail();
        jobDetail.setName("UI Action triggered execution of [default:asynchronous] by user ["+TEST_USER+"]. (0)");
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        CommandActionDefinition definition = new CommandActionDefinition();
        definition.setCommand("asynchronous");
        QuxCommand quxCommand = new QuxCommand();
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "asynchronous")).thenReturn(quxCommand);
        when(scheduler.getCurrentlyExecutingJobs()).thenReturn(Arrays.asList(jobExecutionContext));

        definition.setAsynchronous(true);

        JcrNodeAdapter item = new JcrNodeAdapter(MgnlContext.getJCRSession("website").getNode("/parent/sub"));

        AbstractCommandAction<CommandActionDefinition> action = new TestAbstractCommandAction(
                definition,
                item,
                commandsManager,
                null, i18n, new HashMap<String, Object>());

        action.setCurrentItem(item);

        // WHEN
        action.executeOnItem(item);

        // THEN
        assertEquals("ui-framework.abstractcommand.asyncaction.long", definition.getSuccessMessage());
    }

    private static final class QuxCommand extends MgnlCommand {

        @Override
        public boolean execute(Context context) throws Exception {
            return false;
        }
    }

    private class TestAbstractCommandAction extends AbstractCommandAction<CommandActionDefinition> {

        private Map<String, Object> parameter;

        public TestAbstractCommandAction(CommandActionDefinition definition, JcrItemAdapter item, CommandsManager commandsManager, UiContext uiContext, SimpleTranslator i18n, Map<String, Object> parameter) {
            super(definition, item, commandsManager, uiContext, i18n);
            this.parameter = parameter;
        }

        @Override
        protected Map<String, Object> buildParams(Item jcrItem) {
            Map<String, Object> params = super.buildParams(jcrItem);
            params.putAll(parameter);
            return params;
        }
    }

    private class SchedulerModule extends ModuleDefinition {

        public Scheduler getScheduler() {
            return null;
        }
    }

}
