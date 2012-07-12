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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.app.registry.AppRegistryEvent;
import info.magnolia.ui.framework.app.registry.AppRegistryEventHandler;
import info.magnolia.ui.framework.event.SystemEventBus;

/**
 * Default {@link AppLayoutManager} implementation.
 */
@Singleton
public class AppLayoutManagerImpl implements AppLayoutManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AppDescriptorRegistry appDescriptorRegistry;
    private SystemEventBus systemEventBus;
    private AtomicReference<AppLayout> layout = new AtomicReference<AppLayout>();

    @Inject
    public AppLayoutManagerImpl(AppDescriptorRegistry appDescriptorRegistry, SystemEventBus systemEventBus) {
        this.appDescriptorRegistry = appDescriptorRegistry;
        this.systemEventBus = systemEventBus;

        /**
         * Propagate events from {@link info.magnolia.ui.framework.app.registry.AppDescriptorRegistry} to notify listeners that the layout has changed.
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
    public AppLayout getLayoutForCurrentUser() {

        AppLayout appLayout = layout.get();
        if (appLayout == null) {
            return new AppLayout();
        }

        AppLayout layoutCopy = new AppLayout();
        for (AppGroup group : appLayout.getGroups()) {
            List<AppGroupEntry> copiedEntries = new ArrayList<AppGroupEntry>();
            for (AppGroupEntry entry : group.getApps()) {
                if (isAppEnabledForCurrentUser(entry)) {
                    AppDescriptor appDescriptor;
                    try {
                        appDescriptor = appDescriptorRegistry.getAppDescriptor(entry.getName());
                    } catch (RegistrationException e) {
                        continue;
                    }
                    AppGroupEntry entryCopy = new AppGroupEntry();
                    entryCopy.setName(entry.getName());
                    entryCopy.setEnabled(entry.isEnabled());
                    entryCopy.setAppDescriptor(appDescriptor);
                    copiedEntries.add(entryCopy);
                }
            }
            if (!copiedEntries.isEmpty()) {
                AppGroup groupCopy = new AppGroup();
                groupCopy.setName(group.getName());
                groupCopy.setLabel(group.getLabel());
                groupCopy.setBackgroundColor(group.getBackgroundColor());
                groupCopy.setPermanent(group.isPermanent());
                groupCopy.setApps(copiedEntries);
                layoutCopy.addGroup(groupCopy);
            }
        }
        return layoutCopy;
    }

    @Override
    public void setLayout(AppLayout layout) {
        this.layout.set(layout);
        sendEvent();
    }

    private boolean isAppEnabledForCurrentUser(AppGroupEntry entry) {
        return entry.isEnabled() && appDescriptorRegistry.isAppDescriptorRegistered(entry.getName());
    }

    /**
     * Send an event to the system event bus.
     */
    private void sendEvent() {
        logger.debug("Sending AppLayoutChangedEvent on the system bus");
        systemEventBus.fireEvent(new AppLayoutChangedEvent());
    }
}
