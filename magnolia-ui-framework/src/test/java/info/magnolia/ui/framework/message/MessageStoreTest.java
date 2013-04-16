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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockNode;
import info.magnolia.ui.framework.AdmincentralNodeTypes;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class MessageStoreTest extends MgnlTestCase {

    @Override
    @Before
    public void setUp() throws Exception{
        super.setUp();
        MockContext ctx = MockUtil.getMockContext();
        User usr = mock(User.class);
        when(usr.getName()).thenReturn(Message.DEFAULT_SENDER);
        ctx.setUser(usr);
    }

    @Test
    public void testMarshall() throws Exception {
        // GIVEN
        final MockNode messageNode = new MockNode();
        final String messageText = "Message in a bottle.";
        final String subject = "Test";
        final MessageType type = MessageType.WARNING;

        final Message message = new Message(type, subject, messageText);

        final MessageStore store = new MessageStore();

        // WHEN
        store.marshallMessage(message, messageNode);

        // THEN
        assertEquals(subject, messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT).getString());
        assertEquals(Message.DEFAULT_SENDER, messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.SENDER).getString());
        assertEquals(messageText, messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE).getString());
        assertEquals(type.name(), messageNode.getProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE).getString());
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

        messageNode.setName(id);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.TIMESTAMP, now);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.SENDER, sender);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.SUBJECT, subject);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGE, messageText);
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.MESSAGETYPE, type.name());

        MessageStore store = new MessageStore();

        // WHEN
        final Message result = store.unmarshallMessage(messageNode);

        // THEN
        assertEquals(id, result.getId());
        assertEquals(now, result.getTimestamp());
        assertEquals(sender, result.getSender());
        assertEquals(subject, result.getSubject());
        assertEquals(messageText, result.getMessage());
        assertEquals(type, result.getType());
    }

    @Test
    public void testUnmarshallWorksEvenIfNotAllPropertiesAreSet() throws Exception {
        // GIVEN
        final MockNode messageNode = new MockNode();
        final long now = System.currentTimeMillis();
        final String id = "1234";
        messageNode.setName(id);
        // timestamp is mandatory...
        messageNode.setProperty(AdmincentralNodeTypes.SystemMessage.TIMESTAMP, now);

        MessageStore store = new MessageStore();

        // WHEN
        final Message result = store.unmarshallMessage(messageNode);

        // THEN
        assertEquals(id, result.getId());
    }

}
