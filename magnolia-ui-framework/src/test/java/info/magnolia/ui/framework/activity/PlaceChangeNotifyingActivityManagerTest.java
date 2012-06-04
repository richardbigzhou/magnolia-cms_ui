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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceChangeEvent;
import info.magnolia.ui.framework.view.ViewPort;

/**
 * Test case for {@link PlaceChangeNotifyingActivityManager}.
 *
 * @version $Id$
 */
public class PlaceChangeNotifyingActivityManagerTest {

    public static class TestPlace extends Place {

    }

    public static class PlaceChangeRecordingActivity extends AbstractActivity {

        public TestPlace place;
        private boolean started;

        @PlaceStateHandler
        public void onPlaceChange(TestPlace place) {
            this.place = place;
        }

        @Override
        public void start(ViewPort viewPort, EventBus eventBus) {
            this.started = true;
        }
    }

    @Test
    public void testPlaceChangeOnActivityStart() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        PlaceChangeRecordingActivity activity = new PlaceChangeRecordingActivity();
        when(activityMapper.getActivity(any(Place.class))).thenReturn(activity);

        PlaceChangeNotifyingActivityManager activityManager = new PlaceChangeNotifyingActivityManager(activityMapper, eventBus);

        activityManager.setViewPort(mock(ViewPort.class));

        assertFalse(activity.started);
        assertNull(activity.place);

        // WHEN
        TestPlace place = new TestPlace();
        eventBus.fireEvent(new PlaceChangeEvent(place));

        // THEN
        assertTrue(activity.started);
        assertSame(place, activity.place);
    }

    @Test
    public void testPlaceChangeOnPlaceChangeToCurrentActivity() {

        // GIVEN
        SimpleEventBus eventBus = new SimpleEventBus();
        ActivityMapper activityMapper = mock(ActivityMapper.class);
        PlaceChangeRecordingActivity activity = new PlaceChangeRecordingActivity();
        when(activityMapper.getActivity(any(Place.class))).thenReturn(activity);

        PlaceChangeNotifyingActivityManager activityManager = new PlaceChangeNotifyingActivityManager(activityMapper, eventBus);

        activityManager.setViewPort(mock(ViewPort.class));

        assertFalse(activity.started);
        assertNull(activity.place);

        // WHEN
        TestPlace place = new TestPlace();
        eventBus.fireEvent(new PlaceChangeEvent(place));

        assertTrue(activity.started);
        assertSame(place, activity.place);
        activity.started = false;
        activity.place = null;

        place = new TestPlace();
        eventBus.fireEvent(new PlaceChangeEvent(place));

        // THEN
        assertFalse(activity.started);
        assertSame(place, activity.place);
    }
}
