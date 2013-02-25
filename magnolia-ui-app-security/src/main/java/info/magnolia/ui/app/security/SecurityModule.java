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
import info.magnolia.ui.admincentral.app.CodeConfigurationUtils;
import info.magnolia.ui.admincentral.app.content.builder.ContentAppBuilder;
import info.magnolia.ui.admincentral.app.content.builder.ContentSubAppBuilder;
import info.magnolia.ui.admincentral.column.DateColumnFormatter;
import info.magnolia.ui.admincentral.column.StatusColumnFormatter;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.image.DefaultImageProvider;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.security.column.UserNameColumnDefinition;
import info.magnolia.ui.app.security.column.UserNameColumnFormatter;
import info.magnolia.ui.app.security.dialog.action.SaveGroupDialogActionDefinition;
import info.magnolia.ui.app.security.dialog.action.SaveRoleDialogActionDefinition;
import info.magnolia.ui.app.security.dialog.action.SaveUserDialogActionDefinition;
import info.magnolia.ui.app.security.dialog.field.EnabledFieldBuilder;
import info.magnolia.ui.app.security.dialog.field.GroupManagementFieldBuilder;
import info.magnolia.ui.app.security.dialog.field.RoleManagementFieldBuilder;
import info.magnolia.ui.app.security.dialog.field.validator.UniqueGroupIdValidatorDefinition;
import info.magnolia.ui.app.security.dialog.field.validator.UniqueRoleIdValidatorDefinition;
import info.magnolia.ui.app.security.dialog.field.validator.UniqueUserIdValidatorDefinition;
import info.magnolia.ui.framework.app.builder.App;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.framework.config.UiConfig;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.dialog.builder.Dialog;
import info.magnolia.ui.model.dialog.builder.DialogBuilder;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.model.form.builder.AbstractFieldBuilder;
import info.magnolia.ui.model.form.builder.OptionBuilder;
import info.magnolia.ui.model.imageprovider.definition.ConfiguredImageProviderDefinition;

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
    public void securityApp(ContentAppBuilder app, UiConfig cfg) {

        // group
        CreateDialogActionDefinition addGroupAction = new CreateDialogActionDefinition();
        addGroupAction.setNodeType(NodeTypes.Group.NAME);
        addGroupAction.setDialogName("ui-security-app:groupAdd");

        EditDialogActionDefinition editGroupAction = new EditDialogActionDefinition();
        editGroupAction.setDialogName("ui-security-app:groupEdit");

        // role
        CreateDialogActionDefinition addRoleAction = new CreateDialogActionDefinition();
        addRoleAction.setNodeType(NodeTypes.Role.NAME);
        addRoleAction.setDialogName("ui-security-app:roleAdd");

        EditDialogActionDefinition editRoleAction = new EditDialogActionDefinition();
        editRoleAction.setDialogName("ui-security-app:roleEdit");

        // Configure ImageProvider
        ConfiguredImageProviderDefinition cipd = new ConfiguredImageProviderDefinition();
        cipd.setOriginalImageNodeName("photo");
        cipd.setImageProviderClass(DefaultImageProvider.class);

        app.label("Security").icon("icon-security-app").appClass(SecurityApp.class) // .categoryName("MANAGE")
                .subApps(
                        userSubApp(app, cfg, "users", "/admin").defaultSubApp().label("Users"),
                        userSubApp(app, cfg, "systemUsers", "/system").label("System users"),
                        app.subApp("groups").subAppClass(SecurityGroupsSubApp.class).label("Groups")
                                .workbench(cfg.workbenches.workbench().workspace("usergroups").root("/").defaultOrder(ModelConstants.JCR_NAME)
                                        .nodeType(cfg.workbenches.nodeType(NodeTypes.Group.NAME).icon("icon-user-group"))
                                        .nodeType(cfg.workbenches.nodeType(NodeTypes.Folder.NAME).icon("icon-folder"))
                                        .imageProvider(cipd)
                                        .columns(
                                                cfg.columns.property(ModelConstants.JCR_NAME, "Group name").sortable(true).expandRatio(2),
                                                cfg.columns.property("title", "Full group name").sortable(true).displayInDialog(false).expandRatio(2),
                                                cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(46),
                                                cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification date").sortable(true).propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160)
                                        )
                                        .actionbar(cfg.actionbars.actionbar().defaultAction("edit")
                                                .sections(
                                                        cfg.actionbars.section("groupActions").label("Groups")
                                                                .groups(
                                                                        cfg.actionbars.group("addActions").items(
                                                                                cfg.actionbars.item("addGroup").label("New group").icon("icon-add-item").action(addGroupAction)),
                                                                        cfg.actionbars.group("editActions").items(
                                                                                cfg.actionbars.item("edit").label("Edit group").icon("icon-edit").action(editGroupAction),
                                                                                cfg.actionbars.item("delete").label("Delete group").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                                                )
                                                )
                                        )
                                ),
                        app.subApp("roles").subAppClass(SecurityRolesSubApp.class).label("Roles")
                                .workbench(cfg.workbenches.workbench().workspace("userroles").root("/").defaultOrder(ModelConstants.JCR_NAME)
                                        .nodeType(cfg.workbenches.nodeType(NodeTypes.Role.NAME).icon("icon-user-role"))
                                        .nodeType(cfg.workbenches.nodeType(NodeTypes.Folder.NAME).icon("icon-folder"))
                                        .imageProvider(cipd)
                                        .columns(
                                                cfg.columns.property(ModelConstants.JCR_NAME, "Role name").sortable(true).expandRatio(2),
                                                cfg.columns.property("title", "Full role name").sortable(true).displayInDialog(false).expandRatio(2),
                                                cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(46),
                                                cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification date").sortable(true).propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160)
                                        )
                                        .actionbar(cfg.actionbars.actionbar().defaultAction("edit")
                                                .sections(
                                                        cfg.actionbars.section("roleActions").label("Roles")
                                                                .groups(
                                                                        cfg.actionbars.group("addActions").items(
                                                                                cfg.actionbars.item("addRole").label("New role").icon("icon-add-item").action(addRoleAction)),
                                                                        cfg.actionbars.group("editActions").items(
                                                                                cfg.actionbars.item("edit").label("Edit role").icon("icon-edit").action(editRoleAction),
                                                                                cfg.actionbars.item("delete").label("Delete role").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                                                )
                                                )
                                        )
                                )

                );
    }

    protected ContentSubAppBuilder userSubApp(ContentAppBuilder app, UiConfig cfg, String name, String root) {
        // user
        CreateDialogActionDefinition addUserAction = new CreateDialogActionDefinition();
        addUserAction.setNodeType(NodeTypes.User.NAME);
        addUserAction.setDialogName("ui-security-app:userAdd");

        EditDialogActionDefinition editUserAction = new EditDialogActionDefinition();
        editUserAction.setDialogName("ui-security-app:userEdit");

        // Configure ImageProvider
        ConfiguredImageProviderDefinition cipd = new ConfiguredImageProviderDefinition();
        cipd.setOriginalImageNodeName("photo");
        cipd.setImageProviderClass(DefaultImageProvider.class);

        return app.subApp(name).subAppClass(SecurityUsersSubApp.class)
                .workbench(cfg.workbenches.workbench().workspace("users").root(root).defaultOrder(ModelConstants.JCR_NAME)
                        .nodeType(cfg.workbenches.nodeType(NodeTypes.User.NAME).icon("icon-user-magnolia"))
                        .nodeType(cfg.workbenches.nodeType(NodeTypes.Folder.NAME).icon("icon-folder")) // see MGNLPUR-77
                        .imageProvider(cipd)
                        .columns(
                                cfg.columns.column(new UserNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName(ModelConstants.JCR_NAME).formatterClass(UserNameColumnFormatter.class).expandRatio(2),
                                cfg.columns.property("title", "Full name").sortable(true).expandRatio(2),
                                cfg.columns.property("email", "Email").sortable(true).sortable(true).displayInDialog(false).expandRatio(1),
                                cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(46),
                                cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification date").sortable(true).propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160)
                        )
                        .actionbar(cfg.actionbars.actionbar().defaultAction("edit")
                                .sections(
                                        cfg.actionbars.section("usersActions").label("Users")
                                                .groups(
                                                        cfg.actionbars.group("addActions").items(
                                                                cfg.actionbars.item("addUser").label("New user").icon("icon-add-item").action(addUserAction)),
                                                        cfg.actionbars.group("editActions").items(
                                                                cfg.actionbars.item("edit").label("Edit user").icon("icon-edit").action(editUserAction),
                                                                cfg.actionbars.item("delete").label("Delete user").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                                )
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

        dialog.form(cfg.forms.form().description("Define the user information")
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
        )
                .actions(
                        cfg.dialogs.action("commit").label("save changes").action(new SaveUserDialogActionDefinition()),
                        cfg.dialogs.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
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

        dialog.form(cfg.forms.form().description("Define the group information")
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
        )
                .actions(
                        cfg.dialogs.action("commit").label("save changes").action(new SaveGroupDialogActionDefinition()),
                        cfg.dialogs.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
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

        dialog.form(cfg.forms.form().description("Define the role information")
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
        )
                .actions(
                        cfg.dialogs.action("commit").label("save changes").action(new SaveRoleDialogActionDefinition()),
                        cfg.dialogs.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
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
