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

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.launcherlayout.definition.AppLauncherGroupDefinition;
import info.magnolia.ui.framework.app.launcherlayout.definition.AppLauncherGroupEntryDefinition;
import info.magnolia.ui.framework.app.launcherlayout.definition.AppLauncherLayoutDefinition;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.app.registry.AppRegistryEvent;
import info.magnolia.ui.framework.app.registry.AppRegistryEventHandler;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.event.SystemEventBusConfigurer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link AppLauncherLayoutManager} implementation.
 */
@Singleton
public class AppLauncherLayoutManagerImpl implements AppLauncherLayoutManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AppDescriptorRegistry appDescriptorRegistry;

    private final EventBus systemEventBus;

    private final AtomicReference<AppLauncherLayoutDefinition> layoutDefinitionReference = new AtomicReference<AppLauncherLayoutDefinition>();

    @Inject
    public AppLauncherLayoutManagerImpl(AppDescriptorRegistry appDescriptorRegistry, @Named(SystemEventBusConfigurer.EVENT_BUS_NAME) EventBus systemEventBus) {
        this.appDescriptorRegistry = appDescriptorRegistry;
        this.systemEventBus = systemEventBus;

        /**
         * Propagate events from {@link info.magnolia.ui.framework.app.registry.AppDescriptorRegistry} to notify listeners
         * that the layout has changed.
         */
        systemEventBus.addHandler(AppRegistryEvent.class, new AppRegistryEventHandler() {

            @Override
            public void onAppRegistered(AppRegistryEvent event) {
                String name = event.getAppDescriptor().getName();
                logger.debug("Got AppLifecycleEvent." + event.getEventType() + " for the following appDescriptor " + name);
                sendEvent();
            }

            @Override
            public void onAppReregistered(AppRegistryEvent event) {
                String name = event.getAppDescriptor().getName();
                logger.debug("Got AppLifecycleEvent." + event.getEventType() + " for the following appDescriptor " + name);
                sendEvent();
            }

            @Override
            public void onAppUnregistered(AppRegistryEvent event) {
                String name = event.getAppDescriptor().getName();
                logger.debug("Got AppLifecycleEvent." + event.getEventType() + " for the following appDescriptor " + name);
                sendEvent();
            }
        });
    }

    @Override
    public AppLauncherLayout getLayoutForCurrentUser() {

        AppLauncherLayoutDefinition layoutDefinition = layoutDefinitionReference.get();
        if (layoutDefinition == null) {
            return new AppLauncherLayout();
        }

        AppLauncherLayout layout = new AppLauncherLayout();
        for (AppLauncherGroupDefinition groupDefinition : layoutDefinition.getGroups()) {

            List<AppLauncherGroupEntry> entries = new ArrayList<AppLauncherGroupEntry>();
            for (AppLauncherGroupEntryDefinition entryDefinition : groupDefinition.getApps()) {

                AppDescriptor appDescriptor;
                try {
                    appDescriptor = appDescriptorRegistry.getAppDescriptor(entryDefinition.getName());
                } catch (RegistrationException e) {
                    continue;
                }

                if (isAppEnabledForCurrentUser(entryDefinition)) {
                    AppLauncherGroupEntry entry = new AppLauncherGroupEntry();
                    entry.setName(entryDefinition.getName());
                    entry.setEnabled(entryDefinition.isEnabled());
                    entry.setAppDescriptor(appDescriptor);
                    entries.add(entry);
                }
            }

            if (!entries.isEmpty()) {
                AppLauncherGroup group = new AppLauncherGroup();
                group.setName(groupDefinition.getName());
                group.setLabel(groupDefinition.getLabel());
                group.setColor(groupDefinition.getColor());
                group.setPermanent(groupDefinition.isPermanent());
                group.setClientGroup(groupDefinition.isClientGroup());
                group.setApps(entries);
                layout.addGroup(group);
            }
        }
        return layout;
    }

    @Override
    public void setLayout(AppLauncherLayoutDefinition layout) {
        this.layoutDefinitionReference.set(layout);
        sendEvent();
    }

    private boolean isAppEnabledForCurrentUser(AppLauncherGroupEntryDefinition entry) {
        return entry.isEnabled();
    }

    /**
     * Send an event to the system event bus.
     */
    private void sendEvent() {
        logger.debug("Sending AppLauncherLayoutChangedEvent on the system bus");
        systemEventBus.fireEvent(new AppLauncherLayoutChangedEvent());
    }
}
