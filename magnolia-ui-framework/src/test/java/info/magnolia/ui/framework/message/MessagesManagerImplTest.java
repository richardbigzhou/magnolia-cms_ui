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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;

import java.util.ArrayList;
import java.util.Arrays;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.util.Providers;

/**
 * Test case {@link MessagesManagerImpl}.
 */
public class MessagesManagerImplTest extends MgnlTestCase {

    private Session session;
    private MessagesManagerImpl messagesManager;
    private User alice;
    private User bob;
    private UserManager userManager = mock(UserManager.class);

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        session = SessionTestUtil.createSession("messages", "/");

        MockUtil.initMockContext();
        MockUtil.setSystemContextSessionAndHierarchyManager(session);

        MockUtil.getMockContext().setUser(createMockUser("system"));

        MessageStore messageStore = new MessageStore();

        alice = createMockUser("alice");
        bob = createMockUser("bob");
        ArrayList<User> users = new ArrayList<User>();
        users.add(alice);
        users.add(bob);

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

    @Test
    public void testBroadcastMessage() throws RepositoryException {
        // GIVEN
        MessagesManager.MessageListener listenerA = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("alice", listenerA);
        MessagesManager.MessageListener listenerB = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listenerB);
        MessagesManager.MessageListener listenerC = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("charlie", listenerC);

        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");

        // WHEN
        messagesManager.broadcastMessage(message);

        // THEN
        assertNull(message.getId());

        verify(listenerA).messageSent(any(Message.class));
        verify(listenerB).messageSent(any(Message.class));
        verify(listenerC, never()).messageSent(any(Message.class));

        assertTrue(session.nodeExists("/alice/0"));
        assertTrue(session.nodeExists("/bob/0"));
        assertFalse(session.nodeExists("/charlie"));

        assertTrue(session.getNode("/alice").getPrimaryNodeType().getName().equals(NodeTypes.Content.NAME));
        assertTrue(session.getNode("/alice/0").getPrimaryNodeType().getName().equals(MessageStore.MESSAGE_NODE_TYPE));

        assertEquals(1, messagesManager.getMessagesForUser("alice").size());
        assertEquals(1, messagesManager.getMessagesForUser("bob").size());
        assertEquals(0, messagesManager.getMessagesForUser("charlie").size());
    }

    @Test
    public void testSendMessage() throws RepositoryException {
        // GIVEN
        MessagesManager.MessageListener listenerA = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("alice", listenerA);
        MessagesManager.MessageListener listenerB = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listenerB);
        MessagesManager.MessageListener listenerC = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("charlie", listenerC);

        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");

        // WHEN
        messagesManager.sendMessage("bob", message);

        // THEN
        assertNotNull(message.getId());

        verify(listenerA, never()).messageSent(any(Message.class));
        verify(listenerB).messageSent(any(Message.class));
        verify(listenerC, never()).messageSent(any(Message.class));

        assertFalse(session.nodeExists("/alice"));
        assertTrue(session.nodeExists("/bob/0"));
        assertFalse(session.nodeExists("/charlie"));

        assertThat(session.getNode("/bob").getPrimaryNodeType().getName(), equalTo(NodeTypes.Content.NAME));
        assertThat(session.getNode("/bob/0").getPrimaryNodeType().getName(), equalTo(MessageStore.MESSAGE_NODE_TYPE));
    }

    @Test
    public void testSendGroupMessage() throws RepositoryException {
        // GIVEN
        MessagesManager.MessageListener listenerA = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("alice", listenerA);
        MessagesManager.MessageListener listenerB = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listenerB);
        final String testGroup = "bobOnlyGroup";

        when(userManager.getUsersWithGroup(testGroup, true)).thenReturn(Arrays.asList(new String[]{"bob"}));

        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");

        // WHEN
        messagesManager.sendGroupMessage(testGroup, message);

        // THEN
        assertNull(message.getId());

        verify(listenerA, never()).messageSent(any(Message.class));
        verify(listenerB).messageSent(any(Message.class));

        assertFalse("Alice is not in that group, so she should not have received any message", session.nodeExists("/alice"));
        assertTrue("Bob is in that group, so he should have received a message", session.nodeExists("/bob/0"));
    }

    @Test
    public void testSendTransitiveGroupMessage() throws RepositoryException {
        // GIVEN
        MessagesManager.MessageListener listenerB = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listenerB);
        final String transitiveGroup = "group2";

        // bob is in the transitive but not in the direct group ... however,
        // since we use only UserManager.getUsersWithGroup(String groupName, boolean includeUsersFromTransitiveGroups) in MessagesManagerImpl, there is not so much to mock here
        // Testing returning the correct users (including users from transitive groups) is tested in MgnlUserManagerRepositoryTest now.
        // Hence, this test here might be removed.
        when(userManager.getUsersWithGroup(transitiveGroup, true)).thenReturn(Arrays.asList(new String[]{"bob"}));

        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");

        // WHEN
        messagesManager.sendGroupMessage(transitiveGroup, message);

        // THEN
        assertNull(message.getId());

        verify(listenerB).messageSent(any(Message.class));

        assertTrue("Bob is in that group, so he should have received a message", session.nodeExists("/bob/0"));
    }

    @Test
    public void testSendLocalMessage() throws RepositoryException {
        // GIVEN
        final User me = createMockUser("me");
        ((MockWebContext) MgnlContext.getInstance()).setUser(me);
        MessagesManager.MessageListener listenerA = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener(me.getName(), listenerA);

        Message message = new Message();
        message.setType(MessageType.INFO);
        message.setSubject("subject");
        message.setMessage("message");

        // WHEN
        messagesManager.sendLocalMessage(message);

        // THEN
        assertNotNull("Local messages have id", message.getId());
        verify(listenerA).messageSent(any(Message.class));
        assertTrue("Local message are persisted.", session.nodeExists("/me"));
    }

    @Test
    public void testDoNoClearAlreadyClearedMessage() throws RepositoryException {
        // GIVEN
        MessagesManager.MessageListener listener = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listener);

        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");
        message.setCleared(true);
        messagesManager.sendMessage("bob", message);

        // WHEN
        // this happens when clicking on a message row in pulse
        messagesManager.clearMessage("bob", message.getId());

        // THEN
        verify(listener, never()).messageCleared(any(Message.class));

    }

    @Test
    public void testDoesNotInvokeListenerAfterUnregistration() throws RepositoryException {
        // GIVEN
        MessagesManager.MessageListener listener1 = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listener1);
        MessagesManager.MessageListener listener2 = mock(MessagesManager.MessageListener.class);
        messagesManager.registerMessagesListener("bob", listener2);

        Message message = new Message();
        message.setType(MessageType.ERROR);
        message.setSubject("subject");
        message.setMessage("message");

        // WHEN
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
