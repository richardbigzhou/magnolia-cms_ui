/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.form.validator.definition;

import static org.junit.Assert.assertThat;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.definition.TestDialogDef;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Main test class for {@link FieldValidatorDefinitionKeyGenerator}.
 */
public class FieldValidatorDefinitionKeyGeneratorTest {

    @Test
    public void addKeysForCompositFields() throws SecurityException, NoSuchMethodException {
        // GIVEN
        EmailValidatorDefinition validator = new EmailValidatorDefinition();
        validator.setName("emailValidator");
        ConfiguredFieldDefinition textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition.setValidators(Arrays.asList((FieldValidatorDefinition) validator));
        textFieldDefinition.setName("textFieldDefinition");
        CompositeFieldDefinition compositeFieldDefinition = new CompositeFieldDefinition();
        compositeFieldDefinition.setName("compositeFieldDefinition");
        compositeFieldDefinition.setFields(Arrays.asList(textFieldDefinition));
        ConfiguredTabDefinition configuredTabDefinition = new ConfiguredTabDefinition();
        configuredTabDefinition.setName("configuredTabDefinition");
        configuredTabDefinition.setFields(Arrays.<FieldDefinition>asList(compositeFieldDefinition));
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        form.setTabs(Arrays.<TabDefinition>asList(configuredTabDefinition));
        TestDialogDef dialog = new TestDialogDef("test-module:testDialog");
        dialog.setForm(form);

        // 18n
        TestProxytoysI18nizer i18nizer = new TestProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);
        compositeFieldDefinition = (CompositeFieldDefinition) dialog.getForm().getTabs().get(0).getFields().get(0);


        // init generator
        FieldValidatorDefinitionKeyGenerator generator = new FieldValidatorDefinitionKeyGenerator();
        List<String> keys = new ArrayList<String>();
        AnnotatedElement el = validator.getClass().getMethod("getErrorMessage");

        // WHEN
        generator.keysFor(keys, compositeFieldDefinition.getFields().get(0).getValidators().get(0), el);
        // THEN
        assertThat(keys.toArray(new String[]{}), Matchers.arrayContaining(
                "test-module.dialogs.testDialog.form.tabs.configuredTabDefinition.fields.textFieldDefinition.validators.emailValidator.errorMessage",
                "dialogs.testDialog.form.tabs.configuredTabDefinition.fields.textFieldDefinition.validators.emailValidator.errorMessage",
                "form.tabs.configuredTabDefinition.fields.textFieldDefinition.validators.emailValidator.errorMessage",
                "fields.textFieldDefinition.validators.emailValidator.errorMessage",
                "validators.emailValidator.errorMessage",
                //deprecated:
                "test-module.testDialog.configuredTabDefinition.textFieldDefinition.validation.errorMessage",
                "test-module.testDialog.textFieldDefinition.validation.errorMessage",
                "textFieldDefinition.validation.errorMessage"
        ));
    }

    private class TestProxytoysI18nizer extends ProxytoysI18nizer {

        /**
         * @param translationService
         * @param localeProvider
         */
        public TestProxytoysI18nizer(TranslationService translationService, LocaleProvider localeProvider) {
            super(translationService, localeProvider);
        }

        @Override
        public <P, C> C decorateChild(C child, P parent) {
            return super.decorateChild(child, parent);
        }

    }

}
