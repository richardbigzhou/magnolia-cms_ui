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
package info.magnolia.ui.api.app.launcherlayout;

import static org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


/**
 * Tests for the {@link AppLauncherGroupDefinitionKeyGenerator}.
 */
public class AppLauncherGroupDefinitionKeyGeneratorTest {

    @Test
    public void keysForDialogLabel() throws SecurityException, NoSuchMethodException {
        // GIVEN
        final AppLauncherGroupDefinitionKeyGenerator generator = new AppLauncherGroupDefinitionKeyGenerator();
        // structure
        ConfiguredAppLauncherGroupDefinition definition = new ConfiguredAppLauncherGroupDefinition();
        definition.setName("groupName");
        definition.setLabel("testLabel");
        definition.setPermanent(false);

        // WHEN
        List<String> keys = new ArrayList<String>(3);
        generator.keysFor(keys, definition, definition.getClass().getMethod("getLabel"));

        // THEN
        assertThat(keys.toArray(new String[3]), arrayContaining(
                "appLauncherLayout.groups.groupName.label",
                //deprecated:
                "app-launcher.groupName.label",
                "app-launcher.groupName"
        ));
    }
}
