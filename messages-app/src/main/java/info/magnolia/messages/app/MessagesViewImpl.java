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
package info.magnolia.messages.app;

import info.magnolia.ui.api.message.MessageType;

import java.io.Serializable;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
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

    private static final String MESSAGE_SCOPE_GLOBAL = "Global";
    private static final String MESSAGE_SCOPE_LOCAL = "Local";
    private static final String MESSAGE_SCOPE_USER = "User";
    private static final String MESSAGE_SCOPE_GROUP = "Group";

    private Listener listener;

    private final Component component;
    private Field<String> userOrGroupIdField;

    public MessagesViewImpl() {

        // create form and data item
        final Message message = new Message(); // message POJO
        BeanItem<Message> messageItem = new BeanItem<Message>(message);

        final FieldGroup form = new FieldGroup();
        form.setItemDataSource(messageItem);

        Field<String> subjectField = createSubjectTextField();
        Field<String> messageBodyField = createMessageBodyTextField();
        Field<?> typeField = createTypeSelectionField();
        Field<?> scopeField = createScopeSelectionField();
        userOrGroupIdField = createUserOrGroupIdTextField();

        // disable user/group field if not necessary
        scopeField.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                updateUserOrGroupField((String) event.getProperty().getValue());
            }
        });

        form.bind(subjectField, "title");
        form.bind(messageBodyField, "content");
        form.bind(typeField, "type");
        form.bind(scopeField, "scope");
        form.bind(userOrGroupIdField, "user");
        // FieldGroup overrides fields' own enabled property with its own.
        updateUserOrGroupField(message.getScope());

        FormLayout layout = new FormLayout();
        layout.addComponent(subjectField);
        layout.addComponent(messageBodyField);
        layout.addComponent(typeField);
        layout.addComponent(scopeField);
        layout.addComponent(userOrGroupIdField);

        layout.setSpacing(true);
        layout.setMargin(false);
        layout.setWidth("100%");

        // send button
        NativeButton sendButton = new NativeButton("Send message", new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    form.commit();
                    String subject = message.getTitle();
                    String content = message.getContent();
                    MessageType type = message.getType();
                    String scope = message.getScope();

                    if (MESSAGE_SCOPE_LOCAL.equals(scope)) {
                        listener.handleLocalMessage(type, subject, content);
                    } else if (MESSAGE_SCOPE_GLOBAL.equals(scope)) {
                        listener.handleGlobalMessage(type, subject, content);
                    } else if (MESSAGE_SCOPE_GROUP.equals(scope)) {
                        // message is bound to FieldGroup - hence the group name is to be retrieved from the user field of the message
                        final String groupName = message.getUser();
                        listener.handleGroupMessage(groupName, type, subject, content);
                    } else {
                        // User...
                        final String userName = message.getUser();
                        listener.handleUserMessage(userName, type, subject, content);
                    }
                } catch (CommitException e) {

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
        layout.addComponent(buttons);

        // intro text
        Label intro = new Label("This app enables to send different types of messages to some or all users on a Magnolia instance.<br />" +
                "Please note that this app is for testing purposes only and will be removed in the final release.", ContentMode.HTML);
        intro.addStyleName("intro");

        CssLayout container = new CssLayout();
        container.setSizeFull();
        container.addStyleName("small-app-panel");
        container.addComponent(layout);

        CssLayout root = new CssLayout();
        root.setSizeFull();
        root.setWidth("900px");
        root.setStyleName("small-app");
        root.addComponent(intro);
        root.addComponent(container);

        component = root;
    }

    private Field<String> createUserOrGroupIdTextField() {
        final TextField userOrGroupField = new TextField("User or group");
        userOrGroupField.setWidth("360px");
        return userOrGroupField;
    }

    private void updateUserOrGroupField(String scope) {
        if (MESSAGE_SCOPE_GLOBAL.equals(scope)
                || MESSAGE_SCOPE_LOCAL.equals(scope)) {
            userOrGroupIdField.setEnabled(false);
        } else {
            userOrGroupIdField.setEnabled(true);
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public Component asVaadinComponent() {
        return component;
    }

    private Field<String> createMessageBodyTextField() {
        final TextArea messageField = new TextArea("Message");
        messageField.setWidth(100, Unit.PERCENTAGE);
        return messageField;
    }

    private OptionGroup createTypeSelectionField() {
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
        return types;
    }

    private OptionGroup createScopeSelectionField() {
        final OptionGroup scopes = new OptionGroup("Scope");
        scopes.setImmediate(true);
        scopes.setNullSelectionAllowed(false);
        scopes.addItem(MESSAGE_SCOPE_GLOBAL);
        scopes.setItemCaption(MESSAGE_SCOPE_GLOBAL, "Send to all users");
        scopes.addItem(MESSAGE_SCOPE_LOCAL);
        scopes.setItemCaption(MESSAGE_SCOPE_LOCAL, "Send to yourself only");
        scopes.addItem(MESSAGE_SCOPE_USER);
        scopes.setItemCaption(MESSAGE_SCOPE_USER, "Send to user");
        scopes.addItem(MESSAGE_SCOPE_GROUP);
        scopes.setItemCaption(MESSAGE_SCOPE_GROUP, "Send to group");
        // initial selection
        scopes.addStyleName("vertical");
        return scopes;
    }

    private Field<String> createSubjectTextField() {
        final TextField subjectField = new TextField("Message title");
        subjectField.addStyleName("required");
        subjectField.setWidth(100, Unit.PERCENTAGE);
        subjectField.setRequired(true);
        // force plain input
        subjectField.setColumns(0);
        return subjectField;
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
            scope = MESSAGE_SCOPE_LOCAL;
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
