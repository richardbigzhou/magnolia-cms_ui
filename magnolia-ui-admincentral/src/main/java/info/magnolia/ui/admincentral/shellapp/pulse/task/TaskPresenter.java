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
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemActionExecutor;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.registry.ItemViewDefinitionRegistry;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.task.Task;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.framework.task.TasksStore;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.util.BeanItem;

/**
 * The task detail presenter.
 */
public final class TaskPresenter implements ItemView.Listener, ActionbarPresenter.Listener {

    private final ItemView view;
    private TasksStore tasksStore;
    private ItemActionExecutor itemActionExecutor;
    private ItemViewDefinitionRegistry itemViewDefinitionRegistry;
    private FormBuilder formbuilder;
    private ActionbarPresenter actionbarPresenter;
    private Listener listener;
    private Task task;
    private I18nizer i18nizer;

    @Inject
    public TaskPresenter(ItemView view, TasksStore tasksStore, ItemActionExecutor itemActionExecutor, ItemViewDefinitionRegistry itemViewDefinitionRegistry, FormBuilder formbuilder, ActionbarPresenter actionbarPresenter, I18nizer i18nizer) {
        this.view = view;
        this.tasksStore = tasksStore;
        this.itemActionExecutor = itemActionExecutor;
        this.itemViewDefinitionRegistry = itemViewDefinitionRegistry;
        this.formbuilder = formbuilder;
        this.actionbarPresenter = actionbarPresenter;
        this.i18nizer = i18nizer;

        view.setListener(this);
        actionbarPresenter.setListener(this);
    }

    public View start(String taskId) {
        final String userId = MgnlContext.getUser().getName();
        this.task = tasksStore.getTaskById(Long.valueOf(taskId));
        if (this.task == null) {
            throw new RuntimeException("Could not retrieve task with id [" + taskId + "] for user [" + userId + "]");
        }
        String taskView = "ui-admincentral:default";
        view.setTitle(task.getName());
        try {
            // uses the task name to map a view with the same name.
            // TODO this is hardcoded and just temporary, until we'll have TaskRegistry
            final String specificTaskView = "pages:" + task.getName();
            if (StringUtils.isNotEmpty(specificTaskView)) {
                taskView = specificTaskView;
            }
            ItemViewDefinition itemViewDefinition = itemViewDefinitionRegistry.get(taskView);
            itemViewDefinition = i18nizer.decorate(itemViewDefinition);

            itemActionExecutor.setMessageViewDefinition(itemViewDefinition);
            BeanItem<Task> taskItem = new BeanItem<Task>(task);

            View mView = formbuilder.buildView(itemViewDefinition.getForm(), taskItem);
            view.setItemView(mView);

            view.setActionbarView(actionbarPresenter.start(itemViewDefinition.getActionbar(), itemViewDefinition.getActions()));
        } catch (RegistrationException e) {
            throw new RuntimeException("Could not retrieve messageView for " + taskView, e);
        }
        return view;
    }

    @Override
    public void onNavigateToList() {
        listener.showList();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onActionbarItemClicked(String actionName) {
        try {
            itemActionExecutor.execute(actionName, task, this, itemActionExecutor);

        } catch (ActionExecutionException e) {
            throw new RuntimeException("Could not execute action " + actionName, e);
        }
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        void showList();
    }
}
