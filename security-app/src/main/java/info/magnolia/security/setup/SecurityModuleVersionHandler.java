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

import static info.magnolia.jcr.nodebuilder.Ops.*;

import info.magnolia.i18nsystem.setup.RemoveHardcodedI18nPropertiesFromDialogsTask;
import info.magnolia.i18nsystem.setup.RemoveHardcodedI18nPropertiesFromSubappsTask;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CheckAndModifyPartOfPropertyValueTask;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.OrderNodeToFirstPositionTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.RenameNodeTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.action.DeleteEmptyFolderActionDefinition;
import info.magnolia.security.app.container.GroupDropConstraint;
import info.magnolia.security.app.container.RoleDropConstraint;
import info.magnolia.security.app.dialog.field.ConditionalReadOnlyTextFieldDefinition;
import info.magnolia.security.app.dialog.field.SystemLanguagesFieldDefinition;
import info.magnolia.ui.admincentral.setup.ConvertAclToAppPermissionTask;
import info.magnolia.ui.contentapp.setup.for5_3.ContentAppMigrationTask;
import info.magnolia.ui.framework.action.DeleteActionDefinition;
import info.magnolia.ui.framework.setup.AddIsPublishedRuleToAllDeactivateActionsTask;
import info.magnolia.ui.framework.setup.SetWritePermissionForActionsTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for Security app module.
 */
public class SecurityModuleVersionHandler extends DefaultModuleVersionHandler {

    public SecurityModuleVersionHandler() {

        register(DeltaBuilder.update("5.0", "")
                .addTask(new ConvertAclToAppPermissionTask("Convert permissions for 'Security' app", "Convert ACL permissions for old 'Security' menu to new 'security-app' permission",
                        "/modules/adminInterface/config/menu/security", "/modules/security-app/apps/security", true)));

        register(DeltaBuilder.update("5.0.1", "")

                .addTask(new NodeExistsDelegateTask("Change label of folder creation action to 'Add folder'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addFolder",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of folder creation action to 'Add folder'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addFolder", "label", "New folder", "Add folder")))

                .addTask(new NodeExistsDelegateTask("Change label of user creation action to 'Add user'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addUser",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of user creation action to 'Add user'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/addUser", "label", "New user", "Add user")))

                .addTask(new NodeExistsDelegateTask("Change label of user creation action to 'Add group'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/addGroup",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of user creation action to 'Add group'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/addGroup", "label", "New group", "Add group")))

                .addTask(new NodeExistsDelegateTask("Change label of role creation action to 'Add role'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/addRole",
                        new CheckAndModifyPartOfPropertyValueTask("Change label of role creation action to 'Add role'", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/addRole", "label", "New role", "Add role")))

        );

        register(DeltaBuilder.update("5.1", "")
                .addTask(new PartialBootstrapTask("Bootstrap Delete Items action in Security app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/users/actions/deleteItems"))
                .addTask(new PartialBootstrapTask("Bootstrap new actionbar section in Security app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/users/actionbar/sections/multiple"))
                .addTask(new NodeExistsDelegateTask("Set ruleClass for deleteUser action to IsNotCurrentUserRule", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability",
                        new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability", "ruleClass", "info.magnolia.security.app.action.availability.IsNotCurrentUserRule")))
                .addTask(new NodeExistsDelegateTask("Update class for deleteGroup action", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/deleteGroup",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/deleteGroup", "class", "info.magnolia.security.app.action.DeleteGroupActionDefinition")))
                .addTask(new NodeExistsDelegateTask("Update class for deleteRole action", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/deleteRole",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/deleteRole", "class", "info.magnolia.security.app.action.DeleteRoleActionDefinition")))
                .addTask(new RemovePropertyTask("Remove label from form field", "Remove label property from the static1 field of the ACL tab of the Role dialog", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/acls/fields/static1", "label"))
                .addTask(new RemovePropertyTask("Remove label from form field", "Remove label property from the static1 field of the ACL tab of the Role dialog", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/acls/fields/static2", "label"))
                // Remove hardcoded i18n properties, e.g. label, description, etc.
                .addTask(new RemoveHardcodedI18nPropertiesFromSubappsTask("security-app")));

        register(DeltaBuilder.update("5.1.1", "")
                .addTask(new PartialBootstrapTask("Bootstrap SystemLanguages field type.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.fieldTypes.xml", "/fieldTypes/systemLanguagesField"))
                .addTask(new NodeExistsDelegateTask("Update definition class for language field in user dialog.", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language",
                        new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language", "class", SystemLanguagesFieldDefinition.class.getName())))
                .addTask(new NodeExistsDelegateTask("Remove now unnecessary options from language field in user dialog.", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language/options",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/user/form/tabs/user/fields/language/options"))));

        register(DeltaBuilder.update("5.2", "")
                .addTask(new ArrayDelegateTask("Add folder support to groups sub app.", "",
                        new PartialBootstrapTask("Bootstrap add folder action in groups sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/groups/actions/addFolder"),
                        new PartialBootstrapTask("Bootstrap delete folder action in groups sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/groups/actions/deleteFolder"),
                        new PartialBootstrapTask("Bootstrap edit folder action in groups sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/groups/actions/editFolder"),
                        new PartialBootstrapTask("Bootstrap availability of add group action.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/groups/actions/addGroup/availability/nodeTypes"),
                        new NodeExistsDelegateTask("Remove constraint on add group action on nodes.", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/addGroup/availability",
                                new RemovePropertyTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/addGroup/availability", "nodes")),
                        new PartialBootstrapTask("Bootstrap availability for delete group action.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/groups/actions/deleteGroup/availability"),
                        new PartialBootstrapTask("Bootstrap availability for edit group action.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/groups/actions/editGroup/availability"),
                        new NodeExistsDelegateTask("Set drop constraint for drag and drop support in groups sub app.", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/workbench",
                                new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/workbench", "dropConstraintClass", GroupDropConstraint.class.getName())),
                        new PartialBootstrapTask("Bootstrap action bar section for folders in groups sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/groups/actionbar/sections/folder"),
                        new NodeExistsDelegateTask("Configure add folder action in actionbar in groups sub app.", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actionbar/sections/root/groups/addActions/items",
                                new CreateNodeTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actionbar/sections/root/groups/addActions/items", "addFolder", NodeTypes.ContentNode.NAME))
                        ))
                .addTask(new ArrayDelegateTask("Add folder support to roles sub app.", "",
                        new PartialBootstrapTask("Bootstrap add folder action in roles sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/roles/actions/addFolder"),
                        new PartialBootstrapTask("Bootstrap delete folder action in roles sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/roles/actions/deleteFolder"),
                        new PartialBootstrapTask("Bootstrap edit folder action in roles sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/roles/actions/editFolder"),
                        new PartialBootstrapTask("Bootstrap availability of add role action.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/roles/actions/addRole/availability/nodeTypes"),
                        new NodeExistsDelegateTask("Remove constraint on add role action on nodes.", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/addRole/availability",
                                new RemovePropertyTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/addRole/availability", "nodes")),
                        new PartialBootstrapTask("Bootstrap availability for delete role action.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/roles/actions/deleteRole/availability"),
                        new PartialBootstrapTask("Bootstrap availability for edit role action.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/roles/actions/editRole/availability"),
                        new NodeExistsDelegateTask("Set drop constraint for drag and drop support in roles sub app.", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/workbench",
                                new SetPropertyTask("", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/workbench", "dropConstraintClass", RoleDropConstraint.class.getName())),
                        new PartialBootstrapTask("Bootstrap action bar section for folders in roles sub app.", "", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/roles/actionbar/sections/folder"),
                        new NodeExistsDelegateTask("Configure add folder action in actionbar in roles sub app.", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actionbar/sections/root/groups/addActions/items",
                                new CreateNodeTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actionbar/sections/root/groups/addActions/items", "addFolder", NodeTypes.ContentNode.NAME))
                        ))
                .addTask(new RemovePropertyTask("Remove hardcoded field", "Remove hardcoded description of acl tab from role dialog: static1", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/acls/fields/static1", "value"))
                .addTask(new RemovePropertyTask("Remove hardcoded field", "Remove hardcoded description of acl tab from role dialog: static2", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/acls/fields/static2", "value"))
                .addTask(new NodeExistsDelegateTask("Disallow renaming the superuser role.", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/role/fields/jcrName",
                        new ArrayDelegateTask("Configure role name field to be read only", "",
                                new SetPropertyTask("Change the field type", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/role/fields/jcrName", "class", ConditionalReadOnlyTextFieldDefinition.class.getName()),
                                new SetPropertyTask("Set the conditional value to superuser", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role/form/tabs/role/fields/jcrName", "conditionalValue", "superuser"))
                        )));
        register(DeltaBuilder.update("5.2.1", "")
                .addTask(new NewPropertyTask("Add user default action", "Adds edit user default action when a user is selected.", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actionbar", "defaultAction", "editUser"))
                .addTask(new NodeExistsDelegateTask("Add conditionalReadOnlyTextField field type", "Add conditionalReadOnlyTextField field type if it is not exist", RepositoryConstants.CONFIG, "/modules/security-app/fieldTypes/conditionalReadOnlyTextField", null,
                        new PartialBootstrapTask("Bootsrap conditionalReadOnlyTextField field type", "", "/mgnl-bootstrap/security-app/config.modules.security-app.fieldTypes.xml", "/fieldTypes/conditionalReadOnlyTextField")))
        );

        register(DeltaBuilder.update("5.2.2", "")
                .addTask(new RemoveHardcodedI18nPropertiesFromDialogsTask("security-app"))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteUser/availability", "multiple", "true"))
                .addTask(new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteFolder/availability", "multiple", "true"))
                .addTask(new NodeExistsDelegateTask("Reconfigure deleteUser action", "Change class to info.magnolia.ui.framework.action.DeleteActionDefinition", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteUser",
                        new CheckAndModifyPropertyValueTask("", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actions/deleteUser", "class", "info.magnolia.ui.framework.action.DeleteItemActionDefinition", "info.magnolia.ui.framework.action.DeleteActionDefinition")))
                .addTask(new DeleteFolderActionReconfigurationTask("groups"))
                .addTask(new DeleteFolderActionReconfigurationTask("roles"))
                .addTask(new NodeExistsDelegateTask("Reconfigure deleteUser action", "/modules/security-app/apps/security/subApps/users/actions/deleteUser",
                        new CheckAndModifyPropertyValueTask("/modules/security-app/apps/security/subApps/users/actions/deleteUser", "class", "info.magnolia.ui.framework.action.DeleteItemActionDefinition", DeleteActionDefinition.class.getName())))
                .addTask(new NodeExistsDelegateTask("Reconfigure deleteFolder action of users subApp", "/modules/security-app/apps/security/subApps/users/actions/deleteFolder",
                        new CheckAndModifyPropertyValueTask("/modules/security-app/apps/security/subApps/users/actions/deleteFolder", "class", "info.magnolia.ui.framework.action.DeleteItemActionDefinition", DeleteActionDefinition.class.getName())))
                .addTask(new NodeExistsDelegateTask("Reconfigure deleteItems action of users subApp", "/modules/security-app/apps/security/subApps/users/actions/deleteItems",
                        new CheckAndModifyPropertyValueTask("/modules/security-app/apps/security/subApps/users/actions/deleteItems", "class", "info.magnolia.ui.framework.action.DeleteItemActionDefinition", DeleteActionDefinition.class.getName())))
                .addTask(new PartialBootstrapTask("Add confirmDeleteUser action into users subApp", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/users/actions/confirmDeleteUser"))
                .addTask(new PartialBootstrapTask("Add confirmDeleteFolder action into users subApp", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/users/actions/confirmDeleteFolder"))
                .addTask(new PartialBootstrapTask("Add confirmDeleteItems action into users subApp", "/mgnl-bootstrap/security-app/config.modules.security-app.apps.security.xml", "/security/subApps/users/actions/confirmDeleteItems"))
                .addTask(new NodeExistsDelegateTask("Reconfigure actionbar of users subApp to use confirmDeleteUser action", "/modules/security-app/apps/security/subApps/users/actionbar/sections/user/groups/deleteActions/items/deleteUser",
                        new RenameNodeTask("Reconfigure actionbar of users subApp to use confirmDeleteUser action", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actionbar/sections/user/groups/deleteActions/items", "deleteUser", "confirmDeleteUser", false)))
                .addTask(new NodeExistsDelegateTask("Reconfigure actionbar of users subApp to use confirmDeleteFolder action", "/modules/security-app/apps/security/subApps/users/actionbar/sections/folder/groups/addActions/items/deleteFolder",
                        new RenameNodeTask("Reconfigure actionbar of users subApp to use confirmDeleteUser action", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actionbar/sections/folder/groups/addActions/items", "deleteFolder", "confirmDeleteFolder", false)))
                .addTask(new NodeExistsDelegateTask("Reconfigure actionbar of users subApp to use confirmDeleteItems action", "/modules/security-app/apps/security/subApps/users/actionbar/sections/multiple/groups/addActions/items/deleteItems",
                        new RenameNodeTask("Reconfigure actionbar of users subApp to use confirmDeleteUser action", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/users/actionbar/sections/multiple/groups/addActions/items", "deleteItems", "confirmDeleteItems", false)))
                .addTask(new RemovePropertyTask("Remove implementation class property from delete folder action of roles sub-app", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actions/deleteFolder/", "implementationClass"))
                .addTask(new RemovePropertyTask("Remove implementation class property from delete folder action of groups sub-app", "", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actions/deleteFolder/", "implementationClass"))
                .addTask(new AddDuplicateAndMoveActionsToSecurityAppTask())
        );

        register(DeltaBuilder.update("5.2.3", "")
                .addTask(new SetWritePermissionForActionsTask("/modules/security-app/apps/security/subApps/users/actions",
                        new String[] { "editUser", "addUser", "deleteUser", "confirmDeleteUser", "addFolder", "deleteFolder", "confirmDeleteFolder", "editFolder", "deleteItems", "duplicateUser", "moveUser", "confirmDeleteItems" }))
                .addTask(new SetWritePermissionForActionsTask("/modules/security-app/apps/security/subApps/groups/actions",
                        new String[] { "addGroup", "deleteGroup", "confirmDeleteGroup", "editGroup", "addFolder", "deleteFolder", "editFolder", "duplicateGroup", "moveGroup" }))
                .addTask(new SetWritePermissionForActionsTask("/modules/security-app/apps/security/subApps/roles/actions",
                        new String[] { "deleteRole", "confirmDeleteRole", "editRole", "addRole", "addFolder", "deleteFolder", "editFolder", "duplicateRole", "moveRole" }))
        );

        register(DeltaBuilder.update("5.2.4", "")
                        .addTask(new NodeExistsDelegateTask("Reconfigure save action of user dialog", "/modules/security-app/dialogs/user/actions/commit",
                                new CheckAndModifyPropertyValueTask("/modules/security-app/dialogs/user/actions/commit", "class", "info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition", "info.magnolia.security.app.dialog.action.SaveUserDialogActionDefinition")))
        );

        register(DeltaBuilder.update("5.3", "")
                .addTask(new ContentAppMigrationTask("/modules/security-app"))
                .addTask(new RemovePropertyTask("", "/modules/security-app/apps/security/subApps/roles/workbench/contentViews/tree", "implementationClass"))
                .addTask(new RemovePropertyTask("", "/modules/security-app/apps/security/subApps/users/workbench/contentViews/tree", "implementationClass"))
                );

        register(DeltaBuilder.update("5.3.1", "")
                .addTask(new NewPropertyTask("Set security app role dialog to wide", "", RepositoryConstants.CONFIG, "/modules/security-app/dialogs/role","wide", true))
                .addTask(new SetWritePermissionForActionsTask("/modules/security-app/apps/security/subApps/users/actions", new String[] { "activate", "deactivate" }))
        );

        register(DeltaBuilder.update("5.3.2", "")
                .addTask(new ArrayDelegateTask("Reconfigure delete folder actions",
                        new NodeExistsDelegateTask("Reconfigure delete folder action in Groups", "/modules/security-app/apps/security/subApps/groups/actions/deleteFolder",
                                new CheckAndModifyPropertyValueTask("/modules/security-app/apps/security/subApps/groups/actions/deleteFolder", "class", "info.magnolia.security.app.action.DeleteEmptyFolderActionDefinition", "info.magnolia.security.app.action.DeleteFolderActionDefinition")),
                        new NodeExistsDelegateTask("Reconfigure delete folder action in Roles", "/modules/security-app/apps/security/subApps/roles/actions/deleteFolder",
                                new CheckAndModifyPropertyValueTask("/modules/security-app/apps/security/subApps/roles/actions/deleteFolder", "class", "info.magnolia.security.app.action.DeleteEmptyFolderActionDefinition", "info.magnolia.security.app.action.DeleteFolderActionDefinition"))
                        ))
        );

        register(DeltaBuilder.update("5.3.4", "")
                .addTask(
                        new NodeExistsDelegateTask("Reconfigure duplicate role action in Groups", "/modules/security-app/apps/security/subApps/roles/actions/duplicateRole",
                                new CheckAndModifyPropertyValueTask(
                                        "/modules/security-app/apps/security/subApps/roles/actions/duplicateRole",
                                        "class",
                                        "info.magnolia.ui.framework.action.DuplicateNodeActionDefinition",
                                        "info.magnolia.security.app.dialog.action.DuplicateRoleActionDefinition")
                        ))
                .addTask(new AddListAndSearchViewTask("users"))
                .addTask(new AddListAndSearchViewTask("groups"))
                .addTask(new AddListAndSearchViewTask("roles"))
        );
        register(DeltaBuilder.update("5.3.6", "")
                .addTask(new AddIsPublishedRuleToAllDeactivateActionsTask("","/modules/security-app/apps/"))
        );
        register(DeltaBuilder.update("5.3.7", "")
                .addTask(
                        new NodeExistsDelegateTask("Reconfigure duplicate user action in Users", "/modules/security-app/apps/security/subApps/users/actions/duplicateUser",
                                new CheckAndModifyPropertyValueTask(
                                        "/modules/security-app/apps/security/subApps/users/actions/duplicateUser",
                                        "class",
                                        "info.magnolia.ui.framework.action.DuplicateNodeActionDefinition",
                                        "info.magnolia.security.app.dialog.action.DuplicateUserActionDefinition")
                        ))
        );
        register(DeltaBuilder.update("5.3.9", "")
                .addTask(
                        new NodeExistsDelegateTask("Add validator for email field in user tab", "/modules/security-app/dialogs/user/form/tabs/user/fields/email/validators", null,
                                new PartialBootstrapTask("", "/mgnl-bootstrap/security-app/config.modules.security-app.dialogs.user.xml", "/user/form/tabs/user/fields/email/validators"))
                )
        );
        register(DeltaBuilder.update("5.4.1", "")
                .addTask(new NodeExistsDelegateTask("Reconfigure actionbar of groups subApp to use deleteGroup action", "/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/deleteActions/items/confirmDeleteGroup",
                        new RenameNodeTask("Reconfigure actionbar of groups subApp to use deleteGroup action", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actionbar/sections/group/groups/deleteActions/items", "confirmDeleteGroup", "deleteGroup", false)))
                .addTask(new NodeExistsDelegateTask("Reconfigure actionbar of roles subApp to use deleteRole action", "/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/deleteActions/items/confirmDeleteRole",
                        new RenameNodeTask("Reconfigure actionbar of roles subApp to use deleteRole action", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actionbar/sections/role/groups/deleteActions/items", "confirmDeleteRole", "deleteRole", false)))
                .addTask(new ArrayDelegateTask("Reconfigure actionbar to use deleteFolder action",
                        new NodeExistsDelegateTask("", "/modules/security-app/apps/security/subApps/groups/actionbar/sections/folder/groups/addActions/items/confirmDeleteFolder",
                                new RenameNodeTask("", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/groups/actionbar/sections/folder/groups/addActions/items", "confirmDeleteFolder", "deleteFolder", true)),
                        new NodeExistsDelegateTask("", "/modules/security-app/apps/security/subApps/roles/actionbar/sections/folder/groups/addActions/items/confirmDeleteFolder",
                                new RenameNodeTask("", RepositoryConstants.CONFIG, "/modules/security-app/apps/security/subApps/roles/actionbar/sections/folder/groups/addActions/items", "confirmDeleteFolder", "deleteFolder", true))))
                .addTask(new ArrayDelegateTask("Remove unused cofirmDelete actions.",
                        new NodeExistsDelegateTask("Remove confirmDeleteRole action.", "/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteRole",
                                new RemoveNodeTask("Remove confirmDeleteRole action.", "/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteRole")),
                        new NodeExistsDelegateTask("Remove roles/confirmDeleteFolder action.", "/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteFolder",
                                new RemoveNodeTask("Remove roles/confirmDeleteFolder action.", "/modules/security-app/apps/security/subApps/roles/actions/confirmDeleteFolder")),
                        new NodeExistsDelegateTask("Remove confirmDeleteGroup action.", "/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteGroup",
                                new RemoveNodeTask("Remove confirmDeleteGroup action.", "/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteGroup")),
                        new NodeExistsDelegateTask("Remove groups/confirmDeleteFolder action.", "/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteFolder",
                                new RemoveNodeTask("Remove groups/confirmDeleteFolder action.", "/modules/security-app/apps/security/subApps/groups/actions/confirmDeleteFolder"))
                        ))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        Task orderNodeTo1stPosTask = new OrderNodeToFirstPositionTask("Security app ordering", "Moves the security app before the configuration app", RepositoryConstants.CONFIG, "modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/security");
        NodeExistsDelegateTask delegateTask = new NodeExistsDelegateTask("Security app ordering delegate task", "Moves the security app before the configuration app if the node exists", RepositoryConstants.CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/security", orderNodeTo1stPosTask);
        tasks.add(delegateTask);
        return tasks;
    }

    /**
     * Task capable of fixing DeleteFolder settings of Security app sub-apps. Precisely it sets definition class to {@link DeleteEmptyFolderActionDefinition} and removes implementationClass property as unnecessary.
     */
    private static class DeleteFolderActionReconfigurationTask extends NodeExistsDelegateTask {

        private final static String actionPathTemplate = "/modules/security-app/apps/security/subApps/%s/actions/deleteFolder/";
        public static final String taskTitleTemplate = "Reconfigure delete folder action of %s sub-app";

        public DeleteFolderActionReconfigurationTask(String subAppName) {
            super(String.format(taskTitleTemplate, subAppName), String.format(actionPathTemplate, subAppName), new ArrayDelegateTask(
                    "",
                    new RemovePropertyTask("", "", RepositoryConstants.CONFIG, String.format(actionPathTemplate, subAppName), "implementationClass"),
                    new SetPropertyTask(RepositoryConstants.CONFIG, String.format(actionPathTemplate, subAppName), "class", DeleteEmptyFolderActionDefinition.class.getName())
                    ));
        }
    }

    /**
     * Add a List and Search view to a Security subapp which has only a Tree view.
     */
    private static class AddListAndSearchViewTask extends NodeBuilderTask {

        private final static String workbenchPathTemplate = "/modules/security-app/apps/security/subApps/%s/workbench/";
        private final static String taskTitleTemplate = "Add List and Search view to %s sub-app, in the Security app.";

        public AddListAndSearchViewTask(String subAppName) {
            super(String.format(taskTitleTemplate, subAppName), "", ErrorHandling.logging, RepositoryConstants.CONFIG, String.format(workbenchPathTemplate, subAppName),
                    getNode("contentViews").then(
                            addNode("list", NodeTypes.Content.NAME).then(
                                    addProperty("class", "info.magnolia.ui.workbench.list.ListPresenterDefinition"),
                                    addProperty("extends", "../tree")),
                            addNode("search", NodeTypes.Content.NAME).then(
                                    addProperty("class", "info.magnolia.ui.workbench.search.SearchPresenterDefinition"),
                                    addProperty("extends", "../list"))
                            ));
        }
    }

}
