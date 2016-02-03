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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.event.SimpleEventBus;
import info.magnolia.ui.api.shell.ConfirmationHandler;
import info.magnolia.ui.api.shell.Shell;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link info.magnolia.ui.api.location.LocationController}.
 */
public class LocationControllerTest {

    @Test
    public void testGoToWithoutWarning() {

        // GIVEN
        LocationChangeRequestedHandler requestHandler = new LocationChangeRequestedHandler();
        LocationChangedHandler changeHandler = new LocationChangedHandler();

        SimpleEventBus eventBus = new SimpleEventBus();
        eventBus.addHandler(LocationChangeRequestedEvent.class, requestHandler);
        eventBus.addHandler(LocationChangedEvent.class, changeHandler);

        LocationController locationController = new LocationController(eventBus, mock(Shell.class));

        assertEquals(Location.NOWHERE, locationController.getWhere());

        Location newLocation = getNewEmptyLocation();

        // WHEN
        locationController.goTo(newLocation);

        // THEN
        assertSame(newLocation, locationController.getWhere());

        assertNotNull(requestHandler.event);
        assertSame(newLocation, requestHandler.event.getNewLocation());

        assertNotNull(changeHandler.event);
        assertSame(newLocation, changeHandler.event.getNewLocation());
    }

    @Test
    public void testGoToWithConfirmedWarning() {

        // GIVEN
        LocationChangeRequestedHandler requestHandler = new LocationChangeRequestedHandlerThatWarns();
        LocationChangedHandler changeHandler = new LocationChangedHandler();

        SimpleEventBus eventBus = new SimpleEventBus();
        eventBus.addHandler(LocationChangeRequestedEvent.class, requestHandler);
        eventBus.addHandler(LocationChangedEvent.class, changeHandler);

        final MutableBoolean shellCalledToConfirm = new MutableBoolean(false);

        Shell shell = mock(Shell.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                shellCalledToConfirm.setValue(true);
                ConfirmationHandler o = (ConfirmationHandler) invocation.getArguments()[1];
                o.onConfirm();
                return null;
            }
        }).when(shell).askForConfirmation(anyString(), any(ConfirmationHandler.class));

        LocationController locationController = new LocationController(eventBus, shell);

        assertEquals(Location.NOWHERE, locationController.getWhere());

        Location newLocation = getNewEmptyLocation();

        // WHEN
        locationController.goTo(newLocation);

        // THEN
        assertTrue(shellCalledToConfirm.booleanValue());

        assertSame(newLocation, locationController.getWhere());

        assertNotNull(requestHandler.event);
        assertSame(newLocation, requestHandler.event.getNewLocation());

        assertNotNull(changeHandler.event);
        assertSame(newLocation, changeHandler.event.getNewLocation());
    }

    @Test
    public void testGoToWithCancelledWarning() {

        // GIVEN
        LocationChangeRequestedHandler requestHandler = new LocationChangeRequestedHandlerThatWarns();
        LocationChangedHandler changeHandler = new LocationChangedHandler();

        SimpleEventBus eventBus = new SimpleEventBus();
        eventBus.addHandler(LocationChangeRequestedEvent.class, requestHandler);
        eventBus.addHandler(LocationChangedEvent.class, changeHandler);

        final MutableBoolean shellCalledToConfirm = new MutableBoolean(false);

        Shell shell = mock(Shell.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                shellCalledToConfirm.setValue(true);
                ConfirmationHandler o = (ConfirmationHandler) invocation.getArguments()[1];
                o.onCancel();
                return null;
            }
        }).when(shell).askForConfirmation(anyString(), any(ConfirmationHandler.class));

        LocationController locationController = new LocationController(eventBus, shell);

        assertEquals(Location.NOWHERE, locationController.getWhere());

        Location newLocation = getNewEmptyLocation();

        // WHEN
        locationController.goTo(newLocation);

        // THEN
        assertTrue(shellCalledToConfirm.booleanValue());

        assertEquals(Location.NOWHERE, locationController.getWhere());

        assertNotNull(requestHandler.event);
        assertSame(newLocation, requestHandler.event.getNewLocation());

        assertNull(changeHandler.event);
    }

    @Test
    public void testGoToSameLocationDoesNothing() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();

        LocationController locationController = new LocationController(eventBus, mock(Shell.class));

        assertEquals(Location.NOWHERE, locationController.getWhere());

        Location newLocation = getNewEmptyLocation();

        locationController.goTo(newLocation);

        assertSame(newLocation, locationController.getWhere());

        LocationChangeRequestedHandler requestHandler = new LocationChangeRequestedHandler();
        LocationChangedHandler changeHandler = new LocationChangedHandler();
        eventBus.addHandler(LocationChangeRequestedEvent.class, requestHandler);
        eventBus.addHandler(LocationChangedEvent.class, changeHandler);

        // WHEN
        locationController.goTo(newLocation);

        // THEN
        assertNull(requestHandler.event);
        assertNull(changeHandler.event);
    }

    private Location getNewEmptyLocation() {
        return new Location() {
            @Override
            public String getParameter() {
                return null;
            }

            @Override
            public String getAppType() {
                return null;
            }

            @Override
            public String getAppName() {
                return null;
            }

            @Override
            public String getSubAppId() {
                return null;
            }
        };
    }

    private static class LocationChangeRequestedHandler implements LocationChangeRequestedEvent.Handler {

        public LocationChangeRequestedEvent event;

        @Override
        public void onLocationChangeRequested(LocationChangeRequestedEvent event) {
            this.event = event;
        }
    }

    private static class LocationChangedHandler implements LocationChangedEvent.Handler {

        public LocationChangedEvent event;

        @Override
        public void onLocationChanged(LocationChangedEvent event) {
            this.event = event;
        }
    }

    private static class LocationChangeRequestedHandlerThatWarns extends LocationChangeRequestedHandler {

        @Override
        public void onLocationChangeRequested(LocationChangeRequestedEvent event) {
            event.setWarning("WARNING");
            super.onLocationChangeRequested(event);
        }
    }
}
