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
package info.magnolia.ui.admincentral.module;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.app.launcherlayout.definition.AppLauncherLayoutDefinition;
import info.magnolia.ui.framework.app.registry.ConfiguredAppDescriptorManager;
import info.magnolia.ui.model.dialog.registry.ConfiguredDialogDefinitionManager;

import javax.inject.Inject;

/**
 * Registers the observed managers: {@link ConfiguredAppDescriptorManager}.
 * 
 */
public class AdminCentralModule implements ModuleLifecycle {

    private AppLauncherLayoutDefinition appLauncherLayout;
    private AppLauncherLayoutManager appLauncherLayoutManager;
    private ConfiguredAppDescriptorManager configuredAppDescriptorManager;
    private ConfiguredDialogDefinitionManager configuredDialogDefinitionManager;

    @Inject
    public AdminCentralModule(AppLauncherLayoutManager appLauncherLayoutManager, ConfiguredAppDescriptorManager configuredAppDescriptorManager, ConfiguredDialogDefinitionManager configuredDialogDefinitionManager) {
        this.appLauncherLayoutManager = appLauncherLayoutManager;
        this.configuredAppDescriptorManager = configuredAppDescriptorManager;
        this.configuredDialogDefinitionManager = configuredDialogDefinitionManager;
    }

    @Override
    public void start(ModuleLifecycleContext context) {
        if (context.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {
            this.configuredAppDescriptorManager.start();
            this.configuredDialogDefinitionManager.start();
            appLauncherLayoutManager.setLayout(getAppLauncherLayout());
        }
        if (context.getPhase() == ModuleLifecycleContext.PHASE_MODULE_RESTART) {
            appLauncherLayoutManager.setLayout(getAppLauncherLayout());
        }
    }

    @Override
    public void stop(ModuleLifecycleContext context) {
    }

    public AppLauncherLayoutDefinition getAppLauncherLayout() {
        return appLauncherLayout;
    }

    public void setAppLauncherLayout(AppLauncherLayoutDefinition appLauncherLayout) {
        this.appLauncherLayout = appLauncherLayout;
    }
}
