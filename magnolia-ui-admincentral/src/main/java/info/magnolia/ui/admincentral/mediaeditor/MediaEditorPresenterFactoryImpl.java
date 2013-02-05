/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.mediaeditor;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.actionbar.builder.ActionbarBuilder;
import info.magnolia.ui.admincentral.mediaeditor.editmode.factory.EditModeBuilderFactory;
import info.magnolia.ui.admincentral.mediaeditor.editmode.presenter.EditorPresenter;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.mediaeditor.definition.MediaEditorDefinition;
import info.magnolia.ui.model.mediaeditor.registry.MediaEditorRegistry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * MediaEditorPresenterFactoryImpl.
 */
@Singleton
public class MediaEditorPresenterFactoryImpl implements MediaEditorPresenterFactory {

    private ActionbarBuilder actionbarBuilder;
    
    private ComponentProvider componentProvider;
    
    private MediaEditorRegistry registry;
    
    private EditModeBuilderFactory modeBuilderFactory;
    
    private EventBus eventBus;
    
    @Inject
    public MediaEditorPresenterFactoryImpl(ActionbarBuilder actionbarBuilder, ComponentProvider componentProvider,
            MediaEditorRegistry registry, @Named("system") EventBus eventBus, Shell shell, EditModeBuilderFactory builderFactory) {
        this.actionbarBuilder = actionbarBuilder;
        this.componentProvider = componentProvider;
        this.modeBuilderFactory = builderFactory;
        this.eventBus = eventBus;
        this.registry = registry;
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
            throw new IllegalArgumentException("No media editor definition registered for name [" + id + "]");
        }
        return mediaEditorDefinition;
    }

    @Override
    public MediaEditorPresenter getPresenterByDefinition(MediaEditorDefinition definition) {
        MediaEditorView view = componentProvider.getComponent(MediaEditorView.class);
        EditorPresenter presenter = componentProvider.getComponent(EditorPresenter.class);
        return new MediaEditorPresenterImpl(eventBus, view, presenter, modeBuilderFactory);
    }

}
