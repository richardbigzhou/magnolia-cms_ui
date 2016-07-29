/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.security.app.dialog.field.validator;

import static info.magnolia.cms.security.operations.AccessDefinition.DEFAULT_SUPERUSER_ROLE;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.ACLImpl;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.dialog.field.WorkspaceAccessControlList;
import info.magnolia.test.mock.MockWebContext;

import java.util.ArrayList;

import javax.security.auth.Subject;

import org.junit.Before;
import org.junit.Test;

public class WorkspaceAccessControlValidatorTest {
    private WorkspaceAccessControlValidator accessControlValidator;
    private Subject subject;

    @Before
    public void setUp() throws Exception {
        subject = new Subject();
        grant(RepositoryConstants.USER_ROLES, "/*", Permission.ALL);
        MockWebContext context = new MockWebContext();
        context.setSubject(subject);

        User user = mock(User.class);
        when(user.hasRole(eq(DEFAULT_SUPERUSER_ROLE))).thenReturn(false);
        context.setUser(user);

        MgnlContext.setInstance(context);
    }

    // Workspace permission tests

    @Test
    public void deniesGivingReadPermissionWhenUserDoesNotHaveReadPermission() throws Exception {

        // GIVEN
        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.READ, WorkspaceAccessControlList.ACCESS_TYPE_NODE, "/read");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingRecursiveReadPermissionWhenUserDoesNotHaveRecursiveReadPermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/read", Permission.READ);
        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.READ, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/read");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingDenyPermissionWhenUserDoesNotHaveReadPermission() throws Exception {

        // GIVEN
        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.NONE, WorkspaceAccessControlList.ACCESS_TYPE_NODE, "/read");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void allowsGivingDenyPermissionWhenUserHasReadPermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/read", Permission.READ);
        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.NONE, WorkspaceAccessControlList.ACCESS_TYPE_NODE, "/read");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertTrue(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingWritePermissionWhenUserDoesNotHaveWritePermission() throws Exception {

        // GIVEN
        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE, "/path");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingRecursiveWritePermissionWhenUserDoesNotHaveRecursiveWritePermission() throws Exception {

        // GIVEN
        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/path");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void allowsGivingRecursiveWritePermissionWhenUserHasRecursiveWritePermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/path", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/path/*", Permission.ALL);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/path");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertTrue(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingRecursivePermissionWhenUserHasNodeOnlyPermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/", Permission.ALL);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void allowsGivingSubNodeReadPermissionWhenUserHasReadPermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/*", Permission.READ);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.READ, WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN, "/");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertTrue(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingSubnodeWritePermissionWhenUserHasReadPermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/*", Permission.READ);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN, "/");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingWritePermissionWhenUserHasWritePermissionOnlyOnSubnodes() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/path", Permission.READ);
        grant(RepositoryConstants.WEBSITE, "/path/*", Permission.ALL);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/path");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingWritePermissionOnRootWhenUserHasWritePermissionOnlyOnSubnodes() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/", Permission.READ);
        grant(RepositoryConstants.WEBSITE, "/*", Permission.ALL);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingSubNodePermissionWhenUserHasAnIncompatibleSubNodePermission() throws Exception {
        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/*", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/foo/bar/restricted", Permission.READ);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN, "/");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void deniesGivingSubNodePermissionWhenUserHasAnIncompatibleSubNodePermission1() throws Exception {
        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/", Permission.READ);
        grant(RepositoryConstants.WEBSITE, "/*", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/foo/bar/restricted", Permission.READ);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertFalse(accessControlValidator.isValid(entry));
    }

    @Test
    public void allowsGivingPermissionsWhenUserHasRestrictedSubNodePermission() throws Exception {
        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/*", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/foo/bar/restricted", Permission.READ);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.NONE, WorkspaceAccessControlList.ACCESS_TYPE_CHILDREN, "/");

        // WHEN - THEN
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");
        assertTrue(accessControlValidator.isValid(entry));
    }

    @Test
    public void givingAllPermissionToRootWhenUserHasRecursiveWritePermission() throws Exception {
        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/", Permission.ALL);
        grant(RepositoryConstants.WEBSITE, "/*", Permission.ALL);

        WorkspaceAccessControlList.Entry entry = new WorkspaceAccessControlList.Entry(Permission.ALL, WorkspaceAccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN, "/");
        accessControlValidator = new WorkspaceAccessControlValidator(RepositoryConstants.WEBSITE, "");

        // WHEN
        assertTrue(accessControlValidator.isValid(entry));
    }

    private void grant(String aclName, String path, long permissions) {
        ACL acl = PrincipalUtil.findAccessControlList(this.subject, aclName);
        if (acl == null) {
            acl = new ACLImpl(aclName, new ArrayList<Permission>());
            this.subject.getPrincipals().add(acl);
        }
        PermissionImpl permission = new PermissionImpl();
        permission.setPattern(new SimpleUrlPattern(path));
        permission.setPermissions(permissions);
        acl.getList().add(permission);
    }
}