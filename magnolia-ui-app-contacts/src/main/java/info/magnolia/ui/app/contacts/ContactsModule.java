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

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.column.StatusColumnFormatter;
import info.magnolia.ui.admincentral.content.action.EditItemActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.contacts.action.AddFolderActionDefinition;
import info.magnolia.ui.app.contacts.cconf.CodeConfigurationUtils;
import info.magnolia.ui.app.contacts.cconf.actionbar.ActionbarBuilder;
import info.magnolia.ui.app.contacts.cconf.actionbar.ActionbarGroupBuilder;
import info.magnolia.ui.app.contacts.cconf.actionbar.ActionbarSectionBuilder;
import info.magnolia.ui.app.contacts.cconf.app.App;
import info.magnolia.ui.app.contacts.cconf.app.ContentAppBuilder;
import info.magnolia.ui.app.contacts.cconf.app.SubAppBuilder;
import info.magnolia.ui.app.contacts.cconf.dialog.AbstractFieldBuilder;
import info.magnolia.ui.app.contacts.cconf.dialog.Dialog;
import info.magnolia.ui.app.contacts.cconf.dialog.DialogBuilder;
import info.magnolia.ui.app.contacts.cconf.dialog.TabBuilder;
import info.magnolia.ui.app.contacts.cconf.workbench.WorkbenchBuilder;
import info.magnolia.ui.app.contacts.column.ContactNameColumnDefinition;
import info.magnolia.ui.app.contacts.column.ContactNameColumnFormatter;
import info.magnolia.ui.app.contacts.dialog.action.SaveContactDialogActionDefinition;
import info.magnolia.ui.app.contacts.item.ContactsItemSubApp;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.dialog.action.ConfiguredDialogActionDefinition;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.model.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.model.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.model.field.definition.TextFieldDefinition;
import info.magnolia.ui.model.field.validation.definition.EmailValidatorDefinition;
import info.magnolia.ui.model.field.validation.definition.RegexpValidatorDefinition;
import info.magnolia.ui.model.tab.definition.ConfiguredTabDefinition;
import info.magnolia.ui.model.thumbnail.DefaultImageProvider;

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
    public void contactsApp(ContentAppBuilder app) {

        app.label("Contacts").icon("icon-people").appClass(ContactsApp.class).categoryName("MANAGE");

        SubAppBuilder subApp = app.subApp("main").subAppClass(ContactsMainSubApp.class).defaultSubApp();

        WorkbenchBuilder workbench = subApp.workbench().workspace("contacts").root("/").defaultOrder("jcrName");
        workbench.groupingItemType("mgnl:folder").icon("/.resources/icons/16/folders.gif");
        workbench.mainItemType("mgnl:contact").icon("/.resources/icons/16/pawn_glass_yellow.gif");
        workbench.column(new ContactNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName").formatterClass(ContactNameColumnFormatter.class);
        workbench.column(new PropertyColumnDefinition()).name("email").label("Email").sortable(true).width(180).displayInDialog(false);
        workbench.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass(StatusColumnFormatter.class).width(50);
        workbench.column(new MetaDataColumnDefinition()).name("moddate").label("Mod. Date").propertyName("MetaData/mgnl:lastmodified").displayInDialog(false).width(200).sortable(true);

        DefaultImageProvider imageProvider = new DefaultImageProvider();
        imageProvider.setOriginalImageNodeName("photo");
        workbench.imageProvider(imageProvider);

        ActionbarBuilder actionbar = workbench.actionbar().defaultAction("edit");

        ActionbarSectionBuilder contactsActions = actionbar.section("contactsActions").label("Contacts");
        ActionbarGroupBuilder addActions = contactsActions.group("addActions");
        CreateDialogActionDefinition addContactAction = new CreateDialogActionDefinition();
        addContactAction.setNodeType("mgnl:contact");
        addContactAction.setDialogName("ui-contacts-app:contact");
        addActions.item("addContact").label("New contact").icon("icon-add-item").action(addContactAction);
        addActions.item("addFolder").label("New folder").icon("icon-add-item").action(new AddFolderActionDefinition());
        ActionbarGroupBuilder editActions = contactsActions.group("editActions");
        EditItemActionDefinition editContactAction = new EditItemActionDefinition();
        editContactAction.setAppId("contacts");
        editContactAction.setSubAppId("item");
        editActions.item("edit").label("Edit contact").icon("icon-edit").action(editContactAction);
        editActions.item("delete").label("Delete contact").icon("icon-delete").action(new DeleteItemActionDefinition());

        ActionbarSectionBuilder folderActions = actionbar.section("folderActions").label("Folder");
        ActionbarGroupBuilder folderAddActions = folderActions.group("addActions");
        folderAddActions.item("addContact").label("New contact").icon("icon-add-item").action(addContactAction);
        folderAddActions.item("addFolder").label("New folder").icon("icon-add-item").action(new AddFolderActionDefinition());
        ActionbarGroupBuilder folderEditActions = folderActions.group("editActions");
        EditDialogActionDefinition editFolderAction = new EditDialogActionDefinition();
        editFolderAction.setDialogName("ui-contacts-app:folder");
        folderEditActions.item("edit").label("Edit folder").icon("icon-edit").action(editFolderAction);
        folderEditActions.item("delete").label("Delete folder").icon("icon-delete").action(new DeleteItemActionDefinition());


        subApp = app.subApp("item").subAppClass(ContactsItemSubApp.class);

        workbench = subApp.workbench().workspace("contacts").formName("ui-contacts-app:contact");

        imageProvider = new DefaultImageProvider();
        imageProvider.setOriginalImageNodeName("photo");
        workbench.imageProvider(imageProvider);

        actionbar = workbench.actionbar().defaultAction("edit");


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
    public void contactDialog(DialogBuilder dialog) {
        dialog.description("Define the contact information");

        TabBuilder personalTab = dialog.tab("Personal").label("Personal Tab");
        personalTab.textField("salutation").label("Salutation").description("Define Salutation");
        personalTab.textField("firstName").label("First name").description("Please enter the contact first name. Field is mandatory").required();
        personalTab.textField("lastName").label("Last name").description("Please enter the contact last name. Field is mandatory").required();
        personalTab.fileUploadField("fileUpload").label("Image").preview().imageNodeName("photo");
        personalTab.textField("photoCaption").label("Image caption").description("Please define an image caption");
        personalTab.textField("photoAltText").label("Image alt text").description("Please define an image alt text");

        TabBuilder companyTab = dialog.tab("Company").label("Company Tab");
        companyTab.textField("organizationName").label("Organization name").description("Enter the organization name").required();
        companyTab.textField("organizationUnitName").label("Organization unit name").description("Enter the organization unit name");
        companyTab.textField("streetAddress").label("Street Address").description("Please enter the company street address").rows(2);
        RegexpValidatorDefinition digitOnly = new RegexpValidatorDefinition();
        digitOnly.setPattern("[0-9]+");
        digitOnly.setErrorMessage("validation.message.only.digits");
        companyTab.textField("zipCode").label("ZIP code").description("Please enter the zip code (only digits)").validator(digitOnly);
        companyTab.textField("city").label("City").description("Please enter the company city  ");
        companyTab.textField("country").label("Country").description("Please enter the company country");

        TabBuilder contactsTab = dialog.tab("Contacts").label("Contact Tab");
        contactsTab.textField("officePhoneNr").label("Office phone").description("Please enter the office phone number");
        contactsTab.textField("officeFaxNr").label("Office Fax Nr.").description("Please enter the office fax number");
        contactsTab.textField("mobilePhoneNr").label("Mobile Phone").description("Please enter the mobile phone number");
        EmailValidatorDefinition emailValidator = new EmailValidatorDefinition();
        emailValidator.setErrorMessage("validation.message.non.valid.email");
        contactsTab.textField("email").label("E-Mail address").description("Please enter the email address").required().validator(emailValidator);
        contactsTab.textField("website").label("Website").description("Please enter the Website");

        dialog.dialogAction("commit").label("save changes").action(new SaveContactDialogActionDefinition());
        dialog.dialogAction("cancel").label("cancel").action(new CancelDialogActionDefinition());
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        CodeConfigurationUtils.registerAnnotatedAppProviders(appDescriptorRegistry, this);
        CodeConfigurationUtils.registerAnnotatedDialogProviders(dialogDefinitionRegistry, this);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    /**
     * Field definition for fancy media example field.
     */
    private static class FancyMediaFieldDefinition extends ConfiguredFieldDefinition {

        private int fanciness;

        public int getFanciness() {
            return fanciness;
        }

        public void setFanciness(int fanciness) {
            this.fanciness = fanciness;
        }
    }

    /**
     * Builder for fancy media example field.
     */
    public static class FancyMediaFieldBuilder extends AbstractFieldBuilder {

        private FancyMediaFieldDefinition definition;

        public FancyMediaFieldBuilder(FancyMediaFieldDefinition definition) {
            this.definition = definition;
        }

        public FancyMediaFieldBuilder(ConfiguredTabDefinition tabDefinition, String name) {
            definition = new FancyMediaFieldDefinition();
            definition.setName(name);
        }

        @Override
        protected FancyMediaFieldDefinition getDefinition() {
            return definition;
        }

        public FancyMediaFieldBuilder fanciness(int fanciness) {
            getDefinition().setFanciness(fanciness);
            return this;
        }
    }
}
