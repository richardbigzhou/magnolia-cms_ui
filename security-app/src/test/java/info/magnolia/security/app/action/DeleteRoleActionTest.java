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
package info.magnolia.security.app.action;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.Security;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link DeleteRoleAction}.
 */
public class DeleteRoleActionTest extends RepositoryTestCase {

    private static final String ROLENAME = "testRole";

    private DeleteRoleAction action;
    private Session session;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        DeleteRoleActionDefinition definition = new DeleteRoleActionDefinition();
        session = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        Node groupNode = session.getRootNode().addNode(ROLENAME, NodeTypes.Group.NAME);
        JcrItemAdapter item = new JcrNodeAdapter(groupNode);
        EventBus eventBus = mock(EventBus.class);
        UiContext uiContext = mock(UiContext.class);

        ComponentsTestUtil.setImplementation(SecuritySupport.class, SecuritySupportImpl.class);

        action = new DeleteRoleAction(definition, item, eventBus, uiContext, mock(SimpleTranslator.class));
    }

    @Test
    public void testRoleIsDeletedWhenThereIsNoUserNorGroup() throws Exception {
        // GIVEN
        GroupManager gm = mock(GroupManager.class);
        when(gm.getAllGroups()).thenReturn(new ArrayList(0));
        ((SecuritySupportImpl) Security.getSecuritySupport()).setGroupManager(gm);
        ((SecuritySupportImpl) Security.getSecuritySupport()).setUserManagers(new HashMap<String, UserManager>());

        // WHEN
        action.executeAfterConfirmation();

        // THEN
        assertFalse(session.getRootNode().hasNode(ROLENAME));
    }

    @Test
    public void testRoleIsNotDeletedWhenItIsAssignedToUser() throws Exception {
        // GIVEN
        GroupManager gm = mock(GroupManager.class);
        when(gm.getAllGroups()).thenReturn(new ArrayList(0));
        ((SecuritySupportImpl) Security.getSecuritySupport()).setGroupManager(gm);

        User user = mock(User.class);
        when(user.getRoles()).thenReturn(Collections.singleton(ROLENAME));

        UserManager um = mock(UserManager.class);
        when(um.getAllUsers()).thenReturn(Collections.singleton(user));
        ((SecuritySupportImpl) Security.getSecuritySupport()).setUserManagers(Collections.singletonMap("test", um));

        // WHEN
        action.executeAfterConfirmation();

        // THEN
        assertTrue(session.getRootNode().hasNode(ROLENAME));
    }

    @Test
    public void testRoleIsNotDeletedWhenItIsAssignedToGroup() throws Exception {
        // GIVEN
        Group grp = mock(Group.class);
        when(grp.getRoles()).thenReturn(Collections.singleton(ROLENAME));

        GroupManager gm = mock(GroupManager.class);
        when(gm.getAllGroups()).thenReturn(Collections.singleton(grp));
        ((SecuritySupportImpl) Security.getSecuritySupport()).setGroupManager(gm);

        ((SecuritySupportImpl) Security.getSecuritySupport()).setUserManagers(new HashMap<String, UserManager>());

        // WHEN
        action.executeAfterConfirmation();

        // THEN
        assertTrue(session.getRootNode().hasNode(ROLENAME));
    }
}
