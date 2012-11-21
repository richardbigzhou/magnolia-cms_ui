/**
 * This file Copyright (c) 2010-2012 Magnolia International
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

import com.vaadin.data.Item;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.admincentral.dialog.builder.DialogBuilder;
import info.magnolia.ui.admincentral.form.FormPresenter;
import info.magnolia.ui.admincentral.form.FormPresenterFactory;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.dialog.action.DialogActionDefinition;
import info.magnolia.ui.model.dialog.definition.DialogDefinition;
import info.magnolia.ui.vaadin.dialog.DialogView;
import info.magnolia.ui.vaadin.dialog.NewFormDialogView;

/**
 * Dialog Presenter and Listener implementation.
 */
public class FormDialogPresenterImpl extends BaseDialogPresenter implements NewFormDialogPresenter {

    private final DialogBuilder dialogBuilder;

    private FormPresenterFactory formPresenterFactory;

    private final DialogDefinition dialogDefinition;

    private final MagnoliaShell shell;

    private final NewFormDialogView view;

    private Callback callback;

    private final  DialogActionFactory dialogActionFactory;
    private FormPresenter formPresenter;

    public FormDialogPresenterImpl(final NewFormDialogView view, final DialogBuilder dialogBuilder, final FormPresenterFactory formPresenterFactory,
                                   final DialogDefinition dialogDefinition, final Shell shell, EventBus eventBus, final DialogActionFactory actionFactory) {
        super(view, eventBus);
        this.view = view;
        this.dialogBuilder = dialogBuilder;
        this.formPresenterFactory = formPresenterFactory;
        this.dialogDefinition = dialogDefinition;
        this.shell = (MagnoliaShell)shell;
        this.dialogActionFactory = actionFactory;
        initActions(dialogDefinition);
    }

    @Override
    public DialogView start(final Item item, Callback callback) {
        this.callback = callback;
        dialogBuilder.buildFormDialog(dialogDefinition, view);

        this.formPresenter = formPresenterFactory.createFormPresenterByDefinition(dialogDefinition.getFormDefinition());

        ((NewFormDialogView)view).setFormView(formPresenter.start(item, new FormPresenter.Callback() {

            @Override
            public void onCancel() {
                 FormDialogPresenterImpl.this.closeDialog();
            }

            @Override
            public void onSuccess(String actionName) {
                //eventBus.fireEvent(new ContentChangedEvent(item.getWorkspace(), item.getItemId()));
                FormDialogPresenterImpl.this.closeDialog();
            }
        }));


        shell.openDialog(this);
        return view;
    }

    private void initActions(final DialogDefinition dialogDefinition) {
        for (final DialogActionDefinition action : dialogDefinition.getActions()) {
            DialogPresenterUtil.addActionFromDefinition(this, action, dialogActionFactory);
        }
    }

    @Override
    public NewFormDialogView getView() {
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