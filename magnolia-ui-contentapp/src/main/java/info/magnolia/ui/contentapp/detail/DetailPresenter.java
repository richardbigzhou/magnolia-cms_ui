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
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.contentapp.definition.EditorDefinition;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.actionarea.ActionListener;
import info.magnolia.ui.dialog.actionarea.EditorActionAreaPresenter;
import info.magnolia.ui.dialog.actionarea.definition.FormActionItemDefinition;
import info.magnolia.ui.dialog.actionarea.view.EditorActionAreaView;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.dialog.formdialog.FormView;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.framework.app.SubAppActionExecutor;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;
import com.vaadin.data.Item;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;

/**
 * Presenter for the item displayed in the {@link info.magnolia.ui.contentapp.detail.DetailEditorPresenter}. Takes care
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

    private Item item;

    private Object itemId;

    private DialogView dialogView;

    private final I18nizer i18nizer;

    private final SimpleTranslator i18n;

    private AvailabilityChecker checker;
    private ContentConnector contentConnector;

    @Inject
    public DetailPresenter(SubAppContext subAppContext, final @Named(AdmincentralEventBus.NAME) EventBus eventBus, DetailView view,
            FormBuilder formBuilder, ComponentProvider componentProvider, SubAppActionExecutor executor, I18nizer i18nizer, SimpleTranslator i18n, AvailabilityChecker checker, ContentConnector contentConnector) {
        this.subAppContext = subAppContext;
        this.eventBus = eventBus;
        this.view = view;
        this.formBuilder = formBuilder;
        this.componentProvider = componentProvider;
        this.executor = executor;
        this.i18nizer = i18nizer;
        this.i18n = i18n;
        this.checker = checker;
        this.contentConnector = contentConnector;
    }

    public DetailView start(EditorDefinition editorDefinition, DetailView.ViewType viewType, Object itemId) {
        this.editorDefinition = editorDefinition;
        this.item = contentConnector.getItem(itemId);
        this.itemId = itemId;
        setItemView(viewType);
        return view;
    }

    private void setItemView(DetailView.ViewType viewType) {
        FormView formView = componentProvider.getComponent(FormView.class);
        this.dialogView = formView;

        FormDefinition formDefinition;

        switch (viewType) {
        case VIEW:
            formDefinition = cloneFormDefinitionReadOnly(editorDefinition.getForm());
            break;
        case EDIT:
        default:
            formDefinition = editorDefinition.getForm();
            break;
        }

        initActions();
        formBuilder.buildForm(formView, formDefinition, item, null);
        view.setItemView(dialogView.asVaadinComponent(), viewType);
    }

    private void initActions() {
        EditorActionAreaPresenter editorActionAreaPresenter = componentProvider.newInstance(editorDefinition.getActionArea().getPresenterClass());
        EditorActionAreaView editorActionAreaView = editorActionAreaPresenter.start(filterSubAppActions(), editorDefinition.getActionArea(), this, subAppContext);
        dialogView.setActionAreaView(editorActionAreaView);
    }

    private Iterable<ActionDefinition> filterSubAppActions() {
        Map<String, ActionDefinition> subAppActions = subAppContext.getSubAppDescriptor().getActions();
        List<ActionDefinition> filteredActions = new LinkedList<ActionDefinition>();
        List<FormActionItemDefinition> editorActions = editorDefinition.getActions();
        for (FormActionItemDefinition editorAction : editorActions) {
            ActionDefinition def = subAppActions.get(editorAction.getName());
            AvailabilityDefinition availability = executor.getActionDefinition(editorAction.getName()).getAvailability();
            if (def != null && checker.isAvailable(availability, Arrays.asList(itemId))) {
                filteredActions.add(def);
            } else {
                log.debug("Action is configured for an editor but not configured for sub-app: " + editorAction.getName());
            }
        }
        return filteredActions;
    }

    /**
     * Return clone of a form definition with all fields definitions set to read only.
     *
     * @see ConfiguredFieldDefinition#setReadOnly(boolean)
     */
    private FormDefinition cloneFormDefinitionReadOnly(FormDefinition formDefinition) {
        Cloner cloner = new Cloner();
        FormDefinition formDefinitionClone = cloner.deepClone(formDefinition);

        for (TabDefinition tab : formDefinitionClone.getTabs()) {
            for (FieldDefinition field : tab.getFields()) {
                ((ConfiguredFieldDefinition) field).setReadOnly(true);
            }
        }

        return formDefinitionClone;
    }

    @Override
    public void onCancel() {
        // setItemView(ItemView.ViewType.VIEW);
        subAppContext.close();
    }

    @Override
    public void onSuccess(String actionName) {
        eventBus.fireEvent(new ContentChangedEvent(itemId));
        // setItemView(ItemView.ViewType.VIEW);
        subAppContext.close();
    }

    @Override
    public void showValidation(boolean visible) {
        if (dialogView instanceof FormView) {
            ((FormView) dialogView).showValidation(visible);
        }
    }

    @Override
    public boolean isValid() {
        return dialogView instanceof FormView ? ((FormView) dialogView).isValid() : true;
    }

    @Override
    public void onActionFired(String actionName, Object... actionContextParams) {
        Object[] providedParameters = new Object[] { this, item };
        Object[] combinedParameters = new Object[providedParameters.length + actionContextParams.length];
        System.arraycopy(providedParameters, 0, combinedParameters, 0, providedParameters.length);
        System.arraycopy(actionContextParams, 0, combinedParameters, providedParameters.length, actionContextParams.length);
        try {
            executor.execute(actionName, combinedParameters);
        } catch (ActionExecutionException e) {
            log.error("An error occurred while executing an action.", e);
            Message error = new Message(MessageType.ERROR, i18n.translate("ui-contentapp.error.action.execution"), e.getMessage());
            subAppContext.getAppContext().sendLocalMessage(error);
        }
    }

    private EditorDefinition getEditorDefinition() {
        return ((DetailSubAppDescriptor)subAppContext.getSubAppDescriptor()).getEditor();
    }

    public Item getItem() {
        return item;
    }

    /**
     * Add a shortcut key for the button for a specific action.
     */
    public void addClickShortcut(String actionName, int KeyCode){
        Button button = (Button)(dialogView.getActionAreaView().getViewForAction(actionName).asVaadinComponent());
        button.setClickShortcut(KeyCode);
    }

    /**
     * Add a shortcut key for a specific action.
     */
    public void addShortcut(final String actionName, final int keyCode, final int... modifiers) {
        dialogView.addShortcut(new ShortcutListener(actionName, keyCode, modifiers) {
            @Override
            public void handleAction(Object sender, Object target) {
                onActionFired(actionName, new HashMap<String, Object>());
            }
        });
    }

    public void addShortcut(ShortcutListener shortcut) {
        dialogView.addShortcut(shortcut);
    }

}
