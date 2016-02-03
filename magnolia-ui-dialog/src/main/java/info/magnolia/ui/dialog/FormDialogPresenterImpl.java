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
package info.magnolia.ui.dialog;

import info.magnolia.cms.i18n.MessagesUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.dialog.action.DialogActionExecutor;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.FormBuilder;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.dialog.FormDialogView;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.form.FormView;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;

/**
 * Presenter for forms opened inside dialogs.
 */
public class FormDialogPresenterImpl extends BaseDialogPresenter implements FormDialogPresenter, EditorValidator {

    private final FormDialogView view;

    private EditorCallback callback;

    private DialogDefinitionRegistry dialogDefinitionRegistry;
    private DialogActionExecutor actionExecutor;
    private FormBuilder formBuilder;
    private FormView formView;
    private Item item;


    @Inject
    public FormDialogPresenterImpl(final FormDialogView view, final DialogDefinitionRegistry dialogDefinitionRegistry, final DialogActionExecutor actionExecutor, FormBuilder formBuilder) {
        super(view);
        this.view = view;
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.actionExecutor = actionExecutor;
        this.formBuilder = formBuilder;
    }


    @Override
    public DialogView start(final Item item, String dialogName, final OverlayLayer overlayLayer, EditorCallback callback) {
        try {
            DialogDefinition dialogDefinition = dialogDefinitionRegistry.get(dialogName);

            return start(item, dialogDefinition, overlayLayer, callback);

        } catch (RegistrationException e) {
            throw new RuntimeException("No dialogDefinition found for " + dialogName, e);
        }
    }
    /**
     * Returns a {@link DialogView} containing {@link info.magnolia.ui.vaadin.form.FormView} as content.
     * <ul>
     * <li>Sets the created {@link info.magnolia.ui.vaadin.form.FormView} as content of the created {@link DialogView}.</li>
     * </ul>
     *
     * @param item passed on to{@link FormDialogPresenter}
     * @param dialogDefinition
     */
    @Override
    public DialogView start(final Item item, DialogDefinition dialogDefinition, final OverlayLayer overlayLayer, EditorCallback callback) {
        this.callback = callback;
        this.item = item;

        actionExecutor.setDialogDefinition(dialogDefinition);
        buildView(dialogDefinition);

        final OverlayCloser overlayCloser = overlayLayer.openOverlay(view);

        addDialogCloseHandler(new BaseDialog.DialogCloseEvent.Handler() {
            @Override
            public void onClose(BaseDialog.DialogCloseEvent event) {
                overlayCloser.close();

                getBaseDialog().removeDialogCloseHandler(this);
            }
        });
        showCloseButton();

        initActions(dialogDefinition);

        return view;
    }

    private void buildView(DialogDefinition dialogDefinition) {
        Dialog dialog = new Dialog(dialogDefinition);
        formView = formBuilder.buildForm(dialogDefinition.getForm(), item, dialog);
        view.setFormView(formView);

        final String description = dialogDefinition.getDescription();
        final String label = dialogDefinition.getLabel();
        final String basename = dialogDefinition.getI18nBasename();

        if (StringUtils.isNotBlank(description)) {
            String i18nDescription = MessagesUtil.getWithDefault(description, description, basename);
            view.setDialogDescription(i18nDescription);
        }

        if (StringUtils.isNotBlank(label)) {
            String i18nLabel = MessagesUtil.getWithDefault(label, label, basename);
            view.setCaption(i18nLabel);
        }
    }

    private void initActions(final DialogDefinition dialogDefinition) {
        for (final ActionDefinition action : dialogDefinition.getActions().values()) {
            addAction(action.getName(), action.getLabel(), new DialogActionListener() {
                @Override
                public void onActionExecuted(final String actionName) {

                    try {
                        actionExecutor.execute(actionName, item, FormDialogPresenterImpl.this, callback);
                    } catch (ActionExecutionException e) {
                        throw new RuntimeException("Could not execute action: " + actionName, e);
                    }
                }
            });
        }
    }

    @Override
    public FormDialogView getView() {
        return view;
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
