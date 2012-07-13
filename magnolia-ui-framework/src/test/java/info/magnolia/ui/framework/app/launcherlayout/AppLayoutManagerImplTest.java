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
package info.magnolia.ui.framework.app.launcherlayout;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.launcherlayout.definition.AppLauncherGroupDefinition;
import info.magnolia.ui.framework.app.launcherlayout.definition.ConfiguredAppLauncherGroupDefinition;
import info.magnolia.ui.framework.app.launcherlayout.definition.ConfiguredAppLauncherGroupEntryDefinition;
import info.magnolia.ui.framework.app.launcherlayout.definition.ConfiguredAppLauncherLayoutDefinition;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.app.registry.AppRegistryEvent;
import info.magnolia.ui.framework.app.registry.AppRegistryEventType;
import info.magnolia.ui.framework.event.SimpleSystemEventBus;

/**
 * Test case for {@link AppLauncherLayoutManagerImpl}.
 */
public class AppLayoutManagerImplTest {

    private AppLauncherGroupDefinition appGroup1;
    private AppLauncherGroupDefinition appGroup2;
    private AppDescriptor appDescriptor1;
    private AppDescriptor appDescriptor2;
    private AppDescriptor appDescriptor3;
    private SimpleSystemEventBus systemEventBus;
    private AppLauncherLayoutManagerImpl appLayoutManager;

    @Before
    public void setUp() throws Exception {
        //Init
        appDescriptor1 = AppLauncherLayoutTest.createAppDescriptor("appDescriptor1", "appGroup1");
        appDescriptor2 = AppLauncherLayoutTest.createAppDescriptor("appDescriptor2", "appGroup1");
        appDescriptor3 = AppLauncherLayoutTest.createAppDescriptor("appDescriptor3", "appGroup2");
        appGroup1 = createAppGroup("appGroup1", "appDescriptor1", "appDescriptor2");
        appGroup2 = createAppGroup("appGroup2", "appDescriptor3");

        ConfiguredAppLauncherLayoutDefinition layoutDefinition = new ConfiguredAppLauncherLayoutDefinition();
        layoutDefinition.addGroup(appGroup1);
        layoutDefinition.addGroup(appGroup2);

        ArrayList<AppDescriptor> descriptors = new ArrayList<AppDescriptor>();
        descriptors.add(appDescriptor1);
        descriptors.add(appDescriptor2);
        descriptors.add(appDescriptor3);

        AppDescriptorRegistry registry = mock(AppDescriptorRegistry.class);
        when(registry.getAppDescriptors()).thenReturn(descriptors);

        when(registry.isAppDescriptorRegistered(eq("appDescriptor1"))).thenReturn(true);
        when(registry.isAppDescriptorRegistered(eq("appDescriptor2"))).thenReturn(true);
        when(registry.isAppDescriptorRegistered(eq("appDescriptor3"))).thenReturn(true);

        systemEventBus = new SimpleSystemEventBus();

        appLayoutManager = new AppLauncherLayoutManagerImpl(registry, systemEventBus);
        appLayoutManager.setLayout(layoutDefinition);
    }

    @Test
    public void testGetAppLayout() {

        // WHEN
        AppLauncherLayout layout = appLayoutManager.getLayoutForCurrentUser();

        // THEN
        assertEquals(2, layout.getGroups().size());
    }

    @Test
    public void testSendsEvents() {

        final ArrayList<AppLauncherLayoutChangedEvent> events = new ArrayList<AppLauncherLayoutChangedEvent>();
        systemEventBus.addHandler(AppLauncherLayoutChangedEvent.class, new AppLauncherLayoutChangedEventHandler() {

            @Override
            public void onAppLayoutChanged(AppLauncherLayoutChangedEvent event) {
                events.add(event);
            }
        });

        // WHEN
        systemEventBus.fireEvent(new AppRegistryEvent(appDescriptor1, AppRegistryEventType.REGISTERED));
        systemEventBus.fireEvent(new AppRegistryEvent(appDescriptor2, AppRegistryEventType.REREGISTERED));
        systemEventBus.fireEvent(new AppRegistryEvent(appDescriptor3, AppRegistryEventType.UNREGISTERED));

        // THEN
        assertEquals(3, events.size());
    }

    public static AppLauncherGroupDefinition createAppGroup(String name, String... appNames) {
        ConfiguredAppLauncherGroupDefinition group = new ConfiguredAppLauncherGroupDefinition();
        group.setName(name);
        for (String appName : appNames) {
            ConfiguredAppLauncherGroupEntryDefinition entry = new ConfiguredAppLauncherGroupEntryDefinition();
            entry.setName(appName);
            entry.setEnabled(true);
            group.addApp(entry);
        }
        return group;
    }
}
