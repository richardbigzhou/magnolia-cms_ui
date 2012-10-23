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

import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;

import java.util.Map;


/**
 * Abstract implementation with default behavior suitable for most apps.
 * 
 * @see App
 */
public abstract class AbstractApp implements App {

    protected AppContext appContext;

    protected AbstractApp(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void locationChanged(Location location) {

        SubAppDescriptor subAppDescriptor = null;

        String subAppId = getSubAppId(location);
        subAppDescriptor = getAppDescriptorById(subAppId);
        if (subAppDescriptor == null) {
            subAppDescriptor = getDefaultAppDescriptor();
        }
        appContext.openSubApp(subAppDescriptor.getName(), subAppDescriptor.getSubAppClass(), location);

    }


        @Override
    public void start(Location location) {
        SubAppDescriptor subAppDescriptor = null;

        String subAppId = getSubAppId(location);
        subAppDescriptor = getAppDescriptorById(subAppId);
        if (subAppDescriptor == null) {
            subAppDescriptor = getDefaultAppDescriptor();
        }

        appContext.openSubApp(subAppDescriptor.getName(), subAppDescriptor.getSubAppClass(), location);
    }

    private SubAppDescriptor getDefaultAppDescriptor() {
        Map<String, SubAppDescriptor> subAppDescriptors = appContext.getAppDescriptor().getSubApps();

        SubAppDescriptor defaultSubAppDescriptor = null;
        for (SubAppDescriptor subAppDescriptor : subAppDescriptors.values()) {
            if (subAppDescriptor.isDefault()) {
                defaultSubAppDescriptor = subAppDescriptor;
                break;
            }
        }
        return defaultSubAppDescriptor;
    }

    private SubAppDescriptor getAppDescriptorById(String subAppId) {
        Map<String, SubAppDescriptor> subAppDescriptors = appContext.getAppDescriptor().getSubApps();
        return subAppDescriptors.get(subAppId);
    }

    private String getSubAppId(Location location) {

        DefaultLocation l = (DefaultLocation) location;
        String token = l.getToken();

        // "subAppId"
        int i = token.indexOf(';');
        if (i > -1) {
            return token.substring(0, i);
        }
        else return "";
    }

    @Override
    public void stop() {
    }

    @Override
    public Location getDefaultLocation() {
        return null;
    }
}
