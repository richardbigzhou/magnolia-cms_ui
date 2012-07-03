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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import info.magnolia.content2bean.Content2BeanException;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.registry.RegistrationException;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.AppEventType;
import info.magnolia.ui.framework.app.AppLifecycleEvent;
import info.magnolia.ui.framework.app.AppLifecycleEventHandler;
import info.magnolia.ui.framework.event.SimpleSystemEventBus;

/**
 * Main Test class for {@link AppDescriptorRegistry}.
 */
public class AppDescriptorRegistryTest {

    private AppDescriptorRegistry appDescriptorRegistry;
    private EventCollectingAppLifecycleEventHandler eventHandler;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        SimpleSystemEventBus eventBus = new SimpleSystemEventBus();
        eventHandler = new EventCollectingAppLifecycleEventHandler();
        eventBus.addHandler(AppLifecycleEvent.class, eventHandler);

        appDescriptorRegistry = new AppDescriptorRegistry(eventBus);

        TypeMappingImpl typeMapping = new TypeMappingImpl();
        ComponentsTestUtil.setInstance(Content2BeanProcessor.class, new Content2BeanProcessorImpl(typeMapping));
        ComponentsTestUtil.setImplementation(AppDescriptor.class, ConfiguredAppDescriptor.class);
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
    }

    @Test
    public void testGetAppDescriptors() throws Content2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);

        appDescriptorRegistry.unregisterAndRegister(new ArrayList<String>(), Arrays.asList(appDescriptorProvider1));

        // WHEN
        Collection<AppDescriptor> descriptors = appDescriptorRegistry.getAppDescriptors();

        // THEN
        assertNotNull(descriptors);
        assertEquals(1, descriptors.size());
        assertEquals(addId, ((AppDescriptor) descriptors.toArray()[0]).getName());
        assertEquals("catApp1", ((AppDescriptor) descriptors.toArray()[0]).getCategoryName());
    }

    @Test
    public void testGetAppDescriptor() throws Content2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);

        appDescriptorRegistry.unregisterAndRegister(new ArrayList<String>(), Arrays.asList(appDescriptorProvider1));

        // WHEN
        AppDescriptor appDescriptor = appDescriptorRegistry.getAppDescriptor(addId);

        // THEN
        assertNotNull(appDescriptor);
        assertEquals(addId, appDescriptor.getName());
        assertEquals("catApp1", appDescriptor.getCategoryName());
    }

    @Test(expected = RegistrationException.class)
    public void testGetAppDescriptorThrowsExceptionWhenAppNotFound() throws Content2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);

        appDescriptorRegistry.unregisterAndRegister(new ArrayList<String>(), Arrays.asList(appDescriptorProvider1));

        // WHEN
        appDescriptorRegistry.getAppDescriptor("xx");
    }

    @Test
    public void testUnregisterAndRegisterWhenEmpty() throws Content2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);

        // WHEN
        Set<String> registeredNames = appDescriptorRegistry.unregisterAndRegister(new ArrayList<String>(), Arrays.asList(appDescriptorProvider1));

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(registeredNames);
        assertEquals(1, registeredNames.size());
        assertEquals(addId, registeredNames.toArray()[0]);
        assertEquals(1, eventHandler.events.size());
        assertEquals(AppEventType.REGISTERED, eventHandler.events.get(0).getEventType());
    }

    @Test
    public void testUnregisterAndRegisterWhenAdding() throws Content2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String appName1 = "app1";
        String appName2 = "app2";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(appName1, "catApp1", true);
        AppDescriptorProvider appDescriptorProvider2 = createAppDescriptorProvider(appName2, "catApp2", true);

        //Add app1
        Set<String> registeredNames = appDescriptorRegistry.unregisterAndRegister(new ArrayList<String>(), Arrays.asList(appDescriptorProvider1));
        assertEquals(1, registeredNames.size());
        assertTrue(registeredNames.contains(appName1));
        eventHandler.clear();

        // WHEN
        Set<String> registeredNames2 = appDescriptorRegistry.unregisterAndRegister(registeredNames, Arrays.asList(appDescriptorProvider1, appDescriptorProvider2));

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(registeredNames2);
        assertEquals(2, registeredNames2.size());
        assertEquals(2, eventHandler.events.size());
        assertContainsEvent(AppEventType.REREGISTERED, appName1);
        assertContainsEvent(AppEventType.REGISTERED, appName2);
    }

    @Test
    public void testUnregisterAndRegisterWhenRemoving() throws Content2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String appName1 = "app1";
        String appName2 = "app2";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(appName1, "catApp1", true);
        AppDescriptorProvider appDescriptorProvider2 = createAppDescriptorProvider(appName2, "catApp2", true);

        //Add app1
        Collection<String> registeredNames = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);
        registeredNames = appDescriptorRegistry.unregisterAndRegister(registeredNames, providers);
        assertEquals(appName1, registeredNames.toArray()[0]);

        //Add app2
        providers = Arrays.asList(appDescriptorProvider1, appDescriptorProvider2);
        registeredNames = appDescriptorRegistry.unregisterAndRegister(registeredNames, providers);

        eventHandler.clear();

        // WHEN
        // Remove --> registeredNames don't contain appName2
        providers = Arrays.asList(appDescriptorProvider1);
        Set<String> res = appDescriptorRegistry.unregisterAndRegister(registeredNames, providers);

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(2, eventHandler.events.size());
        assertContainsEvent(AppEventType.REREGISTERED, appName1);
        assertContainsEvent(AppEventType.UNREGISTERED, appName2);
    }

    @Test
    public void TestUnregisterAndRegisterWhenUpdating() throws Content2BeanException, RepositoryException, RegistrationException {

        // GIVEN
        String appName = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(appName, "catApp1", true);
        Set<String> registeredNames = appDescriptorRegistry.unregisterAndRegister(new ArrayList<String>(), Arrays.asList(appDescriptorProvider1));

        //Change content of AppDescriptor info
        AppDescriptorProvider appDescriptorProvider2 = createAppDescriptorProvider(appName, "catApp2", true);

        eventHandler.clear();

        // WHEN
        Set<String> registeredNames2 = appDescriptorRegistry.unregisterAndRegister(registeredNames, Arrays.asList(appDescriptorProvider2));

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(registeredNames2);
        assertEquals(1, registeredNames2.size());
        assertEquals(appName, registeredNames2.toArray()[0]);
        assertEquals(1, eventHandler.events.size());
        assertEquals(AppEventType.REREGISTERED, eventHandler.events.get(0).getEventType());
        assertEquals(appDescriptorRegistry.getAppDescriptor(appName), eventHandler.events.get(0).getAppDescriptor());
    }

    @Test
    public void testUnregisterAndRegisterInComplexCase() throws RepositoryException, Content2BeanException, RegistrationException {

        // GIVEN
        AppDescriptorProvider appThatStays = createAppDescriptorProvider("appThatStays", "category", true);
        AppDescriptorProvider appThatGoesAway = createAppDescriptorProvider("appThatGoesAway", "category", true);
        AppDescriptorProvider appThatAppears = createAppDescriptorProvider("appThatAppears", "category", true);

        Set<String> registeredNames = appDescriptorRegistry.unregisterAndRegister(new ArrayList<String>(), Arrays.asList(appThatStays, appThatGoesAway));

        assertEquals(2, registeredNames.size());
        assertTrue(registeredNames.contains(appThatStays.getName()));
        assertTrue(registeredNames.contains(appThatGoesAway.getName()));

        assertEquals(2, eventHandler.events.size());
        assertContainsEvent(AppEventType.REGISTERED, appThatStays.getName());
        assertContainsEvent(AppEventType.REGISTERED, appThatGoesAway.getName());

        eventHandler.clear();

        // WHEN
        Set<String> registeredNames2 = appDescriptorRegistry.unregisterAndRegister(registeredNames, Arrays.asList(appThatStays, appThatAppears));

        // THEN
        assertEquals(2, registeredNames2.size());
        assertTrue(registeredNames2.contains(appThatStays.getName()));
        assertTrue(registeredNames2.contains(appThatAppears.getName()));
        assertEquals(3, eventHandler.events.size());
        assertContainsEvent(AppEventType.REREGISTERED, appThatStays.getName());
        assertContainsEvent(AppEventType.UNREGISTERED, appThatGoesAway.getName());
        assertContainsEvent(AppEventType.REGISTERED, appThatAppears.getName());
    }

    @Test
    public void testRegister() throws RepositoryException, Content2BeanException, RegistrationException {

        // GIVEN
        String appName = "app1";
        AppDescriptorProvider provider = createAppDescriptorProvider(appName, "catApp1", true);

        assertFalse(appDescriptorRegistry.isAppDescriptorRegistered(appName));

        // WHEN
        appDescriptorRegistry.register(provider);

        // THEN
        assertTrue(appDescriptorRegistry.isAppDescriptorRegistered(appName));
    }

    @Test
    public void testUnregister() throws RepositoryException, Content2BeanException, RegistrationException {

        // GIVEN
        String appName = "app1";
        AppDescriptorProvider provider = createAppDescriptorProvider(appName, "catApp1", true);

        assertFalse(appDescriptorRegistry.isAppDescriptorRegistered(appName));

        appDescriptorRegistry.register(provider);

        assertTrue(appDescriptorRegistry.isAppDescriptorRegistered(appName));

        // WHEN
        appDescriptorRegistry.unregister(appName);

        // THEN
        assertFalse(appDescriptorRegistry.isAppDescriptorRegistered(appName));
    }

    private AppDescriptorProvider createAppDescriptorProvider(String name, String categoryName, boolean enabled) throws RepositoryException, Content2BeanException {
        MockNode node = new MockNode("name");
        node.setProperty("name", name);
        node.setProperty("categoryName", categoryName);
        node.setProperty("enabled", enabled);
        return new ConfiguredAppDescriptorProvider(node);
    }

    private void assertContainsEvent(AppEventType appEventType, String name) {
        for (AppLifecycleEvent event : eventHandler.events) {
            if (event.getEventType() == appEventType && event.getAppDescriptor().getName().equals(name)) {
                return;
            }
        }
        fail("Expected event " + appEventType.name() + " for app " + name);
    }

    private class EventCollectingAppLifecycleEventHandler extends AppLifecycleEventHandler.Adapter {

        List<AppLifecycleEvent> events = new ArrayList<AppLifecycleEvent>();

        public void clear() {
            events.clear();
        }

        @Override
        public void onAppRegistered(AppLifecycleEvent event) {
            events.add(event);
        }

        @Override
        public void onAppUnregistered(AppLifecycleEvent event) {
            events.add(event);
        }

        @Override
        public void onAppReRegistered(AppLifecycleEvent event) {
            events.add(event);
        }
    }
}
