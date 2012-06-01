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

import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceChangeEvent;
import info.magnolia.ui.framework.place.PlaceChangeRequestEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Activity manager responsible for the top level apps management (shell apps).
 *
 * @version $Id$
 */
public class ShellAppActivityManager extends ActivityManager {

    private final ActivityMapper mapper;
    
    public ShellAppActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(mapper, eventBus);
        this.mapper = mapper;
    }
    
    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final Place place = event.getNewPlace();
        final Activity activity = mapper.getActivity(place);
        if (activity != null) {
            final Method[] methods = activity.getClass().getMethods();
            for (final Method method : methods) {
                if (method.getAnnotation(PlaceStateHandler.class) != null) {
                    final Class<?>[] params = method.getParameterTypes();
                    if (params.length == 1 && params[0].equals(place.getClass())) {
                        try {
                            method.invoke(activity, place);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            super.onPlaceChange(event);
        }
    }
    
    @Override
    public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
        super.onPlaceChangeRequest(event);
    }
}
