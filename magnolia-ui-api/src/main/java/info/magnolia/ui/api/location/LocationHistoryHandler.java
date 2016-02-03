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
package info.magnolia.ui.api.location;

import info.magnolia.event.EventBus;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.ui.api.shell.FragmentChangedEvent;
import info.magnolia.ui.api.shell.FragmentChangedHandler;
import info.magnolia.ui.api.shell.Shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors the browser history for location changes and calls the {@link LocationController} to initiate location changes
 * accordingly, also listens for location change events on the EventBus and updates the browser history to match.
 */
public class LocationHistoryHandler {

    private static final Logger log = LoggerFactory.getLogger(LocationHistoryHandler.class.getName());

    private final LocationHistoryMapper mapper;

    private LocationController locationController;

    private Location defaultLocation = Location.NOWHERE;

    private Shell shell;

    public LocationHistoryHandler(LocationHistoryMapper mapper, Shell shell) {
        this.mapper = mapper;
        this.shell = shell;
    }

    /**
     * Initialize this location history handler.
     */
    public HandlerRegistration register(LocationController locationController, EventBus eventBus, Location defaultLocation) {
        this.locationController = locationController;
        this.defaultLocation = defaultLocation;

        shell.addFragmentChangedHandler(new FragmentChangedHandler() {

            @Override
            public void onFragmentChanged(FragmentChangedEvent event) {
                String fragment = event.getFragment();
                log.debug("fragmentChanged with fragment {}", fragment);
                handleFragment(fragment);
            }
        });

        return eventBus.addHandler(LocationChangedEvent.class, new LocationChangedEvent.Handler() {

            @Override
            public void onLocationChanged(LocationChangedEvent event) {
                log.debug("onLocationChanged...");
                Location newLocation = event.getNewLocation();
                shell.setFragment(fragmentForLocation(newLocation));
            }
        });
    }

    /**
     * Handle the current history fragment. Typically called at application start, to ensure bookmark launches work.
     */
    public void handleCurrentFragment() {
        String fragment = shell.getFragment();
        handleFragment(fragment);
    }

    private void handleFragment(String fragment) {

        Location newLocation = null;

        if (fragment == null || "".equals(fragment)) {
            newLocation = defaultLocation;
        }

        if (newLocation == null) {
            newLocation = locationForFragment(fragment);
        }

        if (newLocation == null) {
            log.warn("Unrecognized history fragment: {}, falling back to default location...", fragment);
            newLocation = defaultLocation;
        }

        log.debug("handleFragment with location {}", newLocation);
        locationController.goTo(newLocation);
    }

    private Location locationForFragment(String fragment) {
        Location location = mapper.getLocation(fragment);
        log.debug("locationForFragment returns location [{}]", location);
        return location;
    }

    private String fragmentForLocation(Location newLocation) {

        String fragment = mapper.getFragment(newLocation);

        if (fragment == null) {
            log.debug("Location not mapped to a fragment: {}", newLocation);
            return "";
        }

        log.debug("fragmentForLocation returns fragment [{}]", fragment);
        return fragment;
    }
}
