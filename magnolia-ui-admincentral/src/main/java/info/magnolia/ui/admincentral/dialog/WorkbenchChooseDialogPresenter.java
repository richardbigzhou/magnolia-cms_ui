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

import info.magnolia.ui.admincentral.dialog.action.DialogActionFactory;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.event.EventBus;
import info.magnolia.ui.vaadin.dialog.DialogView.DialogActionListener;

import com.vaadin.data.Item;

/**
 * WorkbenchChooseDialogPresenter.
 */
public class WorkbenchChooseDialogPresenter extends BaseDialogPresenter implements ChooseDialogPresenter<Item> {

    private Item currentValue = null;

    private final ChooseDialogView chooseDialogView;

    public WorkbenchChooseDialogPresenter(DialogActionFactory actionFactory, ChooseDialogView view, EventBus workbenchEventBus) {
        super(view, workbenchEventBus);
        this.chooseDialogView = view;
        workbenchEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {
            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                currentValue = event.getItem();
            }
        });

        addActionCallback(WorkbenchValueChooseDialog.CANCEL_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                closeDialog();
            }
        });

        addActionCallback(WorkbenchValueChooseDialog.CHOOSE_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                closeDialog();
            }
        });

    }

    @Override
    public ChooseDialogView getView() {
        return chooseDialogView;
    }

    @Override
    public void addValueChosenListener(final ValueChosenListener<Item> listener) {
        addActionCallback(WorkbenchValueChooseDialog.CHOOSE_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.onValueChosen(currentValue);
            }
        });

        addActionCallback(WorkbenchValueChooseDialog.CANCEL_ACTION_NAME, new DialogActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                listener.selectionCanceled();
            }
        });
    }

    @Override
    public void removeValueChosenListener(ValueChosenListener<Item> listener) {
        // FIXME implement or remove forever!
    }

    @Override
    public Item getValue() {
        return currentValue;
    }

}
