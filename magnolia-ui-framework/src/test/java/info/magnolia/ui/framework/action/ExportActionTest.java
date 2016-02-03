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
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.ExportCommand;
import info.magnolia.context.MgnlContext;
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
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link info.magnolia.ui.framework.action.ExportAction}.
 */
public class ExportActionTest extends RepositoryTestCase {

    private CommandsManager commandsManager;
    private ExportActionDefinition definition;
    private Map<String, Object> params = new HashMap<String, Object>();
    private ExportCommand exportCommand;
    private Node toCopyNode;
    private ByteArrayOutputStream outputStream;

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

        // Init Command
        Session webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        toCopyNode = webSiteSession.getRootNode().addNode("toCopy");
        toCopyNode.addNode("article", NodeTypes.Page.NAME);
        toCopyNode.getNode("article").setProperty("property_boolean", Boolean.TRUE);
        toCopyNode.getNode("article").setProperty("property_long", Long.decode("1000"));
        toCopyNode.getNode("article").setProperty("property_string", "property");
        toCopyNode.addNode("article/content", NodeTypes.Content.NAME);
        toCopyNode.getNode("article/content").setProperty("property_string", "property");

        outputStream = new ByteArrayOutputStream();

        exportCommand = spy(new ExportCommand());
        doReturn(true).when(exportCommand).checkPermissions(RepositoryConstants.WEBSITE, toCopyNode.getPath(), Permission.READ);

        // Init Action and CommandManager
        definition = new ExportActionDefinition();
        definition.setCommand("export");

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);
        // see for why this is needed.
        ComponentsTestUtil.setInstance(Map.class, params);

        CommandsManager commandsManagerTmp = Components.getComponent(CommandsManager.class);
        Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node exportModuleDef = configSession.getRootNode().addNode("modules", NodeTypes.ContentNode.NAME).addNode("commands", NodeTypes.ContentNode.NAME)
                .addNode("default", NodeTypes.ContentNode.NAME).addNode("export", NodeTypes.ContentNode.NAME);
        exportModuleDef.setProperty("class", ExportCommand.class.getName());
        exportModuleDef.getSession().save();
        commandsManagerTmp.register(ContentUtil.asContent(exportModuleDef.getParent()));
        commandsManager = spy(commandsManagerTmp);
        when(commandsManager.getCommand(CommandsManager.DEFAULT_CATALOG, "export")).thenReturn(exportCommand);
        when(commandsManager.getCommand("export")).thenReturn(exportCommand);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        outputStream.close();
    }

    @Test
    public void testExportActionGetParam() throws Exception {
        // GIVEN
        ExportAction exportActionTmp = new ExportAction(definition, new JcrNodeAdapter(toCopyNode), commandsManager, null, mock(SimpleTranslator.class));
        ExportAction exportAction = spy(exportActionTmp);
        doNothing().when(exportAction).onPostExecute();

        // WHEN
        exportAction.execute();

        // THEN
        Map<String, Object> param = exportAction.getParams();
        assertTrue(param != null);
        assertEquals(".xml", param.get("ext"));
        assertEquals(Boolean.TRUE, param.get("format"));
        assertEquals(Boolean.FALSE, param.get("keepHistory"));
        assertEquals(toCopyNode.getPath(), param.get("path"));
    }
}
