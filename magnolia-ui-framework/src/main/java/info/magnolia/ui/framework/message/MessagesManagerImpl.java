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

import java.util.List;

import javax.inject.Singleton;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Implementation of {@link MessagesManager}.
 *
 * @version $Id$
 */
@Singleton
public class MessagesManagerImpl implements MessagesManager {

    private int idCounter = 0;

    private final ListMultimap<String, Message> messages = ArrayListMultimap.create();
    private final ListMultimap<String, MessageListener> listeners = ArrayListMultimap.create();

    @Override
    public synchronized void sendMessageToAllUsers(Message message) {
        for (final String userId : listeners.keySet()) {
            sendMessage(userId, message);
        }
    }

    @Override
    public synchronized void sendMessage(String userId, Message message) {
        message.setId(generateMessageId());
        messages.put(userId, message);
        for (final MessageListener listener : listeners.get(userId)) {
            if (listener != null) {
                listener.handleMessage(message);
            }
        }
    }

    @Override
    public synchronized void registerMessagesListener(String userId, MessageListener listener) {
        listeners.put(userId, listener);
    }

    @Override
    public synchronized void unregisterMessagesListener(String userId, MessageListener listener) {
        listeners.remove(userId, listener);
    }

    @Override
    public void removeMessage(String id) {

    }

    @Override
    public int getMessageCountForUser(String userId) {
        return 0;
    }

    @Override
    public List<Message> getMessagesForUser(String userId) {
        return messages.get(userId);
    }

    private synchronized String generateMessageId() {
        return String.valueOf(idCounter++);
    }
}
