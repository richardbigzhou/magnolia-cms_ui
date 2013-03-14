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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.event.EventBus;
import info.magnolia.ui.dialog.BaseDialogPresenter;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.editorlike.EditorLikeActionListener;
import info.magnolia.ui.vaadin.view.View;
import info.magnolia.ui.workbench.event.ItemSelectedEvent;

import com.vaadin.data.Item;

/**
 * Factory for creating workbench choose dialog presenters.
 */
public class WorkbenchChooseDialogPresenter extends BaseDialogPresenter implements ChooseDialogPresenter {

    private Item currentValue = null;

    private Listener listener;

    private final ChooseDialogView chooseDialogView;

    public WorkbenchChooseDialogPresenter(ChooseDialogView view, EventBus workbenchEventBus) {
        super(view, workbenchEventBus);
        this.chooseDialogView = view;
        workbenchEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {
            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                currentValue = event.getItem();
            }
        });

        addActionCallback(WorkbenchChooseDialogView.CANCEL_ACTION_NAME, new EditorLikeActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                closeDialog();
            }
        });

        addActionCallback(WorkbenchChooseDialogView.CHOOSE_ACTION_NAME, new EditorLikeActionListener() {
            @Override
            public void onActionExecuted(final String actionName) {
                closeDialog();
            }
        });

        addDialogCloseHandler(new BaseDialog.DialogCloseEvent.Handler() {

            @Override
            public void onClose(BaseDialog.DialogCloseEvent event) {
                event.getView().asVaadinComponent().removeDialogCloseHandler(this);
                listener.onClose();
            }
        });
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;

    }

    @Override
    public View start() {
        return chooseDialogView;
    }

    @Override
    public Item getValue() {
        return currentValue;
    }

}
