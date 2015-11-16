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
package info.magnolia.security.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.junit.Assert.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.action.DeleteFolderActionDefinition;
import info.magnolia.security.app.dialog.field.ConditionalReadOnlyTextFieldDefinition;
import info.magnolia.security.app.dialog.field.SystemLanguagesFieldDefinition;
import info.magnolia.ui.form.field.definition.SelectFieldDefinition;
import info.magnolia.ui.framework.action.DeleteActionDefinition;
import info.magnolia.ui.framework.action.DeleteItemActionDefinition;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;

/**
 * Test class for Security App.
 */
public class SecurityModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Session session;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        this.setupConfigNode("/modules/security-app/apps/security/subApps/users/actionbar");
        this.setupConfigNode("/modules/security_app/apps/security/subApps/users/actions/deleteFolder/availability");
        this.setupConfigNode("/modules/security_app/apps/security/subApps/users/actions/deleteUser/availability");
        this.setupConfigNode("/modules/security_app/apps/security/subApps/roles/workbench/contentViews/tree");
        // for 5.2.2 update:
        this.setupConfigNode("/modules/security-app/apps/security/subApps/roles/workbench/contentViews/tree");
        this.setupConfigNode("/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability");
        this.setupConfigNode("/modules/security-app/apps/security/subApps/users/actions/deleteFolder/availability");
        // for 5.2.3 update:
        setupConfigNode("/modules/security-app/apps/security/subApps/users/actions");
        setupConfigNode("/modules/security-app/apps/security/subApps/groups/actions");
        setupConfigNode("/modules/security-app/apps/security/subApps/roles/actions");

        // for 5.3.1 update:
        setupConfigNode("/modules/security-app/dialogs/role");

        // for 5.3.4 update:
        setupConfigNode("/modules/security-app/apps/security/subApps/users/workbench");
        setupConfigNode("/modules/security-app/apps/security/subApps/groups/workbench");
        setupConfigNode("/modules/security-app/apps/security/subApps/roles/workbench");

        setupConfigNode("/modules/security-app/apps/security/subApps/users/workbench/contentViews/tree");

    }

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
    public void testUpdateTo51DeleteUserActionAvailability() throws ModuleManagementException, RepositoryException {
        // GIVEN
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
        String applauncherManageGroupParentPath = "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps";
        Node appsNode = NodeUtil.createPath(session.getRootNode(), applauncherManageGroupParentPath, NodeTypes.ContentNode.NAME);
        appsNode.addNode("configuration", NodeTypes.ContentNode.NAME);
        // we have to create the security node artificially before bootstrapping, otherwise test would fail in maven
        appsNode.addNode("security", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        Node node1st = appsNode.getNodes().nextNode();
        assertEquals("security", node1st.getName());
    }

    @Test
    public void emptyLabelsAreRemovedOnUpdateTo51() throws RepositoryException, ModuleManagementException {
        // GIVEN
        String static1 = "/modules/security-app/dialogs/role/form/tabs/acls/fields/static1";
        String static2 = "/modules/security-app/dialogs/role/form/tabs/acls/fields/static2";
        Node static1Node = NodeUtil.createPath(session.getRootNode(), static1, NodeTypes.ContentNode.NAME);
        Node static2Node = NodeUtil.createPath(session.getRootNode(), static2, NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(static1Node, "label", "");
        PropertyUtil.setProperty(static2Node, "label", "");
        assertTrue(static1Node.hasProperty("label"));
        assertTrue(static2Node.hasProperty("label"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0.4"));

        // THEN
        assertFalse(static1Node.hasProperty("label"));
        assertFalse(static2Node.hasProperty("label"));
    }

    @Test
    public void systemLanguagesFieldAddedOnUpdateTo511() throws Exception {
        // GIVEN
        String fieldTypes = "/modules/security-app/fieldTypes";
        Node fieldTypesNode = NodeUtil.createPath(session.getRootNode(), fieldTypes, NodeTypes.ContentNode.NAME);
        assertFalse(fieldTypesNode.hasNode("systemLanguagesField"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertTrue(fieldTypesNode.hasNode("systemLanguagesField"));
        assertEquals(SystemLanguagesFieldDefinition.class.getName(), fieldTypesNode.getNode("systemLanguagesField").getProperty("definitionClass").getString());
        assertThat(fieldTypesNode.getNode("systemLanguagesField"), hasProperty("factoryClass"));
    }

    @Test
    public void userDialogLanguageFieldHasNewDefinitionOnUpdateTo511() throws Exception {
        // GIVEN
        String languageField = "/modules/security-app/dialogs/user/form/tabs/user/fields/language";
        Node languageNode = NodeUtil.createPath(session.getRootNode(), languageField, NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(languageNode, "class", SelectFieldDefinition.class.getName());

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertEquals(SystemLanguagesFieldDefinition.class.getName(), languageNode.getProperty("class").getString());
    }

    @Test
    public void userDialogLanguageFieldOptionsHaveBeenRemovedOnUpdateTo511() throws Exception {
        // GIVEN
        String languageField = "/modules/security-app/dialogs/user/form/tabs/user/fields/language";
        Node languageNode = NodeUtil.createPath(session.getRootNode(), languageField, NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(languageNode, "options", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1"));

        // THEN
        assertFalse(languageNode.hasNode("options"));
    }

    @Test
    public void folderSupportAddedToGroupsSubAppOnUpdateTo52() throws Exception {
        // GIVEN
        String base = "/modules/security-app/apps/security/subApps/groups";
        NodeUtil.createPath(session.getRootNode(), base + "/actions/addGroup/availability", NodeTypes.ContentNode.NAME).setProperty("nodes", "foobar");
        NodeUtil.createPath(session.getRootNode(), base + "/workbench", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(session.getRootNode(), base + "/actionbar/sections/root/groups/addActions/items", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1.1"));

        // THEN
        assertTrue(session.nodeExists(base + "/actions/addFolder"));
        assertTrue(session.nodeExists(base + "/actions/deleteFolder"));
        assertTrue(session.nodeExists(base + "/actions/editFolder"));
        assertTrue(session.nodeExists(base + "/actions/addGroup/availability/nodeTypes"));
        assertFalse(session.propertyExists(base + "/actions/addGroup/availability/nodes"));
        assertTrue(session.nodeExists(base + "/actions/deleteGroup/availability"));
        assertTrue(session.nodeExists(base + "/actions/editGroup/availability"));
        assertTrue(session.propertyExists(base + "/workbench/dropConstraintClass"));
        assertTrue(session.nodeExists(base + "/actionbar/sections/folder"));
        assertTrue(session.nodeExists(base + "/actionbar/sections/root/groups/addActions/items/addFolder"));
    }

    @Test
    public void folderSupportAddedToRolesSubAppOnUpdateTo52() throws Exception {
        // GIVEN
        String base = "/modules/security-app/apps/security/subApps/roles";
        NodeUtil.createPath(session.getRootNode(), base + "/actions/addRole/availability", NodeTypes.ContentNode.NAME).setProperty("nodes", "foobar");
        NodeUtil.createPath(session.getRootNode(), base + "/workbench", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(session.getRootNode(), base + "/actionbar/sections/root/groups/addActions/items", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1.1"));

        // THEN
        assertTrue(session.nodeExists(base + "/actions/addFolder"));
        assertTrue(session.nodeExists(base + "/actions/deleteFolder"));
        assertTrue(session.nodeExists(base + "/actions/editFolder"));
        assertTrue(session.nodeExists(base + "/actions/addRole/availability/nodeTypes"));
        assertFalse(session.propertyExists(base + "/actions/addRole/availability/nodes"));
        assertTrue(session.nodeExists(base + "/actions/deleteRole/availability"));
        assertTrue(session.nodeExists(base + "/actions/editRole/availability"));
        assertTrue(session.propertyExists(base + "/workbench/dropConstraintClass"));
        assertTrue(session.nodeExists(base + "/actionbar/sections/folder"));
        assertTrue(session.nodeExists(base + "/actionbar/sections/root/groups/addActions/items/addFolder"));
    }

    @Test
    public void roleNameFieldIsConfiguredToUserConditionalReadOnlyTextFieldOnUpdateTo52() throws RepositoryException, ModuleManagementException {
        // GIVEN
        String fieldPath = "/modules/security-app/dialogs/role/form/tabs/role/fields/jcrName";
        NodeUtil.createPath(session.getRootNode(), fieldPath, NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.1.1"));

        // THEN
        assertTrue(session.nodeExists(fieldPath));
        assertEquals(ConditionalReadOnlyTextFieldDefinition.class.getName(), session.getProperty(fieldPath + "/class").getString());
        assertEquals("superuser", session.getProperty(fieldPath + "/conditionalValue").getString());
    }

    @Test
    public void setEditUserActionAsDefaultOnUpdateto521() throws RepositoryException, ModuleManagementException {
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2"));

        // THEN
        assertEquals("editUser", session.getProperty("/modules/security-app/apps/security/subApps/users/actionbar/defaultAction").getString());
    }

    @Test
    public void setRegisterConditionalReadOnlyTextFieldTypeOnUpdateto521() throws RepositoryException, ModuleManagementException {
        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2"));

        // THEN
        assertTrue(session.getRootNode().hasNode("modules/security-app/fieldTypes/conditionalReadOnlyTextField"));
        assertEquals("info.magnolia.security.app.dialog.field.ConditionalReadOnlyTextFieldDefinition", session.getProperty("/modules/security-app/fieldTypes/conditionalReadOnlyTextField/definitionClass").getString());
        assertEquals("info.magnolia.security.app.dialog.field.ConditionalReadOnlyTextFieldFactory", session.getProperty("/modules/security-app/fieldTypes/conditionalReadOnlyTextField/factoryClass").getString());
    }

    @Test
    public void testUpdateFrom50() throws RepositoryException, ModuleManagementException {
        // GIVEN
        this.setupConfigProperty("/modules/security-app/dialogs/folder/actions/cancel", "label", "someLabel");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.0"));

        // THEN
        assertFalse(session.propertyExists("/modules/security-app/dialogs/folder/actions/cancel/label"));
        assertTrue(session.propertyExists("/modules/security-app/apps/security/subApps/users/actions/deleteFolder/availability/multiple"));
        assertTrue(session.propertyExists("/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability/multiple"));
    }

    @Test
    public void testUpdateFrom521() throws RepositoryException, ModuleManagementException {
        // GIVEN
        this.setupConfigProperty("/modules/security-app/apps/security/subApps/users/actions/deleteUser", "class", DeleteItemActionDefinition.class.getName());
        this.setupConfigProperty("/modules/security-app/apps/security/subApps/users/actions/deleteFolder", "class", DeleteItemActionDefinition.class.getName());
        this.setupConfigProperty("/modules/security-app/apps/security/subApps/users/actions/deleteItems", "class", DeleteItemActionDefinition.class.getName());
        this.setupConfigNode("/modules/security-app/apps/security/subApps/users/actionbar/sections/user/groups/deleteActions/items/deleteUser");
        this.setupConfigNode("/modules/security-app/apps/security/subApps/users/actionbar/sections/folder/groups/addActions/items/deleteFolder");
        this.setupConfigNode("/modules/security-app/apps/security/subApps/users/actionbar/sections/multiple/groups/addActions/items/deleteItems");
        this.setupConfigNode("/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/deleteActions/items/deleteGroup");
        this.setupConfigNode("/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/deleteActions/items/deleteRole");
        this.setupConfigProperty("/modules/security-app/apps/security/subApps/roles/actions/deleteFolder", "class", "info.magnolia.ui.framework.action.DeleteItemActionDefinition");
        this.setupConfigProperty("/modules/security-app/apps/security/subApps/groups/actions/deleteFolder", "class", "info.magnolia.ui.framework.action.DeleteItemActionDefinition");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.1"));

        // THEN
        assertFalse(session.propertyExists("/modules/security-app/apps/security/subApps/roles/actions/deleteFolder/implementationClass"));
        assertFalse(session.propertyExists("/modules/security-app/apps/security/subApps/groups/actions/deleteFolder/implementationClass"));

        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/groups/actions/moveGroup"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/roles/actions/moveRole"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/users/actions/moveUser"));

        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/groups/actions/duplicateGroup"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/roles/actions/duplicateRole"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/users/actions/duplicateUser"));

        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/users/actionbar/sections/user/groups/editActions/items/moveUser"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/editActions/items/moveGroup"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/editActions/items/moveRole"));

        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/users/actionbar/sections/user/groups/editActions/items/duplicateUser"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/editActions/items/duplicateGroup"));
        assertTrue(session.nodeExists("/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/editActions/items/duplicateRole"));
        assertEquals(session.getProperty("/modules/security-app/apps/security/subApps/roles/actions/deleteFolder/class").getString(), DeleteFolderActionDefinition.class.getName());
        assertEquals(session.getProperty("/modules/security-app/apps/security/subApps/groups/actions/deleteFolder/class").getString(), DeleteFolderActionDefinition.class.getName());
        assertEquals(DeleteActionDefinition.class.getName() ,session.getProperty("/modules/security-app/apps/security/subApps/users/actions/deleteUser/class").getString());
        assertEquals(DeleteActionDefinition.class.getName() ,session.getProperty("/modules/security-app/apps/security/subApps/users/actions/deleteFolder/class").getString());
        assertEquals(DeleteActionDefinition.class.getName() ,session.getProperty("/modules/security-app/apps/security/subApps/users/actions/deleteItems/class").getString());
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/users/actions/confirmDeleteUser"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/users/actions/confirmDeleteFolder"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/users/actions/confirmDeleteItems"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/users/actionbar/sections/user/groups/deleteActions/items/confirmDeleteUser"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/users/actionbar/sections/folder/groups/addActions/items/confirmDeleteFolder"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/users/actionbar/sections/multiple/groups/addActions/items/confirmDeleteItems"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/deleteActions/items/deleteGroup"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/deleteActions/items/deleteRole"));
    }

    @Test
    public void testUpdateTo523SetsWritePermissionForSecuritySubappActions() throws Exception {
        // GIVEN
        Node editUserAction = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/editUser", NodeTypes.ContentNode.NAME);
        Node addGroupAction = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/groups/actions/addGroup", NodeTypes.ContentNode.NAME);
        Node deleteRoleAction = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/roles/actions/deleteRole", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.2"));

        // THEN
        assertTrue(editUserAction.hasNode("availability"));
        Node availability = editUserAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());

        assertTrue(addGroupAction.hasNode("availability"));
        availability = addGroupAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());

        assertTrue(deleteRoleAction.hasNode("availability"));
        availability = deleteRoleAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());
    }

    @Test
    public void testUpdateTo524() throws Exception {
        // GIVEN
        setupConfigNode("/modules/security-app/apps/security/subApps/roles/actionbar/sections/folder/groups/addActions/items/deleteFolder");
        setupConfigNode("/modules/security-app/apps/security/subApps/groups/actionbar/sections/folder/groups/addActions/items/deleteFolder");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.2.3"));

        // THEN
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/roles/actionbar/sections/folder/groups/addActions/items/deleteFolder"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/groups/actionbar/sections/folder/groups/addActions/items/deleteFolder"));
    }

    @Test
    public void updateFrom53SetsWritePermissionForUsersSubappActions() throws Exception {
        // GIVEN
        Node activateAction = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/activate", NodeTypes.ContentNode.NAME);
        Node deactivateAction = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/deactivate", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3"));

        // THEN
        assertTrue(activateAction.hasNode("availability"));
        Node availability = activateAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());

        assertTrue(deactivateAction.hasNode("availability"));
        availability = deactivateAction.getNode("availability");
        assertTrue(availability.hasProperty("writePermissionRequired"));
        assertTrue(availability.getProperty("writePermissionRequired").getBoolean());
    }

    @Test
    public void updateFrom531ReconfigureDeleteFolderActions() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/groups/actions/deleteFolder", NodeTypes.ContentNode.NAME).setProperty("class", "info.magnolia.security.app.action.DeleteEmptyFolderActionDefinition");
        NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/roles/actions/deleteFolder", NodeTypes.ContentNode.NAME).setProperty("class", "info.magnolia.security.app.action.DeleteEmptyFolderActionDefinition");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.1"));

        // THEN
        assertEquals(session.getProperty("/modules/security-app/apps/security/subApps/roles/actions/deleteFolder/class").getString(), DeleteFolderActionDefinition.class.getName());
        assertEquals(session.getProperty("/modules/security-app/apps/security/subApps/groups/actions/deleteFolder/class").getString(), DeleteFolderActionDefinition.class.getName());

    }

    @Test
    public void updateFrom533ReconfigureDuplicateRoleAction() throws Exception {
        // GIVEN
        Node duplicateRole = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/roles/actions/duplicateRole", NodeTypes.ContentNode.NAME);
        duplicateRole.setProperty("class", "info.magnolia.ui.framework.action.DuplicateNodeActionDefinition");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.3"));

        // THEN
        assertEquals("info.magnolia.security.app.dialog.action.DuplicateRoleActionDefinition", duplicateRole.getProperty("class").getString());
    }

    @Test
    public void updateFrom533AddSearchToSubApps() throws Exception {
        // GIVEN

        Node contentViewsUsers = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/workbench/contentViews", NodeTypes.ContentNode.NAME);
        Node contentViewsGroups = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/groups/workbench/contentViews", NodeTypes.ContentNode.NAME);
        Node contentViewsRoles = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/roles/workbench/contentViews", NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.3"));

        // THEN
        assertTrue(contentViewsUsers.hasNode("list"));
        assertTrue(contentViewsUsers.hasNode("search"));
        assertTrue(contentViewsGroups.hasNode("list"));
        assertTrue(contentViewsGroups.hasNode("search"));
        assertTrue(contentViewsRoles.hasNode("list"));
        assertTrue(contentViewsRoles.hasNode("search"));
    }

    @Test
    public void updateFrom535ReconfigureDuplicateUserAction() throws Exception {
        // GIVEN
        Node duplicateUser = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/apps/security/subApps/users/actions/duplicateUser", NodeTypes.ContentNode.NAME);
        duplicateUser.setProperty("class", "info.magnolia.ui.framework.action.DuplicateNodeActionDefinition");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.6"));

        // THEN
        assertEquals("info.magnolia.security.app.dialog.action.DuplicateUserActionDefinition", duplicateUser.getProperty("class").getString());
    }

    @Test
    public void updateFrom538AddEmailValidator() throws Exception {
        // GIVEN
        setupConfigNode("/modules/security-app/dialogs/user/form/tabs/user/fields/email");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.3.8"));

        // THEN
        assertThat(session.getNode("/modules/security-app/dialogs/user/form/tabs/user/fields/email"), hasNode("validators"));
    }

    @Test
    public void updateFrom54RemoveIndependentConfirmDialogs() throws Exception {
        // GIVEN
        setupConfigNode("/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/deleteActions/items/confirmDeleteGroup");
        setupConfigNode("/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/deleteActions/items/confirmDeleteRole");
        setupConfigNode("/modules/security-app/apps/security/subApps/groups/actionbar/sections/folder/groups/addActions/items/confirmDeleteFolder");
        setupConfigNode("/modules/security-app/apps/security/subApps/roles/actionbar/sections/folder/groups/addActions/items/confirmDeleteFolder");
        setupConfigNode("/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteRole");
        setupConfigNode("/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteFolder");
        setupConfigNode("/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteGroup");
        setupConfigNode("/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteFolder");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.4"));

        // THEN
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/deleteActions/items/deleteGroup"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/deleteActions/items/deleteRole"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/groups/actionbar/sections/folder/groups/addActions/items/deleteFolder"));
        assertTrue(session.itemExists("/modules/security-app/apps/security/subApps/roles/actionbar/sections/folder/groups/addActions/items/deleteFolder"));
        assertFalse(session.itemExists("/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteRole"));
        assertFalse(session.itemExists("/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteFolder"));
        assertFalse(session.itemExists("/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteGroup"));
        assertFalse(session.itemExists("/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteFolder"));
    }

    @Test
    public void updateFrom541AddSortableProperty() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.4.1"));

        //THEN
        assertTrue(session.propertyExists("/modules/security-app/apps/security/subApps/users/workbench/contentViews/tree/sortable"));
    }

    @Test
    public void updateFrom543ReconfiguresFactoryClassOfSystemLanguagesField() throws Exception {
        // GIVEN
        Node systemLanguagesNode = NodeUtil.createPath(session.getRootNode(), "/modules/security-app/fieldTypes/systemLanguagesField", NodeTypes.ContentNode.NAME);
        systemLanguagesNode.setProperty("factoryClass", "info.magnolia.ui.form.field.factory.SelectFieldFactory");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.4.3"));

        // THEN
        assertThat(systemLanguagesNode, hasProperty("factoryClass", "info.magnolia.security.app.dialog.field.SystemLanguagesFieldFactory"));
    }
}
