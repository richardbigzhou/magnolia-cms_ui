/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.event.TaskEvent;
import info.magnolia.task.event.TaskEventHandler;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagesListPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.task.TasksListPresenter;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.message.MessageEvent;
import info.magnolia.ui.framework.message.MessageEventHandler;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter of {@link PulseView}.
 */
public final class PulsePresenter implements PulseListPresenter.Listener, PulseView.Listener, MessagesListPresenter.Listener, TasksListPresenter.Listener, MessageEventHandler, TaskEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PulsePresenter.class);

    private PulseView view;
    private MessagesListPresenter messagesPresenter;
    private TasksListPresenter tasksPresenter;
    private ShellImpl shell;
    private PulseItemCategory selectedCategory = PulseItemCategory.TASKS;
    private boolean isDisplayingDetailView;

    @Inject
    public PulsePresenter(@Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus, final PulseView view, final ShellImpl shell,
            final MessagesListPresenter messagesPresenter, final TasksListPresenter tasksPresenter) {
        this.view = view;
        this.messagesPresenter = messagesPresenter;
        this.tasksPresenter = tasksPresenter;
        this.shell = shell;
        admincentralEventBus.addHandler(MessageEvent.class, this);
        admincentralEventBus.addHandler(TaskEvent.class, this);

        updatePendingMessagesAndTasksCount();
    }

    public View start() {
        view.setListener(this);
        messagesPresenter.setListener(this);
        tasksPresenter.setListener(this);

        view.setPulseSubView(tasksPresenter.start());

        return view;
    }

    @Override
    public void onCategoryChange(PulseItemCategory category) {
        selectedCategory = category;
        showList();
    }

    @Override
    public void openMessage(String messageId) {
        view.setPulseSubView(messagesPresenter.openItem(messageId));
        isDisplayingDetailView = true;
    }

    @Override
    public void showList() {
        if (selectedCategory == PulseItemCategory.TASKS) {
            view.setPulseSubView(tasksPresenter.start());
        } else {
            view.setPulseSubView(messagesPresenter.start());
        }
        isDisplayingDetailView = false;
    }

    @Override
    public void messageSent(MessageEvent event) {
        updatePendingMessagesAndTasksCount();
    }

    @Override
    public void messageCleared(MessageEvent event) {
        updatePendingMessagesAndTasksCount();
    }

    @Override
    public void messageRemoved(MessageEvent messageEvent) {
        updatePendingMessagesAndTasksCount();
    }

    @Override
    public void openTask(String taskId) {
        try {
            view.setPulseSubView(tasksPresenter.openItem(taskId));
            isDisplayingDetailView = true;
        } catch (RegistrationException e) {
            log.error("Could not open detail view for task.", e);
        }
    }

    @Override
    public void taskClaimed(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
    }

    @Override
    public void taskAdded(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
        updateView(PulseItemCategory.UNCLAIMED);
    }

    @Override
    public void taskResolved(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
        updateView(PulseItemCategory.UNCLAIMED);
    }

    @Override
    public void taskFailed(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
        updateView(PulseItemCategory.FAILED);
    }

    @Override
    public void taskArchived(TaskEvent taskEvent) {
        // nothing to do here
    }

    @Override
    public void taskRemoved(TaskEvent taskEvent) {
        // nothing to do here
    }

    @Override
    public void taskScheduled(TaskEvent taskEvent) {
        updatePendingMessagesAndTasksCount();
        updateView(PulseItemCategory.SCHEDULED);
    }

    public boolean isDisplayingDetailView() {
        return isDisplayingDetailView;
    }

    private void updatePendingMessagesAndTasksCount() {
        int unclearedMessages = messagesPresenter.getNumberOfUnclearedMessagesForCurrentUser();
        int pendingTasks = tasksPresenter.getNumberOfPendingTasksForCurrentUser();

        shell.setIndication(ShellAppType.PULSE, unclearedMessages + pendingTasks);

        view.updateCategoryBadgeCount(PulseItemCategory.MESSAGES, unclearedMessages);
        view.updateCategoryBadgeCount(PulseItemCategory.TASKS, pendingTasks);
    }

    /*
     * This method won't show the tasks in the active tab straight away but will do it when clicking on the pulse icon.
     */
    private void updateView(final PulseItemCategory activeTab) {
        // update top navigation and load new tasks
        selectedCategory = PulseItemCategory.TASKS;
        view.setTabActive(PulseItemCategory.TASKS);
        if (isDisplayingDetailView) {
            showList();
        }
        // update sub navigation and filter out everything but what is in the active tab
        tasksPresenter.setTabActive(activeTab);
    }
}
