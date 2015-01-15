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

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;

/**
 * Event fired when a location change is about to happen usually in response to user interaction. Handlers can call
 * {@link #setWarning(String)} to request that the user be prompted to confirm the change.
 */
public class LocationChangeRequestedEvent implements Event<LocationChangeRequestedEvent.Handler> {

    /**
     * Handler interface for {@link LocationChangeRequestedEvent}.
     */
    public interface Handler extends EventHandler {

        void onLocationChangeRequested(LocationChangeRequestedEvent event);
    }

    private String warning;

    private final Location newLocation;

    public LocationChangeRequestedEvent(Location newLocation) {
        this.newLocation = newLocation;
    }

    /**
     * Returns the location we may navigate to, or null on window close.
     */
    public Location getNewLocation() {
        return newLocation;
    }

    /**
     * Returns the warning message to show the user before allowing the location change, or null if
     * none has been set.
     */
    public String getWarning() {
        return warning;
    }

    /**
     * Set a message to warn the user that it might be unwise to navigate away from the current
     * location, i.e. due to unsaved changes. If the user clicks okay to that message, navigation will
     * proceed to the requested location.
     * <p>
     * Calling with a null warning is the same as not calling the method at all -- the user will not be prompted.
     * <p>
     * Only the first non-null call to setWarning has any effect. That is, once the warning message has been set it cannot be cleared.
     */
    public void setWarning(String warning) {
        if (this.warning == null) {
            this.warning = warning;
        }
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onLocationChangeRequested(this);
    }

}
