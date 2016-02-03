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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.ui.api.shell.FragmentChangedEvent;
import info.magnolia.ui.api.shell.FragmentChangedHandler;
import info.magnolia.ui.api.shell.Shell;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link info.magnolia.ui.api.location.LocationHistoryHandler}.
 */
public class LocationHistoryHandlerTest {

    @Before
    public void setUp() throws Exception {
        MockWebContext ctx = new MockWebContext();
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    // fragment update as result of location change

    @Test
    public void testUpdatesFragmentOnLocationChange() {
        assertSetsFragmentToOnLocationChange(new DefaultLocation("app", "foo", "subAppId", "/some/path"), "app:foo:subAppId;/some/path");
    }

    @Test
    public void testSetsFragmentToEmptyOnLocationChangeWhenMapperDoesntRecognizeTheLocation() {
        assertSetsFragmentToOnLocationChange(new DefaultLocation("app", "bar", "", "/some/path"), "");
    }

    // location change as result of fragment change

    @Test
    public void testCallsLocationControllerOnFragmentChange() {
        assertFragmentChangeCausesLocationChangeTo("app:foo:subAppId;/some/path", new DefaultLocation("app", "foo", "subAppId", "/some/path"));
    }

    @Test
    public void testGoesToDefaultLocationWhenFragmentIsEmpty() {
        assertFragmentChangeCausesLocationChangeTo("", Location.NOWHERE);
    }

    @Test
    public void testGoesToDefaultLocationWhenFragmentIsNull() {
        assertFragmentChangeCausesLocationChangeTo(null, Location.NOWHERE);
    }

    @Test
    public void testGoesToDefaultLocationWhenFragmentHasUnknownPrefix() {
        assertFragmentChangeCausesLocationChangeTo("unknown-prefix:/some/path", Location.NOWHERE);
    }

    // location change as result of handling current history

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultLocationWhenFragmentEmpty() {
        assertCallsLocationControllerWhenCurrentHistoryIs("", Location.NOWHERE);
    }

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultLocationWhenFragmentNull() {
        assertCallsLocationControllerWhenCurrentHistoryIs(null, Location.NOWHERE);
    }

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultLocation() {
        assertCallsLocationControllerWhenCurrentHistoryIs("app:foo;/some/path", new DefaultLocation("app", "foo", "", "/some/path"));
    }

    @Test
    public void testHandleCurrentHistoryNavigatesToDefaultLocationWhenFragmentHasUnknownPrefix() {
        assertCallsLocationControllerWhenCurrentHistoryIs("unknown-prefix:/some/path", Location.NOWHERE);
    }

    private void assertCallsLocationControllerWhenCurrentHistoryIs(String fragment, Location expectedLocation) {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();
        Shell shell = mock(Shell.class);
        when(shell.getFragment()).thenReturn(fragment);

        LocationController locationController = mock(LocationController.class);
        LocationHistoryMapper locationHistoryMapper = new MockLocationHistoryMapper();

        LocationHistoryHandler locationHistoryHandler = new LocationHistoryHandler(locationHistoryMapper, shell);

        locationHistoryHandler.register(locationController, eventBus, Location.NOWHERE);

        // WHEN
        locationHistoryHandler.handleCurrentFragment();

        // THEN
        Mockito.verify(locationController).goTo(eq(expectedLocation));
    }

    private void assertSetsFragmentToOnLocationChange(Location newLocation, String expectedFragment) {

        // GIVEN
        Shell shell = mock(Shell.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        LocationController locationController = mock(LocationController.class);
        LocationHistoryMapper locationHistoryMapper = new MockLocationHistoryMapper();

        LocationHistoryHandler locationHistoryHandler = new LocationHistoryHandler(locationHistoryMapper, shell);

        locationHistoryHandler.register(locationController, eventBus, Location.NOWHERE);

        // WHEN
        eventBus.fireEvent(new LocationChangedEvent(newLocation));

        // THEN
        Mockito.verify(shell).setFragment(expectedFragment);
    }

    private void assertFragmentChangeCausesLocationChangeTo(String fragment, Location expectedLocation) {

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
        SimpleEventBus eventBus = new SimpleEventBus();
        LocationController locationController = mock(LocationController.class);
        LocationHistoryMapper locationHistoryMapper = new MockLocationHistoryMapper();

        LocationHistoryHandler locationHistoryHandler = new LocationHistoryHandler(locationHistoryMapper, shell);

        locationHistoryHandler.register(locationController, eventBus, Location.NOWHERE);

        // WHEN
        handler.value.onFragmentChanged(new FragmentChangedEvent(fragment));

        // THEN
        Mockito.verify(locationController).goTo(eq(expectedLocation));
    }

    private static class MockLocationHistoryMapper implements LocationHistoryMapper {

        @Override
        public Location getLocation(String fragment) {
            String type = DefaultLocation.extractAppType(fragment);
            String appName = DefaultLocation.extractAppName(fragment);
            String subAppId = DefaultLocation.extractSubAppId(fragment);
            String parameter = DefaultLocation.extractParameter(fragment);

            if (type.equals("app") && appName.equals("foo")) {
                return new DefaultLocation(type, appName, subAppId, parameter);
            }

            return null;
        }

        @Override
        public String getFragment(Location location) {
            if (location.getAppType().equals("app") && location.getAppName().equals("foo")) {
                return location.toString();
            }
            return null;
        }
    }

    private class Reference<T> {
        public T value;
    }
}
