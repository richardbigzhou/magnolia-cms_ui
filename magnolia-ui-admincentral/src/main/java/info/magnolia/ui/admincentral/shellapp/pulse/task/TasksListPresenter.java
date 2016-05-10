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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import info.magnolia.context.Context;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.Task;
import info.magnolia.task.Task.Status;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.TaskDefinition;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import info.magnolia.task.event.TaskEvent;
import info.magnolia.task.event.TaskEventHandler;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ConfiguredPulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.footer.PulseListFooterPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.footer.PulseListFooterView;
import info.magnolia.ui.admincentral.shellapp.pulse.task.definition.TaskUiDefinition;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter of {@link TasksListView}.
 */
public final class TasksListPresenter extends AbstractPulseListPresenter implements TasksListView.Listener, PulseDetailPresenter.Listener, TaskEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TasksListPresenter.class);

    private final TasksListView view;
    private final TasksManager tasksManager;
    private final ShellImpl shell;
    private final TaskDefinitionRegistry taskDefinitionRegistry;
    private final ComponentProvider componentProvider;
    private final SimpleTranslator i18n;
    private final String userId;

    /**
     * @deprecated since 5.4.3, use {@link TasksListPresenter#TasksListPresenter(TasksContainer, TasksListView, ShellImpl, TasksManager, TaskDefinitionRegistry,
     * ComponentProvider, SimpleTranslator, Context, EventBus, ConfiguredPulseListDefinition, AvailabilityChecker, PulseListActionExecutor, PulseListFooterPresenter, I18nizer)} instead.
     */
    @Deprecated
    public TasksListPresenter(final TasksContainer container, final TasksListView view, final ShellImpl shellImpl, final TasksManager tasksManager,
            final TaskDefinitionRegistry taskDefinitionRegistry, final ComponentProvider componentProvider, final SimpleTranslator i18n, Context context,
            @Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus, ConfiguredPulseListDefinition definition) {
        this(container, view, shellImpl, tasksManager, taskDefinitionRegistry, componentProvider, i18n, context, admincentralEventBus, definition,
                componentProvider.getComponent(AvailabilityChecker.class), componentProvider.getComponent(PulseListActionExecutor.class),
                componentProvider.getComponent(PulseListFooterPresenter.class), componentProvider.getComponent(I18nizer.class));
    }

    @Inject
    public TasksListPresenter(final TasksContainer container, final TasksListView view, final ShellImpl shellImpl, final TasksManager tasksManager,
            final TaskDefinitionRegistry taskDefinitionRegistry, final ComponentProvider componentProvider, final SimpleTranslator i18n, Context context,
            @Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus, ConfiguredPulseListDefinition definition,
            AvailabilityChecker availabilityChecker, PulseListActionExecutor pulseListActionExecutor, PulseListFooterPresenter pulseListFooterPresenter, I18nizer i18nizer) {
        super(container, i18nizer.decorate(definition), availabilityChecker, pulseListActionExecutor, pulseListFooterPresenter);
        this.view = view;
        this.shell = shellImpl;
        this.tasksManager = tasksManager;
        this.taskDefinitionRegistry = taskDefinitionRegistry;
        this.componentProvider = componentProvider;
        this.i18n = i18n;
        this.userId = context.getUser().getName();

        admincentralEventBus.addHandler(TaskEvent.class, this);
    }

    @Override
    public View start() {
        view.setListener(this);
        view.setDataSource(container.getVaadinContainer());
        PulseListFooterView footer = pulseListFooterPresenter.start(definition.getBulkActions(), getTotalEntriesAmount());
        view.setFooter(footer);

        updateView();
        filterByItemCategory(PulseItemCategory.UNCLAIMED);

        return view;
    }

    @Override
    public View openItem(String itemId) throws RegistrationException {
        Task task = tasksManager.getTaskById(itemId);
        TaskDefinition definition = taskDefinitionRegistry.get(task.getName());

        TaskDetailPresenter taskPresenter;
        if (definition instanceof TaskUiDefinition) {
            taskPresenter = componentProvider.newInstance(((TaskUiDefinition) definition).getPresenterClass(), task, definition);
        } else {
            log.debug("Task definition is not an instance of TaskUiDefinition, the presenter can not be configured.");
            taskPresenter = componentProvider.newInstance(DefaultTaskDetailPresenter.class, task, definition);
        }
        taskPresenter.setListener(this);
        return taskPresenter.start();

    }

    @Override
    public long getTotalEntriesAmount() {
        return container.size();
    }

    @Override
    public void onItemClicked(String itemId) {
        listener.openItem(definition.getName(), itemId);
    }

    @Override
    public void updateDetailView(String itemId) {
        listener.openItem(definition.getName(), itemId);
    }

    @Override
    @Deprecated
    public void deleteItems(final Set<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }

        for (String taskId : itemIds) {
            Task task = tasksManager.getTaskById(taskId);
            if (task.getStatus() != Status.Resolved) {
                // log warn/info?
                shell.openNotification(MessageStyleTypeEnum.WARNING, true, i18n.translate("pulse.tasks.cantRemove", task.getName()));
                return;
            }
            tasksManager.archiveTask(taskId);
        }

        refresh();
    }

    @Override
    @Deprecated
    public void claimTask(final Set<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        for (String taskId : itemIds) {
            Task task = tasksManager.getTaskById(taskId);
            if (task.getStatus() != Status.Created) {
                // log warn/info?
                shell.openNotification(MessageStyleTypeEnum.WARNING, true, i18n.translate("pulse.tasks.cantAssign", task.getName(), task.getStatus()));
                return;
            }
            tasksManager.claim(taskId, userId);
        }
        refresh();
    }

    @Override
    public PulseItemCategory getCategory() {
        return PulseItemCategory.TASKS;
    }

    @Override
    public void taskClaimed(TaskEvent taskEvent) {
        refresh();
    }

    @Override
    public void taskAdded(TaskEvent taskEvent) {
        refresh();
        listener.updateView(PulseItemCategory.TASKS);
        view.setTabActive(PulseItemCategory.UNCLAIMED);
    }

    @Override
    public void taskResolved(TaskEvent taskEvent) {
        refresh();
        listener.updateView(PulseItemCategory.TASKS);
        view.setTabActive(PulseItemCategory.UNCLAIMED);
    }

    @Override
    public void taskFailed(TaskEvent taskEvent) {
        refresh();
        listener.updateView(PulseItemCategory.TASKS);
        view.setTabActive(PulseItemCategory.FAILED);
    }

    @Override
    public void taskArchived(TaskEvent taskEvent) {
        refreshData();
    }

    @Override
    public void taskRemoved(TaskEvent taskEvent) {
    }

    @Override
    public void taskScheduled(TaskEvent taskEvent) {
        refresh();
        listener.updateView(PulseItemCategory.TASKS);
        view.setTabActive(PulseItemCategory.SCHEDULED);
    }

    private void refresh() {
        listener.updatePulseCounter();
        container.refresh();
        updateView();
    }

    @Override
    public int getPendingItemCount() {
        return tasksManager.findPendingTasksByUser(userId).size();
    }

    private void updateView() {
        for (Status status : Status.values()) {
            doTasksStatusUpdate(status);
        }
    }

    private void doTasksStatusUpdate(final Task.Status status) {

        int count;

        switch (status) {

        case Created:
            count = tasksManager.findTasksByUserAndStatus(userId, Arrays.asList(Status.Created)).size();
            view.updateCategoryBadgeCount(PulseItemCategory.UNCLAIMED, count);
            break;
        case InProgress:
            count = tasksManager.findTasksByAssigneeAndStatus(userId, Arrays.asList(Status.InProgress)).size();
            view.updateCategoryBadgeCount(PulseItemCategory.ONGOING, count);
            break;
        case Failed:
            count = tasksManager.findTasksByAssigneeAndStatus(userId, Arrays.asList(Status.Failed)).size();
            view.updateCategoryBadgeCount(PulseItemCategory.FAILED, count);
            break;
        case Scheduled:
            count = tasksManager.findTasksByAssigneeAndStatus(userId, Arrays.asList(Status.Scheduled)).size();
            view.updateCategoryBadgeCount(PulseItemCategory.SCHEDULED, count);
            break;
        default:
            break;
        }
    }

    @Override
    protected List<Object> getSelectedItemIds() {
        return view.getSelectedItemIds();
    }

}
