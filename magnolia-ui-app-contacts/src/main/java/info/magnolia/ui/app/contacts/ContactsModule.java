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
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.contacts.action.AddFolderActionDefinition;
import info.magnolia.ui.admincentral.app.CodeConfigurationUtils;
import info.magnolia.ui.model.actionbar.builder.ActionbarConfig;
import info.magnolia.ui.framework.app.builder.App;
import info.magnolia.ui.admincentral.app.content.builder.ContentAppBuilder;
import info.magnolia.ui.model.dialog.builder.AbstractFieldBuilder;
import info.magnolia.ui.model.dialog.builder.Dialog;
import info.magnolia.ui.model.dialog.builder.DialogBuilder;
import info.magnolia.ui.model.dialog.builder.DialogConfig;
import info.magnolia.ui.model.workbench.builder.WorkbenchConfig;
import info.magnolia.ui.app.contacts.column.ContactNameColumnDefinition;
import info.magnolia.ui.app.contacts.dialog.action.SaveContactDialogActionDefinition;
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
    public void contactsApp(ContentAppBuilder app, WorkbenchConfig wbcfg, ActionbarConfig abcfg) {

        DefaultImageProvider imageProvider = new DefaultImageProvider();
        imageProvider.setOriginalImageNodeName("photo");

        CreateDialogActionDefinition addContactAction = new CreateDialogActionDefinition();
        addContactAction.setNodeType("mgnl:contact");
        addContactAction.setDialogName("ui-contacts-app:contact");

        EditDialogActionDefinition editContactAction = new EditDialogActionDefinition();
        editContactAction.setDialogName("ui-contacts-app:contact");

        EditDialogActionDefinition editFolderAction = new EditDialogActionDefinition();
        editFolderAction.setDialogName("ui-contacts-app:folder");

        app.label("Contacts").icon("icon-people").appClass(ContactsApp.class).categoryName("MANAGE")
                .subApps(
                        app.subApp("main").subAppClass(ContactsMainSubApp.class).defaultSubApp()
                                .workbench(wbcfg.workbench().workspace("contacts").root("/").defaultOrder("jcrName")
                                        .groupingItemType(wbcfg.itemType("mgnl:folder").icon("/.resources/icons/16/folders.gif"))
                                        .mainItemType(wbcfg.itemType("mgnl:contact").icon("/.resources/icons/16/pawn_glass_yellow.gif"))
                                        .imageProvider(imageProvider)
                                        .columns(
                                                wbcfg.column(new ContactNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName("jcrName").formatterClass("info.magnolia.ui.app.contacts.column.ContactNameColumnFormatter"),
                                                wbcfg.column(new PropertyColumnDefinition()).name("email").label("Email").sortable(true).width(180).displayInDialog(false),
                                                wbcfg.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false).formatterClass("info.magnolia.ui.admincentral.column.StatusColumnFormatter").width(50),
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

                        app.subApp("detail").subAppClass(ContactsMainSubApp.class)
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
    public void contactDialog(DialogBuilder dialog, DialogConfig cfg) {

        RegexpValidatorDefinition digitsOnly = new RegexpValidatorDefinition();
        digitsOnly.setPattern("[0-9]+");
        digitsOnly.setErrorMessage("validation.message.only.digits");

        EmailValidatorDefinition emailValidator = new EmailValidatorDefinition();
        emailValidator.setErrorMessage("validation.message.non.valid.email");

        dialog.description("Define the contact information")
                .tabs(
                        cfg.tab("Personal").label("Personal Tab")
                                .fields(
                                        cfg.fields.textField("salutation").label("Salutation").description("Define Salutation"),
                                        cfg.fields.textField("firstName").label("First name").description("Please enter the contact first name. Field is mandatory").required(),
                                        cfg.fields.textField("lastName").label("Last name").description("Please enter the contact last name. Field is mandatory").required(),
                                        cfg.fields.fileUploadField("fileUpload").label("Image").preview().imageNodeName("photo"),
                                        cfg.fields.textField("photoCaption").label("Image caption").description("Please define an image caption"),
                                        cfg.fields.textField("photoAltText").label("Image alt text").description("Please define an image alt text")
                                ),
                        cfg.tab("Company").label("Company Tab")
                                .fields(
                                        cfg.fields.textField("organizationName").label("Organization name").description("Enter the organization name").required(),
                                        cfg.fields.textField("organizationUnitName").label("Organization unit name").description("Enter the organization unit name"),
                                        cfg.fields.textField("streetAddress").label("Street Address").description("Please enter the company street address").rows(2),
                                        cfg.fields.textField("zipCode").label("ZIP code").description("Please enter the zip code (only digits)").validator(digitsOnly),
                                        cfg.fields.textField("city").label("City").description("Please enter the company city  "),
                                        cfg.fields.textField("country").label("Country").description("Please enter the company country")
                                ),
                        cfg.tab("Contacts").label("Contact Tab")
                                .fields(
                                        cfg.fields.textField("officePhoneNr").label("Office phone").description("Please enter the office phone number"),
                                        cfg.fields.textField("officeFaxNr").label("Office Fax Nr.").description("Please enter the office fax number"),
                                        cfg.fields.textField("mobilePhoneNr").label("Mobile Phone").description("Please enter the mobile phone number"),
                                        cfg.fields.textField("email").label("E-Mail address").description("Please enter the email address").required().validator(emailValidator),
                                        cfg.fields.textField("website").label("Website").description("Please enter the Website")
                                )
                )
                .actions(
                        cfg.action("commit").label("save changes").action(new SaveContactDialogActionDefinition()),
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
