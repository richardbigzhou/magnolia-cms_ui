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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.MgnlUserManager;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.AdmincentralNodeTypes;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class MessageStoreTest extends MgnlTestCase {

    private MessageStore store;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockContext ctx = MockUtil.getMockContext();
        final User user = mock(User.class);
        when(user.getName()).thenReturn("system");
        ctx.setUser(user);

        Session session = new MockSession(MessageStore.WORKSPACE_NAME);
        MockUtil.getSystemMockContext().addSession(MessageStore.WORKSPACE_NAME, session);

        final SecuritySupportImpl securitySupport = new SecuritySupportImpl();

        MgnlUserManager userMgr = new MgnlUserManager() {
            {
                setRealmName(Realm.REALM_SYSTEM.getName());
            }

            @Override
            public User getSystemUser() {
                return user;
            }
        };

        securitySupport.addUserManager(Realm.REALM_SYSTEM.getName(), userMgr);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);
        MockUtil.getMockContext().setUser(user);

        store = new MessageStore();
    }

    @Test
    public void testMarshall() throws Exception {
        // GIVEN
        final MockNode messageNode = new MockNode();
        final String messageText = "Message in a bottle.";
        final String subject = "Test";
        final MessageType type = MessageType.WARNING;

        final Message message = new Message(type, subject, messageText);

        // WHEN
        store.marshallMessage(message, messageNode);

        // THEN
        assertEquals(subject, messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT).getString());
        assertEquals("system", messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.SENDER).getString());
        assertEquals(messageText, messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE).getString());
        assertEquals(type.name(), messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE).getString());
    }

    @Test
    public void testMarshallMessageWithNullProperties() throws Exception {
        // GIVEN
        final MockNode messageNode = new MockNode();

        final Message message = new Message();

        // WHEN
        store.marshallMessage(message, messageNode);

        // THEN
        assertEquals("", messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT).getString());
        assertEquals("system", messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.SENDER).getString());
        assertEquals("", messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE).getString());
        assertEquals(MessageType.UNKNOWN.name(), messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE).getString());
    }

    @Test
    public void testUnmarshall() throws Exception {
        // GIVEN
        final MockNode messageNode = new MockNode();
        final String id = "1234";
        final long now = System.currentTimeMillis();
        final String sender = "someone";
        final String messageText = "Message in a bottle.";
        final String subject = "Test";
        final MessageType type = MessageType.WARNING;
        final boolean cleared = false;

        messageNode.setName(id);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.TIMESTAMP, now);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.SENDER, sender);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT, subject);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE, messageText);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE, type.name());
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.CLEARED, cleared);

        // WHEN
        final Message result = store.unmarshallMessage(messageNode);

        // THEN
        assertEquals(id, result.getId());
        assertEquals(now, result.getTimestamp());
        assertEquals(sender, result.getSender());
        assertEquals(subject, result.getSubject());
        assertEquals(messageText, result.getMessage());
        assertEquals(type, result.getType());
        assertFalse(cleared);
    }

    @Test
    public void testUnmarshallWorksEvenIfNotAllPropertiesAreSet() throws Exception {
        // GIVEN
        final String id = "1234";
        final MockNode messageNode = createEmptyMessageMockNode(id);

        // WHEN
        final Message result = store.unmarshallMessage(messageNode);

        // THEN
        assertEquals(id, result.getId());
    }

    @Test
    public void testSaveMessage() throws Exception {
        // GIVEN
        final String id = "1234";
        final MockNode messageNode = createEmptyMessageMockNode(id);

        final String userName = MgnlContext.getUser().getName();

        // WHEN
        boolean saved = store.saveMessage(userName, store.unmarshallMessage(messageNode));

        // THEN
        assertTrue(saved);
        Message result = store.findMessageById(userName, id);
        assertNotNull(result);
    }

    @Test
    public void testFindMessageById() throws Exception {
        // GIVEN
        final String id = "1234";
        final MockNode messageNode = createEmptyMessageMockNode(id);

        final String userName = MgnlContext.getUser().getName();
        boolean saved = store.saveMessage(userName, store.unmarshallMessage(messageNode));
        assertTrue(saved);

        // WHEN
        Message result = store.findMessageById(userName, id);

        // THEN
        assertNotNull(result);
    }

    @Test
    public void testRemoveMessageById() throws Exception {
        // GIVEN
        final String id = "1234";
        final MockNode messageNode = createEmptyMessageMockNode(id);

        final String userName = MgnlContext.getUser().getName();
        store.saveMessage(userName, store.unmarshallMessage(messageNode));

        Message result = store.findMessageById(userName, id);
        assertNotNull(result);

        // WHEN
        store.removeMessageById(userName, id);

        // THEN
        result = store.findMessageById(userName, id);
        assertNull(result);
    }

    @Test
    public void testGetNumberOfUnclearedMessagesForUser() throws Exception {
        // GIVEN
        final String id = "1234";
        final MockNode messageNode = createEmptyMessageMockNode(id);

        final String userName = MgnlContext.getUser().getName();
        store.saveMessage(userName, store.unmarshallMessage(messageNode));

        // WHEN
        int count = store.getNumberOfUnclearedMessagesForUser(userName);

        // THEN
        assertEquals(1, count);
    }

    @Test
    public void testGetNumberOfUnclearedMessagesForUserAndByType() throws Exception {
        // GIVEN
        final MockNode errorMessage = createEmptyMessageMockNode("1");
        errorMessage.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE, MessageType.ERROR.name());

        final MockNode errorMessage2 = createEmptyMessageMockNode("2");
        errorMessage2.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE, MessageType.ERROR.name());

        final MockNode infoMessage = createEmptyMessageMockNode("3");
        infoMessage.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE, MessageType.INFO.name());

        final String userName = MgnlContext.getUser().getName();
        store.saveMessage(userName, store.unmarshallMessage(errorMessage));
        store.saveMessage(userName, store.unmarshallMessage(errorMessage2));
        store.saveMessage(userName, store.unmarshallMessage(infoMessage));

        // WHEN
        int errors = store.getNumberOfUnclearedMessagesForUserAndByType(userName, MessageType.ERROR);
        int info = store.getNumberOfUnclearedMessagesForUserAndByType(userName, MessageType.INFO);

        // THEN
        assertEquals(2, errors);
        assertEquals(1, info);
    }

    private MockNode createEmptyMessageMockNode(final String id) throws RepositoryException {
        MockNode messageNode = new MockNode();
        final long now = System.currentTimeMillis();
        messageNode.setName(id);
        // timestamp is mandatory...
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.TIMESTAMP, now);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.SENDER, "sender");
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE, "message");
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT, "subject");
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE, MessageType.INFO.name());
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.CLEARED, false);

        return messageNode;
    }
}
