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

import info.magnolia.ui.framework.app.AppDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default AppRegistry implementation.
 * Simple POJO bean containing the user registered AppCategories and App.
 */
public class AppLayoutImpl implements AppLayout {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, AppCategory> categories = new HashMap<String, AppCategory>();

    public AppLayoutImpl(Map<String, AppCategory> categories) {
        this.categories = categories;
    }

    @Override
    public Collection<AppCategory> getCategories() {
        return categories.values();
    }

    @Override
    public AppCategory getCategory(String name) throws IllegalArgumentException {
        AppCategory category = this.categories.get(name);
        if (category == null) {
            throw new IllegalArgumentException("No Category registered with name \"" + name + "\".");
        }
        return category;
    }

    @Override
    public AppDescriptor getAppDescriptor(String name) throws IllegalArgumentException {
        AppDescriptor descriptor = internalGetAppDescriptor(name);
        if (descriptor == null) {
            throw new IllegalArgumentException("No app registered with name \"" + name + "\".");
        }
        return descriptor;
    }

    @Override
    public boolean isAppAlreadyRegistered(String name) {
        AppDescriptor descriptor = internalGetAppDescriptor(name);
        return descriptor != null;
    }

    /**
     * Return the AppDescriptor corresponding to the given name.
     * Return null if no AppDescriptor founded.
     */
    private AppDescriptor internalGetAppDescriptor(String name) {
        for (AppCategory category : categories.values()) {
            for (AppDescriptor descriptor : category.getApps()) {
                if (descriptor.getName().equals(name)) {
                    logger.debug("Found AppDescriptor " + descriptor.getName() + " in category " + category.getLabel());
                    return descriptor;
                }
            }
        }
        return null;
    }



}
