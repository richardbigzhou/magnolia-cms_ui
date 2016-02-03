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
package info.magnolia.ui.framework.message;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * Implementation of {@link MessagesManager}.
 */
@Singleton
public class MessagesManagerImpl implements MessagesManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ListMultimap<String, MessageListener> listeners = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, MessageListener>create());

    private Provider<SecuritySupport> securitySupportProvider;

    private MessageStore messageStore;

    @Inject
    public MessagesManagerImpl(Provider<SecuritySupport> securitySupportProvider, MessageStore messageStore) {
        this.securitySupportProvider = securitySupportProvider;
        this.messageStore = messageStore;
    }

    @Override
    public void broadcastMessage(Message message) {

        Collection<User> users;
        try {
            users = securitySupportProvider.get().getUserManager().getAllUsers();
        } catch (UnsupportedOperationException e) {
            logger.error("Unable to broadcast message because UserManager does not support enumerating its users", e);
            return;
        }

        for (User user : users) {
            sendMessage(user.getName(), message);
        }

        // Reset it to null simply to avoid assumptions about the id in calling code
        message.setId(null);
    }

    @Override
    public void sendMessage(String userName, Message message) {
        // We need to set the id to null to make sure each user gets a unique id. Otherwise an id
        // suitable for a first user gets generated and is then used for everyone, possible overwriting
        // already existing messages for some users.
        message.setId(null);

        messageStore.saveMessage(userName, message);

        sendMessageSentEvent(userName, message);
    }

    @Override
    public void sendGroupMessage(final String groupName, final Message message) {
        Collection<String> userNames = securitySupportProvider.get().getUserManager().getUsersWithGroup(groupName, true);
        for (String userName : userNames) {
            sendMessage(userName, message);
        }
        // Reset it to null simply to avoid assumptions about the id in calling code
        message.setId(null);
    }

    @Override
    public void sendLocalMessage(Message message) {
        sendMessage(MgnlContext.getUser().getName(), message);
    }

    @Override
    public void clearMessage(final String userName, final String messageId) {
        final Message message = messageStore.findMessageById(userName, messageId);
        if (message != null && !message.isCleared()) {
            message.setCleared(true);
            messageStore.saveMessage(userName, message);
            sendMessageClearedEvent(userName, message);
        }
    }

    @Override
    public int getNumberOfUnclearedMessagesForUser(String userName) {
        return messageStore.getNumberOfUnclearedMessagesForUser(userName);
    }

    @Override
    public List<Message> getMessagesForUser(String userName) {
        return messageStore.findAllMessagesForUser(userName);
    }

    @Override
    public List<Message> getMessageBatch(String userName, List<MessageType> types, Map<String, Boolean> sortCriteria, int limit, int offset) {
        return messageStore.getMessages(userName, types, sortCriteria, limit, offset);
    }

    @Override
    public long getMessagesAmount(final String userName, final List<MessageType> types) {
        return messageStore.getMessageAmount(userName, types);
    }

    @Override
    public Message getMessageById(String userName, String messageId) {
        return messageStore.findMessageById(userName, messageId);
    }

    @Override
    public void registerMessagesListener(String userName, MessageListener listener) {
        listeners.put(userName, listener);
    }

    @Override
    public void unregisterMessagesListener(String userName, MessageListener listener) {
        listeners.remove(userName, listener);
    }

    @Override
    public void removeMessage(String userName, String messageId) {
        messageStore.removeMessageById(userName, messageId);
        sendMessageRemovedEvent(userName, messageId);
    }

    @Override
    public int getNumberOfUnclearedMessagesForUserAndByType(String userName, MessageType type) {
        return messageStore.getNumberOfUnclearedMessagesForUserAndByType(userName, type);
    }

    private void sendMessageClearedEvent(String userName, Message message) {
        final List<MessageListener> userListener = new LinkedList<MessageListener>(listeners.get(userName));
        for (final MessageListener listener : userListener) {
            listener.messageCleared(message);
        }
    }

    private void sendMessageRemovedEvent(String userName, String id) {
        final List<MessageListener> userListener = new LinkedList<MessageListener>(listeners.get(userName));
        for (final MessageListener listener : userListener) {
            listener.messageRemoved(id);
        }
    }

    private void sendMessageSentEvent(String userName, Message message) {
        final List<MessageListener> userListener = new LinkedList<MessageListener>(listeners.get(userName));
        for (final MessageListener listener : userListener) {
            try {
                listener.messageSent(cloneMessage(message));
            } catch (CloneNotSupportedException e) {
                logger.warn("Exception caught when dispatching event: " + e.getMessage(), e);
            }
        }
    }

    private Message cloneMessage(Message message) throws CloneNotSupportedException {
        return message.clone();
    }

    @Override
    public void saveMessage(String userName, Message message) {
        messageStore.saveMessage(userName, message);
    }
}
