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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.shellapp.pulse.message.PulseMessageCategoryNavigator.MessageCategory;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * Presenter of {@link PulseMessagesView}.
 */
public final class PulseMessagesPresenter implements PulseMessagesView.Listener, MessageEventHandler {

    public static final String GROUP_PLACEHOLDER_ITEMID = "##SUBSECTION##";
    public static final String NEW_PROPERTY_ID = "new";
    public static final String TYPE_PROPERTY_ID = "type";
    public static final String SUBJECT_PROPERTY_ID = "subject";
    public static final String TEXT_PROPERTY_ID = "text";
    public static final String SENDER_PROPERTY_ID = "sender";
    public static final String DATE_PROPERTY_ID = "date";
    public static final String QUICKDO_PROPERTY_ID = "quickdo";

    private final PulseMessagesView view;

    private HierarchicalContainer container;

    private final MessagesManager messagesManager;

    private final ShellImpl shell;

    private boolean grouping = false;
    private Listener listener;

    @Inject
    public PulseMessagesPresenter(@Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus, final PulseMessagesView view, final ShellImpl shellImpl, final MessagesManager messagesManager) {
        this.view = view;
        this.shell = shellImpl;
        this.messagesManager = messagesManager;
        admincentralEventBus.addHandler(MessageEvent.class, this);

        shell.setIndication(
                ShellAppType.PULSE,
                messagesManager.getNumberOfUnclearedMessagesForUser(MgnlContext.getUser().getName()));
    }

    public View start() {
        view.setListener(this);
        initView();
        return view;
    }

    private void initView() {
        container = createMessageDataSource();
        view.setDataSource(container);
        view.refresh();
        for (MessageType type : MessageType.values()) {
            doUnreadMessagesUpdate(type, 0);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void messageSent(MessageEvent event) {
        final Message message = event.getMessage();
        addMessageAsItem(message);

        if (grouping) {
            buildTree();
        }
        final MessageType type = message.getType();
        doUnreadMessagesUpdate(type, 1);
    }

    @Override
    public void messageCleared(MessageEvent event) {
        final Message message = event.getMessage();
        assignPropertiesFromMessage(message, container.getItem(message.getId()));

        final MessageType type = message.getType();
        doUnreadMessagesUpdate(type, -1);
    }

    private HierarchicalContainer createMessageDataSource() {
        container = new HierarchicalContainer();
        container.addContainerProperty(NEW_PROPERTY_ID, Boolean.class, true);
        container.addContainerProperty(SUBJECT_PROPERTY_ID, String.class, null);
        container.addContainerProperty(TYPE_PROPERTY_ID, MessageType.class, MessageType.UNKNOWN);
        container.addContainerProperty(TEXT_PROPERTY_ID, String.class, null);
        container.addContainerProperty(SENDER_PROPERTY_ID, String.class, null);
        container.addContainerProperty(DATE_PROPERTY_ID, Date.class, null);
        container.addContainerProperty(QUICKDO_PROPERTY_ID, String.class, null);

        createSuperItems();

        for (Message message : messagesManager.getMessagesForUser(MgnlContext.getUser().getName())) {
            addMessageAsItem(message);
        }

        container.addContainerFilter(sectionFilter);

        return container;
    }

    private void createSuperItems() {
        for (MessageType type : MessageType.values()) {
            Object itemId = getSuperItem(type);
            Item item = container.addItem(itemId);
            item.getItemProperty(TYPE_PROPERTY_ID).setValue(type);
            container.setChildrenAllowed(itemId, true);
        }
    }

    private void clearSuperItemFromMessages() {
        for (Object itemId : container.getItemIds()) {
            container.setParent(itemId, null);
        }
    }

    private Object getSuperItem(MessageType type) {
        return GROUP_PLACEHOLDER_ITEMID + type;
    }

    /*
     * Sets the grouping of messages
     */
    @Override
    public void setGrouping(boolean checked) {
        grouping = checked;

        clearSuperItemFromMessages();
        container.removeContainerFilter(sectionFilter);

        if (checked) {
            buildTree();
        }

        container.addContainerFilter(sectionFilter);
    }

    /**
     * Return list of child items.
     *
     * @param itemId parent itemId
     */
    @Override
    public Collection<?> getGroup(Object itemId) {
        return container.getChildren(itemId);
    }

    /**
     * Return parent itemId for an item.
     */
    @Override
    public Object getParent(Object itemId) {
        return container.getParent(itemId);
    }

    /*
     * This filter hides grouping titles when
     * grouping is not on or group would be empty
     */
    private Filter sectionFilter = new Filter() {

        @Override
        public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
            if (itemId.toString().startsWith(GROUP_PLACEHOLDER_ITEMID) && (!grouping || isTypeGroupEmpty(itemId))) {
                return false;
            }

            return true;
        }

        @Override
        public boolean appliesToProperty(Object propertyId) {
            return TYPE_PROPERTY_ID.equals(propertyId);
        }

        private boolean isTypeGroupEmpty(Object typeId) {
            return container.getChildren(typeId) == null || container.getChildren(typeId).isEmpty();
        }

    };

    /*
     * Assign messages under correct parents so that
     * grouping works.
     */
    private void buildTree() {
        for (Object itemId : container.getItemIds()) {
            // Skip super items
            if (!itemId.toString().startsWith(GROUP_PLACEHOLDER_ITEMID)) {
                Item item = container.getItem(itemId);
                MessageType type = (MessageType) item.getItemProperty(TYPE_PROPERTY_ID).getValue();
                Object parentItemId = getSuperItem(type);
                Item parentItem = container.getItem(parentItemId);
                if (parentItem != null) {
                    container.setParent(itemId, parentItemId);
                }
            }
        }
    }

    private void addMessageAsItem(Message message) {
        // filter out local messages that have id == null
        if (message.getId() != null) {
            final Item item = container.addItem(message.getId());
            container.setChildrenAllowed(message.getId(), false);
            assignPropertiesFromMessage(message, item);
        }
    }

    private void assignPropertiesFromMessage(Message message, final Item item) {
        if (item != null && message != null) {
            item.getItemProperty(NEW_PROPERTY_ID).setValue(!message.isCleared());
            item.getItemProperty(TYPE_PROPERTY_ID).setValue(message.getType());
            item.getItemProperty(SENDER_PROPERTY_ID).setValue(message.getSender());
            item.getItemProperty(SUBJECT_PROPERTY_ID).setValue(StringUtils.abbreviate(message.getSubject(), 55));
            item.getItemProperty(TEXT_PROPERTY_ID).setValue(StringUtils.abbreviate(message.getMessage(), 75));
            item.getItemProperty(DATE_PROPERTY_ID).setValue(new Date(message.getTimestamp()));
        }
    }

    @Override
    public void filterByMessageCategory(MessageCategory category) {
        if (container != null) {
            container.removeAllContainerFilters();
            container.addContainerFilter(sectionFilter);
            applyCategoryFilter(category);
        }
    }

    private void applyCategoryFilter(final MessageCategory category) {
        final Filter filter = new Filter() {

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
                final MessageType type = (MessageType) item.getItemProperty(TYPE_PROPERTY_ID).getValue();

                switch (category) {
                case WORK_ITEM:
                    return type == MessageType.WORKITEM;
                case PROBLEM:
                    return type == MessageType.ERROR || type == MessageType.WARNING;
                case INFO:
                    return type == MessageType.INFO;
                default:
                    return true;
                }
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return TYPE_PROPERTY_ID.equals(propertyId);
            }

        };
        container.addContainerFilter(filter);
    }

    @Override
    public void onMessageClicked(String messageId) {
        listener.openMessage(messageId);
        messagesManager.clearMessage(MgnlContext.getUser().getName(), messageId);
    }

    @Override
    public void deleteMessages(final Set<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        final String userName = MgnlContext.getUser().getName();
        int messagesDeleted = 0;

        for (String messageId : messageIds) {
            Message message = messagesManager.getMessageById(userName, messageId);
            if (message == null) {
                continue;
            }
            messagesManager.removeMessage(userName, messageId);

            if (!message.isCleared()) {
                messagesDeleted++;
            }
        }
        shell.updateShellAppIndication(ShellAppType.PULSE, -messagesDeleted);
        /*
         * Refreshes the view to display the updated underlying data.
         */
        initView();
    }

    private void doUnreadMessagesUpdate(final MessageType type, int decrementOrIncrement) {

        shell.updateShellAppIndication(ShellAppType.PULSE, decrementOrIncrement);

        int count = 0;
        final String userName = MgnlContext.getUser().getName();

        switch (type) {

        case ERROR:
        case WARNING:
            count = messagesManager.getNumberOfUnclearedMessagesForUserAndByType(userName, MessageType.ERROR);
            count += messagesManager.getNumberOfUnclearedMessagesForUserAndByType(userName, MessageType.WARNING);
            view.updateCategoryBadgeCount(MessageCategory.PROBLEM, count);
            break;
        case INFO:
            count = messagesManager.getNumberOfUnclearedMessagesForUserAndByType(userName, type);
            view.updateCategoryBadgeCount(MessageCategory.INFO, count);
            break;
        case WORKITEM:
            count = messagesManager.getNumberOfUnclearedMessagesForUserAndByType(userName, type);
            view.updateCategoryBadgeCount(MessageCategory.WORK_ITEM, count);
            break;
        }
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        public void openMessage(String messageId);
    }
}
