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
package info.magnolia.ui.admincentral.framework;

import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherActivity;
import info.magnolia.ui.admincentral.shellapp.applauncher.AppLauncherPlace;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesActivity;
import info.magnolia.ui.admincentral.shellapp.favorites.FavoritesPlace;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseActivity;
import info.magnolia.ui.admincentral.shellapp.pulse.PulsePlace;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.place.Place;

import java.util.HashMap;
import java.util.Map;

/**
 * ShellAppActivityMapper.
 *
 * @version $Id$
 */
@SuppressWarnings("serial")
public class ShellAppActivityMapper implements ActivityMapper {

    private ComponentProvider componentProvider;

    private final Map<Class<? extends Place>, Activity> activityToPlace = new HashMap<Class<? extends Place>, Activity>();

    public ShellAppActivityMapper(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
        activityToPlace.put(AppLauncherPlace.class, componentProvider.newInstance(AppLauncherActivity.class));
        activityToPlace.put(PulsePlace.class, componentProvider.newInstance(PulseActivity.class));
        activityToPlace.put(FavoritesPlace.class, componentProvider.newInstance(FavoritesActivity.class));
    }

    @Override
    public Activity getActivity(final Place place) {
        final Activity activity = activityToPlace.get(place.getClass());
        return activity == null ? null : activity;
    }
}
