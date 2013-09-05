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
package info.magnolia.ui.dialog.formdialog;

import com.vaadin.data.Item;
import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ActionPresenter;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.dialog.Dialog;
import info.magnolia.ui.dialog.DialogCloseHandler;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.dialog.action.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.ActionPresenterDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.action.presenter.DefaultEditorActionPresenter;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;

/**
 * Presenter for forms opened inside dialogs.
 */
public class FormDialogPresenterImpl extends BaseDialogPresenter implements FormDialogPresenter, EditorValidator {

    private EditorCallback callback;

    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private DialogActionExecutor actionExecutor;
    private FormBuilder formBuilder;
    private FormView formView;
    private Item item;


    @Inject
    public FormDialogPresenterImpl(final DialogDefinitionRegistry dialogDefinitionRegistry, final DialogActionExecutor actionExecutor, FormBuilder formBuilder, ComponentProvider componentProvider) {
        super(componentProvider);
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.actionExecutor = actionExecutor;
        this.formBuilder = formBuilder;
        this.componentProvider = componentProvider;
    }


    @Override
    public DialogView start(final Item item, String dialogName, final OverlayLayer overlayLayer, EditorCallback callback) {
        try {
            FormDialogDefinition dialogDefinition = dialogDefinitionRegistry.get(dialogName);
            return start(item, dialogDefinition, overlayLayer, callback);
        } catch (RegistrationException e) {
            throw new RuntimeException("No dialogDefinition found for " + dialogName, e);
        }
    }
    /**
     * Returns a {@link DialogView} containing {@link FormView} as content.
     * <ul>
     * <li>Sets the created {@link FormView} as content of the created {@link DialogView}.</li>
     * </ul>
     *
     * @param item passed on to{@link FormDialogPresenter}
     * @param dialogDefinition
     */
    @Override
    public DialogView start(final Item item, FormDialogDefinition dialogDefinition, final OverlayLayer overlayLayer, EditorCallback callback) {
        this.callback = callback;
        this.item = item;

        actionExecutor.setDialogDefinition(dialogDefinition);
        buildView(dialogDefinition);
        final OverlayCloser overlayCloser = overlayLayer.openOverlay(getView());

        getView().addDialogCloseHandler(new DialogCloseHandler() {
            @Override
            public void onDialogClose(DialogView dialogView) {
                overlayCloser.close();
            }
        });
        getView().setClosable(true);
        initActions(dialogDefinition);
        return formView;
    }

    @Override
    protected DialogView initView() {
        return formView;
    }

    private void buildView(FormDialogDefinition dialogDefinition) {
        Dialog dialog = new Dialog(dialogDefinition);
        formView = formBuilder.buildForm(dialogDefinition.getForm(), item, dialog);
        start();
        final String description = dialogDefinition.getDescription();
        final String label = dialogDefinition.getLabel();
        final String basename = dialogDefinition.getI18nBasename();

        if (StringUtils.isNotBlank(description)) {
            String i18nDescription = MessagesUtil.getWithDefault(description, description, basename);
            getView().setDescription(i18nDescription);
        }

        if (StringUtils.isNotBlank(label)) {
            String i18nLabel = MessagesUtil.getWithDefault(label, label, basename);
            getView().setCaption(i18nLabel);
        }
    }

    private void initActions(final FormDialogDefinition definition) {
        DefaultEditorActionPresenter defaultPresenter = new DefaultEditorActionPresenter();
        for (final ActionDefinition action : definition.getActions().values()) {
            ActionPresenterDefinition actionPresenterDef = definition.getActionPresenters().get(action.getName());
            ActionPresenter actionPresenter = actionPresenterDef == null ? defaultPresenter : componentProvider.getComponent(actionPresenterDef.getPresenterClass());
            addAction(action, actionPresenter, !definition.getSecondaryActions().contains(action.getName()));
        }
    }

    @Override
    protected void onActionFired(ActionDefinition definition, Object... actionContextParams) {
        try {
            actionExecutor.execute(definition.getName(), item, FormDialogPresenterImpl.this, callback);
        } catch (ActionExecutionException e) {
            throw new RuntimeException("Could not execute action: " + definition.getName(), e);
        }
    }

    @Override
    public FormView getView() {
        return (FormView) super.getView();
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