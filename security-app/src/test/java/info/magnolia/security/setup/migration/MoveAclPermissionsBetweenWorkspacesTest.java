/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.security.setup.migration;

import static org.junit.Assert.assertTrue;

import static org.junit.Assert.*;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.module.InstallContext.Message;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Main test class for {@link MoveAclPermissionsBetweenWorkspaces}.
 */
public class MoveAclPermissionsBetweenWorkspacesTest extends RepositoryTestCase {

    protected InstallContextImpl installContext;
    private String userRoleNodeTreeDefinition = "sample-userroles.properties";
    private final String sourceWorkspaceName = RepositoryConstants.CONFIG;
    private final String targetWorkspaceName = RepositoryConstants.WEBSITE;
    private Session userRoleSession;
    private Session webSiteSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Init userRole workspace with the nodeType property file
        userRoleSession = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        URL resource = ClasspathResourcesUtil.getResource(userRoleNodeTreeDefinition);
        new PropertiesImportExport().createNodes(userRoleSession.getRootNode(), resource.openStream());
        userRoleSession.save();

        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        installContext = new InstallContextImpl(moduleRegistry);

        webSiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
    }

    @Test
    public void testDoExecuteNoSubPath() throws RepositoryException, TaskExecutionException {
        // GIVEN
        NodeUtil.createPath(webSiteSession.getRootNode(), "/demo-project/img/logos/logos1", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(webSiteSession.getRootNode(), "/demo-docs/logos", NodeTypes.ContentNode.NAME);
        MoveAclPermissionsBetweenWorkspaces task = new MoveAclPermissionsBetweenWorkspaces("name", "description", sourceWorkspaceName, targetWorkspaceName, null, false);

        // WHEN
        task.doExecute(installContext);

        // THEN
        assertFalse("acl_config should have been renamed as acl_website", userRoleSession.nodeExists("/roleV/acl_config"));
        assertTrue("acl_config should have been renamed as acl_website", userRoleSession.nodeExists("/roleV/acl_website"));
        assertTrue("As a path was not found, we should have one warning message", installContext.getMessages().get("General messages").get(0).getMessage().startsWith("The path '/demo-docs/img' defined for the following ACL '/roleV/acl_website/02' is no more valid"));

        assertEquals("/demo-project/img", userRoleSession.getNode("/roleV/acl_website/0").getProperty("path").getString());
        assertEquals("/demo-project/img/logos$", userRoleSession.getNode("/roleV/acl_website/00").getProperty("path").getString());
        assertEquals("/demo-docs/*", userRoleSession.getNode("/roleV/acl_website/01").getProperty("path").getString());
        assertEquals("/demo-docs/img", userRoleSession.getNode("/roleV/acl_website/02").getProperty("path").getString());
    }

    @Test
    public void testDoExecuteSubPathNoUpdate() throws RepositoryException, TaskExecutionException {
        // GIVEN
        NodeUtil.createPath(webSiteSession.getRootNode(), "/demo-project/img/logos/logos1", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(webSiteSession.getRootNode(), "/sunpath/demo-docs/logos", NodeTypes.ContentNode.NAME);
        List<String> subPaths = new ArrayList<String>();
        subPaths.add("titi");
        subPaths.add("sunpath");

        MoveAclPermissionsBetweenWorkspaces task = new MoveAclPermissionsBetweenWorkspaces("name", "description", sourceWorkspaceName, targetWorkspaceName, subPaths, false);

        // WHEN
        task.doExecute(installContext);

        // THEN
        assertFalse("acl_config should have been renamed as acl_website", userRoleSession.nodeExists("/roleV/acl_config"));
        assertTrue("acl_config should have been renamed as acl_website", userRoleSession.nodeExists("/roleV/acl_website"));
        // Check messages
        List<Message> messages = installContext.getMessages().get("General messages");
        assertNotNull(messages);
        assertEquals(2, messages.size());
        Message info = messages.get(0);
        assertEquals(InstallContext.MessagePriority.info, info.getPriority());
        assertTrue(info.getMessage().startsWith("The path '/demo-docs' defined for the following ACL '/roleV/acl_website/01' is no more valid. The following is Valid '/sunpath/demo-docs'"));
        Message warn = messages.get(1);
        assertEquals(InstallContext.MessagePriority.warning, warn.getPriority());
        assertTrue(warn.getMessage().startsWith("The path '/demo-docs/img' defined for the following ACL '/roleV/acl_website/02' is no more valid"));

        assertEquals("/demo-project/img", userRoleSession.getNode("/roleV/acl_website/0").getProperty("path").getString());
        assertEquals("/demo-project/img/logos$", userRoleSession.getNode("/roleV/acl_website/00").getProperty("path").getString());
        assertEquals("/demo-docs/*", userRoleSession.getNode("/roleV/acl_website/01").getProperty("path").getString());
        assertEquals("/demo-docs/img", userRoleSession.getNode("/roleV/acl_website/02").getProperty("path").getString());
    }

    @Test
    public void testDoExecuteSubPathUpdate() throws RepositoryException, TaskExecutionException {
        // GIVEN
        NodeUtil.createPath(webSiteSession.getRootNode(), "/sunpath1/demo-project/img/logos/logos1", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(webSiteSession.getRootNode(), "/sunpath2/demo-docs/logos", NodeTypes.ContentNode.NAME);
        List<String> subPaths = new ArrayList<String>();
        subPaths.add("titi");
        subPaths.add("/sunpath1");
        subPaths.add("/sunpath2/");

        MoveAclPermissionsBetweenWorkspaces task = new MoveAclPermissionsBetweenWorkspaces("name", "description", sourceWorkspaceName, targetWorkspaceName, subPaths, true);

        // WHEN
        task.doExecute(installContext);

        // THEN
        assertFalse("acl_config should have been renamed as acl_website", userRoleSession.nodeExists("/roleV/acl_config"));
        assertTrue("acl_config should have been renamed as acl_website", userRoleSession.nodeExists("/roleV/acl_website"));
        assertTrue("As a path was not found, we should have one warning message", installContext.getMessages().get("General messages").get(0).getMessage().startsWith("The path '/demo-docs/img' defined for the following ACL '/roleV/acl_website/02' is no more valid"));

        assertEquals("/sunpath1/demo-project/img", userRoleSession.getNode("/roleV/acl_website/0").getProperty("path").getString());
        assertEquals("/sunpath1/demo-project/img/logos$", userRoleSession.getNode("/roleV/acl_website/00").getProperty("path").getString());
        assertEquals("/sunpath2/demo-docs/*", userRoleSession.getNode("/roleV/acl_website/01").getProperty("path").getString());
        assertEquals("/demo-docs/img", userRoleSession.getNode("/roleV/acl_website/02").getProperty("path").getString());
    }

    @Test
    public void testInvalidPath() throws RepositoryException, TaskExecutionException {
        // GIVEN
        NodeUtil.createPath(webSiteSession.getRootNode(), "/demo-project", NodeTypes.ContentNode.NAME);
        MoveAclPermissionsBetweenWorkspaces task = new MoveAclPermissionsBetweenWorkspaces("name", "description", "dam", targetWorkspaceName, null, false);

        // WHEN
        task.doExecute(installContext);

        // THEN
        assertTrue(userRoleSession.nodeExists("/roleV/acl_website/0"));
        assertEquals("/*/*", userRoleSession.getNode("/roleV/acl_website/0").getProperty("path").getString());
        assertEquals(63l, userRoleSession.getNode("/roleV/acl_website/0").getProperty("permissions").getLong());
    }
}
