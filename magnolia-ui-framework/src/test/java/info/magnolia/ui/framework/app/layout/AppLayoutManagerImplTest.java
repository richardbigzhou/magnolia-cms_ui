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
package info.magnolia.ui.framework.app.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.layout.event.LayoutEvent;
import info.magnolia.ui.framework.app.layout.event.LayoutEventHandler;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.app.registry.AppRegistryEvent;
import info.magnolia.ui.framework.app.registry.AppRegistryEventType;
import info.magnolia.ui.framework.event.SimpleSystemEventBus;

/**
 * Test case for {@link AppLayoutManagerImpl}.
 */
public class AppLayoutManagerImplTest {

    private AppCategory appCategory1;
    private AppCategory appCategory2;
    private AppDescriptor appDescriptor1;
    private AppDescriptor appDescriptor2;
    private AppDescriptor appDescriptor3;
    private SimpleSystemEventBus systemEventBus;
    private AppLayoutManagerImpl appLayoutManager;

    @Before
    public void setUp() throws Exception {
        //Init
        appDescriptor1 = AppLayoutImplTest.createAppDescriptor("appDescriptor1", "appCategory1");
        appDescriptor2 = AppLayoutImplTest.createAppDescriptor("appDescriptor2", "appCategory1");
        appDescriptor3 = AppLayoutImplTest.createAppDescriptor("appDescriptor3", "appCategory2");
        appCategory1 =  AppLayoutImplTest.createAppCategory("appCategory1", appDescriptor1, appDescriptor2);
        appCategory2 =  AppLayoutImplTest.createAppCategory("appCategory2", appDescriptor3);
        Map<String, AppCategory> categories = new HashMap<String, AppCategory>();
        categories.put("appCategory1", appCategory1);
        categories.put("appCategory2", appCategory2);

        ArrayList<AppDescriptor> descriptors = new ArrayList<AppDescriptor>();
        descriptors.add(appDescriptor1);
        descriptors.add(appDescriptor2);
        descriptors.add(appDescriptor3);

        systemEventBus = new SimpleSystemEventBus();

        AppDescriptorRegistry registry = mock(AppDescriptorRegistry.class);
        when(registry.getAppDescriptors()).thenReturn(descriptors);

        appLayoutManager = new AppLayoutManagerImpl(registry, systemEventBus);
    }

    @Test
    public void testGetAppLayout() {

        // WHEN
        AppLayout layout = appLayoutManager.getLayout();

        // THEN
        assertEquals(2, layout.getCategories().size());
    }

    @Test
    public void testSendsEvents() {

        final ArrayList<LayoutEvent> events = new ArrayList<LayoutEvent>();
        systemEventBus.addHandler(LayoutEvent.class, new LayoutEventHandler() {

            @Override
            public void onReloadApp(LayoutEvent event) {
                events.add(event);
            }
        });

        // WHEN
        systemEventBus.fireEvent(new AppRegistryEvent(appDescriptor1, AppRegistryEventType.REGISTERED));
        systemEventBus.fireEvent(new AppRegistryEvent(appDescriptor2, AppRegistryEventType.REREGISTERED));
        systemEventBus.fireEvent(new AppRegistryEvent(appDescriptor3, AppRegistryEventType.UNREGISTERED));

        // THEN
        assertEquals(3, events.size());
        assertEquals("appDescriptor1", events.get(0).getAppName());
        assertEquals("appDescriptor2", events.get(1).getAppName());
        assertEquals("appDescriptor3", events.get(2).getAppName());
    }
}
