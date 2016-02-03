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
package info.magnolia.ui.api.app.registry;

import info.magnolia.event.EventBus;
import info.magnolia.event.SystemEventBus;
import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.ui.api.app.AppDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The central registry for {@link AppDescriptor}s. Fires {@link AppRegistryEvent} when the registry changes.
 *
 * @see AppDescriptor
 * @see AppDescriptorProvider
 * @see AppRegistryEvent
 */
@Singleton
public class AppDescriptorRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RegistryMap<String, AppDescriptorProvider> registry = new RegistryMap<String, AppDescriptorProvider>() {

        @Override
        protected String keyFromValue(AppDescriptorProvider provider) {
            return provider.getName();
        }
    };

    private EventBus systemEventBus;

    @Inject
    public AppDescriptorRegistry(@Named(SystemEventBus.NAME) EventBus systemEventBus) {
        this.systemEventBus = systemEventBus;
    }

    public AppDescriptor getAppDescriptor(String name) throws RegistrationException {
        AppDescriptorProvider provider;
        try {
            provider = registry.getRequired(name);
        } catch (RegistrationException e) {
            throw new RegistrationException("No app registered for name: " + name, e);
        }
        return provider.getAppDescriptor();
    }

    public boolean isAppDescriptorRegistered(String name) {
        return registry.get(name) != null;
    }

    /**
     * @return all AppDescriptors - in case of errors it'll just deliver the ones that are properly
     *         registered and logs error's for the others.
     */
    public Collection<AppDescriptor> getAppDescriptors() {
        final Collection<AppDescriptor> descriptors = new ArrayList<AppDescriptor>();
        for (AppDescriptorProvider provider : registry.values()) {
            try {
                final AppDescriptor appDescriptor = provider.getAppDescriptor();
                if (appDescriptor == null) {
                    logger.error("Provider's AppDescriptor is null: " + provider);
                } else {
                    descriptors.add(appDescriptor);
                }
            } catch (RegistrationException e) {
                logger.error("Failed to read AppDescriptor definition from " + provider + ".", e);
            }
        }
        return descriptors;
    }

    public void register(AppDescriptorProvider provider) throws RegistrationException {
        registry.put(provider);
        sendEvent(AppRegistryEventType.REGISTERED, Collections.singleton(provider.getAppDescriptor()));
    }

    public void unregister(String name) throws RegistrationException {
        AppDescriptorProvider toRemove;
        // synchronized to make sure we don't remove one added after the get() call
        synchronized (registry) {
            toRemove = registry.get(name);
            registry.remove(name);
        }
        sendEvent(AppRegistryEventType.UNREGISTERED, Collections.singleton(toRemove.getAppDescriptor()));
    }

    @SuppressWarnings("unchecked")
    public Set<String> unregisterAndRegister(Collection<String> namesToUnregister, Collection<AppDescriptorProvider> providersToRegister) throws RegistrationException {

        Collection<String> namesBefore;
        Collection<String> namesAfter;
        Collection<AppDescriptorProvider> providersBefore;
        Set<String> registeredNames;

        // synchronized to make sure concurrent puts don't interfere
        synchronized (registry) {
            namesBefore = registry.keySet();
            providersBefore = registry.values();
            registeredNames = registry.removeAndPutAll(namesToUnregister, providersToRegister);
            namesAfter = registry.keySet();
        }

        Collection<String> added = CollectionUtils.subtract(namesAfter, namesBefore);
        Collection<String> removed = CollectionUtils.subtract(namesBefore, namesAfter);
        Collection<String> kept = CollectionUtils.subtract(namesBefore, removed);
        Collection<String> changed = getAppsThatHaveChanged(kept, providersBefore, providersToRegister);

        sendEvent(AppRegistryEventType.REGISTERED, getAppDescriptorsFromAppDescriptorProviders(added, providersToRegister));
        sendEvent(AppRegistryEventType.UNREGISTERED, getAppDescriptorsFromAppDescriptorProviders(removed, providersBefore));
        sendEvent(AppRegistryEventType.REREGISTERED, getAppDescriptorsFromAppDescriptorProviders(changed, providersToRegister));

        return registeredNames;
    }

    private Collection<String> getAppsThatHaveChanged(Collection<String> names, Collection<AppDescriptorProvider> before, Collection<AppDescriptorProvider> after) {
        ArrayList<String> changed = new ArrayList<String>();
        for (String name : names) {
            if (!getAppDescriptor(name, before).equals(getAppDescriptor(name, after))) {
                changed.add(name);
            }
        }
        return changed;
    }

    private AppDescriptorProvider getAppDescriptor(String name, Collection<AppDescriptorProvider> providers) {
        for (AppDescriptorProvider provider : providers) {
            if (provider.getName().equals(name)) {
                return provider;
            }
        }
        return null;
    }

    private Collection<AppDescriptor> getAppDescriptorsFromAppDescriptorProviders(Collection<String> names, Collection<AppDescriptorProvider> providers) throws RegistrationException {
        ArrayList<AppDescriptor> descriptors = new ArrayList<AppDescriptor>();
        for (AppDescriptorProvider provider : providers) {
            if (names.contains(provider.getName())) {
                descriptors.add(provider.getAppDescriptor());
            }
        }
        return descriptors;
    }

    /**
     * Send an event to the system event bus.
     */
    private void sendEvent(AppRegistryEventType eventType, Collection<AppDescriptor> appDescriptors) {
        for (AppDescriptor appDescriptor : appDescriptors) {
            systemEventBus.fireEvent(new AppRegistryEvent(appDescriptor, eventType));
        }
    }
}
