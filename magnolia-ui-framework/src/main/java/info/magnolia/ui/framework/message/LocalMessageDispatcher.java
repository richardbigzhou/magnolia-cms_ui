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

import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.message.MessagesManager.MessageListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dispatches events on an {@link EventBus} for a certain user.
 */
@Singleton
public class LocalMessageDispatcher implements MessageListener {

    private BlockingQueue<MessageEvent> messageQueue = new LinkedBlockingQueue<MessageEvent>();

    private EventBus eventBus;

    private final Thread messageQueueThread = new Thread() {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final MessageEvent msg = messageQueue.take();
                    eventBus.fireEvent(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    };

    @Inject
    public LocalMessageDispatcher(@Named("admincentral") final EventBus eventBus) {
        this.eventBus = eventBus;
        messageQueueThread.setName("LocalMessageDispatcher");
        messageQueueThread.setDaemon(true);
        messageQueueThread.start();
    }

    @Override
    public void messageSent(Message message) {
        queueEvent(new MessageEvent(message, false));
    }

    @Override
    public void messageCleared(Message message) {
        queueEvent(new MessageEvent(message, true));
    }

    private void queueEvent(MessageEvent event) {
        messageQueue.add(event);
    }
}
