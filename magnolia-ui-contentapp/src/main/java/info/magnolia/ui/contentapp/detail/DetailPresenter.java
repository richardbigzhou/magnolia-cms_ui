/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.definition.FormActionItemDefinition;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.dialog.formdialog.FormView;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.framework.app.SubAppActionExecutor;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Presenter for the item displayed in the
 * {@link info.magnolia.ui.contentapp.detail.DetailEditorPresenter}. Takes care
 * of building and switching between the right {@link DetailView.ViewType}.
 */
public class DetailPresenter implements EditorCallback, EditorValidator, ActionListener {

    private Logger log = LoggerFactory.getLogger(getClass());

    private SubAppContext subAppContext;

    private final EventBus eventBus;

    private final DetailView view;

    private FormBuilder formBuilder;

    private ComponentProvider componentProvider;

    private ActionExecutor executor;

    private EditorDefinition editorDefinition;

    private JcrNodeAdapter item;

    private FormView formView;

    @Inject
    public DetailPresenter(SubAppContext subAppContext, final @Named(AdmincentralEventBus.NAME) EventBus eventBus, DetailView view,
            FormBuilder formBuilder, ComponentProvider componentProvider, SubAppActionExecutor executor, FormView formView) {
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
        this.view = view;
        this.formBuilder = formBuilder;
        this.componentProvider = componentProvider;
        this.executor = executor;
        this.formView = formView;
    }

    public DetailView start(EditorDefinition editorDefinition, final JcrNodeAdapter item, DetailView.ViewType viewType) {
        this.editorDefinition = editorDefinition;
        this.item = item;
        initActions();
        setItemView(viewType);
        return view;
    }

    private void setItemView(DetailView.ViewType viewType) {

        switch (viewType) {
        case VIEW:
        case EDIT:
        default:
            formBuilder.buildForm(formView, editorDefinition.getForm(), item, null);
            view.setItemView(formView.asVaadinComponent(), viewType);
            break;
        }
    }

    private void initActions() {
        EditorActionAreaPresenter editorActionAreaPresenter = componentProvider.getComponent(editorDefinition.getActionPresenter().getPresenterClass());
        EditorActionAreaView editorActionAreaView = editorActionAreaPresenter.start(filterSubAppActions(),editorDefinition.getActionPresenter(), this, subAppContext);
        formView.setActionView(editorActionAreaView);
    }

    private Iterable<ActionDefinition> filterSubAppActions() {
        Map<String, ActionDefinition> subAppActions = subAppContext.getSubAppDescriptor().getActions();
        List<ActionDefinition> filteredActions = new LinkedList<ActionDefinition>();
        List<FormActionItemDefinition> editorActions = editorDefinition.getActions();
        boolean isJcrItemAdapter = (item instanceof JcrItemAdapter);
        for (FormActionItemDefinition editorAction : editorActions) {
            ActionDefinition def = subAppActions.get(editorAction.getName());
            if (def != null || (isJcrItemAdapter && executor.isAvailable(editorAction.getName(), ((JcrItemAdapter)item).getJcrItem()))) {
                filteredActions.add(subAppActions.get(editorAction.getName()));
            } else {
                 log.warn("Action is configured for an editor but not configured for sub-app: " + editorAction.getName());
            }
        }
        return filteredActions;
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

    @Override
    public void onActionFired(String actionName, Object... actionContextParams) {
        Object[] providedParameters = new Object[]{this, item};
        Object[] combinedParameters = new Object[providedParameters.length + actionContextParams.length];
        System.arraycopy(providedParameters, 0, combinedParameters, 0, providedParameters.length);
        System.arraycopy(actionContextParams, 0, combinedParameters, providedParameters.length, actionContextParams.length);
        try {
            executor.execute(actionName, combinedParameters);
        } catch (ActionExecutionException e) {
            log.error("An error occurred while executing an action.", e);
            Message error = new Message(MessageType.ERROR, "An error occurred while executing an action.", e.getMessage());
            subAppContext.getAppContext().broadcastMessage(error);
        }
    }
}
