/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

/**
 * Dummy app used for test purpose.
 *
 * The two static fields are used to access the AppContext set by AppControler.
 * As AppContext is linked to a child GuiceComponentProvider used in AppControler
 * it's not possible in the test class to access child GuiceComponentProvider
 * (app specific provider) defining the AppContext.
 */
public class AppTestImpl extends BaseApp {

    public List<String> events = new ArrayList<String>();
    public AppContext ctx;
    public Location currentLocation;
    public static Map<String, Object> res = new HashMap<String, Object>();
    public static int appNumber = 0;

    @Inject
    public AppTestImpl(AppContext ctx, AppView view) {
        super(ctx, view);
        res.put("TestPageApp" + appNumber, this);
        appNumber += 1;
    }

    @Override
    public void start(Location location) {
        super.start(location);
        events.add("start() with location " + location);
    }

    @Override
    public void locationChanged(Location location) {
        super.locationChanged(location);
        events.add("locationChanged() for location " + location);
    }

    @Override
    public void stop() {
        events.add("stop() ");
    }

}
