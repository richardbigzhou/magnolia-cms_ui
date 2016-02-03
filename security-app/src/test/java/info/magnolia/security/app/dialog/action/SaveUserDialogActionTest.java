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
package info.magnolia.security.app.dialog.action;

import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.SecurityConstants;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.SystemContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.jcr.MockValueFactory;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.api.ModelConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SaveUserDialogAction}.
 */
public class SaveUserDialogActionTest extends MgnlTestCase {

    private SaveUserDialogActionDefinition definition;
    private EditorValidator editorValidator;
    private EditorCallback editorCallback;
    private SecuritySupport securitySupport;

    private Session groupsSession;
    private Session rolesSession;

    private Node userNode;

    private UserManager userManager;
    private User user;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        definition = new SaveUserDialogActionDefinition();
        editorValidator = mock(EditorValidator.class);
        editorCallback = mock(EditorCallback.class);
        securitySupport = mock(SecuritySupport.class);

        userNode = mock(Node.class);
        Session session = mock(Session.class);
        when(userNode.getSession()).thenReturn(session);
        when(session.getValueFactory()).thenReturn(new MockValueFactory());

        SystemContext systemContext = mock(SystemContext.class);
        groupsSession = mock(Session.class);
        rolesSession = mock(Session.class);
        when(systemContext.getJCRSession(RepositoryConstants.USER_GROUPS)).thenReturn(groupsSession);
        when(systemContext.getJCRSession(RepositoryConstants.USER_ROLES)).thenReturn(rolesSession);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);
    }

    private void prepareMocks(JcrNodeAdapter userItem) throws RepositoryException {
        when(editorValidator.isValid()).thenReturn(true);

        userManager = mock(UserManager.class);
        user = mock(User.class);

        when(securitySupport.getUserManager("admin")).thenReturn(userManager);


        when(userItem.getItemProperty(ModelConstants.JCR_NAME)).thenReturn(DefaultPropertyUtil.newDefaultProperty(String.class, "testUser"));
        when(userItem.getItemProperty(MgnlUserManager.PROPERTY_PASSWORD)).thenReturn(DefaultPropertyUtil.newDefaultProperty(String.class, "password"));

        when(userItem.getJcrItem()).thenReturn(userNode);
        Collection propertyId = Arrays.asList(MgnlUserManager.PROPERTY_ENABLED, MgnlUserManager.PROPERTY_TITLE, MgnlUserManager.PROPERTY_EMAIL, MgnlUserManager.PROPERTY_LANGUAGE, MgnlUserManager.PROPERTY_EMAIL, "customProps");
        when(userItem.getItemPropertyIds()).thenReturn(propertyId);
        when(userItem.getItemProperty("customProps")).thenReturn(DefaultPropertyUtil.newDefaultProperty(String.class, "customValue"));
        when(userItem.getItemProperty(MgnlUserManager.PROPERTY_ENABLED)).thenReturn(DefaultPropertyUtil.newDefaultProperty(String.class, "enabled"));
        when(userItem.getItemProperty(MgnlUserManager.PROPERTY_TITLE)).thenReturn(DefaultPropertyUtil.newDefaultProperty(String.class, "title"));
        when(userItem.getItemProperty(MgnlUserManager.PROPERTY_LANGUAGE)).thenReturn(DefaultPropertyUtil.newDefaultProperty(String.class, "language"));
        when(userItem.getItemProperty(MgnlUserManager.PROPERTY_EMAIL)).thenReturn(DefaultPropertyUtil.newDefaultProperty(String.class, "email"));

        when(userItem.getItemProperty(SecurityConstants.NODE_GROUPS)).thenReturn(new DefaultProperty(List.class, new ArrayList<String>()));
        when(userNode.hasNode(SecurityConstants.NODE_GROUPS)).thenReturn(true);
        when(userNode.getNode(SecurityConstants.NODE_GROUPS)).thenReturn(userNode);
        when(userItem.getItemProperty(SecurityConstants.NODE_ROLES)).thenReturn(new DefaultProperty(List.class, new ArrayList<String>()));
        when(userNode.hasNode(SecurityConstants.NODE_ROLES)).thenReturn(true);
        when(userNode.getNode(SecurityConstants.NODE_ROLES)).thenReturn(userNode);

        PropertyIterator propertyIterator = mock(PropertyIterator.class);
        when(userNode.getProperties()).thenReturn(propertyIterator);

    }

    @Test
    public void testGetUserManagerWhenUserManagerRealmIsSpecifiedAndUserAlreadyExists() throws ActionExecutionException, RepositoryException {
        // GIVEN
        JcrNodeAdapter userItem = mock(JcrNodeAdapter.class);
        prepareMocks(userItem);

        when(userNode.getName()).thenReturn("testUser");
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
        JcrNodeAdapter userItem = mock(JcrNodeAdapter.class);
        prepareMocks(userItem);

        when(userNode.getPath()).thenReturn("/admin/testUser");

        when(userNode.getName()).thenReturn("testUser");
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
        JcrNodeAdapter userItem = mock(JcrNewNodeAdapter.class);
        prepareMocks(userItem);

        when(userNode.getPath()).thenReturn("/admin");
        when(userManager.createUser("/admin", "testUser", "password")).thenReturn(user);
        when(user.getName()).thenReturn("testUser");
        when(userNode.getNode("testUser")).thenReturn(userNode);

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        verify(securitySupport).getUserManager("admin");
    }

    @Test
    public void testGetUserManagerWhenUserManagerRealmIsNotSpecifiedAndCreateNewUser() throws ActionExecutionException, RepositoryException {
        // GIVEN
        JcrNodeAdapter userItem = mock(JcrNewNodeAdapter.class);
        prepareMocks(userItem);

        when(userNode.getPath()).thenReturn("/admin/test");
        when(userManager.createUser("/admin/test", "testUser", "password")).thenReturn(user);
        when(user.getName()).thenReturn("testUser");
        when(userNode.getNode("testUser")).thenReturn(userNode);

        SaveUserDialogAction saveUserDialogAction = new SaveUserDialogAction(definition, userItem, editorValidator, editorCallback, securitySupport);

        // WHEN
        saveUserDialogAction.execute();

        // THEN
        verify(securitySupport).getUserManager("admin");
    }

    @Test
    public void testStoreGroupsCollection() throws ActionExecutionException, RepositoryException {
        // GIVEN
        JcrNodeAdapter userItem = mock(JcrNewNodeAdapter.class);
        prepareMocks(userItem);

        when(userNode.getPath()).thenReturn("/admin/test");
        when(userManager.createUser("/admin/test", "testUser", "password")).thenReturn(user);

        Collection<String> newGroupList = new ArrayList<String>();
        newGroupList.add("firstGroupUuid");
        newGroupList.add("secondGroupUuid");
        newGroupList.add("thirdGroupUuid");
        when(userItem.getItemProperty(SecurityConstants.NODE_GROUPS)).thenReturn(new DefaultProperty(List.class, newGroupList));

        Node groupNode = mock(Node.class);
        when(groupsSession.getNodeByIdentifier("firstGroupUuid")).thenReturn(groupNode);
        when(groupsSession.getNodeByIdentifier("secondGroupUuid")).thenReturn(groupNode);
        when(groupsSession.getNodeByIdentifier("thirdGroupUuid")).thenReturn(groupNode);
        when(groupNode.getName()).thenReturn("firstGroupName", "secondGroupName", "thirdGroupName");

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

    @Test
    public void testStoreRolesCollection() throws ActionExecutionException, RepositoryException {
        // GIVEN
        JcrNodeAdapter userItem = mock(JcrNewNodeAdapter.class);
        prepareMocks(userItem);

        when(userNode.getPath()).thenReturn("/admin/test");
        when(userManager.createUser("/admin/test", "testUser", "password")).thenReturn(user);

        Collection<String> newRoleList = new ArrayList<String>();
        newRoleList.add("firstRoleUuid");
        newRoleList.add("secondRoleUuid");
        newRoleList.add("thirdRoleUuid");
        when(userItem.getItemProperty(SecurityConstants.NODE_ROLES)).thenReturn(new DefaultProperty(List.class, newRoleList));

        Node roleNode = mock(Node.class);
        when(rolesSession.getNodeByIdentifier("firstRoleUuid")).thenReturn(roleNode);
        when(rolesSession.getNodeByIdentifier("secondRoleUuid")).thenReturn(roleNode);
        when(rolesSession.getNodeByIdentifier("thirdRoleUuid")).thenReturn(roleNode);
        when(roleNode.getName()).thenReturn("firstRoleName", "secondRoleName", "thirdRoleName");

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

}
