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
package info.magnolia.ui.contentapp.detail;

import info.magnolia.event.EventBus;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.FormBuilder;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.form.FormView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Presenter for the item displayed in the
 * {@link info.magnolia.ui.contentapp.detail.DetailEditorPresenter}. Takes care
 * of building and switching between the right {@link DetailView.ViewType}.
 */
public class DetailPresenter implements DialogActionListener, EditorCallback, EditorValidator {

    private SubAppContext subAppContext;
    private ActionExecutor actionExecutor;
    private final EventBus eventBus;

    private final DetailView view;

    private FormBuilder formBuilder;

    private EditorDefinition editorDefinition;

    private JcrNodeAdapter item;
    private FormView formView;

    @Inject
    public DetailPresenter(SubAppContext subAppContext, final ActionExecutor actionExecutor,
            final @Named(AdmincentralEventBus.NAME) EventBus eventBus, DetailView view,
            FormBuilder formBuilder) {
        this.subAppContext = subAppContext;
        this.actionExecutor = actionExecutor;
        this.eventBus = eventBus;
        this.view = view;
        this.formBuilder = formBuilder;
    }

    public DetailView start(EditorDefinition editorDefinition, final JcrNodeAdapter item, DetailView.ViewType viewType) {
        this.editorDefinition = editorDefinition;
        this.item = item;
        setItemView(viewType);
        return view;
    }

    private void setItemView(DetailView.ViewType viewType) {

        switch (viewType) {
        case VIEW:
        case EDIT:
        default:
            this.formView = formBuilder.buildForm(editorDefinition.getForm(), item, null);
            initActions();
            view.setItemView(formView.asVaadinComponent(), viewType);
            break;
        }
    }

    private void initActions() {
        for (final ActionDefinition action : subAppContext.getSubAppDescriptor().getActions().values()) {
            formView.addAction(action.getName(), action.getLabel(), new DialogActionListener() {
                @Override
                public void onActionExecuted(final String actionName) {
                    try {
                        actionExecutor.execute(actionName, item, DetailPresenter.this);
                    } catch (ActionExecutionException e) {
                        throw new RuntimeException("Could not execute action: " + actionName, e);
                    }
                }
            });
        }
    }

    @Override
    public void onActionExecuted(String actionName) {
        try {
            actionExecutor.execute(actionName, item, this, this);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.",
                    e.getMessage());
            subAppContext.getAppContext().broadcastMessage(error);
        }
    }

    @Override
    public void onCancel() {
        // setItemView(ItemView.ViewType.VIEW);
        subAppContext.close();
    }

    @Override
    public void onSuccess(String actionName) {
        eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
        // setItemView(ItemView.ViewType.VIEW);
        subAppContext.close();
    }

    @Override
    public void showValidation(boolean visible) {
        formView.showValidation(visible);
    }

    @Override
    public boolean isValid() {
        return formView.isValid();
    }
}
