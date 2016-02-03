/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.admincentral.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.OrderNodeAfterTask;
import info.magnolia.module.delta.PartialBootstrapTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.RenameNodesTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * VersionHandler for the Admin Central module.
 */
public class AdmincentralModuleVersionHandler extends DefaultModuleVersionHandler {

    public AdmincentralModuleVersionHandler() {
        register(DeltaBuilder.update("5.0.1", "")
                .addTask(new NodeExistsDelegateTask("Remove dialog links Node", "Remove dialog definition in ui-admincentral/dialogs/links", RepositoryConstants.CONFIG, "/modules/ui-admincentral/dialogs/link",
                        new RemoveNodeTask("Remove dialog links Node", "Remove dialog definition in ui-admincentral/dialogs/links", RepositoryConstants.CONFIG, "/modules/ui-admincentral/dialogs/link")))

                .addTask(new PartialBootstrapTask("Add editProperty dialog", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.dialogs.xml", "/dialogs/editProperty"))
                .addTask(new PartialBootstrapTask("Add editProperty action to Configuration app", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/editProperty"))

                .addTask(new PartialBootstrapTask("Add renameItem dialog", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.dialogs.xml", "/dialogs/renameItem"))
                .addTask(new PartialBootstrapTask("Add rename action to Configuration app", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/rename"))

                        // Update actionbars
                .addTask(new NodeExistsDelegateTask("Remove duplicateActions section from Configuration app.", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/duplicateActions",
                        new RemoveNodeTask("Remove duplicateActions section from Configuration app", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/duplicateActions")))
                .addTask(new PartialBootstrapTask("Add editActions section to Configuration app", "Adds editProperty, rename, and duplicate actions.", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actionbar/sections/folders/groups/editActions"))
                .addTask(new OrderNodeAfterTask("Move editActions section after addingActions", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar/sections/folders/groups/editActions", "addingActions"))

                        // JCR App should extend Configuration App.
                .addTask(new NodeExistsDelegateTask("Remove websiteJcrBrowser App subApps.", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/websiteJcrBrowser/subApps",
                        new RemoveNodeTask("Remove websiteJcrBrowser App subApps.", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/websiteJcrBrowser/subApps")))
                .addTask(new PartialBootstrapTask("Add updated websiteJcrBrowser App subApps.", "It now extends Configuration app.", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.websiteJcrBrowser.xml", "/websiteJcrBrowser/subApps"))

        );

        register(DeltaBuilder.update("5.0.2", "")
                // new action for confirmation
                .addTask(new PartialBootstrapTask("Add new confirmation action definition", "", "/mgnl-bootstrap/ui-admincentral/config.modules.ui-admincentral.apps.configuration.xml", "/configuration/subApps/browser/actions/confirmDeletion"))

                .addTask(new NodeExistsDelegateTask("Remove action availability from delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete/availability",
                        new RemoveNodeTask("", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete/availability")))
                .addTask(new PropertyExistsDelegateTask("Remove label for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "label",
                        new RemovePropertyTask("Remove label for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "label")))
                .addTask(new PropertyExistsDelegateTask("Remove icon for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "icon",
                        new RemovePropertyTask("Remove icon for delete action", "", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actions/delete", "icon")))

                        // update actionbar for confirmation
                .addTask(new NodeExistsDelegateTask("Update actionbar configuration", "Rename action mapping to new confirmation action", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar",
                        new RenameNodesTask("Rename action bar items", "Rename delete to confirmDeletion", RepositoryConstants.CONFIG, "/modules/ui-admincentral/apps/configuration/subApps/browser/actionbar", "delete", "confirmDeletion", NodeTypes.ContentNode.NAME)))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> list = new ArrayList<Task>();

        list.add(new IsModuleInstalledOrRegistered(
                "Replace login security pattern",
                "Replaces old login security pattern '/.resources/loginForm' (if present) with the new one '/.resources/defaultLoginForm'.",
                "adminInterface",
                new CheckAndModifyPropertyValueTask(
                        "",
                        "",
                        RepositoryConstants.CONFIG,
                        "/server/filters/uriSecurity/bypasses/login",
                        "pattern",
                        "/.resources/loginForm",
                        "/.resources/defaultLoginForm")));

        return list;
    }
}
