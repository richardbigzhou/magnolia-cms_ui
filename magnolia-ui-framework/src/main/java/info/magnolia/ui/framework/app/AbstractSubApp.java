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
package info.magnolia.ui.framework.app;

import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation with default behavior suitable for most sub apps.
 * 
 * @see info.magnolia.ui.framework.app.SubApp
 */
public abstract class AbstractSubApp implements SubApp {


    protected DefaultLocation currentLocation;

    private final AppContext appContext;
    private final EventBus subAppEventBus;
    private final View view;

    private String subAppId;
    private String appName;

    protected AbstractSubApp(final AppContext appContext, final View view, final @Named("subapp") EventBus subAppEventBus) {
        if(appContext == null || view == null ||  subAppEventBus == null) {
            throw new IllegalArgumentException("Constructor does not allow for null args. Found AppContext = " + appContext + ", ContentAppView = " + view + ", EventBus = " + subAppEventBus);
        }
        this.appContext = appContext;
        this.view = view;
        this.subAppEventBus = subAppEventBus;
        this.appName = appContext.getName();
    }

    @Override
    public View start(Location location) {
        currentLocation = (DefaultLocation)location;
        onSubAppStart();
        return view;
    }


    /**
     * @return <code>true</code> if subapp id is <code>main</code>.
     */
    public final boolean supportsLocation(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 1 && subAppId.equals(parts.get(0));
    }

    /**
     * Creates a default location for the current subapp whose token has the form <code>main:/:tree</code>.
     */
    public final DefaultLocation createLocation() {
        return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, getAppName(), "", getDefaultToken());
    }

    public String getDefaultToken() {
        return getSubAppId() +":/:" + "tree";
    }

    /**
     * Generic Location token handling, format is {@code subAppId;<someParameters> }. Overwrite this method for more handling parameters.
     */

    public List<String> parseLocationToken(final Location location) {

        ArrayList<String> parts = new ArrayList<String>();

        DefaultLocation l = (DefaultLocation) location;
        String token = l.getParameter();

        // "subAppId"
        int i = token.indexOf(';');
        if (i == -1) {
            if (!subAppId.equals(token)) {
                return new ArrayList<String>();
            }
            parts.add(token);
            return parts;
        }

        String subAppNameFromLocation = token.substring(0, i);
        if (!subAppId.equals(subAppNameFromLocation)) {
            return new ArrayList<String>();
        }
        parts.add(subAppNameFromLocation);
        token = token.substring(i + 1);

        // parameters
        if (token.length() > 0) {

            if ( token.contains(":")) {
                // pagesApp preview
                String[] tokenParts = token.split(":");
                for(String part : tokenParts) {
                    parts.add(part);
                }
            }
            else {
                parts.add(token);
            }
        }
        return parts;
    }

    /**
     * This hook-up method is called on {@link #start(info.magnolia.ui.framework.location.Location)} and enables subclasses to perform additional work before the view is displayed.
     * The default implementation does nothing.
     */
    protected void onSubAppStart() { }

    @Override
    public void setSubAppId(String subAppId) {
        this.subAppId = subAppId;
    }

    public String getSubAppId() {
        return subAppId;
    }

    public View getView() {
        return view;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    @Override
    public String getCaption() {
        return getAppContext().getAppDescriptor().getLabel();
    }

    protected final DefaultLocation getCurrentLocation() {
        return currentLocation;
    }

    /*
    * Creates a location for the current subapp given the current location, the passed parameter and its type.
    */
    protected final DefaultLocation createLocation(final String parameter, final DefaultLocation currentLocation) {
        DefaultLocation location = createLocation();
        if (currentLocation != null) {
            String token = location.getParameter();
            //token = replaceLocationToken(currentLocation, parameter, type);
            return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, getAppName(), "", token);
        }
        return location;
    }

    /**
     * @return the app name as returned by {@link AppContext#getName()}.
     */
    public final String getAppName() {
        return appName;
    }

}
