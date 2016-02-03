/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.overlay.AlertCallback;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.overlay.NotificationCallback;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test covering execution of {@link ConfirmationAction}.
 */
public class ConfirmationActionTest extends MgnlTestCase {

    public static final String SUCCESS_ACTION = "successAction";
    public static final String CANCEL_ACTION = "cancelAction";
    private ConfirmationActionDefinition definition;
    private SimpleActionExecutor actionExecutor;
    private Session session;
    private Messages messages;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        session = new MockSession("workspace");
        MockContext ctx = new MockContext();
        ctx.addSession("workspace", session);
        MgnlContext.setInstance(ctx);

        MessagesManager messagesManager = mock(MessagesManager.class);
        messages = mock(Messages.class);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
        when(messagesManager.getMessages()).thenReturn(messages);


        this.definition = new ConfirmationActionDefinition();
        definition.setSuccessActionName(SUCCESS_ACTION);
        definition.setCancelActionName(CANCEL_ACTION);

        this.actionExecutor = new SimpleActionExecutor();
        ComponentsTestUtil.setInstance(ActionExecutor.class, actionExecutor);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testProceedNodeAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node1");

        ConfirmationAction confirmationAction = new ConfirmationAction(definition, new JcrNodeAdapter(node), new TestUiContext(true), actionExecutor);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), SUCCESS_ACTION);
    }

    @Test
    public void testCancelNodeAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node2");

        ConfirmationAction confirmationAction = new ConfirmationAction(definition, new JcrNodeAdapter(node), new TestUiContext(false), actionExecutor);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), CANCEL_ACTION);
    }

    @Test
    public void testProceedPropertyAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node2");
        node.setProperty("property_long", Long.decode("1000"));

        JcrItemAdapter item = new JcrPropertyAdapter(node.getProperty("property_long"));
        ConfirmationAction confirmationAction = new ConfirmationAction(definition, item, new TestUiContext(true), actionExecutor);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), SUCCESS_ACTION);
    }

    @Test
    public void testCancelPropertyAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node2");
        node.setProperty("property_long", Long.decode("1000"));

        JcrItemAdapter item = new JcrPropertyAdapter(node.getProperty("property_long"));
        ConfirmationAction confirmationAction = new ConfirmationAction(definition, item, new TestUiContext(false), actionExecutor);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), CANCEL_ACTION);
    }

    /**
     * Basic Empty implementation of {@link info.magnolia.ui.api.context.UiContext} for test purpose.
     */
    public static class TestUiContext implements UiContext {
        private boolean validateChanges;

        public TestUiContext(boolean validateChanges) {
            this.validateChanges = validateChanges;
        }

        @Override
        public OverlayCloser openOverlay(View view) {
            return null;
        }

        @Override
        public OverlayCloser openOverlay(View view, ModalityLevel modalityLevel) {
            return null;
        }

        @Override
        public void openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb) {
        }

        @Override
        public void openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        }

        @Override
        public void openConfirmation(MessageStyleType type, View viewToShow, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        }

        @Override
        public void openConfirmation(MessageStyleType type, String title, String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
            if (validateChanges) {
                cb.onSuccess();
            } else {
                cb.onCancel();
            }
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, View viewToShow) {
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, String title) {
        }

        @Override
        public void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, NotificationCallback cb) {
        }

    }

    /**
     * Simple ActionExecutor keeping name of last executed action.
     */
    public class SimpleActionExecutor implements ActionExecutor {

        private String actionName;

        @Override
        public void execute(String actionName, Object... args) throws ActionExecutionException {
            this.actionName = actionName;
        }

        @Override
        public ActionDefinition getActionDefinition(String actionName) {
            return null;
        }

        @Override
        public boolean isAvailable(String actionName, Item... items) {
            return false;
        }

        public String getExecutedActionName() {
            return actionName;
        }
    }
}
