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

import java.util.HashMap;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.activity.Activity;
import info.magnolia.ui.framework.app.layout.AppCategory;
import info.magnolia.ui.framework.app.layout.AppLayout;
import info.magnolia.ui.framework.app.layout.AppLayoutImpl;
import info.magnolia.ui.framework.app.layout.AppLayoutManager;
import info.magnolia.ui.framework.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SimpleEventBus;
import info.magnolia.ui.framework.place.Place;
import info.magnolia.ui.framework.view.ViewPort;

/**
 * Test case for {@link AppActivityMapper}.
 *
 * @version $Id$
 */
public class AppActivityMapperTest {

    private static class TestPlace extends Place {
    }

    private static class TestActivity extends AbstractActivity {

        @Override
        public void start(ViewPort viewPort, EventBus eventBus, Place place) {
        }
    }

    @Test
    public void testReturnsNewInstanceAfterAppStopped() {

        // GIVEN
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        when(componentProvider.newInstance(TestActivity.class)).thenAnswer(new Answer<Activity>() {
            @Override
            public Activity answer(InvocationOnMock invocation) throws Throwable {
                return new TestActivity();
            }
        });

        SimpleEventBus eventBus = new SimpleEventBus();

        AppLayoutManager appLayoutManager = mock(AppLayoutManager.class);

        ConfiguredAppDescriptor descriptor = new ConfiguredAppDescriptor();
        descriptor.setName("test-app");
        PlaceActivityMapping placeActivityMapping = new PlaceActivityMapping();
        placeActivityMapping.setPlace(TestPlace.class);
        placeActivityMapping.setActivity(TestActivity.class);
        descriptor.addActivityMapping(placeActivityMapping);

        AppCategory category = new AppCategory();
        category.setLabel("TEST-category");
        category.addApp(descriptor);

        HashMap<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put(category.getLabel(), category);

        AppLayout layout = new AppLayoutImpl(categories);
        when(appLayoutManager.getLayout()).thenReturn(layout);

        AppActivityMapper mapper = new AppActivityMapper(componentProvider, appLayoutManager, eventBus);
        mapper.addMapping(TestPlace.class, TestActivity.class);

        // WHEN
        Activity activity1 = mapper.getActivity(new TestPlace());
        Activity activity2 = mapper.getActivity(new TestPlace());

        // THEN
        assertNotNull(activity1);
        assertNotNull(activity2);
        assertSame(activity1, activity2);

        // WHEN
        eventBus.fireEvent(new AppLifecycleEvent(category.getApps().get(0), AppEventType.STOPPED));

        Activity activity3 = mapper.getActivity(new TestPlace());

        // THEN
        assertNotNull(activity3);
        assertNotSame(activity3, activity1);
    }
}
