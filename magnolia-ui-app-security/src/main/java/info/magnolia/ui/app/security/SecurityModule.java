/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.security;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.admincentral.image.DefaultImageProvider;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.security.column.UserNameColumnDefinition;
import info.magnolia.ui.app.security.column.UserNameColumnFormatter;
import info.magnolia.ui.app.security.dialog.action.SaveGroupDialogAction;
import info.magnolia.ui.app.security.dialog.action.SaveRoleDialogAction;
import info.magnolia.ui.app.security.dialog.action.SaveUserDialogAction;
import info.magnolia.ui.app.security.dialog.field.EnabledFieldBuilder;
import info.magnolia.ui.app.security.dialog.field.GroupManagementFieldBuilder;
import info.magnolia.ui.app.security.dialog.field.RoleManagementFieldBuilder;
import info.magnolia.ui.app.security.dialog.field.validator.UniqueGroupIdValidatorDefinition;
import info.magnolia.ui.app.security.dialog.field.validator.UniqueRoleIdValidatorDefinition;
import info.magnolia.ui.app.security.dialog.field.validator.UniqueUserIdValidatorDefinition;
import info.magnolia.ui.contentapp.config.BrowserSubAppBuilder;
import info.magnolia.ui.contentapp.config.CodeConfigurationUtils;
import info.magnolia.ui.contentapp.config.ContentAppBuilder;
import info.magnolia.ui.contentapp.config.ContentAppConfig;
import info.magnolia.ui.dialog.config.Dialog;
import info.magnolia.ui.dialog.config.DialogBuilder;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.config.AbstractFieldBuilder;
import info.magnolia.ui.form.config.OptionBuilder;
import info.magnolia.ui.framework.app.config.App;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.config.UiConfig;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.model.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.workbench.column.DateColumnFormatter;
import info.magnolia.ui.workbench.column.StatusColumnFormatter;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.workbench.column.definition.StatusColumnDefinition;

import javax.inject.Inject;

/**
 * Module class for the Security App. It creates the app and sub-apps, as well as the dialogs.
 */
public class SecurityModule implements ModuleLifecycle {

    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private AppDescriptorRegistry appDescriptorRegistry;

    @Inject
    public SecurityModule(DialogDefinitionRegistry dialogDefinitionRegistry, AppDescriptorRegistry appDescriptorRegistry) {
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.appDescriptorRegistry = appDescriptorRegistry;
    }

    @App("security")
    public void securityApp(ContentAppBuilder app, UiConfig cfg, ContentAppConfig contentAppConfig) {

        // group
        CreateDialogActionDefinition addGroupAction = new CreateDialogActionDefinition();
        addGroupAction.setName("addGroup");
        addGroupAction.setLabel("New group");
        addGroupAction.setIcon("icon-add-item");
        addGroupAction.setNodeType(NodeTypes.Group.NAME);
        addGroupAction.setDialogName("ui-security-app:groupAdd");

        EditDialogActionDefinition editGroupAction = new EditDialogActionDefinition();
        editGroupAction.setName("editGroup");
        editGroupAction.setLabel("Edit group");
        editGroupAction.setIcon("icon-edit");
        editGroupAction.setDialogName("ui-security-app:groupEdit");

        // delete group
        DeleteItemActionDefinition deleteGroupActionDefinition = new DeleteItemActionDefinition();
        deleteGroupActionDefinition.setName("deleteGroup");
        deleteGroupActionDefinition.setLabel("Delete group");
        deleteGroupActionDefinition.setIcon("icon-delete");

        // delete role
        DeleteItemActionDefinition deleteRoleActionDefinition = new DeleteItemActionDefinition();
        deleteRoleActionDefinition.setName("deleteRole");
        deleteRoleActionDefinition.setLabel("Delete role");
        deleteRoleActionDefinition.setIcon("icon-delete");

        // role
        CreateDialogActionDefinition addRoleAction = new CreateDialogActionDefinition();
        addRoleAction.setName("addRole");
        addRoleAction.setLabel("New role");
        addRoleAction.setIcon("icon-add-item");
        addRoleAction.setNodeType(NodeTypes.Role.NAME);
        addRoleAction.setDialogName("ui-security-app:roleAdd");

        EditDialogActionDefinition editRoleAction = new EditDialogActionDefinition();
        editRoleAction.setName("editRole");
        editRoleAction.setLabel("Edit role");
        editRoleAction.setIcon("icon-edit");
        editRoleAction.setDialogName("ui-security-app:roleEdit");

        // Configure ImageProvider
        ConfiguredImageProviderDefinition cipd = new ConfiguredImageProviderDefinition();
        cipd.setOriginalImageNodeName("photo");
        cipd.setImageProviderClass(DefaultImageProvider.class);

        app.label("Security").icon("icon-security-app").appClass(SecurityApp.class) // .categoryName("MANAGE")
                .subApps(
                        userSubApp(app, cfg, contentAppConfig, "users", "/admin").label("Users").exec(),
                        userSubApp(app, cfg, contentAppConfig, "systemUsers", "/system").label("System users").exec(),
                        app.browserSubApp("groups").subAppClass(SecurityGroupsSubApp.class).label("Groups")
                                .actions(addGroupAction, editGroupAction, deleteGroupActionDefinition)
                                .imageProvider(cipd)
                                .workbench(contentAppConfig.workbench.workbench().workspace("usergroups").path("/").defaultOrder(ModelConstants.JCR_NAME)
                                        .nodeTypes(
                                                contentAppConfig.workbench.nodeType(NodeTypes.Group.NAME).icon("icon-user-group"),
                                                contentAppConfig.workbench.nodeType(NodeTypes.Folder.NAME).icon("icon-folder"))
                                        .columns(
                                                cfg.columns.property(ModelConstants.JCR_NAME, "Group name").sortable(true).expandRatio(2),
                                                cfg.columns.property("title", "Full group name").sortable(true).displayInDialog(false).expandRatio(2),
                                                cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(46),
                                                cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification date").sortable(true).propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160)
                                        )
                                )
                                .actionbar(cfg.actionbars.actionbar().defaultAction(editGroupAction.getName())
                                        .sections(
                                                cfg.actionbars.section("groupActions").label("Groups")
                                                        .groups(
                                                                cfg.actionbars.group("addActions").actions(addGroupAction.getName()),
                                                                cfg.actionbars.group("editActions").actions(editGroupAction.getName(), deleteGroupActionDefinition.getName())
                                                        )
                                        )
                                ).exec(),
                        app.browserSubApp("roles").subAppClass(SecurityRolesSubApp.class).label("Roles")
                                .actions(addRoleAction, editRoleAction, deleteRoleActionDefinition)
                                .imageProvider(cipd)
                                .workbench(contentAppConfig.workbench.workbench().workspace("userroles").path("/").defaultOrder(ModelConstants.JCR_NAME)
                                        .nodeTypes(
                                                contentAppConfig.workbench.nodeType(NodeTypes.Role.NAME).icon("icon-user-role"),
                                                contentAppConfig.workbench.nodeType(NodeTypes.Folder.NAME).icon("icon-folder"))
                                        .columns(
                                                cfg.columns.property(ModelConstants.JCR_NAME, "Role name").sortable(true).expandRatio(2),
                                                cfg.columns.property("title", "Full role name").sortable(true).displayInDialog(false).expandRatio(2),
                                                cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(46),
                                                cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification date").sortable(true).propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160)
                                        )

                                )
                                .actionbar(cfg.actionbars.actionbar().defaultAction(editRoleAction.getName())
                                        .sections(
                                                cfg.actionbars.section("roleActions").label("Roles")
                                                        .groups(
                                                                cfg.actionbars.group("addActions").actions(addRoleAction.getName()),
                                                                cfg.actionbars.group("editActions").actions(editRoleAction.getName(), deleteRoleActionDefinition.getName())
                                                        )
                                        )
                                ).exec()

                );
    }

    protected BrowserSubAppBuilder userSubApp(ContentAppBuilder app, UiConfig cfg, ContentAppConfig contentAppConfig,String name, String root) {
        // user
        CreateDialogActionDefinition addUserAction = new CreateDialogActionDefinition();
        addUserAction.setName("addUser");
        addUserAction.setLabel("New user");
        addUserAction.setIcon("icon-add-item");
        addUserAction.setNodeType(NodeTypes.User.NAME);
        addUserAction.setDialogName("ui-security-app:userAdd");

        EditDialogActionDefinition editUserAction = new EditDialogActionDefinition();
        editUserAction.setName("editUser");
        editUserAction.setLabel("Edit user");
        editUserAction.setIcon("icon-edit");
        editUserAction.setDialogName("ui-security-app:userEdit");

        DeleteItemActionDefinition deleteUserActionDefinition = new DeleteItemActionDefinition();
        deleteUserActionDefinition.setName("deleteUser");
        deleteUserActionDefinition.setLabel("Delete user");
        deleteUserActionDefinition.setIcon("icon-delete");

        // Configure ImageProvider
        ConfiguredImageProviderDefinition cipd = new ConfiguredImageProviderDefinition();
        cipd.setOriginalImageNodeName("photo");
        cipd.setImageProviderClass(DefaultImageProvider.class);

        return app.browserSubApp(name).subAppClass(SecurityUsersSubApp.class)
                .actions(addUserAction, editUserAction, deleteUserActionDefinition)
                .imageProvider(cipd)
                .workbench(contentAppConfig.workbench.workbench().workspace("users").path(root).defaultOrder(ModelConstants.JCR_NAME)
                        .nodeTypes(
                                contentAppConfig.workbench.nodeType(NodeTypes.User.NAME).icon("icon-user-magnolia"),
                                contentAppConfig.workbench.nodeType(NodeTypes.Folder.NAME).icon("icon-folder")) // see MGNLPUR-77
                        .columns(
                                cfg.columns.column(new UserNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName(ModelConstants.JCR_NAME).formatterClass(UserNameColumnFormatter.class).expandRatio(2),
                                cfg.columns.property("title", "Full name").sortable(true).expandRatio(2),
                                cfg.columns.property("email", "Email").sortable(true).sortable(true).displayInDialog(false).expandRatio(1),
                                cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(46),
                                cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification date").sortable(true).propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160)
                        )
                )
                .actionbar(cfg.actionbars.actionbar().defaultAction(editUserAction.getName())
                        .sections(
                                cfg.actionbars.section("usersActions").label("Users")
                                        .groups(
                                                cfg.actionbars.group("addActions").actions(addUserAction.getName()),
                                                cfg.actionbars.group("editActions").actions(editUserAction.getName(), deleteUserActionDefinition.getName())
                                        )
                        )
                );
    }

    @Dialog("ui-security-app:userAdd")
    public void userAddDialog(DialogBuilder dialog, UiConfig cfg) {
        userDialog(dialog, cfg, false);
    }

    @Dialog("ui-security-app:userEdit")
    public void userEditDialog(DialogBuilder dialog, UiConfig cfg) {
        userDialog(dialog, cfg, true);
    }

    @Dialog("ui-security-app:user")
    public void userDialog(DialogBuilder dialog, UiConfig cfg, boolean editMode) {

        AbstractFieldBuilder username = cfg.fields.text(ModelConstants.JCR_NAME)
                .label("User name")
                .description("Define user name")
                .required(!editMode)
                .readOnly(editMode);
        if (!editMode) {
            username.validator(cfg.validators.custom(new UniqueUserIdValidatorDefinition()).errorMessage("User name already exists."));
        }

        GroupManagementFieldBuilder groups = new GroupManagementFieldBuilder("groups");
        groups.label("Assign user to groups");
        groups.leftColumnCaption("Other available groups");
        groups.rightColumnCaption("User is member of");

        RoleManagementFieldBuilder roles = new RoleManagementFieldBuilder("roles");
        roles.label("Grant additional roles to user");
        roles.leftColumnCaption("Other available roles");
        roles.rightColumnCaption("Granted roles");

        SaveDialogActionDefinition commit = new SaveDialogActionDefinition();
        commit.setImplementationClass(SaveUserDialogAction.class);
        commit.setName("commit");
        commit.setLabel("save changes");
        dialog.addAction(commit);

        CancelDialogActionDefinition cancel = new CancelDialogActionDefinition();
        cancel.setName("cancel");
        cancel.setLabel("cancel");
        dialog.addAction(cancel);

        dialog.form(cfg.forms.form().description("Define the user information").label("User")
                .tabs(
                        cfg.forms.tab("User").label("User info")
                                .fields(
                                        username,
                                        cfg.fields.password("pswd").label("Password").verification().encode(false), // we handle encoding in the save action
                                        (new EnabledFieldBuilder("enabled")).label("Enabled"),
                                        cfg.fields.text("title").label("Full name"),
                                        cfg.fields.text("email").label("E-mail").description("Please enter user's e-mail address."),
                                        cfg.fields.select("language").label("Language")
                                                .options(
                                                        (new OptionBuilder()).value("en").label("English").selected(),
                                                        (new OptionBuilder()).value("de").label("German"),
                                                        (new OptionBuilder()).value("cz").label("Czech"),
                                                        (new OptionBuilder()).value("fr").label("French")
                                                )
                                ),
                        cfg.forms.tab("Groups").label("Groups")
                                .fields(
                                        groups
                                ),
                        cfg.forms.tab("Roles").label("Roles")
                                .fields(
                                        roles
                                )
                )
        );
    }

    @Dialog("ui-security-app:groupAdd")
    public void groupAddDialog(DialogBuilder dialog, UiConfig cfg) {
        groupDialog(dialog, cfg, false);
    }

    @Dialog("ui-security-app:groupEdit")
    public void groupEditDialog(DialogBuilder dialog, UiConfig cfg) {
        groupDialog(dialog, cfg, true);
    }

    public void groupDialog(DialogBuilder dialog, UiConfig cfg, boolean editMode) {


        AbstractFieldBuilder groupName = cfg.fields.text(ModelConstants.JCR_NAME)
                .label("Group name")
                .description("Define group name")
                .required(!editMode)
                .readOnly(editMode);
        if (!editMode) {
            groupName.validator(cfg.validators.custom(new UniqueGroupIdValidatorDefinition()).errorMessage("Group name already exists."));
        }

        GroupManagementFieldBuilder groups = new GroupManagementFieldBuilder("groups");
        groups.label("Assign groups");
        groups.leftColumnCaption("Other available groups");
        groups.rightColumnCaption("Assigned group");

        RoleManagementFieldBuilder roles = new RoleManagementFieldBuilder("roles");
        roles.label("Grant additional roles");
        roles.leftColumnCaption("Other available roles");
        roles.rightColumnCaption("Granted roles");

        SaveDialogActionDefinition commit = new SaveDialogActionDefinition();
        commit.setImplementationClass(SaveGroupDialogAction.class);
        commit.setName("commit");
        commit.setLabel("save changes");
        dialog.addAction(commit);

        CancelDialogActionDefinition cancel = new CancelDialogActionDefinition();
        cancel.setName("cancel");
        cancel.setLabel("cancel");
        dialog.addAction(cancel);

        dialog.form(cfg.forms.form().description("Define the group information").label("Group")
                .tabs(
                        cfg.forms.tab("Group").label("Group info")
                                .fields(
                                        groupName,
                                        cfg.fields.text("title").label("Full name").description("Full name of the group"),
                                        cfg.fields.text("description").label("Description").description("Detail description of the group")
                                ),
                        cfg.forms.tab("Groups").label("Groups")
                                .fields(
                                        groups
                                ),
                        cfg.forms.tab("Roles").label("Roles")
                                .fields(
                                        roles
                                )
                )
        );
    }

    @Dialog("ui-security-app:roleEdit")
    public void roleEditDialog(DialogBuilder dialog, UiConfig cfg) {
        roleDialog(dialog, cfg, true);
    }

    @Dialog("ui-security-app:roleAdd")
    public void roleAddDialog(DialogBuilder dialog, UiConfig cfg) {
        roleDialog(dialog, cfg, false);
    }

    public void roleDialog(DialogBuilder dialog, UiConfig cfg, boolean editMode) {

        AbstractFieldBuilder roleName = cfg.fields.text(ModelConstants.JCR_NAME)
                .label("Role name")
                .description("Define unique role name")
                .required(!editMode)
                .readOnly(editMode);
        if (!editMode) {
            roleName.validator(cfg.validators.custom(new UniqueRoleIdValidatorDefinition()).errorMessage("Role name already exists."));
        }

        SaveDialogActionDefinition commit = new SaveDialogActionDefinition();
        commit.setImplementationClass(SaveRoleDialogAction.class);
        commit.setName("commit");
        commit.setLabel("save changes");
        dialog.addAction(commit);

        CancelDialogActionDefinition cancel = new CancelDialogActionDefinition();
        cancel.setName("cancel");
        cancel.setLabel("cancel");
        dialog.addAction(cancel);

        dialog.form(cfg.forms.form().description("Define the role information").label("Role")
                .tabs(
                        cfg.forms.tab("Role").label("Role info")
                                .fields(
                                        roleName,
                                        cfg.fields.text("title").label("Full name").description("Full name of the role"),
                                        cfg.fields.text("description").label("Role Description").description("Description of the role")
                                ),
                        cfg.forms.tab("ACLs").label("Access control lists")
                                .fields(
                                        cfg.fields.staticField("placeholder").label("Placeholder for ACL control")
                                )
                )
        );
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        CodeConfigurationUtils.registerAnnotatedAppProviders(appDescriptorRegistry, this);
        CodeConfigurationUtils.registerAnnotatedDialogProviders(dialogDefinitionRegistry, this);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        // nothing to do yet
    }

}
