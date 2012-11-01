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

import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;


/**
 * Abstract implementation with default behavior suitable for most sub apps.
 * 
 * @see info.magnolia.ui.framework.app.SubApp
 */
public abstract class AbstractSubApp implements SubApp {


    protected Location currentLocation;
    private final SubAppContext subAppContext;
    private final View view;

    protected AbstractSubApp(final SubAppContext subAppContext, final View view) {
        if(subAppContext == null || view == null) {
            throw new IllegalArgumentException("Constructor does not allow for null args. Found SubAppContext = " + subAppContext + ", ContentAppView = " + view);
        }
        this.subAppContext = subAppContext;
        this.view = view;
    }

    @Override
    public View start(Location location) {
        currentLocation = location;
        onSubAppStart();
        return view;
    }

    @Override
    public void locationChanged(Location location) {
        currentLocation = location;
    }

    /**
     * This method is being called by the AppController when iterating over opened subApps.
     * The subApp itself decides whether it supports the current location based on parameters or
     * whether the appController should launch a new instance of the subApp.
     */

    @Override
    public boolean supportsLocation(Location location) {
        return true;
    }

    /**
     * This hook-up method is called on {@link #start(info.magnolia.ui.framework.location.Location)} and enables subclasses to perform additional work before the view is displayed.
     * The default implementation does nothing.
     */
    protected void onSubAppStart() { }

    public SubAppContext getSubAppContext() {
        return subAppContext;
    }

    @Override
    public String getSubAppId() {
        return subAppContext.getSubAppId();
    }

    public View getView() {
        return view;
    }

    public AppContext getAppContext() {
        return subAppContext.getAppContext();
    }

    @Override
    public String getCaption() {
        return getAppContext().getAppDescriptor().getLabel();
    }

    protected Location getCurrentLocation() {
        return currentLocation;
    }

}
