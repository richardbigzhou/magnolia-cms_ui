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

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.app.CodeConfigurationUtils;
import info.magnolia.ui.admincentral.app.content.builder.ContentAppBuilder;
import info.magnolia.ui.admincentral.column.StatusColumnFormatter;
import info.magnolia.ui.admincentral.content.action.EditItemActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.admincentral.form.action.CancelFormActionDefinition;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.contacts.action.AddFolderActionDefinition;
import info.magnolia.ui.app.contacts.column.ContactNameColumnDefinition;
import info.magnolia.ui.app.contacts.column.ContactNameColumnFormatter;
import info.magnolia.ui.app.contacts.form.action.SaveContactFormActionDefinition;
import info.magnolia.ui.app.contacts.item.ContactsItemSubApp;
import info.magnolia.ui.framework.app.builder.App;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.model.actionbar.builder.ActionbarConfig;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.dialog.action.ConfiguredDialogActionDefinition;
import info.magnolia.ui.model.dialog.builder.Dialog;
import info.magnolia.ui.model.dialog.builder.DialogBuilder;
import info.magnolia.ui.model.dialog.builder.DialogConfig;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.model.field.definition.TextFieldDefinition;
import info.magnolia.ui.model.field.validation.definition.EmailValidatorDefinition;
import info.magnolia.ui.model.field.validation.definition.RegexpValidatorDefinition;
import info.magnolia.ui.model.form.builder.FormConfig;
import info.magnolia.ui.model.tab.definition.ConfiguredTabDefinition;
import info.magnolia.ui.model.thumbnail.DefaultImageProvider;
import info.magnolia.ui.model.workbench.builder.WorkbenchConfig;

import javax.inject.Inject;

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
    public void contactsApp(ContentAppBuilder app, WorkbenchConfig wbcfg, ActionbarConfig abcfg, FormConfig formcfg) {

        DefaultImageProvider imageProvider = new DefaultImageProvider();
        imageProvider.setOriginalImageNodeName("photo");

        CreateDialogActionDefinition addContactAction = new CreateDialogActionDefinition();
        addContactAction.setNodeType("mgnl:contact");
        addContactAction.setDialogName("ui-contacts-app:contact");

        EditItemActionDefinition editContactAction = new EditItemActionDefinition();
        editContactAction.setAppId("contacts");
        editContactAction.setSubAppId("item");

        EditDialogActionDefinition editContactActioninDialog = new EditDialogActionDefinition();
        editContactActioninDialog.setDialogName("ui-contacts-app:contact");


        EditDialogActionDefinition editFolderAction = new EditDialogActionDefinition();
        editFolderAction.setDialogName("ui-contacts-app:folder");

        // form
        RegexpValidatorDefinition digitsOnly = new RegexpValidatorDefinition();
        digitsOnly.setPattern("[0-9]+");
        digitsOnly.setErrorMessage("validation.message.only.digits");

        EmailValidatorDefinition emailValidator = new EmailValidatorDefinition();
        emailValidator.setErrorMessage("validation.message.non.valid.email");
        // form end

        app.label("Contacts").icon("icon-people").appClass(ContactsApp.class)
                .subApps(
                        app.subApp("main").subAppClass(ContactsMainSubApp.class).defaultSubApp()
                                .workbench(wbcfg.workbench().workspace("contacts").root("/").defaultOrder("jcrName")
                                        .groupingItemType(wbcfg.itemType("mgnl:folder").icon("/.resources/icons/16/folders.gif"))
                                        .mainItemType(wbcfg.itemType("mgnl:contact").icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                                        .imageProvider(imageProvider)
                                        .columns(
                                                wbcfg.column(new ContactNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName").formatterClass(ContactNameColumnFormatter.class),
                                                wbcfg.column(new PropertyColumnDefinition()).name("email").label("Email").sortable(true).width(180).displayInDialog(false),
                                                wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50),
                                                wbcfg.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName("MetaData/mgnl:lastmodified").displayInDialog(false).width(200).sortable(true)
                                        )
                                        .actionbar(abcfg.actionbar().defaultAction("edit")
                                                .sections(
                                                        abcfg.section("contactsActions").label("Contacts")
                                                                .groups(
                                                                        abcfg.group("addActions").items(
                                                                                abcfg.item("addContact").label("New contact").icon("icon-add-item").action(addContactAction),
                                                                                abcfg.item("addFolder").label("New folder").icon("icon-add-item").action(new AddFolderActionDefinition())),
                                                                        abcfg.group("editActions").items(
                                                                                abcfg.item("edit").label("Edit contact").icon("icon-edit").action(editContactAction),
                                                                                abcfg.item("editindialog").label("Edit contact in Dialog").icon("icon-edit").action(editContactActioninDialog),
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

                                ),

                        app.subApp("item").subAppClass(ContactsItemSubApp.class)
                                .workbench(wbcfg.workbench().workspace("contacts").root("/").defaultOrder("jcrName")
                                        .form(formcfg.form().description("Define the contact information")
                                                .tabs(
                                                        formcfg.tab("Personal").label("Personal Tab")
                                                                .fields(
                                                                        formcfg.fields.textField("salutation").label("Salutation").description("Define Salutation"),
                                                                        formcfg.fields.textField("firstName").label("First name").description("Please enter the contact first name. Field is mandatory").required(),
                                                                        formcfg.fields.textField("lastName").label("Last name").description("Please enter the contact last name. Field is mandatory").required(),
                                                                        formcfg.fields.fileUploadField("fileUpload").label("Image").preview().imageNodeName("photo"),
                                                                        formcfg.fields.textField("photoCaption").label("Image caption").description("Please define an image caption"),
                                                                        formcfg.fields.textField("photoAltText").label("Image alt text").description("Please define an image alt text")
                                                                ),
                                                        formcfg.tab("Company").label("Company Tab")
                                                                .fields(
                                                                        formcfg.fields.textField("organizationName").label("Organization name").description("Enter the organization name").required(),
                                                                        formcfg.fields.textField("organizationUnitName").label("Organization unit name").description("Enter the organization unit name"),
                                                                        formcfg.fields.textField("streetAddress").label("Street Address").description("Please enter the company street address").rows(2),
                                                                        formcfg.fields.textField("zipCode").label("ZIP code").description("Please enter the zip code (only digits)").validator(digitsOnly),
                                                                        formcfg.fields.textField("city").label("City").description("Please enter the company city  "),
                                                                        formcfg.fields.textField("country").label("Country").description("Please enter the company country")
                                                                ),
                                                        formcfg.tab("Contacts").label("Contact Tab")
                                                                .fields(
                                                                        formcfg.fields.textField("officePhoneNr").label("Office phone").description("Please enter the office phone number"),
                                                                        formcfg.fields.textField("officeFaxNr").label("Office Fax Nr.").description("Please enter the office fax number"),
                                                                        formcfg.fields.textField("mobilePhoneNr").label("Mobile Phone").description("Please enter the mobile phone number"),
                                                                        formcfg.fields.textField("email").label("E-Mail address").description("Please enter the email address").required().validator(emailValidator),
                                                                        formcfg.fields.textField("website").label("Website").description("Please enter the Website")
                                                                )
                                                )
                                                .actions(
                                                        formcfg.action("commit").label("save changes").action(new SaveContactFormActionDefinition()),
                                                        formcfg.action("cancel").label("cancel").action(new CancelFormActionDefinition())
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
    public void contactDialog(DialogBuilder dialog, DialogConfig cfg, FormConfig formcfg) {

        RegexpValidatorDefinition digitsOnly = new RegexpValidatorDefinition();
        digitsOnly.setPattern("[0-9]+");
        digitsOnly.setErrorMessage("validation.message.only.digits");

        EmailValidatorDefinition emailValidator = new EmailValidatorDefinition();
        emailValidator.setErrorMessage("validation.message.non.valid.email");

        dialog.description("Define the contact information")
                .form(formcfg.form().description("Define the contact information")
                        .tabs(
                                formcfg.tab("Personal").label("Personal Tab")
                                        .fields(
                                                formcfg.fields.textField("salutation").label("Salutation").description("Define Salutation"),
                                                formcfg.fields.textField("firstName").label("First name").description("Please enter the contact first name. Field is mandatory").required(),
                                                formcfg.fields.textField("lastName").label("Last name").description("Please enter the contact last name. Field is mandatory").required(),
                                                formcfg.fields.fileUploadField("fileUpload").label("Image").preview().imageNodeName("photo"),
                                                formcfg.fields.textField("photoCaption").label("Image caption").description("Please define an image caption"),
                                                formcfg.fields.textField("photoAltText").label("Image alt text").description("Please define an image alt text")
                                        ),
                                formcfg.tab("Company").label("Company Tab")
                                        .fields(
                                                formcfg.fields.textField("organizationName").label("Organization name").description("Enter the organization name").required(),
                                                formcfg.fields.textField("organizationUnitName").label("Organization unit name").description("Enter the organization unit name"),
                                                formcfg.fields.textField("streetAddress").label("Street Address").description("Please enter the company street address").rows(2),
                                                formcfg.fields.textField("zipCode").label("ZIP code").description("Please enter the zip code (only digits)").validator(digitsOnly),
                                                formcfg.fields.textField("city").label("City").description("Please enter the company city  "),
                                                formcfg.fields.textField("country").label("Country").description("Please enter the company country")
                                        ),
                                formcfg.tab("Contacts").label("Contact Tab")
                                        .fields(
                                                formcfg.fields.textField("officePhoneNr").label("Office phone").description("Please enter the office phone number"),
                                                formcfg.fields.textField("officeFaxNr").label("Office Fax Nr.").description("Please enter the office fax number"),
                                                formcfg.fields.textField("mobilePhoneNr").label("Mobile Phone").description("Please enter the mobile phone number"),
                                                formcfg.fields.textField("email").label("E-Mail address").description("Please enter the email address").required().validator(emailValidator),
                                                formcfg.fields.textField("website").label("Website").description("Please enter the Website")
                                        )
                        )
                        .actions(
                                formcfg.action("commit").label("save changes").action(new SaveContactFormActionDefinition()),
                                formcfg.action("cancel").label("cancel").action(new CancelFormActionDefinition())
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
    }
}
