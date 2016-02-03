/**
 * This file Copyright (c) 2010-2016 Magnolia International
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

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.framework.app.SubAppContextImpl;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Test extending the {@link ActivationAction} with parameters passed to constructor.
 */
public class ExtendableActivationActionTest extends MgnlTestCase {

    private MockSession session;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = new MockSession("workspace");
        MockContext ctx = new MockContext();
        ctx.addSession("workspace", session);
        MgnlContext.setInstance(ctx);
    }

    @Test
    public void testExtendingParameters() throws Exception {
        // GIVEN
        Node item = session.getRootNode().addNode("node1");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key1", true);
        params.put("key2", "value2");

        // WHEN
        ActivationAction action = new ExtendableActivationAction(new ActivationActionDefinition(), new JcrNodeAdapter(item), params, mock(CommandsManager.class), mock(EventBus.class), mock(SubAppContextImpl.class), mock(ModuleRegistry.class));
        action.onPreExecute();

        // THEN
        assertTrue((Boolean) action.getParams().get("key1"));
        assertEquals("value2", action.getParams().get("key2"));
        assertNull(action.getParams().get("key3"));
    }

    @Test
    public void testExtendingParamsWithParamsFromDefinition() throws Exception {
        // GIVEN
        ActivationActionDefinition definition = new ActivationActionDefinition();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key1", true);
        params.put("key2", "value2");
        definition.setParams(params);

        Map<String, Object> paramsCtor = new HashMap<String, Object>();
        paramsCtor.put("key3", false);
        paramsCtor.put("key4", "value4");

        Node item = session.getRootNode().addNode("node1");

        // WHEN
        ActivationAction action = new ExtendableActivationAction(definition, new JcrNodeAdapter(item), paramsCtor, mock(CommandsManager.class), mock(EventBus.class), mock(SubAppContextImpl.class), mock(ModuleRegistry.class));
        action.onPreExecute();

        // THEN
        assertTrue((Boolean) action.getParams().get("key1"));
        assertEquals("value2", action.getParams().get("key2"));
        assertFalse((Boolean) action.getParams().get("key3"));
        assertEquals("value4", action.getParams().get("key4"));
        assertNull(action.getParams().get("key5"));
    }
}
