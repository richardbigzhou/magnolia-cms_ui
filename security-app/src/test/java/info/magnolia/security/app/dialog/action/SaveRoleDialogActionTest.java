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

import static info.magnolia.cms.security.operations.AccessDefinition.DEFAULT_SUPERUSER_ROLE;
import static info.magnolia.jcr.nodebuilder.Ops.*;
import static info.magnolia.repository.RepositoryConstants.*;
import static info.magnolia.security.app.dialog.field.WorkspaceAccessControlList.*;
import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.nodebuilder.NodeBuilder;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeTypes.ContentNode;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.dialog.field.AccessControlList;
import info.magnolia.security.app.dialog.field.WorkspaceAccessControlList;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.ModelConstants;

import java.util.List;

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
import com.vaadin.data.util.ObjectProperty;

public class SaveRoleDialogActionTest extends RepositoryTestCase {

    private SecuritySupport securitySupport;
    private Session session;
    private MgnlRoleManager roleManager;
    private Subject subject;
    private EditorValidator validator;

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
        MockWebContext context = (MockWebContext) MgnlContext.getInstance();
        context.setSubject(subject);

        User user = mock(User.class);
        when(user.hasRole(eq(DEFAULT_SUPERUSER_ROLE))).thenReturn(false);
        context.setUser(user);
    }

    @Test
    public void createRoleAddsWorkspaceAcls() throws RepositoryException, ActionExecutionException {
        // GIVEN
        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        acl.addEntry(new WorkspaceAccessControlList.Entry(Permission.READ, ACCESS_TYPE_NODE, "/read"));

        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("testRole"));
        roleItem.addItemProperty(formatPropertyName(WEBSITE), new ObjectProperty<>(acl));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        Node aclNode = session.getNode("/testRole/acl_website");
        List<Node> nodes = NodeUtil.asList(NodeUtil.getNodes(aclNode));
        assertThat(nodes, hasSize(1));
        assertThat(nodes, contains(
                allOf(
                        hasProperty("path", "/read"),
                        hasProperty("permissions", 8L))
        ));
    }

    @Test
    public void createRoleWithRecursiveWorkspaceAclAddsTwoEntries() throws RepositoryException, ActionExecutionException {
        // GIVEN
        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        acl.addEntry(new WorkspaceAccessControlList.Entry(Permission.READ, ACCESS_TYPE_NODE_AND_CHILDREN, "/read"));

        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("testRole"));
        roleItem.addItemProperty(formatPropertyName(WEBSITE), new ObjectProperty<>(acl));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        Node aclNode = session.getNode("/testRole/acl_website");
        List<Node> nodes = NodeUtil.asList(NodeUtil.getNodes(aclNode));
        assertThat(nodes, hasSize(2));
        assertThat(nodes, containsInAnyOrder(
                allOf(
                        hasProperty("path", "/read"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/read/*"),
                        hasProperty("permissions", 8L))
        ));
    }

    @Test
    public void createRoleWithAclsForUserRolesWorkspace() throws Exception {
        // GIVEN
        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        acl.addEntry(new WorkspaceAccessControlList.Entry(Permission.READ, ACCESS_TYPE_NODE, "/read"));

        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("testRole"));
        roleItem.addItemProperty(formatPropertyName(USER_ROLES), new ObjectProperty<>(acl));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");
        assertRoleHasReadAccessToPath("testRole", "/read");

    }

    @Test
    public void createRoleWithEmptyAcls() throws Exception {
        // GIVEN
        final JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("testRole"));
        roleItem.addItemProperty(formatPropertyName(WEBSITE), new ObjectProperty<>(new WorkspaceAccessControlList()));
        WorkspaceAccessControlList aclWithEmptyPath = new WorkspaceAccessControlList();
        aclWithEmptyPath.addEntry(new WorkspaceAccessControlList.Entry(0, ""));
        roleItem.addItemProperty(formatPropertyName(CONFIG), new ObjectProperty<>(aclWithEmptyPath));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertThat(session.getNode("/testRole"), not(hasNode("acl_website")));
        assertThat(session.getNode("/testRole"), not(hasNode("acl_config")));
    }

    @Test
    public void editRoleAddsWorkspaceAcls() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");

        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        acl.addEntry(new WorkspaceAccessControlList.Entry(Permission.READ, ACCESS_TYPE_NODE, "/read"));

        JcrNodeAdapter roleItem = new JcrNodeAdapter(session.getRootNode().getNode("testRole"));
        roleItem.addItemProperty(formatPropertyName(WEBSITE), new ObjectProperty<>(acl));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        Node aclNode = session.getNode("/testRole/acl_website");
        List<Node> nodes = NodeUtil.asList(NodeUtil.getNodes(aclNode));
        assertThat(nodes, hasSize(1));
        assertThat(nodes, contains(
                allOf(
                        hasProperty("path", "/read"),
                        hasProperty("permissions", 8L))
        ));
    }

    @Test
    public void createRoleAddsUriAcls() throws RepositoryException, ActionExecutionException {
        // GIVEN
        AccessControlList<AccessControlList.Entry> acl = new AccessControlList<>();
        acl.addEntry(new AccessControlList.Entry(Permission.READ, "/read"));

        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("testRole"));
        roleItem.addItemProperty(formatPropertyName("uri"), new ObjectProperty<>(acl));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("testRole");

        Node aclNode = session.getNode("/testRole/acl_uri");
        List<Node> nodes = NodeUtil.asList(NodeUtil.getNodes(aclNode));
        assertThat(nodes, hasSize(1));
        assertThat(nodes, contains(
                allOf(
                        hasProperty("path", "/read"),
                        hasProperty("permissions", 8L))
        ));
    }

    @Test
    public void renameRole() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");
        Node testRoleNode = session.getRootNode().getNode("testRole");
        Node aclNode = testRoleNode.getNode("acl_userroles");
        // sanity check: has ACE for self node
        assertThat(NodeUtil.getNodes(aclNode), contains(
                allOf(
                        hasProperty("path", "/testRole"),
                        hasProperty("permissions", 8L))
        ));

        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        acl.readEntries(aclNode);

        JcrNodeAdapter roleItem = new JcrNodeAdapter(testRoleNode);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("renamedRole"));
        roleItem.addItemProperty(formatPropertyName(USER_ROLES), new ObjectProperty<>(acl));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertRoleHasReadAccessToItself("renamedRole");

        // Assert that no other node present under '/renamedRole/acl_userroles/' except for the existed one,
        // i.e there are no artifacts left after the action.
        aclNode = session.getNode("/renamedRole/acl_userroles");
        List<Node> nodes = NodeUtil.asList(NodeUtil.getNodes(aclNode));
        assertThat(nodes, hasSize(1));
        assertThat(nodes, contains(
                allOf(
                        hasProperty("path", "/renamedRole"),
                        hasProperty("permissions", 8L))
        ));
    }

    @Test
    public void renameRoleAndModifyOtherUserRoleAcls() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");
        Node testRoleNode = session.getRootNode().getNode("testRole");
        Node aclNode = testRoleNode.getNode("acl_userroles");

        // sanity check: has ACE for self node
        assertThat(NodeUtil.getNodes(aclNode), contains(
                allOf(
                        hasProperty("path", "/testRole"),
                        hasProperty("permissions", 8L))
        ));
        // + two other ACEs in userroles workspace
        new NodeBuilder(aclNode,
                addNode("00", ContentNode.NAME).then(
                        addProperty("path", "/admins/*"),
                        addProperty("permissions", String.valueOf(Permission.READ))),
                addNode("01", ContentNode.NAME).then(
                        addProperty("path", "/editors/*"),
                        addProperty("permissions", String.valueOf(Permission.ALL)))
        ).exec();

        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        acl.readEntries(aclNode);

        JcrNodeAdapter roleItem = new JcrNodeAdapter(testRoleNode);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("renamedRole"));
        roleItem.addItemProperty(formatPropertyName(USER_ROLES), new ObjectProperty<>(acl));
        acl.removeEntry(new WorkspaceAccessControlList.Entry(Permission.ALL, ACCESS_TYPE_CHILDREN, "/editors"));
        acl.addEntry(new WorkspaceAccessControlList.Entry(Permission.ALL, ACCESS_TYPE_CHILDREN, "/edited"));

        // WHEN
        createAction(roleItem).execute();

        // THEN own role was renamed
        assertRoleHasReadAccessToItself("renamedRole");
        aclNode = session.getNode("/renamedRole/acl_userroles/");
        List<Node> nodes = NodeUtil.asList(NodeUtil.getNodes(aclNode));
        assertThat(nodes, hasSize(3));
        assertThat(nodes, containsInAnyOrder(
                allOf(
                        hasProperty("path", "/renamedRole"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/admins/*"),
                        hasProperty("permissions", 8L)),
                allOf(
                        hasProperty("path", "/edited/*"),
                        hasProperty("permissions", 63L))
        ));
    }

    @Test
    public void validatesWhenNodeHasAclsForWorkspaceThatNoLongerExists() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");
        Node roleNode = session.getRootNode().getNode("testRole");
        Node aclNode = roleNode.addNode("acl_Store", ContentNode.NAME);
        Node entryNode = aclNode.addNode("0", ContentNode.NAME);
        entryNode.setProperty("path", "/read");
        entryNode.setProperty("permissions", Permission.READ);
        entryNode.setProperty("accessType", 1);

        JcrNodeAdapter roleItem = new JcrNodeAdapter(session.getRootNode().getNode("testRole"));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertThat(roleNode, hasNode(allOf(
                nodeName("acl_Store"), hasNode(allOf(
                        nodeName("0"),
                        hasProperty("path", "/read"),
                        hasProperty("permissions", Permission.READ)
                ))
        )));
    }

    @Test
    public void removeAclEntry() throws Exception {
        // GIVEN pre-existing permissions
        roleManager.createRole("testRole");
        Node roleNode = session.getRootNode().getNode("testRole");
        Node aclNode = roleNode.addNode("acl_data", ContentNode.NAME);
        Node entryNode = aclNode.addNode("0", ContentNode.NAME);
        entryNode.setProperty("path", "/read");
        entryNode.setProperty("permissions", Permission.READ);
        entryNode = aclNode.addNode("00", ContentNode.NAME);
        entryNode.setProperty("path", "/test");
        entryNode.setProperty("permissions", Permission.READ);

        // we keep /read and we remove /test
        AccessControlList<WorkspaceAccessControlList.Entry> acl = new WorkspaceAccessControlList();
        acl.addEntry(new WorkspaceAccessControlList.Entry(Permission.READ, ACCESS_TYPE_NODE, "/read"));

        JcrNodeAdapter roleItem = new JcrNodeAdapter(roleNode);
        roleItem.addItemProperty(formatPropertyName("data"), new ObjectProperty<>(acl));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertThat(aclNode, hasNode("0"));
        assertThat(aclNode, not(hasNode("00")));
    }

    @Test
    public void removingAllAclEntriesAlsoRemovesAclNode() throws Exception {
        // GIVEN pre-existing permissions
        roleManager.createRole("testRole");
        Node roleNode = session.getRootNode().getNode("testRole");
        Node aclNode = roleNode.addNode("acl_data", ContentNode.NAME);
        Node entryNode = aclNode.addNode("0", ContentNode.NAME);
        entryNode.setProperty("path", "/read");
        entryNode.setProperty("permissions", Permission.READ);
        entryNode = aclNode.addNode("00", ContentNode.NAME);
        entryNode.setProperty("path", "/test");
        entryNode.setProperty("permissions", Permission.READ);

        JcrNodeAdapter roleItem = new JcrNodeAdapter(roleNode);
        roleItem.addItemProperty(formatPropertyName("data"), new ObjectProperty<>(new WorkspaceAccessControlList()));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertThat(roleNode, not(hasNode("acl_data")));
    }

    @Test
    public void validRoleNameIsUsedWhenCreatingRole() throws Exception {
        // GIVEN
        JcrNewNodeAdapter roleItem = new JcrNewNodeAdapter(session.getRootNode(), NodeTypes.Role.NAME);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("test@test"));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertThat(roleManager.getRole("test@test"), is(nullValue()));
        assertThat(roleManager.getRole("test-test"), is(not(nullValue())));
        assertRoleHasReadAccessToItself("test-test");
    }

    @Test
    public void validRoleNameIsUsedWhenRenameRole() throws Exception {
        // GIVEN
        roleManager.createRole("testRole");
        final Node testRoleNode = session.getRootNode().getNode("testRole");

        JcrNodeAdapter roleItem = new JcrNodeAdapter(testRoleNode);
        roleItem.addItemProperty(ModelConstants.JCR_NAME, new DefaultProperty<>("renamed@role"));

        // WHEN
        createAction(roleItem).execute();

        // THEN
        assertThat(roleManager.getRole("testRole"), is(nullValue()));
        assertThat(roleManager.getRole("renamed-role"), is(not(nullValue())));
        assertRoleHasReadAccessToItself("renamed-role");

        // Assert that no other node present under '/renamedRole/acl_userroles/' except for the existing one,
        // i.e there are no artifacts left after the action.
        final Node userRoleAcls = session.getNode("/renamed-role/acl_userroles/");
        assertEquals(userRoleAcls.getNodes().getSize(), 1);
        // Make sure the ACL path is also updated after the action.
        assertThat(userRoleAcls.getNode("0"), hasProperty("path", "/renamed-role"));
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

    private SaveRoleDialogAction createAction(Item item) {
        validator = mock(EditorValidator.class);
        when(validator.isValid()).thenReturn(true);
        return new SaveRoleDialogAction(mock(SaveRoleDialogActionDefinition.class), item, validator, mock(EditorCallback.class), securitySupport);
    }

    private String formatPropertyName(String name){
        return "acl_" + name;
    }
}
