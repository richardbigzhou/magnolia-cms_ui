/**
 * This file Copyright (c) 2012-2013 Magnolia International
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
package info.magnolia.ui.vaadin.dialog;

import info.magnolia.ui.vaadin.view.MessageStyleType;
import info.magnolia.ui.vaadin.view.View;

import java.util.Timer;
import java.util.TimerTask;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Component for showing indication messages.
 */
public class NotificationIndicator implements View {

    /**
     * Listener for handling close button clicks.
     */
    public interface ConfirmationListener {
        void onClose();
    }

    private CssLayout layout;
    private ConfirmationListener listener;

    public NotificationIndicator() {
        layout = new CssLayout();
        layout.addStyleName("lightdialog");
        layout.addStyleName("modal-child");
        layout.addStyleName("dialog-panel");
        layout.addStyleName("notification-dialog");

        Button closeButton = new Button();
        closeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                listener.onClose();
            }
        });

        closeButton.addStyleName("notification-close-button");
        closeButton.addStyleName("icon-close");
        closeButton.addStyleName("m-closebutton");

        layout.addComponent(closeButton);
    }

    /**
     * Indicator will go away after defined timeout or if user clicks close button.
     * 
     * @param timeout_msec
     */
    public void setTimeout(int timeout_msec) {
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
                listener.onClose();
                timer.cancel();
            }

        }, timeout_msec);

        layout.addComponent(progress);
    }

    /**
     * Set notification content.
     * 
     * @param content
     */
    public void setContent(Component content) {
        layout.addComponent(content);
    }

    /**
     * Set the style of this notification.
     * 
     * @param type
     */
    public void setMessageType(MessageStyleType type) {
        layout.addStyleName(type.Name());
    }

    /**
     * Set listener for close button clicks.
     * 
     * @param listener
     */
    public void setConfirmationListener(ConfirmationListener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }
}