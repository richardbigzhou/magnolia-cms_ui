/**
 * This file Copyright (c) 2012-2014 Magnolia International
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
package info.magnolia.ui.contentapp;


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
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogContentConnectorProvider;
import info.magnolia.ui.contentapp.choosedialog.ChooseDialogImageProviderProvider;
import info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.framework.app.BaseApp;
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Names;
import com.rits.cloning.Cloner;

/**
 * Extends the {@link BaseApp} by the ability to open a choose dialog.
 */
public class ContentApp extends BaseApp {

    private static final Logger log = LoggerFactory.getLogger(ContentApp.class);

    private static final String CHOOSE_DIALOG_COMPONENT_ID = "choosedialog";

    private ComponentProvider componentProvider;
    private Cloner cloner = new Cloner();
    private ChooseDialogPresenter presenter;

    @Inject
    public ContentApp(AppContext appContext, AppView view, ComponentProvider componentProvider) {
        super(appContext, view);
        this.componentProvider = componentProvider;
    }

    @Override
    public void openChooseDialog(UiContext overlayLayer, String selectedId, ChooseDialogCallback callback) {
        openChooseDialog(overlayLayer, null, selectedId, callback);
    }

    @Override
    public void openChooseDialog(UiContext overlayLayer, String targetTreeRootPath, String selectedId, final ChooseDialogCallback callback) {

        // fetch or compute chooseDialogDefinition from default subApp
        ChooseDialogDefinition chooseDialogDefinition;
        if (appContext.getAppDescriptor() instanceof ContentAppDescriptor) {
            chooseDialogDefinition = ((ContentAppDescriptor) appContext.getAppDescriptor()).getChooseDialog();
        } else {
            chooseDialogDefinition = new ConfiguredChooseDialogDefinition();
        }
        chooseDialogDefinition = ensureChooseDialogField(chooseDialogDefinition, targetTreeRootPath);

        // create chooseDialogComponentProvider and get new instance of presenter from there
        ComponentProvider chooseDialogComponentProvider = createChooseDialogComponentProvider(chooseDialogDefinition);
        presenter = chooseDialogComponentProvider.newInstance(chooseDialogDefinition.getPresenterClass(), chooseDialogComponentProvider);

        presenter.start(callback, chooseDialogDefinition, overlayLayer, selectedId);
    }

    ComponentProvider createChooseDialogComponentProvider(final ChooseDialogDefinition chooseDialogDefinition) {
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

    private ChooseDialogDefinition ensureChooseDialogField(ChooseDialogDefinition definition, String targetTreeRootPath) {

        if (definition.getField() != null && definition.getContentConnector() != null) {
            return definition;
        }

        // check whether default subApp is a browser to fetch config from
        if (!(appContext.getDefaultSubAppDescriptor() instanceof BrowserSubAppDescriptor)) {
            log.error("Cannot start workbench choose dialog since targeted app is not a content app");
            return definition;
        }
        BrowserSubAppDescriptor subApp = (BrowserSubAppDescriptor) appContext.getDefaultSubAppDescriptor();

        // work on cloned definition so that we don't spoil raw config
        ConfiguredChooseDialogDefinition chooseDialogDefinition = (ConfiguredChooseDialogDefinition) cloner.deepClone(definition);

        // ensure contentConnector
        if (definition.getContentConnector() == null) {
            ContentConnectorDefinition contentConnector = cloner.deepClone(subApp.getContentConnector());
            if (StringUtils.isNotBlank(targetTreeRootPath) && contentConnector instanceof JcrContentConnectorDefinition) {
                ((ConfiguredJcrContentConnectorDefinition) contentConnector).setRootPath(targetTreeRootPath);
            }
            chooseDialogDefinition.setContentConnector(contentConnector);
        }

        // ensure workbench field
        if (chooseDialogDefinition.getField() == null) {
            WorkbenchFieldDefinition workbenchField = new WorkbenchFieldDefinition();
            workbenchField.setName("workbenchField");
            chooseDialogDefinition.setField(workbenchField);
        }

        if (chooseDialogDefinition.getField() instanceof WorkbenchFieldDefinition) {
            WorkbenchFieldDefinition workbenchField = (WorkbenchFieldDefinition) chooseDialogDefinition.getField();

            if (workbenchField.getWorkbench() == null) {
                ConfiguredWorkbenchDefinition workbench = (ConfiguredWorkbenchDefinition) cloner.deepClone(subApp.getWorkbench());
                // mark definition as a dialog workbench so that workbench presenter can disable drag n drop
                workbench.setDialogWorkbench(true);
                workbenchField.setWorkbench(workbench);
            }

            if (workbenchField.getImageProvider() == null) {
                ImageProviderDefinition imageProvider = cloner.deepClone(subApp.getImageProvider());
                workbenchField.setImageProvider(imageProvider);
            }
        }

        return chooseDialogDefinition;
    }
}
