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

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.operations.VoterBasedConfiguredAccessDefinition;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.voting.voters.RoleBaseVoter;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ConvertAclToAppPermissionTask}.
 */
public class ConvertAclToAppPermissionTaskTest extends RepositoryTestCase {

    private Session config;
    private Session userRoles;
    private InstallContext installContext;
    private Node permission;
    private ConvertAclToAppPermissionTask task;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        config = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        config.getRootNode().addNode("newApp");
        config.save();

        userRoles = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        final Node acl = NodeUtil.createPath(userRoles.getRootNode(), "someUserRole", NodeTypes.Role.NAME).addNode("acl_uri", NodeTypes.ContentNode.NAME);
        permission = acl.addNode("0", NodeTypes.ContentNode.NAME);
        userRoles.save();

        installContext = mock(InstallContext.class);
        when(installContext.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(config);
        when(installContext.getJCRSession(RepositoryConstants.USER_ROLES)).thenReturn(userRoles);

        task = new ConvertAclToAppPermissionTask("name", "description", "oldURL", "/newApp", true);
    }

    @Test
    public void dennyPermissionForRole() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 0);
        userRoles.save();

        // WHEN
        task.execute(installContext);

        // THEN
        assertThat(userRoles.getRootNode(), not(hasNode("someUser/acl_uri/0")));
        assertThat(config.getNode("/newApp/permissions"), hasProperty(ConvertAclToAppPermissionTask.PROPERTY_CLASS_NAME, VoterBasedConfiguredAccessDefinition.class.getName()));
        assertThat(config.getNode("/newApp/permissions/deniedRoles"), hasProperty(ConvertAclToAppPermissionTask.PROPERTY_CLASS_NAME, RoleBaseVoter.class.getName()));
        assertThat(config.getNode("/newApp/permissions/deniedRoles"), hasProperty(ConvertAclToAppPermissionTask.PROPERTY_NOT_NAME, "true"));
        assertThat(config.getRootNode(), hasNode("newApp/permissions/deniedRoles/roles"));
        assertThat(config.getNode("/newApp/permissions/deniedRoles/roles"), hasProperty("someUserRole"));
    }

    @Test
    public void addPermissionOnlyForSomeRole() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 4);
        userRoles.save();

        // WHEN
        task.execute(installContext);

        // THEN
        assertThat(userRoles.getRootNode(), not(hasNode("someUser/acl_uri/0")));
        assertThat(config.getRootNode(), hasNode("newApp/permissions/roles"));
        assertThat(config.getNode("/newApp/permissions/roles"), hasProperty("someUserRole"));
    }

    @Test
    public void addPermissionToDeniedRoles() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 0);
        NodeUtil.createPath(config.getRootNode(), "/newApp/permissions/", NodeTypes.ContentNode.NAME).setProperty(ConvertAclToAppPermissionTask.PROPERTY_CLASS_NAME, VoterBasedConfiguredAccessDefinition.class.getName());
        NodeUtil.createPath(config.getRootNode(), "/newApp/permissions/deniedRoles/roles/", NodeTypes.ContentNode.NAME).setProperty("nameDoesntMatter", "someUserRole");
        userRoles.save();

        // WHEN
        task.execute(installContext);

        // THEN
        assertThat(userRoles.getRootNode(), not(hasNode("someUser/acl_uri/0")));
        assertThat(config.getRootNode(), hasNode("newApp/permissions/deniedRoles/roles"));
        assertThat(config.getNode("/newApp/permissions/deniedRoles/roles"), hasProperty("nameDoesntMatter"));
        assertThat(config.getNode("/newApp/permissions/deniedRoles/roles"), hasProperty("someUserRole"));
    }

    @Test
    public void dontAddPermissionIfAlreadySet() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 4);
        NodeUtil.createPath(config.getRootNode(), "/newApp/permissions/roles/", NodeTypes.ContentNode.NAME).setProperty("someUserRole", "someUserRole");
        userRoles.save();

        // WHEN
        task.execute(installContext);

        // THEN
        assertThat(userRoles.getRootNode(), not(hasNode("someUser/acl_uri/0")));
        assertThat(config.getRootNode(), hasNode("newApp/permissions/roles"));
        assertThat(config.getNode("/newApp/permissions/roles"), hasProperty("someUserRole"));
    }

    @Test
    public void moveToAllowedRolesIfSomePermissionIsSetAlreadyAndCreateDeniedRoles() throws Exception {
        // GIVEN
        permission.setProperty("path", "oldURL");
        permission.setProperty("permissions", 0);
        NodeUtil.createPath(config.getRootNode(), "/newApp/permissions/roles/", NodeTypes.ContentNode.NAME).setProperty("nameDoesntMatter", "someUserRole");
        userRoles.save();

        // WHEN
        task.execute(installContext);

        // THEN
        assertThat(userRoles.getRootNode(), not(hasNode("someUser/acl_uri/0")));
        assertThat(config.getNode("/newApp/permissions"), hasProperty(ConvertAclToAppPermissionTask.PROPERTY_CLASS_NAME, VoterBasedConfiguredAccessDefinition.class.getName()));
        assertThat(config.getNode("/newApp/permissions/allowedRoles"), hasProperty(ConvertAclToAppPermissionTask.PROPERTY_CLASS_NAME, RoleBaseVoter.class.getName()));
        assertThat(config.getNode("/newApp/permissions/deniedRoles"), hasProperty(ConvertAclToAppPermissionTask.PROPERTY_CLASS_NAME, RoleBaseVoter.class.getName()));
        assertThat(config.getNode("/newApp/permissions/deniedRoles"), hasProperty(ConvertAclToAppPermissionTask.PROPERTY_NOT_NAME, "true"));
        assertThat(config.getRootNode(), hasNode("newApp/permissions/allowedRoles/roles"));
        assertThat(config.getNode("/newApp/permissions/allowedRoles/roles"), hasProperty("nameDoesntMatter"));
        assertThat(config.getRootNode(), hasNode("newApp/permissions/deniedRoles/roles"));
        assertThat(config.getNode("/newApp/permissions/deniedRoles/roles"), hasProperty("someUserRole"));
    }
}
