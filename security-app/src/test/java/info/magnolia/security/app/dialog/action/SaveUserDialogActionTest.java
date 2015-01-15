/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.security.app.dialog.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.SecurityConstants;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for the {@link SaveUserDialogAction}.
 */
public class SaveUserDialogActionTest extends RepositoryTestCase {

    private SaveUserDialogActionDefinition definition;
    private EditorValidator editorValidator;
    private EditorCallback editorCallback;
    private SecuritySupport securitySupport;

    private Session groupsSession;
    private Session rolesSession;

    private Node userNode;

    private UserManager userManager;
    private User user;
    private Session userSession;
    private Node adminNode;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        definition = new SaveUserDialogActionDefinition();
        editorValidator = mock(EditorValidator.class);
        editorCallback = mock(EditorCallback.class);
        securitySupport = mock(SecuritySupport.class);

        userSession = MgnlContext.getJCRSession(RepositoryConstants.USERS);
        adminNode = userSession.getRootNode().addNode("admin");

        groupsSession = MgnlContext.getJCRSession(RepositoryConstants.USER_GROUPS);
        rolesSession = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
    }


    private void prepareMocks(JcrNodeAdapter userItem) throws RepositoryException {
        when(editorValidator.isValid()).thenReturn(true);

        userManager = mock(UserManager.class);
        user = mock(User.class);

        when(securitySupport.getUserManager("admin")).thenReturn(userManager);

        userItem.addItemProperty(ModelConstants.JCR_NAME, DefaultPropertyUtil.newDefaultProperty(String.class, "testUser"));
        userItem.addItemProperty(MgnlUserManager.PROPERTY_PASSWORD, DefaultPropertyUtil.newDefaultProperty(String.class, "password"));

        userItem.addItemProperty(MgnlUserManager.PROPERTY_ENABLED, DefaultPropertyUtil.newDefaultProperty(String.class, "enabled"));
        userItem.addItemProperty(MgnlUserManager.PROPERTY_LANGUAGE, DefaultPropertyUtil.newDefaultProperty(String.class, "language"));
        userItem.addItemProperty(MgnlUserManager.PROPERTY_TITLE, DefaultPropertyUtil.newDefaultProperty(String.class, "title"));
        userItem.addItemProperty(MgnlUserManager.PROPERTY_EMAIL, DefaultPropertyUtil.newDefaultProperty(String.class, "email"));
        userItem.addItemProperty("customProps", DefaultPropertyUtil.newDefaultProperty(String.class, "customValue"));

        userItem.addItemProperty(SecurityConstants.NODE_GROUPS, new DefaultProperty(List.class, new ArrayList<String>()));
        userItem.addItemProperty(SecurityConstants.NODE_ROLES, new DefaultProperty(List.class, new ArrayList<String>()));
    }

    @Test
    public void testGetUserManagerWhenUserManagerRealmIsSpecifiedAndUserAlreadyExists() throws ActionExecutionException, RepositoryException {
        // GIVEN
        userNode = adminNode.addNode("testUser");
        JcrNodeAdapter userItem = new JcrNodeAdapter(userNode);
        prepareMocks(userItem);

        when(userManager.getUser("testUser")).thenReturn(user);

        definition.setUserManagerRealm("admin");

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        verify(securitySupport).getUserManager("admin");
    }

    @Test
    public void testGetUserManagerWhenUserManagerRealmIsNotSpecifiedAndUserAlreadyExists() throws ActionExecutionException, RepositoryException {
        // GIVEN
        userNode = adminNode.addNode("testUser");
        JcrNodeAdapter userItem = new JcrNodeAdapter(userNode);;
        prepareMocks(userItem);

        when(userManager.getUser("testUser")).thenReturn(user);

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        verify(securitySupport).getUserManager("admin");
    }

    @Test
    public void testGetUserManagerWhenUserManagerRealmIsNotSpecifiedAndCreateNewUserIntoRealmRoot() throws ActionExecutionException, RepositoryException {
        // GIVEN
        JcrNodeAdapter userItem  = new JcrNewNodeAdapter(adminNode, "testUser");
        prepareMocks(userItem);

        mockUserCreation("/admin", "testUser");

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        verify(securitySupport).getUserManager("admin");
    }

    @Test
    public void testGetUserManagerWhenUserManagerRealmIsNotSpecifiedAndCreateNewUser() throws ActionExecutionException, RepositoryException {
        // GIVEN
        Node testNode = adminNode.addNode("test");
        JcrNodeAdapter userItem  = new JcrNewNodeAdapter(testNode, "testUser");
        prepareMocks(userItem);

        mockUserCreation("/admin/test", "testUser");
        when(user.getName()).thenReturn("testUser");

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        verify(securitySupport).getUserManager("admin");
    }

    @Test
    public void testStoreGroupsCollection() throws ActionExecutionException, RepositoryException {
        // GIVEN
        Node testNode = adminNode.addNode("test");
        JcrNodeAdapter userItem  = new JcrNewNodeAdapter(testNode, "testUser");
        prepareMocks(userItem);

        mockUserCreation("/admin/test", "testUser");

        Collection<String> newGroupList = Arrays.asList(
                groupsSession.getRootNode().addNode("firstGroupName").getIdentifier(),
                groupsSession.getRootNode().addNode("secondGroupName").getIdentifier(),
                groupsSession.getRootNode().addNode("thirdGroupName").getIdentifier()
        );

        userItem.removeItemProperty(SecurityConstants.NODE_GROUPS);
        userItem.addItemProperty(SecurityConstants.NODE_GROUPS, new DefaultProperty(List.class, newGroupList));

        Collection<String> oldGroupList = new ArrayList<String>();
        oldGroupList.add("firstGroupUuid");
        oldGroupList.add("oldGroup");
        when(user.getGroups()).thenReturn(oldGroupList);

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        //verify that all new groups are added to user
        verify(userManager).addGroup(user, "firstGroupName");
        verify(userManager).addGroup(user, "secondGroupName");
        verify(userManager).addGroup(user, "thirdGroupName");

        //verify that old group is removed from user groups
        verify(userManager).removeGroup(user, "oldGroup");
        verify(userManager, never()).addGroup(user, "oldGroup");

        //verify that group which was already assigned to user is not removed
        verify(userManager, never()).removeGroup(user, "firstGroupName");
    }

    private void mockUserCreation(final String path, final String name) {
        when(userManager.createUser(path, name, "password")).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Node node = userSession.getNode(path).addNode(name);
                userSession.save();
                doReturn(node.getIdentifier()).when(user).getIdentifier();
                return user;
            }
        });
    }

    @Test
    public void testStoreRolesCollection() throws ActionExecutionException, RepositoryException {
        // GIVEN
        Node testNode = adminNode.addNode("test");
        JcrNodeAdapter userItem  = new JcrNewNodeAdapter(testNode, "testUser");
        prepareMocks(userItem);

        mockUserCreation("/admin/test", "testUser");

        Collection<String> newRoleList = Arrays.asList(
                rolesSession.getRootNode().addNode("firstRoleName").getIdentifier(),
                rolesSession.getRootNode().addNode("secondRoleName").getIdentifier(),
                rolesSession.getRootNode().addNode("thirdRoleName").getIdentifier()
        );

        userItem.removeItemProperty(SecurityConstants.NODE_ROLES);
        userItem.addItemProperty(SecurityConstants.NODE_ROLES, new DefaultProperty(List.class, newRoleList));

        Collection<String> oldRoleList = new ArrayList<String>();
        oldRoleList.add("firstRoleUuid");
        oldRoleList.add("oldRole");
        when(user.getRoles()).thenReturn(oldRoleList);

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        //verify that all new roles are added to user
        verify(userManager).addRole(user, "firstRoleName");
        verify(userManager).addRole(user, "secondRoleName");
        verify(userManager).addRole(user, "thirdRoleName");

        //verify that old role is removed from user roles
        verify(userManager).removeRole(user, "oldRole");
        verify(userManager, never()).addRole(user, "oldRole");

        //verify that role which was already assigned to user is not removed
        verify(userManager, never()).removeRole(user, "firstRoleName");
    }

    @Test
    public void testChildNodeIsUpdated() throws Exception {
        // GIVEN
        userNode = adminNode.addNode("testUser");
        JcrNodeAdapter userItem = new JcrNodeAdapter(userNode);
        prepareMocks(userItem);

        when(userManager.getUser("testUser")).thenReturn(user);

        final JcrNewNodeAdapter newChildItem = new JcrNewNodeAdapter(userNode, NodeTypes.ContentNode.NAME, "newChild");
        userItem.addChild(newChildItem);

        final Node existingChild = userNode.addNode("existingChild");
        final JcrNodeAdapter existingChildItem = new JcrNodeAdapter(existingChild);
        userItem.addChild(existingChildItem);

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        newChildItem.addItemProperty("test", DefaultPropertyUtil.newDefaultProperty(String.class, "testValue"));
        existingChildItem.addItemProperty("test", DefaultPropertyUtil.newDefaultProperty(String.class, "testValue"));

        saveUserDialogAction.execute();


        // THEN
        assertEquals("testValue", userNode.getNode("newChild").getProperty("test").getString());
        assertEquals("testValue", userNode.getNode("existingChild").getProperty("test").getString());
    }

}
