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

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.Registry;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.api.app.registry.AppRegistryEvent;
import info.magnolia.ui.api.app.registry.AppRegistryEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link AppLauncherLayoutManager} implementation.
 */
@Singleton
public class AppLauncherLayoutManagerImpl implements AppLauncherLayoutManager {

    private final Logger log = LoggerFactory.getLogger(AppLauncherLayoutManagerImpl.class);

    private final AppDescriptorRegistry appDescriptorRegistry;

    private final EventBus systemEventBus;

    private final AtomicReference<AppLauncherLayoutDefinition> layoutDefinitionReference = new AtomicReference<AppLauncherLayoutDefinition>();

    private final Provider<I18nizer> i18nizerProvider;

    @Inject
    public AppLauncherLayoutManagerImpl(AppDescriptorRegistry appDescriptorRegistry, @Named(SystemEventBus.NAME) EventBus systemEventBus, Provider<I18nizer> i18nizerProvider) {
        this.appDescriptorRegistry = appDescriptorRegistry;
        this.systemEventBus = systemEventBus;
        this.i18nizerProvider = i18nizerProvider;

        /**
         * Propagate events from {@link info.magnolia.ui.api.app.registry.AppDescriptorRegistry} to notify listeners
         * that the layout has changed.
         */
        systemEventBus.addHandler(AppRegistryEvent.class, new AppRegistryEventHandler() {

            @Override
            public void onAppRegistered(AppRegistryEvent event) {
                String name = event.getAppDescriptorMetadata().getName();
                log.debug("Got AppRegistryEvent." + event.getEventType() + " for app: " + name);
                sendChangedEvent();
            }

            @Override
            public void onAppReregistered(AppRegistryEvent event) {
                String name = event.getAppDescriptorMetadata().getName();
                log.debug("Got AppRegistryEvent." + event.getEventType() + " for app: " + name);
                sendChangedEvent();
            }

            @Override
            public void onAppUnregistered(AppRegistryEvent event) {
                String name = event.getAppDescriptorMetadata().getName();
                log.debug("Got AppRegistryEvent." + event.getEventType() + " for app: " + name);
                sendChangedEvent();
            }
        });
    }

    @Deprecated
    @Override
    public AppLauncherLayout getLayoutForCurrentUser() {
        return getLayoutForUser(MgnlContext.getUser());
    }

    @Override
    public AppLauncherLayout getLayoutForUser(User user) {
        AppLauncherLayoutDefinition layoutDefinition = layoutDefinitionReference.get();
        if (layoutDefinition == null) {
            return new AppLauncherLayout();
        }

        AppLauncherLayout layout = new AppLauncherLayout();
        for (AppLauncherGroupDefinition groupDefinition : layoutDefinition.getGroups()) {

            if (!isGroupVisibleForUser(groupDefinition, user)) {
                continue;
            }

            List<AppLauncherGroupEntry> entries = new ArrayList<>();
            for (AppLauncherGroupEntryDefinition entryDefinition : groupDefinition.getApps()) {
                AppDescriptor appDescriptor;
                try {
                    final DefinitionProvider<AppDescriptor> definitionProvider = appDescriptorRegistry.getProvider(entryDefinition.getName());
                    if (!definitionProvider.isValid()) {
                        // app-descriptor is not valid, won't add it to the launcher
                        continue;
                    }
                    appDescriptor = i18nizerProvider.get().decorate(definitionProvider.get());
                } catch (Registry.NoSuchDefinitionException e) {
                    log.warn("Definition not found: {}", e.getMessage());
                    continue;
                } catch (IllegalStateException e) {
                    log.warn("Encountered IllegalStateException while getting appDescriptor: ", e.getMessage(), e);
                    continue;
                }

                if (StringUtils.isBlank(appDescriptor.getLabel()) || StringUtils.isBlank(appDescriptor.getIcon())) {
                    log.warn("Label and/or icon for app [{}] are blank. App won't be displayed in the app launcher. Please either define them in the configuration tree or in the app's i18n properties file.", entryDefinition.getName());
                    continue;
                }

                if (isAppVisibleForUser(entryDefinition, appDescriptor, user)) {
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
        this.layoutDefinitionReference.set(i18nizerProvider.get().decorate(layout));
        sendChangedEvent();
    }

    private boolean isAppVisibleForUser(AppLauncherGroupEntryDefinition entry, AppDescriptor appDescriptor, User user) {
        AccessDefinition permissions = appDescriptor.getPermissions();
        return entry.isEnabled() && appDescriptor.isEnabled() && (permissions == null || permissions.hasAccess(user));
    }

    private boolean isGroupVisibleForUser(AppLauncherGroupDefinition group, User user) {
        AccessDefinition permissions = group.getPermissions();
        return (permissions == null || permissions.hasAccess(user));
    }

    /**
     * Sends an event on the system event bus.
     */
    private void sendChangedEvent() {
        log.debug("Sending AppLauncherLayoutChangedEvent on the system bus");
        systemEventBus.fireEvent(new AppLauncherLayoutChangedEvent());
    }
}
