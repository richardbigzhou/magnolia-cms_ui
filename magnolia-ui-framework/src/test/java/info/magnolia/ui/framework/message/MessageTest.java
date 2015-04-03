/**
 * This file Copyright (c) 2013-2015 Magnolia International
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

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link Message}.
 */
public class MessageTest {

    private Message message;

    @Before
    public void setUp() {
        Context ctx = mock(Context.class);
        User usr = mock(User.class);
        when(ctx.getUser()).thenReturn(usr);
        when(usr.getName()).thenReturn("peter");
        MgnlContext.setInstance(ctx);

        message = new Message();
        message.setMessage("bar");
        message.setType(MessageType.INFO);
    }

    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
    }


    @Test
    public void testRetrieveMessageValuesFromMap() throws Exception {
        // GIVEN setup
        final String key = "any";
         message.addProperty(key, "value");

        // WHEN
        final boolean result = message.hasProperty(key);

        // WHEN THEN
        assertTrue("Message should contain key: " + key, result);
    }

    @Test
    public void testMessageInitiallyIsNotCleared() throws Exception {
        // GIVEN setup

        // WHEN THEN
        assertFalse(message.isCleared());
    }

    @Test
    public void testNonDefaultSender() throws Exception {
        // GIVEN
        final String nonDefaultUserName = "eric";
        Context ctx = mock(Context.class);
        User usr = mock(User.class);
        when(ctx.getUser()).thenReturn(usr);
        when(usr.getName()).thenReturn(nonDefaultUserName);
        MgnlContext.setInstance(ctx);

        final Message messageFromNonDefaultSender = new Message();

        // WHEN
        final String nonDefaultSenderName = messageFromNonDefaultSender.getSender();

        // THEN
        assertEquals(nonDefaultUserName, nonDefaultSenderName);
    }
}
