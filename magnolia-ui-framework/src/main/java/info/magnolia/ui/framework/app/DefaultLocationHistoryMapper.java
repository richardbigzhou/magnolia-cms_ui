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
package info.magnolia.ui.framework.app;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayout;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherLayoutManager;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationHistoryMapper;

/**
 * LocationHistoryMapper that creates locations for all apps and shell apps.
 */
public class DefaultLocationHistoryMapper implements LocationHistoryMapper {

    private final User user;
    private final AppLauncherLayoutManager appLauncherLayoutManager;

    public DefaultLocationHistoryMapper(final AppLauncherLayoutManager appLauncherLayoutManager, final Context context) {
        this.appLauncherLayoutManager = appLauncherLayoutManager;
        this.user = context.getUser();
    }

    @Override
    public Location getLocation(String fragment) {

        String appType = DefaultLocation.extractAppType(fragment);
        String appName = DefaultLocation.extractAppName(fragment);
        String subAppId = DefaultLocation.extractSubAppId(fragment);
        String parameter = DefaultLocation.extractParameter(fragment);

        if (!supported(appType, appName, subAppId, parameter)) {
            return null;
        }

        return new DefaultLocation(appType, appName, subAppId, parameter);
    }

    @Override
    public String getFragment(Location location) {

        if (!supported(location.getAppType(), location.getAppName(), location.getSubAppId(), location.getParameter())) {
            return null;
        }

        return location.toString();
    }

    private boolean supported(String appType, String appName, String subAppId, String parameter) {

        if (appType.equals(Location.LOCATION_TYPE_SHELL_APP) && (appName.equals("applauncher") || appName.equals("pulse") || appName.equals("favorite"))) {
            return true;
        }

        if (!appType.equals(Location.LOCATION_TYPE_APP)) {
            return false;
        }

        AppLauncherLayout appLauncherLayout = appLauncherLayoutManager.getLayoutForUser(user);
        return appLauncherLayout.containsApp(appName);
    }
}
