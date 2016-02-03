/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.admincentral.dialog.action;

import static org.junit.Assert.assertTrue;

import info.magnolia.cms.security.MgnlUser;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.Event;
import info.magnolia.event.EventBus;
import info.magnolia.event.EventHandler;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Item;

/**
 * Main test class for {@link SaveConfigDialogAction} and {@link SaveConfigDialogActionDefinition}.
 */
public class SaveConfigDialogActionTest extends MgnlTestCase {

    private SaveConfigDialogAction dialogAction;
    private SaveConfigDialogActionDefinition dialogActionDefinition;
    private CallbackDialogActionTest.FormDialogPresenterTest presenter;
    private Node node;
    private Item item;
    private MockSession session;
    private TestEventBus subAppEventBus;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        dialogActionDefinition = new SaveConfigDialogActionDefinition();
        dialogActionDefinition.setLabel("label");
        dialogActionDefinition.setName("name");

        presenter = new CallbackDialogActionTest.FormDialogPresenterTest();
        // Init Node
        session = new MockSession(RepositoryConstants.WEBSITE);
        MockContext ctx = new MockContext();
        ctx.setUser(new MgnlUser("userName", "realm", new ArrayList<String>(), new ArrayList<String>(), new HashMap<String, String>()));
        ctx.addSession(RepositoryConstants.WEBSITE, session);
        MgnlContext.setInstance(ctx);

        subAppEventBus = new TestEventBus();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        dialogActionDefinition = null;
        presenter = null;
        session = null;
        subAppEventBus = null;
        MgnlContext.setInstance(null);
    }

    /**
     * Test if an event is sent when the action is executed.
     */
    @Test
    public void testExecuteDefaultExecute() throws RepositoryException, ActionExecutionException {
        // GIVEN
        node = session.getRootNode().addNode("underlying");
        node.setProperty("test", "test");
        node.getSession().save();
        item = new JcrPropertyAdapter(node.getProperty("test"));
        item.getItemProperty("jcrName").setValue("1");
        dialogAction = new SaveConfigDialogAction(dialogActionDefinition, item, presenter, presenter.getCallback(), subAppEventBus);

        // WHEN
        dialogAction.execute();

        // THEN
        assertTrue(subAppEventBus.getEventFired());
    }

    /**
     * Helper so that we can test if an event was fired on an eventbus.
     */
    private class TestEventBus implements EventBus {

        private boolean eventFired = false;

        public boolean getEventFired() {
            return eventFired;
        }

        @Override
        public <H extends EventHandler> HandlerRegistration addHandler(Class<? extends Event<H>> eventClass, H handler) {
            // Do Nothing for the moment.
            return null;
        }

        @Override
        public <H extends EventHandler> void fireEvent(Event<H> event) {
            eventFired = true;
        }
    }

}
