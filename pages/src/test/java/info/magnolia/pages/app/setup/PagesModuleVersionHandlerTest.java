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
package info.magnolia.pages.app.setup;

import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.nodebuilder.NodeBuilderUtil;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.pages.setup.PagesModuleVersionHandler;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.contentapp.ConfiguredContentAppDescriptor;
import info.magnolia.ui.contentapp.availability.IsNotVersionedDetailLocationRule;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
        return "/META-INF/magnolia/ui-admincentral.xml";
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

        this.setupConfigNode("/modules/pages/apps/pages");
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
        assertTrue(editPageAction.getNode("availability").hasProperty("ruleClass"));
        assertEquals(IsNotVersionedDetailLocationRule.class.getName(), editPageAction.getNode("availability").getProperty("ruleClass").getString());

        assertTrue(activatePageAction.hasNode("availability"));
        assertTrue(activatePageAction.getNode("availability").hasProperty("ruleClass"));
        assertEquals(IsNotVersionedDetailLocationRule.class.getName(), activatePageAction.getNode("availability").getProperty("ruleClass").getString());

        assertTrue(deactivatePageAction.hasNode("availability"));
        assertTrue(deactivatePageAction.getNode("availability").hasProperty("ruleClass"));
        assertEquals(IsNotVersionedDetailLocationRule.class.getName(), deactivatePageAction.getNode("availability").getProperty("ruleClass").getString());
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
    public void testUpdateTo527SetsWritePermissionForPagesBrowserActions() throws Exception {
        // GIVEN
        Node activateAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activate", NodeTypes.ContentNode.NAME);
        Node activateRecursiveAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activateRecursive", NodeTypes.ContentNode.NAME);
        Node deactivateAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/deactivate", NodeTypes.ContentNode.NAME);
        Node activateDeletionAction = NodeUtil.createPath(session.getRootNode(), "/modules/pages/apps/pages/subApps/browser/actions/activateDeletion", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.6"));

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

}
