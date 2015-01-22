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

import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
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
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Main test class for Activation Action.
 */
public class ActivationActionTest extends RepositoryTestCase {
    private String website =
            "/foo.uuid=1\n" +
                    "/foo/bar.uuid=2";

    private MockSession session;
    private CommandsManager commandsManager;
    private Command activationCommand;
    private ActivationActionDefinition definition;
    private Map<String, Object> params = new HashMap<String, Object>();
    public static final String TEST_USER = "phantomas";

    @Override
    @Before
    public void setUp() throws Exception {
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);

        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);
        ComponentsTestUtil.setImplementation(ModuleRegistry.class, ModuleRegistryImpl.class);

        session = SessionTestUtil.createSession("website", website);
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        MockContext ctx = new MockContext();
        ctx.addSession("website", session);
        ctx.setLocale(new Locale("en"));
        MgnlContext.setInstance(ctx);
        final User user = mock(User.class);
        when(user.getName()).thenReturn(TEST_USER);
        final SecuritySupportImpl sec = new info.magnolia.cms.security.SecuritySupportImpl();
        ComponentsTestUtil.setInstance(SecuritySupport.class, sec);
        ctx.setUser(user);

        commandsManager = mock(CommandsManager.class);

        definition = new ActivationActionDefinition();
        definition.setCommand("activate");

        activationCommand = mock(Command.class);

        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "activate")).thenReturn(activationCommand);
        when(commandsManager.getCommand("workflow", "activate")).thenReturn(activationCommand);
        when(commandsManager.getCommand("activate")).thenReturn(activationCommand);

    }

    @Override
    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        params.clear();
    }

    @Test(expected = ExchangeException.class)
    public void testGetExchangeException() throws Exception {
        // GIVEN
        ActivationAction action = new ActivationAction(definition, new JcrNodeAdapter(session.getNode("foo")), commandsManager, mock(EventBus.class), mock(SubAppContextImpl.class), mock(SimpleTranslator.class));
        // for some reason we need to call this twice else no exception is thrown
        when(commandsManager.executeCommand("activate", params)).thenThrow(new ExchangeException());
        when(commandsManager.executeCommand("activate", params)).thenThrow(new ExchangeException());

        // WHEN
        action.execute();

        // THEN ExchangeException
    }

    @Test
    public void testBuildParamsSetsRecursiveParameter() throws Exception {
        // GIVEN
        definition.setRecursive(true);

        // WHEN
        ActivationAction action = new ActivationAction(definition, new JcrNodeAdapter(session.getNode("foo")), commandsManager, mock(EventBus.class), mock(SubAppContextImpl.class), mock(SimpleTranslator.class));
        action.setCurrentItem((JcrItemAdapter) action.getItems().get(0));
        action.onPreExecute();

        // THEN
        assertTrue((Boolean) action.getParams().get(Context.ATTRIBUTE_RECURSIVE));
    }

    @Test
    public void testBuildParamsSetsChangedOnlyParameter() throws Exception {
        // GIVEN
        definition.setModifiedOnly(true);

        // WHEN
        ActivationAction action = new ActivationAction(definition, new JcrNodeAdapter(session.getNode("foo")), commandsManager, mock(EventBus.class), mock(SubAppContextImpl.class), mock(SimpleTranslator.class));
        action.setCurrentItem((JcrItemAdapter) action.getItems().get(0));
        action.onPreExecute();

        // THEN
        assertTrue((Boolean) action.getParams().get(ActivationAction.ATTRIBUTE_MODIFIEDONLY));
    }

    // TODO remove Ignore - now due to missing bundle
    @Ignore
    @Test
    public void testBasicSuccessMessage() throws Exception {
        // GIVEN
        when(commandsManager.executeCommand("activate", params)).thenReturn(false);
        TestSubAppContext testCtx = new TestSubAppContext();
        ActivationAction action = new ActivationAction(definition, new JcrNodeAdapter(session.getNode("foo")), commandsManager, mock(EventBus.class), testCtx, mock(SimpleTranslator.class));

        // WHEN
        action.execute();

        // THEN
        assertEquals("Publication has been started.", testCtx.message);
    }

    private static class TestSubAppContext extends SubAppContextImpl {

        public String message;

        public TestSubAppContext() {
            super(mock(SubAppDescriptor.class), mock(Shell.class));
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, String title) {
            this.message = title;
        }
    }
}
