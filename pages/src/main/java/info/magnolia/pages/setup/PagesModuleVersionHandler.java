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
import info.magnolia.repository.RepositoryConstants;

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

                // remove and bootstrap new deactivation command
                .addTask(new NodeExistsDelegateTask("Remove deactivation command", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/deactivate",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/pages/commands/website/deactivate")))
                .addTask(new PartialBootstrapTask("Bootstrap new deactivation command.", "", "/mgnl-bootstrap/pages/config.modules.pages.commands.xml", "/commands/website/deactivate"))

        );


    }

}
