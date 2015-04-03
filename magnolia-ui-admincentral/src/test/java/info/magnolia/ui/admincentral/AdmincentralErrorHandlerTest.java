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
package info.magnolia.ui.admincentral;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.vaadin.server.ErrorEvent;

/**
 * The AdmincentralErrorHandlerTest.
 */
public class AdmincentralErrorHandlerTest {

    private static final String WORKSPACE = "messages";
    private static final String USER_NAME = "oswin";

    private Session session;

    private List<Message> messages;

    private AdmincentralErrorHandler errorHandler;

    @Before
    public void setUp() {
        MessagesManager mockMessagesManager = mock(MessagesManager.class);
        messages = new ArrayList<Message>();
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                messages.add((Message) args[0]);
                return null;
            }
        }).when(mockMessagesManager).sendLocalMessage(any(Message.class));

        User mockUser = mock(User.class);
        when(mockUser.getName()).thenReturn(USER_NAME);
        errorHandler = new AdmincentralErrorHandler(mockMessagesManager);

        session = new MockSession(WORKSPACE);
        MockContext ctx = new MockContext();
        ctx.addSession(WORKSPACE, session);
        ctx.setUser(mockUser);
        MgnlContext.setInstance(ctx);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void testErrorMessageNotEmpty() {
        // GIVEN

        // WHEN
        errorHandler.error(new ErrorEvent(new RuntimeException("")));

        // THEN
        assertEquals(1, messages.size());
        Message message = messages.get(0);

        assertTrue(StringUtils.isNotBlank(message.getSubject()));
        assertTrue(StringUtils.isNotBlank(message.getMessage()));
        assertEquals(USER_NAME, message.getSender());
    }

    @Test
    public void testPreciseErrorMessage() {
        // GIVEN
        final String lessPreciseDetails = "An unsupported exception happened.";
        final String mostPreciseDetails = "Consciously throwing some unsupported exception for this test.";
        Exception e = new InvocationTargetException(new RuntimeException(lessPreciseDetails, new UnsupportedOperationException(mostPreciseDetails)));

        // WHEN
        errorHandler.error(new ErrorEvent(e));

        // THEN
        assertEquals(1, messages.size());
        Message message = messages.get(0);

        assertTrue(StringUtils.isNotBlank(message.getSubject()));
        assertTrue(StringUtils.isNotBlank(message.getMessage()));
        assertEquals(mostPreciseDetails, message.getSubject());
        assertEquals(USER_NAME, message.getSender());
    }

}
