/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.api.location;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.shell.ConfirmationHandler;
import info.magnolia.ui.api.shell.Shell;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Manages the user's location in the application and controls location changes.
 */
@Singleton
public class LocationController {

    private final EventBus eventBus;

    private Shell shell;

    private Location where = Location.NOWHERE;

    @Inject
    public LocationController(@Named(AdmincentralEventBus.NAME) final EventBus eventBus, Shell shell) {
        this.eventBus = eventBus;
        this.shell = shell;
    }

    /**
     * Returns the current location.
     */
    public Location getWhere() {
        return where;
    }

    /**
     * Request a change to a new location.
     */
    public void goTo(final Location newLocation) {

        if (newLocation == null || getWhere().equals(newLocation)) {
            return;
        }

        LocationChangeRequestedEvent willChange = new LocationChangeRequestedEvent(newLocation);
        eventBus.fireEvent(willChange);

        // Listeners to this event will set warning if they want the users to confirm the location change

        if (willChange.getWarning() != null) {
            shell.askForConfirmation(willChange.getWarning(), new ConfirmationHandler() {

                @Override
                public void onConfirm() {
                    goToWithoutChecks(newLocation);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            goToWithoutChecks(newLocation);
        }
    }

    protected void goToWithoutChecks(Location newLocation) {
        this.where = newLocation;
        eventBus.fireEvent(new LocationChangedEvent(newLocation));
    }
}
