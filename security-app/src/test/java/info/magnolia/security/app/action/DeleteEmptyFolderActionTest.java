/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.security.app.action;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.DeleteCommand;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DeleteEmptyFolderAction}.
 */
public class DeleteEmptyFolderActionTest extends RepositoryTestCase {

    private DeleteEmptyFolderAction action;
    private DeleteEmptyFolderActionDefinition definition;
    private JcrItemAdapter item;
    private EventBus eventBus;
    private UiContext uiContext;
    private SimpleTranslator i18n;
    private CommandsManager commandsManager;
    private Session session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        definition = new DeleteEmptyFolderActionDefinition();

        session = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        Node folderNode = session.getRootNode().addNode("testFolder", NodeTypes.Folder.NAME);
        item = new JcrNodeAdapter(folderNode);

        eventBus = mock(EventBus.class);
        uiContext = mock(UiContext.class);
        i18n = mock(SimpleTranslator.class);

        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);
        ComponentsTestUtil.setInstance(Map.class, new HashMap<String, Object>());

        commandsManager = Components.getComponent(CommandsManager.class);
        Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node exportModuleDef = configSession.getRootNode().addNode("modules", NodeTypes.ContentNode.NAME).addNode("commands", NodeTypes.ContentNode.NAME)
                .addNode("default", NodeTypes.ContentNode.NAME).addNode("delete", NodeTypes.ContentNode.NAME);
        exportModuleDef.setProperty("class", DeleteCommand.class.getName());
        exportModuleDef.getSession().save();
        commandsManager.register(ContentUtil.asContent(exportModuleDef.getParent()));

        action = new DeleteEmptyFolderAction(definition, item, commandsManager, eventBus, uiContext, i18n);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        commandsManager.clear();
    }

    @Test
    public void testFolderIsDeleted() throws Exception {
        // GIVEN

        // WHEN
        action.execute();

        // THEN
        assertTrue(!session.itemExists("/testFolder"));
    }


    @Test
    public void testFolderIsNotDeletedIfNotEmpty() throws Exception {
        // GIVEN
        session.getNode("/testFolder").addNode("role", NodeTypes.Role.NAME);
        session.save();

        // WHEN
        action.execute();

        // THEN
        verify(uiContext).openNotification(MessageStyleTypeEnum.ERROR, false, "security.delete.folder.folderNotEmpty");
        assertTrue(session.itemExists("/testFolder"));
    }

}
