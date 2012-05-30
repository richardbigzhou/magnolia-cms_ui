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
package info.magnolia.ui.framework.place;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.Shell;

/**
 * @version $Id$
 */
public class PlaceControllerTest {

    @Test
    public void testGoToWithoutWarning() {

        // GIVEN
        PlaceChangeRequestHandler requestHandler = new PlaceChangeRequestHandler();
        PlaceChangeHandler changeHandler = new PlaceChangeHandler();

        SimpleEventBus eventBus = new SimpleEventBus();
        eventBus.addHandler(PlaceChangeRequestEvent.class, requestHandler);
        eventBus.addHandler(PlaceChangeEvent.class, changeHandler);

        PlaceController placeController = new PlaceController(eventBus, mock(Shell.class));

        assertEquals(Place.NOWHERE, placeController.getWhere());

        Place newPlace = new Place() {
        };

        // WHEN
        placeController.goTo(newPlace);

        // THEN
        assertSame(newPlace, placeController.getWhere());

        assertNotNull(requestHandler.event);
        assertSame(newPlace, requestHandler.event.getNewPlace());

        assertNotNull(changeHandler.event);
        assertSame(newPlace, changeHandler.event.getNewPlace());
    }

    @Test
    public void testGoToWithConfirmedWarning() {

        // GIVEN
        PlaceChangeRequestHandler requestHandler = new PlaceChangeRequestHandlerThatWarns();
        PlaceChangeHandler changeHandler = new PlaceChangeHandler();

        SimpleEventBus eventBus = new SimpleEventBus();
        eventBus.addHandler(PlaceChangeRequestEvent.class, requestHandler);
        eventBus.addHandler(PlaceChangeEvent.class, changeHandler);

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

        PlaceController placeController = new PlaceController(eventBus, shell);

        assertEquals(Place.NOWHERE, placeController.getWhere());

        Place newPlace = new Place() {
        };

        // WHEN
        placeController.goTo(newPlace);

        // THEN
        assertTrue(shellCalledToConfirm.booleanValue());

        assertSame(newPlace, placeController.getWhere());

        assertNotNull(requestHandler.event);
        assertSame(newPlace, requestHandler.event.getNewPlace());

        assertNotNull(changeHandler.event);
        assertSame(newPlace, changeHandler.event.getNewPlace());
    }

    @Test
    public void testGoToWithCancelledWarning() {

        // GIVEN
        PlaceChangeRequestHandler requestHandler = new PlaceChangeRequestHandlerThatWarns();
        PlaceChangeHandler changeHandler = new PlaceChangeHandler();

        SimpleEventBus eventBus = new SimpleEventBus();
        eventBus.addHandler(PlaceChangeRequestEvent.class, requestHandler);
        eventBus.addHandler(PlaceChangeEvent.class, changeHandler);

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

        PlaceController placeController = new PlaceController(eventBus, shell);

        assertEquals(Place.NOWHERE, placeController.getWhere());

        Place newPlace = new Place() {
        };

        // WHEN
        placeController.goTo(newPlace);

        // THEN
        assertTrue(shellCalledToConfirm.booleanValue());

        assertEquals(Place.NOWHERE, placeController.getWhere());

        assertNotNull(requestHandler.event);
        assertSame(newPlace, requestHandler.event.getNewPlace());

        assertNull(changeHandler.event);
    }

    @Test
    public void testGoToSamePlaceDoesNothing() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();

        PlaceController placeController = new PlaceController(eventBus, mock(Shell.class));

        assertEquals(Place.NOWHERE, placeController.getWhere());

        Place newPlace = new Place() {
        };

        placeController.goTo(newPlace);

        assertSame(newPlace, placeController.getWhere());

        PlaceChangeRequestHandler requestHandler = new PlaceChangeRequestHandler();
        PlaceChangeHandler changeHandler = new PlaceChangeHandler();
        eventBus.addHandler(PlaceChangeRequestEvent.class, requestHandler);
        eventBus.addHandler(PlaceChangeEvent.class, changeHandler);

        // WHEN
        placeController.goTo(newPlace);

        // THEN
        assertNull(requestHandler.event);
        assertNull(changeHandler.event);
    }

    private static class PlaceChangeRequestHandler implements PlaceChangeRequestEvent.Handler {

        public PlaceChangeRequestEvent event;

        @Override
        public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
            this.event = event;
        }
    }

    private static class PlaceChangeHandler implements PlaceChangeEvent.Handler {

        public PlaceChangeEvent event;

        @Override
        public void onPlaceChange(PlaceChangeEvent event) {
            this.event = event;
        }
    }

    private static class PlaceChangeRequestHandlerThatWarns extends PlaceChangeRequestHandler {

        @Override
        public void onPlaceChangeRequest(PlaceChangeRequestEvent event) {
            event.setWarning("WARNING");
            super.onPlaceChangeRequest(event);
        }
    }
}
