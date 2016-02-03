/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.ChooseDialogEventBus;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.choosedialog.ContentAppChooseDialogPresenter;
import info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.framework.app.BaseApp;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Names;
import com.google.inject.util.Providers;
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
    public void openChooseDialog(UiContext overlayLayer, String selectedId, final ChooseDialogCallback callback) {
        openChooseDialog(overlayLayer, null, selectedId, callback);
    }

    @Override
    public void openChooseDialog(UiContext overlayLayer, String targetTreeRootPath, String selectedId, ChooseDialogCallback callback) {
        ChooseDialogDefinition chooseDialogDefinition;
        ComponentProvider chooseDialogComponentProvider = createChooseDialogComponentProvider();
        if (appContext.getAppDescriptor() instanceof ContentAppDescriptor) {
            ContentAppDescriptor contentAppDescriptor = (ContentAppDescriptor) appContext.getAppDescriptor();
            presenter = chooseDialogComponentProvider.getComponent(contentAppDescriptor.getChooseDialog().getPresenterClass());
            chooseDialogDefinition = contentAppDescriptor.getChooseDialog();
        } else {
            chooseDialogDefinition = new ConfiguredChooseDialogDefinition();
            presenter = componentProvider.newInstance(ContentAppChooseDialogPresenter.class, chooseDialogComponentProvider);
        }

        chooseDialogDefinition = ensureChooseDialogField(chooseDialogDefinition, targetTreeRootPath);
        presenter.start(callback, chooseDialogDefinition, overlayLayer, selectedId);
    }

    ComponentProvider createChooseDialogComponentProvider() {
        ModuleRegistry moduleRegistry = componentProvider.getComponent(ModuleRegistry.class);
        final EventBus eventBus = new SimpleEventBus();
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
                bind(EventBus.class).annotatedWith(Names.named(ChooseDialogEventBus.NAME)).toProvider(Providers.of(eventBus));
            }
        };
        return builder.build(c);
    }

    private ChooseDialogDefinition ensureChooseDialogField(ChooseDialogDefinition definition, String targetTreeRootPath) {
        if (definition.getField() != null) {
            definition = cloner.deepClone(definition);
            if (definition.getField() instanceof WorkbenchFieldDefinition && StringUtils.isNotBlank(targetTreeRootPath)) {
                WorkbenchDefinition workbenchDefinition = ((WorkbenchFieldDefinition) definition.getField()).getWorkbench();
                if (workbenchDefinition != null && workbenchDefinition instanceof ConfiguredWorkbenchDefinition) {
                    ((ConfiguredWorkbenchDefinition) workbenchDefinition).setPath(targetTreeRootPath);
                }
            }
            return definition;
        }

        SubAppDescriptor subAppContext = appContext.getDefaultSubAppDescriptor();
        if (!(subAppContext instanceof BrowserSubAppDescriptor)) {
            log.error("Cannot start workbench choose dialog since targeted app is not a content app");
            return definition;
        }

        ConfiguredChooseDialogDefinition result = (ConfiguredChooseDialogDefinition) definition;
        result = cloner.deepClone(result);

        BrowserSubAppDescriptor subApp = (BrowserSubAppDescriptor) subAppContext;

        ConfiguredWorkbenchDefinition workbench = (ConfiguredWorkbenchDefinition) (cloner.deepClone(subApp.getWorkbench()));
        // mark definition as a dialog workbench so that workbench presenter can disable drag n drop
        workbench.setDialogWorkbench(true);
        workbench.setIncludeProperties(false);
        if (StringUtils.isNotBlank(targetTreeRootPath)) {
            workbench.setPath(targetTreeRootPath);
        }
        // Create the Choose Dialog Title

        ImageProviderDefinition imageProvider = cloner.deepClone(subApp.getImageProvider());

        WorkbenchFieldDefinition wbFieldDefinition = new WorkbenchFieldDefinition();
        wbFieldDefinition.setName("workbenchField");
        wbFieldDefinition.setWorkbench(workbench);
        wbFieldDefinition.setImageProvider(imageProvider);
        result.setField(wbFieldDefinition);
        result.setPresenterClass(presenter.getClass());
        return result;
    }
}
