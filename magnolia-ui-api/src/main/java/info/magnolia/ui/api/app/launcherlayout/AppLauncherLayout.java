/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.api.app.launcherlayout;

import info.magnolia.ui.api.app.AppDescriptor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Describes the layout in the app launcher. Provides convenience methods for querying the layout.
 *
 * @see AppLauncherGroup
 * @see AppLauncherGroupEntry
 * @see AppLauncherLayoutManager
 * @see AppLauncherLayoutManager#getLayoutForCurrentUser()
 */
public class AppLauncherLayout {

    private List<AppLauncherGroup> groups;

    public List<AppLauncherGroup> getGroups() {
        if (groups == null) {
            groups = new ArrayList<AppLauncherGroup>();
        }
        return groups;
    }

    public void setGroups(List<AppLauncherGroup> groups) {
        this.groups = groups;
    }

    public void addGroup(AppLauncherGroup group) {
        getGroups().add(group);
    }

    public boolean containsApp(String name) {
        return getAppGroupEntry(name) != null;
    }

    public AppLauncherGroup getGroup(String name) {
        for (AppLauncherGroup group : getGroups()) {
            if (StringUtils.equals(group.getName(), name)) {
                return group;
            }
        }
        return null;
    }

    public AppLauncherGroupEntry getAppGroupEntry(String name) {
        for (AppLauncherGroup group : getGroups()) {
            for (AppLauncherGroupEntry entry : group.getApps()) {
                if (StringUtils.equals(entry.getName(), name)) {
                    return entry;
                }
            }
        }
        return null;
    }

    public AppDescriptor getAppDescriptor(String name) {
        AppLauncherGroupEntry entry = getAppGroupEntry(name);
        return entry != null ? entry.getAppDescriptor() : null;
    }
}
