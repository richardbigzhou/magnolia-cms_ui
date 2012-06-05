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

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.view.ViewPort;

/**
 * Test case for {@link ActivityMapperImpl}.
 *
 * @version $Id$
 */
public class ActivityMapperImplTest {

    public static class TestPlace extends Place {

    }

    public static class TestActivity extends AbstractActivity {

        @Override
        public void start(ViewPort viewPort, EventBus eventBus, Place place) {
        }
    }

    private ActivityMapperImpl activityMapper;

    @Before
    public void setUp() {
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(TestActivity.class)).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return new TestActivity();
            }
        });

        activityMapper = new ActivityMapperImpl(componentProvider);
    }

    @Test
    public void testMapsPlaceToActivity() {

        // GIVEN
        activityMapper.addMapping(TestPlace.class, TestActivity.class);

        // WHEN
        Activity activity1 = activityMapper.getActivity(new TestPlace());
        Activity activity2 = activityMapper.getActivity(new TestPlace());

        // THEN
        assertNotNull(activity1);
        assertNotNull(activity2);
        assertNotSame(activity1, activity2);
    }

    @Test
    public void testReturnsNullOnUnknownPlace() {

        // GIVEN
        activityMapper.addMapping(TestPlace.class, TestActivity.class);

        // WHEN
        Activity activity = activityMapper.getActivity(new Place() {
        });

        // THEN
        assertNull(activity);
    }

    @Test
    public void testKeepsActivityInistancesAroundWhenToldTo() {

        // GIVEN
        activityMapper.setLongLivingActivities(true);
        activityMapper.addMapping(TestPlace.class, TestActivity.class);

        // WHEN
        Activity activity1 = activityMapper.getActivity(new TestPlace());
        Activity activity2 = activityMapper.getActivity(new TestPlace());

        // THEN
        assertNotNull(activity1);
        assertNotNull(activity2);
        assertSame(activity1, activity2);
    }

    public class ActivityMapperWithRemoveInstanceMethodPublic extends ActivityMapperImpl {

        public ActivityMapperWithRemoveInstanceMethodPublic(ComponentProvider componentProvider) {
            super(componentProvider);
        }

        @Override
        public synchronized void removeActivityInstanceForPlace(Class<? extends Place> placeClass) {
            super.removeActivityInstanceForPlace(placeClass);
        }
    }

    @Test
    public void testReturnsNewInstanceAfterExistingRemoved() {

        // GIVEN
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(TestActivity.class)).thenAnswer(new Answer<Activity>() {
            @Override
            public Activity answer(InvocationOnMock invocation) throws Throwable {
                return new TestActivity();
            }
        });

        ActivityMapperWithRemoveInstanceMethodPublic mapper = new ActivityMapperWithRemoveInstanceMethodPublic(componentProvider);
        mapper.addMapping(TestPlace.class, TestActivity.class);

        // WHEN
        TestPlace place = new TestPlace();
        Activity activity1 = mapper.getActivity(place);
        mapper.removeActivityInstanceForPlace(TestPlace.class);
        Activity activity2 = mapper.getActivity(place);

        // THEN
        assertNotNull(activity1);
        assertNotNull(activity2);
        assertNotSame(activity1, activity2);
    }
}
