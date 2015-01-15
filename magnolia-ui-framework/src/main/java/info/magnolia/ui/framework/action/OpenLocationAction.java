/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.framework.action;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;

import javax.inject.Inject;

/**
 * The {@link OpenLocationAction} opens a given location in the admincentral.
 * 
 * @see {@link OpenLocationActionDefinition}
 */
public class OpenLocationAction extends AbstractAction<OpenLocationActionDefinition> {

    protected final LocationController locationController;

    @Inject
    public OpenLocationAction(OpenLocationActionDefinition definition, LocationController locationController) {
        super(definition);
        this.locationController = locationController;
    }

    @Override
    public void execute() throws ActionExecutionException {
        final String appType = this.getDefinition().getAppType();
        final String appName = this.getDefinition().getAppName();
        final String subAppId = this.getDefinition().getSubAppId();
        final String parameter = this.getDefinition().getParameter();

        Location location = new DefaultLocation(appType, appName, subAppId, parameter);
        locationController.goTo(location);
    }
}
