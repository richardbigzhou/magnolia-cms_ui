/**
 * This file Copyright (c) 2016 Magnolia International
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
package info.magnolia.about.app.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.ui.admincentral.setup.ConvertAclToAppPermissionTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Install tasks for the About app app.
 */
public class AboutAppModuleVersionHandler extends DefaultModuleVersionHandler {

    static final String CONFIG_INFO = "configInfo";
    static final String APP_LAUNCHER_APPS_PATH = "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/";

    public AboutAppModuleVersionHandler() {
        register(DeltaBuilder.update("5.4.7", "")
                .addTask(new RemoveNodeTask("Remove old Config info app from app launcher", APP_LAUNCHER_APPS_PATH + CONFIG_INFO))
                .addTask(new BootstrapSingleResource("Configure about app", "", "/mgnl-bootstrap/about-app/config.modules.about-app.apps.about.xml"))
        );

        register(DeltaBuilder.update("5.4.8", "")
                        .addTask(new ConvertAclToAppPermissionTask("Convert permissions for configInfo app", "Convert ACL permissions for old 'configInfo' menu to new 'about-app' permission",
                                "/.magnolia/pages/configuration", "/modules/about-app/apps/about", false))
                );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new RemoveNodeTask("Remove old Config info app from app launcher", APP_LAUNCHER_APPS_PATH + CONFIG_INFO));
        return tasks;
    }
}
