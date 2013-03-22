/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.contentapp.item;

import info.magnolia.event.EventBus;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.contentapp.definition.FormActionItemDefinition;
import info.magnolia.ui.form.FormPresenter;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.AdminCentralEventBusConfigurer;
import info.magnolia.ui.framework.event.ContentChangedEvent;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.model.action.ActionExecutor;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.form.FormView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Presenter for the item displayed in the {@link info.magnolia.ui.contentapp.workbench.ItemWorkbenchPresenter}. Takes
 * care of building and switching between the right {@link ItemView.ViewType}.
 */
public class ItemPresenter implements DialogActionListener, FormPresenter.Callback, FormPresenter.Validator {

    private SubAppContext subAppContext;
    private ActionExecutor actionExecutor;
    private final EventBus eventBus;

    private final ItemView view;

    private FormPresenter formPresenter;

    private EditorDefinition editorDefinition;

    private JcrNodeAdapter item;

    @Inject
    public ItemPresenter(SubAppContext subAppContext, final ActionExecutor actionExecutor, final @Named(AdminCentralEventBusConfigurer.EVENT_BUS_NAME) EventBus eventBus, ItemView view, FormPresenter formPresenter) {
        this.subAppContext = subAppContext;
        this.actionExecutor = actionExecutor;
        this.eventBus = eventBus;
        this.view = view;
        this.formPresenter = formPresenter;
    }

    public ItemView start(EditorDefinition editorDefinition, final JcrNodeAdapter item, ItemView.ViewType viewType) {
        this.editorDefinition = editorDefinition;
        this.item = item;

        setItemView(viewType);
        return view;
    }

    private void setItemView(ItemView.ViewType viewType) {

        switch (viewType) {
        case VIEW:
        case EDIT:
        default:
            final FormView formView = formPresenter.start(item, editorDefinition.getForm(), this, null);

            initActions();
            view.setItemView(formView.asVaadinComponent(), viewType);

            break;

        }
    }

    private void initActions() {
        for (final FormActionItemDefinition action : this.editorDefinition.getActions()) {
            formPresenter.addAction(action.getName(), getLabel(action.getName()), this);
        }
    }
    @Override
    public void onActionExecuted(String actionName) {
        try {
            actionExecutor.execute(actionName, item, this, this);
        } catch (ActionExecutionException e) {
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            subAppContext.getAppContext().broadcastMessage(error);
        }
    }

    public String getLabel(String actionName) {
        ActionDefinition actionDefinition = actionExecutor.getActionDefinition(actionName);
        return actionDefinition != null ? actionDefinition.getLabel() : null;
    }

    @Override
    public void onCancel() {
        //setItemView(ItemView.ViewType.VIEW);
        subAppContext.close();
    }

    @Override
    public void onSuccess(String actionName) {
        eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getPath()));
        //setItemView(ItemView.ViewType.VIEW);
        subAppContext.close();
    }

    @Override
    public void showValidation(boolean visible) {
        formPresenter.showValidation(visible);
    }

    @Override
    public boolean isValid() {
        return formPresenter.isValid();
    }
}
