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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.registry.RegistrationException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.Assertion;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockObservationManager;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.api.app.AppDescriptor;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for {@link info.magnolia.ui.api.app.registry.ConfiguredAppDescriptorManager}.
 */
public class ConfiguredAppDescriptorManagerTest {

    private ModuleRegistry moduleRegistry;
    private AppDescriptorRegistry appRegistry;
    private Session session;

    @Before
    public void setUp() throws Exception {
        // INIT
        EventBus eventBus = new SimpleEventBus();
        ComponentsTestUtil.setImplementation(AppDescriptor.class, ConfiguredAppDescriptor.class);
        session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                "/modules/aModule/apps/app1.name=appNameA",
                "/modules/bModule/apps/app1.name=appNameB"
        );
        MockUtil.initMockContext();
        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        Set<String> moduleNames = new LinkedHashSet<String>();
        moduleNames.add("aModule");
        moduleNames.add("bModule");
        moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleNames()).thenReturn(moduleNames);

        appRegistry = new AppDescriptorRegistry(eventBus);

        TypeMappingImpl typeMapping = new TypeMappingImpl();
        Node2BeanTransformerImpl transformer = new Node2BeanTransformerImpl();
        ComponentsTestUtil.setInstance(Node2BeanProcessor.class, new Node2BeanProcessorImpl(typeMapping, transformer));
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    @Ignore("ConfiguredAppDescriptorManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testAppDescriptorOnStart() throws RegistrationException {
        // GIVEN
        ConfiguredAppDescriptorManager addDescriptorManager = new ConfiguredAppDescriptorManager();

        // WHEN
        addDescriptorManager.start();

        // THEN
        AppDescriptor a = appRegistry.getAppDescriptor("appNameA");
        assertNotNull(a);
        assertEquals("appNameA", a.getName());

        AppDescriptor b = appRegistry.getAppDescriptor("appNameB");
        assertNotNull(b);
        assertEquals("appNameB", b.getName());

    }

    @Test
    @Ignore("ConfiguredAppDescriptorManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testAppDescriptorReloadsOnChange() throws RepositoryException, RegistrationException, InterruptedException {
        // GIVEN
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        ConfiguredAppDescriptorManager addDescriptorManager = new ConfiguredAppDescriptorManager();

        // WHEN
        addDescriptorManager.start();

        // THEN
        // Make sure app a is there.
        AppDescriptor a = appRegistry.getAppDescriptor("appNameA");
        assertNotNull(a);

        // WHEN
        // Remove app a:
        session.getNode("/modules/aModule/apps/app1").remove();
        observationManager.fireEvent(MockEvent.nodeRemoved("/modules/aModule/apps/app1"));

        // THEN a is gone
        assertAppIsRemoved("appNameA");

        // WHEN
        // Add a property and fire event
        session.getNode("/modules/bModule/apps/app1").setProperty("theme", "aTheme");
        observationManager.fireEvent(MockEvent.propertyAdded("/modules/bModule/apps/app1"));

        // THEN
        // app b has his property modified.
        TestUtil.delayedAssert(new Assertion() {

            @Override
            public void evaluate() throws RegistrationException {
                AppDescriptor b = appRegistry.getAppDescriptor("appNameB");
                assertEquals("aTheme", b.getTheme());
            }
        });

        // WHEN
        // Rename app b, change the app name.
        session.getNode("/modules/bModule/apps/app1").getProperty("name").setValue("appNameB_B");
        MockEvent event = new MockEvent();
        event.setType(Event.PROPERTY_CHANGED);
        event.setPath("/modules/bModule/apps/app1");

        observationManager.fireEvent(event);

        // THEN
        // app b has gone.
        assertAppIsRemoved("appNameB");

        AppDescriptor b = appRegistry.getAppDescriptor("appNameB_B");
        assertNotNull(b);
    }

    private void assertAppIsRemoved(final String name) {
        TestUtil.delayedAssert(new Assertion() {
            @Override
            public void evaluate() {
                appRegistry.getAppDescriptor(name);
                fail();
            }
        });
    }
}
