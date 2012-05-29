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

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.shell.FragmentChangedEvent;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;

/**
 * Test case for {@link PlaceHistoryHandler}.
 *
 * @version $Id$
 */
public class PlaceHistoryHandlerTest {

    @Prefix("foo")
    public static class FooPlace extends Place {

        public static class Tokenizer implements PlaceTokenizer<FooPlace> {

            @Override
            public FooPlace getPlace(String token) {
                return new FooPlace(token);
            }

            @Override
            public String getToken(FooPlace place) {
                return place.getPath();
            }
        }

        private String path;

        public FooPlace(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FooPlace fooPlace = (FooPlace) o;

            if (path != null ? !path.equals(fooPlace.path) : fooPlace.path != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }
    }

    @Prefix("bar")
    public static class BarPlace extends Place {

        public static class Tokenizer implements PlaceTokenizer<BarPlace> {

            @Override
            public BarPlace getPlace(String token) {
                return new BarPlace(token);
            }

            @Override
            public String getToken(BarPlace place) {
                return place.getPath();
            }
        }

        private String path;

        public BarPlace(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BarPlace barPlace = (BarPlace) o;

            if (path != null ? !path.equals(barPlace.path) : barPlace.path != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }
    }

    // fragment update as result of place change

    @Test
    public void testUpdatesFragmentOnPlaceChange() {
        assertSetsFragmentToOnPlaceChange(new FooPlace("/some/path"), "foo:/some/path");
    }

    @Test
    public void testSetsFragmentToEmptyOnPlaceChangeWhenMapperDoesntRecognizeThePlace() {
        assertSetsFragmentToOnPlaceChange(new BarPlace("/some/path"), "");
    }

    // place change as result of fragment change

    @Test
    public void testCallsPlaceControllerOnFragmentChange() {
        assertFragmentChangeCausesPlaceChangeTo("foo:/some/path", new FooPlace("/some/path"));
    }

    @Test
    public void testGoesToDefaultPlaceWhenFragmentIsEmpty() {
        assertFragmentChangeCausesPlaceChangeTo("", Place.NOWHERE);
    }

    @Test
    public void testGoesToDefaultPlaceWhenFragmentIsNull() {
        assertFragmentChangeCausesPlaceChangeTo(null, Place.NOWHERE);
    }

    @Test
    public void testGoesToDefaultPlaceWhenFragmentHasUnknownPrefix() {
        assertFragmentChangeCausesPlaceChangeTo("unknown-prefix:/some/path", Place.NOWHERE);
    }

    // place change as result of handling current history

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultPlaceWhenFragmentEmpty() {
        assertCallsPlaceControllerWhenCurrentHistoryIs("", Place.NOWHERE);
    }

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultPlaceWhenFragmentNull() {
        assertCallsPlaceControllerWhenCurrentHistoryIs(null, Place.NOWHERE);
    }

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultPlace() {
        assertCallsPlaceControllerWhenCurrentHistoryIs("foo:/some/path", new FooPlace("/some/path"));
    }

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultPlaceWhenFragmentHasUnknownPrefix() {
        assertCallsPlaceControllerWhenCurrentHistoryIs("unknown-prefix:/some/path", Place.NOWHERE);
    }

    private void assertCallsPlaceControllerWhenCurrentHistoryIs(String fragment, Place expectedPlace) {

        // GIVEN
        Shell shell = mock(Shell.class);
        when(shell.getFragment()).thenReturn(fragment);
        PlaceController placeController = mock(PlaceController.class);

        SimpleEventBus eventBus = new SimpleEventBus();
        PlaceHistoryMapperImpl placeHistoryMapper = new PlaceHistoryMapperImpl(FooPlace.class);
        PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(placeHistoryMapper, shell);

        placeHistoryHandler.register(placeController, eventBus, Place.NOWHERE);

        // WHEN
        placeHistoryHandler.handleCurrentHistory();

        // THEN
        Mockito.verify(placeController).goTo(eq(expectedPlace));
    }

    private void assertSetsFragmentToOnPlaceChange(Place newPlace, String expectedFragment) {

        // GIVEN
        Shell shell = mock(Shell.class);
        PlaceController placeController = mock(PlaceController.class);

        SimpleEventBus eventBus = new SimpleEventBus();
        PlaceHistoryMapperImpl placeHistoryMapper = new PlaceHistoryMapperImpl(FooPlace.class);
        PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(placeHistoryMapper, shell);

        placeHistoryHandler.register(placeController, eventBus, Place.NOWHERE);

        // WHEN
        eventBus.fireEvent(new PlaceChangeEvent(newPlace));

        // THEN
        Mockito.verify(shell).setFragment(expectedFragment);
    }

    private void assertFragmentChangeCausesPlaceChangeTo(String fragment, Place expectedPlace) {

        // GIVEN
        Shell shell = mock(Shell.class);
        final Reference<FragmentChangedHandler> handler = new Reference<FragmentChangedHandler>();
        doAnswer(new Answer<HandlerRegistration>() {
            @Override
            public HandlerRegistration answer(InvocationOnMock invocation) throws Throwable {
                handler.value = (FragmentChangedHandler) invocation.getArguments()[0];
                return null;
            }
        }).when(shell).addFragmentChangedHandler(any(FragmentChangedHandler.class));

        PlaceController placeController = mock(PlaceController.class);

        SimpleEventBus eventBus = new SimpleEventBus();
        PlaceHistoryMapperImpl placeHistoryMapper = new PlaceHistoryMapperImpl(FooPlace.class);
        PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(placeHistoryMapper, shell);

        placeHistoryHandler.register(placeController, eventBus, Place.NOWHERE);

        // WHEN
        handler.value.onFragmentChanged(new FragmentChangedEvent(fragment));

        // THEN
        Mockito.verify(placeController).goTo(eq(expectedPlace));
    }

    private class Reference<T> {
        public T value;
    }
}
