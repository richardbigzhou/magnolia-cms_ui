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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.task.Task;
import info.magnolia.task.Task.Status;
import info.magnolia.task.TasksManager;
import info.magnolia.task.definition.TaskDefinition;
import info.magnolia.task.definition.registry.TaskDefinitionRegistry;
import info.magnolia.ui.admincentral.shellapp.pulse.item.AbstractItemsPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemsPresenter;
import info.magnolia.ui.api.pulse.task.ItemPresenter;
import info.magnolia.ui.api.pulse.task.TaskPresenter;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;

import com.vaadin.data.util.HierarchicalContainer;

/**
 * Presenter of {@link PulseTasksView}.
 */
public final class PulseTasksPresenter extends AbstractItemsPresenter<Task, PulseTasksPresenter.Listener> implements PulseTasksView.Listener, ItemPresenter.Listener {


    private final PulseTasksView view;
    private final TasksManager tasksManager;
    private final ShellImpl shell;
    private final TaskDefinitionRegistry taskDefinitionRegistry;
    private final ComponentProvider componentProvider;
    private final SimpleTranslator i18n;

    @Inject
    public PulseTasksPresenter(final PulseTasksView view, final TasksContainer container, final ShellImpl shellImpl, final TasksManager tasksManager,
                               final TaskDefinitionRegistry taskDefinitionRegistry, final ComponentProvider componentProvider, final SimpleTranslator i18n) {
        super(container);
        this.view = view;
        this.shell = shellImpl;
        this.tasksManager = tasksManager;
        this.taskDefinitionRegistry = taskDefinitionRegistry;
        this.componentProvider = componentProvider;
        this.i18n = i18n;
    }

    @Override
    public View start() {
        view.setListener(this);
        view.setTaskListener(this);
        initView();
        return view;
    }

    @Override
    public View openItem(String itemId) throws RegistrationException {
        Task task = tasksManager.getTaskById(itemId);
        TaskDefinition definition = taskDefinitionRegistry.get(task.getName());

        TaskPresenter taskPresenter = componentProvider.newInstance(definition.getPresenterClass(), task, definition);
        taskPresenter.setListener(this);
        return taskPresenter.start();

    }

    private void initView() {
        Collection<Task> tasks = tasksManager.findTasksByUserAndStatus(MgnlContext.getUser().getName(), Arrays.asList(Task.Status.Created, Task.Status.InProgress, Task.Status.Completed, Task.Status.Failed));
        HierarchicalContainer dataSource = container.createDataSource(tasks);
        view.setDataSource(dataSource);
        view.refresh();
        for (Status status : Status.values()) {
            doTasksStatusUpdate(status);
        }
    }

    @Override
    public void deleteItems(final Set<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }

        for (String taskId : itemIds) {
            Task task = tasksManager.getTaskById(taskId);
            if (task.getStatus() != Status.Completed) {
                // log warn/info?
                shell.openNotification(MessageStyleTypeEnum.WARNING, true, i18n.translate("pulse.tasks.cantRemove", task.getName()));
                return;
            }
            tasksManager.removeTask(taskId);
        }

        // refresh the view
        initView();
    }

    @Override
    public void onItemClicked(String itemId) {
        listener.openTask(itemId);
    }

    @Override
    public void updateDetailView(String itemId) {
        listener.openTask(itemId);
    }

    @Override
    public void claimTask(final Set<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        final String userId = MgnlContext.getUser().getName();

        for (String taskId : itemIds) {
            Task task = tasksManager.getTaskById(taskId);
            if (task.getStatus() != Status.Created) {
                // log warn/info?
                shell.openNotification(MessageStyleTypeEnum.WARNING, true, i18n.translate("pulse.tasks.cantAssign", task.getName()));
                return;
            }
            tasksManager.claim(taskId, userId);
        }

        // refresh the view
        initView();
    }

    private void doTasksStatusUpdate(final Task.Status status) {

        final String userName = MgnlContext.getUser().getName();
        int count;

        switch (status) {

        case Created:
            count = tasksManager.findTasksByUserAndStatus(userName, Arrays.asList(Status.Created)).size();
            view.updateCategoryBadgeCount(ItemCategory.UNCLAIMED, count);
            break;
        case InProgress:
            count = tasksManager.findTasksByAssigneeAndStatus(userName, Arrays.asList(Status.InProgress)).size();
            view.updateCategoryBadgeCount(ItemCategory.ONGOING, count);
            break;
        case Failed:
            count = tasksManager.findTasksByAssigneeAndStatus(userName, Arrays.asList(Status.Failed)).size();
            view.updateCategoryBadgeCount(ItemCategory.FAILED, count);
            break;
        default:
            break;
        }
    }

    public int getNumberOfPendingTasksForCurrentUser() {
        return tasksManager.findTasksByUserAndStatus(MgnlContext.getUser().getName(), Arrays.asList(Status.Created, Status.Failed)).size();
    }

    public void setTabActive(ItemCategory category) {
        view.setTabActive(category);
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener extends ItemsPresenter.Listener {
        public void openTask(String taskId);
    }

}
