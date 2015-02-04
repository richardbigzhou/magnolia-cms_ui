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

import info.magnolia.config.registry.DefinitionMetadata;
import info.magnolia.config.registry.DefinitionMetadataBuilder;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionRawView;
import info.magnolia.config.registry.Registry;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.form.field.definition.BasicTextCodeFieldDefinition;
import info.magnolia.ui.form.field.definition.BasicUploadFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.form.field.factory.BasicTextCodeFieldFactory;
import info.magnolia.ui.form.field.factory.TextFieldFactory;
import info.magnolia.ui.form.fieldtype.definition.ConfiguredFieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.DefinitionTypes;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class.
 */
public class FieldTypeDefinitionRegistryTest {

    private FieldTypeDefinitionRegistry registry;

    @Before
    public void setUp() throws Exception {
        registry = new FieldTypeDefinitionRegistry();
        ConfiguredFieldTypeDefinition textFieldDefinition = new ConfiguredFieldTypeDefinition();
        textFieldDefinition.setDefinitionClass(TextFieldDefinition.class);
        textFieldDefinition.setFactoryClass(TextFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("text", textFieldDefinition));

        ConfiguredFieldTypeDefinition codeFieldDefinition = new ConfiguredFieldTypeDefinition();
        codeFieldDefinition.setDefinitionClass(BasicTextCodeFieldDefinition.class);
        codeFieldDefinition.setFactoryClass(BasicTextCodeFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("code", codeFieldDefinition));

    }

    @Test
    public void testGetExisting() throws RegistrationException {
        // GIVEN

        // WHEN
        FieldTypeDefinition res = registry.get("text");

        // THEN
        assertNotNull(res);
        assertTrue(res instanceof ConfiguredFieldTypeDefinition);
        assertEquals(TextFieldDefinition.class.getName(), res.getDefinitionClass().getName());
    }

    @Test(expected = RegistrationException.class)
    public void testGetNonExisting() throws RegistrationException {
        // GIVEN

        // WHEN
        FieldTypeDefinition res = registry.get("tutu");

        // THEN
    }

    @Test
    public void testGetByDefinitionExisting() throws RegistrationException {
        // GIVEN

        // WHEN
        FieldTypeDefinition res = registry.getByDefinition(TextFieldDefinition.class);

        // THEN
        assertNotNull(res);
        assertTrue(res instanceof ConfiguredFieldTypeDefinition);
        assertEquals(TextFieldFactory.class.getName(), res.getFactoryClass().getName());
    }

    @Test(expected = RegistrationException.class)
    public void testGetByDefinitionNonExisting() throws RegistrationException {
        // GIVEN

        // WHEN
        FieldTypeDefinition res = registry.getByDefinition(BasicUploadFieldDefinition.class);

        // THEN
    }

    /**
     * Null has to be returned if we ask for a factory for ConfiguredFieldDefinition.
     */
    @Test
    public void testGetByDefinitionNull() throws RegistrationException {
        // GIVEN
        ConfiguredFieldTypeDefinition codeFieldDefinition = new ConfiguredFieldTypeDefinition();
        codeFieldDefinition.setDefinitionClass(ConfiguredFieldDefinition.class);
        codeFieldDefinition.setFactoryClass(BasicTextCodeFieldFactory.class);
        registry.register(new TestFieldTypeDefinitionProvider("extend", codeFieldDefinition));
        // WHEN
        FieldTypeDefinition res = registry.getByDefinition(ConfiguredFieldDefinition.class);

        // THEN
        assertNull(res);
    }

    /**
     * .
     */
    public static class TestFieldTypeDefinitionProvider implements DefinitionProvider<FieldTypeDefinition> {

        private final DefinitionMetadata metadata;

        private FieldTypeDefinition definition;

        public TestFieldTypeDefinitionProvider(String fieldId, FieldTypeDefinition definition) {
            this.definition = definition;
            this.metadata = DefinitionMetadataBuilder.usingNameAsId().
                    type(DefinitionTypes.FIELD_TYPE).
                    name(fieldId).module("module").
                    relativeLocation("/").
                    build();
        }

        @Override
        public DefinitionMetadata getMetadata() {
            return metadata;
        }

        @Override
        public FieldTypeDefinition get() throws Registry.InvalidDefinitionException {
            return definition;
        }

        @Override
        public DefinitionRawView getRaw() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public List<String> getErrorMessages() {
            throw new UnsupportedOperationException();
        }
    }
}
