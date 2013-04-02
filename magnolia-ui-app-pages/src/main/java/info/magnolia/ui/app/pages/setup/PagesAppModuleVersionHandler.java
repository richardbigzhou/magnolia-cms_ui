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
package info.magnolia.ui.app.pages.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.Collections;
import java.util.List;

/**
 * Version handler for the pages app module.
 */
public class PagesAppModuleVersionHandler extends DefaultModuleVersionHandler {

    public PagesAppModuleVersionHandler() {
        super();
        register(DeltaBuilder.update("5.0", "Configuration update for Magnolia 5.0")
                .addTask(new IsModuleInstalledOrRegistered("", "", "adminInterface",
                        new BootstrapConditionally("Bootstrap activation commands", "Bootstraps the default activation and deletion commands which no longer reside under adminInterface.", "config.modules.ui-pages-app.commands.xml"))));
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        Task reorderSubAppTask = new OrderNodeBeforeTask("Make browser sub app default", "Fix bootstrapping order by putting the correct default sub app.", RepositoryConstants.CONFIG, "/modules/ui-pages-app/apps/pages/subApps/browser", "detail");
        return Collections.singletonList(reorderSubAppTask);
    }

}
