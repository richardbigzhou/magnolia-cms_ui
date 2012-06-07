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
package info.magnolia.ui.framework.activity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.event.TestEvent;
import info.magnolia.ui.framework.event.TestEventHandler;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceChangeEvent;
import info.magnolia.ui.framework.place.PlaceChangeRequestEvent;
import info.magnolia.ui.framework.view.ViewPort;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link ActivityManager}.
 *
 * @version $Id$
 */
public class ActivityManagerTest {

    public static class FooActivity extends AbstractActivity {

        @Override
        public void start(ViewPort viewPort, EventBus eventBus, Place place) {
        }
    }

    public static class FooPlace extends Place {

    }

    public static class BarPlace extends Place {

    }

    @Test
    public void testStartsActivity() {

        // GIVEN
        Activity activity = mock(Activity.class);
        ViewPort viewPort = mock(ViewPort.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        when(activityMapper.getActivity(any(Place.class))).thenReturn(activity);

        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setViewPort(viewPort);

        // WHEN
        FooPlace place = new FooPlace();
        eventBus.fireEvent(new PlaceChangeEvent(place));

        // THEN
        verify(activity).start(eq(viewPort), any(EventBus.class), eq(place));
    }

    @Test
    public void testRefusesToNavigateToSameActivity() {

        // GIVEN
        Activity activity = mock(Activity.class);
        ViewPort viewPort = mock(ViewPort.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        when(activityMapper.getActivity(any(Place.class))).thenReturn(activity);

        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setViewPort(viewPort);

        FooPlace place = new FooPlace();
        eventBus.fireEvent(new PlaceChangeEvent(place));
        verify(activity, times(1)).start(eq(viewPort), any(EventBus.class), eq(place));

        // WHEN
        FooPlace place2 = new FooPlace();
        eventBus.fireEvent(new PlaceChangeEvent(place2));

        // THEN (still 1)
        verify(activity, times(1)).start(eq(viewPort), any(EventBus.class), any(Place.class));
    }

    @Test
    public void testActivatingNullActivityStopsTheActiveActivity() {

        // GIVEN
        Activity activity = mock(Activity.class);
        ViewPort viewPort = mock(ViewPort.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        when(activityMapper.getActivity(any(Place.class)))
                .thenReturn(activity)
                .thenReturn(null);

        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setViewPort(viewPort);

        FooPlace place = new FooPlace();
        eventBus.fireEvent(new PlaceChangeEvent(place));

        verify(activity, times(1)).start(eq(viewPort), any(EventBus.class), eq(place));

        // WHEN
        eventBus.fireEvent(new PlaceChangeEvent(new Place() {
        }));

        // THEN
        verify(activity, times(1)).onStop();
    }

    @Test
    public void testSetsWarningFromActivity() {

        // GIVEN
        Activity activity = mock(Activity.class);
        when(activity.mayStop()).thenReturn("WARNING");
        ViewPort viewPort = mock(ViewPort.class);
        SimpleEventBus eventBus = new SimpleEventBus();
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        when(activityMapper.getActivity(any(Place.class))).thenReturn(activity);

        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setViewPort(viewPort);

        eventBus.fireEvent(new PlaceChangeEvent(new FooPlace()));

        // WHEN
        PlaceChangeRequestEvent event = new PlaceChangeRequestEvent(new FooPlace());
        eventBus.fireEvent(event);

        // THEN
        verify(activity).mayStop();
        assertEquals("WARNING", event.getWarning());
    }

    public static class EventListeningTestActivity extends AbstractActivity implements TestEventHandler {

        public int invocationCount = 0;

        @Override
        public void start(ViewPort viewPort, EventBus eventBus, Place place) {
            eventBus.addHandler(TestEvent.class, this);
        }

        @Override
        public void handleEvent(TestEvent event) {
            invocationCount++;
        }
    }

    @Ignore
    @Test
    public void testActivityGetsEventEvenWhenNotActive() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();
        ViewPort viewPort = mock(ViewPort.class);

        EventListeningTestActivity fooActivity = new EventListeningTestActivity();
        EventListeningTestActivity barActivity = new EventListeningTestActivity();

        ActivityMapper activityMapper = mock(ActivityMapper.class);
        when(activityMapper.getActivity(isA(FooPlace.class))).thenReturn(fooActivity);
        when(activityMapper.getActivity(isA(BarPlace.class))).thenReturn(barActivity);

        ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
        activityManager.setViewPort(viewPort);

        eventBus.fireEvent(new PlaceChangeEvent(new FooPlace()));

        // WHEN
        eventBus.fireEvent(new TestEvent());

        // THEN
        assertEquals(1, fooActivity.invocationCount);
        assertEquals(0, barActivity.invocationCount);

        // WHEN
        eventBus.fireEvent(new PlaceChangeEvent(new BarPlace()));
        eventBus.fireEvent(new TestEvent());

        // THEN
        assertEquals(2, fooActivity.invocationCount);
        assertEquals(1, barActivity.invocationCount);

    }
}
