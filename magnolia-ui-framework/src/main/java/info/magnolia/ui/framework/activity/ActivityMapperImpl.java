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
package info.magnolia.ui.framework.activity;

import java.util.HashMap;
import java.util.Map;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.place.Place;

/**
 * Activity Mapper that instantiates activities using a ComponentProvider to allow dependency injection on the activity
 * instances and optionally keeps the activities long living, meaning it returns the same instance every time its
 * queried with the same place.
 *
 * @version $Id$
 */
public class ActivityMapperImpl implements ActivityMapper {

    private ComponentProvider componentProvider;

    private boolean longLivingActivities = false;
    private final Map<Class<? extends Place>, Class<? extends Activity>> placeToActivityClass = new HashMap<Class<? extends Place>, Class<? extends Activity>>();
    private final Map<Class<? extends Place>, Activity> placeToActivityInstance = new HashMap<Class<? extends Place>, Activity>();

    public ActivityMapperImpl(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    public synchronized void setLongLivingActivities(boolean longLivingActivities) {
        this.longLivingActivities = longLivingActivities;
    }

    public synchronized void addMapping(Class<? extends Place> placeClass, Class<? extends Activity> activityClass) {
        placeToActivityClass.put(placeClass, activityClass);
    }

    @Override
    public synchronized Activity getActivity(Place place) {

        Activity activity = placeToActivityInstance.get(place.getClass());
        if (activity != null) {
            return activity;
        }

        Class<? extends Activity> activityClass = placeToActivityClass.get(place.getClass());
        if (activityClass == null) {
            return null;
        }

        activity = instantiateActivity(activityClass);
        if (longLivingActivities) {
            placeToActivityInstance.put(place.getClass(), activity);
        }
        return activity;
    }

    protected synchronized void removeActivityInstanceForPlace(Class<? extends Place> placeClass) {
        placeToActivityInstance.remove(placeClass);
    }

    protected Activity instantiateActivity(Class<? extends Activity> activityClass) {
        return componentProvider.newInstance(activityClass);
    }
}
