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
package info.magnolia.ui.framework.overlay;

import info.magnolia.objectfactory.Classes;
import info.magnolia.ui.api.overlay.AlertCallback;
import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.overlay.NotificationCallback;
import info.magnolia.ui.api.overlay.OverlayCloser;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.icon.CompositeIcon;

import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Provides implementations for most OverlayLayer methods.
 */
public abstract class OverlayPresenter implements OverlayLayer {

    public static final int TIMEOUT_SECONDS_DEFAULT = 3;

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
    public void openAlert(MessageStyleType type, View viewToShow, String confirmButtonText, final AlertCallback cb) {
        final AlertView view = new AlertView(viewToShow.asVaadinComponent(), confirmButtonText, type, cb);

        final OverlayCloser overlayCloser = openOverlay(view, ModalityLevel.LIGHT);
        view.addCloseHandler(BaseAlertView.createCloseHandler(overlayCloser));
    }

    /**
     * Convenience method with string content for opening an alert.
     */
    @Override
    public void openAlert(MessageStyleType type, String title, String body, String confirmButtonText, AlertCallback cb) {
        openAlert(type, createConfirmationView(type, title, body), confirmButtonText, cb);
    }

    /**
     * Present modal confirmation dialog with light modality level. Allow any Vaadin content to be presented.
     */
    @Override
    public void openConfirmation(MessageStyleType type, View contentView, String confirmButtonText, String cancelButtonText,
            boolean cancelIsDefault, ConfirmationCallback callback) {
        final ConfirmationView view = new ConfirmationView(contentView.asVaadinComponent(), confirmButtonText, cancelButtonText, type, cancelIsDefault);

        final OverlayCloser overlayCloser = openOverlay(view, ModalityLevel.LIGHT);
        view.addConfirmationHandler(BaseAlertView.createConfirmationHandler(overlayCloser, callback));
        view.addCloseHandler(BaseAlertView.createCloseHandler(overlayCloser));

    }

    /**
     * Present modal confirmation dialog with light modality level. Allow only string content.
     */
    @Override
    public void openConfirmation(MessageStyleType type, final String title, final String body, String confirmButtonText, String cancelButtonText, boolean cancelIsDefault, ConfirmationCallback cb) {
        openConfirmation(type, createConfirmationView(type, title, body), confirmButtonText, cancelButtonText, cancelIsDefault, cb);
    }

    /**
     * Present notification indicator with no modality. Close after timeout expires.
     */
    @Override
    public void openNotification(final MessageStyleType type, final boolean doesTimeout, final View viewToShow) {
        final NotificationView notificationView = new NotificationView(type);
        final OverlayCloser closer = openOverlay(notificationView, ModalityLevel.NON_MODAL);
        notificationView.setContent(viewToShow.asVaadinComponent());
        notificationView.addCloseButtonListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent clickEvent) {
                closer.close();
            }
        });

        notificationView.addNotificationBodyClickListener(new LayoutEvents.LayoutClickListener() {
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
     * Convenience method for presenting notification indicator with string content.
     */
    @Override
    public void openNotification(final MessageStyleType type, final boolean doesTimeout, final String title) {
        openNotification(type, doesTimeout, new ViewAdapter(new Label(title, ContentMode.HTML)));
    }

    /**
     * Convenience method for presenting notification indicator with string content.
     */
    @Override
    public void openNotification(final MessageStyleType type, final boolean doesTimeout, final String title, final String linkText, final NotificationCallback cb) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.addComponent(new Label(title, ContentMode.HTML));

        Button button = new Button(linkText, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                cb.onLinkClicked();
            }
        });
        button.setStyleName(BaseTheme.BUTTON_LINK);

        layout.addComponent(button);
        openNotification(type, doesTimeout, new ViewAdapter(layout));
    }

    private View createConfirmationView(final MessageStyleType type, final String title, final String body) {
        Layout layout = new CssLayout();

        Label titleLabel = new Label(title, ContentMode.HTML);
        titleLabel.addStyleName("title");
        layout.addComponent(titleLabel);

        Label bodyLabel = new Label(body, ContentMode.HTML);
        bodyLabel.addStyleName("body");
        layout.addComponent(bodyLabel);

        CompositeIcon icon = (CompositeIcon) Classes.getClassFactory().newInstance(type.getIconClass());
        icon.setStyleName("dialog-icon");
        layout.addComponent(icon);
        return new ViewAdapter(layout);
    }

}