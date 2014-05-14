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

import static info.magnolia.ui.admincentral.shellapp.pulse.item.AbstractPulseItemView.GROUP_PLACEHOLDER_ITEMID;

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.task.Task;
import info.magnolia.task.Task.Status;
import info.magnolia.task.TasksManager;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemCategory;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * Presenter of {@link PulseTasksView}.
 */
public final class PulseTasksPresenter implements PulseTasksView.Listener {

    public static final String NEW_PROPERTY_ID = "new";
    public static final String TASK_PROPERTY_ID = "task";
    public static final String STATUS_PROPERTY_ID = "status";
    public static final String SENDER_PROPERTY_ID = "sender";
    public static final String LAST_CHANGE_PROPERTY_ID = "date";
    public static final String SENT_TO_PROPERTY_ID = "sentTo";
    public static final String ASSIGNED_TO_PROPERTY_ID = "assignedTo";

    private static final Logger log = LoggerFactory.getLogger(PulseTasksPresenter.class);

    private final PulseTasksView view;

    private HierarchicalContainer container;

    private final TasksManager tasksManager;

    private final ShellImpl shell;

    private boolean grouping = false;
    private Listener listener;
    private SimpleTranslator i18n;

    @Inject
    public PulseTasksPresenter(final PulseTasksView view, final ShellImpl shellImpl, final TasksManager tasksManager, final SimpleTranslator i18n) {
        this.view = view;
        this.shell = shellImpl;
        this.tasksManager = tasksManager;
        this.i18n = i18n;
    }

    public View start() {
        view.setListener(this);
        view.setTaskListener(this);
        initView();
        return view;
    }

    private void initView() {
        container = createTaskDataSource();
        view.setDataSource(container);
        view.refresh();
        for (Status status : Status.values()) {
            doTasksStatusUpdate(status, 0);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private HierarchicalContainer createTaskDataSource() {
        container = new HierarchicalContainer();
        container.addContainerProperty(NEW_PROPERTY_ID, Boolean.class, true);
        container.addContainerProperty(TASK_PROPERTY_ID, String.class, null);
        container.addContainerProperty(STATUS_PROPERTY_ID, Status.class, Status.Created);
        container.addContainerProperty(SENDER_PROPERTY_ID, String.class, null);
        container.addContainerProperty(ASSIGNED_TO_PROPERTY_ID, String.class, null);
        container.addContainerProperty(SENT_TO_PROPERTY_ID, String.class, null);
        container.addContainerProperty(LAST_CHANGE_PROPERTY_ID, Date.class, null);

        createSuperItems();

        for (Task task : tasksManager.findTasksByUserAndStatus(MgnlContext.getUser().getName(), Arrays.asList(Status.Created, Status.InProgress, Status.Completed, Status.Failed))) {
            addTaskAsItem(task);
        }

        container.addContainerFilter(sectionFilter);

        return container;
    }

    private void createSuperItems() {
        for (Status status : Status.values()) {
            Object itemId = getSuperItem(status);
            Item item = container.addItem(itemId);
            item.getItemProperty(STATUS_PROPERTY_ID).setValue(status);
            container.setChildrenAllowed(itemId, true);
        }

    }

    private void clearSuperItemFromTasks() {
        for (Object itemId : container.getItemIds()) {
            container.setParent(itemId, null);
        }
    }

    private Object getSuperItem(Status status) {
        return GROUP_PLACEHOLDER_ITEMID + status;
    }

    /*
     * Sets the grouping of messages
     */
    @Override
    public void setGrouping(boolean checked) {
        grouping = checked;

        clearSuperItemFromTasks();
        container.removeContainerFilter(sectionFilter);

        if (checked) {
            buildTree();
        }

        container.addContainerFilter(sectionFilter);
    }

    /**
     * Return list of child items.
     */
    @Override
    public Collection<?> getGroup(Object itemId) {
        return container.getChildren(itemId);
    }

    /**
     * Return parent itemId for an item.
     */
    @Override
    public Object getParent(Object itemId) {
        return container.getParent(itemId);
    }

    /*
     * This filter hides grouping titles when
     * grouping is not on or group would be empty
     */
    private Filter sectionFilter = new Filter() {

        @Override
        public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
            if (itemId.toString().startsWith(GROUP_PLACEHOLDER_ITEMID) && (!grouping || isTypeGroupEmpty(itemId))) {
                return false;
            }

            return true;
        }

        @Override
        public boolean appliesToProperty(Object propertyId) {
            return STATUS_PROPERTY_ID.equals(propertyId);
        }

        private boolean isTypeGroupEmpty(Object typeId) {
            return container.getChildren(typeId) == null || container.getChildren(typeId).isEmpty();
        }

    };

    /*
     * Assign messages under correct parents so that
     * grouping works.
     */
    private void buildTree() {
        for (Object itemId : container.getItemIds()) {
            // Skip super items
            if (!itemId.toString().startsWith(GROUP_PLACEHOLDER_ITEMID)) {
                Item item = container.getItem(itemId);

                Status status = (Status) item.getItemProperty(STATUS_PROPERTY_ID).getValue();
                Object parentItemId = getSuperItem(status);
                Item parentItem = container.getItem(parentItemId);
                if (parentItem != null) {
                    container.setParent(itemId, parentItemId);
                }
            }
        }
    }

    private void addTaskAsItem(Task task) {
        log.debug("Adding task {}", task);
        final String taskId = String.valueOf(task.getId());
        final Item item = container.addItem(taskId);
        container.setChildrenAllowed(taskId, false);
        assignPropertiesFromTask(task, item);
    }

    @SuppressWarnings("unchecked")
    private void assignPropertiesFromTask(Task task, final Item item) {
        if (item != null && task != null) {
            item.getItemProperty(NEW_PROPERTY_ID).setValue(task.getStatus() == Status.Created);
            item.getItemProperty(TASK_PROPERTY_ID).setValue(getTaskTitle(task));
            item.getItemProperty(SENDER_PROPERTY_ID).setValue(task.getRequestor());
            item.getItemProperty(LAST_CHANGE_PROPERTY_ID).setValue(task.getModificationDate());
            item.getItemProperty(STATUS_PROPERTY_ID).setValue(task.getStatus());
            item.getItemProperty(ASSIGNED_TO_PROPERTY_ID).setValue(StringUtils.defaultString(task.getActorId()));
            item.getItemProperty(SENT_TO_PROPERTY_ID).setValue(StringUtils.join(task.getGroupIds(), ",") + "|" + StringUtils.join(task.getActorIds(), ","));
        }
    }

    @Override
    public void filterByItemCategory(ItemCategory category) {
        if (container != null) {
            container.removeAllContainerFilters();
            container.addContainerFilter(sectionFilter);
            applyCategoryFilter(category);
        }
    }

    private void applyCategoryFilter(final ItemCategory category) {
        final Filter filter = new Filter() {

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
                final Status type = (Status) item.getItemProperty(STATUS_PROPERTY_ID).getValue();

                switch (category) {
                case UNCLAIMED:
                    return type == Status.Created;
                case ONGOING:
                    return type == Status.InProgress;
                case DONE:
                    return type == Status.Completed;
                case FAILED:
                    return type == Status.Failed;
                default:
                    return true;
                }
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return STATUS_PROPERTY_ID.equals(propertyId);
            }

        };
        container.addContainerFilter(filter);
    }

    @Override
    public void onItemClicked(String itemId) {
        listener.openTask(itemId);
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

    private void doTasksStatusUpdate(final Task.Status status, int decrementOrIncrement) {

        shell.updateShellAppIndication(ShellAppType.PULSE, decrementOrIncrement);

        final String userName = MgnlContext.getUser().getName();
        int count = 0;

        switch (status) {

        case Created:
            count = tasksManager.findTasksByUserAndStatus(userName, Arrays.asList(Status.Created)).size();
            view.updateCategoryBadgeCount(ItemCategory.UNCLAIMED, count);
            break;
        case InProgress:
            count = tasksManager.findTasksByUserAndStatus(userName, Arrays.asList(Status.InProgress)).size();
            view.updateCategoryBadgeCount(ItemCategory.ONGOING, count);
            break;
        case Failed:
            count = tasksManager.findTasksByUserAndStatus(userName, Arrays.asList(Status.Failed)).size();
            view.updateCategoryBadgeCount(ItemCategory.FAILED, count);
            break;
        default:
            break;
        }
    }

    /*
     * Default visibility for testing purposes only.
     */
    String getTaskTitle(final Task task) {
        String subject = (String) task.getContent().get("subject");
        String repo = (String) task.getContent().get("repository");
        String path = (String) task.getContent().get("path");

        // fallback to task name in case subject is not available
        String title = subject != null ? i18n.translate(subject, repo, path) : task.getName();
        return title + "|" + StringUtils.defaultString(task.getComment());
    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        public void openTask(String taskId);
    }

    public int getNumberOfPendingTasksForCurrentUser() {
        return tasksManager.findTasksByUserAndStatus(MgnlContext.getUser().getName(), Arrays.asList(Status.Created)).size();
    }

    public void setTabActive(ItemCategory category) {
        view.setTabActive(category);
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
                shell.openNotification(MessageStyleTypeEnum.WARNING, true, i18n.translate("pulse.tasks.cantAssign", task.getName(), task.getStatus()));
                return;
            }
            tasksManager.claim(taskId, userId);
        }

        // refresh the view
        initView();
    }

}
