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
package info.magnolia.ui.vaadin.overlay;

import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog.ConfirmationEvent;
import info.magnolia.ui.vaadin.dialog.LightDialog;
import info.magnolia.ui.vaadin.dialog.Notification;
import info.magnolia.ui.vaadin.editorlike.DialogActionListener;
import info.magnolia.ui.vaadin.icon.CompositeIcon;
import info.magnolia.ui.vaadin.overlay.Overlay.ModalityLevel;
import info.magnolia.ui.vaadin.view.View;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Provides default implementations for many OverlayLayer methods.
 */
public abstract class BaseOverlayLayer implements OverlayLayer {

    private static final String ACTION_CONFIRM = "confirm";

    /**
     * Convenience method to open an overlay with the default strong modality level.
     */
    @Override
    public OverlayCloser openOverlay(View view) {
        return openOverlay(view, ModalityLevel.STRONG);
    }

    /**
     * Open alert dialog with light modality level. Close dialog on confirm.
     */
    @Override
    public OverlayCloser openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, final AlertCallback cb) {
        BaseDialog dialog = createAlertDialog(viewToShow, confirmButtonText, type.getCssClass());
        dialog.showCloseButton();

        final OverlayCloser overlayCloser = openOverlay(dialog, ModalityLevel.LIGHT);
        dialog.addDialogCloseHandler(createCloseHandler(overlayCloser));
        dialog.addActionCallback(ACTION_CONFIRM, new DialogActionListener() {

            @Override
            public void onActionExecuted(String actionName) {
                overlayCloser.close();
                cb.onOk();
            }
        });

        return overlayCloser;
    }

    /**
     * Convenience method with string content. for opening an alert.
     */
    @Override
    public OverlayCloser openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        return openAlert(type, createConfirmationView(type, title, body), confirmButtonText, cb);
    }

    private ConfirmationDialog.ConfirmationEvent.Handler createHandler(final OverlayCloser overlayCloser, final ConfirmationCallback callback) {
        return new ConfirmationDialog.ConfirmationEvent.Handler() {

            @Override
            public void onConfirmation(ConfirmationEvent event) {

                if (event.isConfirmed()) {
                    callback.onSuccess("");
                } else {
                    callback.onCancel();
                }

                overlayCloser.close();
            }
        };
    }

    private ConfirmationDialog createConfirmationDialog(View contentView, String confirmButtonText, String cancelButtonText, String stylename, boolean cancelIsDefault) {
        ConfirmationDialog dialog = new ConfirmationDialog(contentView, cancelIsDefault);
        // dialog.addStyleName("lightdialog");
        dialog.addStyleName(stylename);
        dialog.setConfirmActionLabel(confirmButtonText);
        if (cancelButtonText != null) {
            dialog.setRejectActionLabel(cancelButtonText);
        }

        return dialog;
    }

    private BaseDialog createAlertDialog(View contentView, String confirmButtonText, String stylename) {
        BaseDialog dialog = new LightDialog();
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
                icon.setStyleName("dialog-icon");
                layout.addComponent(icon);

                return layout;
            }
        };
    }

    private BaseDialog.DialogCloseEvent.Handler createCloseHandler(final OverlayCloser overlayCloser) {
        return new BaseDialog.DialogCloseEvent.Handler() {
            @Override
            public void onClose(BaseDialog.DialogCloseEvent event) {
                overlayCloser.close();
                event.getView().asVaadinComponent().removeDialogCloseHandler(this);
            }
        };
    }

    /**
     * Present modal confirmation dialog with light modality level. Allow any Vaadin content to be presented.
     */
    @Override
    public OverlayCloser openConfirmation(MessageStyleType type, View contentView, String confirmButtonText, String cancelButtonText,
            boolean cancelIsDefault, final ConfirmationCallback callback) {
        ConfirmationDialog dialog = createConfirmationDialog(contentView, confirmButtonText, cancelButtonText, type.getCssClass(), cancelIsDefault);
        dialog.showCloseButton();

        final OverlayCloser overlayCloser = openOverlay(dialog, ModalityLevel.LIGHT);
        dialog.addConfirmationHandler(createHandler(overlayCloser, callback));
        dialog.addDialogCloseHandler(createCloseHandler(overlayCloser));

        return overlayCloser;
    }

    /**
     * Present modal confirmation dialog with light modality level. Allow only string content.
     */
    @Override
    public OverlayCloser openConfirmation(MessageStyleType type, final String title, final String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        return openConfirmation(type, createConfirmationView(type, title, body), confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }


    /**
     * Present notification indicator with no modality. Close after timeout expires.
     */
    @Override
    public OverlayCloser openNotification(final MessageStyleType type, final int timeout_msec, final View viewToShow) {
        return new OverlayCloser() {
            private OverlayCloser compositeCloser;

            {
                Notification dialog = new Notification(type);
                dialog.setContent(viewToShow.asVaadinComponent());
                dialog.setTimeout(timeout_msec);
                dialog.setConfirmationListener(new Notification.ConfirmationListener() {

                    @Override
                    public void onClose() {
                        compositeCloser.close();
                    }
                });

                compositeCloser = openOverlay(dialog, ModalityLevel.NON_MODAL);
            }

            @Override
            public void close() {
                compositeCloser.close();
            }
        };
    }

    /**
     * Convenience method for presenting notification indicator with string content.
     */
    @Override
    public OverlayCloser openNotification(final MessageStyleType type, final int timeout_msec, final String title) {
        View view = new View() {
            @Override
            public Component asVaadinComponent() {
                return new Label(title);
            }
        };
        return openNotification(type, timeout_msec, view);

    }

    /**
     * Convenience method for presenting notification indicator with string content.
     */
    @Override
    public OverlayCloser openNotification(final MessageStyleType type, final int timeout_msec, final String title, final String linkText, final NotificationCallback cb ) {
        View view = new View() {
            @Override
            public Component asVaadinComponent() {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setSpacing(true);
                layout.addComponent(new Label(title));

                String linkTextBrackets = "[" + linkText + "]";
                Button button = new Button(linkTextBrackets, new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        cb.onLinkClicked();
                    }
                });
                button.setStyleName(BaseTheme.BUTTON_LINK);

                layout.addComponent(button);
                return layout;
            }
        };
        return openNotification(type, timeout_msec, view);

    }

}
