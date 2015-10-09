/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ConfiguredPulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.footer.PulseListFooterPresenter;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.message.MessagesManager;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Presenter of {@link MessagesListView}.
 */
public final class MessagesListPresenter extends AbstractPulseListPresenter implements MessagesListView.Listener, MessageEventHandler {

    private final MessagesListView view;
    private final MessagesManager messagesManager;
    private final ComponentProvider componentProvider;
    private final String userId;

    /**
     * @deprecated since 5.4.3.
     */
    @Deprecated
    public MessagesListPresenter(MessagesContainer container, @Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus,
            final MessagesListView view, final MessagesManager messagesManager,
            ComponentProvider componentProvider, Context context, ConfiguredPulseListDefinition definition) {
        this(container, admincentralEventBus, view, messagesManager, componentProvider,
                context, definition, componentProvider.getComponent(AvailabilityChecker.class), componentProvider.getComponent(PulseListActionExecutor.class),
                componentProvider.getComponent(PulseListFooterPresenter.class), componentProvider.getComponent(I18nizer.class));
    }

    @Inject
    public MessagesListPresenter(MessagesContainer container, @Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus,
            final MessagesListView view, final MessagesManager messagesManager,
            ComponentProvider componentProvider, Context context, ConfiguredPulseListDefinition definition,
            AvailabilityChecker availabilityChecker, PulseListActionExecutor pulseListActionExecutor, PulseListFooterPresenter pulseListFooterPresenter, I18nizer i18nizer) {
        super(container, i18nizer.decorate(definition), availabilityChecker, pulseListActionExecutor, pulseListFooterPresenter);
        this.view = view;
        this.messagesManager = messagesManager;
        this.componentProvider = componentProvider;
        this.userId = context.getUser().getName();

        admincentralEventBus.addHandler(MessageEvent.class, this);
    }

    @Override
    public View start() {
        view.setListener(this);
        initView();
        return view;
    }

    @Override
    public View openItem(String messageId) {
        Message message = messagesManager.getMessageById(userId, messageId);

        MessageDetailPresenter messagePresenter = componentProvider.newInstance(MessageDetailPresenter.class, message);
        messagePresenter.setListener(this);

        return messagePresenter.start();
    }

    private void initView() {
        view.setDataSource(container.getVaadinContainer());
        for (MessageType type : MessageType.values()) {
            doUnreadMessagesUpdate(type);
        }
        View footer = pulseListFooterPresenter.start(definition.getBulkActions(), getTotalEntriesAmount());
        view.setFooter(footer.asVaadinComponent());
    }

    @Override
    public void messageSent(MessageEvent event) {
        final Message message = event.getMessage();
        container.refresh();

        final MessageType type = message.getType();
        doUnreadMessagesUpdate(type);
        listener.updatePulseCounter();
    }

    @Override
    public void messageCleared(MessageEvent event) {
        final Message message = event.getMessage();
        final MessageType type = message.getType();
        doUnreadMessagesUpdate(type);
        listener.updatePulseCounter();
        container.refresh();
    }

    @Override
    public void deleteItems(final Set<String> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }
        for (String messageId : messageIds) {
            Message message = messagesManager.getMessageById(userId, messageId);
            if (message == null) {
                continue;
            }
            messagesManager.removeMessage(userId, messageId);
        }
    }

    @Override
    public long getTotalEntriesAmount() {
        return container.size();
    }

    @Override
    public void onItemClicked(String messageId) {
        listener.openItem(this.definition.getName(), messageId);
        messagesManager.clearMessage(userId, messageId);
    }

    @Override
    public void updateDetailView(String itemId) {
        listener.openItem(this.definition.getName(), itemId);
    }

    @Override
    public void messageRemoved(MessageEvent messageEvent) {
        container.refresh();
        for (MessageType type : MessageType.values()) {
            doUnreadMessagesUpdate(type);
        }
        listener.updatePulseCounter();
    }

    private void doUnreadMessagesUpdate(final MessageType type) {
        int count;
        switch (type) {
        case ERROR:
        case WARNING:
            count = messagesManager.getNumberOfUnclearedMessagesForUserAndByType(userId, MessageType.ERROR);
            count += messagesManager.getNumberOfUnclearedMessagesForUserAndByType(userId, MessageType.WARNING);
            view.updateCategoryBadgeCount(PulseItemCategory.PROBLEM, count);
            break;
        case INFO:
            count = messagesManager.getNumberOfUnclearedMessagesForUserAndByType(userId, type);
            view.updateCategoryBadgeCount(PulseItemCategory.INFO, count);
            break;
        default:
            break;
        }
    }

    @Override
    public PulseItemCategory getCategory() {
        return PulseItemCategory.MESSAGES;
    }

    @Override
    public int getPendingItemCount() {
        return messagesManager.getNumberOfUnclearedMessagesForUser(userId);
    }

    @Override
    protected List<Object> getSelectedItemIds() {
        return view.getSelectedItemIds();
    }

}
