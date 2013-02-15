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

import info.magnolia.ui.admincentral.event.ActionbarItemClickedEvent;
import info.magnolia.ui.admincentral.mediaeditor.actionbar.MediaEditorActionbarPresenter;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorEvent;
import info.magnolia.ui.admincentral.mediaeditor.editmode.factory.EditModeProviderFactory;
import info.magnolia.ui.admincentral.mediaeditor.editmode.field.MediaField;
import info.magnolia.ui.admincentral.mediaeditor.editmode.provider.EditModeProvider;
import info.magnolia.ui.admincentral.mediaeditor.editmode.provider.EditModeProvider.ActionContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionFactory;
import info.magnolia.ui.model.mediaeditor.definition.MediaEditorDefinition;
import info.magnolia.ui.model.mediaeditor.features.MediaEditorFeatureDefinition;
import info.magnolia.ui.model.mediaeditor.provider.EditModeProviderActionDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.vaadin.data.Property.Transactional;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.TransactionalPropertyWrapper;

/**
 * MediaEditorPresenterImpl.
 */
public class MediaEditorPresenterImpl implements MediaEditorPresenter, MediaEditorEvent.Handler {

    private Logger log = Logger.getLogger(getClass());
    
    /**
     * ActionbarEventHandler.
     */
    private final class ActionbarEventHandler implements ActionbarItemClickedEvent.Handler {
        @Override
        public void onActionbarItemClicked(ActionbarItemClickedEvent event) {
            final ActionDefinition actionDefinition = event.getActionDefinition();
            dispatchActionbarEvent(actionDefinition);
        }
    }

    private MediaEditorView view;

    private EditModeProviderFactory editModeBuilderFactory;

    private MediaEditorActionbarPresenter actionbarPresenter;

    private MediaEditorDefinition definition;

    private ActionFactory<ActionDefinition, Action> actionFactory;

    private MediaField currentMediaField;
   
    private ObjectProperty<byte[]> dataSource;
    
    private Transactional<byte[]> transactionHandler;
    
    public MediaEditorPresenterImpl(
            MediaEditorDefinition definition, 
            EventBus eventBus, 
            MediaEditorView view,
            EditModeProviderFactory modeBuilderFactory,
            MediaEditorActionbarPresenter actionbarPresenter, 
            ActionFactory<ActionDefinition, Action> actionFactory) {
        eventBus.addHandler(ActionbarItemClickedEvent.class, new ActionbarEventHandler());
        this.view = view;
        this.actionFactory = actionFactory;
        this.editModeBuilderFactory = modeBuilderFactory;
        this.actionbarPresenter = actionbarPresenter;
        this.definition = definition;
        eventBus.addHandler(MediaEditorEvent.class, this);
    }

    @Override
    public View start(final InputStream stream) {
        try {
            final ActionbarView actionbar = actionbarPresenter.start(definition.getActionBar(), actionFactory);
            final byte[] bytes = IOUtils.toByteArray(stream);
            dataSource = new ObjectProperty<byte[]>(bytes);
            transactionHandler = new TransactionalPropertyWrapper<byte[]>(dataSource);
            transactionHandler.startTransaction();
            view.setActionBar(actionbar);
            switchToDefaultMode();
            return view;
        } catch (IOException e) {
            log.error("Error occured while editing media: " + e.getMessage(), e);
        }
        return null;
    }
    
    protected void dispatchActionbarEvent(ActionDefinition actionDefinition) {
        if (actionDefinition instanceof EditModeProviderActionDefinition) {
            switchEditMode((EditModeProviderActionDefinition)actionDefinition);
        }
        if (actionDefinition instanceof MediaEditorFeatureDefinition) {
            MediaEditorFeatureDefinition def = (MediaEditorFeatureDefinition)actionDefinition;
            try {
                final Class<?> clazz = Class.forName(def.getRequiredInterfaceName());
                if (clazz.isInstance(currentMediaField)) {
                    actionFactory.createAction(def, currentMediaField).execute();
                }
            } catch (ClassNotFoundException e) {
                log.error("Action required interface does not exist: " + e.getMessage(), e);
            } catch (ActionExecutionException e) {
                log.error("Action failed: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onSubmit(MediaEditorEvent e) {
        transactionHandler.commit();
    }

    @Override
    public void onCancelAll(MediaEditorEvent e) {
        transactionHandler.rollback();
        transactionHandler.startTransaction();
        switchToDefaultMode();
    }
    
    @Override
    public void onCancelLast(MediaEditorEvent e) {
        switchToDefaultMode();
    }
    
    @Override
    public void onApply(MediaEditorEvent e) {
        switchToDefaultMode();
    }
    
    private void switchEditMode(EditModeProviderActionDefinition definition) {
        EditModeProvider provider = editModeBuilderFactory.getBuilder(definition);
        if (provider != null) {
            doSwitchEditMode(provider);
        } else {        
            log.warn("No provider was found for definition " + definition.getClass().getName());
        }
    }

    private void doSwitchEditMode(EditModeProvider provider) {
        MediaField newMediaField = provider.getMediaField();
        if (newMediaField != null) {
            this.currentMediaField = newMediaField;
            
            view.clearActions();
            view.setMediaContent(currentMediaField);
            view.setToolbar(provider.getStatusControls());
            
            for (ActionContext ctx : provider.getActionContextList()) {
                view.getDialog().addAction(ctx.getActionId(), ctx.getLabel(), ctx.getListener());
            }                   
            currentMediaField.setPropertyDataSource(dataSource);
        } else {
            log.warn("Provider did not provide any content UI ");    
        }
    }

    private void switchToDefaultMode() {
        switchEditMode(definition.getDefaultEditModeProvider());
    }
}
