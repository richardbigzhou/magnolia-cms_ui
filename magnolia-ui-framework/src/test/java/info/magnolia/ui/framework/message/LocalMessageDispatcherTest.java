/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.event.SimpleEventBus;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link LocalMessageDispatcher}.
 */
public class LocalMessageDispatcherTest {
    @Before
    public void setUp() {
        Context ctx = mock(Context.class);
        User usr = mock(User.class);
        when(ctx.getUser()).thenReturn(usr);
        when(usr.getName()).thenReturn("peter");
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }

    @Test
    public void sendsEvents() throws InterruptedException {

        // GIVEN
        EventBus eventBus = new SimpleEventBus();
        LocalMessageDispatcher dispatcher = new LocalMessageDispatcher(eventBus, null);
        ArrayList<MessageEvent> events = new ArrayList<MessageEvent>();
        eventBus.addHandler(MessageEvent.class, new CollectingMessageEventHandler(events));

        // WHEN
        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");

        dispatcher.messageSent(message);
        Thread.sleep(100);

        Message clearedMessage = new Message();
        clearedMessage.setType(MessageType.ERROR);
        clearedMessage.setSubject("subject");
        clearedMessage.setMessage("cleared message");

        dispatcher.messageCleared(clearedMessage);
        Thread.sleep(100);

        // THEN
        assertEquals(2, events.size());
        assertEquals("subject", events.get(0).getMessage().getSubject());
        assertEquals("cleared message", events.get(1).getMessage().getMessage());
    }

    @Test
    public void sendsEventsEvenIfHandlersFail() throws InterruptedException {

        // GIVEN
        EventBus eventBus = new SimpleEventBus();
        LocalMessageDispatcher dispatcher = new LocalMessageDispatcher(eventBus, null);
        ArrayList<MessageEvent> events = new ArrayList<MessageEvent>();
        eventBus.addHandler(MessageEvent.class, new ThrowingMessageEventHandler(events));
        eventBus.addHandler(MessageEvent.class, new ThrowingMessageEventHandler(events));

        // WHEN
        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");

        dispatcher.messageSent(message);
        Thread.sleep(100);

        Message clearedMessage = new Message();
        clearedMessage.setType(MessageType.ERROR);
        clearedMessage.setSubject("subject");
        clearedMessage.setMessage("cleared message");

        dispatcher.messageCleared(clearedMessage);
        Thread.sleep(100);

        // THEN
        assertEquals(4, events.size());
        assertEquals("subject", events.get(0).getMessage().getSubject());
        assertEquals("subject", events.get(1).getMessage().getSubject());
        assertEquals("cleared message", events.get(2).getMessage().getMessage());
        assertEquals("cleared message", events.get(3).getMessage().getMessage());
    }

    private static class CollectingMessageEventHandler implements MessageEventHandler {

        private final List<MessageEvent> events;

        public CollectingMessageEventHandler(List<MessageEvent> events) {
            this.events = events;
        }

        @Override
        public void messageSent(MessageEvent event) {
            events.add(event);
        }

        @Override
        public void messageCleared(MessageEvent event) {
            events.add(event);
        }
    }

    private static class ThrowingMessageEventHandler extends CollectingMessageEventHandler {

        private ThrowingMessageEventHandler(List<MessageEvent> events) {
            super(events);
        }

        @Override
        public void messageSent(MessageEvent event) {
            super.messageSent(event);
            throw new RuntimeException("Intentionally thrown exception for testing");
        }

        @Override
        public void messageCleared(MessageEvent event) {
            super.messageCleared(event);
            throw new RuntimeException("Intentionally thrown exception for testing");
        }
    }
}
