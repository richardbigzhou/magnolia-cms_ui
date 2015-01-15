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
package info.magnolia.ui.admincentral.setup;

import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class AdmincentralModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Session session;
    private Node dialogs;
    private Node actions;
    private Node configActionbarFolderGroups;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-admincentral.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new AdmincentralModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml",
                "/META-INF/magnolia/ui-framework.xml"
                );
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        dialogs = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/dialogs", NodeTypes.ContentNode.NAME);
        dialogs.getSession().save();

        actions = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actions", NodeTypes.ContentNode.NAME);
        configActionbarFolderGroups = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups", NodeTypes.ContentNode.NAME);
    }

    @Test
    public void testUpdateTo5_0_1WithoutExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN
        dialogs.addNode("link", NodeTypes.ContentNode.NAME);
        assertTrue(dialogs.hasNode("link"));
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialogs.hasNode("link"));

    }

    @Test
    public void testUpdateTo5_0_1WithoutNonExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialogs.hasNode("link"));
    }

    @Test
    public void testUpdateTo501EditPropertyDialogExists() throws ModuleManagementException, RepositoryException {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(dialogs.hasNode("editProperty"));
    }

    @Test
    public void testUpdateTo501renameItemDialogExists() throws ModuleManagementException, RepositoryException {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(dialogs.hasNode("renameItem"));
    }

    @Test
    public void testUpdateTo501EditPropertyActionInstalled() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(actions.hasNode("editProperty"));
        Node actionbarFolderEditItems = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/editActions/items", NodeTypes.ContentNode.NAME);
        assertTrue(actionbarFolderEditItems.hasNode("editProperty"));
    }

    @Test
    public void testUpdateTo501RenameActionInstalled() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(actions.hasNode("rename"));
        Node actionbarFolderEditItems = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/editActions/items", NodeTypes.ContentNode.NAME);
        assertTrue(actionbarFolderEditItems.hasNode("rename"));
    }

    @Test
    public void testUpdateTo501ConfigurationAppDuplicateActionsGroupIsRemoved() throws ModuleManagementException, RepositoryException {
        // GIVEN
        configActionbarFolderGroups.addNode("duplicateActions", NodeTypes.ContentNode.NAME);
        assertTrue(configActionbarFolderGroups.hasNode("duplicateActions"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(configActionbarFolderGroups.hasNode("duplicateActions"));
    }

    @Test
    public void testUpdateTo501ConfigurationAppEditActionsGroupIsAdded() throws ModuleManagementException, RepositoryException {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        configActionbarFolderGroups = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups", NodeTypes.ContentNode.NAME);
        assertTrue(configActionbarFolderGroups.hasNode("editActions"));
    }

    @Test
    public void testUpdateTo501JCRBrowserAppExtendsConfigurationApp() throws ModuleManagementException, RepositoryException {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        Node jcrSubApps = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/websiteJcrBrowser/subApps", NodeTypes.ContentNode.NAME);
        assertTrue(jcrSubApps.hasProperty("extends"));
        String subAppExtends = jcrSubApps.getProperty("extends").getString();
        assertTrue("/modules/ui-admincentral/apps/configuration/subApps".equals(subAppExtends));
    }

    @Test
    public void testUpdateTo502HasNewActions() throws ModuleManagementException, RepositoryException {

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(actions.hasNode("confirmDeletion"));
    }


    @Test
    public void testUpdateTo502CleanupDeleteAction() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", NodeTypes.ContentNode.NAME);
        action.setProperty("label", "Delete item");
        action.setProperty("icon", "icon-delete");
        action.getSession().save();

        // WHEN
        NodeUtil.createPath(action, "availability",  NodeTypes.ContentNode.NAME);

        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        Node delete = actions.getNode("delete");
        assertFalse(delete.hasNode("availability"));
        assertFalse(delete.hasProperty("icon"));
        assertFalse(delete.hasProperty("label"));
    }

    @Test
    public void testUpdateTo502ActionbarNodesUpdated() throws ModuleManagementException, RepositoryException {

        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node actionbarItems = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folder/groups/addingActions/items", NodeTypes.ContentNode.NAME);

        NodeUtil.createPath(actionbarItems, "delete",  NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertFalse(actionbarItems.hasNode("delete"));
        assertTrue(actionbarItems.hasNode("confirmDeletion"));
    }
}
