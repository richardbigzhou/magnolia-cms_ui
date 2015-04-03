/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.framework.overlay;

import info.magnolia.objectfactory.Classes;
import info.magnolia.ui.api.overlay.AlertCallback;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.overlay.NotificationCallback;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog;
import info.magnolia.ui.vaadin.dialog.ConfirmationDialog.ConfirmationEvent;
import info.magnolia.ui.vaadin.dialog.LightDialog;
import info.magnolia.ui.vaadin.dialog.Notification;
import info.magnolia.ui.vaadin.icon.CompositeIcon;

import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Provides implementations for most OverlayLayer methods.
 */
public abstract class OverlayPresenter implements OverlayLayer {

    public static final int TIMEOUT_SECONDS_DEFAULT = 3;

    /**
     * Opens an overlay with default strong modality level.
     */
    @Override
    public OverlayCloser openOverlay(View view) {
        return openOverlay(view, ModalityLevel.STRONG);
    }

    @Override
    public void openAlert(MessageStyleType type, View body, String okButton, AlertCallback callback) {
        openAlert(type, null, body, okButton, callback);
    }

    @Override
    public void openAlert(MessageStyleType type, String title, String body, String okButton, AlertCallback callback) {
        openAlert(type, title, new ViewAdapter(new Label(body, ContentMode.HTML)), okButton, callback);
    }

    @Override
    public void openAlert(MessageStyleType type, String title, View body, String okButton, final AlertCallback callback) {

        final BaseDialog dialog = new LightDialog();
        dialog.addStyleName(type.getCssClass());
        dialog.addStyleName("alert");

        dialog.setCaption(title);
        CompositeIcon icon = (CompositeIcon) Classes.getClassFactory().newInstance(type.getIconClass());
        icon.setStyleName("dialog-icon");
        dialog.setHeaderToolbar(icon);
        dialog.showCloseButton();

        dialog.setContent(body.asVaadinComponent());

        Panel shortcutPanel = new Panel();
        shortcutPanel.setStyleName("shortcut-panel");
        shortcutPanel.setHeight(100, Unit.PERCENTAGE);
        shortcutPanel.setWidth(100, Unit.PERCENTAGE);
        shortcutPanel.setContent(dialog);

        final OverlayCloser overlayCloser = openOverlay(new ViewAdapter(shortcutPanel), ModalityLevel.LIGHT);
        final ShortcutListener escapeShortcut = new ShortcutListener("Escape shortcut", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                callback.onOk();
                dialog.closeSelf();
            }
        };
        shortcutPanel.addShortcutListener(escapeShortcut);
        addOkHandler(dialog, okButton, overlayCloser, callback);
        dialog.addDialogCloseHandler(createCloseHandler(overlayCloser));
    }

    private void addOkHandler(BaseDialog dialog, String okButtonText, final OverlayCloser overlayCloser, final AlertCallback cb) {
        CssLayout footer = new CssLayout();
        footer.setWidth(100, Unit.PERCENTAGE);
        footer.addStyleName("v-align-right");
        Button okButton = new Button(okButtonText, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                cb.onOk();
                overlayCloser.close();
            }
        });
        okButton.focus();
        footer.addComponent(okButton);
        dialog.setFooterToolbar(footer);
    }

    @Override
    public void openConfirmation(MessageStyleType type, View body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback) {
        openConfirmation(type, null, body, confirmButton, cancelButton, cancelIsDefault, callback);
    }

    @Override
    public void openConfirmation(MessageStyleType type, String title, String body, String confirmButton, String cancelButton, boolean cancelIsDefault, ConfirmationCallback callback) {
        openConfirmation(type, title, new ViewAdapter(new Label(body, ContentMode.HTML)), confirmButton, cancelButton, cancelIsDefault, callback);
    }

    @Override
    public void openConfirmation(MessageStyleType type, String title, View body, String confirmButton, String cancelButton, boolean cancelIsDefault, final ConfirmationCallback callback) {
        final ConfirmationDialog dialog = new ConfirmationDialog(body.asVaadinComponent(), confirmButton, cancelButton, cancelIsDefault);
        dialog.addStyleName(type.getCssClass());
        dialog.addStyleName("confirmation");

        dialog.setCaption(title);
        CompositeIcon icon = (CompositeIcon) Classes.getClassFactory().newInstance(type.getIconClass());
        icon.setStyleName("dialog-icon");
        dialog.setHeaderToolbar(icon);
        dialog.showCloseButton();

        dialog.setContent(body.asVaadinComponent());

        Panel shortcutPanel = new Panel();
        shortcutPanel.setStyleName("shortcut-panel");
        shortcutPanel.setHeight(100, Unit.PERCENTAGE);
        shortcutPanel.setWidth(100, Unit.PERCENTAGE);
        shortcutPanel.setContent(dialog);

        final OverlayCloser overlayCloser = openOverlay(new ViewAdapter(shortcutPanel), ModalityLevel.LIGHT);

        final ShortcutListener escapeShortcut = new ShortcutListener("Escape shortcut", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                callback.onCancel();
                dialog.closeSelf();
            }
        };
        shortcutPanel.addShortcutListener(escapeShortcut);
        dialog.addConfirmationHandler(
                new ConfirmationDialog.ConfirmationEvent.Handler() {
                    @Override
                    public void onConfirmation(ConfirmationEvent event) {
                        if (event.isConfirmed()) {
                            callback.onSuccess();
                        } else {
                            callback.onCancel();
                        }
                        overlayCloser.close();
                    }
                });
        dialog.addDialogCloseHandler(createCloseHandler(overlayCloser));
    }

    private BaseDialog.DialogCloseEvent.Handler createCloseHandler(final OverlayCloser overlayCloser) {
        return new BaseDialog.DialogCloseEvent.Handler() {
            @Override
            public void onClose(BaseDialog.DialogCloseEvent event) {
                overlayCloser.close();
            }
        };
    }

    /**
     * Opens a notification of given {@link MessageStyleType type}, with given body; it can close automatically after a timeout.
     */
    @Override
    public void openNotification(final MessageStyleType type, boolean doesTimeout, View viewToShow) {
        final Notification notification = new Notification(type);
        notification.setContent(viewToShow.asVaadinComponent());

        Panel shortcutPanel = new Panel();
        shortcutPanel.setStyleName("shortcut-panel");
        shortcutPanel.setWidth(null);
        shortcutPanel.setContent(notification.asVaadinComponent());
        final OverlayCloser closer = openOverlay(new ViewAdapter(shortcutPanel), ModalityLevel.NON_MODAL);

        final ShortcutListener escapeShortcut = new ShortcutListener("Escape shortcut", ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                closer.close();
            }
        };
        shortcutPanel.addShortcutListener(escapeShortcut);

        notification.addCloseButtonListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent clickEvent) {
                closer.close();
            }
        });

        notification.addNotificationBodyClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent layoutClickEvent) {
                closer.setCloseTimeout(-1);
            }
        });

        if (doesTimeout) {
            closer.setCloseTimeout(TIMEOUT_SECONDS_DEFAULT);
        }

    }

    /**
     * Opens a notification of given {@link MessageStyleType type}, with given body text; it can close automatically after a timeout.
     */
    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, final String title) {
        openNotification(type, doesTimeout, new ViewAdapter(new Label(title, ContentMode.HTML)));

    }

    /**
     * Convenience method for presenting notification indicator with string content.
     */
    @Override
    public void openNotification(MessageStyleType type, boolean doesTimeout, String title, String linkText, final NotificationCallback callback) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponent(new Label(title, ContentMode.HTML));

        Button button = new Button(linkText, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                callback.onLinkClicked();
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);

        layout.addComponent(button);
        openNotification(type, doesTimeout, new ViewAdapter(layout));
    }

}
