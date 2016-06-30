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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ConvertListAclToAppPermissionsTask}.
 */
public class ConvertListAclToAppPermissionsTaskTest {

    private MockSession userRoles;
    private InstallContext installContext;
    private MockSession config;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();

        installContext = mock(InstallContext.class);
        Workspace workspace = mock(Workspace.class);
        QueryManager qm = mock(QueryManager.class);
        Query query = mock(Query.class);
        QueryResult queryResult = mock(QueryResult.class);

        config = new MockSession(RepositoryConstants.CONFIG);
        config.getRootNode().addNode("newApp1");
        config.getRootNode().addNode("newApp2");

        userRoles = new MockSession(RepositoryConstants.USER_ROLES);
        userRoles.setWorkspace(workspace);
        Node acl1 = NodeUtil.createPath(userRoles.getRootNode(), "someUserRole1/acl_uri", NodeTypes.Role.NAME);
        Node permission1 = acl1.addNode("0", NodeTypes.ContentNode.NAME);
        permission1.setProperty("permissions", 0);

        Node acl2 = NodeUtil.createPath(userRoles.getRootNode(), "someUserRole2/acl_uri", NodeTypes.Role.NAME);
        Node permission2 = acl2.addNode("1", NodeTypes.ContentNode.NAME);
        permission2.setProperty("permissions", 8);

        NodeIterator nodeIterator1 = acl1.getNodes();
        NodeIterator nodeIterator2 = acl2.getNodes();

        MockContext context = (MockContext) MgnlContext.getInstance();
        context.addSession(RepositoryConstants.CONFIG, config);
        context.addSession(RepositoryConstants.USER_ROLES, userRoles);

        when(installContext.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(config);
        when(installContext.getJCRSession(RepositoryConstants.USER_ROLES)).thenReturn(userRoles);
        when(workspace.getQueryManager()).thenReturn(qm);
        when(qm.createQuery(anyString(), anyString())).thenReturn(query);
        when(query.execute()).thenReturn(queryResult);
        when(queryResult.getNodes()).thenReturn(nodeIterator1, nodeIterator2);
    }

    @Test
    public void testDoExecute() throws Exception {
        // GIVEN
        Map<String, String[]> map = new LinkedHashMap<String, String[]>();
        map.put("oldURL1", new String[] { "newApp1" });
        map.put("oldURL2", new String[] { "newApp2" });
        Task task = new ConvertListAclToAppPermissionsTask("name", "description", map, false);

        // WHEN
        task.execute(installContext);

        // THEN
        assertTrue(config.itemExists("/newApp1/permissions/deniedRoles/roles/someUserRole1"));
        assertTrue(config.itemExists("/newApp2/permissions/roles/someUserRole2"));
    }

    public void after() {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }
}
