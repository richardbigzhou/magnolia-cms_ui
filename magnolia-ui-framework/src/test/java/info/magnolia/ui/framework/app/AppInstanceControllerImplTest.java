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
package info.magnolia.ui.framework.app;

import static org.mockito.Mockito.*;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests.
 */
public class AppInstanceControllerImplTest {

    private MessagesManager messagesManager;
    private AppInstanceControllerImpl appInstanceControllerImpl;
    private I18nizer i18nizer;
    private SimpleTranslator i18n;

    @Before
    public void setUp() throws Exception {
        messagesManager = mock(MessagesManager.class);
        i18nizer = mock(I18nizer.class);
        i18n = mock(SimpleTranslator.class);
    }

    @Test
    public void testSendGroupMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final String testGroup = "test";
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.sendGroupMessage(testGroup, message);

        // THEN
        verify(messagesManager).sendGroupMessage(testGroup, message);
    }

    @Test
    public void testSendUserMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final String testUser = "test";
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.sendUserMessage(testUser, message);

        // THEN
        verify(messagesManager).sendMessage(testUser, message);
    }

    @Test
    public void testSendLocalMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.sendLocalMessage(message);

        // THEN
        verify(messagesManager).sendLocalMessage(message);
    }

    @Test
    public void testBroadcastMessageForwardsToMessagesManager() {
        // GIVEN
        appInstanceControllerImpl = new AppInstanceControllerImpl(null, null, null, null, messagesManager, null, null, null, i18nizer, i18n);
        final Message message = mock(Message.class);

        // WHEN
        appInstanceControllerImpl.broadcastMessage(message);

        // THEN
        verify(messagesManager).broadcastMessage(message);
    }
}
