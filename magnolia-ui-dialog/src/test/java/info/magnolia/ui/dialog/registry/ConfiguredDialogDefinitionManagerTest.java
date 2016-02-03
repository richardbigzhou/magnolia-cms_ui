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
package info.magnolia.ui.dialog.registry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.config.registry.DefinitionProvider;
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
import info.magnolia.test.TestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockEvent;
import info.magnolia.test.mock.jcr.MockObservationManager;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the dialog definition manager.
 */
public class ConfiguredDialogDefinitionManagerTest {

    private static final String A_DIALOG_PATH = "/modules/aModule/dialogs/aDialog";
    private static final String B_DIALOG_PATH = "/modules/bModule/dialogs/bDialog";
    private static final String C_DIALOG_PATH = "/modules/bModule/dialogs/cDialog";
    private static final String GENERIC_FIELD_PATH = "/modules/aModule/dialogs/field";
    private static final String EXTENDING_FIELD_PATH = "/modules/bModule/dialogs/field";

    private ModuleRegistry moduleRegistry;
    private DialogDefinitionRegistry dialogRegistry;
    private Node2BeanProcessor node2BeanProcessor;

    private Session session;

    @Before
    public void setUp() throws Exception {

        ComponentsTestUtil.setImplementation(FormDialogDefinition.class, ConfiguredFormDialogDefinition.class);
        ComponentsTestUtil.setImplementation(FormDefinition.class, ConfiguredFormDefinition.class);
        ComponentsTestUtil.setImplementation(TabDefinition.class, ConfiguredTabDefinition.class);
        ComponentsTestUtil.setImplementation(ActionDefinition.class, ConfiguredActionDefinition.class);

        session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                A_DIALOG_PATH + ".class=" + ConfiguredFormDialogDefinition.class.getName(),
                A_DIALOG_PATH + "/form/tabs/taba",
                A_DIALOG_PATH + "/form/tabs/taba.label=label",
                B_DIALOG_PATH + "/actions/actionb",
                B_DIALOG_PATH + "/actions/actionb.label=label",
                GENERIC_FIELD_PATH + ".class=" + TextFieldDefinition.class.getName(),
                EXTENDING_FIELD_PATH + ".extends=" + GENERIC_FIELD_PATH
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
        Node2BeanTransformerImpl transformer = new Node2BeanTransformerImpl();
        node2BeanProcessor = new Node2BeanProcessorImpl(typeMapping, transformer);
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
    }

    @Test
    @Ignore("ConfiguredDialogDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testDialogDefinitionOnStart() throws RegistrationException {
        // GIVEN
        ConfiguredDialogDefinitionManager dialogManager = new ConfiguredDialogDefinitionManager();

        // WHEN
        dialogManager.start();

        // THEN
        FormDialogDefinition aDialog = dialogRegistry.getDialogDefinition("aModule:aDialog");
        assertNotNull(aDialog);
        assertEquals("aModule:aDialog", aDialog.getId());

        FormDialogDefinition bDialog = dialogRegistry.getDialogDefinition("bModule:bDialog");
        assertNotNull(bDialog);
        assertEquals("bModule:bDialog", bDialog.getId());
    }

    @Test
    @Ignore("ConfiguredDialogDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testDialogDefinitionReloadsOnChange() throws RegistrationException, RepositoryException, InterruptedException {
        // GIVEN
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        ConfiguredDialogDefinitionManager dialogManager = new ConfiguredDialogDefinitionManager();

        // WHEN
        dialogManager.start();

        // THEN
        // Make sure dialog a is there.
        FormDialogDefinition aDialog = dialogRegistry.getDialogDefinition("aModule:aDialog");
        assertNotNull(aDialog);

        // WHEN
        // Remove dialog a:
        session.getNode(A_DIALOG_PATH).remove();
        observationManager.fireEvent(MockEvent.nodeRemoved(A_DIALOG_PATH));

        // THEN a is gone
        assertDialogDefinitionIsRemoved("aModule:aDialog");

        // WHEN
        // Add a property and fire event
        session.getNode(B_DIALOG_PATH).setProperty("description", "dialog for bItems");
        observationManager.fireEvent(MockEvent.propertyAdded(B_DIALOG_PATH));

        // THEN
        // dialog b has its property modified.
        TestUtil.delayedAssert(new Assertion() {

            @Override
            public void evaluate() throws RegistrationException {
                FormDialogDefinition bDialog = dialogRegistry.getDialogDefinition("bModule:bDialog");
                assertNotNull(bDialog);
                assertEquals("dialog for bItems", bDialog.getDescription());
            }
        });

        // WHEN
        // add dialog c, remove dialog b
        session.getNode(B_DIALOG_PATH).getParent().addNode("cDialog").addNode("form");
        session.getNode(B_DIALOG_PATH).remove();
        MockEvent event = new MockEvent();
        event.setType(Event.NODE_ADDED);
        event.setPath(C_DIALOG_PATH);
        observationManager.fireEvent(event);

        // THEN
        // dialog b is gone.
        assertDialogDefinitionIsRemoved("bModule:bDialog");
        // and dialog c is present.
        dialogRegistry.getDialogDefinition("bModule:cDialog");
    }

    @Test
    @Ignore("ConfiguredDialogDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testOnlyDialogsAreRegisteredWhenExtendingFeatureIsUsed() throws Exception {
        // GIVEN
        ConfiguredDialogDefinitionManager dialogManager = new ConfiguredDialogDefinitionManager() {

            protected DefinitionProvider<DialogDefinition> newProvider(Node dialogNode) throws RepositoryException {
                if (dialogNode.getName().equals("field")) {
                    fail("should not happen");
                }
                return null;
            };
        };

        // WHEN
        dialogManager.start();

        // THEN
        // should not fail
    }

    private void assertDialogDefinitionIsRemoved(final String dialogId) {
        TestUtil.delayedAssert(new Assertion() {

            @Override
            public void evaluate() {
                try {
                    dialogRegistry.getDialogDefinition(dialogId);
                    fail();
                } catch (RegistrationException e) {
                    // expected
                }
            }
        });
    }
}
