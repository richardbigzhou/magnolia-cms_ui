/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.security.setup;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for Security App.
 */
public class SecurityModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/security-app.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml"
        );
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new SecurityModuleVersionHandler();
    }

    @Test
    public void testUpdateTo501LabelIsAddFolder() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/addFolder", NodeTypes.ContentNode.NAME);
        action.setProperty("label", "New folder");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        String label = action.getProperty("label").getString();
        assertTrue("Add folder".equals(label));
    }

    @Test
    public void testUpdateTo501LabelIsAddUser() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/addUser", NodeTypes.ContentNode.NAME);
        action.setProperty("label", "New user");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        String label = action.getProperty("label").getString();
        assertTrue("Add user".equals(label));
    }

    @Test
    public void testUpdateTo501LabelIsAddGroup() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/groups/actions/addGroup", NodeTypes.ContentNode.NAME);
        action.setProperty("label", "New group");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        String label = action.getProperty("label").getString();
        assertTrue("Add group".equals(label));
    }

    @Test
    public void testUpdateTo501LabelIsAddRole() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/roles/actions/addRole", NodeTypes.ContentNode.NAME);
        action.setProperty("label", "New role");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        String label = action.getProperty("label").getString();
        assertTrue("Add role".equals(label));
    }

    @Test
    public void testUpdateTo51DeleteUserActionAvailability() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability", NodeTypes.ContentNode.NAME);
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(action.hasProperty("ruleClass"));
        assertEquals("info.magnolia.security.app.action.availability.IsNotCurrentUserRule", action.getProperty("ruleClass").getString());
    }

    @Test
    public void testUpdateTo51DeleteGroupActionClass() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/groups/actions/deleteGroup", NodeTypes.ContentNode.NAME);
        action.setProperty("class", "oldValue");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(action.hasProperty("class"));
        assertEquals("info.magnolia.security.app.action.DeleteGroupActionDefinition", action.getProperty("class").getString());
    }

    @Test
    public void testUpdateTo51DeleteRoleActionClass() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/roles/actions/deleteRole", NodeTypes.ContentNode.NAME);
        action.setProperty("class", "oldValue");
        action.getSession().save();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(action.hasProperty("class"));
        assertEquals("info.magnolia.security.app.action.DeleteRoleActionDefinition", action.getProperty("class").getString());
    }

    @Test
    public void testUpdateTo51DeleteItemsAction() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node actions = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions", NodeTypes.ContentNode.NAME);
        assertFalse(actions.hasNode("deleteItems"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(actions.hasNode("deleteItems"));
    }

    @Test
    public void testUpdateTo51NewActionbarSectionInUsers() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node sections = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actionbar/sections", NodeTypes.ContentNode.NAME);
        assertFalse(sections.hasNode("multiple"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(sections.hasNode("multiple"));
    }

    @Test
    public void testTo51EnsureSecurityAppLauncherConfigOrderAfterExtraInstallTask() throws RepositoryException, ModuleManagementException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        String applauncherManageGroupParentPath = "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps";
        String securityNodeName = "security";
        Node nodeCat = NodeUtil.createPath(session.getRootNode(), applauncherManageGroupParentPath + "/categories", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        NodeIterator someNodes = nodeCat.getParent().getNodes();
        boolean secNodeExists = false;
        boolean secNodeIs1st=false;
        int counter=0;
        StringBuffer buffer = new StringBuffer("");
        while (someNodes.hasNext()) {
            Node node = someNodes.nextNode();
            String path = node.getPath();
            buffer.append(path).append(" ,");
            if ( path.endsWith(securityNodeName)) {
                secNodeExists = true;
            }
            if(counter==0 && secNodeExists){
                secNodeIs1st = true;
            }
            counter++;
        }
        assertTrue("secure-node doesn't exist! (found "+counter+"nodes ("+buffer.toString()+"))", secNodeExists);
        assertTrue("the security-node is not the 1st sibbling within '/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps'",secNodeIs1st);
    }


}
