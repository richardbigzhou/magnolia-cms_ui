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
package info.magnolia.pages.app.setup;

import static info.magnolia.jcr.nodebuilder.Ops.*;
import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.nodebuilder.NodeBuilderUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.pages.app.action.DeletePageItemAction;
import info.magnolia.pages.app.editor.availability.IsPageEditableRule;
import info.magnolia.pages.setup.PagesModuleVersionHandler;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.contentapp.ConfiguredContentAppDescriptor;
import info.magnolia.ui.contentapp.availability.IsNotVersionedDetailLocationRule;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hamcrest.core.AllOf;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PagesModuleVersionHandler}.
 */
public class PagesModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Node dialog;
    private Node actions;
    private Node detailActions;
    private Node editPageAction;
    private Node activatePageAction;
    private Node deactivatePageAction;
    private Session session;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/pages.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList(
                "/META-INF/magnolia/core.xml"
                );
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new PagesModuleVersionHandler();
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
        dialog = NodeUtil.createPath(session.getRootNode(), "/modules/pages/dialogs", NodeTypes.ContentNode.NAME);
        dialog.getSession().save();

        actions = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions", NodeTypes.ContentNode.NAME);
        // Detail actions
        detailActions = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/detail/actions", NodeTypes.ContentNode.NAME);
        editPageAction = NodeUtil.createPath(detailActions, "edit", NodeTypes.ContentNode.NAME);
        activatePageAction = NodeUtil.createPath(detailActions, "activate", NodeTypes.ContentNode.NAME);
        deactivatePageAction = NodeUtil.createPath(detailActions, "deactivate", NodeTypes.ContentNode.NAME);

        this.setupConfigNode("/modules/pages/apps/pages/subApps/browser/actions/import/availability");
    }

    @Test
    public void testUpdateTo501WithExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN
        dialog.addNode("link", NodeTypes.ContentNode.NAME);
        assertTrue(dialog.hasNode("link"));
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialog.hasNode("link"));

    }

    @Test
    public void testUpdateTo501WithNonExistingLinkDefinition() throws ModuleManagementException, RepositoryException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(dialog.hasNode("link"));
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
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node action = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/delete", NodeTypes.ContentNode.NAME);
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
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node actionbarItems = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actionbar/sections/pageActions/groups/addingActions/items", NodeTypes.ContentNode.NAME);

        NodeUtil.createPath(actionbarItems, "delete", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.1"));

        // THEN
        assertFalse(actionbarItems.hasNode("delete"));
        assertTrue(actionbarItems.hasNode("confirmDeletion"));
    }

    @Test
    public void testUpdateTo5dot1ConfirmDeletionIsSetToMultiple() throws RepositoryException, ModuleManagementException {
        // GIVEN
        Node availability = NodeUtil.createPath(MgnlContext.getJCRSession(RepositoryConstants.CONFIG).getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/confirmDeletion/availability", NodeTypes.ContentNode.NAME);
        assertFalse(availability.hasProperty("multiple"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(availability.hasProperty("multiple"));
        assertEquals("true", availability.getProperty("multiple").getString());
    }

    @Test
    public void testUpdateTo51BootstrapsNewCommands() throws ModuleManagementException, RepositoryException {

        // GIVEN
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node commands = NodeUtil.createPath(session.getRootNode(), "/modules/pages/commands/website", NodeTypes.ContentNode.NAME);

        NodeBuilderUtil.build(RepositoryConstants.CONFIG, commands.getPath(),
                addNode("activate", NodeTypes.ContentNode.NAME).then(
                        addNode("version", NodeTypes.ContentNode.NAME),
                        addNode("activate", NodeTypes.ContentNode.NAME)
                        ),
                addNode("deactivate", NodeTypes.ContentNode.NAME).then(
                        addNode("deactivate", NodeTypes.ContentNode.NAME).then(
                                addNode("version", NodeTypes.ContentNode.NAME),
                                addNode("deactivate", NodeTypes.ContentNode.NAME)
                                )
                        ),
                addNode("customCommand", NodeTypes.ContentNode.NAME).then(
                        addNode("custom", NodeTypes.ContentNode.NAME).then(
                                addNode("version", NodeTypes.ContentNode.NAME),
                                addNode("else", NodeTypes.ContentNode.NAME)
                                )
                        )
                );

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN

        assertTrue(session.nodeExists("/modules/pages/commands/website/activate"));
        assertTrue(session.nodeExists("/modules/pages/commands/website/deactivate"));

        Node activate = session.getNode("/modules/pages/commands/website/activate");
        Node deactivate = session.getNode("/modules/pages/commands/website/deactivate");
        assertEquals(activate.getProperty("commandName").getString(), "versioned-activate");
        assertEquals(deactivate.getProperty("commandName").getString(), "versioned-deactivate");

        assertTrue(session.nodeExists("/modules/pages/commands/website"));
        assertTrue(session.nodeExists("/modules/pages/commands"));
    }

    @Test
    public void testUpdateTo51VersionActionsInstalled() throws RepositoryException, ModuleManagementException {
        // GIVEN
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        Node actions = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions", NodeTypes.ContentNode.NAME);
        Node pageActionsGroups = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actionbar/sections/pageActions/groups", NodeTypes.ContentNode.NAME);

        // Also create a showVersions action to simulate an installed module diff
        NodeUtil.createPath(actions, "showVersions", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(actions.hasNode("showVersions"));
        assertTrue(pageActionsGroups.hasNode("versionActions"));
        assertTrue(pageActionsGroups.getNode("versionActions").hasNode("items"));
        assertTrue(pageActionsGroups.getNode("versionActions").getNode("items").hasNode("showVersions"));
    }

    @Test
    public void testUpdateTo51ActionsHaveAvailabilityRule() throws RepositoryException, ModuleManagementException {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.2"));

        // THEN
        assertTrue(editPageAction.hasNode("availability"));
        Node availability = editPageAction.getNode("availability");
        assertTrue(availability.hasNode("rules")); // expect 5.3-migrated rules node
        assertThat(availability.getNode("rules"), hasNode(hasProperty("implementationClass", IsNotVersionedDetailLocationRule.class.getName())));

        assertTrue(activatePageAction.hasNode("availability"));
        availability = activatePageAction.getNode("availability");
        assertTrue(availability.hasNode("rules")); // expect 5.3-migrated rules node
        assertThat(availability.getNode("rules"), hasNode(hasProperty("implementationClass", IsNotVersionedDetailLocationRule.class.getName())));

        assertTrue(deactivatePageAction.hasNode("availability"));
        availability = deactivatePageAction.getNode("availability");
        assertTrue(availability.hasNode("rules")); // expect 5.3-migrated rules node
        assertThat(availability.getNode("rules"), hasNode(hasProperty("implementationClass", IsNotVersionedDetailLocationRule.class.getName())));
    }

    @Test
    public void testUpdateTo511ImportActionAvailabilityHasRootProperty() throws ModuleManagementException, RepositoryException {
        // GIVEN
        Node availability = NodeUtil.createPath(MgnlContext.getJCRSession(RepositoryConstants.CONFIG).getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/import/availability", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertTrue(availability.hasProperty("root"));
        assertTrue(availability.getProperty("root").getBoolean());
    }

    @Test
    public void testUpdateFrom50() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigProperty("/modules/pages/dialogs/editTemplate/form", "i18nBasename", "someI18nBasename");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertTrue(session.propertyExists("/modules/pages/apps/pages/class"));
        assertEquals(ConfiguredContentAppDescriptor.class.getName(), session.getProperty("/modules/pages/apps/pages/class").getString());
        assertFalse(session.propertyExists("/modules/pages/dialogs/editTemplate/form/i18nBasename"));
    }

    @Test
    public void testUpdateTo523SetsWritePermissionForPagesBrowserActions() throws Exception {
        // GIVEN
        Node addAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/add", NodeTypes.ContentNode.NAME);
        Node editAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/detail/actions/edit", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.2"));

        // THEN
        assertTrue(addAction.hasNode("availability"));
        Node availability = addAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());

        assertTrue(editAction.hasNode("availability"));
        availability = editAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());
    }

    @Test
    public void testDialogsAreAddedModalityLevelProperty() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigNode("/modules/pages/dialogs/editPage");
        this.setupConfigNode("/modules/pages/dialogs/createPage");
        this.setupConfigNode("/modules/pages/dialogs/editTemplate");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.2"));

        // THEN

        assertEquals("light", session.getProperty("/modules/pages/dialogs/editPage/modalityLevel").getString());
        assertEquals("strong", session.getProperty("/modules/pages/dialogs/createPage/modalityLevel").getString());
        assertEquals("light", session.getProperty("/modules/pages/dialogs/editTemplate/modalityLevel").getString());
    }

    @Test
    public void testUpdateFrom524() throws ModuleManagementException, RepositoryException {
        // GIVEN
        this.setupConfigNode("/modules/pages/apps/pages/subApps/browser/actions/activate");
        this.setupConfigNode("/modules/pages/apps/pages/subApps/browser/actions/activateRecursive");
        this.setupConfigNode("/modules/pages/apps/pages/subApps/browser/actions/delete");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.4"));

        // THEN
        assertThat(session.getNode("/modules/pages/apps/pages/subApps/browser/actions/activateRecursive"), hasProperty("asynchronous", "true"));
        assertThat(session.getNode("/modules/pages/apps/pages/subApps/browser/actions/delete"), hasProperty("asynchronous", "true"));
    }

    @Test
    public void testUpdateFrom53SetsWritePermissionForPagesBrowserActions() throws Exception {
        // GIVEN
        Node activateAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activate", NodeTypes.ContentNode.NAME);
        Node activateRecursiveAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activateRecursive", NodeTypes.ContentNode.NAME);
        Node deactivateAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/deactivate", NodeTypes.ContentNode.NAME);
        Node activateDeletionAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activateDeletion", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3"));

        // THEN
        assertTrue(activateAction.hasNode("availability"));
        Node availability = activateAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());

        assertTrue(activateRecursiveAction.hasNode("availability"));
        availability = activateRecursiveAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());

        assertTrue(deactivateAction.hasNode("availability"));
        availability = deactivateAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());

        assertTrue(activateDeletionAction.hasNode("availability"));
        availability = activateDeletionAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());
    }

    @Test
    public void testUpdateTo533UpdatesPageEditorActions() throws Exception {
        // GIVEN
        Node actions = NodeUtil.createPath(session.getRootNode(),"/modules/pages/apps/pages/subApps/detail/actions", NodeTypes.ContentNode.NAME);

        String editArea = "editArea";
        String addArea = "addArea";
        String deleteArea = "deleteArea";
        String editComponent = "editComponent";
        String addComponent = "addComponent";
        String copyComponent = "copyComponent";
        String pasteComponent = "pasteComponent";
        String deleteComponent = "deleteComponent";
        String startMoveComponent = "startMoveComponent";
        String stopMoveComponent = "stopMoveComponent";
        String showPreviousVersion = "showPreviousVersion";
        String redo = "redo";
        String undo = "undo";

        NodeBuilderUtil.build(RepositoryConstants.CONFIG, actions.getPath(),
                addNode(editArea, NodeTypes.ContentNode.NAME),
                addNode(addArea, NodeTypes.ContentNode.NAME),
                addNode(deleteArea, NodeTypes.ContentNode.NAME),
                addNode(editComponent, NodeTypes.ContentNode.NAME),
                addNode(addComponent, NodeTypes.ContentNode.NAME),
                addNode(deleteComponent, NodeTypes.ContentNode.NAME).then(
                        addProperty("implementationClass", "info.magnolia.pages.app.action.DeleteComponentAction")
                ),
                addNode(startMoveComponent, NodeTypes.ContentNode.NAME),
                addNode(copyComponent, NodeTypes.ContentNode.NAME),
                addNode(pasteComponent, NodeTypes.ContentNode.NAME),
                addNode(stopMoveComponent, NodeTypes.ContentNode.NAME),
                addNode(showPreviousVersion, NodeTypes.ContentNode.NAME),
                addNode(redo, NodeTypes.ContentNode.NAME),
                addNode(undo, NodeTypes.ContentNode.NAME)
        );

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.2"));

        // THEN
        assertThat(actions.getNode(editArea), hasNode("availability"));
        assertThat(actions.getNode(addArea), hasNode("availability"));
        assertThat(actions.getNode(deleteArea), hasNode("availability"));
        assertThat(actions.getNode(deleteArea), hasProperty("implementationClass", DeletePageItemAction.class.getName()));
        assertThat(actions.getNode(editComponent), hasNode("availability"));
        assertThat(actions.getNode(addComponent), hasNode("availability"));
        assertThat(actions.getNode(deleteComponent), hasNode("availability"));
        assertThat(actions.getNode(deleteComponent), hasProperty("implementationClass", DeletePageItemAction.class.getName()));
        assertThat(actions.getNode(startMoveComponent), hasNode("availability"));
        assertThat(actions.getNode(stopMoveComponent), hasNode("availability"));
        assertThat(actions.getNode(showPreviousVersion), hasNode("availability"));
        assertThat(actions, not(hasNode(copyComponent)));
        assertThat(actions, not(hasNode(pasteComponent)));
        assertThat(actions, not(hasNode(redo)));
        assertThat(actions, not(hasNode(undo)));
        assertThat(actions, hasNode("editPageNodeArea"));
    }

    @Test
    public void testUpdateTo533UpdatesActionbarSectionAvailability() throws Exception {
        // GIVEN
        Node actionbar = NodeUtil.createPath(session.getRootNode(),"/modules/pages/apps/pages/subApps/detail/actionbar/sections", NodeTypes.ContentNode.NAME);

        String pagePreviewActions = "pagePreviewActions";
        String pageActions = "pageActions";
        String areaActions = "areaActions";
        String componentActions = "componentActions";
        String pageDeleteActions = "pageDeleteActions";

        String editableAreaActions = "editableAreaActions";
        String optionalAreaActions = "optionalAreaActions";
        String optionalEditableAreaActions = "optionalEditableAreaActions";

        NodeBuilderUtil.build(RepositoryConstants.CONFIG, actionbar.getPath(),
                addNode(pagePreviewActions, NodeTypes.ContentNode.NAME),
                addNode(pageActions, NodeTypes.ContentNode.NAME),
                addNode(areaActions, NodeTypes.ContentNode.NAME),
                addNode(componentActions, NodeTypes.ContentNode.NAME),
                addNode(pageDeleteActions, NodeTypes.ContentNode.NAME),
                addNode(editableAreaActions, NodeTypes.ContentNode.NAME),
                addNode(optionalAreaActions, NodeTypes.ContentNode.NAME),
                addNode(optionalEditableAreaActions, NodeTypes.ContentNode.NAME)
        );

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.2"));

        // THEN
        assertThat(actionbar.getNode(pagePreviewActions), hasNode("availability"));
        assertThat(actionbar.getNode(pageActions), hasNode("availability"));
        assertThat(actionbar.getNode(areaActions), hasNode("availability"));
        assertThat(actionbar.getNode(componentActions), hasNode("availability"));
        assertThat(actionbar.getNode(pageDeleteActions), hasNode("availability"));

        assertThat(actionbar, not(hasNode(editableAreaActions)));
        assertThat(actionbar, not(hasNode(optionalAreaActions)));
        assertThat(actionbar, not(hasNode(optionalEditableAreaActions)));

        assertThat(actionbar, hasNode("pageNodeAreaActions"));
    }

    @Test
    public void testUpdateTo533UpdatesAreaActions() throws Exception {

        Node areaActions = NodeUtil.createPath(session.getRootNode(),"/modules/pages/apps/pages/subApps/detail/actionbar/sections/areaActions", NodeTypes.ContentNode.NAME);

        NodeBuilderUtil.build(RepositoryConstants.CONFIG, areaActions.getPath(),
                addNode("groups", NodeTypes.ContentNode.NAME).then(
                        addNode("editingFlow", NodeTypes.ContentNode.NAME).then(
                                addNode("items", NodeTypes.ContentNode.NAME).then(
                                    addNode("preview", NodeTypes.ContentNode.NAME)
                                )
                        ),
                        addNode("addingActions", NodeTypes.ContentNode.NAME).then(
                                addNode("items", NodeTypes.ContentNode.NAME).then(
                                        addNode("addComponent", NodeTypes.ContentNode.NAME)
                                )
                        )
                )
        );

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.2"));

        // THEN
        assertThat(areaActions, hasNode("availability"));
        assertThat(areaActions.getNode("groups").getNode("editingActions/items"), hasNode("editArea"));
        assertThat(areaActions.getNode("groups").getNode("addingActions/items"), hasNode("addArea"));
        assertThat(areaActions.getNode("groups").getNode("addingActions/items"), hasNode("deleteArea"));
        assertThat(areaActions.getNode("groups").getNode("addingActions/items"), hasNode("addComponent"));
    }

    @Test
    public void testUpdateTo534() throws Exception {

        Node restorePreviousVersion = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/restorePreviousVersion", NodeTypes.ContentNode.NAME);
        Node availability = restorePreviousVersion.addNode("availability", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.3"));

        // THEN
        assertThat(restorePreviousVersion, hasProperty("parentNodeTypeOnly", true));
        assertThat(availability, hasProperty("multiple", true));

    }

    @Test
    public void testUpdateTo534UpdatesActionbarPageSectionsAvailability() throws Exception {
        // GIVEN
        Node actionbar = NodeUtil.createPath(session.getRootNode(),"/modules/pages/apps/pages/subApps/detail/actionbar/sections", NodeTypes.ContentNode.NAME);

        String pagePreviewActions = "pagePreviewActions";
        String pageActions = "pageActions";


        NodeBuilderUtil.build(RepositoryConstants.CONFIG, actionbar.getPath(),
                addNode(pagePreviewActions, NodeTypes.ContentNode.NAME),
                addNode(pageActions, NodeTypes.ContentNode.NAME)
        );

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.3"));

        // THEN
        assertThat(actionbar.getNode(pagePreviewActions), hasNode("availability/rules/isPageElement"));
        assertThat(actionbar.getNode(pageActions), hasNode("availability/rules/isPageElement"));
        assertThat(actionbar.getNode(pagePreviewActions), hasNode("availability/rules/isNotDeleted"));
        assertThat(actionbar.getNode(pageActions), hasNode("availability/rules/isNotDeleted"));
    }

    @Test
    public void testUpdateTo536() throws Exception {
        // GIVEN
        Node availability = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activate/availability", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.5"));

        // THEN
        Node rule = availability.getNode("rules/IsPublishableRule");
        assertThat(rule, hasProperty("implementationClass", "info.magnolia.ui.framework.availability.IsPublishableRule"));

        // GIVEN
        availability = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activateRecursive/availability", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.5"));

        // THEN
        rule = availability.getNode("rules/IsPublishableRule");
        assertThat(rule, hasProperty("implementationClass", "info.magnolia.ui.framework.availability.IsPublishableRule"));

    }

    @Test
    public void updateFrom537BootstrapsNewComponentDialog() throws Exception {
        // GIVEN
        Node dialogs = NodeUtil.createPath(session.getRootNode(), "/modules/pages/dialogs", NodeTypes.Content.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.7"));

        // THEN
        assertThat(dialogs, hasNode("newComponent"));

        // GIVEN — since this upgrade task will also be added to pages 5.4 delta-builder, it should not override potential changes
        dialogs.getNode("newComponent").setProperty("modalityLevel", "light");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.7"));

        // THEN
        assertThat(dialogs, hasNode(AllOf.<Node> allOf(nodeName("newComponent"), hasProperty("modalityLevel", "light"))));
    }

    @Test
    public void updateFrom537NewAvailabilityRule() throws Exception {
        // GIVEN
        String actionPath = "/modules/pages/apps/pages/subApps/detail/actions/editProperties";
        Node actionNode = NodeUtil.createPath(session.getRootNode(), actionPath, NodeTypes.Content.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.7"));

        // THEN
        assertTrue(session.nodeExists(actionPath + "/availability/rules/isPageEditable"));
        assertEquals(PropertyUtil.getString(actionNode.getNode("availability/rules/isPageEditable"), "implementationClass"), IsPageEditableRule.class.getName());
    }

    @Test
    public void updateFrom538UpdateAvailabilityRule() throws Exception {
        // GIVEN
        String availabilityPath = "/modules/pages/apps/pages/subApps/browser/actions/activateDeletion/availability";
        Node availabilityNode = NodeUtil.createPath(session.getRootNode(), availabilityPath, NodeTypes.Content.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.8"));

        // THEN
        assertThat(availabilityNode, hasProperty("multiple"));
        assertThat(availabilityNode, hasProperty("multiple", true));
    }

}
