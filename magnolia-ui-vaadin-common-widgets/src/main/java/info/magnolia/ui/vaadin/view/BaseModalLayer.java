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
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.icon.CompositeIcon;

import java.util.Timer;
import java.util.TimerTask;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ProgressIndicator;

/**
 * Implementers can open modal views over their display area.
 */
public abstract class BaseModalLayer implements ModalLayer {

    private static final String ACTION_CONFIRM = "confirm";

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
    public ModalCloser openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, final AlertCallback cb) {
        BaseDialog dialog = createAlertDialog(viewToShow, confirmButtonText, type.Name());
        dialog.showCloseButton();

        final ModalCloser modalCloser = openModal(dialog, ModalityLevel.LIGHT);
        dialog.addDialogCloseHandler(createCloseHandler(modalCloser));
        dialog.addActionCallback(ACTION_CONFIRM, new DialogActionListener() {

            @Override
            public void onActionExecuted(String actionName) {
                modalCloser.close();
                cb.onOk();
            }
        });

        return modalCloser;
    }

    @Override
    public ModalCloser openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        return openAlert(type, createConfirmationView(type, title, body), confirmButtonText, cb);
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
        if (cancelButtonText != null) {
            dialog.setRejectActionLabel(cancelButtonText);
        }

        return dialog;
    }

    private BaseDialog createAlertDialog(View contentView, String confirmButtonText, String stylename) {
        BaseDialog dialog = new BaseDialog();
        dialog.addStyleName("lightdialog");
        dialog.addStyleName(stylename);
        dialog.setContent(contentView.asVaadinComponent());
        dialog.addAction(ACTION_CONFIRM, confirmButtonText);
        dialog.setDefaultAction(ACTION_CONFIRM);
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

    private BaseDialog.DialogCloseEvent.Handler createCloseHandler(final ModalCloser modalCloser) {
        return new BaseDialog.DialogCloseEvent.Handler() {
            @Override
            public void onClose(BaseDialog.DialogCloseEvent event) {
                modalCloser.close();
                event.getView().asVaadinComponent().removeDialogCloseHandler(this);
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
        dialog.addDialogCloseHandler(createCloseHandler(modalCloser));

        return modalCloser;
    }

    @Override
    public ModalCloser openConfirmation(MessageStyleType type, final String title, final String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        return openConfirmation(type, createConfirmationView(type, title, body), confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }

    private CssLayout createAlertComponent(Component content) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("modal-child");
        layout.addStyleName("dialog-panel");
        layout.addStyleName("lightdialog");
        layout.addComponent(content);
        return layout;
    }

    @Override
    public ModalCloser openNotification(final View viewToShow, String confirmButtonText, final NotificationCallback cb) {
        return new ModalCloser() {
            private View view;
            private ModalCloser compositeCloser;
            private CssLayout layout;

            {
                view = new View() {

                    @Override
                    public Component asVaadinComponent() {
                        if (layout == null) {
                            layout = createAlertComponent(viewToShow.asVaadinComponent());
                            layout.addLayoutClickListener(new LayoutClickListener() {

                                @Override
                                public void layoutClick(LayoutClickEvent event) {
                                    cb.onOk();
                                    compositeCloser.close();
                                }
                            });

                        }

                        return layout;
                    }

                };

                compositeCloser = openModal(view, ModalityLevel.LIGHT);
            }

            @Override
            public void close() {
                compositeCloser.close();
            }
        };
    }

    @Override
    public ModalCloser openNotification(View parent, final View viewToShow, final int timeout_msec) {
        return new ModalCloser() {
            private View view;
            private ModalCloser compositeCloser;
            private CssLayout layout;

            {
                view = new View() {

                    @Override
                    public Component asVaadinComponent() {
                        if (layout == null) {
                            layout = createAlertComponent(viewToShow.asVaadinComponent());
                            /*
                             * Using the progressbar here like this is a hack.
                             * When Vaadin 7.1 with built-in push is out
                             * this code can be refactored to use it.
                             * Second alternative is to use Refresher add-on,
                             * but as a temp solution the stock progressbar is simpler.
                             */
                            ProgressIndicator progress = new ProgressIndicator();
                            progress.setPollingInterval(timeout_msec);
                            progress.setIndeterminate(true);
                            progress.setStyleName("alert-progressbar");
                            final Timer timer = new Timer();
                            timer.schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    compositeCloser.close();
                                    timer.cancel();
                                }

                            }, timeout_msec);

                            layout.addComponent(progress);
                        }

                        return layout;
                    }

                };

                compositeCloser = openModal(view, ModalityLevel.LIGHT);
            }

            @Override
            public void close() {
                compositeCloser.close();
            }
        };
    }

}
