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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import info.magnolia.context.Context;
import info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseListContainer;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageQueryDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageQueryFactory;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Iterables;

/**
 * {@link info.magnolia.ui.admincentral.shellapp.pulse.data.LazyPulseListContainer} implementation capable of serving {@link info.magnolia.ui.api.message.Message} objects via
 * {@link MessagesManager}. {@link MessageType} enumeration is used as a grouping criteria.
 */
public class MessagesContainer extends LazyPulseListContainer<MessageType, MessageQueryDefinition, MessageQueryFactory> {

    private final MessagesManager messagesManager;

    @Inject
    public MessagesContainer(MessagesManager messagesManager, Context ctx,
                             MessageQueryDefinition messageQueryDefinition,
                             Provider<MessageQueryFactory> messageQueryFactoryProvider) {
        super(messageQueryDefinition, messageQueryFactoryProvider, ctx.getUser().getName());
        this.messagesManager = messagesManager;
    }

    @Override
    public void filterByItemCategory(PulseItemCategory category) {
        MessageType[] newVisibleTypes;
        switch (category) {
        case PROBLEM:
            newVisibleTypes = new MessageType[]{MessageType.ERROR, MessageType.WARNING};
            break;
        case INFO:
            newVisibleTypes = new MessageType[]{MessageType.INFO};
            break;
        default:
            newVisibleTypes = MessageType.values();
        }

        List<MessageType> newVisibleTypeList = Arrays.asList(newVisibleTypes);
        if (!Iterables.elementsEqual(newVisibleTypeList, getQueryDefinition().types())) {
            getQueryDefinition().setTypes(newVisibleTypeList);
            refresh();
        }
    }

    @Override
    public long size() {
        return messagesManager.getMessagesAmount(getUserName(), getQueryDefinition().types());
    }
}

