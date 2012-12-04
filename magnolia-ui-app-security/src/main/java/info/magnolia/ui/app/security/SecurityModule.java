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

import javax.inject.Inject;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.app.CodeConfigurationUtils;
import info.magnolia.ui.admincentral.app.content.builder.ContentAppBuilder;
import info.magnolia.ui.admincentral.app.content.builder.ContentSubAppBuilder;
import info.magnolia.ui.admincentral.column.StatusColumnFormatter;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
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
import info.magnolia.ui.model.actionbar.builder.ActionbarConfig;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.form.builder.AbstractFieldBuilder;
import info.magnolia.ui.model.form.builder.FormConfig;
import info.magnolia.ui.model.dialog.builder.Dialog;
import info.magnolia.ui.model.dialog.builder.DialogBuilder;
import info.magnolia.ui.model.dialog.builder.DialogConfig;
import info.magnolia.ui.model.form.builder.OptionBuilder;
import info.magnolia.ui.model.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.admincentral.image.DefaultImageProvider;
import info.magnolia.ui.model.workbench.builder.WorkbenchConfig;

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
    public void securityApp(ContentAppBuilder app, WorkbenchConfig wbcfg, ActionbarConfig abcfg) {


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
                    userSubApp(app, wbcfg, abcfg, "users", "/admin").defaultSubApp(),
                    userSubApp(app, wbcfg, abcfg, "systemUsers", "/system"),
                    app.subApp("groups").subAppClass(SecurityGroupsSubApp.class)
                    .workbench(wbcfg.workbench().workspace("usergroups").root("/").defaultOrder("jcrName")
                            .groupingItemType(wbcfg.itemType(NodeTypes.Folder.NAME).icon("/.resources/icons/16/folders.gif"))
                            .mainItemType(wbcfg.itemType(NodeTypes.Group.NAME).icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                            .imageProvider(cipd)
                            .columns(
                                    wbcfg.column(new PropertyColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName"),
                                    wbcfg.column(new PropertyColumnDefinition()).name("title").label("Full Name").sortable(true).propertyName("title").width(180).displayInDialog(false),
                                    wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50),
                                    wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).width(200).sortable(true)
                            )
                            .actionbar(abcfg.actionbar().defaultAction("edit")
                                    .sections(
                                            abcfg.section("groupActions").label("Groups")
                                                    .groups(
                                                            abcfg.group("addActions").items(
                                                                    abcfg.item("addGroup").label("New group").icon("icon-add-item").action(addGroupAction)),
                                                            abcfg.group("editActions").items(
                                                                    abcfg.item("edit").label("Edit group").icon("icon-edit").action(editGroupAction),
                                                                    abcfg.item("delete").label("Delete group").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                            )
                                    )
                            )
                    ),
                    app.subApp("roles").subAppClass(SecurityRolesSubApp.class)
                    .workbench(wbcfg.workbench().workspace("userroles").root("/").defaultOrder("jcrName")
                            .groupingItemType(wbcfg.itemType(NodeTypes.Folder.NAME).icon("/.resources/icons/16/folders.gif"))
                            .mainItemType(wbcfg.itemType(NodeTypes.Role.NAME).icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                            .imageProvider(cipd)
                            .columns(
                                    wbcfg.column(new PropertyColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName"),
                                    wbcfg.column(new PropertyColumnDefinition()).name("title").label("Full Name").sortable(true).propertyName("title").width(180).displayInDialog(false),
                                    wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50),
                                    wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).width(200).sortable(true)
                            )
                            .actionbar(abcfg.actionbar().defaultAction("edit")
                                    .sections(
                                            abcfg.section("roleActions").label("Roles")
                                                    .groups(
                                                            abcfg.group("addActions").items(
                                                                    abcfg.item("addRole").label("New role").icon("icon-add-item").action(addRoleAction)),
                                                            abcfg.group("editActions").items(
                                                                    abcfg.item("edit").label("Edit role").icon("icon-edit").action(editRoleAction),
                                                                    abcfg.item("delete").label("Delete role").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                            )
                                    )
                            )
                     )

            )
        ;
    }

    protected ContentSubAppBuilder userSubApp(ContentAppBuilder app, WorkbenchConfig wbcfg, ActionbarConfig abcfg, String name, String root) {
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
                .workbench(wbcfg.workbench().workspace("users").root(root).defaultOrder("jcrName")
                        .groupingItemType(wbcfg.itemType(NodeTypes.Folder.NAME).icon("/.resources/icons/16/folders.gif"))  // see MGNLPUR-77
                        .mainItemType(wbcfg.itemType(NodeTypes.User.NAME).icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                        .imageProvider(cipd)
                        .columns(
                                wbcfg.column(new UserNameColumnDefinition()).name("name").label("Name").sortable(true).width(200).propertyName("jcrName").formatterClass(UserNameColumnFormatter.class),
                                wbcfg.column(new PropertyColumnDefinition()).name("title").label("Full name").sortable(true).expandRatio(2.0f),
                                wbcfg.column(new PropertyColumnDefinition()).name("email").label("Email").sortable(true).width(350).displayInDialog(false),
                                wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(60),
                                wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).width(250).sortable(true)
                        )
                        .actionbar(abcfg.actionbar().defaultAction("edit")
                                .sections(
                                        abcfg.section("usersActions").label("Users")
                                                .groups(
                                                        abcfg.group("addActions").items(
                                                                abcfg.item("addUser").label("New user").icon("icon-add-item").action(addUserAction)),
                                                        abcfg.group("editActions").items(
                                                                abcfg.item("edit").label("Edit user").icon("icon-edit").action(editUserAction),
                                                                abcfg.item("delete").label("Delete user").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                        )
                                )
                        )
                );
    }

    @Dialog("ui-security-app:userAdd")
    public void userAddDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg) {
        userDialog(dialog,cfg,formcfg,false);
    }

    @Dialog("ui-security-app:userEdit")
    public void userEditDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg) {
        userDialog(dialog,cfg,formcfg,true);
    }

    @Dialog("ui-security-app:user")
    public void userDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg, boolean editMode) {

        UniqueUserIdValidatorDefinition uniqueUserid = new UniqueUserIdValidatorDefinition();
        uniqueUserid.setErrorMessage("User name already exists.");

        AbstractFieldBuilder username = cfg.fields.textField("jcrName")
                                           .label("User name")
                                           .description("Define Username")
                                           .required(!editMode)
                                           .readOnly(editMode);
        if (!editMode) {
            username.validator(uniqueUserid);
        }

        GroupManagementFieldBuilder groups = new GroupManagementFieldBuilder("groups");
        groups.label("");
        groups.leftColumnCaption("Available groups");
        groups.rightColumnCaption("Assigned groups");

        RoleManagementFieldBuilder roles = new RoleManagementFieldBuilder("roles");
        roles.label("");
        roles.leftColumnCaption("Available roles");
        roles.rightColumnCaption("Assigned roles");

        dialog.form(formcfg.form().description("Define the user information")
                        .tabs(
                                formcfg.tab("User").label("User Info")
                                        .fields(
                                                username,
                                                formcfg.fields.passwordField("pswd").label("Password").verification().encode(false), //we handle encoding in the save action
                                                (new EnabledFieldBuilder("enabled")).label("Enabled"),
                                                formcfg.fields.textField("title").label("Full name"),
                                                formcfg.fields.textField("email").label("E-mail").description("Please enter user's e-mail address."),
                                                formcfg.fields.selectField("language").label("Language")
                                                    .options(
                                                        (new OptionBuilder()).value("en").label("English").selected(),
                                                        (new OptionBuilder()).value("de").label("German"),
                                                        (new OptionBuilder()).value("cz").label("Czech"),
                                                        (new OptionBuilder()).value("fr").label("French")
                                                    )
                                               ),
                                formcfg.tab("Groups").label("Groups")
                                        .fields(
                                                groups
                                               ),
                                formcfg.tab("Roles").label("Roles")
                                        .fields(
                                                roles
                                               )

                             )
                     )
                     .actions(
                             cfg.action("commit").label("save changes").action(new SaveUserDialogActionDefinition()),
                             cfg.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
                             );
    }

    @Dialog("ui-security-app:groupAdd")
    public void groupAddDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg) {
        groupDialog(dialog, cfg, formcfg, false);
    }

    @Dialog("ui-security-app:groupEdit")
    public void groupEditDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg) {
        groupDialog(dialog, cfg, formcfg, true);
    }

    public void groupDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg, boolean editMode) {

        UniqueGroupIdValidatorDefinition uniqueGroupId = new UniqueGroupIdValidatorDefinition();
        uniqueGroupId.setErrorMessage("Group name already exists.");

        AbstractFieldBuilder groupName = cfg.fields.textField("jcrName")
                                            .label("Group name")
                                            .description("Define Groupname")
                                            .required(!editMode)
                                            .readOnly(editMode);
        if (!editMode) {
            groupName.validator(uniqueGroupId);
        }

        GroupManagementFieldBuilder groups = new GroupManagementFieldBuilder("groups");
        groups.label("Assigned groups");

        RoleManagementFieldBuilder roles = new RoleManagementFieldBuilder("roles");
        roles.label("Assigned roles");

        dialog.description("Define the group information")
                .form(formcfg.form().description("Define the group information")
                        .tabs(
                                formcfg.tab("Group").label("Group Info")
                                    .fields(
                                            groupName,
                                            formcfg.fields.textField("title").label("Full Name").description("Full name of the group"),
                                            formcfg.fields.textField("description").label("Description").description("Detail description of the group")
                                           ),
                                formcfg.tab("Groups").label("Groups")
                                    .fields(
                                            groups
                                           ),
                                formcfg.tab("Roles").label("Roles")
                                    .fields(
                                            roles
                                           )
                             )
                        )
                        .actions(
                                 cfg.action("commit").label("save changes").action(new SaveGroupDialogActionDefinition()),
                                 cfg.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
                                );
    }

    @Dialog("ui-security-app:roleEdit")
    public void roleEditDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg) {
        roleDialog(dialog,cfg,formcfg,true);
    }


    @Dialog("ui-security-app:roleAdd")
    public void roleAddDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg) {
        roleDialog(dialog,cfg,formcfg,false);
    }

    public void roleDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg, boolean editMode) {

        UniqueRoleIdValidatorDefinition uniqueRoleId = new UniqueRoleIdValidatorDefinition();
        uniqueRoleId.setErrorMessage("Role name already exists.");

        AbstractFieldBuilder rolename = cfg.fields.textField("jcrName")
                                           .label("Role name")
                                           .description("Define unique role name")
                                           .required(!editMode)
                                           .readOnly(editMode);
        if (!editMode) {
            rolename.validator(uniqueRoleId);
        }

        dialog.description("Define the role information")
                .form(formcfg.form().description("Define the group information")
                        .tabs(
                              formcfg.tab("Role").label("Role Info")
                                  .fields(
                                          rolename,
                                          formcfg.fields.textField("title").label("Full name").description("Full name of the role"),
                                          formcfg.fields.textField("description").label("Role Description").description("Description of the role")
                                         ),
                              formcfg.tab("ACLs").label("Access Control Lists")
                                  .fields(
                                          formcfg.fields.staticField("placeholder").label("Placeholder for ACL control")
                                         )
                             )
                        )
                        .actions(
                                 cfg.action("commit").label("save changes").action(new SaveRoleDialogActionDefinition()),
                                 cfg.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
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
