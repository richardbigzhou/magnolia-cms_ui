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
package info.magnolia.ui.model.dialog.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.content2bean.Content2BeanProcessor;
import info.magnolia.content2bean.impl.Content2BeanProcessorImpl;
import info.magnolia.content2bean.impl.TypeMappingImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.registry.RegistrationException;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockObservationManager;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.model.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the dialog definition manager.
 */
public class ConfiguredDialogDefinitionManagerTest {

    private ModuleRegistry moduleRegistry;

    private DialogDefinitionRegistry dialogRegistry;

    private Session session;

    @Before
    public void setUp() throws Exception {

        ComponentsTestUtil.setImplementation(DialogDefinition.class, ConfiguredDialogDefinition.class);
        session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
            "/modules/aModule/dialogs/aDialog.id=aModule:aDialog",
            "/modules/bModule/dialogs/bDialog.id=bModule:bDialog"
            );
        MockUtil.initMockContext();
        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        Set<String> moduleNames = new LinkedHashSet<String>();
        moduleNames.add("aModule");
        moduleNames.add("bModule");
        moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleNames()).thenReturn(moduleNames);

        dialogRegistry = new DialogDefinitionRegistry();

        TypeMappingImpl typeMapping = new TypeMappingImpl();
        ComponentsTestUtil.setInstance(Content2BeanProcessor.class, new Content2BeanProcessorImpl(typeMapping));
    }

    @Test
    public void testDialogDefinitionOnStart() throws RegistrationException {
        // GIVEN
        ConfiguredDialogDefinitionManager dialogManager = new ConfiguredDialogDefinitionManager(moduleRegistry, dialogRegistry);

        // WHEN
        dialogManager.start();

        // THEN
        DialogDefinition aDialog = dialogRegistry.get("aModule:aDialog");
        assertNotNull(aDialog);
        assertEquals("aModule:aDialog", aDialog.getId());

        DialogDefinition bDialog = dialogRegistry.get("bModule:bDialog");
        assertNotNull(bDialog);
        assertEquals("bModule:bDialog", bDialog.getId());
    }

    @Test
    public void testDialogDefinitionReloadsOnChange() throws RegistrationException, UnsupportedRepositoryOperationException, RepositoryException, InterruptedException {
        // GIVEN
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        ConfiguredDialogDefinitionManager dialogManager = new ConfiguredDialogDefinitionManager(moduleRegistry, dialogRegistry);

        // WHEN
        dialogManager.start();

        // THEN
        // Make sure dialog a is there.
        DialogDefinition aDialog = dialogRegistry.get("aModule:aDialog");
        assertNotNull(aDialog);

        // WHEN
        // Remove dialog a:
        session.getNode("/modules/aModule/dialogs/aDialog").remove();
        observationManager.fireEvent(MockEvent.nodeRemoved("/modules/aModule/dialogs/aDialog"));
        Thread.sleep(6000);
        // THEN a is gone
        try {
            aDialog = dialogRegistry.get("aModule:aDialog");
            fail();
        } catch (RegistrationException expected) {
        }

        // WHEN
        // Add a property and fire event
        session.getNode("/modules/bModule/dialogs/bDialog").setProperty("description", "dialog for bItems");
        observationManager.fireEvent(MockEvent.propertyAdded("/modules/bModule/dialogs/bDialog"));
        Thread.sleep(6000);
        // THEN
        // dialog b has its property modified.
        DialogDefinition bDialog = dialogRegistry.get("bModule:bDialog");
        assertNotNull(bDialog);
        assertEquals("dialog for bItems", bDialog.getDescription());

        // WHEN
        // Rename dialog b, change the dialog name.
        session.getNode("/modules/bModule/dialogs/bDialog").getParent().addNode("cDialog").setProperty("id", "bModule:cDialog");
        session.getNode("/modules/bModule/dialogs/bDialog").remove();
        MockEvent event = new MockEvent();
        event.setType(MockEvent.NODE_MOVED);
        event.setPath("/modules/bModule/dialogs/bDialog");
        observationManager.fireEvent(event);
        Thread.sleep(6000);

        // THEN
        // dialog b is gone.
        try {
            bDialog = dialogRegistry.get("bModule:bDialog");
            fail();
        } catch (RegistrationException expected) {
        }
        bDialog = dialogRegistry.get("bModule:cDialog");
        assertNotNull(bDialog);
    }

}
