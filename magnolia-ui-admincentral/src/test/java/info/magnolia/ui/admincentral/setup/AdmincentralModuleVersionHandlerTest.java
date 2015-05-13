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

import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import info.magnolia.ui.workbench.tree.drop.NodesAndPropsDropConstraint;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;

import org.apache.jackrabbit.JcrConstants;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link AdmincentralModuleVersionHandler}.
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
    protected List<String> getInstalledModules() {
        return Arrays.asList("scheduler");
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
        appLauncherLayoutConfigNodeSourceParent = NodeUtil.createPath(session.getRootNode(), appLauncherLayoutConfigNodeSourceParent_path, NodeTypes.ContentNode.NAME);
        appLauncherLayoutConfigNodeTargetParent = NodeUtil.createPath(session.getRootNode(), appLauncherLayoutConfigNodeTargetParent_path, NodeTypes.ContentNode.NAME);

        NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/addingActions/items", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/move/availability", NodeTypes.ContentNode.NAME);

        // for 5.2.2 update:
        this.setupConfigNode("/modules/ui-admincentral/templates/deleted");
        Node command = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/commands/default/delete/deactivate", NodeTypes.ContentNode.NAME);
        command.setProperty("enabled", true);
    }

    @Test
    public void testUpdateTo501WithoutExistingLinkDefinition() throws Exception {
        // GIVEN
        dialogs.addNode("link", NodeTypes.ContentNode.NAME);
        assertTrue(dialogs.hasNode("link"));
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialogs.hasNode("link"));

    }

    @Test
    public void testUpdateTo501WithoutNonExistingLinkDefinition() throws Exception {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialogs.hasNode("link"));
    }

    @Test
    public void testUpdateTo501EditPropertyDialogExists() throws Exception {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(dialogs.hasNode("editProperty"));
    }

    @Test
    public void testUpdateTo501renameItemDialogExists() throws Exception {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(dialogs.hasNode("renameItem"));
    }

    @Test
    public void testUpdateTo501EditPropertyActionInstalled() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(actions.hasNode("editProperty"));
        Node actionbarFolderEditItems = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/editActions/items", NodeTypes.ContentNode.NAME);
        assertTrue(actionbarFolderEditItems.hasNode("editProperty"));
    }

    @Test
    public void testUpdateTo501RenameActionInstalled() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(actions.hasNode("rename"));
        Node actionbarFolderEditItems = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/editActions/items", NodeTypes.ContentNode.NAME);
        assertTrue(actionbarFolderEditItems.hasNode("rename"));
    }

    @Test
    public void testUpdateTo501ConfigurationAppDuplicateActionsGroupIsRemoved() throws Exception {
        // GIVEN
        configActionbarFolderGroups.addNode("duplicateActions", NodeTypes.ContentNode.NAME);
        assertTrue(configActionbarFolderGroups.hasNode("duplicateActions"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(configActionbarFolderGroups.hasNode("duplicateActions"));
    }

    @Test
    public void testUpdateTo501ConfigurationAppEditActionsGroupIsAdded() throws Exception {
        // GIVEN
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        configActionbarFolderGroups = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups", NodeTypes.ContentNode.NAME);
        assertTrue(configActionbarFolderGroups.hasNode("editActions"));
    }

    @Test
    public void testUpdateTo501JCRBrowserAppExtendsConfigurationApp() throws Exception {
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
    public void testUpdateTo502HasNewActions() throws Exception {

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertTrue(actions.hasNode("confirmDeletion"));
    }

    @Test
    public void testUpdateTo502CleanupDeleteAction() throws Exception {
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
    public void testUpdateTo502ActionbarNodesUpdated() throws Exception {

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
    public void testUpdateTo51SetsNodeTypesForConfigurationAppAsStrict() throws Exception {

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
    public void testUpdateTo51ConfirmDeletionActionAllowsMultipleItems() throws Exception {

        // GIVEN
        assertFalse(confirmDeleteActionAvailability.hasProperty("multiple"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(confirmDeleteActionAvailability.hasProperty("multiple"));
        assertEquals("true", confirmDeleteActionAvailability.getProperty("multiple").getString());
    }

    @Test
    public void testUpdateTo51CreatesNewActionbarSectionInConfigApp() throws Exception {

        // GIVEN
        assertFalse(configActionbarSections.hasNode("multiple"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(configActionbarSections.hasNode("multiple"));
    }

    @Test
    public void testUpdateTo51SetsJCRBrowserAppNodeTypesAsNotStrict() throws Exception {
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
    public void testUpdateTo51ChangesAdmincentralServletParameters() throws Exception {
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
    public void testUpdateTo51ChangesAppLauncherLayoutConfigLocation() throws Exception {
        String applauncherlayoutNodeName = "appLauncherLayout";
        // GIVEN
        Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node applauncherLayoutConfig = NodeUtil.createPath(session.getRootNode(), appLauncherLayoutConfigNodeSourceParent_path + "/" + applauncherlayoutNodeName, NodeTypes.ContentNode.NAME);
        Node appLauncherLayoutConfigNodeSourceGrandParent = appLauncherLayoutConfigNodeSourceParent.getParent();

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(appLauncherLayoutConfigNodeTargetParent.hasNode(applauncherlayoutNodeName));
        assertFalse(appLauncherLayoutConfigNodeSourceGrandParent.hasNode("config"));

    }

    @Test
    public void testUpdateTo511UpdatesFavoriteNodeType() throws Exception {
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

    @Test
    public void testUpdateTo52BootstrapsVirtualURIMapping() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertTrue(session.itemExists("/modules/ui-admincentral/virtualURIMapping/default"));
    }

    @Test
    public void testUpdateTo52RegisterActivationApp() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), "/modules/activation", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/websiteJcrBrowser", NodeTypes.ContentNode.NAME);
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertTrue(session.itemExists("/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activation"));
        assertTrue(session.itemExists("/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activationMonitor"));
    }

    @Test
    public void testUpdateTo52DoNotRegisterActivationApp() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertFalse(session.itemExists("/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/activation"));
    }

    @Test
    public void testUpdateFrom50() throws Exception {
        // GIVEN
        this.setupConfigNode("/modules/ui-admincentral/apps/stkSiteApp/subApps");
        this.setupConfigProperty("/modules/ui-admincentral/apps/stkSiteApp", "icon", "someIcon");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertEquals("info.magnolia.ui.contentapp.ContentAppDescriptor", session.getProperty("/modules/ui-admincentral/apps/configuration/class").getString());
        assertFalse(session.itemExists("/modules/ui-admincentral/apps/stkSiteApp/app"));
        assertFalse(session.itemExists("/modules/ui-admincentral/apps/stkSiteApp/icon"));

        assertTrue(session.itemExists("/modules/ui-admincentral/templates/deleted/i18nBasename"));
        assertEquals("info.magnolia.module.admininterface.messages", session.getProperty("/modules/ui-admincentral/templates/deleted/i18nBasename").getString());
        assertEquals(PropertyType.STRING, session.getProperty("/modules/ui-admincentral/commands/default/delete/deactivate/enabled").getType());
    }

    @Test
    public void testUpdateFrom521AddEmptyItemTypesInToParamsOfActivateAction() throws Exception {
        // GIVEN
        this.setupConfigNode("/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activate");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.1"));

        // THEN
        assertTrue(session.itemExists("/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activate/params/itemTypes"));
        assertEquals("", session.getProperty("/modules/ui-admincentral/apps/configuration/subApps/browser/actions/activate/params/itemTypes").getString());
    }

    @Test
    public void testICEPushMimeMappingRemovedIn522() throws Exception {
        // GIVEN
        this.setupConfigNode("/server/MIMEMapping/icepush");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.1"));

        // THEN
        assertFalse("ICEPush MIMEMapping is gone", session.itemExists("/server/MIMEMapping/icepush"));
    }

    @Test
    public void testUpdateTo524AddsEditUserProfileCapabilityAndIcons() throws Exception {
        // GIVEN
        Node dialogs = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/dialogs", NodeTypes.Content.NAME);
        Node config = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/config", NodeTypes.Content.NAME);
        Node actions = NodeUtil.createPath(config, "userMenu/actions", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(actions, "logout", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.3"));

        // THEN
        assertTrue(dialogs.hasNode("editUserProfile"));
        assertTrue(actions.hasNode("logout"));
        assertTrue(actions.getNode("logout").hasProperty("icon"));
        assertTrue(actions.hasNode("editUserProfile"));
        assertTrue(actions.getNode("editUserProfile").hasProperty("icon"));
        NodeIterator it = actions.getNodes();
        assertEquals("editUserProfile", it.nextNode().getName());
        assertEquals("logout", it.nextNode().getName());
    }

    @Test
    public void testUpdateTo524ChangesConfigurationAppClass() throws Exception {
        // GIVEN
        setupConfigProperty("modules/ui-admincentral/apps/configuration", "class", "info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.3"));

        // THEN
        assertEquals("info.magnolia.ui.contentapp.ContentAppDescriptor", session.getProperty("/modules/ui-admincentral/apps/configuration/class").getString());
    }

    @Test
    public void testUpdateFrom525() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node activateAction = NodeUtil.createPath(session.getRootNode(), AdmincentralModuleVersionHandler.UI_ACTIONS_IMPORT, NodeTypes.Content.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.5"));

        // THEN
        assertThat(activateAction, hasNode("availability"));
        assertThat(activateAction.getNode("availability"), hasProperty("root", true));
    }

    @Test
    public void testUpdateTo53AddsSupportForMovingProperties() throws Exception {
        // GIVEN
        Node workbench = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/workbench", NodeTypes.ContentNode.NAME);
        Node moveAvailability = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/move/availability", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.4"));

        // THEN
        assertTrue(workbench.hasProperty("dropConstraintClass"));
        assertEquals(NodesAndPropsDropConstraint.class.getName(), workbench.getProperty("dropConstraintClass").getString());
        assertTrue(moveAvailability.hasProperty("properties"));
        assertTrue(moveAvailability.getProperty("properties").getBoolean());
    }

    @Test
    public void testUpdateFrom531MakesImportFieldMandatory() throws Exception {
        // GIVEN
        Node importField = NodeUtil.createPath(session.getRootNode(), AdmincentralModuleVersionHandler.UI_IMPORT_FIELD, NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.1"));

        // THEN
        assertThat(importField, hasProperty("required"));
        assertThat(importField.getProperty("required").getBoolean(), is(true));
    }

    @Test
    public void testUpdateFrom533() throws Exception {
        // GIVEN
        Node config = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/commands/default", NodeTypes.Content.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.3"));

        // THEN
        assertThat(config, hasNode("restorePreviousVersion"));

    }

    @Test
    public void testUpdateFrom538() throws Exception {
        // GIVEN
        Node config = NodeUtil.createPath(session.getRootNode(), "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/deactivate/availability/rules", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.8"));

        // THEN
        assertThat(config, hasNode("ConfigProtectedNodeRule"));

    }

}
