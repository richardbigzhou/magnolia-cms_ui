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
package info.magnolia.ui.form.field.definition;

import static org.junit.Assert.*;

import info.magnolia.i18nsystem.I18nable;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.TestDialogDef;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for {@link FieldDefinitionKeyGenerator}.
 */
public class FieldDefinitionKeyGeneratorTest {

    @Test
    public void keysForFieldLabelInChooseDialog() throws Exception {
        // GIVEN
        // generator
        FieldDefinitionKeyGenerator generator = new FieldDefinitionKeyGenerator();
        // structure
        TestContentAppDescriptor app = new TestContentAppDescriptor();
        app.setName("test-app");
        TestChooseDialogDefinition chooseDialog = new TestChooseDialogDefinition();
        MultiValueFieldDefinition field = new MultiValueFieldDefinition();
        field.setName("mgnl:testField");
        // hierarchy
        chooseDialog.setField(field);
        app.setChooseDialog(chooseDialog);
        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        app = i18nizer.decorate(app);

        // WHEN
        String[] keys = generator.keysFor("undecorated", app.getChooseDialog().getField(), field.getClass().getMethod("getLabel"));


        // THEN
        assertThat(keys, Matchers.arrayContaining(
                "undecorated",
                "test-app.chooseDialog.fields.mgnl-testField.label",
                "test-app.chooseDialog.fields.mgnl-testField"));
    }

    @Test
    public void keysForFieldLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        FieldDefinitionKeyGenerator generator = new FieldDefinitionKeyGenerator();
        // structure
        TestDialogDef dialog = new TestDialogDef("test-module:testFolder/testDialog");
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("testTab");
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("mgnl:testField");
        // hierarchy
        dialog.setForm(form);
        form.addTab(tab);
        tab.addField(field);
        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        String[] keys = generator.keysFor("undecorated", dialog.getForm().getTabs().get(0).getFields().get(0), field.getClass().getMethod("getLabel"));

        // THEN
        assertThat(keys, Matchers.arrayContaining(
                "undecorated",
                "test-module.testFolder.testDialog.testTab.mgnl-testField.label",
                "test-module.testFolder.testDialog.testTab.mgnl-testField",
                "testTab.mgnl-testField.label",
                "testTab.mgnl-testField",
                "test-module.testFolder.testDialog.mgnl-testField.label",
                "test-module.testFolder.testDialog.mgnl-testField",
                "testDialog.mgnl-testField.label",
                "testDialog.mgnl-testField",
                "testDialog.testTab.mgnl-testField.label",
                "testDialog.testTab.mgnl-testField"));
    }

    @Test
    public void keysForNestedFieldLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        FieldDefinitionKeyGenerator generator = new FieldDefinitionKeyGenerator();
        // structure
        TestDialogDef dialog = new TestDialogDef("test-module:testFolder/testDialog");
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("testTab");
        MultiValueFieldDefinition parentField1 = new MultiValueFieldDefinition();
        parentField1.setName("mgnl:parentField1");
        MultiValueFieldDefinition parentField2 = new MultiValueFieldDefinition();
        parentField2.setName("mgnl:parentField2");
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("mgnl:testField");
        // hierarchy
        dialog.setForm(form);
        form.addTab(tab);
        tab.addField(parentField1);
        parentField1.setField(parentField2);
        parentField2.setField(field);
        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
       String[] keys = generator.keysFor("undecorated",
                ((MultiValueFieldDefinition) ((MultiValueFieldDefinition) dialog.getForm().getTabs().get(0).getFields().get(0)).getField()).getField(),
                field.getClass().getMethod("getLabel"));

        // THEN
        assertThat(keys, Matchers.arrayContaining(
                "undecorated",
                "test-module.testFolder.testDialog.testTab.mgnl-parentField1.mgnl-parentField2.mgnl-testField.label",
                "test-module.testFolder.testDialog.testTab.mgnl-parentField1.mgnl-parentField2.mgnl-testField",
                "test-module.testFolder.testDialog.testTab.mgnl-testField.label",
                "test-module.testFolder.testDialog.testTab.mgnl-testField",
                "testTab.mgnl-testField.label",
                "testTab.mgnl-testField",
                "test-module.testFolder.testDialog.mgnl-testField.label",
                "test-module.testFolder.testDialog.mgnl-testField",
                "testDialog.mgnl-testField.label",
                "testDialog.mgnl-testField",
                "testDialog.testTab.mgnl-testField.label",
                "testDialog.testTab.mgnl-testField"
                ));
    }

    @Test
    public void fieldDefinitionsNotInFormContextAreSupported() throws SecurityException, NoSuchMethodException {
        // GIVEN
        ConfiguredFieldDefinition fieldDefinition = new ConfiguredFieldDefinition();
        fieldDefinition.setName("dummyField");
        FieldDefinitionKeyGenerator generator = new FieldDefinitionKeyGenerator();
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        DummyDefinition dummyDefinition = i18nizer.decorate(new DummyDefinition(fieldDefinition));

        // WHEN
        FieldDefinition i18nFieldDef = dummyDefinition.getDummyField();
        List<String> keys = Arrays.asList(generator.keysFor(
                (String)null,
                i18nFieldDef,
                i18nFieldDef.getClass().getMethod("getLabel")));

        // THEN
        assertTrue(keys.contains("dummy.dummyField.label"));
    }

    /**
     * Fake ContentAppDescriptor - cannot use the right one here, as it is defined in a dependent artifact.
     */
    public static class TestContentAppDescriptor extends ConfiguredAppDescriptor {
        private TestChooseDialogDefinition chooseDialog;

        public TestChooseDialogDefinition getChooseDialog() {
            return chooseDialog;
        }

        public void setChooseDialog(TestChooseDialogDefinition chooseDialog) {
            this.chooseDialog = chooseDialog;
        }
    }

    /**
     * Dummy definition for testing key generation field definitions outside of form context.
     */
    @I18nable
    public static class DummyDefinition {

        private FieldDefinition dummyField;

        public DummyDefinition(FieldDefinition dummyField) {
            this.dummyField = dummyField;
        }

        public String getName() {
            return "dummy";
        }

        public FieldDefinition getDummyField() {
            return dummyField;
        }
    }

    /**
     * Fake ChooseDialogDefinition - cannot use the right one here, as it is defined in a dependent artifact.
     */
    @I18nable
    public static class TestChooseDialogDefinition {
        private FieldDefinition field;

        public FieldDefinition getField() {
            return field;
        }

        public void setField(FieldDefinition field) {
            this.field = field;
        }
    }
}
