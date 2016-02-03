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
package info.magnolia.ui.api.app.launcherlayout;

import static org.junit.Assert.*;

import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link info.magnolia.ui.api.app.launcherlayout.AppLauncherLayout}.
 */
public class AppLauncherLayoutTest {

    private AppLauncherLayout applayout;
    private AppLauncherGroup appGroup1;
    private AppLauncherGroup appGroup2;
    private AppDescriptor appDescriptor1;
    private AppDescriptor appDescriptor2;
    private AppDescriptor appDescriptor3;

    @Before
    public void setUp() throws Exception {
        // Init
        appDescriptor1 = createAppDescriptor("appDescriptor1");
        appDescriptor2 = createAppDescriptor("appDescriptor2");
        appDescriptor3 = createAppDescriptor("appDescriptor3");
        appGroup1 = createAppGroup("appGroup1", appDescriptor1, appDescriptor2);
        appGroup2 = createAppGroup("appGroup2", appDescriptor3);
        applayout = new AppLauncherLayout();
        applayout.addGroup(appGroup1);
        applayout.addGroup(appGroup2);
    }

    @Test
    public void testGetGroups() {
        // GIVEN

        // WHEN
        Collection<AppLauncherGroup> groups = applayout.getGroups();

        // THEN
        assertNotNull(groups);
        assertEquals(2, groups.size());
    }

    @Test
    public void testGetGroup() {
        // GIVEN

        // WHEN
        AppLauncherGroup res = applayout.getGroup("appGroup1");

        // THEN
        assertNotNull(res);
        assertEquals(res, appGroup1);

        // WHEN
        res = applayout.getGroup("x");

        assertNull(res);
    }

    @Test
    public void testGetGroupEntry() {
        // GIVEN

        // WHEN
        AppLauncherGroupEntry res = applayout.getAppGroupEntry("appDescriptor3");

        // THEN
        assertNotNull(res);
        assertEquals(appDescriptor3, res.getAppDescriptor());

        // WHEN
        res = applayout.getAppGroupEntry("x");
        assertNull(res);
    }

    @Test
    public void testGetAppDescriptor() {
        // GIVEN

        // WHEN
        AppDescriptor res = applayout.getAppDescriptor("appDescriptor3");

        // THEN
        assertNotNull(res);
        assertEquals(appDescriptor3, res);

        // WHEN
        res = applayout.getAppDescriptor("x");
        assertNull(res);
    }

    @Test
    public void testContainsApp() {
        // GIVEN

        // WHEN
        boolean res = applayout.containsApp("appDescriptor3");

        // THEN
        assertEquals(res, true);

        // WHEN
        res = applayout.containsApp("x");

        // THEN
        assertEquals(res, false);
    }

    public static AppLauncherGroup createAppGroup(String name, AppDescriptor... appDescriptors) {
        AppLauncherGroup group = new AppLauncherGroup();
        group.setName(name);
        for (AppDescriptor descriptor : appDescriptors) {
            AppLauncherGroupEntry entry = new AppLauncherGroupEntry();
            entry.setName(descriptor.getName());
            entry.setAppDescriptor(descriptor);
            entry.setEnabled(true);
            group.addApp(entry);
        }
        return group;
    }

    public static ConfiguredAppDescriptor createAppDescriptor(String name) {
        ConfiguredAppDescriptor descriptor = new ConfiguredAppDescriptor();
        descriptor.setName(name);
        descriptor.setIcon("an-icon");
        descriptor.setLabel(name);
        return descriptor;
    }
}
