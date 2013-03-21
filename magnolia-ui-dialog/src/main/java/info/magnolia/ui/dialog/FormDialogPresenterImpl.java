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
package info.magnolia.ui.dialog;

import info.magnolia.event.EventBus;
import info.magnolia.ui.dialog.action.DialogActionFactory;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.form.FormPresenter;
import info.magnolia.ui.model.action.Action;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.dialog.FormDialogView;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.view.ModalCloser;
import info.magnolia.ui.vaadin.view.ModalLayer;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.data.Item;

/**
 * Presenter for forms opened inside dialogs.
 * Combines functionality of {@link DialogPresenter} and {@link FormPresenter}.
 */
public class FormDialogPresenterImpl extends BaseDialogPresenter implements FormDialogPresenter {

    private final DialogBuilder dialogBuilder;

    private final FormDialogView view;

    private Callback callback;

    private final DialogActionFactory dialogActionFactory;

    private FormPresenter formPresenter;


    @Inject
    public FormDialogPresenterImpl(final FormDialogView view, final DialogBuilder dialogBuilder, final FormPresenter formPresenter, @Named("admincentral") EventBus eventBus, final DialogActionFactory actionFactory) {
        super(view, eventBus);
        this.view = view;
        this.dialogBuilder = dialogBuilder;
        this.formPresenter = formPresenter;
        this.dialogActionFactory = actionFactory;
    }

    /**
     * Returns a {@link DialogView} containing {@link info.magnolia.ui.vaadin.form.FormView} as content.
     * <ul>
     * <li>Delegates the building of the {@link FormDialogPresenter} to the {@link FormDialogPresenterFactory}.</li>
     * <li>Sets the created {@link info.magnolia.ui.vaadin.form.FormView} as content of the created {@link DialogView}.</li>
     * </ul>
     *
     * @param item passed on to{@link FormDialogPresenter}
     * @param dialogDefinition
     */
    @Override
    public DialogView start(final Item item, DialogDefinition dialogDefinition, final ModalLayer modalLayer) {
        dialogBuilder.buildFormDialog(dialogDefinition, view);

        // This is needed to access properties from the parent. Currently only the i18basename.
        Dialog dialog = new Dialog(dialogDefinition);
        view.setFormView(formPresenter.start(item, dialogDefinition.getFormDefinition(), null, dialog));

        final ModalCloser modalCloser = modalLayer.openModal(view);

         addDialogCloseHandler(new BaseDialog.DialogCloseEvent.Handler() {
             @Override
             public void onClose(BaseDialog.DialogCloseEvent event) {
                modalCloser.close();

                 event.getView().asVaadinComponent().removeDialogCloseHandler(this);
             }
         });
        initActions(dialogDefinition);

        return view;
    }

    private void initActions(final DialogDefinition dialogDefinition) {
        for (final ActionDefinition action : dialogDefinition.getActions()) {
            addAction(action.getName(), action.getLabel(), new DialogActionListener() {
                @Override
                public void onActionExecuted(final String actionName) {
                    final Action action1 = dialogActionFactory.createAction(action, FormDialogPresenterImpl.this);
                    try {
                        action1.execute();
                    } catch (final ActionExecutionException e) {
                        e.printStackTrace();
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
    public FormPresenter getForm() {
        return formPresenter;
    }

    @Override
    public Callback getCallback() {
        return this.callback;
    }

    /**
     * Registers callback functions created by caller.
     */
    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

}