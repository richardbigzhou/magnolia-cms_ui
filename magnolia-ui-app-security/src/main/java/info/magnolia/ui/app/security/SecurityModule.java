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
import info.magnolia.ui.app.security.column.UserNameColumnDefinition;
import info.magnolia.ui.app.security.column.UserNameColumnFormatter;
import info.magnolia.ui.framework.app.builder.App;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.model.actionbar.builder.ActionbarConfig;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.model.workbench.builder.WorkbenchConfig;

/**
 * TODO: describe this type
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
    public void contactsApp(ContentAppBuilder app, WorkbenchConfig wbcfg, ActionbarConfig abcfg) {
        app.label("security").icon("icon-security-app").appClass(SecurityApp.class).categoryName("MANAGE")
            .subApps(
                    app.subApp("main").subAppClass(SecurityMainSubApp.class).defaultSubApp()
                    .workbench(wbcfg.workbench().workspace("users").root("/").defaultOrder("jcrName")
                            .groupingItemType(wbcfg.itemType(MgnlNodeType.NT_CONTENT).icon("/.resources/icons/16/folders.gif"))
                            .mainItemType(wbcfg.itemType(MgnlNodeType.USER).icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                            .columns(
                                    wbcfg.column(new UserNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName").formatterClass(UserNameColumnFormatter.class),
                                    wbcfg.column(new PropertyColumnDefinition()).name("email").label("Email").sortable(true).width(180).displayInDialog(false),
                                    wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50),
                                    wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName("MetaData/mgnl:lastmodified").displayInDialog(false).width(200).sortable(true)
                            )
                            /*
                            .actionbar(abcfg.actionbar().defaultAction("edit")
                                    .sections(
                                            abcfg.section("contactsActions").label("Contacts")
                                                    .groups(
                                                            abcfg.group("addActions").items(
                                                                    abcfg.item("addContact").label("New contact").icon("icon-add-item").action(addContactAction),
                                                                    abcfg.item("addFolder").label("New folder").icon("icon-add-item").action(new AddFolderActionDefinition())),
                                                            abcfg.group("editActions").items(
                                                                    abcfg.item("edit").label("Edit contact").icon("icon-edit").action(editContactAction),
                                                                    abcfg.item("delete").label("Delete contact").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                                    ),
                                            abcfg.section("folderActions").label("Folder")
                                                    .groups(
                                                            abcfg.group("addActions").items(
                                                                    abcfg.item("addContact").label("New contact").icon("icon-add-item").action(addContactAction),
                                                                    abcfg.item("addFolder").label("New folder").icon("icon-add-item").action(new AddFolderActionDefinition())),
                                                            abcfg.group("editActions").items(
                                                                    abcfg.item("edit").label("Edit folder").icon("icon-edit").action(editFolderAction),
                                                                    abcfg.item("delete").label("Delete folder").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                                    )
                                    )
                            )
                            */
                    )
            )
        ;
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
