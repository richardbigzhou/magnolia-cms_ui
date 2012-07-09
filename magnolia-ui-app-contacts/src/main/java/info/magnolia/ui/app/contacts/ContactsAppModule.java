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
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.app.content.ConfiguredContentAppDescriptor;
import info.magnolia.ui.admincentral.column.ReadOnlyPropertyColumnDefinition;
import info.magnolia.ui.admincentral.dialog.action.CreateDialogActionDefinition;
import info.magnolia.ui.admincentral.dialog.action.EditDialogActionDefinition;
import info.magnolia.ui.admincentral.tree.action.DeleteItemActionDefinition;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.registry.AppDescriptorProvider;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ConfiguredActionbarSectionDefinition;
import info.magnolia.ui.model.column.definition.MetaDataColumnDefinition;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredItemTypeDefinition;
import info.magnolia.ui.model.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.model.workbench.definition.ItemTypeDefinition;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;


/**
 * Module lifecycle handler for the contacts app, that may register app and its configuration.
 */
@SuppressWarnings("serial")
public class ContactsAppModule implements ModuleLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ContactsAppModule.class);

    private final AppDescriptorRegistry appRegistry;

    @Inject
    public ContactsAppModule(AppDescriptorRegistry appRegistry) {
        this.appRegistry = appRegistry;
    }

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        // Uncomment to register definition through code.
        // Beware, you may have to remove configured "contacts" app in module config.
        buildAndRegisterApp();
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    /**
     * Builds a dummy app descriptor and adds it to the app registry.
     */
    private void buildAndRegisterApp() {
        try {
            appRegistry.register(new AppDescriptorProvider() {

                @Override
                public String getName() {
                    return "contacts";
                }

                @Override
                public AppDescriptor getAppDescriptor() throws RegistrationException {
                    return new ConfiguredContentAppDescriptor() {

                        {
                            this.setName("contacts");
                            this.setCategoryName("MANAGE");
                            this.setAppClass(ContactsApp.class);
                            this.setLabel("Contacts");
                            this.setIcon("img/icon-app-default.png");
                            this.setWorkbench(new ConfiguredWorkbenchDefinition() {

                                {
                                    setName("contactsWorkbenchByCode");
                                    setWorkspace("contacts");
                                    setPath("/");

                                    // workbench item types
                                    setItemTypes(new ArrayList<ItemTypeDefinition>() {

                                        {
                                            this.add(new ConfiguredItemTypeDefinition() {

                                                {
                                                    setItemType("mgnl:folder");
                                                    setIcon("/.resources/icons/16/folders.gif");
                                                }
                                            });
                                            this.add(new ConfiguredItemTypeDefinition() {

                                                {
                                                    setItemType("mgnl:contact");
                                                    setIcon("/.resources/icons/16/pawn_glass_yellow.gif");
                                                }
                                            });
                                        }
                                    });

                                    // workbench columns
                                    addColumn(new ReadOnlyPropertyColumnDefinition() {

                                        {
                                            setName("firstName");
                                            setPropertyName("firstName");
                                            setLabel("First name");
                                        }
                                    });
                                    addColumn(new ReadOnlyPropertyColumnDefinition() {

                                        {
                                            setName("lastName");
                                            setPropertyName("lastName");
                                            setLabel("Last name");
                                        }
                                    });
                                    addColumn(new ReadOnlyPropertyColumnDefinition() {

                                        {
                                            setName("email");
                                            setPropertyName("email");
                                            setLabel("Email");
                                        }
                                    });
                                    addColumn(new StatusColumnDefinition() {

                                        {
                                            setName("status");
                                            setLabel("Status");
                                        }
                                    });
                                    addColumn(new MetaDataColumnDefinition() {

                                        {
                                            setName("moddate");
                                            setPropertyName("MetaData/mgnl:lastmodified");
                                            setLabel("Mod. date");
                                        }
                                    });

                                    // workbench action bar
                                    setActionbar(new ConfiguredActionbarDefinition() {

                                        {
                                            this.setName("contactsActionbarByCode");

                                            this.addSection(new ConfiguredActionbarSectionDefinition() {

                                                {
                                                    this.setName("Actions");
                                                    this.setLabel("Actions");

                                                    this.addGroup(new ConfiguredActionbarGroupDefinition() {

                                                        {
                                                            this.setName("0");
                                                            this.addItem(new ConfiguredActionbarItemDefinition() {

                                                                {
                                                                    this.setName("newContact");
                                                                    this.setLabel("New contact");
                                                                    this.setIcon("img/actionbar-icons/icon-action-add-tablet.png");
                                                                    this.setActionDefinition(new CreateDialogActionDefinition() {

                                                                        {
                                                                            this.setDialogName("ui-contacts-app:contact");
                                                                            this.setNodeType("mgnl:contact");
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                            this.addItem(new ConfiguredActionbarItemDefinition() {

                                                                {
                                                                    this.setName("deleteContact");
                                                                    this.setLabel("Delete contact");
                                                                    this.setIcon("img/actionbar-icons/icon-action-delete-tablet.png");
                                                                    this.setActionDefinition(new DeleteItemActionDefinition());
                                                                }
                                                            });
                                                        }
                                                    });

                                                    this.addGroup(new ConfiguredActionbarGroupDefinition() {

                                                        {
                                                            this.setName("1");
                                                            this.addItem(new ConfiguredActionbarItemDefinition() {

                                                                {
                                                                    this.setName("editContact");
                                                                    this.setLabel("Edit contact");
                                                                    this.setIcon("img/actionbar-icons/icon-action-edit-tablet.png");
                                                                    this.setActionDefinition(new EditDialogActionDefinition() {

                                                                        {
                                                                            this.setDialogName("ui-contacts-app:contact");
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    };
                }
            });
        } catch (RegistrationException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
