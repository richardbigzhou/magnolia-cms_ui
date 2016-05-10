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
package info.magnolia.ui.form.definition;

import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.Assert.assertThat;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link TabDefinitionKeyGenerator}.
 */
public class TabDefinitionKeyGeneratorTest {

    @Test
    public void keysForTabLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        TabDefinitionKeyGenerator generator = new TabDefinitionKeyGenerator();
        // structure
        TestDialogDef dialog = new TestDialogDef();
        dialog.setId("test-module:testFolder/testDialog");
        ConfiguredFormDefinition form = new ConfiguredFormDefinition();
        ConfiguredTabDefinition tab = new ConfiguredTabDefinition();
        tab.setName("testTab");
        // hierarchy
        dialog.setForm(form);
        form.addTab(tab);
        // 18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        List<String> keys = new ArrayList<String>(4);
        generator.keysFor(keys, dialog.getForm().getTabs().get(0), tab.getClass().getMethod("getLabel"));

        // THEN
        assertThat(keys.toArray(new String[]{}), arrayContaining(
                "test-module.testFolder.testDialog.testTab.label",
                "test-module.testFolder.testDialog.testTab",
                "tabs.testTab.label",
                "tabs.testTab",
                "testTab.label", //deprecated
                "testTab" //deprecated
        ));
    }

}
