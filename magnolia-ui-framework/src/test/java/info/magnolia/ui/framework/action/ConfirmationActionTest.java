/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.ContextLocaleProvider;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
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
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test covering execution of {@link ConfirmationAction}.
 */
public class ConfirmationActionTest extends MgnlTestCase {

    public static final String SUCCESS_ACTION = "successAction";
    public static final String CANCEL_ACTION = "cancelAction";
    private ConfirmationActionDefinition definition;
    private SimpleActionExecutor actionExecutor;
    private SimpleTranslator i18n;
    private Session session;
    private ContentConnector contentConnector;
    private ConfirmationAction confirmationAction;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);
        contentConnector = mock(ContentConnector.class);
        session = new MockSession("workspace");
        MockContext ctx = new MockContext();
        ctx.addSession("workspace", session);
        ctx.setLocale(Locale.ENGLISH);
        MgnlContext.setInstance(ctx);

        this.definition = new ConfirmationActionDefinition();
        definition.setSuccessActionName(SUCCESS_ACTION);
        definition.setCancelActionName(CANCEL_ACTION);
        definition.setConfirmationHeader("foo");
        definition.setConfirmationMessage("bar");

        i18n = mock(SimpleTranslator.class);

        this.actionExecutor = new SimpleActionExecutor();
        ComponentsTestUtil.setInstance(ActionExecutor.class, actionExecutor);
        confirmationAction = new ConfirmationAction(definition, new ArrayList<JcrItemAdapter>(), uiContext(false), actionExecutor, i18n, contentConnector);
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

        ConfirmationAction confirmationAction = new ConfirmationAction(definition, new JcrNodeAdapter(node), uiContext(true), actionExecutor, i18n, contentConnector);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), SUCCESS_ACTION);
        assertTrue(actionExecutor.getArgument() instanceof JcrItemAdapter);
    }

    @Test
    public void testCancelNodeAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node2");

        ConfirmationAction confirmationAction = new ConfirmationAction(definition, new JcrNodeAdapter(node), uiContext(false), actionExecutor, i18n, contentConnector);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), CANCEL_ACTION);
        assertTrue(actionExecutor.getArgument() instanceof JcrItemAdapter);
    }

    @Test
    public void testProceedPropertyAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node2");
        node.setProperty("property_long", Long.decode("1000"));

        JcrItemAdapter item = new JcrPropertyAdapter(node.getProperty("property_long"));
        ConfirmationAction confirmationAction = new ConfirmationAction(definition, item, uiContext(true), actionExecutor, i18n, contentConnector);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), SUCCESS_ACTION);
        assertTrue(actionExecutor.getArgument() instanceof JcrItemAdapter);
    }

    @Test
    public void testCancelPropertyAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node2");
        node.setProperty("property_long", Long.decode("1000"));

        JcrItemAdapter item = new JcrPropertyAdapter(node.getProperty("property_long"));
        ConfirmationAction confirmationAction = new ConfirmationAction(definition, item, uiContext(false), actionExecutor, i18n, contentConnector);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), CANCEL_ACTION);
        assertTrue(actionExecutor.getArgument() instanceof JcrItemAdapter);
    }

    @Test
    public void testProceedMultipleAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node1");
        node.setProperty("property_long", Long.decode("1000"));

        JcrItemAdapter item = new JcrNodeAdapter(node);
        JcrItemAdapter prop = new JcrPropertyAdapter(node.getProperty("property_long"));

        List<JcrItemAdapter> items = new ArrayList<JcrItemAdapter>(2);
        items.add(item);
        items.add(prop);

        ConfirmationAction confirmationAction = new ConfirmationAction(definition, items, uiContext(true), actionExecutor, i18n, contentConnector);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), SUCCESS_ACTION);
        assertTrue(actionExecutor.getArgument() instanceof List);
        assertEquals("/node1", JcrItemUtil.getItemPath(((List<JcrItemAdapter>) actionExecutor.getArgument()).get(0).getJcrItem()));
        assertEquals("/node1@property_long", JcrItemUtil.getItemPath(((List<JcrItemAdapter>) actionExecutor.getArgument()).get(1).getJcrItem()));
    }

    @Test
    public void testCancelMultipleAction() throws Exception {
        // GIVEN
        Node root = session.getRootNode();
        Node node = root.addNode("node1");
        node.setProperty("property_long", Long.decode("1000"));

        JcrItemAdapter item = new JcrNodeAdapter(node);
        JcrItemAdapter prop = new JcrPropertyAdapter(node.getProperty("property_long"));

        List<JcrItemAdapter> items = new ArrayList<JcrItemAdapter>(2);
        items.add(item);
        items.add(prop);

        ConfirmationAction confirmationAction = new ConfirmationAction(definition, items, uiContext(false), actionExecutor, i18n, contentConnector);

        // WHEN
        confirmationAction.execute();

        // THEN
        assertEquals(actionExecutor.getExecutedActionName(), CANCEL_ACTION);
        assertTrue(actionExecutor.getArgument() instanceof List);
        assertEquals("/node1", JcrItemUtil.getItemPath(((List<JcrItemAdapter>) actionExecutor.getArgument()).get(0).getJcrItem()));
        assertEquals("/node1@property_long", JcrItemUtil.getItemPath(((List<JcrItemAdapter>) actionExecutor.getArgument()).get(1).getJcrItem()));
    }

    @Test
    public void testConfirmationActionShouldNotFailOnInvalidI18nMessage() throws Exception {
        //GIVEN
        definition.setConfirmationHeader("There'{0,choice,0#re no files|1#s one file|1<re {0} files}.");

        //WHEN
        String s = confirmationAction.getConfirmationHeader();

        //THEN
        // should not throw IllegalArgumentException
        assertEquals("There{0,choice,0#re no files|1#s one file|1<re {0} files}.", s);
    }

    private UiContext uiContext(final boolean checkValidation) {
        final UiContext uiContext = mock(UiContext.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ConfirmationCallback callback = (ConfirmationCallback) invocation.getArguments()[6];
                if (checkValidation) {
                    callback.onSuccess();
                } else {
                    callback.onCancel();
                }
                return null;
            }
        }).when(uiContext).openConfirmation(any(MessageStyleType.class), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(ConfirmationCallback.class));
        return uiContext;
    }

    /**
     * Simple ActionExecutor keeping name of last executed action.
     */
    public class SimpleActionExecutor implements ActionExecutor {

        private String actionName;
        private Object argument;

        @Override
        public void execute(String actionName, Object... args) throws ActionExecutionException {
            this.actionName = actionName;
            this.argument = args[0];
        }

        @Override
        public ActionDefinition getActionDefinition(String actionName) {
            return null;
        }

        public String getExecutedActionName() {
            return actionName;
        }

        public Object getArgument() {
            return this.argument;
        }
    }
}
