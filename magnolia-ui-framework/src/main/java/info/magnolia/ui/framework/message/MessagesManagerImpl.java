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
package info.magnolia.ui.framework.message;

import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;

/**
 * Implementation of {@link MessagesManager}.
 */
@Singleton
public class MessagesManagerImpl implements MessagesManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ListMultimap<String, MessageListener> listeners = ArrayListMultimap.create();

    private Provider<SecuritySupport> securitySupportProvider;
    private MessageStore messageStore;

    @Inject
    public MessagesManagerImpl(Provider<SecuritySupport> securitySupportProvider, MessageStore messageStore) {
        this.securitySupportProvider = securitySupportProvider;
        this.messageStore = messageStore;
    }

    @Override
    public void sendMessageToAllUsers(Message message) {

        Collection<User> users;
        try {
            users = securitySupportProvider.get().getUserManager().getAllUsers();
        } catch (UnsupportedOperationException e) {
            logger.error("Unable to broadcast message because UserManager does not support enumerating its users", e);
            return;
        }

        synchronized (listeners) {
            for (User user : users) {

                // We need to set the id to null for each loop to make sure each user gets a unique id. Otherwise an id
                // suitable for the first user gets generated and is then used for everyone, possible overwriting
                // already existing messages for some users.
                message.setId(null);

                messageStore.saveMessage(user.getName(), message);
                sendMessageSentEvent(user.getName(), message);
            }
        }
    }

    @Override
    public void sendMessage(String userId, Message message) {
        messageStore.saveMessage(userId, message);
        sendMessageSentEvent(userId, message);
    }

    @Override
    public void clearMessage(final String userId, final String messageId) {
        final Message message = messageStore.findMessageById(userId, messageId);
        if (message != null) {
            message.setCleared(true);
            messageStore.saveMessage(userId, message);
            sendMessageClearedEvent(userId, message);
        }
    }

    @Override
    public int getNumberOfUnclearedMessagesForUser(String userId) {
        return messageStore.getNumberOfUnclearedMessagesForUser(userId);
    }

    @Override
    public List<Message> getMessagesForUser(String userId) {
        return messageStore.findAllMessagesForUser(userId);
    }

    @Override
    public void registerMessagesListener(String userId, MessageListener listener) {
        synchronized (listeners) {
            listeners.put(userId, listener);
        }
    }

    @Override
    public void unregisterMessagesListener(String userId, MessageListener listener) {
        synchronized (listeners) {
            listeners.remove(userId, listener);
        }
    }

    private void sendMessageClearedEvent(String userId, Message message) {
        synchronized (listeners) {
            final List<MessageListener> listenerList = listeners.get(userId);
            if (listenerList != null) {
                for (final MessageListener listener : listenerList) {
                    listener.messageCleared(message);
                }
            }
        }
    }

    private void sendMessageSentEvent(String userId, Message message) {
        synchronized (listeners) {
            for (final MessageListener listener : listeners.get(userId)) {
                if (listener != null) {
                    listener.messageSent(message);
                }
            }
        }
    }
}
