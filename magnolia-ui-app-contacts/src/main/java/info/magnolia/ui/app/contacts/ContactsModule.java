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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.dialog.action.CancelDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.SaveDialogActionDefinition;
import info.magnolia.ui.admincentral.image.DefaultImageProvider;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.app.contacts.ContactNodeType.Contact;
import info.magnolia.ui.app.contacts.action.AddFolderActionDefinition;
import info.magnolia.ui.app.contacts.column.ContactNameColumnDefinition;
import info.magnolia.ui.app.contacts.column.ContactNameColumnFormatter;
import info.magnolia.ui.app.contacts.form.action.SaveContactFormAction;
import info.magnolia.ui.app.contacts.main.ContactsMainSubApp;
import info.magnolia.ui.app.contacts.main.tree.ContactDropConstraint;
import info.magnolia.ui.contentapp.ContentApp;
import info.magnolia.ui.contentapp.config.CodeConfigurationUtil;
import info.magnolia.ui.contentapp.config.ContentAppBuilder;
import info.magnolia.ui.contentapp.config.ContentAppConfig;
import info.magnolia.ui.contentapp.detail.DetailSubApp;
import info.magnolia.ui.contentapp.detail.action.CreateItemActionDefinition;
import info.magnolia.ui.contentapp.detail.action.EditItemActionDefinition;
import info.magnolia.ui.dialog.config.Dialog;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.action.CancelFormActionDefinition;
import info.magnolia.ui.form.action.SaveFormActionDefinition;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.framework.app.config.App;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.config.UiConfig;
import info.magnolia.ui.model.ModelConstants;
import info.magnolia.ui.model.imageprovider.definition.ConfiguredImageProviderDefinition;
import info.magnolia.ui.workbench.column.DateColumnFormatter;
import info.magnolia.ui.workbench.column.StatusColumnFormatter;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.workbench.column.definition.StatusColumnDefinition;
import info.magnolia.ui.workbench.list.ListContentViewDefinition;
import info.magnolia.ui.workbench.search.SearchContentViewDefinition;
import info.magnolia.ui.workbench.thumbnail.ThumbnailContentViewDefinition;
import info.magnolia.ui.workbench.tree.TreeContentViewDefinition;

import javax.inject.Inject;
import javax.jcr.PropertyType;

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
    public void contactsApp(ContentAppBuilder app, UiConfig cfg, ContentAppConfig contentAppConfig) {

        // Configure ImageProvider
        ConfiguredImageProviderDefinition cipd = new ConfiguredImageProviderDefinition();
        cipd.setOriginalImageNodeName(Contact.IMAGE_NODE_NAME);
        cipd.setImageProviderClass(DefaultImageProvider.class);


        CreateItemActionDefinition addContactAction = new CreateItemActionDefinition();
        addContactAction.setName("addContact");
        addContactAction.setLabel("New contact");
        addContactAction.setIcon("icon-add-item");
        addContactAction.setNodeType(Contact.NAME);
        addContactAction.setAppId("contacts");
        addContactAction.setSubAppId("detail");

        EditItemActionDefinition editContactAction = new EditItemActionDefinition();
        editContactAction.setName("editContact");
        editContactAction.setLabel("Edit contact");
        editContactAction.setIcon("icon-edit");
        editContactAction.setNodeType(Contact.NAME);
        editContactAction.setAppId("contacts");
        editContactAction.setSubAppId("detail");

        DeleteItemActionDefinition deleteItemAction = new DeleteItemActionDefinition();
        deleteItemAction.setName("deleteContact");
        deleteItemAction.setLabel("Delete contact");
        deleteItemAction.setIcon("icon-delete");


        AddFolderActionDefinition addFolderAction = new AddFolderActionDefinition();
        addFolderAction.setName("addFolder");
        addFolderAction.setLabel("New folder");
        addFolderAction.setIcon("icon-add-item");


        EditDialogActionDefinition editFolderAction = new EditDialogActionDefinition();
        editFolderAction.setName("editFolder");
        editFolderAction.setLabel("Rename folder");
        editFolderAction.setIcon("icon-edit");
        editFolderAction.setDialogName("ui-contacts-app:folder");

        DeleteItemActionDefinition deleteFolderAction = new DeleteItemActionDefinition();
        deleteFolderAction.setName("deleteFolder");
        deleteFolderAction.setLabel("Delete folder");
        deleteFolderAction.setIcon("icon-delete");

        SaveFormActionDefinition saveFormAction = new SaveFormActionDefinition();
        saveFormAction.setImplementationClass(SaveContactFormAction.class);
        saveFormAction.setName("commit");
        saveFormAction.setLabel("save changes");

        CancelFormActionDefinition cancelFormAction = new CancelFormActionDefinition();
        cancelFormAction.setName("cancel");
        cancelFormAction.setLabel("cancel");

        app.label("Contacts")
                .icon("icon-people")
                .appClass(ContentApp.class)
                .subApps(
                        app.browserSubApp("browser")
                                .subAppClass(ContactsMainSubApp.class)
                                .actions(addContactAction, editContactAction, deleteItemAction, addFolderAction, editFolderAction, deleteFolderAction)
                                .imageProvider(cipd)
                                .workbench(
                                        contentAppConfig.workbench
                                                .workbench()
                                                .workspace("contacts")
                                                .path("/")

                                                .dropConstraintClass(ContactDropConstraint.class)
                                                .contentViews(new TreeContentViewDefinition(), new ListContentViewDefinition(), new ThumbnailContentViewDefinition(), new SearchContentViewDefinition())
                                                .defaultOrder(ModelConstants.JCR_NAME)
                                                .nodeTypes(
                                                        contentAppConfig.workbench.nodeType(Contact.NAME).icon("icon-node-content"),
                                                        contentAppConfig.workbench.nodeType(NodeTypes.Folder.NAME).icon("icon-folder"))
                                                .columns(
                                                        cfg.columns.column(new ContactNameColumnDefinition()).name("name").label("Name").sortable(true).propertyName(ModelConstants.JCR_NAME)
                                                                .formatterClass(ContactNameColumnFormatter.class).expandRatio(2),
                                                        cfg.columns.property("email", "Email").sortable(true).displayInDialog(false).expandRatio(1),
                                                        cfg.columns.column(new StatusColumnDefinition()).name("status").label("Status").displayInDialog(false)
                                                                .formatterClass(StatusColumnFormatter.class).width(46),
                                                        cfg.columns.column(new MetaDataColumnDefinition()).name("moddate").label("Modification Date").sortable(true)
                                                                .propertyName(NodeTypes.LastModified.LAST_MODIFIED).displayInDialog(false).formatterClass(DateColumnFormatter.class).width(160))

                                )
                                .actionbar(
                                        cfg.actionbars
                                                .actionbar()
                                                .defaultAction(editContactAction.getName())
                                                .sections(
                                                        cfg.actionbars
                                                                .section("contactsActions")
                                                                .label("Contact")
                                                                .groups(cfg.actionbars.group("addActions").actions(addContactAction.getName()),
                                                                        cfg.actionbars.group("editActions").actions(editContactAction.getName(), deleteItemAction.getName())
                                                                )
                                                        ,
                                                        cfg.actionbars
                                                                .section("folderActions")
                                                                .label("Folder")
                                                                .groups(cfg.actionbars.group("addActions").actions(addFolderAction.getName()),
                                                                        cfg.actionbars.group("editActions").actions(editFolderAction.getName(), deleteFolderAction.getName())
                                                                )
                                                )
                                ).exec(),

                        app.detailSubApp("detail")
                                .subAppClass(DetailSubApp.class)
                                .actions(saveFormAction, cancelFormAction)
                                .editor(contentAppConfig.editor.editor()
                                        .nodeType(contentAppConfig.workbench.nodeType(Contact.NAME).icon("icon-node-content"))
                                        .workspace("contacts")
                                        .form(cfg.forms
                                                .form()
                                                .label("Edit contact")
                                                .description("Define the contact information")
                                                .tabs(cfg.forms
                                                        .tab("Personal")
                                                        .label("Personal")
                                                        .fields(cfg.fields.text(Contact.PROPERTY_SALUTATION).label("Salutation").description("Define salutation"),
                                                                cfg.fields.text(Contact.PROPERTY_FIRST_NAME).label("First name")
                                                                        .description("Please enter the contact first name. Field is mandatory")
                                                                        .required(),
                                                                cfg.fields.text(Contact.PROPERTY_LAST_NAME).label("Last name")
                                                                        .description("Please enter the contact last name. Field is mandatory").required(),
                                                                cfg.fields.fileUpload("fileUpload").label("Image").preview().imageNodeName(Contact.IMAGE_NODE_NAME),
                                                                cfg.fields.text(Contact.PROPERTY_PHOTO_CAPTION).label("Image caption")
                                                                        .description("Please define an image caption"),
                                                                cfg.fields.text(Contact.PROPERTY_PHOTO_ALT_TEXT).label("Image alt text")
                                                                        .description("Please define an image alt text")),
                                                        cfg.forms
                                                                .tab("Address")
                                                                .label("Address")
                                                                .fields(cfg.fields.text(Contact.PROPERTY_ORGANIZATION_NAME).label("Organization name")
                                                                        .description("Enter the organization name").required(),
                                                                        cfg.fields.text(Contact.PROPERTY_ORGANIZATION_UNIT_NAME).label("Organization unit name")
                                                                                .description("Enter the organization unit name"),
                                                                        cfg.fields.text(Contact.PROPERTY_STREET_ADDRESS).label("Street address")
                                                                                .description("Please enter the company street address").rows(2),
                                                                        cfg.fields.text(Contact.PROPERTY_ZIP_CODE).type(PropertyType.TYPENAME_LONG).label("ZIP code")
                                                                                .description("Please enter the zip code (only digits)"),
                                                                        cfg.fields.text(Contact.PROPERTY_CITY).label("City")
                                                                                .description("Please enter the company city  "),
                                                                        cfg.fields.text(Contact.PROPERTY_COUNTRY).label("Country")
                                                                                .description("Please enter the company country")),
                                                        cfg.forms
                                                                .tab("Contact details")
                                                                .label("Contact details")
                                                                .fields(cfg.fields.text(Contact.PROPERTY_OFFICE_PHONE_NR).label("Office phone")
                                                                        .description("Please enter the office phone number"),
                                                                        cfg.fields.text(Contact.PROPERTY_OFFICE_FAX_NR).label("Office fax nr.")
                                                                                .description("Please enter the office fax number"),
                                                                        cfg.fields.text(Contact.PROPERTY_MOBILE_PHONE_NR).label("Mobile phone")
                                                                                .description("Please enter the mobile phone number"),
                                                                        cfg.fields.text(Contact.PROPERTY_EMAIL).label("E-Mail address")
                                                                                .description("Please enter the email address").required()
                                                                                .validator(cfg.validators.email().errorMessage("validation.message.non.valid.email")),
                                                                        cfg.fields.text(Contact.PROPERTY_WEBSITE).label("Website")
                                                                                .description("Please enter the Website")))
                                        )
                                        .actions("commit", "cancel")
                                )
                                .exec()
                );
    }

    @Dialog("ui-contacts-app:folder")
    public DialogDefinition folderDialog() {

        ConfiguredDialogDefinition dialog = new ConfiguredDialogDefinition();
        dialog.setLabel("Edit folder");
        dialog.setDescription("Rename folder");

        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        dialog.setForm(form);

        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("folder");
        tab.setLabel("Folder");
        form.addTab(tab);

        TextFieldDefinition name = new TextFieldDefinition();
        name.setName(ModelConstants.JCR_NAME);
        name.setLabel("Name");
        name.setDescription("Folder name");
        tab.addField(name);

        SaveDialogActionDefinition commit = new SaveDialogActionDefinition();
        commit.setName("commit");
        commit.setLabel("save changes");
        dialog.addAction(commit);

        CancelDialogActionDefinition cancel = new CancelDialogActionDefinition();
        cancel.setName("cancel");
        cancel.setLabel("cancel");
        dialog.addAction(cancel);

        return dialog;
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        CodeConfigurationUtil.registerAnnotatedAppProviders(appDescriptorRegistry, this);
        CodeConfigurationUtil.registerAnnotatedDialogProviders(dialogDefinitionRegistry, this);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        // Do nothing.
    }
}
