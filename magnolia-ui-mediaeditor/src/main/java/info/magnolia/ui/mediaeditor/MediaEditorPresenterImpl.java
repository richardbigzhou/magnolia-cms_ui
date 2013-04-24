/**
 * This file Copyright (c) 2013 Magnolia International
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

import com.vaadin.data.Property.Transactional;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.TransactionalPropertyWrapper;
import info.magnolia.event.EventBus;
import info.magnolia.event.HandlerRegistration;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.mediaeditor.definition.MediaEditorDefinition;
import info.magnolia.ui.mediaeditor.editmode.event.MediaEditorCompletedEvent;
import info.magnolia.ui.mediaeditor.editmode.event.MediaEditorCompletedEvent.CompletionType;
import info.magnolia.ui.mediaeditor.editmode.event.MediaEditorCompletedEvent.Handler;
import info.magnolia.ui.mediaeditor.editmode.event.MediaEditorInternalEvent;
import info.magnolia.ui.mediaeditor.editmode.factory.EditModeProviderFactory;
import info.magnolia.ui.mediaeditor.editmode.field.MediaField;
import info.magnolia.ui.mediaeditor.editmode.provider.EditModeProvider;
import info.magnolia.ui.mediaeditor.editmode.provider.EditModeProvider.ActionContext;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.model.overlay.View;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link MediaEditorPresenter}.
 */
public class MediaEditorPresenterImpl implements MediaEditorPresenter, ActionbarPresenter.Listener, MediaEditorInternalEvent.Handler {

    private Logger log = Logger.getLogger(getClass());

    private MediaEditorView view;

    private ActionbarPresenter actionbarPresenter;

    private MediaEditorDefinition definition;

    private MediaField currentMediaField;

    private ObjectProperty<byte[]> dataSource;

    private Transactional<byte[]> transactionHandler;

    private EventBus eventBus;

    private ActionExecutor actionExecutor;

    private EditModeProviderFactory providerFactory;

    private HandlerRegistration internalMediaEditorEventHandlerRegistration;

    private Set<HandlerRegistration> completionHandlers = new HashSet<HandlerRegistration>();

    public MediaEditorPresenterImpl(
            MediaEditorDefinition definition,
            EventBus eventBus,
            MediaEditorView view,
            ActionbarPresenter actionbarPresenter,
            EditModeProviderFactory providerFactory) {
        this.eventBus = eventBus;
        this.view = view;
        this.actionbarPresenter = actionbarPresenter;
        this.definition = definition;
        this.actionbarPresenter.setListener(this);
        this.providerFactory = providerFactory;
        this.internalMediaEditorEventHandlerRegistration = eventBus.addHandler(MediaEditorInternalEvent.class, this);
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

    @Override
    public void onSubmit(MediaEditorInternalEvent event) {
        transactionHandler.commit();
        InputStream is = new ByteArrayInputStream(transactionHandler.getValue());
        eventBus.fireEvent(new MediaEditorCompletedEvent(CompletionType.SUBMIT, is));
        clearEventHandlers();
    }

    @Override
    public void onCancelAll(MediaEditorInternalEvent event) {
        transactionHandler.rollback();
        transactionHandler.startTransaction();
        InputStream is = new ByteArrayInputStream(transactionHandler.getValue());
        eventBus.fireEvent(new MediaEditorCompletedEvent(CompletionType.CANCEL, is));
        clearEventHandlers();
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
        doExecuteMediaEditorAction(definition.getDefaultEditModeProvider());
    }

    @Override
    public HandlerRegistration addCompletionHandler(Handler handler) {
        HandlerRegistration hr = eventBus.addHandler(MediaEditorCompletedEvent.class, handler);
        completionHandlers.add(hr);
        return hr;
    }

    @Override
    public void onExecute(String actionName) {
        doExecuteMediaEditorAction(actionName);
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

    private void clearEventHandlers() {
        internalMediaEditorEventHandlerRegistration.removeHandler();
        for (HandlerRegistration hr : completionHandlers) {
            hr.removeHandler();
        }
    }

    private void doExecuteMediaEditorAction(String actionName) {
        try {
            if (currentMediaField != null) {
                actionExecutor.execute(actionName, this, providerFactory, currentMediaField);
            } else {
                actionExecutor.execute(actionName, this, providerFactory);
            }

        } catch (ActionExecutionException e) {
            log.warn("Unable to execute action [" + actionName + "]");
        }
    }
}