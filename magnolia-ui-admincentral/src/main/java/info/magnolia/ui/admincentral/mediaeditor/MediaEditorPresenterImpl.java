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

import info.magnolia.event.EventBus;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.mediaeditor.actionfactory.MediaEditorActionFactory;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorCompletedEvent;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorCompletedEvent.CompletionType;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorCompletedEvent.Handler;
import info.magnolia.ui.admincentral.mediaeditor.editmode.event.MediaEditorInternalEvent;
import info.magnolia.ui.admincentral.mediaeditor.editmode.factory.EditModeProviderFactory;
import info.magnolia.ui.admincentral.mediaeditor.editmode.field.MediaField;
import info.magnolia.ui.admincentral.mediaeditor.editmode.provider.EditModeProvider;
import info.magnolia.ui.admincentral.mediaeditor.editmode.provider.EditModeProvider.ActionContext;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.model.mediaeditor.definition.MediaEditorDefinition;
import info.magnolia.ui.model.mediaeditor.features.MediaEditorFeatureDefinition;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import info.magnolia.ui.vaadin.view.View;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.vaadin.data.Property.Transactional;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.TransactionalPropertyWrapper;

/**
 * Implementation of {@link MediaEditorPresenter}.
 */
public class MediaEditorPresenterImpl implements MediaEditorPresenter, ActionbarPresenter.Listener, MediaEditorInternalEvent.Handler {

    private Logger log = Logger.getLogger(getClass());

    private MediaEditorView view;

    private ActionbarPresenter actionbarPresenter;

    private MediaEditorDefinition definition;

    private MediaEditorActionFactory actionFactory;

    private MediaField currentMediaField;

    private ObjectProperty<byte[]> dataSource;

    private Transactional<byte[]> transactionHandler;

    private EventBus eventBus;

    private ActionExecutor actionExecutor;

    private EditModeProviderFactory providerFactory;
    
    public MediaEditorPresenterImpl(
            MediaEditorDefinition definition,
            EventBus eventBus,
            MediaEditorView view,
            ActionbarPresenter actionbarPresenter,
            MediaEditorActionFactory actionFactory, 
            EditModeProviderFactory providerFactory) {
        this.eventBus = eventBus;
        this.view = view;
        this.actionFactory = actionFactory;
        this.actionbarPresenter = actionbarPresenter;
        this.definition = definition;
        this.actionbarPresenter.setListener(this);
        this.providerFactory = providerFactory;
        eventBus.addHandler(MediaEditorInternalEvent.class, this);
    }

    @Override
    public void setActionExecutor(ActionExecutor actionExecutor) {
        this.actionExecutor = actionExecutor;
    }
    
    @Override
    public View start(final InputStream stream) {
        try {
            final ActionbarView actionbar = actionbarPresenter.start(definition.getActionBar());
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
    public void onSubmit(MediaEditorInternalEvent event) {
        transactionHandler.commit();
        InputStream is = new ByteArrayInputStream(transactionHandler.getValue());
        eventBus.fireEvent(new MediaEditorCompletedEvent(CompletionType.SUBMIT, is));
    }

    @Override
    public void onCancelAll(MediaEditorInternalEvent event) {
        transactionHandler.rollback();
        transactionHandler.startTransaction();
        InputStream is = new ByteArrayInputStream(transactionHandler.getValue());
        eventBus.fireEvent(new MediaEditorCompletedEvent(CompletionType.CANCEL, is));
    }

    @Override
    public void onCancelLast(MediaEditorInternalEvent e) {
        switchToDefaultMode();
    }

    @Override
    public void onApply(MediaEditorInternalEvent e) {
        switchToDefaultMode();
    }

    @Override
    public void switchEditMode(EditModeProvider provider) {
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

        //switchEditMode(definition.getDefaultEditModeProvider());
    }

    @Override
    public HandlerRegistration addCompletionHandler(Handler handler) {
        return eventBus.addHandler(MediaEditorCompletedEvent.class, handler);
    }

    @Override
    public void onExecute(String actionName) {
        try {
            actionExecutor.execute(actionName, this, providerFactory);
        } catch (ActionExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getLabel(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return actionDefinition != null ? actionDefinition.getLabel() : null;
    }

    @Override
    public String getIcon(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return actionDefinition != null ? actionDefinition.getIcon() : null;
    }

    @Override
    public void setFullScreen(boolean fullscreen) {
        // TODO Auto-generated method stub

    }

    @Override
    public MediaEditorDefinition getDefinition() {
        return definition;
    }

    @Override
    public MediaField getCurrentMediaField() {
        return currentMediaField;
    }
}
