/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.pages.setup;

import info.magnolia.cms.core.Content;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeCheckDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.RenameNodesTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.RepositoryException;

/**
 * Version handler for the pages app module.
 */
public class PagesModuleVersionHandler extends DefaultModuleVersionHandler {

    public PagesModuleVersionHandler() {
        super();
        register(DeltaBuilder.update("5.0", "Configuration update for Magnolia 5.0")
                .addTask(new IsModuleInstalledOrRegistered("", "", "adminInterface",
                        new BootstrapConditionally("Bootstrap activation commands", "Bootstraps the default activation and deletion commands which no longer reside under adminInterface.", "config.modules.pages.commands.xml"))));
        register(DeltaBuilder.update("5.0.1", "")
                .addTask(new NodeExistsDelegateTask("Remove dialog links Node", "Remove dialog definition in pages/dialogs/links", RepositoryConstants.CONFIG, "/modules/pages/dialogs/link",
                        new RemoveNodeTask("Remove dialog links Node", "Remove dialog definition in pages/dialogs/links", RepositoryConstants.CONFIG, "/modules/pages/dialogs/link")))
                .addTask(new NodeExistsDelegateTask("Add title to CreatePage dialog", "", RepositoryConstants.CONFIG, "/modules/pages/dialogs/createPage/form",
                        new NewPropertyTask("Add title to CreatePage dialog", "", RepositoryConstants.CONFIG, "/modules/pages/dialogs/createPage/form", "label", "pages.dialog.add_page"))));

        register(DeltaBuilder.update("5.0.2", "")
                // new action for confirmation
                .addTask(new PartialBootstrapTask("Add new confirmation action definition", "", "/mgnl-bootstrap/pages/config.modules.pages.apps.pages.xml", "/pages/subApps/browser/actions/confirmDeletion"))

                .addTask(new NodeExistsDelegateTask("Remove action availability from delete action", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/delete/availability",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/delete/availability")))
                .addTask(new PropertyExistsDelegateTask("Remove label for delete action", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/delete", "label",
                        new RemovePropertyTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/delete", "label")))
                .addTask(new PropertyExistsDelegateTask("Remove icon for delete action", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/delete", "icon",
                        new RemovePropertyTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/delete", "icon")))

                        // update actionbar for confirmation
                .addTask(new NodeExistsDelegateTask("Update actionbar configuration", "Rename action mapping to new confirmation action", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actionbar",
                        new RenameNodesTask("Rename action bar items", "Rename delete to confirmDeletion", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actionbar", "delete", "confirmDeletion", NodeTypes.ContentNode.NAME)))
        );

        register(DeltaBuilder.update("5.1", "")
                .addTask(new NodeExistsDelegateTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/confirmDeletion",
                        new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/confirmDeletion/availability", "multiple", true)))

                        // update activation actions to use versioned catalog
                .addTask(new PropertyExistsDelegateTask("update activate action to use versioned command chain", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/activate", "catalog",
                        new CheckAndModifyPropertyValueTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/activate", "catalog", "website", "versioned")))
                .addTask(new PropertyExistsDelegateTask("update deactivate action to use versioned command chain", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/deactivate", "catalog",
                        new CheckAndModifyPropertyValueTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/deactivate", "catalog", "website", "versioned")))
                .addTask(new PropertyExistsDelegateTask("update activateRecursive action to use versioned command chain", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/activateRecursive", "catalog",
                        new CheckAndModifyPropertyValueTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/activateRecursive", "catalog", "website", "versioned")))
                .addTask(new PropertyExistsDelegateTask("update activateDeletion action to use versioned command chain", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/activateDeletion", "catalog",
                        new CheckAndModifyPropertyValueTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/activateDeletion", "catalog", "website", "versioned")))

                        // add show versions action
                .addTask(new PartialBootstrapTask("Bootstrap showVersions action", "", "/mgnl-bootstrap/pages/config.modules.pages.apps.pages.xml", "/pages/subApps/browser/actions/showVersions"))
                .addTask(new PartialBootstrapTask("Bootstrap actionbar section group for versionActions", "", "/mgnl-bootstrap/pages/config.modules.pages.apps.pages.xml", "/pages/subApps/browser/actionbar/sections/pageActions/groups/versionActions"))

                        // cleanup pages commands
                .addTask(new NodeExistsDelegateTask("remove the activate command chain from pages app", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/activate",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/activate")))
                .addTask(new NodeExistsDelegateTask("remove the deactivate command chain from pages app", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/deactivate",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/deactivate")))
                .addTask(new NodeCheckDelegateTask("Remove website commands from pages app if empty", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website", "",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website"), null) {
                    @Override
                    protected boolean checkNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
                        return !node.hasChildren();
                    }
                })
                .addTask(new NodeCheckDelegateTask("Remove commands from pages app if empty", "", RepositoryConstants.CONFIG, "/modules/pages/commands", "",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/commands"), null) {
                    @Override
                    protected boolean checkNode(Content node, InstallContext ctx) throws RepositoryException, TaskExecutionException {
                        return !node.hasChildren();
                    }
                })
        );



    }


}
