/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.framework.place;

import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors the browser history for location changes and calls the {@link PlaceController} to initiate place changes
 * accordingly, also listens for place change events on the EventBus and updates the browser history to match.
 *
 * Inspired by {@link com.google.gwt.place.shared.PlaceHistoryHandler}
 *
 * @version $Id$
 */
public class PlaceHistoryHandler {

    private static final Logger log = LoggerFactory.getLogger(PlaceHistoryHandler.class.getName());

    private final PlaceHistoryMapper mapper;

    private PlaceController placeController;

    private Place defaultPlace = Place.NOWHERE;

    private Shell shell;

    public PlaceHistoryHandler(PlaceHistoryMapper mapper, Shell shell) {
        this.mapper = mapper;
        this.shell = shell;
    }

    /**
     * Initialize this place history handler.
     */
    public HandlerRegistration register(PlaceController placeController, EventBus eventBus, Place defaultPlace) {
        this.placeController = placeController;
        this.defaultPlace = defaultPlace;

        shell.addFragmentChangedHandler(new FragmentChangedHandler() {

            @Override
            public void onFragmentChanged(FragmentChangedEvent event) {
                String token = event.getFragment();
                log.debug("fragmentChanged with token {}", token);
                handleHistoryToken(token);
            }
        });

        return eventBus.addHandler(PlaceChangeEvent.class, new PlaceChangeEvent.Handler() {

            @Override
            public void onPlaceChange(PlaceChangeEvent event) {
                log.debug("onPlaceChange...");
                Place newPlace = event.getNewPlace();
                shell.setFragment(tokenForPlace(newPlace));
            }
        });
    }

    /**
     * Handle the current history token. Typically called at application start, to ensure bookmark launches work.
     */
    public void handleCurrentHistory() {
        String fragment = shell.getFragment();
        handleHistoryToken(StringUtils.defaultString(fragment));
    }

    private void handleHistoryToken(String token) {

        Place newPlace = null;

        if ("".equals(token)) {
            newPlace = defaultPlace;
        }

        if (newPlace == null) {
            newPlace = mapper.getPlace(token);
        }

        if (newPlace == null) {
            log.warn("Unrecognized history token: {}, falling back to default place...", token);
            newPlace = defaultPlace;
        }

        log.debug("handleHistoryToken with place {}", newPlace);
        placeController.goTo(newPlace);
    }

    private String tokenForPlace(Place newPlace) {
        // FIXME if uncommented the URI fragment won't be written
        /*if (defaultPlace.equals(newPlace)) {
            return "";
        }*/

        String token = mapper.getToken(newPlace);
        if (token != null) {
            log.debug("tokenForPlace returns token [{}]", token);
            return token;
        }

        log.debug("Place not mapped to a token: {}", newPlace);
        return "";
    }

}
