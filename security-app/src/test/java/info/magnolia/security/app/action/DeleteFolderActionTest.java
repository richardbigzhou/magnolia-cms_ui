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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.MgnlGroupManager;
import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.impl.DeleteCommand;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DeleteFolderAction}.
 */
public class DeleteFolderActionTest extends RepositoryTestCase {

    private DeleteFolderAction actionRoles;
    private DeleteFolderAction actionGroups;
    private DeleteFolderActionDefinition definition;
    private JcrItemAdapter itemRoles;
    private JcrItemAdapter itemGroups;
    private EventBus eventBus;
    private UiContext uiContext;
    private SimpleTranslator i18n;
    private CommandsManager commandsManager;
    private Session sessionRoles;
    private Session sessionGroups;
    private SecuritySupport securitySupport;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        definition = new DeleteFolderActionDefinition();

        sessionRoles = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        sessionGroups = MgnlContext.getJCRSession(RepositoryConstants.USER_GROUPS);
        Node folderNode1 = sessionRoles.getRootNode().addNode("testFolder", NodeTypes.Folder.NAME);
        Node folderNode2 = sessionGroups.getRootNode().addNode("testFolder", NodeTypes.Folder.NAME);
        itemRoles = new JcrNodeAdapter(folderNode1);
        itemGroups = new JcrNodeAdapter(folderNode2);

        eventBus = mock(EventBus.class);
        uiContext = mock(UiContext.class);
        i18n = mock(SimpleTranslator.class);
        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);
        securitySupport = Components.getComponent(SecuritySupport.class);

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);
        ComponentsTestUtil.setInstance(Map.class, new HashMap<String, Object>());

        commandsManager = Components.getComponent(CommandsManager.class);
        Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node exportModuleDef = NodeUtil.createPath(configSession.getRootNode(), "modules/commands/default/delete", NodeTypes.ContentNode.NAME);
        exportModuleDef.setProperty("class", DeleteCommand.class.getName());
        exportModuleDef.getSession().save();
        commandsManager.register(ContentUtil.asContent(exportModuleDef.getParent()));
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        commandsManager.clear();
    }

    @Test
    public void testEmptyFolderIsDeleted() throws Exception {
        // GIVEN
        actionRoles = new DeleteFolderAction(definition, itemRoles, commandsManager, eventBus, uiContext, i18n, securitySupport);
        actionGroups = new DeleteFolderAction(definition, itemGroups, commandsManager, eventBus, uiContext, i18n, securitySupport);

        // WHEN
        actionRoles.execute();
        actionGroups.execute();

        // THEN
        assertFalse(sessionRoles.itemExists("/testFolder"));
        assertFalse(sessionGroups.itemExists("/testFolder"));
    }


    @Test
    public void testFolderIsNotDeletedIfGroupInUse() throws Exception {
        // GIVEN
        MgnlGroupManager mgnlGroupManager = new NoValidationMgnlGroupManager();

        Group g = mgnlGroupManager.createGroup("/", "testGroup");
        mgnlGroupManager.createGroup("/testFolder", "testGroup1");

        ((SecuritySupportImpl) securitySupport).setGroupManager(mgnlGroupManager);

        ((SecuritySupportImpl) securitySupport).setUserManagers(new HashMap<String, UserManager>());
        mgnlGroupManager.addGroup(g, "testGroup1");
        actionGroups = new DeleteFolderAction(definition, itemGroups, commandsManager, eventBus, uiContext, i18n, securitySupport);

        // WHEN
        actionGroups.execute();

        // THEN
        assertTrue(sessionGroups.itemExists("/testFolder"));
    }

    @Test
    public void testFolderIsNotDeletedIfRoleInUse() throws Exception {
        // GIVEN
        MgnlGroupManager mgnlGroupManager = new NoValidationMgnlGroupManager();
        MgnlRoleManager mgnlRoleManager = new NoValidationMgnlRoleManager();

        Group g = mgnlGroupManager.createGroup("/", "testGroup");
        mgnlRoleManager.createRole("/testFolder", "testRole");

        ((SecuritySupportImpl) securitySupport).setGroupManager(mgnlGroupManager);
        ((SecuritySupportImpl) securitySupport).setRoleManager(mgnlRoleManager);
        ((SecuritySupportImpl) securitySupport).setUserManagers(new HashMap<String, UserManager>());
        mgnlGroupManager.addRole(g, "testRole");
        actionRoles = new DeleteFolderAction(definition, itemRoles, commandsManager, eventBus, uiContext, i18n, securitySupport);

        // WHEN
        actionRoles.execute();

        // THEN
        assertTrue(sessionRoles.itemExists("/testFolder"));
    }

    @Test
    public void testDeleteMultiFoldersInRoles() throws Exception {
        // GIVEN
        Node folderNode1 = sessionRoles.getRootNode().addNode("testFolder1", NodeTypes.Folder.NAME);
        JcrItemAdapter item1 = new JcrNodeAdapter(folderNode1);
        Node folderNode2 = sessionRoles.getRootNode().addNode("testFolder2", NodeTypes.Folder.NAME);
        JcrItemAdapter item2 = new JcrNodeAdapter(folderNode2);
        actionRoles = new DeleteFolderAction(definition, Arrays.asList(item1, item2), commandsManager, eventBus, uiContext, i18n, securitySupport);
        // WHEN
        actionRoles.execute();

        // THEN
        assertFalse(sessionRoles.itemExists("/testFolder1"));
        assertFalse(sessionRoles.itemExists("/testFolder2"));
    }

    @Test
    public void testDeleteMultiFoldersInGroups() throws Exception {
        // GIVEN
        Node folderNode1 = sessionGroups.getRootNode().addNode("testFolder1", NodeTypes.Folder.NAME);
        JcrItemAdapter item1 = new JcrNodeAdapter(folderNode1);
        Node folderNode2 = sessionGroups.getRootNode().addNode("testFolder2", NodeTypes.Folder.NAME);
        JcrItemAdapter item2 = new JcrNodeAdapter(folderNode2);
        actionGroups = new DeleteFolderAction(definition, Arrays.asList(item1, item2), commandsManager, eventBus, uiContext, i18n, securitySupport);
        // WHEN
        actionGroups.execute();

        // THEN
        assertFalse(sessionGroups.itemExists("/testFolder1"));
        assertFalse(sessionGroups.itemExists("/testFolder2"));
    }

    @Test
    public void testFolderIsDeletedWhenContainsGroupAndGroupIsNotUsed() throws Exception {
        // GIVEN
        MgnlGroupManager mgnlGroupManager = new NoValidationMgnlGroupManager();

        mgnlGroupManager.createGroup("/testFolder", "testGroup1");

        ((SecuritySupportImpl) securitySupport).setGroupManager(mgnlGroupManager);

        ((SecuritySupportImpl) securitySupport).setUserManagers(new HashMap<String, UserManager>());

        actionGroups = new DeleteFolderAction(definition, itemGroups, commandsManager, eventBus, uiContext, i18n, securitySupport);

        // WHEN
        actionGroups.execute();

        // THEN
        assertFalse(sessionGroups.itemExists("/testFolder"));
    }

    private static class NoValidationMgnlGroupManager extends MgnlGroupManager {
        @Override
        protected void validateGroupName(String name) throws AccessDeniedException {
            //not needed for test
        }
    }

    private static class NoValidationMgnlRoleManager extends MgnlRoleManager {
        @Override
        protected void validateRoleName(String name) throws AccessDeniedException {
            //not needed for test
        }
    }
}
