/**
 * This file Copyright (c) 2011-2015 Magnolia International
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

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.definition.TestDialogDef;

import java.util.LinkedList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases fo {@link SelectFieldOptionDefinitionKeyGenerator}
 */
public class SelectFieldOptionDefinitionKeyGeneratorTest {

    private TestDialogDef dialog;
    private ConfiguredFormDefinition form;
    private ConfiguredTabDefinition tab;
    private SelectFieldOptionDefinitionKeyGenerator generator;

    @Before
    public void setUp() {
        // generator
        generator = new SelectFieldOptionDefinitionKeyGenerator();

        // structure
        FieldDefinitionKeyGeneratorTest.TestContentAppDescriptor app = new FieldDefinitionKeyGeneratorTest.TestContentAppDescriptor();
        app.setName("test-app");
        dialog = new TestDialogDef("test-module:testFolder/testDialog");
        form = new ConfiguredFormDefinition();
        tab = new ConfiguredTabDefinition();
        tab.setName("testTab");

        // hierarchy
        dialog.setForm(form);
        form.addTab(tab);

    }

    @Test
    public void testOptionsInSelectField() throws Exception {
        // GIVEN
        SelectFieldDefinition field = new SelectFieldDefinition();
        field.setName("mgnl:testField");
        List<SelectFieldOptionDefinition> options = new LinkedList<SelectFieldOptionDefinition>();

        SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
        option.setName("testOption");
        options.add(option);
        field.setOptions(options);

        tab.addField(field);

        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        String[] keys = generator.keysFor("undecorated", ((SelectFieldDefinition) dialog.getForm().getTabs().get(0).getFields().get(0)).getOptions().get(0), option.getClass().getMethod("getLabel"));

        // THEN
        assertThat(keys, Matchers.arrayContaining(
                "undecorated",
                "test-module.testFolder.testDialog.testTab.mgnl-testField.options.testOption",
                "testTab.mgnl-testField.options.testOption",
                "test-module.testFolder.testDialog.mgnl-testField.options.testOption",
                "testDialog.mgnl-testField.options.testOption",
                "testDialog.testTab.mgnl-testField.options.testOption"
                ));
    }

    @Test
    public void testOptionsInSwitchableField() throws Exception {

        // GIVEN
        SwitchableFieldDefinition field = new SwitchableFieldDefinition();
        field.setName("mgnl:testField");

        List<SelectFieldOptionDefinition> options = new LinkedList<SelectFieldOptionDefinition>();

        SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
        option.setName("testOption");
        options.add(option);
        field.setOptions(options);

        tab.addField(field);

        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        String[] keys = generator.keysFor("undecorated", ((SwitchableFieldDefinition) dialog.getForm().getTabs().get(0).getFields().get(0)).getOptions().get(0), option.getClass().getMethod("getLabel"));

        // THEN
        assertThat(keys, Matchers.arrayContaining(
                "undecorated",
                "test-module.testFolder.testDialog.testTab.mgnl-testField.options.testOption",
                "testTab.mgnl-testField.options.testOption",
                "test-module.testFolder.testDialog.mgnl-testField.options.testOption",
                "testDialog.mgnl-testField.options.testOption",
                "testDialog.testTab.mgnl-testField.options.testOption"
                ));
    }
}
