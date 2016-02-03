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
package info.magnolia.ui.admincentral.shellapp.pulse.message.data;

import static info.magnolia.ui.admincentral.shellapp.pulse.PulseMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.security.User;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.ui.admincentral.shellapp.pulse.data.PulseConstants;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagesManagerMock;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.vaadin.data.Item;

public class MessageQueryTest extends MgnlTestCase {

    private static final String USER = "user";

    private MessagesManager messagesManager;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ((MockContext) MgnlContext.getInstance()).setUser(mock(User.class));

        this.messagesManager = MessagesManagerMock.builder().
                withErrorsWithIds(1, 2, 3).
                withWarningsWithIds(4, 5, 6).
                withInfosWithIds(7, 8, 9).
                build();
    }

    @Test
    public void testSize() throws Exception {
        // GIVEN
        final MessageQueryDefinition defAllTypesAllowed = new TestMessageQueryDefinition();
        final MessageQueryDefinition defErrorsAndWarnings = new TestMessageQueryDefinition();
        defErrorsAndWarnings.setTypes(ImmutableList.of(MessageType.ERROR, MessageType.WARNING));

        final MessageQueryDefinition defInfoWithGrouping = new TestMessageQueryDefinition();
        defInfoWithGrouping.setTypes(ImmutableList.of(MessageType.INFO));
        defInfoWithGrouping.setGroupingByType(true);

        // WHEN
        int sizeWithAllTypeAllowed = new MessageQuery(defAllTypesAllowed, messagesManager).size();
        int sizeWithErrorsAndWarnings = new MessageQuery(defErrorsAndWarnings, messagesManager).size();
        int sizeWithInfosAndGrouping = new MessageQuery(defInfoWithGrouping, messagesManager).size();

        // THEN
        // Nine altogether
        assertEquals(sizeWithAllTypeAllowed, 9);
        // Three errors and three warnings
        assertEquals(sizeWithErrorsAndWarnings, 6);
        // Three info messages plus one grouping entry
        assertEquals(sizeWithInfosAndGrouping, 4);
    }

    @Test
    public void testGetItems() throws Exception {
        // GIVEN
        final MessageQueryDefinition defAllTypesAllowed = new TestMessageQueryDefinition();
        final MessageQueryDefinition defErrorAndWarningSortedDesc = new TestMessageQueryDefinition();
        defErrorAndWarningSortedDesc.setTypes(Arrays.asList(MessageType.ERROR, MessageType.WARNING));
        defErrorAndWarningSortedDesc.setSortPropertyIds(new Object[]{MessageConstants.ID});
        defErrorAndWarningSortedDesc.setSortPropertyAscendingStates(new boolean[]{false});

        final MessageQuery query1 = new MessageQuery(defAllTypesAllowed, messagesManager);
        final MessageQuery query2 = new MessageQuery(defErrorAndWarningSortedDesc, messagesManager);

        // WHEN
        final List<Item> items = query1.loadItems(0, 9);
        final List<Item> itemsSorted = query2.loadItems(0, 6);

        // THEN
        assertThat(items, containsAmountOfMessageItems(9));
        assertThat(items, containsIdsInOrder(1, 2, 3, 4, 5, 6, 7, 8, 9));

        assertThat(itemsSorted, containsAmountOfMessageItems(6));
        assertThat(itemsSorted, containsIdsInOrder(6, 5, 4, 3, 2, 1));
    }

    @Test
    public void testGetItemsWithGrouping() throws Exception {
        // GIVEN
        final MessageQueryDefinition defInfoWithGrouping = new TestMessageQueryDefinition();
        defInfoWithGrouping.setTypes(ImmutableList.of(MessageType.INFO));
        defInfoWithGrouping.setGroupingByType(true);

        final MessageQuery query = new MessageQuery(defInfoWithGrouping, messagesManager);

        // WHEN
        final List<Item> items = query.loadItems(0, 4);

        // THEN
        // One item for grouping item and three info message items
        assertThat(items, containsAmountOfMessageItems(4));
        assertThat(items, containsIdsInOrder(PulseConstants.GROUP_PLACEHOLDER_ITEMID + MessageType.INFO.name(), 7, 8, 9));
    }


    private class TestMessageQueryDefinition extends MessageQueryDefinition {

        public TestMessageQueryDefinition() {
            setUserName(USER);
        }
    }
}
