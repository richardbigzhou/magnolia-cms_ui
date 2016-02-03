/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
package info.magnolia.ui.vaadin.applauncher;

import info.magnolia.ui.vaadin.gwt.client.applauncher.connector.AppLauncherState;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppGroup;
import info.magnolia.ui.vaadin.gwt.client.applauncher.shared.AppTile;

import java.util.List;

import com.vaadin.ui.AbstractComponent;

/**
 * Server side of AppLauncher.
 */
public class AppLauncher extends AbstractComponent {

    public AppLauncher() {
        super();
        setImmediate(true);
        addStyleName("v-app-launcher");
    }

    public void addAppGroup(String name, String caption, String color, boolean isPermanent, boolean clientGroup) {
        getState().appGroups.put(name, new AppGroup(name, caption, color, isPermanent, clientGroup));
        getState().groupsOrder.add(name);
    }

    @Override
    protected AppLauncherState getState() {
        return (AppLauncherState) super.getState();
    }

    public void addAppTile(String name, String caption, String icon, String groupName) {
        final AppGroup group = getState().appGroups.get(groupName);
        if (group != null) {
            group.addAppTile(new AppTile(name, caption, icon));
        }
    }

    public void clear() {
        getState().groupsOrder.clear();
        getState().appGroups.clear();
        getState().runningApps.clear();
    }

    public void setAppActive(String appName, boolean isActive) {
        final List<String> runningApps = getState().runningApps;
        if (isActive && !runningApps.contains(appName)) {
            runningApps.add(appName);
        } else {
            runningApps.remove(appName);
        }
    }
}
