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
package info.magnolia.ui.framework.app;

import info.magnolia.ui.api.app.App;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherGroup;
import info.magnolia.ui.api.app.launcherlayout.AppLauncherGroupEntry;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;

import java.util.Map;

/**
 * Utility Class for the App TestCases.
 */
public class AppTestUtility {

    /**
     * Create a AppDescriptor.
     */
    public static AppDescriptor createAppDescriptor(String startLibell, Class<? extends App> appClass) {
        ConfiguredAppDescriptor descriptor = new ConfiguredAppDescriptor();
        descriptor.setAppClass(appClass);
        descriptor.setIcon(startLibell + "_icon");
        descriptor.setLabel(startLibell + "_label");
        descriptor.setName(startLibell + "_name");
        return descriptor;
    }

    public static AppLauncherGroup createAppGroup(String name, AppDescriptor... descriptors) {
        AppLauncherGroup group = new AppLauncherGroup();
        group.setName(name);
        group.setLabel(name);
        for (AppDescriptor descriptor : descriptors) {
            AppLauncherGroupEntry entry = new AppLauncherGroupEntry();
            entry.setName(descriptor.getName());
            entry.setAppDescriptor(descriptor);
            group.addApp(entry);
        }
        return group;
    }

    /**
     * Create a AppDescriptor.
     */
    public static SubAppDescriptor createSubAppDescriptor(String startLibell, Class<? extends SubApp> subAppClass, boolean isDefault) {
        ConfiguredSubAppDescriptor descriptor = new ConfiguredSubAppDescriptor();
        descriptor.setSubAppClass(subAppClass);
        descriptor.setIcon(startLibell + "_icon");
        descriptor.setLabel(startLibell + "_label");
        descriptor.setName(startLibell + "_name");
        return descriptor;
    }

    public static AppDescriptor createAppDescriptorWithSubApps(String startLibell, Class<? extends App> appClass, Map<String, SubAppDescriptor> subApps) {
        ConfiguredAppDescriptor appDescriptor = (ConfiguredAppDescriptor) createAppDescriptor(startLibell, appClass);
        appDescriptor.setSubApps(subApps);
        return appDescriptor;
    }
}
