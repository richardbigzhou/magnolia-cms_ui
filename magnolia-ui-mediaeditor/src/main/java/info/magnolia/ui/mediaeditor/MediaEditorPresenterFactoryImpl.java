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
package info.magnolia.ui.mediaeditor;

import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentConfigurer;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.ComponentProviderConfigurationBuilder;
import info.magnolia.objectfactory.guice.AbstractGuiceComponentConfigurer;
import info.magnolia.objectfactory.guice.GuiceComponentProvider;
import info.magnolia.objectfactory.guice.GuiceComponentProviderBuilder;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.BaseDialogViewImpl;
import info.magnolia.ui.dialog.DialogPresenter;
import info.magnolia.ui.mediaeditor.action.MediaEditorActionExecutor;
import info.magnolia.ui.mediaeditor.action.availability.MediaEditorAvailabilityChecker;
import info.magnolia.ui.mediaeditor.definition.MediaEditorDefinition;
import info.magnolia.ui.mediaeditor.registry.MediaEditorRegistry;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.google.inject.name.Names;
import com.google.inject.util.Providers;

/**
 * Implementation of {@link MediaEditorPresenterFactory}.
 */
@Singleton
public class MediaEditorPresenterFactoryImpl implements MediaEditorPresenterFactory {

    private static final String MEDIA_EDITOR_COMPONENT_ID = "mediaeditor";

    private Logger log = Logger.getLogger(getClass());

    private ComponentProvider subAppComponentProvider;

    private MediaEditorRegistry registry;

    private ModuleRegistry moduleRegistry;

    private EventBus eventBus = new SimpleEventBus();

    private I18nizer i18nizer;

    private final SimpleTranslator i18n;

    @Inject
    public MediaEditorPresenterFactoryImpl(ComponentProvider subAppComponentProvider, ModuleRegistry moduleRegistry, MediaEditorRegistry registry, I18nizer i18nizer, SimpleTranslator i18n) {
        this.subAppComponentProvider = subAppComponentProvider;
        this.moduleRegistry = moduleRegistry;
        this.registry = registry;
        this.i18nizer = i18nizer;
        this.i18n = i18n;
    }

    @Override
    public MediaEditorPresenter getPresenterById(String id) {
        return getPresenterByDefinition(createDefinition(id));
    }

    private MediaEditorDefinition createDefinition(String id) {
        MediaEditorDefinition mediaEditorDefinition;
        try {
            mediaEditorDefinition = registry.get(id);
        } catch (RegistrationException e1) {
            throw new RuntimeException(e1);
        }

        if (mediaEditorDefinition == null) {
            throw new IllegalArgumentException("No media editor definition registered for name [" + id + "]"); // TODO-TRANSLATE-EXCEPTION
        }
        return i18nizer.decorate(mediaEditorDefinition);
    }

    private ComponentProvider createMediaEditorComponentProvider() {
        ComponentProviderConfigurationBuilder configurationBuilder = new ComponentProviderConfigurationBuilder();
        List<ModuleDefinition> moduleDefinitions = moduleRegistry.getModuleDefinitions();
        // Get components common to all sub apps
        ComponentProviderConfiguration configuration =
                configurationBuilder.getComponentsFromModules(MEDIA_EDITOR_COMPONENT_ID, moduleDefinitions);
        log.debug("Creating component provider for media editor...");
        GuiceComponentProviderBuilder builder = new GuiceComponentProviderBuilder();
        builder.withConfiguration(configuration);
        builder.withParent((GuiceComponentProvider) subAppComponentProvider);
        ComponentConfigurer c = new AbstractGuiceComponentConfigurer() {
            @Override
            protected void configure() {
                bind(EventBus.class).annotatedWith(Names.named(MediaEditorEventBus.NAME)).toProvider(Providers.of(eventBus));
            }
        };
        return builder.build(c);
    }

    @Override
    public MediaEditorPresenter getPresenterByDefinition(MediaEditorDefinition definition) {
        ComponentProvider mediaEditorComponentProvider = createMediaEditorComponentProvider();

        AppContext appContext = mediaEditorComponentProvider.getComponent(AppContext.class);
        MediaEditorView view = mediaEditorComponentProvider.getComponent(MediaEditorView.class);
        ActionExecutor mediaActionExecutor = mediaEditorComponentProvider.getComponent(ActionExecutor.class);
        ((MediaEditorActionExecutor) mediaActionExecutor).setDef(definition);

        ActionbarPresenter actionBarPresenter = mediaEditorComponentProvider.getComponent(ActionbarPresenter.class);
        MediaEditorAvailabilityChecker mediaEditorAvailabilityChecker = mediaEditorComponentProvider.getComponent(MediaEditorAvailabilityChecker.class);
        DialogPresenter dialogPresenter = new BaseDialogPresenter(mediaEditorComponentProvider, mediaActionExecutor, new BaseDialogViewImpl(), this.i18nizer, i18n);
        MediaEditorPresenter mediaEditorPresenter = new MediaEditorPresenterImpl(definition, eventBus, view, actionBarPresenter, dialogPresenter, appContext, i18n, mediaEditorAvailabilityChecker);

        mediaEditorPresenter.setActionExecutor(mediaActionExecutor);
        return mediaEditorPresenter;
    }

}
