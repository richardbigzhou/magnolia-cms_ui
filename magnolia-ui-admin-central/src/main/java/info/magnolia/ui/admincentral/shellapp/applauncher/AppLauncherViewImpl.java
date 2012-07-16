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
package info.magnolia.ui.admincentral.shellapp.applauncher;

import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroup;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherGroupEntry;
import info.magnolia.ui.framework.app.launcherlayout.AppLauncherLayout;
import info.magnolia.ui.vaadin.integration.widget.AppLauncher;
import info.magnolia.ui.vaadin.integration.widget.AppLauncher.AppActivatedEvent;
import info.magnolia.ui.vaadin.integration.widget.AppLauncher.AppActivationListener;

import com.vaadin.ui.Component;


/**
 * Default view implementation for the app launcher.
 * Handle the following AppLifecycleEvent:
 *   StopApp : Remove the runningApp Icon
 *   StartApp: Add the runningApp Icon
 *   RegisterApp: Create a new App & Group Icon/Section
 *   UnregisterApp: Remove the App & Group Icon/Section
 *
 */
@SuppressWarnings("serial")
public class AppLauncherViewImpl implements AppLauncherView {

    private Presenter presenter;

    private AppLauncher appLauncher = new AppLauncher();
    
    public AppLauncherViewImpl() {
        appLauncher.setHeight("100%");
        appLauncher.setWidth("720px");
        appLauncher.addAppActivationListener(new AppActivationListener() {
            @Override
            public void onAppActivated(AppActivatedEvent event) {
                presenter.onAppInvoked(event.getAppName());
            }
        });
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
            appLauncher.addAppSection(group.getLabel(), group.getColor(), group.isPermanent());
            for (AppLauncherGroupEntry entry : group.getApps()) {
                AppDescriptor descriptor = entry.getAppDescriptor();
                appLauncher.addAppTile(descriptor.getName(), descriptor.getIcon(), descriptor.getCategoryName());
            }
        }
    }
}
