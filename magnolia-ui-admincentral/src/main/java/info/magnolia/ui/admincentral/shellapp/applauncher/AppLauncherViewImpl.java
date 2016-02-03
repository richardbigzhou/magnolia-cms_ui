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
package info.magnolia.ui.admincentral.shellapp.applauncher;

import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherGroup;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherGroupEntry;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayout;
import info.magnolia.ui.vaadin.applauncher.AppLauncher;

import javax.inject.Inject;

import com.vaadin.ui.Component;

/**
 * Default view implementation for the app launcher. It handles the following AppLifecycleEvent:
 * <ul>
 * <li>StopApp: Remove the runningApp Icon
 * <li>StartApp: Add the runningApp Icon
 * <li>RegisterApp: Create a new App and Group Icon/Section
 * <li>UnregisterApp: Remove the App and Group Icon/Section
 * </ul>
 */
public class AppLauncherViewImpl implements AppLauncherView {

    private final AppLauncher appLauncher = new AppLauncher();

    private Presenter presenter;

    @Inject
    public AppLauncherViewImpl() {
    }

    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Component asVaadinComponent() {
        return appLauncher;
    }

    @Override
    public void activateButton(boolean activate, String appName) {
        appLauncher.setAppActive(appName, activate);
    }

    @Override
    public void clearView() {
        appLauncher.clear();
    }

    @Override
    public void registerApp(AppLauncherLayout layout) {
        for (AppLauncherGroup group : layout.getGroups()) {
            appLauncher.addAppGroup(group.getName(), group.getLabel(), group.getColor(), group.isPermanent(), group.isClientGroup());
            for (AppLauncherGroupEntry entry : group.getApps()) {
                AppDescriptor descriptor = entry.getAppDescriptor();
                appLauncher.addAppTile(descriptor.getName(), descriptor.getLabel(), descriptor.getIcon(), group.getName());
            }
        }
    }
}
