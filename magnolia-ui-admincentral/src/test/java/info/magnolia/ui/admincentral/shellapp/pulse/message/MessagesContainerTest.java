/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageQuery;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageQueryDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageQueryFactory;
import info.magnolia.ui.framework.message.MessagesManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.vaadin.data.Container;

public class MessagesContainerTest extends MgnlTestCase {

    private static final String USER = "user";

    private MessagesManager messagesManager;

    private MessagesContainer messagesContainer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final User user = mock(User.class);
        doReturn(USER).when(user).getName();
        ((MockContext) MgnlContext.getInstance()).setUser(user);

        this.messagesManager = MessagesManagerMock.builder().
                withErrorsWithIds(1, 2, 3).
                withWarningsWithIds(4, 5, 6).
                withInfosWithIds(7, 8, 9).
                build();

        final MessageQueryDefinition messageQueryDefinition = new MessageQueryDefinition();
        messageQueryDefinition.setUserName(USER);

        final ComponentProvider componentProvider = mock(ComponentProvider.class);
        doReturn(new MessageQuery(messageQueryDefinition, messagesManager)).when(componentProvider).newInstance(eq(MessageQuery.class), anyVararg());

        final Provider<MessageQueryFactory> p2 = Providers.of(new MessageQueryFactory(componentProvider));

        this.messagesContainer = new MessagesContainer(messagesManager, MgnlContext.getInstance(), messageQueryDefinition, p2);
    }

    @Test
    public void testGetSize() throws Exception {
        // WHEN
        long size = messagesContainer.size();

        // THEN
        assertThat(size, equalTo(9l));

        // WHEN
        messagesContainer.filterByItemCategory(PulseItemCategory.INFO);
        size = messagesContainer.size();

        // THEN
        assertThat(size, equalTo(3l));
    }

    /**
     * The test is ignored because the part where the filtered container item id set is evaluated - strange things happen.
     * The query object inside of LQC fails to be assigned to {@code null} at {@link org.vaadin.addons.lazyquerycontainer.LazyQueryView#refresh}.
     * The issue cannot be reproduced in production, but certainly worth investigating.
     */
    @Test
    @Ignore
    public void testGetDataSource() throws Exception {
        // WHEN
        final Container c = messagesContainer.getVaadinContainer();

        // THEN
        assertThat(c.getItemIds(), contains((Object) "1", "2", "3", "4", "5", "6", "7", "8", "9"));

        // WHEN
        messagesContainer.filterByItemCategory(PulseItemCategory.PROBLEM);

        // THEN
        assertThat(c.getItemIds(), contains((Object) "1", "2", "3", "4", "5", "6"));
    }
}
