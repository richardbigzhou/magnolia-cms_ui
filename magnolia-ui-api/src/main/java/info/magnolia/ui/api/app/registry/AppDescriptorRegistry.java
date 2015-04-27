/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.api.app.registry;

import info.magnolia.config.registry.AbstractRegistry;
import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionMetadataBuilder;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.ui.api.app.AppDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;

/**
 * The central registry for {@link AppDescriptor}s. Fires {@link AppRegistryEvent} when the registry changes.
 *
 * @see AppDescriptor
 * @see AppRegistryEvent
 */
@Singleton
public class AppDescriptorRegistry extends AbstractRegistry<AppDescriptor> {

    private EventBus systemEventBus;

    @Inject
    public AppDescriptorRegistry(@Named(SystemEventBus.NAME) EventBus systemEventBus) {
        this.systemEventBus = systemEventBus;
    }

    @Override
    public void register(DefinitionProvider<AppDescriptor> provider) {
        super.register(provider);
        sendEvent(AppRegistryEventType.REGISTERED, Collections.singleton(provider.getMetadata()));
    }

    //TODO implement unregister method properly once it is present in the parent class.
    public void unregister(String name) {
        DefinitionProvider<AppDescriptor> toRemove = getProvider(name);
        getRegistryMap().remove(toRemove.getMetadata());
        sendEvent(AppRegistryEventType.UNREGISTERED, Collections.singleton(toRemove.getMetadata()));
    }

    /**
     * @deprecated since 5.4 - use {@link AbstractRegistry#getProvider(String)} method instead.
     */
    @Deprecated
    public AppDescriptor getAppDescriptor(String name) {
        return getProvider(name).get();
    }

    /**
     * @deprecated since 5.4 - use {@link AbstractRegistry#getProvider(String)} method instead.
     */
    @Deprecated
    public boolean isAppDescriptorRegistered(String name) {
        try {
            getProvider(name);
        } catch (NoSuchDefinitionException | IllegalStateException e) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<DefinitionMetadata> unregisterAndRegister(Collection<DefinitionMetadata> metaDataToUnregister, Collection<DefinitionProvider<AppDescriptor>> providersToRegister) {

        Collection<DefinitionMetadata> metadataBefore = getRegistryMap().keySet();
        Collection<DefinitionProvider<AppDescriptor>> providersBefore = getRegistryMap().values();

        Set<DefinitionMetadata> registeredMetaData = super.unregisterAndRegister(metaDataToUnregister, providersToRegister);

        Collection<DefinitionMetadata> metadataAfter = getRegistryMap().keySet();

        Collection<DefinitionMetadata> added = CollectionUtils.subtract(metadataAfter, metadataBefore);
        Collection<DefinitionMetadata> removed = CollectionUtils.subtract(metadataBefore, metadataAfter);
        Collection<DefinitionMetadata> kept = CollectionUtils.subtract(metadataBefore, removed);
        Collection<DefinitionMetadata> changed = getAppsThatHaveChanged(kept, providersBefore, providersToRegister);

        sendEvent(AppRegistryEventType.REGISTERED, added);
        sendEvent(AppRegistryEventType.UNREGISTERED, removed);
        sendEvent(AppRegistryEventType.REREGISTERED, changed);

        return registeredMetaData;
    }

    @Override
    public DefinitionType type() {
        return DefinitionTypes.APP;
    }

    @Override
    public DefinitionMetadataBuilder newMetadataBuilder() {
        return DefinitionMetadataBuilder.usingNameAsId();
    }

    private Collection<DefinitionMetadata> getAppsThatHaveChanged(Collection<DefinitionMetadata> kept, Collection<DefinitionProvider<AppDescriptor>> providersBefore, Collection<DefinitionProvider<AppDescriptor>> providersToRegister) {
        final List<DefinitionMetadata> changed = new ArrayList<>();
        for (DefinitionMetadata metadata : kept) {
            if (!getAppDescriptor(metadata, providersBefore).equals(getAppDescriptor(metadata, providersToRegister))) {
                changed.add(metadata);
            }
        }
        return changed;
    }

    private AppDescriptor getAppDescriptor(DefinitionMetadata metadata, Collection<DefinitionProvider<AppDescriptor>> providers) {
        for (DefinitionProvider<AppDescriptor> provider : providers) {
            if (provider.getMetadata().equals(metadata)) {
                return provider.get();
            }
        }
        return null;
    }

    /**
     * Send an event to the system event bus.
     */
    private void sendEvent(AppRegistryEventType eventType, Collection<DefinitionMetadata> appDescriptors) {
        for (DefinitionMetadata appMetadata : appDescriptors) {
            systemEventBus.fireEvent(new AppRegistryEvent(appMetadata, eventType));
        }
    }
}