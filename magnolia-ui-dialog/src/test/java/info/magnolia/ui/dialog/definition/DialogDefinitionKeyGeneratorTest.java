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
package info.magnolia.ui.dialog.definition;

import static org.junit.Assert.assertEquals;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.proxytoys.ProxytoysI18nizer;
import info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DialogDefinitionKeyGenerator}.
 */
public class DialogDefinitionKeyGeneratorTest {
    private I18nizer i18nizer;

    @Before
    public void setUp() {
        i18nizer = new ProxytoysI18nizer(null, null);
    }

    @Test
    public void keysForDialogLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        // generator
        DialogDefinitionKeyGenerator generator = new DialogDefinitionKeyGenerator();
        // structure
        ConfiguredFormDialogDefinition dialog = new ConfiguredFormDialogDefinition();
        dialog.setId("test-module:testFolder/testDialog");

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

        dialog = i18nizer.decorate(dialog);

        // WHEN
        List<String> keys = new ArrayList<String>(4);
        generator.keysFor(keys, dialog, dialog.getClass().getMethod("getDescription"));

        // THEN
        assertEquals(1, keys.size());
        assertEquals("test-module.testFolder.testDialog.description", keys.get(0));
    }

    @Test
    public void keysForFieldLabelInChooseDialog() throws Exception {
        // GIVEN
        // generator
        DialogDefinitionKeyGenerator generator = new DialogDefinitionKeyGenerator();
        // structure
        TestContentAppDescriptor app = new TestContentAppDescriptor();
        app.setName("test-app");
        ConfiguredChooseDialogDefinition chooseDialog = new ConfiguredChooseDialogDefinition();
        // hierarchy
        app.setChooseDialog(chooseDialog);

        app = i18nizer.decorate(app);

        // WHEN
        List<String> keys = new ArrayList<String>(2);
        generator.keysFor(keys, app.getChooseDialog(), chooseDialog.getClass().getMethod("getLabel"));

        // THEN
        assertEquals(2, keys.size());
        assertEquals("test-app.chooseDialog.label", keys.get(0));
        assertEquals("test-app.chooseDialog", keys.get(1));
    }

    /**
     * Fake ContentAppDescriptor - cannot use the right one here, as it is defined in a dependent artifact.
     */
    public static class TestContentAppDescriptor extends ConfiguredAppDescriptor {
        private ChooseDialogDefinition chooseDialog;

        public ChooseDialogDefinition getChooseDialog() {
            return chooseDialog;
        }

        public void setChooseDialog(ChooseDialogDefinition chooseDialog) {
            this.chooseDialog = chooseDialog;
        }
    }
}
