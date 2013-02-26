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
package info.magnolia.ui.admincentral.dialog;

import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.ShellImpl;
import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.form.FormPresenter;
import info.magnolia.ui.admincentral.form.FormPresenterFactory;
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog.DialogCloseEvent;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.dialog.FormDialogView;
import info.magnolia.ui.vaadin.dialog.Modal.ModalityLevel;
import info.magnolia.ui.vaadin.view.View;

import com.vaadin.data.Item;

/**
 * Presenter for forms opened inside dialogs.
 * Combines functionality of {@link DialogPresenter} and {@link FormPresenter}.
 */
public class FormDialogPresenterImpl extends BaseDialogPresenter implements FormDialogPresenter {

    private final DialogBuilder dialogBuilder;

    private FormPresenterFactory formPresenterFactory;

    private final DialogDefinition dialogDefinition;

    private final ShellImpl shell;

    private final FormDialogView view;

    private Callback callback;

    private final DialogActionFactory dialogActionFactory;
    private FormPresenter formPresenter;

    private final AppContext appContext;
    private final SubAppContext subAppContext;

    public FormDialogPresenterImpl(final FormDialogView view, final DialogBuilder dialogBuilder, final FormPresenterFactory formPresenterFactory,
            final DialogDefinition dialogDefinition, final Shell shell, EventBus eventBus, final DialogActionFactory actionFactory,
            final AppContext appContext, final SubAppContext subAppContext) {
        super(view, eventBus);
        this.view = view;
        this.dialogBuilder = dialogBuilder;
        this.formPresenterFactory = formPresenterFactory;
        this.dialogDefinition = dialogDefinition;
        this.shell = (ShellImpl) shell;
        this.dialogActionFactory = actionFactory;
        this.subAppContext = subAppContext;
        this.appContext = appContext;
        initActions(dialogDefinition);
    }

    /**
     * Returns a {@link DialogView} containing {@link info.magnolia.ui.vaadin.form.FormView} as content.
     * <ul>
     * <li>Delegates the building of the {@link FormPresenter} to the {@link FormPresenterFactory}.</li>
     * <li>Delegates the building of the {@link FormDialogPresenter} to the {@link FormDialogPresenterFactory}.</li>
     * <li>Sets the created {@link info.magnolia.ui.vaadin.form.FormView} as content of the created {@link DialogView}.</li>
     * </ul>
     *
     * @param item passed on to{@link FormDialogPresenter}
     * @param callback registers callback functions created by caller
     */
    @Override
    public DialogView start(final Item item, final ModalityLevel modalityLevel, final Callback callback) {
        this.callback = callback;
        dialogBuilder.buildFormDialog(dialogDefinition, view);
        this.formPresenter = formPresenterFactory.createFormPresenterByDefinition(dialogDefinition.getFormDefinition());

        // This is needed to acces properties from the parent. Currently only the i18basename.
        Dialog dialog = new Dialog(dialogDefinition);
        view.setFormView(formPresenter.start(item, dialog));

        // shell.openDialog(this);

        this.addDialogCloseHandler(new DialogCloseEvent.Handler() {
            @Override
            public void onClose(DialogCloseEvent event) {
                // appContext.getView().clearModal(FormDialogPresenterImpl.this.getView());
                shell.closeModal(FormDialogPresenterImpl.this.getView());
                event.getView().asVaadinComponent().removeDialogCloseHandler(this);
            }
        });

        View modalView = this.getView();
        shell.openModal(modalView, modalityLevel);

        return view;
    }

    private void initActions(final DialogDefinition dialogDefinition) {
        for (final DialogActionDefinition action : dialogDefinition.getActions()) {
            DialogPresenterUtil.addActionFromDefinition(this, action, dialogActionFactory);
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

}