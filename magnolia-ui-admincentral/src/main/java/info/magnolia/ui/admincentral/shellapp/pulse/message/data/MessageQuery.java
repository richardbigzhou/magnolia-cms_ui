/**
 * This file Copyright (c) 2015-2016 Magnolia International
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

import info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseQuery;
import info.magnolia.ui.admincentral.shellapp.pulse.data.PulseConstants;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;

/**
 * {@link info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseQuery} implementation which serves {@link Message} objects via {@link MessagesManager}.
 */
public class MessageQuery extends LazyPulseQuery<MessageType, Message> {

    private MessagesManager messagesManager;

    @Inject
    public MessageQuery(MessageQueryDefinition queryDefinition, MessagesManager messagesManager) {
        super(queryDefinition);
        this.messagesManager = messagesManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void mapObjectToItem(Message object, Item item) {
        item.addItemProperty(MessageConstants.ID, new ObjectProperty(object.getId(), String.class));
        item.addItemProperty(MessageConstants.NEW_PROPERTY_ID, new ObjectProperty(!object.isCleared(), Boolean.class));
        item.addItemProperty(MessageConstants.SUBJECT_PROPERTY_ID, new ObjectProperty(object.getSubject(), String.class));
        item.addItemProperty(MessageConstants.TYPE_PROPERTY_ID, new ObjectProperty(object.getType(), MessageType.class));
        item.addItemProperty(MessageConstants.TEXT_PROPERTY_ID, new ObjectProperty(object.getMessage(), String.class));
        item.addItemProperty(MessageConstants.SENDER_PROPERTY_ID, new ObjectProperty(object.getSender(), String.class));
        item.addItemProperty(MessageConstants.DATE_PROPERTY_ID, new ObjectProperty(object.getTimestamp(), Date.class));
    }

    @Override
    protected long getEntriesAmount(List<MessageType> types) {
        return messagesManager.getMessagesAmount(getQueryDefinition().userName(), types);
    }

    @Override
    protected List<Message> getEntries(List<MessageType> types, int limit, int offset) {
        return messagesManager.getMessageBatch(getQueryDefinition().userName(), types, getSortCriteria(), limit, offset);
    }

    @Override
    protected Message createGroupingEntry(MessageType type) {
        Message grouperMessage = new Message();
        grouperMessage.setId(PulseConstants.GROUP_PLACEHOLDER_ITEMID + type.name());
        grouperMessage.setType(type);
        return grouperMessage;
    }

}
