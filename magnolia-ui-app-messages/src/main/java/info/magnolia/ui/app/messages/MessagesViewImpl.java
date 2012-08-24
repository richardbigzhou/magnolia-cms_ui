/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.app.messages;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import info.magnolia.ui.framework.message.MessageType;

/**
 * View implementation for the Messages app.
 */
@SuppressWarnings("serial")
public class MessagesViewImpl implements MessagesView {

    private Listener listener;
    private final Component component;

    public MessagesViewImpl() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);

        final TextField subjectField = new TextField("Subject");
        layout.addComponent(subjectField);

        final TextArea messageField = new TextArea("Message");
        layout.addComponent(messageField);

        HorizontalLayout middle = new HorizontalLayout();
        middle.setSpacing(true);

        final OptionGroup types = new OptionGroup("Type");
        types.setNullSelectionAllowed(false);
        types.addItem("Info");
        types.addItem("Warning");
        types.addItem("Error");
        types.setValue("Info");
        middle.addComponent(types);

        final OptionGroup scopes = new OptionGroup("Scope");
        scopes.setNullSelectionAllowed(false);
        scopes.addItem("User");
        scopes.setItemCaption("User", "Specific user");
        scopes.addItem("Local");
        scopes.setItemCaption("Local", "Current user");
        scopes.addItem("Global");
        scopes.setItemCaption("Global", "All users");
        scopes.setValue("Local");
        middle.addComponent(scopes);

        final TextField userField = new TextField("User");
        middle.addComponent(userField);

        layout.addComponent(middle);

        layout.addComponent(new Button("Send system message", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                String subject = (String) subjectField.getValue();
                String message = (String) messageField.getValue();
                MessageType type = MessageType.valueOf(((String) types.getValue()).toUpperCase());
                String scope = (String) scopes.getValue();

                if ("Local".equals(scope)) {
                    listener.handleLocalMessage(type, subject, message);
                } else if ("Global".equals(scope)){
                    listener.handleGlobalMessage(type, subject, message);
                } else {
                    final String userName = (String) userField.getValue();
                    listener.handleUserMessage(userName, type, subject, message);
                }
            }
        }));
/*
        layout.addComponent(new Button("Send confirmation message", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                listener.showConfirmationMessage("Confirmation - " + messageField.getValue());
            }
        }));
*/
        component = layout;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return component;
    }
}
