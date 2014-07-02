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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentConfigurer;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.List;

import com.google.inject.name.Names;

/**
 * Utility class creating a {@link ComponentProvider} with a choose dialog scope including correct contentConnector,...<br>
 */
public class ChooseDialogComponentProviderUtil {

    private static final String CHOOSE_DIALOG_COMPONENT_ID = "choosedialog";

    public static ComponentProvider createChooseDialogComponentProvider(final ChooseDialogDefinition chooseDialogDefinition, ComponentProvider componentProvider) {
        ModuleRegistry moduleRegistry = componentProvider.getComponent(ModuleRegistry.class);
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();
        ComponentProviderConfiguration configuration =
                configurationBuilder.getComponentsFromModules(CHOOSE_DIALOG_COMPONENT_ID, moduleDefinitions);
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) componentProvider);

        ComponentConfigurer c = new AbstractGuiceComponentConfigurer() {
            @Override
            protected void configure() {
                // add binding for ChooseDialogDefinition to feed guice providers
                bind(ChooseDialogDefinition.class).toInstance(chooseDialogDefinition);

                bind(EventBus.class).annotatedWith(Names.named(ChooseDialogEventBus.NAME)).toInstance(new SimpleEventBus());
                bind(ContentConnector.class).toProvider(ChooseDialogContentConnectorProvider.class);
                bind(ImageProvider.class).toProvider(ChooseDialogImageProviderProvider.class);
            }
        };
        return builder.build(c);
    }
}
