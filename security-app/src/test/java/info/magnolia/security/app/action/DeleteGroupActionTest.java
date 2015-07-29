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
package info.magnolia.security.app.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
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
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.framework.action.async.AsyncActionExecutor;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DeleteGroupActionTest extends RepositoryTestCase {

    private static final String GROUPNAME = "testGroup";

    private DeleteGroupAction action;
    private SecuritySupport securitySupport;
    private Session session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        DeleteGroupActionDefinition definition = new DeleteGroupActionDefinition();
        session = MgnlContext.getJCRSession(RepositoryConstants.USER_GROUPS);
        Node groupNode = session.getRootNode().addNode(GROUPNAME, NodeTypes.Group.NAME);
        JcrItemAdapter item = new JcrNodeAdapter(groupNode);
        EventBus eventBus = mock(EventBus.class);
        UiContext uiContext = mock(UiContext.class);
        doAnswer(new Answer() {
                     @Override
                     public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                         Object[] args = invocationOnMock.getArguments();
                         ((ConfirmationCallback) args[6]).onSuccess();
                         return null;
                     }
                 }
        ).when(uiContext).openConfirmation(any(MessageStyleType.class),anyString(),anyString(),anyString(),anyString(),anyBoolean(),any(ConfirmationCallback.class));

        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);
        ComponentsTestUtil.setImplementation(AsyncActionExecutor.class, mock(AsyncActionExecutor.class).getClass());

        ComponentsTestUtil.setInstance(Map.class, new HashMap<String, Object>());

        CommandsManager commandsManager = Components.getComponent(CommandsManager.class);
        Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node exportModuleDef = configSession.getRootNode()
                .addNode("modules", NodeTypes.ContentNode.NAME)
                .addNode("commands", NodeTypes.ContentNode.NAME)
                .addNode("default", NodeTypes.ContentNode.NAME)
                .addNode("delete", NodeTypes.ContentNode.NAME);
        exportModuleDef.setProperty("class", DeleteCommand.class.getName());
        exportModuleDef.getSession().save();
        commandsManager.register(ContentUtil.asContent(exportModuleDef.getParent()));

        securitySupport = mock(SecuritySupport.class);

        UserManager um = mock(UserManager.class);
        when(securitySupport.getUserManager()).thenReturn(um);

        action = new DeleteGroupAction(definition, item, commandsManager, eventBus, uiContext, mock(SimpleTranslator.class), securitySupport);
    }

    @Test
    public void testGroupIsDeletedWhenThereIsNoUserNorGroup() throws Exception {
        // GIVEN
        GroupManager gm = mock(GroupManager.class);
        when(gm.getAllGroups()).thenReturn(new ArrayList(0));
        when(securitySupport.getGroupManager()).thenReturn(gm);

        UserManager um = mock(UserManager.class);
        when(um.getUsersWithGroup(GROUPNAME)).thenReturn(Collections.EMPTY_LIST);

        when(securitySupport.getUserManager()).thenReturn(um);

        // WHEN
        action.execute();

        // THEN
        assertFalse(session.getRootNode().hasNode(GROUPNAME));
    }

    @Test
    public void testGroupIsDeletedIfItIsAssignedToUser() throws Exception {
        // GIVEN
        GroupManager gm = mock(GroupManager.class);
        when(gm.getAllGroups()).thenReturn(new ArrayList(0));
        when(securitySupport.getGroupManager()).thenReturn(gm);

        User user = mock(User.class);
        when(user.getGroups()).thenReturn(Collections.singleton(GROUPNAME));

        UserManager um = mock(UserManager.class);
        when(um.getUsersWithGroup(GROUPNAME)).thenReturn(Arrays.asList("userThatHasThatGroupAssigned"));
        when(securitySupport.getUserManager()).thenReturn(um);

        // WHEN
        action.execute();

        // THEN
        assertFalse(session.getRootNode().hasNode(GROUPNAME));
    }

    @Test
    public void testGroupIsDeletedIfItIsAssignedToGroup() throws Exception {
        // GIVEN
        Group grp = mock(Group.class);
        when(grp.getGroups()).thenReturn(Collections.singleton(GROUPNAME));

        GroupManager gm = mock(GroupManager.class);
        when(gm.getAllGroups()).thenReturn(Collections.singleton(grp));
        when(securitySupport.getGroupManager()).thenReturn(gm);

        // WHEN
        action.execute();

        // THEN
        assertFalse(session.getRootNode().hasNode(GROUPNAME));
    }
}
