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
package info.magnolia.ui.admincentral.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link RegisterAppIntoAppLauncherTask}.
 */
public class RegisterAppIntoAppLauncherTaskTest {

    private final String APP_LAUNCHER_LAYOUT_GROUPS_PATH = "/modules/ui-admincentral/config/appLauncherLayout/groups";

    private Session session;
    private InstallContext ctx;
    private Node appLauncherGroupsNode;
    private Node existingGroupNode;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(RepositoryConstants.CONFIG);

        ctx = mock(InstallContext.class);
        when(ctx.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(session);
        when(ctx.getConfigJCRSession()).thenReturn(session);

        appLauncherGroupsNode = NodeUtil.createPath(session.getRootNode(), APP_LAUNCHER_LAYOUT_GROUPS_PATH, NodeTypes.ContentNode.NAME);
        existingGroupNode = NodeUtil.createPath(appLauncherGroupsNode, "existingGroup", NodeTypes.ContentNode.NAME);
    }

    @Test
    public void testRegisterAppWhenGroupExist() throws Exception {
        // GIVEN
        RegisterAppIntoAppLauncherTask task = new RegisterAppIntoAppLauncherTask("newAppName", "existingGroup");

        // WHEN
        task.execute(ctx);

        // THEN
        assertThat(existingGroupNode, hasNode("apps/newAppName"));
        verify(ctx, never()).warn(anyString());
    }

    @Test
    public void testDoNotRegisterAppWhenGroupDoesNotExist() throws Exception {
        // GIVEN
        RegisterAppIntoAppLauncherTask task = new RegisterAppIntoAppLauncherTask("newAppName", "notExistingGroup");

        // WHEN
        task.execute(ctx);

        // THEN
        assertThat(appLauncherGroupsNode, not(hasNode("notExistingGroup")));
        verify(ctx).warn("Can't register newAppName app, because notExistingGroup group does not exist.");
    }

    @Test
    public void testCreateGroupWhenRegisterAppGroupDoesNotExistAndCreateGroupIsSetToTrue() throws Exception {
        // GIVEN
        RegisterAppIntoAppLauncherTask task = new RegisterAppIntoAppLauncherTask("newAppName", "notExistingGroup", true);

        // WHEN
        task.execute(ctx);

        // THEN
        assertThat(appLauncherGroupsNode, allOf(hasNode("notExistingGroup"), hasNode("notExistingGroup/apps/newAppName")));
        verify(ctx, never()).warn(anyString());
    }

    @Test
    public void testCreateGroupAndSetPermissionsWhenRegisterAppGroupDoesNotExistAndCreateGroupIsSetToTrue() throws Exception {
        // GIVEN
        RegisterAppIntoAppLauncherTask task = new RegisterAppIntoAppLauncherTask("newAppName", "notExistingGroup", true, Arrays.asList("role1", "role2", "role3"));

        // WHEN
        task.execute(ctx);

        // THEN
        assertThat(appLauncherGroupsNode, allOf(hasNode("notExistingGroup"), hasNode("notExistingGroup/apps/newAppName"), hasNode("notExistingGroup/permissions/roles")));
        Node permissionsRolesNode = appLauncherGroupsNode.getNode("notExistingGroup/permissions/roles");
        assertThat(permissionsRolesNode, allOf(hasProperty("role1", "role1"), hasProperty("role2", "role2"), hasProperty("role3", "role3")));
        verify(ctx, never()).warn(anyString());
    }

    @Test
    public void testCreateGroupAndSetPermanentAndColorPropertyRegisterAppGroupDoesNotExistAndCreateGroupIsSetToTrue() throws Exception {
        // GIVEN
        RegisterAppIntoAppLauncherTask task = new RegisterAppIntoAppLauncherTask("newAppName", "notExistingGroup", true, true, "#000000");

        // WHEN
        task.execute(ctx);

        // THEN
        assertThat(appLauncherGroupsNode, allOf(hasNode("notExistingGroup"), hasNode("notExistingGroup/apps/newAppName")));
        Node groupNode = appLauncherGroupsNode.getNode("notExistingGroup");
        assertThat(groupNode, allOf(hasProperty("permanent", true), hasProperty("color", "#000000")));
        verify(ctx, never()).warn(anyString());
    }

    @Test
    public void testRegisterAppWhenGroupDoesNotExistButFallbackGroupExists() throws Exception {
        // GIVEN
        RegisterAppIntoAppLauncherTask task = new RegisterAppIntoAppLauncherTask("newAppName", "notExistingGroup", "existingGroup");

        // WHEN
        task.execute(ctx);

        // THEN
        assertThat(existingGroupNode, hasNode("apps/newAppName"));
        verify(ctx, never()).warn(anyString());
    }

    @Test
    public void testDoNotRegisterAppWhenGroupAndFallbackGroupDoNotExist() throws Exception {
        // GIVEN
        RegisterAppIntoAppLauncherTask task = new RegisterAppIntoAppLauncherTask("newAppName", "notExistingGroup", "notExistingGroup2");

        // WHEN
        task.execute(ctx);

        // THEN
        assertThat(appLauncherGroupsNode, allOf(not(hasNode("notExistingGroup")), not(hasNode("notExistingGroup2"))));
        verify(ctx).warn("Can't register newAppName app, because notExistingGroup group and notExistingGroup2 fallback group do not exist.");
    }
}
