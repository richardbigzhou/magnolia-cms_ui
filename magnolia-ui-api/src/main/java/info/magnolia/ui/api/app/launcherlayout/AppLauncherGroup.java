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

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a group of apps displayed in the app launcher including display properties of the group.
 *
 * @see AppLauncherLayout
 * @see AppLauncherGroupEntry
 * @see AppLauncherLayoutManager
 */
public class AppLauncherGroup {

    private String name;

    private String label;

    private boolean permanent;

    private boolean clientGroup;

    private String color;

    private List<AppLauncherGroupEntry> apps;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public boolean isClientGroup() {
        return clientGroup;
    }

    public void setClientGroup(boolean clientGroup) {
        this.clientGroup = clientGroup;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<AppLauncherGroupEntry> getApps() {
        if (apps == null) {
            apps = new ArrayList<AppLauncherGroupEntry>();
        }
        return apps;
    }

    public void setApps(List<AppLauncherGroupEntry> apps) {
        this.apps = apps;
    }

    public void addApp(AppLauncherGroupEntry entry) {
        getApps().add(entry);
    }
}
