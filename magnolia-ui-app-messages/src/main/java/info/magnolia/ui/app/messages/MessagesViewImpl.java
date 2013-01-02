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

import info.magnolia.ui.framework.message.MessageType;

import java.io.Serializable;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * View implementation for the Messages app.
 */
public class MessagesViewImpl implements MessagesView {

    private Listener listener;

    private final Component component;

    public MessagesViewImpl() {

        // create form and data item
        final Message message = new Message(); // message POJO
        BeanItem<Message> messageItem = new BeanItem<Message>(message);

        final Form form = new Form();
        form.setItemDataSource(messageItem);
        form.setFormFieldFactory(new MessageFieldFactory());
        form.setVisibleItemProperties(new String[] { "title", "content", "type", "scope", "user" });
        // form.setWriteThrough(false);
        form.setImmediate(true);
        form.setWidth("100%");
        /**
         * TODO: HANDLE MARGINS
         */
        // form.getLayout().setMargin(false);
        ((FormLayout) form.getLayout()).setSpacing(true);

        // send button
        NativeButton sendButton = new NativeButton("Send message", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                form.commit();

                String subject = message.getTitle();
                String content = message.getContent();
                MessageType type = message.getType();
                String scope = message.getScope();

                if ("Local".equals(scope)) {
                    listener.handleLocalMessage(type, subject, content);
                } else if ("Global".equals(scope)) {
                    listener.handleGlobalMessage(type, subject, content);
                } else {
                    final String userName = message.getUser();
                    listener.handleUserMessage(userName, type, subject, content);
                }
            }
        });
        sendButton.addStyleName("btn-dialog");
        sendButton.addStyleName("btn-dialog-commit");

        // reset button
        NativeButton resetButton = new NativeButton("Reset", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                message.reset();
                form.discard();
            }
        });
        resetButton.addStyleName("btn-dialog");
        resetButton.addStyleName("btn-dialog-cancel");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addStyleName("buttons");
        buttons.setSpacing(true);
        buttons.addComponent(sendButton);
        buttons.addComponent(resetButton);
        form.getLayout().addComponent(buttons);

        // intro text
        Label intro = new Label("This app enables to send different types of messages to some or all users on a Magnolia instance.<br />" +
                "Please note that this app is for testing purposes only and will be removed in the final release.", Label.CONTENT_XHTML);
        intro.addStyleName("intro");

        CssLayout container = new CssLayout();
        container.setSizeFull();
        container.addStyleName("small-app-panel");
        /**
         * TODO: handle margins in CSS stylesheet.
         */
        // container.setMargin(true, true, true, false);
        container.addComponent(form);

        CssLayout root = new CssLayout();
        root.setSizeFull();
        root.setWidth("900px");
        root.setStyleName("small-app");
        root.addComponent(intro);
        root.addComponent(container);

        component = root;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return component;
    }

    /**
     * A factory for creating message fields.
     */
    private class MessageFieldFactory extends DefaultFieldFactory {

        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            Field f;

            if ("title".equals(propertyId)) {

                final TextField subjectField = new TextField("Message title");
                subjectField.addStyleName("required");
                subjectField.setSizeFull();
                subjectField.setRequired(true);
                // force plain input
                subjectField.setColumns(0);
                f = subjectField;

            } else if ("content".equals(propertyId)) {

                final TextArea messageField = new TextArea("Message");
                messageField.setSizeFull();
                f = messageField;

            } else if ("type".equals(propertyId)) {

                final OptionGroup types = new OptionGroup("Type of message");
                types.setNullSelectionAllowed(false);
                types.addItem(MessageType.INFO);
                types.setItemCaption(MessageType.INFO, "Informational");
                types.addItem(MessageType.WARNING);
                types.setItemCaption(MessageType.WARNING, "Warning");
                types.addItem(MessageType.ERROR);
                types.setItemCaption(MessageType.ERROR, "Error");
                types.setValue(MessageType.INFO);
                types.addStyleName("horizontal");
                f = types;

            } else if ("scope".equals(propertyId)) {

                final OptionGroup scopes = new OptionGroup("Scope");
                scopes.setNullSelectionAllowed(false);
                scopes.addItem("Global");
                scopes.setItemCaption("Global", "Send to all users");
                scopes.addItem("Local");
                scopes.setItemCaption("Local", "Send to yourself only");
                scopes.addItem("User");
                scopes.setItemCaption("User", "Send to user:");
                scopes.setValue("Local");
                scopes.addStyleName("vertical");
                f = scopes;

            } else if ("user".equals(propertyId)) {

                final TextField userField = new TextField();
                userField.addStyleName("relative");
                userField.setWidth("360px");
                f = userField;

            } else {
                f = super.createField(item, propertyId, uiContext);
            }

            return f;
        }
    }

    /**
     * The Message POJO.
     */
    public class Message implements Serializable {

        private String title;

        private String content;

        private MessageType type;

        private String scope;

        private String user;

        public Message() {
            reset();
        }

        public void reset() {
            title = "";
            content = "";
            type = MessageType.INFO;
            scope = "Local";
            user = "";
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public MessageType getType() {
            return type;
        }

        public void setType(MessageType type) {
            this.type = type;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

    }
}
