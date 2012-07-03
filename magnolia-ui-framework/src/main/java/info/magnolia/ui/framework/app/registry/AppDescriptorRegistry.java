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
package info.magnolia.ui.framework.app.registry;

import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppEventType;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.event.SystemEventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The central registry of all {@link AppDescriptor}s.
 *
 * @version $Id$
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

    private SystemEventBus eventBus;

    @Inject
    public AppDescriptorRegistry(SystemEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public AppDescriptor getAppDescriptor(String id) throws RegistrationException {
        AppDescriptorProvider provider;
        try {
            provider = registry.getRequired(id);
        } catch (RegistrationException e) {
            throw new RegistrationException("No app registered for id: " + id, e);
        }
        return provider.getAppDescriptor();
    }

    public boolean isAppDescriptorRegistered(String id) {
        return registry.get(id) != null;
    }

    /**
     * @return all AppDescriptors - in case of errors it'll just deliver the ones that are properly
     * registered and logs error's for the others.
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
        sendEvent(AppEventType.REGISTERED, Arrays.asList(provider.getAppDescriptor()));
    }

    public void unregister(String id) throws RegistrationException {
        AppDescriptorProvider toRemove;
        // synchronized to make sure we don't remove one added after the get() call
        synchronized (registry) {
            toRemove = registry.get(id);
            registry.remove(id);
        }
        sendEvent(AppEventType.UNREGISTERED, Arrays.asList(toRemove.getAppDescriptor()));
    }

    @SuppressWarnings("unchecked")
    public Set<String> unregisterAndRegister(Collection<String> registeredIds, Collection<AppDescriptorProvider> providers) throws RegistrationException {

        Collection<AppDescriptorProvider> initialProviders;
        Set<String> set;
        Collection<AppDescriptorProvider> finalProviders;

        // synchronized to make sure concurrent puts don't interfere
        synchronized (registry) {
            initialProviders = registry.values();
            set = registry.removeAndPutAll(registeredIds, providers);
            finalProviders = registry.values();
        }

        //Handle Events
        if (CollectionUtils.isSubCollection(registeredIds, set)) {
            // Add new AppDescriptor --> REGISTERED
            if(CollectionUtils.disjunction(set, registeredIds).isEmpty()) {
                // Content of one existing AppDescriptorProvider was changed
                for(AppDescriptorProvider appProvider:initialProviders) {
                    if(!finalProviders.contains(appProvider)){
                        sendEvent(AppEventType.REREGISTERED, Arrays.asList(registry.get(appProvider.getName()).getAppDescriptor()));
                    }
                }
            }else {
                // Add new AppDescriptor --> REGISTERED
                sendEvent(AppEventType.REGISTERED, getAppDescriptorsFromAppDescriptorProviders(CollectionUtils.disjunction(set, registeredIds), finalProviders));
            }
        } else if (CollectionUtils.isSubCollection(set, registeredIds)) {
            // Remove AppDescriptor --> UNREGISTERED
            sendEvent(AppEventType.UNREGISTERED, getAppDescriptorsFromAppDescriptorProviders(CollectionUtils.disjunction(registeredIds, set), initialProviders));
        } else {
            // Add and Remove AppDescriptor --> REGISTERED & UNREGISTERED.
            sendEvent(AppEventType.REGISTERED, getAppDescriptorsFromAppDescriptorProviders(CollectionUtils.disjunction(set, registeredIds), finalProviders));
            sendEvent(AppEventType.UNREGISTERED, getAppDescriptorsFromAppDescriptorProviders(CollectionUtils.disjunction(registeredIds, set), initialProviders));
        }
        return set;
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
    private void sendEvent(AppEventType eventType, Collection<AppDescriptor> appDescriptors) {
        for (AppDescriptor appDescriptor : appDescriptors) {
            eventBus.fireEvent(new AppLifecycleEvent(appDescriptor, eventType));
        }
    }
}
