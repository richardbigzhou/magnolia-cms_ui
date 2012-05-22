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
package info.magnolia.m5admincentral.framework;

import info.magnolia.m5admincentral.app.AppLifecycleEvent;
import info.magnolia.m5admincentral.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.activity.ActivityManager;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceChangeEvent;
import info.magnolia.ui.framework.place.PlaceChangeRequestEvent;

/**
 * Activity manager responsible for the app management.
 * 
 * @author p4elkin
 * 
 */
public class AppActivityManager extends ActivityManager {

    private ActivityMapper mapper;

    public AppActivityManager(final AppActivityMapper mapper, final EventBus eventBus) {
        super(mapper, eventBus);
        this.mapper = mapper;
        eventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {
            
            @Override
            public void onStartApp(AppLifecycleEvent event) {
                mapper.registerAppStart(event.getApp());
            }
            
            @Override
            public void onStopApp(AppLifecycleEvent event) {
                mapper.uregisterApp(event.getApp());
            }
        });
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final Activity activity = mapper.getActivity(event.getNewPlace());
        if (activity != null) {
            super.onPlaceChange(event);
        }
    }

    @Override
    public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
        super.onPlaceChangeRequest(event);
    }

}
