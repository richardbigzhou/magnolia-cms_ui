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
    }

    /**
     * Beware: this method is for registering message listeners and should only be used by the entry point of our application AdmincentralUI where we register a dispatcher.
     *         If you'll use it to register your own MessageListeners this likely to introduce a memory leak. You should listen to the MessageEvent instead.
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
     */
    List<Message> getMessagesForUser(String userName);

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
}
