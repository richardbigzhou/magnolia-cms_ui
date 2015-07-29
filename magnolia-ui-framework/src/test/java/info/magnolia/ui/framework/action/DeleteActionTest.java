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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.DeleteCommand;
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
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.action.async.AsyncActionExecutor;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link info.magnolia.ui.framework.action.DeleteAction}.
 */
public class DeleteActionTest extends RepositoryTestCase {
    private CommandsManager commandsManager;
    private DeleteActionDefinition definition;
    private Map<String, Object> params = new HashMap<String, Object>();
    private DeleteCommand deleteCommand;
    private Node referenceNode;
    private EventBus eventBus;
    private SimpleTranslator i18n;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);
        ComponentsTestUtil.setImplementation(TranslationService.class, TranslationServiceImpl.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        ComponentsTestUtil.setImplementation(Node2BeanProcessor.class, Node2BeanProcessorImpl.class);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
        ComponentsTestUtil.setImplementation(Node2BeanTransformer.class, Node2BeanTransformerImpl.class);
        ComponentsTestUtil.setImplementation(LocaleProvider.class, ContextLocaleProvider.class);
        ComponentsTestUtil.setImplementation(AsyncActionExecutor.class, AbstractCommandActionTest.DummyAsyncExecutor.class);

        MessagesManager messagesManager = mock(MessagesManager.class);
        Messages messages = mock(Messages.class);
        ComponentsTestUtil.setInstance(MessagesManager.class, messagesManager);
        when(messagesManager.getMessages(anyString(), any(Locale.class))).thenReturn(messages);
        when(messages.get(anyString())).thenReturn("Some translated string");

        // Init Command
        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        referenceNode = webSiteSession.getRootNode().addNode("referenceNode");
        referenceNode.addNode("article1", NodeTypes.Page.NAME);
        referenceNode.getNode("article1").setProperty("property_boolean", Boolean.TRUE);
        referenceNode.getNode("article1").setProperty("property_long", Long.decode("1000"));
        referenceNode.getNode("article1").setProperty("property_string", "property");
        referenceNode.addNode("article2", NodeTypes.Page.NAME);
        referenceNode.getNode("article2").setProperty("property_boolean", Boolean.TRUE);
        referenceNode.getNode("article2").setProperty("property_long", Long.decode("1000"));
        referenceNode.getNode("article2").setProperty("property_string", "property");

        deleteCommand = new DeleteCommand();

        // Init Action and CommandManager
        definition = new DeleteActionDefinition();

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);

        // see for why this is needed.
        ComponentsTestUtil.setInstance(Map.class, params);

        CommandsManager commandsManagerTmp = Components.getComponent(CommandsManager.class);
        Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node exportModuleDef = configSession.getRootNode().addNode("modules", NodeTypes.ContentNode.NAME).addNode("commands", NodeTypes.ContentNode.NAME)
                .addNode("default", NodeTypes.ContentNode.NAME).addNode("delete", NodeTypes.ContentNode.NAME);
        exportModuleDef.setProperty("class", DeleteCommand.class.getName());
        exportModuleDef.getSession().save();
        commandsManagerTmp.register(ContentUtil.asContent(exportModuleDef.getParent()));
        commandsManager = spy(commandsManagerTmp);
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "delete")).thenReturn(deleteCommand);
        when(commandsManager.getCommand("delete")).thenReturn(deleteCommand);

        eventBus = mock(EventBus.class);
        i18n = mock(SimpleTranslator.class);
    }

    @Test
    public void testDeleteNode() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(referenceNode.getNode("article1"));
        DeleteAction<DeleteActionDefinition> deleteAction = new DeleteAction<DeleteActionDefinition>(definition, item, commandsManager, eventBus, mock(UiContext.class), i18n);

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article2"));
        assertFalse(referenceNode.hasNode("article1"));
    }

    @Test
    public void testDeleteProperty() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrPropertyAdapter(referenceNode.getNode("article1").getProperty("property_long"));
        DeleteAction<DeleteActionDefinition> deleteAction = new DeleteAction<DeleteActionDefinition>(definition, item, commandsManager, eventBus, mock(UiContext.class), i18n);

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article2"));
        assertTrue(referenceNode.hasNode("article1"));
        assertTrue(referenceNode.getNode("article1").hasProperty("property_boolean"));
        assertTrue(referenceNode.getNode("article1").hasProperty("property_string"));
        assertFalse(referenceNode.getNode("article1").hasProperty("property_long"));
    }

    @Test
    public void testDeleteMultipleItems() throws Exception {
        // GIVEN
        JcrItemAdapter node = new JcrNodeAdapter(referenceNode.getNode("article1"));
        JcrItemAdapter prop = new JcrPropertyAdapter(referenceNode.getNode("article2").getProperty("property_long"));
        List<JcrItemAdapter> items = new ArrayList<JcrItemAdapter>(2);
        items.add(node);
        items.add(prop);
        DeleteAction<DeleteActionDefinition> deleteAction = new DeleteAction<DeleteActionDefinition>(definition, items, commandsManager, eventBus, mock(UiContext.class), i18n);

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article2"));
        assertFalse(referenceNode.hasNode("article1"));
        assertTrue(referenceNode.getNode("article2").hasProperty("property_boolean"));
        assertTrue(referenceNode.getNode("article2").hasProperty("property_string"));
        assertFalse(referenceNode.getNode("article2").hasProperty("property_long"));
    }
}
