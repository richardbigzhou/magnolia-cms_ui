/**
 * This file Copyright (c) 2014 Magnolia International
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


import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageConstants;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * {@link MessagesManager} mock utility class, provides convenient builder to mimic the message content via provided lists.
 */
public class MessagesManagerMock {

    private List<Message> errors;
    private List<Message> warnings;
    private List<Message> infos;

    /**
     * Private c-tor, {@link Builder} is used to construct an instance.
     */
    private MessagesManagerMock(List<Message> errors, List<Message> warnings, List<Message> infos) {
        this.errors = errors;
        this.warnings = warnings;
        this.infos = infos;
    }

    MessagesManager createMockMessagesManager() {
        MessagesManager messagesManager = mock(MessagesManager.class);

        doAnswer(new Answer() {
            @SuppressWarnings("unchecked")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final List<MessageType> types = (List<MessageType>) invocation.getArguments()[1];
                final Map<String, Boolean> sortCriteria = (Map<String, Boolean>) invocation.getArguments()[2];
                int limit = (Integer) invocation.getArguments()[3];
                int offset = (Integer) invocation.getArguments()[4];
                return getMockList(types, sortCriteria).subList(offset, offset + limit);
            }
        }).when(messagesManager).getMessageBatch(anyString(), anyListOf(MessageType.class), anyMapOf(String.class, Boolean.class), anyInt(), anyInt());

        doAnswer(new Answer() {
            @Override
            @SuppressWarnings("unchecked")
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return getMockList((List<MessageType>) invocation.getArguments()[1], Collections.<String, Boolean>emptyMap()).size();
            }
        }).when(messagesManager).getMessagesAmount(anyString(), anyListOf(MessageType.class));

        return messagesManager;
    }

    private List<Message> getMockList(List<MessageType> types, Map<String, Boolean> sortCriteria) {
        final List<Message> list = Lists.newArrayList();
        if (types.contains(MessageType.ERROR)) {
            list.addAll(errors);
        }

        if (types.contains(MessageType.WARNING)) {
            list.addAll(warnings);
        }

        if (types.contains(MessageType.INFO)) {
            list.addAll(infos);
        }

        if (sortCriteria.containsKey(MessageConstants.ID)) {
            final boolean asc = sortCriteria.get(MessageConstants.ID);
            Collections.sort(list, new Comparator<Message>() {
                @Override
                public int compare(Message o1, Message o2) {
                    int comparisonResult = o1.getId().compareTo(o2.getId());
                    return asc ? comparisonResult : -comparisonResult;
                }
            });
        }
        return list;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Message> errors;
        private List<Message> warnings;
        private List<Message> infos;

        public Builder withErrorsWithIds(Object... ids) {
            this.errors = Lists.transform(Arrays.asList(ids), new IdToMessage(MessageType.ERROR));
            return this;
        }

        public Builder withWarningsWithIds(Object... ids) {
            this.warnings = Lists.transform(Arrays.asList(ids), new IdToMessage(MessageType.WARNING));
            return this;
        }

        public Builder withInfosWithIds(Object... ids) {
            this.infos = Lists.transform(Arrays.asList(ids), new IdToMessage(MessageType.INFO));
            return this;
        }

        public MessagesManager build() {
            return new MessagesManagerMock(errors, warnings, infos).createMockMessagesManager();
        }

        private static class IdToMessage implements Function<Object, Message> {

            private MessageType type;

            public IdToMessage(MessageType type) {
                this.type = type;
            }

            @Nullable
            @Override
            public Message apply(@Nullable Object input) {
                final Message message = new Message();
                message.setType(type);
                message.setId(String.valueOf(input));
                return message;
            }
        }
    }
}
