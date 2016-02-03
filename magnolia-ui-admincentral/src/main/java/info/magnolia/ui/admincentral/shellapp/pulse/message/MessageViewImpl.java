/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.icon.Icon;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * View implementation of {@link MessageView}.
 */
public final class MessageViewImpl extends HorizontalLayout implements MessageView {

    private CssLayout messageContainer = new CssLayout();
    private CssLayout actionbarContainer = new CssLayout();
    private Label title = new Label();
    private MessageView.Listener listener;
    private View messageView;


    public MessageViewImpl() {
        construct();
    }

    private void construct() {
        setSizeFull();
        addStyleName("message-detail");
        messageContainer.setSizeFull();
        title.setStyleName("message-title");

        actionbarContainer.setStyleName("message-actionbar-container");
        actionbarContainer.setHeight("100%");
        messageContainer.addComponent(new SimpleButton());
        title.setSizeUndefined();
        messageContainer.addComponent(title);

        addComponent(messageContainer);
        addComponent(actionbarContainer);
        setExpandRatio(messageContainer, 1);

    }

    @Override
    public void setTitle(String subject) {
        title.setValue(subject);
    }

    @Override
    public void setMessageView(View view) {
        if(messageView != null) {
            messageContainer.replaceComponent(messageView.asVaadinComponent(), view.asVaadinComponent());
        } else {
            messageContainer.addComponent(view.asVaadinComponent());
        }

        this.messageView = view;
    }

    @Override
    public void setActionbarView(View view) {
        Actionbar actionbarComponent = (Actionbar) view.asVaadinComponent();

        if(actionbarContainer.getComponentCount() > 0) {
            actionbarContainer.removeAllComponents();
        }
        actionbarContainer.addComponent(actionbarComponent);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setListener(MessageView.Listener listener) {
        this.listener = listener;
    }

    private class SimpleButton extends CssLayout{

        private final static String BACK_BUTTON_LABEL ="Back to all messages";

        private SimpleButton() {
            setStyleName("back-button");

            Icon icon = new Icon("arrow2_w", 16);
            Label label = new Label(BACK_BUTTON_LABEL);
            label.setSizeUndefined();
            addComponent(icon);
            addComponent(label);

            addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                @Override
                public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                    listener.onNavigateToList();
                }
            });

        }
    }
}
