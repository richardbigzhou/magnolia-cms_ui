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
package info.magnolia.ui.framework.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.activity.ActivityMapper;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLauncherLayout;
import info.magnolia.ui.framework.app.layout.AppLauncherLayoutImpl;
import info.magnolia.ui.framework.app.layout.AppLauncherLayoutManager;
import info.magnolia.ui.framework.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.event.SimpleSystemEventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.place.PlaceChangeEvent;
import info.magnolia.ui.framework.view.ViewPort;

/**
 * Test case for {@link AppActivityManager}.
 *
 * @version $Id$
 */
public class AppActivityManagerTest {

    private static class TestPlace extends Place {
    }

    private static class TestActivity extends AbstractActivity {

        private final List<String> eventSink;

        public TestActivity(List<String> eventSink) {
            this.eventSink = eventSink;
        }

        @Override
        public void start(ViewPort viewPort, EventBus eventBus, Place place) {
            eventSink.add("activity-start");
        }
    }

    private static class TestApp implements AppLifecycle {

        private final List<String> eventSink;

        public TestApp(List<String> eventSink) {
            this.eventSink = eventSink;
        }

        @Override
        public void start() {
            eventSink.add("app-start");
        }

        @Override
        public void focus() {
            eventSink.add("app-focus");
        }

        @Override
        public void stop() {
            eventSink.add("app-stop");
        }
    }

    @Test
    public void testStartsAppWhenActivityStarts() {

        // GIVEN
        ArrayList<String> eventSink = new ArrayList<String>();

        SimpleEventBus eventBus = new SimpleEventBus();
        SimpleSystemEventBus systemEventBus = new SimpleSystemEventBus();

        AppLauncherLayoutManager appLauncherLayoutManager = mock(AppLauncherLayoutManager.class);

        ConfiguredAppDescriptor descriptor = new ConfiguredAppDescriptor();
        descriptor.setName("test-app");
        descriptor.setAppClass(TestApp.class);
        PlaceActivityMapping placeActivityMapping = new PlaceActivityMapping();
        placeActivityMapping.setPlace(TestPlace.class);
        placeActivityMapping.setActivity(TestActivity.class);
        descriptor.addActivityMapping(placeActivityMapping);

        AppCategory category = new AppCategory();
        category.setLabel("TEST-category");
        category.addApp(descriptor);

        HashMap<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put(category.getLabel(), category);

        AppLauncherLayout layout = new AppLauncherLayoutImpl(categories);
        when(appLauncherLayoutManager.getLayout()).thenReturn(layout);

        ActivityMapper activityMapper = mock(ActivityMapper.class);
        when(activityMapper.getActivity(any(Place.class))).thenReturn(new TestActivity(eventSink));

        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(eq(TestApp.class))).thenReturn(new TestApp(eventSink));
        AppController appController = new AppControllerImpl(appLauncherLayoutManager, componentProvider, eventBus, systemEventBus);

        AppActivityManager activityManager = new AppActivityManager(activityMapper, eventBus, appLauncherLayoutManager, appController);
        activityManager.setViewPort(mock(ViewPort.class));

        // WHEN
        eventBus.fireEvent(new PlaceChangeEvent(new TestPlace()));

        // THEN
        assertEquals(2, eventSink.size());
        assertEquals("app-start", eventSink.get(0));
        assertEquals("activity-start", eventSink.get(1));
    }
}
