/**
 * This file Copyright (c) 2013 Magnolia International
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

import org.junit.Before;
import org.junit.Test;

public class MessageTest {

    public Message message;

    @Before
    public void setUp() {
        message = new Message();
        message.setMessage("bar");
        message.setType(MessageType.INFO);

    }

    @Test
    public void testRetrieveMessageValuesFromMap() throws Exception {
        // GIVEN setup

        // WHEN THEN
        assertTrue(message.containsKey(Message.MESSAGE));
        assertFalse(message.containsKey(Message.SUBJECT));
        assertEquals(MessageType.INFO.name(), message.get(Message.MESSAGETYPE));
    }

    @Test
    public void testMessageInitiallyIsNotCleared() throws Exception {
        // GIVEN setup

        // WHEN THEN
        assertFalse(message.isCleared());
    }

    @Test
    public void testTimestampIsPreservedOnClear() throws Exception {
        // GIVEN
        long timestampBeforeClear = message.getTimestamp();

        // WHEN
        message.clear();

        // THEN
        assertTrue(message.containsKey(Message.TIMESTAMP));
        assertEquals(timestampBeforeClear, message.getTimestamp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotRemoveTimestamp() throws Exception {
        // THEN should throw exception
        message.remove(Message.TIMESTAMP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotReplaceTimestamp() throws Exception {
        // THEN should throw exception
        message.put(Message.TIMESTAMP, 1234565);
    }

}
