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
package info.magnolia.ui.admincentral;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.ui.admincentral.shellapp.pulse.message.registry.ConfiguredMessageViewDefinitionManager;
import info.magnolia.ui.admincentral.usermenu.definition.UserMenuDefinition;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutDefinition;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;

import javax.inject.Inject;

/**
 * Registers the observed managers: {@link ConfiguredMessageViewDefinitionManager}.
 */
public class AdmincentralModule implements ModuleLifecycle {

    private ConfiguredMessageViewDefinitionManager configuredMessageViewDefinitionManager;
    private UserMenuDefinition userControl;

    private AppLauncherLayoutManager appLauncherLayoutManager;
    private AppLauncherLayoutDefinition appLauncherLayout;


    @Inject
    public AdmincentralModule(ConfiguredMessageViewDefinitionManager configuredMessageViewDefinitionManager, AppLauncherLayoutManager appLauncherLayoutManager) {
        this.configuredMessageViewDefinitionManager = configuredMessageViewDefinitionManager;
        this.appLauncherLayoutManager = appLauncherLayoutManager;
    }

    @Override
    public void start(ModuleLifecycleContext context) {
        if (context.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {
            configuredMessageViewDefinitionManager.start();
        }
        appLauncherLayoutManager.setLayout(getAppLauncherLayout());
    }

    @Override
    public void stop(ModuleLifecycleContext context) {
    }

    public UserMenuDefinition getUserMenu() {
        return userControl;
    }

    public void setUserMenu(UserMenuDefinition userControl) {
        this.userControl = userControl;
    }


    public AppLauncherLayoutDefinition getAppLauncherLayout() {
        return appLauncherLayout;
    }

    public void setAppLauncherLayout(AppLauncherLayoutDefinition appLauncherLayout) {
        this.appLauncherLayout = appLauncherLayout;
    }

}
