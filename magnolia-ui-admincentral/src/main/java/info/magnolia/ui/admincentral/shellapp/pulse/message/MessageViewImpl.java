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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.ui.vaadin.actionbar.Actionbar;
import info.magnolia.ui.vaadin.view.View;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;

/**
 * View implementation of {@link MessageView}.
 */
public class MessageViewImpl  extends HorizontalLayout implements MessageView {

    private CssLayout messageContainer = new CssLayout();
    private CssLayout actionbarContainer = new CssLayout();
    private MessageView.Listener listener;
    private View messageView;

    public MessageViewImpl() {
        setupLayout();
    }

    private void setupLayout() {
        Button back = new NativeButton("Back to messages");
        back.setStyleName("back-button");
        back.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                listener.onNavigateToList();
            }
        });
        messageContainer.addComponent(back);

        addComponent(messageContainer);
        addComponent(actionbarContainer);


    }

    @Override
    public void setMessageView(View view) {
        if(messageView != null) {
            messageContainer.replaceComponent(messageView.asVaadinComponent(), view.asVaadinComponent());
        }
        else {messageContainer.addComponent(view.asVaadinComponent());}
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

    public void setListener(MessageView.Listener listener) {
        this.listener = listener;
    }
}
