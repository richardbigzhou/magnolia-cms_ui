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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;

/**
 * Activity manager that invokes methods on the activity annoted with {@link PlaceStateHandler} to notify it about the
 * place causing the activity to start and place changes that map to it when its the current activity.
 *
 * @version $Id$
 */
public class PlaceChangeNotifyingActivityManager extends ActivityManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PlaceChangeNotifyingActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(mapper, eventBus);
    }

    @Override
    protected void onPlaceChangeToCurrentActivity(Activity currentActivity, Place newPlace) {
        invokePlaceStateHandlerMethods(currentActivity, newPlace);
    }

    @Override
    protected void beforeActivityStarts(Activity currentActivity, Place newPlace) {
        invokePlaceStateHandlerMethods(currentActivity, newPlace);
    }

    private void invokePlaceStateHandlerMethods(Activity activity, Place place) {

        // Calls all methods annotated with @PlaceStateHandler and passes the place as argument
        final Method[] methods = activity.getClass().getMethods();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(PlaceStateHandler.class)) {
                final Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && params[0].equals(place.getClass())) {
                    try {
                        method.invoke(activity, place);
                    } catch (IllegalArgumentException e) {
                        logger.error("Unable to invoke method annotated with @PlaceStateHandler", e);
                    } catch (IllegalAccessException e) {
                        logger.error("Unable to invoke method annotated with @PlaceStateHandler", e);
                    } catch (InvocationTargetException e) {
                        logger.error("Unable to invoke method annotated with @PlaceStateHandler", e);
                    }
                }
            }
        }
    }
}
