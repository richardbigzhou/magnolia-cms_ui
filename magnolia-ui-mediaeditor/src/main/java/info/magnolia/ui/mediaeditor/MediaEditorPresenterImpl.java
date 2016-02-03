/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import info.magnolia.event.HandlerRegistration;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.DialogPresenter;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.definition.ConfiguredDialogDefinition;
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingProperty;
import info.magnolia.ui.mediaeditor.data.EditHistoryTrackingPropertyImpl;
import info.magnolia.ui.mediaeditor.definition.MediaEditorDefinition;
import info.magnolia.ui.mediaeditor.event.MediaEditorCompletedEvent;
import info.magnolia.ui.mediaeditor.event.MediaEditorCompletedEvent.CompletionType;
import info.magnolia.ui.mediaeditor.event.MediaEditorCompletedEvent.Handler;
import info.magnolia.ui.mediaeditor.event.MediaEditorInternalEvent;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.vaadin.server.ClientConnector;

/**
 * Implementation of {@link MediaEditorPresenter}.
 */
public class MediaEditorPresenterImpl implements MediaEditorPresenter, ActionbarPresenter.Listener, MediaEditorInternalEvent.Handler, EditHistoryTrackingProperty.Listener {

    private Logger log = Logger.getLogger(getClass());
    private MediaEditorView view;
    private ActionbarPresenter actionbarPresenter;
    private MediaEditorDefinition definition;
    private DialogPresenter dialogPresenter;
    private AppContext appContext;
    private EditHistoryTrackingProperty dataSource;
    private EventBus eventBus;
    private ActionExecutor actionExecutor;
    private HandlerRegistration internalMediaEditorEventHandlerRegistration;
    private Set<HandlerRegistration> completionHandlers = new HashSet<HandlerRegistration>();
    private final SimpleTranslator i18n;

    public MediaEditorPresenterImpl(
            MediaEditorDefinition definition,
            EventBus eventBus,
            MediaEditorView view,
            ActionbarPresenter actionbarPresenter,
            DialogPresenter dialogPresenter,
            AppContext appContext,
            SimpleTranslator i18n) {
        this.eventBus = eventBus;
        this.view = view;
        this.actionbarPresenter = actionbarPresenter;
        this.definition = definition;
        this.dialogPresenter = dialogPresenter;
        this.appContext = appContext;
        this.i18n = i18n;
        this.actionbarPresenter.setListener(this);
        this.internalMediaEditorEventHandlerRegistration = eventBus.addHandler(MediaEditorInternalEvent.class, this);
        this.view.asVaadinComponent().addDetachListener(new ClientConnector.DetachListener() {
            @Override
            public void detach(ClientConnector.DetachEvent event) {
                dataSource.purgeHistory();
            }
        });
    }

    @Override
    public void setActionExecutor(ActionExecutor actionExecutor) {
        this.actionExecutor = actionExecutor;
    }

    @Override
    public View start(final InputStream stream) {
        try {
            final ActionbarView actionbar = actionbarPresenter.start(definition.getActionBar());
            final DialogView dialogView = dialogPresenter.start(new ConfiguredDialogDefinition(), appContext);

            this.dataSource = new EditHistoryTrackingPropertyImpl(IOUtils.toByteArray(stream), i18n);
            this.dataSource.setListener(this);
            view.setActionBar(actionbar);
            view.setDialog(dialogView);
            switchToDefaultMode();
            return view;
        } catch (IOException e) {
            errorOccurred(i18n.translate("ui-mediaeditor.mediaeditorPresenter.errorWhileEditing")+" ", e);
            log.error("Error occurred while editing media: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return null;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onSubmit(MediaEditorInternalEvent event) {
        dataSource.commit();
        complete(CompletionType.SUBMIT);
    }

    @Override
    public void onCancelAll(MediaEditorInternalEvent event) {
        dataSource.revert();
        complete(CompletionType.CANCEL);
    }

    @Override
    public void onLastActionCancelled(MediaEditorInternalEvent e) {
        switchToDefaultMode();
    }

    @Override
    public void onLastActionApplied(MediaEditorInternalEvent e) {
        switchToDefaultMode();
    }

    private void switchToDefaultMode() {
        doExecuteMediaEditorAction(definition.getDefaultAction());
    }

    @Override
    public HandlerRegistration addCompletionHandler(Handler handler) {
        HandlerRegistration hr = eventBus.addHandler(MediaEditorCompletedEvent.class, handler);
        completionHandlers.add(hr);
        return hr;
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
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
    public MediaEditorDefinition getDefinition() {
        return definition;
    }

    private void complete(CompletionType completionType) {
        InputStream is = new ByteArrayInputStream(dataSource.getValue());
        eventBus.fireEvent(new MediaEditorCompletedEvent(completionType, is));
        clearEventHandlers();
    }

    private void clearEventHandlers() {
        internalMediaEditorEventHandlerRegistration.removeHandler();
        for (HandlerRegistration hr : completionHandlers) {
            hr.removeHandler();
        }
    }

    private void doExecuteMediaEditorAction(String actionName) {
        try {
            actionExecutor.execute(actionName, this, view, dataSource);
        } catch (ActionExecutionException e) {
            errorOccurred(i18n.translate("ui-mediaeditor.mediaeditorPresenter.actionExecutionException")+" ", e);
            log.warn("Unable to execute action [" + actionName + "]", e);
        }
    }

    @Override
    public void errorOccurred(String message, Throwable e) {
        Message error = new Message(MessageType.ERROR, message, e.getMessage());
        appContext.sendLocalMessage(error);
    }
}
