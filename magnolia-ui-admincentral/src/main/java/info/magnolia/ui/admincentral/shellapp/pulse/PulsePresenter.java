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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.event.EventBus;
import info.magnolia.task.TaskEvent;
import info.magnolia.task.TaskEventHandler;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagePresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.message.PulseMessagesPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.task.PulseTasksPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.task.TaskPresenter;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Presenter of {@link PulseView}.
 */
public final class PulsePresenter implements PulseView.Listener, PulseMessagesPresenter.Listener, PulseTasksPresenter.Listener, MessagePresenter.Listener, MessageEventHandler, TaskEventHandler {

    private PulseView view;
    private PulseMessagesPresenter messagesPresenter;
    private PulseTasksPresenter tasksPresenter;
    private MessagePresenter detailMessagePresenter;
    private TaskPresenter detailTaskPresenter;
    private ShellImpl shell;
    private ItemCategory selectedCategory;
    private boolean isDisplayingDetailView;

    @Inject
    public PulsePresenter(@Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus, final PulseView view, final ShellImpl shell,
            final PulseMessagesPresenter messagesPresenter, final PulseTasksPresenter tasksPresenter, final MessagePresenter detailMessagePresenter,
            final TaskPresenter detailTaskPresenter) {
        this.view = view;
        this.messagesPresenter = messagesPresenter;
        this.tasksPresenter = tasksPresenter;
        this.detailMessagePresenter = detailMessagePresenter;
        this.detailTaskPresenter = detailTaskPresenter;
        this.shell = shell;
        admincentralEventBus.addHandler(MessageEvent.class, this);
        admincentralEventBus.addHandler(TaskEvent.class, this);

        updatePendingMessagesAndTasksCount();
    }

    public View start() {
        view.setListener(this);
        messagesPresenter.setListener(this);
        tasksPresenter.setListener(this);
        detailMessagePresenter.setListener(this);
        detailTaskPresenter.setListener(this);

        view.setPulseSubView(messagesPresenter.start());

        return view;
    }

    @Override
    public void onCategoryChange(ItemCategory category) {
        selectedCategory = category;
        showList();
    }

    @Override
    public void openMessage(String messageId) {
        view.setPulseSubView(detailMessagePresenter.start(messageId));
        isDisplayingDetailView = true;
    }

    @Override
    public void showList() {
        if (selectedCategory == ItemCategory.TASKS) {
            view.setPulseSubView(tasksPresenter.start());
        } else {
            view.setPulseSubView(messagesPresenter.start());
        }
        isDisplayingDetailView = false;
    }

    @Override
    public void messageSent(MessageEvent event) {
        view.updateCategoryBadgeCount(ItemCategory.MESSAGES, messagesPresenter.getNumberOfUnclearedMessagesForCurrentUser());
    }

    @Override
    public void messageCleared(MessageEvent event) {
        view.updateCategoryBadgeCount(ItemCategory.MESSAGES, messagesPresenter.getNumberOfUnclearedMessagesForCurrentUser());
    }

    @Override
    public void messageRemoved(MessageEvent messageEvent) {
        view.updateCategoryBadgeCount(ItemCategory.MESSAGES, messagesPresenter.getNumberOfUnclearedMessagesForCurrentUser());
    }

    @Override
    public void openTask(String taskId) {
        view.setPulseSubView(detailTaskPresenter.start(taskId));
        isDisplayingDetailView = true;
    }

    @Override
    public void taskClaimed(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
    }

    @Override
    public void taskAdded(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
    }

    @Override
    public void taskRemoved(TaskEvent taskEvent) {
        // nothing to do here
    }

    @Override
    public void taskCompleted(TaskEvent taskEvent) {
        // nothing to do here
    }

    @Override
    public void taskFailed(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
    }

    public boolean isDisplayingDetailView() {
        return isDisplayingDetailView;
    }

    private void updatePendingMessagesAndTasksCount() {
        int unclearedMessages = messagesPresenter.getNumberOfUnclearedMessagesForCurrentUser();
        int pendingTasks = tasksPresenter.getNumberOfPendingTasksForCurrentUser();

        shell.setIndication(ShellAppType.PULSE, unclearedMessages + pendingTasks);

        view.updateCategoryBadgeCount(ItemCategory.MESSAGES, unclearedMessages);
        view.updateCategoryBadgeCount(ItemCategory.TASKS, pendingTasks);
    }

    @Override
    public void updateDetailView(final String itemId) {
        if (selectedCategory == ItemCategory.TASKS) {
            openTask(itemId);
        } else {
            openMessage(itemId);
        }
    }
}
