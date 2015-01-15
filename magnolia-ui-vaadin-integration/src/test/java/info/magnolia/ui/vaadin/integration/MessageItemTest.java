/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.vaadin.integration;

import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.api.message.Message;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * MessageItemTest.
 */
public class MessageItemTest {

    @Before
    public void setUp() {
        Context ctx = mock(Context.class);
        User user = mock(User.class);

        when(ctx.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("someone");

        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void messageItemResolvesAdditionalProperties() throws Exception {
        // GIVEN
        Message message = new Message();
        message.addProperty("foo", 1);
        message.addProperty("bar", "baz");

        // WHEN
        MessageItem messageItem = new MessageItem(message);
        Collection<String> ids = (Collection<String>) messageItem.getItemPropertyIds();

        // THEN
        assertThat(ids, hasItems("foo", "bar", "sender", "message", "subject"));

        assertEquals("1", messageItem.getItemProperty("foo").getValue());
        assertEquals("baz", messageItem.getItemProperty("bar").getValue());
    }

    @Test
    public void customMessageItemContainsAdditionalGetters() throws Exception {
        // GIVEN
        Message message = new MyMessage();

        // WHEN
        MessageItem messageItem = new MessageItem(message);
        Collection<String> ids = (Collection<String>) messageItem.getItemPropertyIds();

        // THEN
        assertThat(ids, hasItems("qux", "sender", "message", "subject"));
    }

    private class MyMessage extends Message {
        private String qux;

        public String getQux() {
            return qux;
        }

        public void setQux(String qux) {
            this.qux = qux;
        }

    }
}
