/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.framework.config;

import static org.junit.Assert.assertNotNull;

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;

import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Before;
import org.junit.Test;

public class GroovyScriptConfigTest extends MgnlTestCase {

    public static final String TEST_DIALOG_SCRIPT = "info/magnolia/config/modules/ui-framework/dialogs/samples/testDialog.groovy";

    private GroovyScriptExecutor provider;
    private DialogDefinitionRegistry dialogDefinitionRegistry;

    @Before
    public void setUp() throws Exception {
        this.provider = new GroovyScriptExecutor();
        this.dialogDefinitionRegistry = new DialogDefinitionRegistry();
        ComponentsTestUtil.setInstance(DialogDefinitionRegistry.class, dialogDefinitionRegistry);
    }



    @Test
    public void testResourceResolution() throws Exception {
        // GIVEN
        Reader in = new InputStreamReader(ClasspathResourcesUtil.getStream(TEST_DIALOG_SCRIPT, false));

        // WHEN
        provider.executeScript(in, "test");
        FormDialogDefinition dialogDefinition = dialogDefinitionRegistry.getDialogDefinition("testDialog");

        // THEN
        assertNotNull(dialogDefinition);
    }
}
