/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.message.registry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.operations.AccessDefinition;
import info.magnolia.cms.security.operations.ConfiguredAccessDefinition;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.node2bean.impl.Node2BeanProcessorImpl;
import info.magnolia.jcr.node2bean.impl.Node2BeanTransformerImpl;
import info.magnolia.jcr.node2bean.impl.TypeMappingImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.registry.RegistrationException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.Assertion;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.TestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockObservationManager;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ConfiguredItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.junit.Before;
import org.junit.Test;

/**
 * ConfiguredMessageViewDefinitionManagerTest.
 */
public class ConfiguredMessageViewDefinitionManagerTest extends MgnlTestCase {

    private static final String A_MESSAGE_VIEW_PATH = "/modules/aModule/" + ConfiguredMessageViewDefinitionManager.MESSAGE_VIEW_CONFIG_NODE_NAME + "/aMessageView";
    private static final String B_MESSAGE_VIEW_PATH = "/modules/bModule/" + ConfiguredMessageViewDefinitionManager.MESSAGE_VIEW_CONFIG_NODE_NAME + "/bMessageView";
    private static final String C_MESSAGE_VIEW_PATH = "/modules/bModule/" + ConfiguredMessageViewDefinitionManager.MESSAGE_VIEW_CONFIG_NODE_NAME + "/bMessageView";

    private Session session;
    private ModuleRegistry moduleRegistry;
    private ItemViewDefinitionRegistry messageViewRegistry;


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(AccessDefinition.class, ConfiguredAccessDefinition.class);
        ComponentsTestUtil.setImplementation(AvailabilityDefinition.class, ConfiguredAvailabilityDefinition.class);

        ComponentsTestUtil.setImplementation(ItemViewDefinition.class, ConfiguredItemViewDefinition.class);
        ComponentsTestUtil.setImplementation(FormDefinition.class, ConfiguredFormDefinition.class);
        ComponentsTestUtil.setImplementation(TabDefinition.class, ConfiguredTabDefinition.class);
        ComponentsTestUtil.setImplementation(ActionDefinition.class, ConfiguredActionDefinition.class);

        session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                A_MESSAGE_VIEW_PATH + ".class=" + ConfiguredItemViewDefinition.class.getName(),
                A_MESSAGE_VIEW_PATH + "/form/tabs/taba",
                A_MESSAGE_VIEW_PATH + "/form/tabs/taba.label=labelA",
                B_MESSAGE_VIEW_PATH + "/actions/actionb",
                B_MESSAGE_VIEW_PATH + "/actions/actionb.label=labelB",
                C_MESSAGE_VIEW_PATH + "/form/tabs/tabc"
                );
        MockUtil.initMockContext();
        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        Set<String> moduleNames = new LinkedHashSet<String>();
        moduleNames.add("aModule");
        moduleNames.add("bModule");
        moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleNames()).thenReturn(moduleNames);

        messageViewRegistry = new ItemViewDefinitionRegistry();

        TypeMappingImpl typeMapping = new TypeMappingImpl();
        Node2BeanTransformerImpl transformer = new Node2BeanTransformerImpl();
        ComponentsTestUtil.setInstance(Node2BeanProcessor.class, new Node2BeanProcessorImpl(typeMapping, transformer));
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
    }

    @Test
    public void testMessageViewDefinitionOnStart() throws RegistrationException {
        // GIVEN
        ConfiguredMessageViewDefinitionManager messageViewManager = new ConfiguredMessageViewDefinitionManager(moduleRegistry, messageViewRegistry);

        // WHEN
        messageViewManager.start();

        // THEN
        ItemViewDefinition aMessageView = messageViewRegistry.get("aModule:aMessageView");
        assertNotNull(aMessageView);
        assertEquals("labelA", aMessageView.getForm().getTabs().get(0).getLabel());

        ItemViewDefinition bMessageView = messageViewRegistry.get("bModule:bMessageView");
        assertNotNull(bMessageView);
        assertEquals("labelB", bMessageView.getActions().get("actionb").getLabel());

    }

    @Test(expected = RegistrationException.class)
    public void testNonExistentMessageViewDefinition() throws RegistrationException {
        // GIVEN
        ConfiguredMessageViewDefinitionManager messageViewManager = new ConfiguredMessageViewDefinitionManager(moduleRegistry, messageViewRegistry);

        // WHEN
        messageViewManager.start();

        // THEN
        messageViewRegistry.get("cModule:cMessageView");
    }

    @Test
    public void testMessageViewDefinitionReloadsOnAddition() throws RegistrationException, RepositoryException, InterruptedException {
        // GIVEN
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        ConfiguredMessageViewDefinitionManager messageViewManager = new ConfiguredMessageViewDefinitionManager(moduleRegistry, messageViewRegistry);

        // WHEN
        messageViewManager.start();

        // THEN
        // Make sure messageView a is there.
        ItemViewDefinition aMessageView = messageViewRegistry.get("aModule:aMessageView");
        assertNotNull(aMessageView);

        // WHEN
        // add messageView c
        String newPath = session.getNode(A_MESSAGE_VIEW_PATH).getParent().addNode("cMessageView").getPath();
        MockEvent event = new MockEvent();
        event.setType(Event.NODE_ADDED);
        event.setPath(newPath);
        observationManager.fireEvent(event);

        // THEN c is added
        assertMessageViewIsAdded("aModule:cMessageView");
    }

    @Test
    public void testMessageViewDefinitionReloadsOnRemoval() throws RegistrationException, RepositoryException, InterruptedException {
        // GIVEN
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        ConfiguredMessageViewDefinitionManager messageViewManager = new ConfiguredMessageViewDefinitionManager(moduleRegistry, messageViewRegistry);

        // WHEN
        messageViewManager.start();

        // THEN
        // Make sure messageView a is there.
        ItemViewDefinition aMessageView = messageViewRegistry.get("aModule:aMessageView");
        assertNotNull(aMessageView);

        // WHEN
        // Remove messageView a:
        session.getNode(A_MESSAGE_VIEW_PATH).remove();
        MockEvent event2 = new MockEvent();
        event2.setType(Event.NODE_REMOVED);
        event2.setPath(A_MESSAGE_VIEW_PATH);
        observationManager.fireEvent(event2);

        // THEN a is gone
        assertMessageViewIsRemoved("aModule:cMessageView");
    }

    private void assertMessageViewIsAdded(final String id) {
        TestUtil.delayedAssert(new Assertion() {

            @Override
            public void evaluate() throws RegistrationException {
                messageViewRegistry.get(id);
            }
        });
    }

    private void assertMessageViewIsRemoved(final String id) {
        TestUtil.delayedAssert(2000, 5000, new Assertion() {

            @Override
            public void evaluate() {
                try {
                    messageViewRegistry.get(id);
                    fail();
                } catch (RegistrationException e) {
                    // expected
                }
            }
        });
    }
}
