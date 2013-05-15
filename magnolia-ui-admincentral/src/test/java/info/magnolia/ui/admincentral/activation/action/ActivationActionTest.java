/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.admincentral.activation.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Main test class for Activation Action.
 */
public class ActivationActionTest {
    private String website =
            "/foo.uuid=1\n" +
                    "/foo/bar.uuid=2";

    private MockSession session;
    private CommandsManager commandsManager;
    private Command activationCommand;
    private ActivationActionDefinition definition;
    private Map<String, Object> params = new HashMap<String, Object>();

    @Before
    public void setUp() throws Exception {
        session = SessionTestUtil.createSession("website", website);

        Context ctx = mock(Context.class);
        when(ctx.getJCRSession("website")).thenReturn(session);
        MgnlContext.setInstance(ctx);

        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getJCRSession("website")).thenReturn(session);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);

        commandsManager = mock(CommandsManager.class);

        definition = new ActivationActionDefinition(mock(ModuleRegistry.class));
        definition.setCommand("activate");

        activationCommand = mock(Command.class);

        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "activate")).thenReturn(activationCommand);
        when(commandsManager.getCommand("activate")).thenReturn(activationCommand);

    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        params.clear();
    }

    @Test(expected = ExchangeException.class)
    public void testGetExchangeException() throws Exception {
        // GIVEN
        ActivationAction action = new ActivationAction(definition, new JcrNodeAdapter(session.getNode("foo")), commandsManager, mock(EventBus.class), mock(SubAppContextImpl.class));
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
        ActivationAction action = new ActivationAction(definition, new JcrNodeAdapter(session.getNode("foo")), commandsManager, mock(EventBus.class), mock(SubAppContextImpl.class));

        // THEN
        assertTrue((Boolean) action.getParams().get(Context.ATTRIBUTE_RECURSIVE));
    }
}
