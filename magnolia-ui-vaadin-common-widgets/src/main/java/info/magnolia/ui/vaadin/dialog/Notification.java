/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.objectfactory.Classes;
import info.magnolia.ui.api.overlay.MessageStyleType;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.icon.CompositeIcon;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * Component for showing notification messages.
 */
public class Notification implements View {

    private CssLayout layout;

    private Button closeButton = new Button();

    public Notification(final MessageStyleType type) {
        layout = new CssLayout();
        layout.addStyleName("light-dialog-panel");
        layout.addStyleName("notification-dialog");
        layout.addStyleName(type.getCssClass());

        CompositeIcon icon = (CompositeIcon) Classes.getClassFactory().newInstance(type.getIconClass());
        icon.setStyleName("dialog-icon");
        layout.addComponent(icon);

        layout.addLayoutClickListener(new LayoutClickListener() {
            @Override
            public void layoutClick(LayoutClickEvent event) {
                layout.addStyleName("notification-dialog-selected");
            }
        });

        closeButton.addStyleName("icon-close");
        closeButton.addStyleName("m-closebutton");

        layout.addComponent(closeButton);
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

    public void addCloseButtonListener(Button.ClickListener listener) {
        closeButton.addClickListener(listener);
    }

    public void addNotificationBodyClickListener(LayoutClickListener listener) {
        layout.addLayoutClickListener(listener);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }
}
