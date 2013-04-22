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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.shellapp.pulse.message.PulseMessageCategoryNavigator.MessageCategory;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.model.overlay.View;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * Presenter of {@link PulseMessagesView}.
 */
public class PulseMessagesPresenter implements PulseMessagesView.Listener {

    public static final String GROUP_PLACEHOLDER_ITEMID = "##SUBSECTION##";

    public static final String GROUP_COLUMN = "type";

    private final PulseMessagesView view;

    private HierarchicalContainer container = null;

    private final MessagesManager messagesManager;

    private final ShellImpl shell;

    private boolean grouping = false;
    private Listener listener;

    @Inject
    public PulseMessagesPresenter(final PulseMessagesView view, final ShellImpl shellImpl, final MessagesManager messagesManager) {
        this.view = view;
        this.shell = shellImpl;
        this.messagesManager = messagesManager;

        messagesManager.registerMessagesListener(MgnlContext.getUser().getName(), new MessagesManager.MessageListener() {

            @Override
            public void messageSent(Message message) {
                addMessageAsItem(message);

                if (grouping) {
                    buildTree();
                }

                if (message.getType().isSignificant()) {
                    shell.updateShellAppIndication(ShellAppType.PULSE, 1);
                }
            }

            @Override
            public void messageCleared(Message message) {
                assignPropertiesFromMessage(message, container.getItem(message.getId()));
                if (message.getType().isSignificant()) {
                    shell.updateShellAppIndication(ShellAppType.PULSE, -1);
                }
            }
        });

        shell.setIndication(
                ShellAppType.PULSE,
                messagesManager.getNumberOfUnclearedMessagesForUser(MgnlContext.getUser().getName()));
    }

    public View start() {
        view.setListener(this);
        container = createMessageDataSource();
        view.setDataSource(container);

        return view;
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private HierarchicalContainer createMessageDataSource() {
        container = new HierarchicalContainer();
        container.addContainerProperty("new", String.class, null);
        container.addContainerProperty("type", MessageType.class, MessageType.UNKNOWN);
        container.addContainerProperty("text", String.class, null);
        container.addContainerProperty("sender", String.class, null);
        container.addContainerProperty("date", Date.class, null);
        container.addContainerProperty("quickdo", String.class, null);

        createSuperItems();

        for (Message message : messagesManager.getMessagesForUser(MgnlContext.getUser().getName())) {
            addMessageAsItem(message);
        }

        container.addContainerFilter(sectionFilter);

        return container;
    }

    private void createSuperItems() {
        for (MessageType type : MessageType.values()) {
            Item item = container.addItem(getSuperItem(type));
            item.getItemProperty(GROUP_COLUMN).setValue(type);
            container.setChildrenAllowed(getSuperItem(type), true);
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
            return "type".equals(propertyId);
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
                MessageType type = (MessageType) item.getItemProperty("type").getValue();
                Item parentItem = container.getItem(getSuperItem(type));
                if (parentItem != null) {
                    container.setParent(itemId, getSuperItem(type));
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
            item.getItemProperty("new").setValue(message.isCleared() ? "No" : "Yes");
            item.getItemProperty("type").setValue(message.getType());
            item.getItemProperty("sender").setValue(message.getSender());
            item.getItemProperty("text").setValue(StringUtils.abbreviate(message.getMessage(), 40));
            item.getItemProperty("date").setValue(new Date(message.getTimestamp()));
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
                final MessageType type = (MessageType) item.getItemProperty("type").getValue();

                switch (category) {
                case WORK_ITEM:
                    return type == MessageType.WORKITEM;
                case PROBLEM:
                    return type == MessageType.ERROR;
                case INFO:
                    return type == MessageType.INFO;
                default:
                    return true;
                }
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return "type".equals(propertyId);
            }

        };
        container.addContainerFilter(filter);
    }

    @Override
    public void onMessageClicked(String messageId) {
        listener.openMessage(messageId);
        messagesManager.clearMessage(MgnlContext.getUser().getName(), messageId);
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        public void openMessage(String messageId);
    }
}
