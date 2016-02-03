/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.contentapp.choosedialog;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.DefaultContentConnector;

import org.junit.Before;
import org.junit.Test;

/**
 * Main test class for {@link ChooseDialogComponentProviderUtil}.
 */
public class ChooseDialogComponentProviderUtilTest {

    private ChooseDialogDefinition chooseDialogDefinition;
    private ComponentProvider componentProvider;

    @Before
    public void setUp() throws Exception {
        chooseDialogDefinition = new ConfiguredChooseDialogDefinition();
        // componentProvider has to be a GuiceComponentProvider (impl details)
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        ModuleRegistry moduleRegistry = mock(ModuleRegistry.class);
        configuration.registerInstance(ModuleRegistry.class, moduleRegistry);
        configuration.registerInstance(ContentConnector.class, DefaultContentConnector.class); // global content connector
        configuration.registerInstance(ImageProvider.class, mock(ImageProvider.class)); // global image provider
        componentProvider = new GuiceComponentProviderBuilder().withConfiguration(configuration).build();
    }

    @Test
    public void createChooseDialogComponentProvider() {
        // GIVEN

        // WHEN
        ComponentProvider newComponentProvider = ChooseDialogComponentProviderUtil.createChooseDialogComponentProvider(chooseDialogDefinition, componentProvider);

        // THEN
        assertThat(newComponentProvider, notNullValue());
        assertThat(newComponentProvider.getParent(), is(componentProvider));
    }

    @Test
    public void createChooseDialogComponentProviderWithUiContext() {
        // GIVEN
        UiContext uiContext = mock(UiContext.class);

        // WHEN
        ComponentProvider newComponentProvider = ChooseDialogComponentProviderUtil.createChooseDialogComponentProvider(uiContext, chooseDialogDefinition, componentProvider);

        // THEN
        assertThat(newComponentProvider, notNullValue());
        assertEquals(uiContext, newComponentProvider.getComponent(UiContext.class));
    }
}
