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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Main Test class for {AppDescriptorRegistry}.
 */
public class AppDescriptorRegistryTest {

    private AppDescriptorRegistry appDescriptorRegistry;
    private AppLifecycleEventHandlerTest appLifecycleEventHandler;


    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        SimpleSystemEventBus eventBus = new SimpleSystemEventBus();
        appLifecycleEventHandler = new AppLifecycleEventHandlerTest();
        eventBus.addHandler(AppLifecycleEvent.class, appLifecycleEventHandler);

        appDescriptorRegistry = new AppDescriptorRegistry(eventBus);


        TypeMappingImpl typeMapping = new TypeMappingImpl();
        ComponentsTestUtil.setInstance(Content2BeanProcessor.class, new Content2BeanProcessorImpl(typeMapping));
        ComponentsTestUtil.setImplementation(AppDescriptor.class, ConfiguredAppDescriptor.class);

    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
    }

    public void testGetAppDescriptors() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);
        Collection<String> registeredIds = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);

        // WHEN
        appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);

        // THEN
        // Test get getAppDescriptors()
        Collection<AppDescriptor> res = appDescriptorRegistry.getAppDescriptors();

        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(addId, ((AppDescriptor)res.toArray()[0]).getName());
        assertEquals("catApp1", ((AppDescriptor)res.toArray()[0]).getCategoryName());
    }

    public void testGetAppDescriptor() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);
        Collection<String> registeredIds = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);

        // WHEN
        appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);

        // THEN
        // Test get getAppDescriptor(name)
        AppDescriptor appDescriptor = appDescriptorRegistry.getAppDescriptor(addId);

        assertNotNull(appDescriptor);
        assertEquals(addId, appDescriptor.getName());
        assertEquals("catApp1",  appDescriptor.getCategoryName());
    }

    @Test(expected=RegistrationException.class)
    public void testGetAppDescriptorThrowsException() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);
        Collection<String> registeredIds = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);

        // WHEN
        appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);

        // THEN
        // Test get getAppDescriptor(xx) with unregistered appName
        appDescriptorRegistry.getAppDescriptor("xx");

    }

    @Test
    public void TestUnregisterAndRegister_Initial() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN
        String addId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(addId, "catApp1", true);
        Collection<String> registeredIds = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);

        // WHEN
        Set<String> res = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(addId, res.toArray()[0]);
        assertEquals(AppEventType.REGISTERED.toString(), appLifecycleEventHandler.eventSended);
    }

    @Test
    public void TestUnregisterAndRegister_Add() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN
        String appId1 = "app1";
        String appId2 = "app2";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(appId1, "catApp1", true);
        AppDescriptorProvider appDescriptorProvider2 = createAppDescriptorProvider(appId2, "catApp2", true);
        //Add app1
        Collection<String> registeredIds = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);
        registeredIds = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);
        assertEquals(appId1, registeredIds.toArray()[0]);
        providers = Arrays.asList(appDescriptorProvider1,appDescriptorProvider2);

        // WHEN
        Set<String> res = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals(AppEventType.REGISTERED.toString(), appLifecycleEventHandler.eventSended);
        assertEquals(appDescriptorRegistry.getAppDescriptor(appId2), appLifecycleEventHandler.appDescriptor);
    }


    @Test
    public void TestUnregisterAndRegister_Remove() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN
        String appId1 = "app1";
        String appId2 = "app2";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(appId1, "catApp1", true);
        AppDescriptorProvider appDescriptorProvider2 = createAppDescriptorProvider(appId2, "catApp2", true);
        //Add app1
        Collection<String> registeredIds = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);
        registeredIds = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);
        assertEquals(appId1, registeredIds.toArray()[0]);
        //Add app2
        providers = Arrays.asList(appDescriptorProvider1,appDescriptorProvider2);
        registeredIds = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);
        providers = Arrays.asList(appDescriptorProvider1);

        // WHEN
        // Remove --> registeredIds don't contain appId2
        Set<String> res = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(AppEventType.UNREGISTERED.toString(), appLifecycleEventHandler.eventSended);
        assertEquals(appId2, appLifecycleEventHandler.appDescriptor.getName());
    }

    @Test
    public void TestUnregisterAndRegister_Update() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN
        String appId = "app1";
        AppDescriptorProvider appDescriptorProvider1 = createAppDescriptorProvider(appId, "catApp1", true);
        Collection<String> registeredIds = Arrays.asList();
        Collection<AppDescriptorProvider> providers = Arrays.asList(appDescriptorProvider1);
        registeredIds = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);
        //Change content of AppDescriptor info
        appDescriptorProvider1 = createAppDescriptorProvider(appId, "catApp2", true);
        providers = Arrays.asList(appDescriptorProvider1);
        // WHEN
        Set<String> res = appDescriptorRegistry.unregisterAndRegister(registeredIds, providers);

        // THEN
        // appDescriptorProvider1 should be registered.
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(appId, res.toArray()[0]);
        assertEquals(AppEventType.REREGISTERED.toString(), appLifecycleEventHandler.eventSended);
        assertEquals(appDescriptorRegistry.getAppDescriptor(appId), appLifecycleEventHandler.appDescriptor);
    }

    @Test
    public void TestUnregisterAndRegister_Duplicate() throws Content2BeanException, RepositoryException, RegistrationException {
        // GIVEN

        // WHEN

        // THEN
        // appDescriptorProvider1 should be registered.
    }


    private AppDescriptorProvider createAppDescriptorProvider(String name, String categoryName, boolean isEnable) throws RepositoryException, Content2BeanException{
        MockNode node = new MockNode("name");
        node.setProperty("name", name);
        node.setProperty("categoryName", categoryName);
        node.setProperty("enabled", isEnable);
        return new ConfiguredAppDescriptorProvider(node);
    }

    private class AppLifecycleEventHandlerTest extends AppLifecycleEventHandler.Adapter {
        private AppDescriptor appDescriptor = null;;
        private String eventSended = "";
        @Override
        public void onAppRegistered(AppLifecycleEvent event) {
            appDescriptor = event.getAppDescriptor();
            eventSended = event.getEventType().name();
        }

        @Override
        public void onAppUnregistered(AppLifecycleEvent event) {
            appDescriptor = event.getAppDescriptor();
            eventSended = event.getEventType().name();
        }

        @Override
        public void onAppReRegistered(AppLifecycleEvent event) {
            appDescriptor = event.getAppDescriptor();
            eventSended = event.getEventType().name();
        }
    }


}
