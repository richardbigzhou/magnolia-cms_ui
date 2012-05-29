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
package info.magnolia.ui.admincentral.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default AppRegistry implementation.
 *
 * @version $Id$
 */
@Singleton
public class AppRegistryImpl implements AppRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<AppCategory> categories = new ArrayList<AppCategory>();

    @Override
    public List<AppCategory> getCategories() {
        return categories;
    }

    public void addCategory(AppCategory category) {

        // Filter out disabled apps and apps with identical names
        Set<String> clearedNames = new HashSet<String>();
        Iterator<AppDescriptor> iterator = category.getApps().iterator();
        while (iterator.hasNext()) {
            AppDescriptor descriptor = iterator.next();
            if (!descriptor.isEnabled()) {
                iterator.remove();
            } else if (isAppDescriptionRegistered(descriptor.getName()) || clearedNames.contains(descriptor.getName())) {
                logger.warn("App \"" + descriptor.getName() + "\" already registered will not be added twice");
                iterator.remove();
            } else {
                clearedNames.add(descriptor.getName());
            }
        }

        this.categories.add(category);
    }

    @Override
    public AppDescriptor getAppDescriptor(String name) {
        AppDescriptor descriptor = internalGetAppDescriptor(name);

        if (descriptor == null) {
            throw new IllegalArgumentException("No app registered with name \"" + name + "\".");
        }

        return descriptor;
    }

    @Override
    public boolean isAppDescriptionRegistered(String name) {
        AppDescriptor descriptor = internalGetAppDescriptor(name);
        return descriptor != null;
    }

    private AppDescriptor internalGetAppDescriptor(String name) {
        for (AppCategory category : categories) {
            for (AppDescriptor descriptor : category.getApps()) {
                if (descriptor.getName().equals(name)) {
                    return descriptor;
                }
            }
        }
        return null;
    }
}
