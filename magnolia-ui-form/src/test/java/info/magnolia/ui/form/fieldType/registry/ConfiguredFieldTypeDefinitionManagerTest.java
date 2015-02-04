/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.form.fieldType.registry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import info.magnolia.ui.form.field.factory.StaticFieldFactory;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.form.field.definition.StaticFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.ConfiguredFieldTypeDefinitionManager;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * ConfiguredFieldTypeDefinitionManagerTest.
 */
public class ConfiguredFieldTypeDefinitionManagerTest {

    private static final String A_FIELD_TYPE_PATH = "/modules/aModule/" + ConfiguredFieldTypeDefinitionManager.FIELD_CONFIG_NODE_NAME + "/aFieldType";
    private static final String B_FIELD_TYPE_PATH = "/modules/bModule/" + ConfiguredFieldTypeDefinitionManager.FIELD_CONFIG_NODE_NAME + "/bFieldType";
    private static final String C_FIELD_TYPE_PATH = "/modules/bModule/" + ConfiguredFieldTypeDefinitionManager.FIELD_CONFIG_NODE_NAME + "/bFieldType";
    private Session session;
    private ModuleRegistry moduleRegistry;
    private FieldTypeDefinitionRegistry fieldTypeRegistry;

    @Before
    public void setUp() throws Exception {

        ComponentsTestUtil.setImplementation(FieldTypeDefinition.class, ConfiguredFieldTypeDefinition.class);

        session = SessionTestUtil.createSession(RepositoryConstants.CONFIG,
                A_FIELD_TYPE_PATH + ".name=text",
                A_FIELD_TYPE_PATH + ".definitionClass=" + TextFieldDefinition.class.getName(),
                A_FIELD_TYPE_PATH + ".factoryClass=" + TextFieldFactory.class.getName(),
                B_FIELD_TYPE_PATH + ".name=bFieldType",
                B_FIELD_TYPE_PATH + ".definitionClass=" + StaticFieldDefinition.class.getName(),
                B_FIELD_TYPE_PATH + ".factoryClass=" + StaticFieldFactory.class.getName(),
                C_FIELD_TYPE_PATH + ".name=cModule:cFieldType"
        );
        MockUtil.initMockContext();
        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        Set<String> moduleNames = new LinkedHashSet<String>();
        moduleNames.add("aModule");
        moduleNames.add("bModule");
        moduleRegistry = mock(ModuleRegistry.class);
        when(moduleRegistry.getModuleNames()).thenReturn(moduleNames);

        fieldTypeRegistry = new FieldTypeDefinitionRegistry();

        TypeMappingImpl typeMapping = new TypeMappingImpl();
        Node2BeanTransformerImpl transformer = new Node2BeanTransformerImpl();
        ComponentsTestUtil.setInstance(Node2BeanProcessor.class, new Node2BeanProcessorImpl(typeMapping, transformer));
        ComponentsTestUtil.setImplementation(TypeMapping.class, TypeMappingImpl.class);
    }

    @Test
    @Ignore("ConfiguredFieldTypeDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testGetFieldTypeById() throws RegistrationException {
        // GIVEN
        ConfiguredFieldTypeDefinitionManager fieldTypeManager = new ConfiguredFieldTypeDefinitionManager();

        // WHEN
        fieldTypeManager.start();

        // THEN
        FieldTypeDefinition aFieldType = fieldTypeRegistry.get("aFieldType");
        assertNotNull(aFieldType);
        assertEquals(TextFieldDefinition.class.getName(), aFieldType.getDefinitionClass().getName());
        assertEquals(TextFieldFactory.class.getName(), aFieldType.getFactoryClass().getName());

    }

    @Test
    @Ignore("ConfiguredFieldTypeDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testGetFieldTypeByDefinition() throws RegistrationException {
        // GIVEN
        ConfiguredFieldTypeDefinitionManager fieldTypeManager = new ConfiguredFieldTypeDefinitionManager();

        // WHEN
        fieldTypeManager.start();

        // THEN
        FieldTypeDefinition aFieldType = fieldTypeRegistry.getByDefinition(TextFieldDefinition.class);
        assertNotNull(aFieldType);
        assertEquals(TextFieldDefinition.class.getName(), aFieldType.getDefinitionClass().getName());
        assertEquals(TextFieldFactory.class.getName(), aFieldType.getFactoryClass().getName());
    }

    @Test(expected = RegistrationException.class)
    @Ignore("ConfiguredFieldTypeDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testNonExistentFieldTypeDefinition() throws RegistrationException {
        // GIVEN
        ConfiguredFieldTypeDefinitionManager fieldTypeManager = new ConfiguredFieldTypeDefinitionManager();

        // WHEN
        fieldTypeManager.start();

        // THEN
        fieldTypeRegistry.get("cFieldType");
    }

    @Test
    @Ignore("ConfiguredFieldTypeDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testFieldTypeDefinitionReloadsOnAddition() throws RegistrationException, RepositoryException, InterruptedException {
        // GIVEN
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        ConfiguredFieldTypeDefinitionManager fieldTypeManager = new ConfiguredFieldTypeDefinitionManager();

        // WHEN
        fieldTypeManager.start();

        // THEN
        // Make sure fieldType a is there.
        FieldTypeDefinition aFieldType = fieldTypeRegistry.get("aFieldType");
        assertNotNull(aFieldType);

        // WHEN
        // add fieldType c.
        String newPath = session.getNode(A_FIELD_TYPE_PATH).getParent().addNode("cFieldType").getPath();
        MockEvent event = new MockEvent();
        event.setType(Event.NODE_ADDED);
        event.setPath(newPath);
        observationManager.fireEvent(event);

        // THEN c is added
        assertFieldTypeIsAdded("cFieldType");
    }

    @Test
    @Ignore("ConfiguredFieldTypeDefinitionManager is deprecated. It will be revived to provide backwards compatibility, but should not be used anymore.")
    public void testFieldTypeDefinitionReloadsOnRemoval() throws RegistrationException, RepositoryException, InterruptedException {
        // GIVEN
        MockObservationManager observationManager = (MockObservationManager) session.getWorkspace().getObservationManager();
        ConfiguredFieldTypeDefinitionManager fieldTypeManager = new ConfiguredFieldTypeDefinitionManager();

        // WHEN
        fieldTypeManager.start();

        // THEN
        FieldTypeDefinition aFieldType = fieldTypeRegistry.get("aFieldType");
        assertNotNull(aFieldType);

        // WHEN
        // Remove fieldType a:
        session.getNode(A_FIELD_TYPE_PATH).remove();
        MockEvent event2 = new MockEvent();
        event2.setType(Event.NODE_REMOVED);
        event2.setPath(A_FIELD_TYPE_PATH);
        observationManager.fireEvent(event2);

        // THEN a is gone
        assertFieldTypeIsRemoved("aFieldType");
    }

    private void assertFieldTypeIsRemoved(final String id) {
        TestUtil.delayedAssert(new Assertion() {

            @Override
            public void evaluate() {
                try {
                    fieldTypeRegistry.get(id);
                } catch (RegistrationException e) {
                    fail();
                }
            }
        });
    }

    private void assertFieldTypeIsAdded(final String id) {
        TestUtil.delayedAssert(new Assertion() {

            @Override
            public void evaluate() throws RegistrationException {
                fieldTypeRegistry.get(id);
            }
        });
    }
}
