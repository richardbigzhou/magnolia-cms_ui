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
package info.magnolia.security.app.dialog.action;

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.ACLImpl;
import info.magnolia.cms.security.AccessManagerImpl;
import info.magnolia.cms.security.DummyUser;
import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.PermissionImpl;
import info.magnolia.cms.security.PermissionUtil;
import info.magnolia.cms.security.PrincipalUtil;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.context.JCRSessionStrategy;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.decoration.AbstractContentDecorator;
import info.magnolia.jcr.decoration.ContentDecoratorSessionWrapper;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.dialog.field.AccessControlList;
import info.magnolia.security.app.dialog.field.WorkspaceAccessFieldFactory;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.security.AccessControlException;
import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.vaadin.data.Item;

/**
 * Test case for SaveRoleDialogActionTest.
 */
public class SaveRoleDialogActionTest extends RepositoryTestCase {

    private SecuritySupport securitySupport;
    private Session session;
    private MgnlRoleManager roleManager;
    private Subject subject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        roleManager = new MgnlRoleManager();
        securitySupport = mock(SecuritySupport.class);
        when(securitySupport.getRoleManager()).thenReturn(roleManager);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);

        session = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);

        subject = new Subject();
        grant(RepositoryConstants.USER_ROLES, "/*", Permission.ALL);
        MockWebContext context = (MockWebContext) MgnlContext.getInstance();
        context.setSubject(subject);
        context.setUser(new DummyUser(){
            @Override
            public boolean hasRole(String roleName) {
                return false;
            }
        });

        final JCRSessionStrategy repositoryStrategy = context.getRepositoryStrategy();
        context.setRepositoryStrategy(new JCRSessionStrategy() {

            @Override
            public Session getSession(final String workspaceName) throws RepositoryException {
                Session targetSession = repositoryStrategy.getSession(workspaceName);
                return new MockSessionSecurityContentDecorator(subject).wrapSession(targetSession);
            }

            @Override
            public void release() {
                repositoryStrategy.release();
            }
        });
    }

    @Test
    public void createRoleAddsWorkspaceAcls() throws RepositoryException, ActionExecutionException {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/read", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/read", Permission.READ, AccessControlList.ACCESS_TYPE_NODE);

        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        assertTrue(session.nodeExists("/testRole/acl_website/0"));
        assertEquals("/read", session.getProperty("/testRole/acl_website/0/path").getString());
        assertEquals(Permission.READ, session.getProperty("/testRole/acl_website/0/permissions").getLong());
    }

    @Test
    public void createRoleWithRecursiveWorkspaceAclAddsTwoEntries() throws RepositoryException, ActionExecutionException {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/*", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/read", Permission.READ, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN);

        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        assertTrue(session.nodeExists("/testRole/acl_website/0"));
        assertEquals("/read", session.getProperty("/testRole/acl_website/0/path").getString());
        assertEquals(Permission.READ, session.getProperty("/testRole/acl_website/0/permissions").getLong());
        assertEquals("/read/*", session.getProperty("/testRole/acl_website/00/path").getString());
        assertEquals(Permission.READ, session.getProperty("/testRole/acl_website/00/permissions").getLong());
    }

    @Test
    public void createRoleWithAclsForUserRolesWorkspace() throws Exception {

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_userroles"), "0", "/read", Permission.READ, AccessControlList.ACCESS_TYPE_NODE);

        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");
        assertRoleHasReadAccessToPath("testRole", "/read");
    }

    @Test
    public void editRoleAddsWorkspaceAcls() throws Exception {

        // GIVEN
        roleManager.createRole("testRole");
        grant(RepositoryConstants.WEBSITE, "/read", Permission.READ);

        // WHEN
        JcrNodeAdapter roleItem = new JcrNodeAdapter(session.getRootNode().getNode("testRole"));
        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/read", Permission.READ, AccessControlList.ACCESS_TYPE_NODE);

        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        assertTrue(session.nodeExists("/testRole/acl_website/0"));
        assertEquals("/read", session.getProperty("/testRole/acl_website/0/path").getString());
        assertEquals(Permission.READ, session.getProperty("/testRole/acl_website/0/permissions").getLong());
    }

    @Test
    public void createRoleAddsUriAcls() throws RepositoryException, ActionExecutionException {

        // GIVEN
        grant("uri", "/read", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addUriAclEntry(addUriAclItem(roleItem, "acl_uri"), "0", "/read", Permission.READ);

        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        assertTrue(session.nodeExists("/testRole/acl_uri/0"));
        assertEquals("/read", session.getProperty("/testRole/acl_uri/0/path").getString());
        assertEquals(Permission.READ, session.getProperty("/testRole/acl_uri/0/permissions").getLong());
    }

    @Test
    public void renameRole() throws Exception {

        // GIVEN
        roleManager.createRole("testRole");
        final Node testRoleNode = session.getRootNode().getNode("testRole");

        // WHEN
        JcrNodeAdapter roleItem = new JcrNodeAdapter(testRoleNode);

        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("renamedRole"));
        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("renamedRole");

        // Assert that no other node present under '/renamedRole/acl_userroles/' except for the existed one,
        // i.e there are no artifacts left after the action.
        final Node userRoleAcls = session.getNode("/renamedRole/acl_userroles/");
        assertEquals(userRoleAcls.getNodes().getSize(), 1);


        // Make sure the ACL path is also updated after the action.
        final String userRoleAcl = userRoleAcls.getNode("0").getProperty("path").getString();
        assertEquals(userRoleAcl, "/renamedRole");
    }

    @Test
    public void validatesWhenNodeHasAclsForWorkspaceThatNoLongerExists() throws Exception {

        // GIVEN
        roleManager.createRole("testRole");
        Node roleNode = session.getRootNode().getNode("testRole");
        Node aclNode = roleNode.addNode("acl_Store", NodeTypes.ContentNode.NAME);
        Node entryNode = aclNode.addNode("0", NodeTypes.ContentNode.NAME);
        entryNode.setProperty("path", "/read");
        entryNode.setProperty("permissions", Permission.READ);
        entryNode.setProperty("__intermediary_format", true);
        entryNode.setProperty("accessType", 1);

        // WHEN
        JcrNodeAdapter roleItem = new JcrNodeAdapter(session.getRootNode().getNode("testRole"));

        createAction(roleItem).execute();

        // THEN
        assertTrue(session.nodeExists("/testRole/acl_Store/0"));
        assertEquals("/read", session.getProperty("/testRole/acl_Store/0/path").getString());
        assertEquals(Permission.READ, session.getProperty("/testRole/acl_Store/0/permissions").getLong());
    }

    @Test
    public void refusesToAddNodeWhenUserDoesNotHaveWriteAccessToUserRolesWorkspace() throws Exception {

        // GIVEN
        PrincipalUtil.findAccessControlList(subject, RepositoryConstants.USER_ROLES).getList().clear();

        // WHEN
        Node folderNode = session.getRootNode().addNode("folder", NodeTypes.Folder.NAME);
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(folderNode, NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        try {
            createAction(roleItem).execute();
            fail();
        } catch (ActionExecutionException e) {
            assertEquals(AccessControlException.class, e.getCause().getClass());
            // expected
        }
    }

    @Test
    public void testRemoveAclEntry() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");
        Node roleNode = session.getRootNode().getNode("testRole");
        Node aclNode = roleNode.addNode("acl_data", NodeTypes.ContentNode.NAME);
        Node entryNode = aclNode.addNode("0", NodeTypes.ContentNode.NAME);
        entryNode.setProperty("path", "/read");
        entryNode.setProperty("permissions", Permission.READ);
        entryNode.setProperty("__intermediary_format", true);
        entryNode.setProperty("accessType", 1);
        entryNode = aclNode.addNode("00", NodeTypes.ContentNode.NAME);
        entryNode.setProperty("path", "/test");
        entryNode.setProperty("permissions", Permission.READ);
        JcrNodeAdapter roleItem = new JcrNodeAdapter(session.getRootNode().getNode("testRole"));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertTrue(session.itemExists("/testRole/acl_data/0"));
        assertFalse(session.itemExists("/testRole/acl_data/00"));
    }

    // Workspace permission tests

    @Test
    public void deniesGivingReadPermissionWhenUserDoesNotHaveReadPermission() throws Exception {

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/read", Permission.READ, AccessControlList.ACCESS_TYPE_NODE);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void deniesGivingRecursiveReadPermissionWhenUserDoesNotHaveRecursiveReadPermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/read", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/read", Permission.READ, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void deniesGivingDenyPermissionWhenUserDoesNotHaveReadPermission() throws Exception {

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/read", Permission.NONE, AccessControlList.ACCESS_TYPE_NODE);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void allowsGivingDenyPermissionWhenUserHasReadPermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/read", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/read", Permission.NONE, AccessControlList.ACCESS_TYPE_NODE);

        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        assertTrue(session.nodeExists("/testRole/acl_website/0"));
        assertEquals("/read", session.getProperty("/testRole/acl_website/0/path").getString());
        assertEquals(Permission.NONE, session.getProperty("/testRole/acl_website/0/permissions").getLong());
    }

    @Test
    public void deniesGivingWritePermissionWhenUserDoesNotHaveWritePermission() throws Exception {

        // GIVEN
        grant(RepositoryConstants.WEBSITE, "/path", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/path", Permission.ALL, AccessControlList.ACCESS_TYPE_NODE);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void deniesGivingRecursiveWritePermissionWhenUserDoesNotHaveRecursiveWritePermission() throws Exception {

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addAclEntry(addAclItem(roleItem, "acl_website"), "0", "/path", Permission.ALL, AccessControlList.ACCESS_TYPE_NODE_AND_CHILDREN);

        assertFailsWithActionExecutionException(roleItem);
    }

    // URI permission tests

    @Test
    public void deniesGivingReadPermissionForUriWhenUserDoesNotHaveReadPermission() throws Exception {

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addUriAclEntry(addUriAclItem(roleItem, "acl_uri"), "0", "/read", Permission.READ);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void deniesGivingRecursiveReadPermissionForUriWhenUserDoesNotHaveRecursiveReadPermission() throws Exception {

        grant("uri", "/read", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addUriAclEntry(addUriAclItem(roleItem, "acl_uri"), "0", "/read/*", Permission.READ);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void deniesGivingDenyPermissionForUriWhenUserDoesNotHaveReadPermission() throws Exception {

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addUriAclEntry(addUriAclItem(roleItem, "acl_uri"), "0", "/read", Permission.NONE);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void allowsGivingDenyPermissionForUriWhenUserHasReadPermission() throws Exception {

        grant("uri", "/read", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addUriAclEntry(addUriAclItem(roleItem, "acl_uri"), "0", "/read", Permission.NONE);

        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        assertTrue(session.nodeExists("/testRole/acl_uri/0"));
        assertEquals("/read", session.getProperty("/testRole/acl_uri/0/path").getString());
        assertEquals(Permission.NONE, session.getProperty("/testRole/acl_uri/0/permissions").getLong());
    }

    @Test
    public void deniesGivingWritePermissionForUriWhenUserDoesNotHaveWritePermission() throws Exception {

        grant("uri", "/read", Permission.READ);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addUriAclEntry(addUriAclItem(roleItem, "acl_uri"), "0", "/read", Permission.ALL);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void deniesGivingRecursiveWritePermissionForUriWhenUserDoesNotHaveRecursiveWritePermission() throws Exception {

        grant("uri", "/write", Permission.ALL);

        // WHEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("testRole"));

        addUriAclEntry(addUriAclItem(roleItem, "acl_uri"), "0", "/write/*", Permission.ALL);

        assertFailsWithActionExecutionException(roleItem);
    }

    @Test
    public void validRoleNameIsUsedWhenCreatingRole() throws Exception {
        // GIVEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("test@test"));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertNull(roleManager.getRole("test@test"));
        assertNotNull(roleManager.getRole("test-test"));
        assertRoleHasReadAccessToItself("test-test");
    }

    @Test
    public void validRoleNameIsUsedWhenRenameRole() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");
        final Node testRoleNode = session.getRootNode().getNode("testRole");

        JcrNodeAdapter roleItem = new JcrNodeAdapter(testRoleNode);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<String>("renamed@role"));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertNull(roleManager.getRole("testRole"));
        assertNotNull(roleManager.getRole("renamed-role"));
        assertRoleHasReadAccessToItself("renamed-role");

        // Assert that no other node present under '/renamedRole/acl_userroles/' except for the existing one,
        // i.e there are no artifacts left after the action.
        final Node userRoleAcls = session.getNode("/renamed-role/acl_userroles/");
        assertEquals(userRoleAcls.getNodes().getSize(), 1);
        // Make sure the ACL path is also updated after the action.
        assertThat(userRoleAcls.getNode("0"), hasProperty("path", "/renamed-role"));
    }

    private void assertFailsWithActionExecutionException(JcrNewNodeAdapter roleItem) {
        try {
            createAction(roleItem).execute();
            fail();
        } catch (ActionExecutionException e) {
            // expected
        }
    }

    private void assertRoleHasReadAccessToItself(String roleName) throws RepositoryException {
        assertRoleHasReadAccessToPath(roleName, "/" + roleName);
    }

    private void assertRoleHasReadAccessToPath(String roleName, String path) throws RepositoryException {
        Optional<Node> node = getACLNodeForPath(roleName, path);
        assertTrue(node.isPresent());
        assertThat(node.get(), hasProperty("permissions", Permission.READ));
    }

    private Optional<Node> getACLNodeForPath(String roleName, final String path) throws RepositoryException {
        Node role = session.getNode("/"+ roleName);
        Node userRoles = role.getNode("acl_userroles");

        Optional<Node> aclNode = Iterators.tryFind(userRoles.getNodes(), new Predicate<Node>() {
            @Override
            public boolean apply(Node node) {
                return path.equals(PropertyUtil.getString(node, "path"));
            }
        });
        return aclNode;

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

    private JcrNewNodeAdapter addAclItem(AbstractJcrNodeAdapter roleItem, String aclName) {
        JcrNewNodeAdapter aclItem = new JcrNewNodeAdapter(roleItem.getJcrItem(), NodeTypes.ContentNode.NAME);
        aclItem.setNodeName(aclName);
        aclItem.addItemProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));
        roleItem.addChild(aclItem);
        return aclItem;
    }

    private JcrNewNodeAdapter addUriAclItem(AbstractJcrNodeAdapter roleItem, String aclName) {
        JcrNewNodeAdapter aclItem = new JcrNewNodeAdapter(roleItem.getJcrItem(), NodeTypes.ContentNode.NAME);
        aclItem.setNodeName(aclName);
        roleItem.addChild(aclItem);
        return aclItem;
    }

    private void addAclEntry(AbstractJcrNodeAdapter aclItem, String nodeName, String path, long permissions, long accessType) {
        JcrNewNodeAdapter entryItem = new JcrNewNodeAdapter(aclItem.getJcrItem(), NodeTypes.ContentNode.NAME);
        entryItem.setNodeName(nodeName);
        entryItem.addItemProperty(WorkspaceAccessFieldFactory.INTERMEDIARY_FORMAT_PROPERTY_NAME, new DefaultProperty<String>(String.class, "true"));
        entryItem.addItemProperty(AccessControlList.PATH_PROPERTY_NAME, new DefaultProperty<String>(path));
        entryItem.addItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME, new DefaultProperty<Long>(permissions));
        entryItem.addItemProperty(WorkspaceAccessFieldFactory.ACCESS_TYPE_PROPERTY_NAME, new DefaultProperty<Long>(accessType));
        aclItem.addChild(entryItem);
    }

    private void addUriAclEntry(AbstractJcrNodeAdapter aclItem, String nodeName, String path, long permissions) {
        JcrNewNodeAdapter entryItem = new JcrNewNodeAdapter(aclItem.getJcrItem(), NodeTypes.ContentNode.NAME);
        entryItem.setNodeName(nodeName);
        entryItem.addItemProperty(AccessControlList.PATH_PROPERTY_NAME, new DefaultProperty<String>(path));
        entryItem.addItemProperty(AccessControlList.PERMISSIONS_PROPERTY_NAME, new DefaultProperty<Long>(permissions));
        aclItem.addChild(entryItem);
    }

    private SaveRoleDialogAction createAction(Item item) {
        EditorValidator validator = mock(EditorValidator.class);
        when(validator.isValid()).thenReturn(true);
        return new SaveRoleDialogAction(mock(SaveRoleDialogActionDefinition.class), item, validator, mock(EditorCallback.class), securitySupport);
    }

    private static class MockSessionSecurityContentDecorator extends AbstractContentDecorator {

        private Subject subject;

        public MockSessionSecurityContentDecorator(Subject subject) {
            this.subject = subject;
        }

        @Override
        public boolean isMultipleWrapEnabled() {
            return true;
        }

        @Override
        public Session wrapSession(Session session) {
            return new ContentDecoratorSessionWrapper(session, MockSessionSecurityContentDecorator.this) {

                @Override
                public boolean hasPermission(String absPath, String actions) throws RepositoryException {
                    ACL acl = PrincipalUtil.findAccessControlList(subject, getWrappedSession().getWorkspace().getName());
                    if (acl == null) return false;
                    AccessManagerImpl ami = new AccessManagerImpl();
                    ami.setPermissionList(acl.getList());
                    long permissions = PermissionUtil.convertPermissions(actions);
                    return ami.isGranted(absPath, permissions);
                }

                @Override
                public void checkPermission(String absPath, String actions) throws RepositoryException {
                    if (!hasPermission(absPath, actions)) {
                        throw new AccessControlException(actions);
                    }
                }
            };
        }
    }
}
