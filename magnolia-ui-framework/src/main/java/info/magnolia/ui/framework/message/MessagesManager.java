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

import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;

import java.util.List;
import java.util.Map;

/**
 * Manages users messages.
 */
public interface MessagesManager {

    /**
     * MessageListener.
     */
    public interface MessageListener {

        void messageSent(Message message);

        void messageCleared(Message message);

        void messageRemoved(String id);
    }

    /**
     * Beware: this method is for registering message listeners and should only be used by the entry point of our application AdmincentralUI where we register a dispatcher.
     * If you'll use it to register your own MessageListeners this likely to introduce a memory leak. You should listen to the MessageEvent instead.
     */
    void registerMessagesListener(String userName, MessageListener listener);

    void unregisterMessagesListener(String userName, MessageListener listener);

    /**
     * Returns the number of uncleared (unread) messages for this user.
     *
     * @param userName name of the user
     * @return number of uncleared messages
     */
    int getNumberOfUnclearedMessagesForUser(String userName);

    /**
     * Returns the number of uncleared (unread) messages for this user and for the specific message type.
     */
    int getNumberOfUnclearedMessagesForUserAndByType(String userName, MessageType type);

    /**
     * Returns all messages kept for a specific user.
     *
     * @param userName name of the user
     * @return list of messages kept for the user
     * @deprecated since 5.3.9 - potentially dangerous since it returns the whole set of messages for the user which could be large.
     *             The {@link #getMessageBatch(String, java.util.List, java.util.Map, int, int)} should be used instead because it allows to set the limit and offset parameters.
     */
    @Deprecated
    List<Message> getMessagesForUser(String userName);

    /**
     * More efficient way to query message objects - the amount of return payload is limited, pre-sorted and filtered by type.
     *
     * @param types message types to include in the batch
     * @param sortCriteria properties to order by (true if ascending)
     * @param limit max amount of entries to inclde in the batch
     * @param offset first entry index to query
     * @return list of <code>N = limit</code> {@link Message}'s starting from <code>index = offset</code>
     */
    List<Message> getMessageBatch(String userName, List<MessageType> types, Map<String, Boolean> sortCriteria, int limit, int offset);

    /**
     * Get amount of messages of certain types.
     *
     * @param types types of messages to take in account
     * @return amount of messages of certain types
     */
    long getMessagesAmount(String userName, List<MessageType> types);

    /**
     * Returns a message.
     *
     * @param userName name of the user
     * @return list of messages kept for the user
     */
    Message getMessageById(String userName, String messageId);

    /**
     * Send message to a specific user.
     *
     * @param userName name of the user to receive the message
     * @param message message to send
     */
    void sendMessage(String userName, Message message);

    /**
     * Send message to a specific group.
     *
     * @param groupName name of the group to receive the message
     * @param message message to send
     */
    void sendGroupMessage(String groupName, Message message);

    /**
     * Send message to the current user.
     *
     * @param message message to send
     */
    void sendLocalMessage(Message message);

    /**
     * Sends a message to all users.
     *
     * @param message message to send
     */
    void broadcastMessage(Message message);

    /**
     * Marks a message as cleared.
     *
     * @param userName name of the user the message belongs to
     * @param messageId id of message
     */
    void clearMessage(String userName, String messageId);

    void removeMessage(String userName, String messageId);

    void saveMessage(String userName, Message message);
}
