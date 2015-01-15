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
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.List;

import com.google.inject.name.Names;

/**
 * The {@link ChooseDialogComponentProviderUtil} helps creating a {@link ComponentProvider} dedicated to a choose dialog.
 */
public class ChooseDialogComponentProviderUtil {

    private static final String CHOOSE_DIALOG_COMPONENT_ID = "choosedialog";

    /**
     * Creates the choose-dialog specific component provider, with proper bindings for e.g. {@link ContentConnector} or {@link ImageProvider}.
     * <p>
     * In particular, this ensures that within the dialog, these components get their dependencies as configured in the choose-dialog definition, rather than from current sub-app context.
     *
     * @param chooseDialogDefinition the choose-dialog definition, as configured for a content app, or built by code.
     * @param componentProvider the parent {@link ComponentProvider}.
     * @return a new component provider, to create {@link info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter ChooseDialogPresenter} with.
     */
    public static ComponentProvider createChooseDialogComponentProvider(ChooseDialogDefinition chooseDialogDefinition, ComponentProvider componentProvider) {
        GuiceComponentProviderBuilder builder = createComponentProviderBuilder(componentProvider);
        ComponentConfigurer chooseDialogBindings = getChooseDialogConfigurer(chooseDialogDefinition);
        return builder.build(chooseDialogBindings);
    }

    /**
     * Creates the choose-dialog specific component provider, with proper bindings for e.g. {@link ContentConnector} or {@link ImageProvider}, and registers given {@link UiContext} for further use.
     * <p>
     * In particular, this ensures that within the dialog, these components get their dependencies as configured in the choose-dialog definition, rather than from current sub-app context.
     *
     * @param uiContext the choose-dialog's UiContext, so that dialog actions can use it.
     * @param chooseDialogDefinition the choose-dialog definition, as configured for a content app, or built by code.
     * @param componentProvider the parent {@link ComponentProvider}.
     * @return a new component provider, to create {@link info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter ChooseDialogPresenter} with.
     */
    public static ComponentProvider createChooseDialogComponentProvider(UiContext uiContext, ChooseDialogDefinition chooseDialogDefinition, ComponentProvider componentProvider) {
        GuiceComponentProviderBuilder builder = createComponentProviderBuilder(componentProvider);
        ComponentConfigurer chooseDialogConfigurer = getChooseDialogConfigurer(chooseDialogDefinition);
        ComponentConfigurer uiContextConfigurer = getUiContextConfigurer(uiContext);
        return builder.build(chooseDialogConfigurer, uiContextConfigurer);
    }

    private static GuiceComponentProviderBuilder createComponentProviderBuilder(ComponentProvider componentProvider) {

        // fetch component bindings from module descriptors
        ModuleRegistry moduleRegistry = componentProvider.getComponent(ModuleRegistry.class);
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        ComponentProviderConfiguration configuration = configurationBuilder.getComponentsFromModules(CHOOSE_DIALOG_COMPONENT_ID, moduleDefinitions);

        // build up Guice component provider
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) componentProvider);
        return builder;
    }

    private static ComponentConfigurer getChooseDialogConfigurer(final ChooseDialogDefinition chooseDialogDefinition) {

        return new AbstractGuiceComponentConfigurer() {
            @Override
            protected void configure() {
                bind(ChooseDialogDefinition.class).toInstance(chooseDialogDefinition); // binding ChooseDialogDefinition to feed Guice providers below
                bind(EventBus.class).annotatedWith(Names.named(ChooseDialogEventBus.NAME)).toInstance(new SimpleEventBus());
                bind(ContentConnector.class).toProvider(ChooseDialogContentConnectorProvider.class);
                bind(ImageProvider.class).toProvider(ChooseDialogImageProviderProvider.class);
            }
        };
    }

    private static ComponentConfigurer getUiContextConfigurer(final UiContext uiContext) {

        return new AbstractGuiceComponentConfigurer() {
            @Override
            protected void configure() {
                bind(UiContext.class).toInstance(uiContext);
            }
        };
    }

}
