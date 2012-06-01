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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppEventType;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SystemEventBus;

/**
 * Default AppRegistry implementation.
 *
 * @version $Id$
 */
@Singleton
public class AppLauncherLayoutImpl implements AppLauncherLayout {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, AppCategory> categories = new HashMap<String, AppCategory>();

    private AppDescriptorRegistry appDescriptorRegistry;

    private EventBus eventBus;

    @Inject
    public AppLauncherLayoutImpl(AppDescriptorRegistry appDescriptorRegistry, EventBus eventBus, SystemEventBus systemEventBus) {
        this.appDescriptorRegistry = appDescriptorRegistry;
        this.eventBus = eventBus;

        // Build layout
        reloadCategories();

        // Register for app registration events
        systemEventBus.addHandler(AppLifecycleEvent.class, new AppLifecycleEventHandler.Adapter() {

            /**
             * Propagate Event to the AppShell event bus if this app has to be
             * displayed to the User.
             * Add App to Category.
             */
            @Override
            public void onAppRegistered(AppLifecycleEvent event) {
                if (hasToAddApp(event.getAppDescriptor())) {
                    handleCategory(event.getAppDescriptor());
                    sendEvent(AppEventType.REGISTERED, event.getAppDescriptor());
                }
            }

            /**
             * Propagate Event to the AppShell event bus if this app has to be
             * displayed to the User.
             * Remove App from Category.
             */
            @Override
            public void onAppUnregistered(AppLifecycleEvent event) {
                if (isAppDescriptionRegistered(event.getAppDescriptor().getName())) {
                    removeAppFromCategory(event.getAppDescriptor());
                    sendEvent(AppEventType.UNREGISTERED, event.getAppDescriptor());
                }
            }

        });
    }

    @Override
    public Collection<AppCategory> reloadCategories() {
        categories.clear();
        Collection<AppDescriptor> appDescriptors = this.appDescriptorRegistry.getAppDescriptors();
        for (AppDescriptor app : appDescriptors) {
            if (hasToAddApp(app)) {
                handleCategory(app);
            }
        }
        return getCategories();
    }

    @Override
    public Collection<AppCategory> getCategories() {
        return categories.values();
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

    /**
     * Add the AppDescriptor to an Existing or newly created AppCategory.
     */
    private void handleCategory(AppDescriptor app) {
        AppCategory category;
        String catName = app.getCategoryName();
        logger.debug("Handle app " + app.getName() + " for category " + catName);
        if (categories.containsKey(catName)) {
            // Add to Category
            category = categories.get(catName);
        } else {
            // Create
            category = new AppCategory();
            category.setLabel(catName);
            categories.put(catName, category);
        }
        category.addApp(app);
    }

    /**
     * Filter out disabled apps and apps with identical names.
     */
    private boolean hasToAddApp(AppDescriptor app) {
        // Filter out disabled apps and apps with identical names
        if (!app.isEnabled() || isAppDescriptionRegistered(app.getName()) || !isThisAppRegisteredForUser(app)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if the user has registered for this App.
     */
    private boolean isThisAppRegisteredForUser(AppDescriptor app) {
        return true;
    }

    /**
     * Remove an App from a Category.
     * Remove the Category if empty.
     */
    private void removeAppFromCategory(AppDescriptor app) {
        AppCategory category = categories.get(app.getCategoryName());
        for (AppDescriptor appDescriptor : category.getApps()) {
            if (appDescriptor.getName().equals(app.getName())) {
                category.getApps().remove(appDescriptor);
                break;
            }
        }
        if (category.getApps().isEmpty()) {
            categories.remove(app.getCategoryName());
        }
    }

    /**
     * Send an Event to the ShellApp EventBuss.
     */
    private void sendEvent(AppEventType eventType, AppDescriptor appDescriptor) {
        eventBus.fireEvent(new AppLifecycleEvent(appDescriptor, eventType));
    }

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