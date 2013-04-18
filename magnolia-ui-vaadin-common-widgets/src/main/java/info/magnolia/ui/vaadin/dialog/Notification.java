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

import info.magnolia.ui.vaadin.icon.CompositeIcon;
import info.magnolia.ui.vaadin.overlay.MessageStyleType;
import info.magnolia.ui.vaadin.view.View;

import java.util.Timer;
import java.util.TimerTask;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.ProgressIndicator;

/**
 * Component for showing notification messages.
 */
public class Notification implements View {

    public static final int TIMEOUT_SECONDS_DEFAULT = 3;
    /**
     * Listener for handling close button clicks.
     */
    public interface ConfirmationListener {
        void onClose();
    }

    private CssLayout layout;
    private ConfirmationListener listener;
    Timer timer;

    public Notification(final MessageStyleType type) {
        timer = new Timer();
        layout = new CssLayout();
        layout.addStyleName("light-dialog-panel");
        layout.addStyleName("notification-dialog");

        // Set the type
        layout.addStyleName(type.getCssClass());

        CompositeIcon icon = type.makeIcon();
        icon.setStyleName("dialog-icon");
        layout.addComponent(icon);

        Button closeButton = new Button();
        closeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                listener.onClose();
            }
        });

        layout.addLayoutClickListener(new LayoutClickListener() {

            @Override
            public void layoutClick(LayoutClickEvent event) {
                cancelTimeout();
                layout.addStyleName("notification-dialog-selected");
            }
        });

        closeButton.addStyleName("icon-close");
        closeButton.addStyleName("m-closebutton");

        layout.addComponent(closeButton);
    }

    /**
     * Cancel any pending timeout.
     */
    public void cancelTimeout() {
        timer.cancel();
    }

    /**
     * Indicator will go away after defined timeout or if user clicks close button.
     * 
     * @param timeoutSeconds if set to -1 then Timeout is not added.
     */
    public void setTimeout(int timeoutSeconds) {
        if (timeoutSeconds < 0) {
            return;
        }
        int timeoutMsec = timeoutSeconds * 1000;

        /*
         * Using the progressbar here like this is a hack.
         * When Vaadin 7.1 with built-in push is out
         * this code can be refactored to use it.
         * Second alternative is to use Refresher add-on,
         * but as a temp solution the stock progressbar is simpler.
         * 
         * See ticket MGNLUI-1112
         */
        ProgressIndicator progress = new ProgressIndicator();
        progress.setPollingInterval(timeoutMsec);
        progress.setIndeterminate(true);
        progress.setStyleName("alert-progressbar");
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                listener.onClose();
                timer.cancel();
            }

        }, timeoutMsec);

        layout.addComponent(progress);
    }

    /**
     * Set notification content.
     *
     * @param content
     */
    public void setContent(Component content) {
        layout.addComponent(content);
        content.addStyleName("dialog-content");
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