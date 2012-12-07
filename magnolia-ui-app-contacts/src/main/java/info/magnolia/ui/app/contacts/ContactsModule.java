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
package info.magnolia.ui.app.contacts;

import javax.inject.Inject;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.app.CodeConfigurationUtils;
import info.magnolia.ui.admincentral.app.content.builder.ContentAppBuilder;
import info.magnolia.ui.admincentral.column.DateColumnFormatter;
import info.magnolia.ui.admincentral.column.StatusColumnFormatter;
import info.magnolia.ui.admincentral.content.action.EditItemActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.admincentral.form.action.CancelFormActionDefinition;
import info.magnolia.ui.admincentral.form.action.CreateItemActionDefinition;
import info.magnolia.ui.admincentral.image.DefaultImageProvider;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.contacts.action.AddFolderActionDefinition;
import info.magnolia.ui.app.contacts.column.ContactNameColumnDefinition;
import info.magnolia.ui.app.contacts.column.ContactNameColumnFormatter;
import info.magnolia.ui.app.contacts.dialog.action.SaveContactDialogActionDefinition;
import info.magnolia.ui.app.contacts.form.action.SaveContactFormActionDefinition;
import info.magnolia.ui.app.contacts.item.ContactsItemSubApp;
import info.magnolia.ui.framework.app.builder.App;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.model.builder.UiConfig;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.dialog.action.ConfiguredDialogActionDefinition;
import info.magnolia.ui.model.dialog.builder.Dialog;
import info.magnolia.ui.model.dialog.builder.DialogBuilder;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.model.field.definition.TextFieldDefinition;
import info.magnolia.ui.model.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.model.tab.definition.ConfiguredTabDefinition;

/**
 * Module class for the contacts module.
 */
public class ContactsModule implements ModuleLifecycle {

    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private AppDescriptorRegistry appDescriptorRegistry;

    @Inject
    public ContactsModule(DialogDefinitionRegistry dialogDefinitionRegistry, AppDescriptorRegistry appDescriptorRegistry) {
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.appDescriptorRegistry = appDescriptorRegistry;
    }

    @App("contacts")
    public void contactsApp(ContentAppBuilder app, UiConfig cfg) {

        // Configure ImageProvider
        ConfiguredImageProviderDefinition cipd = new ConfiguredImageProviderDefinition();
        cipd.setOriginalImageNodeName("photo");
        cipd.setImageProviderClass(DefaultImageProvider.class);

        CreateItemActionDefinition addContactAction = new CreateItemActionDefinition();
        addContactAction.setNodeType("mgnl:contact");
        addContactAction.setAppId("contacts");
        addContactAction.setSubAppId("item");

        EditItemActionDefinition editContactAction = new EditItemActionDefinition();
        editContactAction.setAppId("contacts");
        editContactAction.setSubAppId("item");

        EditDialogActionDefinition editContactActionInDialog = new EditDialogActionDefinition();
        editContactActionInDialog.setDialogName("ui-contacts-app:contact");

        EditDialogActionDefinition editFolderAction = new EditDialogActionDefinition();
        editFolderAction.setDialogName("ui-contacts-app:folder");

        app.label("Contacts").icon("icon-people").appClass(ContactsApp.class)
                .subApps(
                        app.subApp("main").subAppClass(ContactsMainSubApp.class).defaultSubApp()
                                .workbench(cfg.workbenches.workbench().workspace("contacts").root("/").defaultOrder("jcrName")
                                        .groupingItemType(cfg.workbenches.itemType("mgnl:folder").icon("/.resources/icons/16/folders.gif"))
                                        .mainItemType(cfg.workbenches.itemType("mgnl:contact").icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                                        .imageProvider(cipd)
                                        .columns(
                                                cfg.columns.column(new ContactNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName").formatterClass(ContactNameColumnFormatter.class).expandRatio(2),
                                                cfg.columns.property("email", "Email").sortable(true).displayInDialog(false).expandRatio(1),
                                                cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(46),
                                                cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification Date").sortable(true).propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160)
                                        )
                                        .actionbar(cfg.actionbars.actionbar().defaultAction("edit")
                                                .sections(
                                                        cfg.actionbars.section("contactsActions").label("Contacts")
                                                                .groups(
                                                                        cfg.actionbars.group("addActions").items(
                                                                                cfg.actionbars.item("addContact").label("New contact").icon("icon-add-item").action(addContactAction),
                                                                                cfg.actionbars.item("addFolder").label("New folder").icon("icon-add-item").action(new AddFolderActionDefinition())),
                                                                        cfg.actionbars.group("editActions").items(
                                                                                cfg.actionbars.item("edit").label("Edit contact").icon("icon-edit").action(editContactAction),
                                                                                cfg.actionbars.item("editindialog").label("Edit contact in Dialog").icon("icon-edit").action(editContactActionInDialog),
                                                                                cfg.actionbars.item("delete").label("Delete contact").icon("icon-delete").action(new DeleteItemActionDefinition()))

                                                                ),
                                                        cfg.actionbars.section("folderActions").label("Folder")
                                                                .groups(
                                                                        cfg.actionbars.group("addActions").items(
                                                                                cfg.actionbars.item("addContact").label("New contact").icon("icon-add-item").action(addContactAction),
                                                                                cfg.actionbars.item("addFolder").label("New folder").icon("icon-add-item").action(new AddFolderActionDefinition())),
                                                                        cfg.actionbars.group("editActions").items(
                                                                                cfg.actionbars.item("edit").label("Edit folder").icon("icon-edit").action(editFolderAction),
                                                                                cfg.actionbars.item("delete").label("Delete folder").icon("icon-delete").action(new DeleteItemActionDefinition()))
                                                                )
                                                )
                                        )

                                ),

                        app.subApp("item").subAppClass(ContactsItemSubApp.class)
                                .workbench(cfg.workbenches.workbench().workspace("contacts").root("/").defaultOrder("jcrName")
                                        .form(cfg.forms.form().description("Define the contact information")
                                                .tabs(
                                                        cfg.forms.tab("Personal").label("Personal tab")
                                                                .fields(
                                                                        cfg.fields.text("salutation").label("Salutation").description("Define salutation"),
                                                                        cfg.fields.text("firstName").label("First name").description("Please enter the contact first name. Field is mandatory").required(),
                                                                        cfg.fields.text("lastName").label("Last name").description("Please enter the contact last name. Field is mandatory").required(),
                                                                        cfg.fields.fileUpload("fileUpload").label("Image").preview().imageNodeName("photo"),
                                                                        cfg.fields.text("photoCaption").label("Image caption").description("Please define an image caption"),
                                                                        cfg.fields.text("photoAltText").label("Image alt text").description("Please define an image alt text")
                                                                ),
                                                        cfg.forms.tab("Company").label("Company tab")
                                                                .fields(
                                                                        cfg.fields.text("organizationName").label("Organization name").description("Enter the organization name").required(),
                                                                        cfg.fields.text("organizationUnitName").label("Organization unit name").description("Enter the organization unit name"),
                                                                        cfg.fields.text("streetAddress").label("Street address").description("Please enter the company street address").rows(2),
                                                                        cfg.fields.text("zipCode").label("ZIP code").description("Please enter the zip code (only digits)").validator(cfg.validators.digitsOnly().errorMessage("validation.message.only.digits")),
                                                                        cfg.fields.text("city").label("City").description("Please enter the company city  "),
                                                                        cfg.fields.text("country").label("Country").description("Please enter the company country")
                                                                ),
                                                        cfg.forms.tab("Contacts").label("Contact tab")
                                                                .fields(
                                                                        cfg.fields.text("officePhoneNr").label("Office phone").description("Please enter the office phone number"),
                                                                        cfg.fields.text("officeFaxNr").label("Office fax nr.").description("Please enter the office fax number"),
                                                                        cfg.fields.text("mobilePhoneNr").label("Mobile phone").description("Please enter the mobile phone number"),
                                                                        cfg.fields.text("email").label("E-Mail address").description("Please enter the email address").required().validator(cfg.validators.email().errorMessage("validation.message.non.valid.email")),
                                                                        cfg.fields.text("website").label("Website").description("Please enter the Website")
                                                                )
                                                )
                                                .actions(
                                                        cfg.forms.action("commit").label("save changes").action(new SaveContactFormActionDefinition()),
                                                        cfg.forms.action("cancel").label("cancel").action(new CancelFormActionDefinition())
                                                )
                                        )
                                )
                );
    }

    @Dialog("ui-contacts-app:folder")
    public DialogDefinition folderDialog() {

        ConfiguredDialogDefinition dialog = new ConfiguredDialogDefinition();
        dialog.setLabel("Folder");
        dialog.setDescription("Rename folder");

        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("folder");
        tab.setLabel("Folder");
        dialog.addTab(tab);

        TextFieldDefinition name = new TextFieldDefinition();
        name.setName("jcrName");
        name.setLabel("Name");
        name.setDescription("Folder name");
        tab.addField(name);

        ConfiguredDialogActionDefinition commit = new ConfiguredDialogActionDefinition();
        commit.setName("commit");
        commit.setLabel("save changes");
        commit.setActionDefinition(new SaveDialogActionDefinition());
        dialog.addAction(commit);

        ConfiguredDialogActionDefinition cancel = new ConfiguredDialogActionDefinition();
        cancel.setName("cancel");
        cancel.setLabel("cancel");
        cancel.setActionDefinition(new CancelDialogActionDefinition());
        dialog.addAction(cancel);

        return dialog;
    }

    @Dialog("ui-contacts-app:contact")
    public void contactDialog(DialogBuilder dialog, UiConfig cfg) {

        dialog.form(cfg.forms.form().description("Define the contact information")
                .tabs(
                        cfg.forms.tab("Personal").label("Personal tab")
                                .fields(
                                        cfg.fields.text("salutation").label("Salutation").description("Define salutation"),
                                        cfg.fields.text("firstName").label("First name").description("Please enter the contact first name. Field is mandatory").required(),
                                        cfg.fields.text("lastName").label("Last name").description("Please enter the contact last name. Field is mandatory").required(),
                                        cfg.fields.fileUpload("fileUpload").label("Image").preview().imageNodeName("photo"),
                                        cfg.fields.text("photoCaption").label("Image caption").description("Please define an image caption"),
                                        cfg.fields.text("photoAltText").label("Image alt text").description("Please define an image alt text")
                                ),
                        cfg.forms.tab("Company").label("Company tab")
                                .fields(
                                        cfg.fields.text("organizationName").label("Organization name").description("Enter the organization name").required(),
                                        cfg.fields.text("organizationUnitName").label("Organization unit name").description("Enter the organization unit name"),
                                        cfg.fields.text("streetAddress").label("Street address").description("Please enter the company street address").rows(2),
                                        cfg.fields.text("zipCode").label("ZIP code").description("Please enter the zip code (only digits)").validator(cfg.validators.digitsOnly().errorMessage("validation.message.only.digits")),
                                        cfg.fields.text("city").label("City").description("Please enter the company city  "),
                                        cfg.fields.text("country").label("Country").description("Please enter the company country")
                                ),
                        cfg.forms.tab("Contacts").label("Contact tab")
                                .fields(
                                        cfg.fields.text("officePhoneNr").label("Office phone").description("Please enter the office phone number"),
                                        cfg.fields.text("officeFaxNr").label("Office fax nr.").description("Please enter the office fax number"),
                                        cfg.fields.text("mobilePhoneNr").label("Mobile phone").description("Please enter the mobile phone number"),
                                        cfg.fields.text("email").label("E-Mail address").description("Please enter the email address").required().validator(cfg.validators.email().errorMessage("validation.message.non.valid.email")),
                                        cfg.fields.text("website").label("Website").description("Please enter the Website")
                                )
                )
        )
                .actions(
                        cfg.dialogs.action("commit").label("save changes").action(new SaveContactDialogActionDefinition()),
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
    }
}
