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
package info.magnolia.ui.dialog.definition;

import static org.junit.Assert.*;

import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.transformer.multi.MultiValueSubChildrenNodePropertiesTransformer;

import java.util.Locale;

import org.junit.Test;

/**
 * .
 */
public class ConfiguredFormDialogDefinitionTest {

    @Test
    public void changeDefaultPropertyTest() {
        // GIVEN
        final ConfiguredFormDialogDefinition configuredFormDialogDefinition = new ConfiguredFormDialogDefinition();
        final ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        configuredFormDialogDefinition.setForm(form);

        final ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("TAB NAME");
        form.addTab(tab);

        final MultiValueFieldDefinition multiField = new MultiValueFieldDefinition();
        multiField.setName("FIELD NAME");
        // Override default value
        multiField.setTransformerClass(MultiValueSubChildrenNodePropertiesTransformer.class);

        tab.addField(multiField);
        // Pre Check
        assertEquals(multiField.getTransformerClass(), MultiValueSubChildrenNodePropertiesTransformer.class);
        // initialize a ProxytoysI18nizer

        final ProxytoysI18nizer i18nizer = new ProxytoysI18nizer(new TestTranslationService(), new LocaleProvider() {

            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            }
        });

        // WHEN
        final ConfiguredFormDialogDefinition decoratedFormDialogDefinition = i18nizer.decorate(configuredFormDialogDefinition);

        // THEN
        final FieldDefinition firstField = decoratedFormDialogDefinition.getForm().getTabs().get(0).getFields().get(0);
        assertTrue(firstField instanceof MultiValueFieldDefinition);
        assertNotNull(firstField.getTransformerClass());
        assertEquals(firstField.getTransformerClass(), MultiValueSubChildrenNodePropertiesTransformer.class);

    }

    /**
     * Test translation service.
     */
    public static class TestTranslationService implements TranslationService {

        @Override
        public String translate(LocaleProvider localeProvider, String basename, String[] keys) {
            return "translated with key [" + keys[0] + "] and basename [" + basename + "] and locale [" + localeProvider.getLocale() + "]";
        }

        @Override
        public String translate(LocaleProvider localeProvider, String[] keys) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reloadMessageBundles() {
        }
    }
}
