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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.Registry;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.PreConfiguredBeanUtils;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.registry.RegistrationException;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.api.app.AppDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Main Test class for {@link AppDescriptorRegistry}.
 */
public class AppDescriptorRegistryTest {

    private static final String MODULE = "module";

    private AppDescriptorRegistry appDescriptorRegistry;

    private EventCollectingAppRegistryEventHandler eventHandler;

    @Before
    public void setUp() throws Exception {
        EventBus eventBus = new SimpleEventBus();
        eventHandler = new EventCollectingAppRegistryEventHandler();
        eventBus.addHandler(AppRegistryEvent.class, eventHandler);

        appDescriptorRegistry = new AppDescriptorRegistry(eventBus, mock(ModuleRegistry.class));

        TypeMappingImpl typeMapping = new TypeMappingImpl();
        Node2BeanTransformerImpl transformer = new Node2BeanTransformerImpl(new PreConfiguredBeanUtils());

        ComponentsTestUtil.setInstance(Node2BeanProcessor.class, new Node2BeanProcessorImpl(typeMapping, transformer));
        ComponentsTestUtil.setImplementation(AppDescriptor.class, ConfiguredAppDescriptor.class);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
    }

    @Test
    public void testGetAppDescriptors() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(addId, true);

        appDescriptorRegistry.unregisterAndRegister(Collections.EMPTY_LIST, Arrays.asList(appDescriptorProvider1));

        // WHEN
        Collection<AppDescriptor> descriptors = appDescriptorRegistry.getAllDefinitions();

        // THEN
        assertNotNull(descriptors);
        assertEquals(1, descriptors.size());
        assertEquals(addId, ((AppDescriptor) descriptors.toArray()[0]).getName());
    }

    @Test
    public void testGetAppDescriptor() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(addId, true);
        appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(), Arrays.asList(appDescriptorProvider1));

        // WHEN
        AppDescriptor appDescriptor = appDescriptorRegistry.getProvider(addId).get();

        // THEN
        assertNotNull(appDescriptor);
        assertEquals(addId, appDescriptor.getName());
    }

    @Test(expected = Registry.NoSuchDefinitionException.class)
    public void testGetAppDescriptorThrowsExceptionWhenAppNotFound() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(addId, true);
        appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(), Arrays.asList(appDescriptorProvider1));

        // WHEN
        appDescriptorRegistry.getProvider("xx");
    }

    @Test
    public void testUnregisterAndRegisterWhenEmpty() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(addId, true);

        // WHEN
        Set<DefinitionMetadata> registeredMetadata = appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(), Arrays.asList(appDescriptorProvider1));

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(registeredMetadata);
        assertEquals(1, registeredMetadata.size());
        assertEquals(addId, registeredMetadata.iterator().next().getName());
        assertEquals(1, eventHandler.events.size());
        assertEquals(AppRegistryEventType.REGISTERED, eventHandler.events.get(0).getEventType());
    }

    @Test
    public void testUnregisterAndRegisterWhenAdding() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        final String appName1 = "app1";
        String appName2 = "app2";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(appName1, true);
        DefinitionProvider<AppDescriptor> appDescriptorProvider2 = createAppDescriptorProvider(appName2, true);

        // Add app1
        Set<DefinitionMetadata> registeredMetadata = appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(), Arrays.asList(appDescriptorProvider1));
        assertEquals(1, registeredMetadata.size());
        assertNotNull(appDescriptorRegistry.getProvider(appName1));

        eventHandler.clear();

        // WHEN
        Set<DefinitionMetadata> registeredNames2 = appDescriptorRegistry.unregisterAndRegister(registeredMetadata, Arrays.asList(appDescriptorProvider1, appDescriptorProvider2));

        // THEN
        assertNotNull(registeredNames2);
        assertEquals(2, registeredNames2.size());
        assertEquals(1, eventHandler.events.size());
        assertContainsEvent(AppRegistryEventType.REGISTERED, appName2);
    }

    @Test
    public void testUnregisterAndRegisterWhenRemoving() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String appName1 = "app1";
        String appName2 = "app2";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(appName1, true);
        DefinitionProvider<AppDescriptor> appDescriptorProvider2 = createAppDescriptorProvider(appName2, true);

        // Add app1
        Collection<DefinitionMetadata> registeredMetadata = Arrays.asList();
        Collection<DefinitionProvider<AppDescriptor>> providers = Arrays.asList(appDescriptorProvider1);
        registeredMetadata = appDescriptorRegistry.unregisterAndRegister(registeredMetadata, providers);
        assertEquals(appName1, registeredMetadata.iterator().next().getName());

        // Add app2
        providers = Arrays.asList(appDescriptorProvider1, appDescriptorProvider2);
        registeredMetadata = appDescriptorRegistry.unregisterAndRegister(registeredMetadata, providers);

        eventHandler.clear();

        // WHEN
        // Remove --> registeredMetadata don't contain appName2
        providers = Arrays.asList(appDescriptorProvider1);
        Set<DefinitionMetadata> res = appDescriptorRegistry.unregisterAndRegister(registeredMetadata, providers);

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(1, eventHandler.events.size());
        assertContainsEvent(AppRegistryEventType.UNREGISTERED, appName2);
    }

    @Test
    public void TestUnregisterAndRegisterWhenUpdating() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String appName = "app1";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(appName, true);
        Set<DefinitionMetadata> registeredNames = appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(), Arrays.asList(appDescriptorProvider1));

        // Change content of AppDescriptor info
        DefinitionProvider<AppDescriptor> appDescriptorProvider2 = createAppDescriptorProvider(appName, false);

        eventHandler.clear();

        // WHEN
        Set<DefinitionMetadata> registeredNames2 = appDescriptorRegistry.unregisterAndRegister(registeredNames, Arrays.asList(appDescriptorProvider2));

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(registeredNames2);
        assertEquals(1, registeredNames2.size());
        assertEquals(appName, registeredNames2.iterator().next().getName());
        assertEquals(1, eventHandler.events.size());
        assertEquals(AppRegistryEventType.REREGISTERED, eventHandler.events.get(0).getEventType());
        assertEquals(appDescriptorRegistry.getProvider(appName).getMetadata(), eventHandler.events.get(0).getAppDescriptorMetadata());
    }

    @Test
    public void testUnregisterAndRegisterInComplexCase() throws RepositoryException, Node2BeanException, RegistrationException {

        // GIVEN
        DefinitionProvider<AppDescriptor> appThatStays = createAppDescriptorProvider("appThatStays", true);
        DefinitionProvider<AppDescriptor> appThatGoesAway = createAppDescriptorProvider("appThatGoesAway", true);
        DefinitionProvider<AppDescriptor> appThatAppears = createAppDescriptorProvider("appThatAppears", true);

        Set<DefinitionMetadata> registeredMetadata = appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(), Arrays.asList(appThatStays, appThatGoesAway));

        assertEquals(2, registeredMetadata.size());
        assertTrue(registeredMetadata.contains(appThatStays.getMetadata()));
        assertTrue(registeredMetadata.contains(appThatGoesAway.getMetadata()));

        assertEquals(2, eventHandler.events.size());
        assertContainsEvent(AppRegistryEventType.REGISTERED, appThatStays.getMetadata().getName());
        assertContainsEvent(AppRegistryEventType.REGISTERED, appThatGoesAway.getMetadata().getName());

        eventHandler.clear();

        // WHEN
        Set<DefinitionMetadata> registeredNames2 = appDescriptorRegistry.unregisterAndRegister(registeredMetadata, Arrays.asList(appThatStays, appThatAppears));

        // THEN
        assertEquals(2, registeredNames2.size());
        assertTrue(registeredNames2.contains(appThatStays.getMetadata()));
        assertTrue(registeredNames2.contains(appThatAppears.getMetadata()));
        assertEquals(2, eventHandler.events.size());
        assertContainsEvent(AppRegistryEventType.UNREGISTERED, appThatGoesAway.getMetadata().getName());
        assertContainsEvent(AppRegistryEventType.REGISTERED, appThatAppears.getMetadata().getName());
    }

    @Test
    public void testUnregisterAndRegisterWhenAddingFromMultipleSources() throws Node2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String appName1 = "app1";
        String appName2 = "app2";
        DefinitionProvider<AppDescriptor> appDescriptorProvider1 = createAppDescriptorProvider(appName1, true);

        Set<DefinitionMetadata> registeredNames = appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(), Arrays.asList(appDescriptorProvider1));
        assertEquals(1, registeredNames.size());
        assertTrue(registeredNames.contains(appDescriptorProvider1.getMetadata()));
        eventHandler.clear();

        // WHEN
        // first app gets reregistered due to change in in active state
        appDescriptorProvider1 = createAppDescriptorProvider(appName1, false);
        // second app gets registered
        DefinitionProvider<AppDescriptor> appDescriptorProvider2 = createAppDescriptorProvider(appName2, true);

        Set<DefinitionMetadata> registeredNames2 = appDescriptorRegistry.unregisterAndRegister(Arrays.<DefinitionMetadata>asList(appDescriptorProvider1.getMetadata()), Arrays.asList(appDescriptorProvider1, appDescriptorProvider2));

        // THEN
        assertNotNull(registeredNames2);
        assertEquals(2, registeredNames2.size());
        assertEquals(2, eventHandler.events.size());
        assertContainsEvent(AppRegistryEventType.REREGISTERED, appName1);
        assertContainsEvent(AppRegistryEventType.REGISTERED, appName2);
    }

    @Test
    public void testRegister() throws RepositoryException, Node2BeanException, RegistrationException {

        // GIVEN
        String appName = "app1";
        DefinitionProvider<AppDescriptor> provider = createAppDescriptorProvider(appName, true);

        assertFalse(appDescriptorRegistry.isAppDescriptorRegistered(appName));

        // WHEN
        appDescriptorRegistry.register(provider);

        // THEN
        assertTrue(appDescriptorRegistry.isAppDescriptorRegistered(appName));
    }


    @Test
    public void testUnregister() throws RepositoryException, Node2BeanException, RegistrationException {

        // GIVEN
        String appName = "app1";
        DefinitionProvider<AppDescriptor> provider = createAppDescriptorProvider(appName, true);

        assertFalse(appDescriptorRegistry.isAppDescriptorRegistered(appName));

        appDescriptorRegistry.register(provider);

        assertTrue(appDescriptorRegistry.isAppDescriptorRegistered(appName));

        // WHEN
        appDescriptorRegistry.unregister(appName);

        // THEN
        assertFalse(appDescriptorRegistry.isAppDescriptorRegistered(appName));
    }

    private DefinitionProvider<AppDescriptor> createAppDescriptorProvider(String name, boolean enabled) throws RepositoryException, Node2BeanException {
        ConfiguredAppDescriptor appDescriptor = new ConfiguredAppDescriptor();
        appDescriptor.setEnabled(enabled);
        appDescriptor.setName(name);
        return new DummyAppDescriptorProvider(name, MODULE, "/apps", appDescriptor);
    }

    private void assertContainsEvent(AppRegistryEventType appEventType, String name) {
        for (AppRegistryEvent event : eventHandler.events) {
            if (event.getEventType() == appEventType && event.getAppDescriptorMetadata().getName().equals(name)) {
                return;
            }
        }
        fail("Expected event " + appEventType.name() + " for app " + name);
    }


    private class EventCollectingAppRegistryEventHandler implements AppRegistryEventHandler {

        List<AppRegistryEvent> events = new ArrayList<AppRegistryEvent>();

        public void clear() {
            events.clear();
        }

        @Override
        public void onAppRegistered(AppRegistryEvent event) {
            events.add(event);
        }

        @Override
        public void onAppUnregistered(AppRegistryEvent event) {
            events.add(event);
        }

        @Override
        public void onAppReregistered(AppRegistryEvent event) {
            events.add(event);
        }
    }
}
