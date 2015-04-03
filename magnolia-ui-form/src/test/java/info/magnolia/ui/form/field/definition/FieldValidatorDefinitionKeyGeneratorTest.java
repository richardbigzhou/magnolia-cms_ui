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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.TestDialogDef;
import info.magnolia.ui.form.validator.definition.ConfiguredFieldValidatorDefinition;
import info.magnolia.ui.form.validator.definition.FieldValidatorDefinition;
import info.magnolia.ui.form.validator.definition.FieldValidatorDefinitionKeyGenerator;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link FieldValidatorDefinition}.
 */
public class FieldValidatorDefinitionKeyGeneratorTest {

    private ProxytoysI18nizer i18nizer;

    @Before
    public void setup() {
        i18nizer = new ProxytoysI18nizer(mock(TranslationService.class), mock(LocaleProvider.class));
    }

    @Test
    public void keyGeneratedFromAppDescriptorIsCompliant() throws Exception {
        // GIVEN
        TestDialogDef dialog = setupDialogDefinition();

        // WHEN
        dialog = i18nizer.decorate(dialog);

        // THEN
        final String[] generatedKeys = findGeneratedLabelKeys("some configured value", dialog.getForm().getTabs().get(0).getFields().get(0).getValidators().get(0));
        assertThat(generatedKeys, Matchers.arrayContaining(
                "some configured value",
                "test-module.testDialog.testTab.testField.validation.errorMessage",
                "test-module.testDialog.testField.validation.errorMessage",
                "testField.validation.errorMessage"
                ));
    }

    private TestDialogDef setupDialogDefinition() {
        TestDialogDef dialog = new TestDialogDef("test-module/testDialog");
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("testTab");
        ConfiguredFieldDefinition field = new ConfiguredFieldDefinition();
        field.setName("testField");
        field.addValidator(new ConfiguredFieldValidatorDefinition());
        dialog.setForm(form);
        form.addTab(tab);
        tab.addField(field);
        return dialog;
    }

    private String[] findGeneratedLabelKeys(String configuredValue, FieldValidatorDefinition def) throws NoSuchMethodException {
        return new FieldValidatorDefinitionKeyGenerator().keysFor(configuredValue, def, ConfiguredFieldValidatorDefinition.class.getMethod("getErrorMessage"));
    }
}
