/**
 * This file Copyright (c) 2013 Magnolia International
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

import static org.junit.Assert.assertEquals;

import info.magnolia.i18n.I18nizer;
import info.magnolia.i18n.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.form.definition.ConfiguredFormDefinition;
import info.magnolia.ui.form.definition.FormDefinitionKeyGenerator;
import info.magnolia.ui.form.definition.TestDialogDef;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * TODO Type description here.
 */
public class DialogDefinitionKeyGeneratorTest {
    @Test
    public void keysForDialogLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        DialogDefinitionKeyGenerator generator = new DialogDefinitionKeyGenerator();
        // structure
        ConfiguredFormDialogDefinition dialog = new ConfiguredFormDialogDefinition();
        dialog.setId("test-module:testFolder/testDialog");
        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        List<String> keys = new ArrayList<String>(4);
        generator.keysFor(keys, dialog, dialog.getClass().getMethod("getLabel"));

        // THEN
        assertEquals(2, keys.size());
        assertEquals("test-module.testFolder.testDialog.label", keys.get(0));
        assertEquals("test-module.testFolder.testDialog", keys.get(1));
    }

    @Test
    public void keysForDialogDescription() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        DialogDefinitionKeyGenerator generator = new DialogDefinitionKeyGenerator();
        // structure
        ConfiguredFormDialogDefinition dialog = new ConfiguredFormDialogDefinition();
        dialog.setId("test-module:testFolder/testDialog");
        // i18n
        I18nizer i18nizer = new ProxytoysI18nizer(null, null);
        dialog = i18nizer.decorate(dialog);

        // WHEN
        List<String> keys = new ArrayList<String>(4);
        generator.keysFor(keys, dialog, dialog.getClass().getMethod("getDescription"));

        // THEN
        assertEquals(1, keys.size());
        assertEquals("test-module.testFolder.testDialog.desc", keys.get(0));
    }

}
