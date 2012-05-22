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
package info.magnolia.m5admincentral.app;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Singleton;

/**
 * Default AppRegistry implementation.
 *
 * @version $Id$
 */
@Singleton
public class AppRegistryImpl implements AppRegistry {

    private final Map<String, AppDescriptor> apps;

    /**
     * Default constructor.
     */
    public AppRegistryImpl() {
        // Synchronize the Map.
        apps = Collections.synchronizedMap(new LinkedHashMap<String, AppDescriptor>());
    }

    public void addAppDescriptor(AppDescriptor descriptor) {
        this.apps.put(descriptor.getName(), descriptor);
    }

    @Override
    public Collection<AppDescriptor> getAppDescriptors() {
        return apps.values();
    }

    /**
     *
     * @throws IllegalArgumentException: If key don't exist.
     */
    @Override
    public AppDescriptor getAppDescriptor(String name) {
        if(!apps.containsKey(name)) {
            throw new IllegalArgumentException("No App's registered with name \"" + name + "\".");
        }
        return apps.get(name);
    }

    /**
     *
     * @throws IllegalArgumentException: In case of the Registry already contains an App with the same name.
     */
    @Override
    public void registerAppDescription(String key, AppDescriptor value) {
        if(apps.containsKey(key)) {
            throw new IllegalArgumentException("Can't register this App's. Another Apps is already registered with name \"" + key + "\".");
        }
        apps.put(key, value);
    }

    /**
     *
     * @throws IllegalArgumentException: In case of the Registry don't contains an App with this name.
     */
    @Override
    public AppDescriptor unregisterAppDescription(String name) {
        if(!apps.containsKey(name)) {
            throw new IllegalArgumentException("Can't Un register this App's. No Apps define in Registery with name  \"" + name + "\".");
        }
        return apps.remove(name);
    }

    @Override
    public boolean isAppDescriptionRegistered(String name) {
        return apps.containsKey(name);
    }
}
