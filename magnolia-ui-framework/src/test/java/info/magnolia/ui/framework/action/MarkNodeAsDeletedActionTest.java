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

import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.DeleteCommand;
import info.magnolia.commands.impl.MarkNodeAsDeletedCommand;
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
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class MarkNodeAsDeletedActionTest extends RepositoryTestCase {
    private CommandsManager commandsManager;
    private MarkNodeAsDeletedActionDefinition definition;
    private Map<String, Object> params = new HashMap<String, Object>();
    private MarkNodeAsDeletedCommand markNodeAsDeleteCommand;
    private Node referenceNode;
    private EventBus eventBus;

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

        MgnlContext.setLocale(Locale.ENGLISH);
        // Init Command
        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        referenceNode = webSiteSession.getRootNode().addNode("referenceNode");
        referenceNode.addNode("article1", NodeTypes.Page.NAME);
        referenceNode.getNode("article1").setProperty("property_boolean", Boolean.TRUE);
        referenceNode.getNode("article1").setProperty("property_long", Long.decode("1000"));
        referenceNode.getNode("article1").setProperty("property_string", "property");
        referenceNode.getNode("article1").addNode("article2", NodeTypes.Page.NAME);
        referenceNode.getNode("article1").getNode("article2").setProperty("property_boolean", Boolean.TRUE);
        referenceNode.getNode("article1").getNode("article2").setProperty("property_long", Long.decode("1000"));
        referenceNode.getNode("article1").getNode("article2").setProperty("property_string", "property");
        referenceNode.addNode("article3", NodeTypes.Page.NAME);
        referenceNode.getNode("article3").setProperty("property_boolean", Boolean.TRUE);
        referenceNode.getNode("article3").setProperty("property_long", Long.decode("1000"));
        referenceNode.getNode("article3").setProperty("property_string", "property");

        markNodeAsDeleteCommand = new MarkNodeAsDeletedCommand();

        ActivationManager activationManager = mock(ActivationManager.class);
        Subscriber subscriber = mock(Subscriber.class);
        Collection<Subscriber> subscribers = new ArrayList<Subscriber>();
        subscribers.add(subscriber);
        ComponentsTestUtil.setInstance(ActivationManager.class, activationManager);
        when(activationManager.getSubscribers()).thenReturn(subscribers);
        when(subscriber.isActive()).thenReturn(true);

        // Init Action and CommandManager
        definition = new MarkNodeAsDeletedActionDefinition();
        definition.setCommand("markAsDeleted");

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);
        // see for why this is needed.
        ComponentsTestUtil.setInstance(Map.class, params);

        CommandsManager commandsManagerTmp = Components.getComponent(CommandsManager.class);
        Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node exportModuleDef = configSession.getRootNode().addNode("modules", NodeTypes.ContentNode.NAME).addNode("commands", NodeTypes.ContentNode.NAME)
                .addNode("default", NodeTypes.ContentNode.NAME).addNode("markAsDeleted", NodeTypes.ContentNode.NAME);
        exportModuleDef.setProperty("class", DeleteCommand.class.getName());
        exportModuleDef.getSession().save();
        commandsManagerTmp.register(ContentUtil.asContent(exportModuleDef.getParent()));
        commandsManager = spy(commandsManagerTmp);
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "markAsDeleted")).thenReturn(markNodeAsDeleteCommand);
        when(commandsManager.getCommand("markAsDeleted")).thenReturn(markNodeAsDeleteCommand);

        eventBus = mock(EventBus.class);
    }

    @Test
    public void testMarkNodeAsDeleteLeaf() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(referenceNode.getNode("article1").getNode("article2"));
        MarkNodeAsDeletedAction deleteAction = new MarkNodeAsDeletedAction(definition, item, commandsManager, eventBus, new ConfirmationActionTest.TestUiContext(true), mock(SimpleTranslator.class));

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article1"));
        assertFalse(referenceNode.getNode("article1").hasProperty(NodeTypes.Deleted.DELETED));
        assertTrue(referenceNode.hasNode("article1/article2"));
        assertTrue(referenceNode.getNode("article1/article2").hasProperty(NodeTypes.Deleted.DELETED));
    }

    @Test
    public void testMarkNodeAsDeleteWithChildren() throws Exception {
        // GIVEN
        JcrItemAdapter item = new JcrNodeAdapter(referenceNode.getNode("article1"));
        MarkNodeAsDeletedAction deleteAction = new MarkNodeAsDeletedAction(definition, item, commandsManager, eventBus, new ConfirmationActionTest.TestUiContext(true), mock(SimpleTranslator.class));

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article1"));
        assertTrue(referenceNode.getNode("article1").hasProperty(NodeTypes.Deleted.DELETED));
        assertTrue(referenceNode.hasNode("article1/article2"));
        assertTrue(referenceNode.getNode("article1/article2").hasProperty(NodeTypes.Deleted.DELETED));
    }

    @Test
    public void testMarkMultipleNodesAsDeleted() throws Exception {
        // GIVEN
        List<JcrItemAdapter> items = new ArrayList<JcrItemAdapter>(2);
        items.add(new JcrNodeAdapter(referenceNode.getNode("article1").getNode("article2")));
        items.add(new JcrNodeAdapter(referenceNode.getNode("article3")));
        MarkNodeAsDeletedAction deleteAction = new MarkNodeAsDeletedAction(definition, items, commandsManager, eventBus, new ConfirmationActionTest.TestUiContext(true), mock(SimpleTranslator.class));

        // WHEN
        deleteAction.execute();

        // THEN
        assertTrue(referenceNode.hasNode("article1"));
        assertFalse(referenceNode.getNode("article1").hasProperty(NodeTypes.Deleted.DELETED));
        assertTrue(referenceNode.hasNode("article1/article2"));
        assertTrue(referenceNode.getNode("article1/article2").hasProperty(NodeTypes.Deleted.DELETED));
        assertTrue(referenceNode.hasNode("article3"));
        assertTrue(referenceNode.getNode("article3").hasProperty(NodeTypes.Deleted.DELETED));

    }
}
