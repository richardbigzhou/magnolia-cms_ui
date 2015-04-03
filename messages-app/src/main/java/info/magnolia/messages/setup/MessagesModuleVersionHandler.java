/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.messages.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.OrderNodeToFirstPositionTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.admincentral.setup.ConvertAclToAppPermissionTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for the messages app module.
 */
public class MessagesModuleVersionHandler extends DefaultModuleVersionHandler {

    public MessagesModuleVersionHandler() {
        super();

        register(DeltaBuilder.update("5.0", "")
                .addTask(new ConvertAclToAppPermissionTask("Convert permissions for Messages app", "Convert ACL permissions for old 'Messages' menu to new 'messages-app' permission",
                        "/.magnolia/pages/messages", "/modules/messages-app/apps/messages", true)));

        register(DeltaBuilder.update("5.2", "")
                .addTask(new OrderNodeToFirstPositionTask("Reorder Messages in DEV", "This reorders the Messages app as first in the Dev group in the applauncher.", RepositoryConstants.CONFIG, "modules/ui-admincentral/config/appLauncherLayout/groups/dev/apps/messages"))
                .addTask(new PropertyExistsDelegateTask("Remove icon property", "/modules/messages-app/apps/messages", "icon",
                        new RemovePropertyTask("", "/modules/messages-app/apps/messages", "icon")))
                .addTask(new PropertyExistsDelegateTask("Remove icon property", "/modules/messages-app/apps/messages", "label",
                        new RemovePropertyTask("", "/modules/messages-app/apps/messages", "label")))
                );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>();
        tasks.add(new OrderNodeToFirstPositionTask("Reorder Messages in DEV", "This reorders the Messages app as first in the Dev group in the applauncher.", RepositoryConstants.CONFIG, "modules/ui-admincentral/config/appLauncherLayout/groups/dev/apps/messages"));
        return tasks;
    }

}
