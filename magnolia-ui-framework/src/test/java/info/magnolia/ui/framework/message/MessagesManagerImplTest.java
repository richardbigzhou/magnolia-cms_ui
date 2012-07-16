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

import java.util.ArrayList;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.google.inject.util.Providers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.SessionTestUtil;

/**
 * Test case {@link MessagesManagerImpl}.
 */
public class MessagesManagerImplTest {

    private Session session;
    private MessagesManagerImpl messagesManager;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {

        session = SessionTestUtil.createSession("messages", "/");

        MockUtil.initMockContext();
        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        MessageStore messageStore = new MessageStore();

        ArrayList<User> users = new ArrayList<User>();
        users.add(createMockUser("alice"));
        users.add(createMockUser("bob"));

        UserManager userManager = mock(UserManager.class);
        when(userManager.getAllUsers()).thenReturn(users);

        SecuritySupport securitySupport = mock(SecuritySupport.class);
        when(securitySupport.getUserManager()).thenReturn(userManager);

        messagesManager = new MessagesManagerImpl(Providers.of(securitySupport), messageStore);
    }

    private User createMockUser(String name) {
        User user = mock(User.class);
        when(user.getName()).thenReturn(name);
        return user;
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testBroadcastMessage() throws RepositoryException {

        MessagesManager.MessageListener listenerA = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("alice", listenerA);
        MessagesManager.MessageListener listenerB = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listenerB);
        MessagesManager.MessageListener listenerC = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("charlie", listenerC);

        // WHEN
        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");
        messagesManager.broadcastMessage(message);

        // THEN
        assertNull(message.getId());

        verify(listenerA).messageSent(any(Message.class));
        verify(listenerB).messageSent(any(Message.class));
        verify(listenerC, never()).messageSent(any(Message.class));

        assertTrue(session.nodeExists("/alice/0"));
        assertTrue(session.nodeExists("/bob/0"));
        assertFalse(session.nodeExists("/charlie"));

        assertTrue(session.getNode("/alice").getPrimaryNodeType().getName().equals(MgnlNodeType.NT_CONTENT));
        assertTrue(session.getNode("/alice/0").getPrimaryNodeType().getName().equals("mgnl:message"));

        assertEquals(1, messagesManager.getMessagesForUser("alice").size());
        assertEquals(1, messagesManager.getMessagesForUser("bob").size());
        assertEquals(0, messagesManager.getMessagesForUser("charlie").size());
    }

    @Test
    public void testSendMessage() throws RepositoryException {

        MessagesManager.MessageListener listenerA = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("alice", listenerA);
        MessagesManager.MessageListener listenerB = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listenerB);
        MessagesManager.MessageListener listenerC = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("charlie", listenerC);

        // WHEN
        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");
        messagesManager.sendMessage("bob", message);

        // THEN
        assertNotNull(message.getId());

        verify(listenerA, never()).messageSent(any(Message.class));
        verify(listenerB).messageSent(any(Message.class));
        verify(listenerC, never()).messageSent(any(Message.class));

        assertFalse(session.nodeExists("/alice"));
        assertTrue(session.nodeExists("/bob/0"));
        assertFalse(session.nodeExists("/charlie"));

        assertTrue(session.getNode("/bob").getPrimaryNodeType().getName().equals(MgnlNodeType.NT_CONTENT));
        assertTrue(session.getNode("/bob/0").getPrimaryNodeType().getName().equals("mgnl:message"));
    }

    @Test
    public void testClearMessage() throws RepositoryException {

        MessagesManager.MessageListener listener = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listener);

        // WHEN
        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");
        messagesManager.sendMessage("bob", message);

        assertEquals(1, messagesManager.getNumberOfUnclearedMessagesForUser("bob"));

        messagesManager.clearMessage("bob", message.getId());

        // THEN
        assertEquals(0, messagesManager.getNumberOfUnclearedMessagesForUser("bob"));
        verify(listener).messageSent(any(Message.class));
        assertTrue(session.getNode("/bob/0").getProperty("cleared").getBoolean());
    }

    @Test
    public void testDoesNotInvokeListenerAfterUnregistration() throws RepositoryException {

        MessagesManager.MessageListener listener1 = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listener1);
        MessagesManager.MessageListener listener2 = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listener2);

        // WHEN
        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");
        messagesManager.sendMessage("bob", message);

        verify(listener1, times(1)).messageSent(any(Message.class));
        verify(listener2, times(1)).messageSent(any(Message.class));

        // WHEN
        messagesManager.unregisterMessagesListener("bob", listener1);
        messagesManager.sendMessage("bob", message);

        // THEN
        verify(listener1, times(1)).messageSent(any(Message.class)); // still one, not called
        verify(listener2, times(2)).messageSent(any(Message.class));
    }
}
