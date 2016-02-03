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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypeTemplateUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.framework.AdmincentralNodeTypes;
import info.magnolia.ui.framework.favorite.FavoriteStore;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;

import org.apache.jackrabbit.JcrConstants;
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
    private Node mainNodeType;
    private Node folderNodeType;
    private Node confirmDeleteActionAvailability;
    private Node configActionbarSections;
    private Node servletParameters;
    private String appLauncherLayoutConfigNodeSourceParent_path = "/modules/ui-framework/config";
    private String appLauncherLayoutConfigNodeTargetParent_path = "/modules/ui-admincentral/config";
    private Node appLauncherLayoutConfigNodeSourceParent;
    private Node appLauncherLayoutConfigNodeTargetParent;


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
    protected String getRepositoryConfigFileName() {
        String repositoryFileName = "test-admincentral-repositories.xml";
        setRepositoryConfigFileName(repositoryFileName);
        return repositoryFileName;
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
        mainNodeType = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/workbench/nodeTypes/mainNodeType", NodeTypes.ContentNode.NAME);
        folderNodeType = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/workbench/nodeTypes/folderNodeType", NodeTypes.ContentNode.NAME);
        confirmDeleteActionAvailability = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/confirmDeletion/availability", NodeTypes.ContentNode.NAME);
        configActionbarSections = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections", NodeTypes.ContentNode.NAME);
        servletParameters = NodeUtil.createPath(session.getRootNode(), "/server/filters/servlets/AdminCentral/parameters", NodeTypes.ContentNode.NAME);
        appLauncherLayoutConfigNodeSourceParent = NodeUtil.createPath(session.getRootNode(),appLauncherLayoutConfigNodeSourceParent_path, NodeTypes.ContentNode.NAME);
        appLauncherLayoutConfigNodeTargetParent = NodeUtil.createPath(session.getRootNode(),appLauncherLayoutConfigNodeTargetParent_path, NodeTypes.ContentNode.NAME);
    }

    @Test
    public void testUpdateTo501WithoutExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN
        dialogs.addNode("link", NodeTypes.ContentNode.NAME);
        assertTrue(dialogs.hasNode("link"));
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialogs.hasNode("link"));

    }

    @Test
    public void testUpdateTo501WithoutNonExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
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
        NodeUtil.createPath(action, "availability", NodeTypes.ContentNode.NAME);

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

        NodeUtil.createPath(actionbarItems, "delete", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertFalse(actionbarItems.hasNode("delete"));
        assertTrue(actionbarItems.hasNode("confirmDeletion"));
    }

    @Test
    public void testUpdateTo51SetsNodeTypesForConfigurationAppAsStrict() throws ModuleManagementException, RepositoryException {

        // GIVEN
        assertFalse(mainNodeType.hasProperty("strict"));
        assertFalse(folderNodeType.hasProperty("strict"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(mainNodeType.hasProperty("strict"));
        assertTrue(folderNodeType.hasProperty("strict"));
    }

    @Test
    public void testUpdateTo51ConfirmDeletionActionAllowsMultipleItems() throws ModuleManagementException, RepositoryException {

        // GIVEN
        assertFalse(confirmDeleteActionAvailability.hasProperty("multiple"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(confirmDeleteActionAvailability.hasProperty("multiple"));
        assertEquals("true", confirmDeleteActionAvailability.getProperty("multiple").getString());
    }

    @Test
    public void testUpdateTo51CreatesNewActionbarSectionInConfigApp() throws ModuleManagementException, RepositoryException {

        // GIVEN
        assertFalse(configActionbarSections.hasNode("multiple"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(configActionbarSections.hasNode("multiple"));
    }

    @Test
    public void testUpdateTo51SetsJCRBrowserAppNodeTypesAsNotStrict() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node jcrBrowserSubApp = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/websiteJcrBrowser/subApps/browser/workbench", NodeTypes.ContentNode.NAME);
        assertFalse(jcrBrowserSubApp.hasNode("nodeTypes"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        Node mainNodeType = jcrBrowserSubApp.getNode("nodeTypes/mainNodeType");
        assertFalse(mainNodeType.getProperty("strict").getBoolean());

        Node folderNodeType = jcrBrowserSubApp.getNode("nodeTypes/folderNodeType");
        assertFalse(folderNodeType.getProperty("strict").getBoolean());
    }

    @Test
    public void testUpdateTo51ChangesAdmincentralServletParameters() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        servletParameters.setProperty("widgetset", "some.gwt.package.SomeWidgetset");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertFalse(servletParameters.hasProperty("widgetset"));
        assertFalse(servletParameters.hasProperty("UIProvider"));
    }

    @Test
    public void testUpdateTo51ChangesAppLauncherLayoutConfigLocation() throws RepositoryException, ModuleManagementException{
        String applauncherlayoutNodeName = "appLauncherLayout";
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node applauncherLayoutConfig = NodeUtil.createPath( session.getRootNode(),appLauncherLayoutConfigNodeSourceParent_path+"/"+applauncherlayoutNodeName,NodeTypes.ContentNode.NAME);
        Node appLauncherLayoutConfigNodeSourceGrandParent = appLauncherLayoutConfigNodeSourceParent.getParent();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(appLauncherLayoutConfigNodeTargetParent.hasNode(applauncherlayoutNodeName));
        assertFalse(appLauncherLayoutConfigNodeSourceGrandParent.hasNode("config"));

    }

    @Test
    public void testUpdateTo511UpdatesFavoriteNodeType() throws RepositoryException, ModuleManagementException {
        // GIVEN
        Session session = MgnlContext.getJCRSession(FavoriteStore.WORKSPACE_NAME);
        NodeTypeManager nodeTypeManager = session.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate template = NodeTypeTemplateUtil.createSimpleNodeType(nodeTypeManager, AdmincentralNodeTypes.Favorite.NAME, Arrays.asList(JcrConstants.NT_BASE));
        nodeTypeManager.registerNodeType(template, false);
        assertFalse(nodeTypeManager.getNodeType(AdmincentralNodeTypes.Favorite.NAME).isNodeType(NodeTypes.Created.NAME));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertTrue(nodeTypeManager.getNodeType(AdmincentralNodeTypes.Favorite.NAME).isNodeType(NodeTypes.Created.NAME));
        assertTrue(nodeTypeManager.getNodeType(AdmincentralNodeTypes.Favorite.NAME).isNodeType(NodeTypes.LastModified.NAME));
    }

}
