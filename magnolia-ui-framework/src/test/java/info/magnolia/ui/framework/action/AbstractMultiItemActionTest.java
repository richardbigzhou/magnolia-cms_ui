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
package info.magnolia.ui.framework.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.i18nsystem.ContextLocaleProvider;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.TranslationServiceImpl;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.Node2BeanTransformer;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;

import org.junit.Before;
import org.junit.Test;

/**
 * Basic test for the AbstractMultiItemAction.
 */
public class AbstractMultiItemActionTest extends MgnlTestCase {

    private List<JcrItemAdapter> items = new ArrayList<JcrItemAdapter>();
    private Node root;

    @Override
    @Before
    public void setUp() throws Exception {
        MockSession session = new MockSession("test");

        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getJCRSession("test")).thenReturn(session);
        MgnlContext.setInstance(systemContext);

        root = session.getRootNode();
        Node node = root.addNode("node");
        Property prop = node.setProperty("property", "value");

        items.add(new JcrNodeAdapter(node));
        items.add(new JcrPropertyAdapter(prop));

        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);
    }

    @Test
    public void testSuccessAction() throws Exception {
        // GIVEN
        UiContext uiContext = mock(UiContext.class);
        SuccessAction action = new SuccessAction(new ConfiguredActionDefinition(), items, uiContext);

        // WHEN
        action.execute();

        // THEN
        verify(uiContext).openNotification(MessageStyleTypeEnum.INFO, true, "Success.");
    }

    @Test
    public void testgetSortedItems() throws Exception {
        // GIVEN
        items.clear();
        Node b = root.addNode("b");
        Node b_b = b.addNode("b_b");
        Property p_b_b = b_b.setProperty("p_b_b", "p_b_b");
        addItems(new JcrNodeAdapter(b), new JcrPropertyAdapter(p_b_b), new JcrNodeAdapter(b_b));
        UiContext uiContext = mock(UiContext.class);
        SuccessAction action = new SuccessAction(new ConfiguredActionDefinition(), items, uiContext);

        // WHEN
        List<JcrItemAdapter> res = action.getSortedItems(action.getItemComparator());

        // THEN
        assertNotNull(res);
        assertEquals(3, res.size());
        assertEquals("p_b_b", res.get(0).getJcrItem().getName());
        assertEquals("b_b", res.get(1).getJcrItem().getName());
        assertEquals("b", res.get(2).getJcrItem().getName());

    }

    @Test
    public void testErrorAction() throws Exception {
        // GIVEN
        UiContext uiContext = mock(UiContext.class);
        ErrorAction action = new ErrorAction(new ConfiguredActionDefinition(), items, uiContext);

        // WHEN
        action.execute();

        // THEN
        verify(uiContext).openNotification(MessageStyleTypeEnum.ERROR, false, "Failure.<ul><li><b>/node@property</b>: Problem.</li><li><b>/node</b>: Problem.</li></ul>");
    }

    private void addItems(JcrItemAdapter... adapters) {
        items.addAll(Arrays.asList(adapters));
    }

    private static class SuccessAction extends AbstractMultiItemAction<ConfiguredActionDefinition> {

        protected SuccessAction(ConfiguredActionDefinition definition, List<JcrItemAdapter> items, UiContext uiContext) {
            super(definition, items, uiContext);
        }

        @Override
        protected void executeOnItem(JcrItemAdapter item) throws Exception {
            // nothing to do
        }

        @Override
        protected String getSuccessMessage() {
            return "Success.";
        }

        @Override
        protected String getFailureMessage() {
            return "Failure.";
        }

    }

    private static class ErrorAction extends AbstractMultiItemAction<ConfiguredActionDefinition> {

        protected ErrorAction(ConfiguredActionDefinition definition, List<JcrItemAdapter> items, UiContext uiContext) {
            super(definition, items, uiContext);
        }

        @Override
        protected void executeOnItem(JcrItemAdapter item) throws Exception {
            throw new Exception("Problem.");
        }

        @Override
        protected String getSuccessMessage() {
            return "Success.";
        }

        @Override
        protected String getFailureMessage() {
            return "Failure.";
        }

    }
}
