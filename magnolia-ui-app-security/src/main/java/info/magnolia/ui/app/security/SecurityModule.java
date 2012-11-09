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

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.app.CodeConfigurationUtils;
import info.magnolia.ui.admincentral.app.content.builder.ContentAppBuilder;
import info.magnolia.ui.admincentral.column.StatusColumnFormatter;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.security.column.UserNameColumnDefinition;
import info.magnolia.ui.app.security.column.UserNameColumnFormatter;
import info.magnolia.ui.framework.app.builder.App;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.model.actionbar.builder.ActionbarConfig;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.dialog.builder.Dialog;
import info.magnolia.ui.model.dialog.builder.DialogBuilder;
import info.magnolia.ui.model.dialog.builder.DialogConfig;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.model.workbench.builder.WorkbenchConfig;

/**
 * Module class for the Security App.
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

        // user
        CreateDialogActionDefinition addUserAction = new CreateDialogActionDefinition();
        addUserAction.setNodeType(MgnlNodeType.USER);
        addUserAction.setDialogName("ui-security-app:user");

        EditDialogActionDefinition editUserAction = new EditDialogActionDefinition();
        editUserAction.setDialogName("ui-security-app:user");

        // group
        CreateDialogActionDefinition addGroupAction = new CreateDialogActionDefinition();
        addGroupAction.setNodeType(MgnlNodeType.GROUP);
        addGroupAction.setDialogName("ui-security-app:group");

        EditDialogActionDefinition editGroupAction = new EditDialogActionDefinition();
        editGroupAction.setDialogName("ui-security-app:group");

        // role
        CreateDialogActionDefinition addRoleAction = new CreateDialogActionDefinition();
        addRoleAction.setNodeType(MgnlNodeType.ROLE);
        addRoleAction.setDialogName("ui-security-app:role");

        EditDialogActionDefinition editRoleAction = new EditDialogActionDefinition();
        editRoleAction.setDialogName("ui-security-app:role");


        app.label("security").icon("icon-security-app").appClass(SecurityApp.class).categoryName("MANAGE")
            .subApps(
                    app.subApp("users").subAppClass(SecurityUsersSubApp.class)
                    .workbench(wbcfg.workbench().workspace("users").root("/").defaultOrder("jcrName")
                            .groupingItemType(wbcfg.itemType(MgnlNodeType.NT_CONTENT).icon("/.resources/icons/16/folders.gif"))
                            .mainItemType(wbcfg.itemType(MgnlNodeType.USER).icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                            .columns(
                                    wbcfg.column(new UserNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName").formatterClass(UserNameColumnFormatter.class),
                                    wbcfg.column(new PropertyColumnDefinition()).name("email").label("Email").sortable(true).width(180).displayInDialog(false),
                                    wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50),
                                    wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName("MetaData/mgnl:lastmodified").displayInDialog(false).width(200).sortable(true)
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
                    ),
                    app.subApp("groups").subAppClass(SecurityGroupsSubApp.class)
                    .workbench(wbcfg.workbench().workspace("usergroups").root("/").defaultOrder("jcrName")
                            // .groupingItemType(wbcfg.itemType(MgnlNodeType.NT_CONTENT).icon("/.resources/icons/16/folders.gif"))
                            .mainItemType(wbcfg.itemType(MgnlNodeType.GROUP).icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                            .columns(
                                    wbcfg.column(new PropertyColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName"),
                                    wbcfg.column(new PropertyColumnDefinition()).name("title").label("Full Name").sortable(true).propertyName("title").width(180).displayInDialog(false),
                                    wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50),
                                    wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName("MetaData/mgnl:lastmodified").displayInDialog(false).width(200).sortable(true)
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
                    app.subApp("roles").subAppClass(SecurityRolesSubApp.class).defaultSubApp()
                    .workbench(wbcfg.workbench().workspace("userroles").root("/").defaultOrder("jcrName")
                            // .groupingItemType(wbcfg.itemType(MgnlNodeType.NT_CONTENT).icon("/.resources/icons/16/folders.gif"))
                            .mainItemType(wbcfg.itemType(MgnlNodeType.ROLE).icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                            .columns(
                                    wbcfg.column(new PropertyColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName"),
                                    wbcfg.column(new PropertyColumnDefinition()).name("title").label("Full Name").sortable(true).propertyName("title").width(180).displayInDialog(false),
                                    wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50),
                                    wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName("MetaData/mgnl:lastmodified").displayInDialog(false).width(200).sortable(true)
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

    @Dialog("ui-security-app:user")
    public void userDialog(DialogBuilder dialog, DialogConfig cfg) {
        dialog.description("Define the user information")
        .tabs(
                cfg.tab("User").label("User Tab")
                        .fields(
                                cfg.fields.textField("jcrName").label("User name").description("Define Username").required(),
                                cfg.fields.textField("email").label("E-mail").description("Please enter user's e-mail address. Field is mandatory")
                        )
        )
        .actions(
                // do not allow to save the data yet
                // cfg.action("commit").label("save changes").action(new SaveContactDialogActionDefinition()),
                cfg.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
        );
    }

    @Dialog("ui-security-app:group")
    public void groupDialog(DialogBuilder dialog, DialogConfig cfg) {
        dialog.description("Define the group information")
        .tabs(
                cfg.tab("Group").label("Group Tab")
                        .fields(
                                cfg.fields.textField("jcrName").label("Group name").description("Define Groupname").required(),
                                cfg.fields.textField("title").label("Group Description").description("Description of the group")
                        )
        )
        .actions(
                // do not allow to save the data yet
                // cfg.action("commit").label("save changes").action(new SaveContactDialogActionDefinition()),
                cfg.action("cancel").label("cancel").action(new CancelDialogActionDefinition())
        );
    }

    @Dialog("ui-security-app:role")
    public void roleDialog(DialogBuilder dialog, DialogConfig cfg) {
        dialog.description("Define the role information")
        .tabs(
                cfg.tab("Role").label("Role Tab")
                        .fields(
                                cfg.fields.textField("jcrName").label("Role name").description("Define Rolename").required(),
                                cfg.fields.textField("title").label("Role Description").description("Description of the role")
                        )
        )
        .actions(
                // do not allow to save the data yet
                // cfg.action("commit").label("save changes").action(new SaveContactDialogActionDefinition()),
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
    }

}
