/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.vaadin.view;

import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog.ConfirmationEvent;
import info.magnolia.ui.vaadin.dialog.Modal.ModalityLevel;
import info.magnolia.ui.vaadin.icon.CompositeIcon;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

/**
 * Implementers can open modal views over their display area.
 */
public abstract class BaseModalLayer implements ModalLayer {

    /**
     * Convenience method to open a modal with the default strong modality level.
     */
    @Override
    public ModalCloser openModal(View view) {
        return openModal(view, ModalityLevel.STRONG);
    }

    /**
     * Open a Modal on top of the ModalLayer implementer.
     *
     * @param view View of the component to be displayed modally.
     */
    // public ModalCloser openModal(View view, ModalityLevel modalityLevel);

    @Override
    public ModalCloser openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, AlertCallback cb) {
        return null;
    }

    @Override
    public ModalCloser openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        return null;
    }

    private ConfirmationDialog.ConfirmationEvent.Handler createHandler(final ModalCloser modalCloser, final ConfirmationCallback callback) {
        return new ConfirmationDialog.ConfirmationEvent.Handler() {

            @Override
            public void onConfirmation(ConfirmationEvent event) {

                if (event.isConfirmed()) {
                    callback.onSuccess("");
                } else {
                    callback.onCancel();
                }

                modalCloser.close();
            }
        };
    }

    private ConfirmationDialog.ConfirmationEvent.Handler createHandler(final ModalCloser modalCloser, final NotificationCallback callback) {
        return new ConfirmationDialog.ConfirmationEvent.Handler() {

            @Override
            public void onConfirmation(ConfirmationEvent event) {
                callback.onOk();
                modalCloser.close();
            }
        };
    }

    private ConfirmationDialog.ConfirmationEvent.Handler createHandler(final ModalCloser modalCloser, final AlertCallback callback) {
        return new ConfirmationDialog.ConfirmationEvent.Handler() {

            @Override
            public void onConfirmation(ConfirmationEvent event) {
                callback.onOk();
                modalCloser.close();
            }
        };
    }

    private ConfirmationDialog createConfirmationDialog(View contentView, String confirmButtonText, String cancelButtonText, String stylename, boolean cancelIsDefault) {
        ConfirmationDialog dialog = new ConfirmationDialog(contentView, cancelIsDefault);
        dialog.addStyleName("lightdialog");
        dialog.addStyleName(stylename);
        dialog.setConfirmActionLabel(confirmButtonText);
        dialog.setRejectActionLabel(cancelButtonText);

        return dialog;
    }

    private View createConfirmationView(final MessageStyleType type, final String title, final String body) {
        return new View() {
            @Override
            public Component asVaadinComponent() {
                Layout layout = new CssLayout();

                Label titleLabel = new Label(title);
                titleLabel.addStyleName("title");
                layout.addComponent(titleLabel);

                Label bodyLabel = new Label(body);
                bodyLabel.addStyleName("body");
                layout.addComponent(bodyLabel);

                CompositeIcon icon = type.Icon();
                layout.addComponent(icon);

                return layout;
            }
        };
    }

    @Override
    public ModalCloser openConfirmation(MessageStyleType type, View contentView, String confirmButtonText, String cancelButtonText,
            boolean cancelIsDefault, final ConfirmationCallback callback) {
        ConfirmationDialog dialog = createConfirmationDialog(contentView, confirmButtonText, cancelButtonText, type.Name(), cancelIsDefault);
        dialog.showCloseButton();

        final ModalCloser modalCloser = openModal(dialog, ModalityLevel.LIGHT);
        dialog.addConfirmationHandler(createHandler(modalCloser, callback));

        dialog.addDialogCloseHandler(new BaseDialog.DialogCloseEvent.Handler() {
            @Override
            public void onClose(BaseDialog.DialogCloseEvent event) {
                modalCloser.close();
                event.getView().asVaadinComponent().removeDialogCloseHandler(this);
            }
        });

        return modalCloser;
    }

    @Override
    public ModalCloser openConfirmation(MessageStyleType type, final String title, final String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        return openConfirmation(type, createConfirmationView(type, title, body), confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }


    @Override
    public ModalCloser openNotification(View viewToShow, String confirmButtonText, NotificationCallback cb) {
        return null;
    }

    @Override
    public ModalCloser openNotification(View parent, View viewToShow, int timeout_msec) {
        return null;
    }

}
