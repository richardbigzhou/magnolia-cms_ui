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
package info.magnolia.ui.admincentral.setup;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ConvertAclToAppPermissionTask}.
 */
public class ConvertAclToAppPermissionTaskTest {

    private MockSession userRoles;
    private InstallContext installContext;
    private ConvertAclToAppPermissionTask task;
    private MockSession config;
    private Node permission;
    private final QueryResult queryResult = mock(QueryResult.class);

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();

        installContext = mock(InstallContext.class);
        Workspace workspace = mock(Workspace.class);
        QueryManager qm = mock(QueryManager.class);
        Query query = mock(Query.class);

        config = new MockSession(RepositoryConstants.CONFIG);
        config.getRootNode().addNode("newApp");

        userRoles = new MockSession(RepositoryConstants.USER_ROLES);
        userRoles.setWorkspace(workspace);

        Node acl = NodeUtil.createPath(userRoles.getRootNode(), "someUserRole/acl_uri", NodeTypes.Role.NAME);
        permission = acl.addNode("0", NodeTypes.ContentNode.NAME);
        NodeIterator nodeIterator = acl.getNodes();

        when(queryResult.getNodes()).thenReturn(acl.getNodes());

        MockContext context = (MockContext) MgnlContext.getInstance();
        context.addSession(RepositoryConstants.CONFIG, config);
        context.addSession(RepositoryConstants.USER_ROLES, userRoles);

        when(installContext.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(config);
        when(installContext.getJCRSession(RepositoryConstants.USER_ROLES)).thenReturn(userRoles);
        when(workspace.getQueryManager()).thenReturn(qm);
        when(qm.createQuery(anyString(), anyString())).thenReturn(query);
        when(query.execute()).thenReturn(queryResult);
        when(queryResult.getNodes()).thenReturn(nodeIterator);

        task = new ConvertAclToAppPermissionTask("name", "description", "oldURL", "newApp", true);
        RoleManager roleManager = new MgnlRoleManager();
        ComponentsTestUtil.setInstance(RoleManager.class, roleManager);
        roleManager.createRole("superuser");
    }

    @Test
    public void testAddPermissionOnlyForSuperuser() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 0);

        // WHEN
        task.execute(installContext);

        // THEN
        assertFalse(userRoles.itemExists("/someUser/acl_uri/0"));
        assertTrue(config.itemExists("/newApp/permissions/roles/superuser"));
    }

    @Test
    public void testAddPermissionOnlyForSomeRole() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 4);

        // WHEN
        task.execute(installContext);

        // THEN
        assertFalse(userRoles.itemExists("/someUser/acl_uri/0"));
        assertTrue(config.itemExists("/newApp/permissions/roles/someUserRole"));
    }

    @Test
    public void testDontAddPermissionIfAlreadySet() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 4);
        NodeUtil.createPath(config.getRootNode(), "/newApp/permissions/roles/", NodeTypes.ContentNode.NAME).setProperty("someUserRole", "someUserRole");

        // WHEN
        task.execute(installContext);

        // THEN
        assertFalse(userRoles.itemExists("/someUser/acl_uri/0"));
        assertTrue(config.itemExists("/newApp/permissions/roles/someUserRole"));
    }

    @Test
    public void testDontAddSuperuserPermissionToDenyAccessIfSomePermissionIsSetAlready() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 0);
        NodeUtil.createPath(config.getRootNode(), "/newApp/permissions/roles/", NodeTypes.ContentNode.NAME).setProperty("nameDoesntMatter", "someUserRole");

        // WHEN
        task.execute(installContext);

        // THEN
        assertTrue(config.itemExists("/newApp/permissions/roles/nameDoesntMatter"));
        assertFalse(config.itemExists("/newApp/permissions/roles/superuser"));
        assertFalse(userRoles.itemExists("/someUser/acl_uri/0"));
    }

    public void tearDown() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }
}
