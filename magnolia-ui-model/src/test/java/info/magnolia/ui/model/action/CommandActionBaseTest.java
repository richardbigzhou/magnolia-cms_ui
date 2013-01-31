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
package info.magnolia.ui.model.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.util.Map;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for CommandActionBaseTest.
 */
public class CommandActionBaseTest {
    private String website =
            "/parent.uuid=1\n" +
                    "/parent/sub.uuid=2";
    private MockSession session;

    public static class TestAction extends CommandActionBase<CommandActionDefinition> {


        public TestAction(CommandActionDefinition definition, Node node, CommandsManager commandsManager) {
            super(definition, node, commandsManager);
        }

        @Override
        public void execute() throws ActionExecutionException {
        }
    }

    @Before
    public void setUp() throws Exception {
        session = SessionTestUtil.createSession("website", website);

        WebContext webContext = mock(WebContext.class);
        when(webContext.getContextPath()).thenReturn("/foo");
        when(webContext.getJCRSession("website")).thenReturn(session);
        MgnlContext.setInstance(webContext);

        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getJCRSession("website")).thenReturn(session);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);

        Node2BeanProcessor node2BeanProcessor = mock(Node2BeanProcessor.class);
        ComponentsTestUtil.setInstance(Node2BeanProcessor.class, node2BeanProcessor);
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
        TestAction action = new TestAction(new CommandActionDefinition(), session.getNode("/parent/sub"), Components.getComponent(CommandsManager.class));

        // WHEN
        Map<String, Object> params = action.getParams();

        // THEN
        assertTrue(params.containsKey(Context.ATTRIBUTE_REPOSITORY));
        assertTrue(params.containsKey(Context.ATTRIBUTE_PATH));
        assertTrue(params.containsKey(Context.ATTRIBUTE_UUID));

        assertEquals("website", params.get(Context.ATTRIBUTE_REPOSITORY));
        assertEquals("/parent/sub", params.get(Context.ATTRIBUTE_PATH));
        assertEquals("2", params.get(Context.ATTRIBUTE_UUID));
    }

    @Test
    public void testGetParamsReturnsOtherParamsFromDefinition() throws Exception {

        // GIVEN
        CommandActionDefinition definition = new CommandActionDefinition();
        definition.getParams().put("abc", "bar");
        definition.getParams().put("def", "baz");
        definition.getParams().put("ghi", "456");

        TestAction action = new TestAction(definition, session.getNode("/parent/sub"), Components.getComponent(CommandsManager.class));

        // WHEN
        Map<String, Object> params = action.getParams();

        // THEN
        assertEquals("bar", params.get("abc"));
        assertEquals("baz", params.get("def"));
        assertEquals("456", params.get("ghi"));

        // ensure the default params are returned as well.
        assertEquals("website", params.get(Context.ATTRIBUTE_REPOSITORY));
        assertEquals("/parent/sub", params.get(Context.ATTRIBUTE_PATH));
        assertEquals("2", params.get(Context.ATTRIBUTE_UUID));
    }

    @Test
    public void testGetCommandsManager() throws Exception {
        // GIVEN
        TestAction action = new TestAction(new CommandActionDefinition(), session.getNode("/parent/sub"), Components.getComponent(CommandsManager.class));

        // WHEN
        CommandsManager manager = action.getCommandsManager();

        // THEN
        assertNotNull(manager);
        assertTrue(manager instanceof CommandsManager);
    }
}
