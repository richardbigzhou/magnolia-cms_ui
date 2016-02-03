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
import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a subApp with default behavior suitable for most sub apps.
 *
 * @see info.magnolia.ui.api.app.SubApp
 *
 * @param <V> {@link View} implementation used by subApp.
 */
public class BaseSubApp<V extends View> implements SubApp {

    private final SubAppContext subAppContext;
    private final V view;

    private static final Logger log = LoggerFactory.getLogger(BaseSubApp.class);

    protected BaseSubApp(final SubAppContext subAppContext, final V view) {
        if (subAppContext == null || view == null) {
            throw new IllegalArgumentException("Constructor does not allow for null args. Found SubAppContext = " + subAppContext + ", View = " + view);
        }
        this.subAppContext = subAppContext;
        this.view = view;
    }

    @Override
    public V start(Location location) {
        onSubAppStart();
        return view;
    }

    @Override
    public void stop() {
        onSubAppStop();
    }

    @Override
    public void locationChanged(Location location) {
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
     * This hook-up method is called on {@link #start(info.magnolia.ui.api.location.Location)} and enables subclasses to perform additional work before the view is displayed.
     * The default implementation does nothing.
     */
    protected void onSubAppStart() {
    }

    /**
     * This hook-up method is called on {@link #stop()} and enables subclasses to perform additional work when stopping the subApp.
     * The default implementation does nothing.
     */
    protected void onSubAppStop() {
    }

    public SubAppContext getSubAppContext() {
        return subAppContext;
    }

    @Override
    public String getSubAppId() {
        return subAppContext.getSubAppId();
    }

    @Override
    public V getView() {
        return view;
    }

    public AppContext getAppContext() {
        return subAppContext.getAppContext();
    }

    /**
     * This method will try to determine the current sub app caption, the one usually displayed in the tab where the subapp opens.
     *
     * @return the configured label for this subapp. If no label is found in the subapp configuration, it will try to use the label from the parent app.
     *         If the latter is missing too, it will return an empty string.
     */
    @Override
    public String getCaption() {
        String label = subAppContext.getSubAppDescriptor().getLabel();
        if (StringUtils.isNotBlank(label)) {
            return label;
        }
        label = subAppContext.getAppContext().getLabel();
        if (StringUtils.isNotBlank(label)) {
            return label;
        }
        log.warn("No label could be found for sub app [{}] in app [{}]", subAppContext.getSubAppDescriptor().getName(), subAppContext.getAppContext().getName());
        return "";
    }

    protected Location getCurrentLocation() {
        return getSubAppContext().getLocation();
    }

    @Override
    public boolean isCloseable() {
        return subAppContext.getSubAppDescriptor().isClosable();
    }
}
