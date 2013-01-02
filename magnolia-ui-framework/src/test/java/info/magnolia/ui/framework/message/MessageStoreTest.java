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

import info.magnolia.test.mock.jcr.MockNode;

import org.junit.Test;

/**
 * Tests.
 */
public class MessageStoreTest {

    @Test
    public void testMarshall() throws Exception {
        // GIVEN
        final MockNode messageNode = new MockNode();
        final long now = System.currentTimeMillis();
        final String messageText = "Message in a bottle.";
        final String subject = "Test";
        final MessageType type = MessageType.WARNING;

        final Message message = new Message(now);
        message.setSubject(subject);
        message.setMessage(messageText);
        message.setType(type);

        final MessageStore store = new MessageStore();

        // WHEN
        store.marshallMessage(message, messageNode);

        // THEN
        assertEquals(subject, messageNode.getProperty(MessageStore.SUBJECT).getString());
        assertEquals(messageText, messageNode.getProperty(MessageStore.MESSAGE).getString());
        assertEquals(type.name(), messageNode.getProperty(MessageStore.TYPE).getString());
    }

    @Test
    public void testUnmarshall() throws Exception {
        // GIVEN
        final MockNode messageNode = new MockNode();
        final String id = "1234";
        final long now = System.currentTimeMillis();
        final String messageText = "Message in a bottle.";
        final String subject = "Test";
        final MessageType type = MessageType.WARNING;

        messageNode.setName(id);
        messageNode.setProperty(MessageStore.TIMESTAMP, now);
        messageNode.setProperty(MessageStore.SUBJECT, subject);
        messageNode.setProperty(MessageStore.MESSAGE, messageText);
        messageNode.setProperty(MessageStore.TYPE, type.name());

        MessageStore store = new MessageStore();

        // WHEN
        final Message result = store.unmarshallMessage(messageNode);

        // THEN
        assertEquals(id, result.getId());
        assertEquals(now, result.getTimestamp());
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
        messageNode.setProperty(MessageStore.TIMESTAMP, now);

        MessageStore store = new MessageStore();

        // WHEN
        final Message result = store.unmarshallMessage(messageNode);

        // THEN
        assertEquals(id, result.getId());
    }

}
