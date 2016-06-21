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
package info.magnolia.ui.framework.message;

import static org.junit.Assert.*;

import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MessageStoreRepositoryTest extends RepositoryTestCase {

    private static final String USER = "user";
    private static final String USER_WITH_AT = "user@user.cz";
    public static final String WORKSPACE = "messages";
    public static final String REPOSITORY = "magnolia";
    public static final String NODE_TYPES = "/info/magnolia/ui/framework/message/test-system-messages-nodetypes.xml";
    public static final String ID = "id";

    private MessageStore messageStore;
    private Message error;
    private Message warning;
    private Message info;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        repositoryManager.loadWorkspace(REPOSITORY, WORKSPACE);
        repositoryManager.getRepositoryProvider(REPOSITORY).registerNodeTypes(getClass().getResourceAsStream(NODE_TYPES));

        messageStore = new MessageStore();


        error = createMessage("1", MessageType.ERROR);
        warning = createMessage("2", MessageType.WARNING);
        info = createMessage("3", MessageType.INFO);

        messageStore.saveMessage(USER, error);
        messageStore.saveMessage(USER, warning);
        messageStore.saveMessage(USER, info);
    }

    @Test
    public void getMessageAmount() throws Exception {
        // WHEN
        long amountWithoutTypeSpecified = messageStore.getMessageAmount(USER, Lists.<MessageType>newArrayList());
        long amountWithAllTypesIncluded = messageStore.getMessageAmount(USER, Arrays.asList(MessageType.values()));
        long amountOfErrorsAndWarnings = messageStore.getMessageAmount(USER, Arrays.asList(MessageType.ERROR, MessageType.WARNING));

        // THEN
        assertEquals(amountWithoutTypeSpecified, 3l);
        assertEquals(amountWithAllTypesIncluded, 3l);
        assertEquals(amountOfErrorsAndWarnings, 2l);
    }

    @Test
    public void fetchMessages() throws Exception {
        // GIVEN
        List<MessageType> noTypes = Lists.newArrayList();
        HashMap<String, Boolean> noSort = Maps.newHashMap();

        // WHEN
        final List<Message> allMessages = messageStore.getMessages(USER, noTypes, noSort, 3, 0);
        final List<Message> firstTwoMessages = messageStore.getMessages(USER, noTypes, noSort, 2, 0);
        final List<Message> onlyInfo = messageStore.getMessages(USER, Arrays.asList(MessageType.INFO), noSort, 3, 0);

        final Map<String, Boolean> sortCriteria = Maps.newHashMap();
        sortCriteria.put(ID, false);
        final List<Message> orderedByIds = messageStore.getMessages(USER, noTypes, sortCriteria, 3, 0);

        // THEN
        assertThat(allMessages, containsMessages(error, warning, info));
        assertThat(firstTwoMessages, containsMessages(error, warning));
        assertThat(onlyInfo, containsMessages(info));
        assertThat(orderedByIds, containsMessages(info, warning, error));
    }

    @Test
    public void getNumberOfUnclearedMessagesForUser() throws Exception {
        //GIVEN
        error.setCleared(false);
        warning.setCleared(true);
        info.setCleared(true);
        messageStore.saveMessage(USER, error);
        messageStore.saveMessage(USER, warning);
        messageStore.saveMessage(USER, info);

        // WHEN
        int count = messageStore.getNumberOfUnclearedMessagesForUser(USER);

        // THEN
        assertEquals(1, count);
    }

    @Test
    public void getNumberOfUnclearedMessagesForUserWithAtInName() throws Exception {
        //GIVEN
        messageStore.saveMessage(USER_WITH_AT, error);
        messageStore.saveMessage(USER_WITH_AT, warning);
        messageStore.saveMessage(USER_WITH_AT, info);

        // WHEN
        int count = messageStore.getNumberOfUnclearedMessagesForUser(USER_WITH_AT);

        // THEN
        assertEquals(3, count);
    }

    @Test
    public void getMessageAmounForUserWithAtInName() throws Exception {
        //GIVEN
        messageStore.saveMessage(USER_WITH_AT, error);
        messageStore.saveMessage(USER_WITH_AT, warning);
        messageStore.saveMessage(USER_WITH_AT, info);

        // WHEN
        long amountWithoutTypeSpecified = messageStore.getMessageAmount(USER_WITH_AT, Lists.<MessageType>newArrayList());

        // THEN
        assertEquals(3, amountWithoutTypeSpecified);
    }

    @Test
    public void fetchMessagesForUserWithAtInName() throws Exception {
        //GIVEN
        messageStore.saveMessage(USER_WITH_AT, error);
        messageStore.saveMessage(USER_WITH_AT, warning);
        messageStore.saveMessage(USER_WITH_AT, info);

        // WHEN
        final List<Message> allMessages = messageStore.getMessages(USER_WITH_AT, Lists.<MessageType>newArrayList(), Maps.<String, Boolean>newHashMap(), 3, 0);

        // THEN
        assertEquals(3, allMessages.size());
        assertThat(allMessages, containsMessages(error, warning, info));
    }

    private Message createMessage(String id, MessageType type) {
        Message message = new Message();
        message.setId(id);
        message.setType(type);
        message.setSender("");
        message.setMessage("");
        message.setSubject("");
        return message;
    }

    private static Matcher<Iterable<? extends Message>> containsMessages(Message... ids) {
        final List<Matcher<? super Message>> itemMatchers = Lists.newLinkedList();
        for (final Message id : ids) {
            itemMatchers.add(new IsMessageWithSameIdAndType<>(id));
        }
        return new IsIterableContainingInOrder<>(itemMatchers);
    }

    private static class IsMessageWithSameIdAndType<T extends Message> extends TypeSafeMatcher<T> {

        private Message task;

        public IsMessageWithSameIdAndType(T task) {
            this.task = task;
        }

        @Override
        protected boolean matchesSafely(T item) {
            return task.getId().equals(item.getId()) && task.getType() == item.getType();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format("Task with id=%s and type=%s", task.getId(), task.getType()));
        }

        @Override
        protected void describeMismatchSafely(T item, Description mismatchDescription) {
            mismatchDescription.appendText(String.format("Task with id=%s and status=%s", item.getId(), item.getType()));
        }
    }
}
