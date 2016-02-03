/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import static info.magnolia.nodebuilder.Ops.*;

import info.magnolia.i18nsystem.setup.RemoveHardcodedI18nPropertiesFromSubappsTask;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.NewPropertyTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.RenameNodesTask;
import info.magnolia.nodebuilder.task.ErrorHandling;
import info.magnolia.nodebuilder.task.NodeBuilderTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.contentapp.availability.IsNotVersionedDetailLocationRule;
import info.magnolia.ui.contentapp.browser.action.ShowVersionsActionDefinition;

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
                        new RenameNodesTask("Rename action bar items", "Rename delete to confirmDeletion", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actionbar", "delete", "confirmDeletion", NodeTypes.ContentNode.NAME))));

        register(DeltaBuilder.update("5.1", "")
                .addTask(new NodeExistsDelegateTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/confirmDeletion",
                        new NewPropertyTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/confirmDeletion/availability", "multiple", true)))

                // add show versions action
                // if module diff is installed, this task will create a second node showVersions
                // we'll handle the renaming of the correct node in module diff
                .addTask(new NodeBuilderTask("Create showVersions action", "", ErrorHandling.logging, RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions",
                        addNode("showVersions", NodeTypes.ContentNode.NAME).then(
                                addProperty("class", ShowVersionsActionDefinition.class.getName()),
                                addProperty("icon", "icon-show-versions"),
                                addNode("availability", NodeTypes.ContentNode.NAME).then(
                                        addProperty("ruleClass", "info.magnolia.ui.api.availability.HasVersionsRule")
                                )
                        )
                ))
                // bootstrap versionActions to action bar if it doesn't exists already
                .addTask(new NodeExistsDelegateTask("Bootstrap actionbar section group for versionActions", "", RepositoryConstants.CONFIG, "/pages/subApps/browser/actionbar/sections/pageActions/groups/versionActions", null,
                        new PartialBootstrapTask("", "", "/mgnl-bootstrap/pages/config.modules.pages.apps.pages.xml", "/pages/subApps/browser/actionbar/sections/pageActions/groups/versionActions")
                ))

                // Remove hardcoded i18n properties, e.g. label, description, etc.
                .addTask(new RemoveHardcodedI18nPropertiesFromSubappsTask("pages"))

                // cleanup pages commands
                .addTask(new NodeExistsDelegateTask("remove the activate command chain from pages app", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/activate",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/activate")))
                .addTask(new NodeExistsDelegateTask("remove the deactivate command chain from pages app", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/deactivate",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/deactivate")))

                .addTask(new PartialBootstrapTask("Bootstrap new activate commands to website catalog.", "", "/mgnl-bootstrap/pages/config.modules.pages.commands.xml", "/commands/website/activate"))
                .addTask(new PartialBootstrapTask("Bootstrap new deactivate commands to website catalog.", "", "/mgnl-bootstrap/pages/config.modules.pages.commands.xml", "/commands/website/deactivate"))

                // Add availability rules to detail view actions
                .addTask(new NodeBuilderTask("Add availability rule to edit action", "", ErrorHandling.logging, RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/detail/actions/edit",
                        addNode("availability", NodeTypes.ContentNode.NAME).then(
                                addProperty("ruleClass", IsNotVersionedDetailLocationRule.class.getName())
                        )
                ))
                .addTask(new NodeBuilderTask("Add availability rule to activate action", "", ErrorHandling.logging, RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/detail/actions/activate",
                        addNode("availability", NodeTypes.ContentNode.NAME).then(
                                addProperty("ruleClass", IsNotVersionedDetailLocationRule.class.getName())
                        )
                ))
                .addTask(new NodeBuilderTask("Add availability rule to deactivate action", "", ErrorHandling.logging, RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/detail/actions/deactivate",
                        addNode("availability", NodeTypes.ContentNode.NAME).then(
                                addProperty("ruleClass", IsNotVersionedDetailLocationRule.class.getName())
                        )
                ))

                .addTask(new PartialBootstrapTask("Bootstrap move action in Pages app", "",
                        "/mgnl-bootstrap/pages/config.modules.pages.apps.pages.xml", "/pages/subApps/browser/actions/move"))
                .addTask(new PartialBootstrapTask("Bootstrap move action to Pages app actionbar", "Adds action move to folder/editingActions section in actionbar.",
                        "/mgnl-bootstrap/pages/config.modules.pages.apps.pages.xml", "/pages/subApps/browser/actionbar/sections/pageActions/groups/editingActions/items/move"))
        );
        
        register(DeltaBuilder.update("5.1.1", "")
                .addTask(new NodeExistsDelegateTask("Add root availability to import", "Add root availability to import action in Pages app", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/import/availability",
                        new NewPropertyTask("Add root availability to import", "Add root availability to import action in Pages app", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/subApps/browser/actions/import/availability", "root", true)))
        );

    }

}
