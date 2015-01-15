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

import static org.junit.Assert.*;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.CompositeFieldDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Main test class for {@link FieldValidatorDefinitionKeyGenerator}.
 */
public class FieldValidatorDefinitionKeyGeneratorTest {

    @Test
    public void addKeysForCompositFields() throws SecurityException, NoSuchMethodException {
        // GIVEN
        TestProxytoysI18nizer i18nizer = new TestProxytoysI18nizer(null, null);

        EmailValidatorDefinition validator = new EmailValidatorDefinition();
        ConfiguredFieldDefinition textFieldDefinition = new TextFieldDefinition();
        textFieldDefinition.setValidators(Arrays.asList((FieldValidatorDefinition) validator));
        textFieldDefinition.setName("textFieldDefinition");
        CompositeFieldDefinition compositeFieldDefinition = new CompositeFieldDefinition();
        compositeFieldDefinition.setName("compositeFieldDefinition");
        compositeFieldDefinition.setFields(Arrays.asList(textFieldDefinition));
        ConfiguredTabDefinition configuredTabDefinition = new ConfiguredTabDefinition();
        configuredTabDefinition.setName("configuredTabDefinition");
        configuredTabDefinition.setFields(Arrays.asList((FieldDefinition) compositeFieldDefinition));

        // 18n
        configuredTabDefinition = i18nizer.decorate(configuredTabDefinition);
        compositeFieldDefinition = i18nizer.decorateChild(compositeFieldDefinition, configuredTabDefinition);
        textFieldDefinition = i18nizer.decorateChild(textFieldDefinition, compositeFieldDefinition);
        validator = i18nizer.decorateChild(validator, textFieldDefinition);

        // init generator
        FieldValidatorDefinitionKeyGenerator generator = new FieldValidatorDefinitionKeyGenerator();
        List<String> keys = new ArrayList<String>();
        AnnotatedElement el = validator.getClass().getMethod("getErrorMessage");

        // WHEN
        generator.keysFor(keys, validator, el);
        // THEN
        assertEquals(3, keys.size());
        assertEquals("configuredTabDefinition.configuredTabDefinition.textFieldDefinition.validation.errorMessage", keys.get(0));
        assertEquals("configuredTabDefinition.textFieldDefinition.validation.errorMessage", keys.get(1));
        assertEquals("textFieldDefinition.validation.errorMessage", keys.get(2));
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
