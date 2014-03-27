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

import static info.magnolia.ui.admincentral.shellapp.pulse.item.PulseItemsView.GROUP_PLACEHOLDER_ITEMID;

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.PulseItemsView;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.task.Task;
import info.magnolia.ui.api.task.Task.Status;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.shell.ShellImpl;
import info.magnolia.ui.framework.task.TasksManager;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

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
    public static final String DATE_PROPERTY_ID = "date";
    public static final String SENT_TO_PROPERTY_ID = "sentTo";
    public static final String ASSIGNED_TO_PROPERTY_ID = "assignedTo";

    private final PulseItemsView view;

    private HierarchicalContainer container;

    private final TasksManager tasksManager;

    private final ShellImpl shell;

    private boolean grouping = false;
    private Listener listener;

    @Inject
    public PulseTasksPresenter(@Named(AdmincentralEventBus.NAME) final EventBus admincentralEventBus, final PulseTasksView view, final ShellImpl shellImpl, final TasksManager tasksManager) {
        this.view = view;
        this.shell = shellImpl;
        this.tasksManager = tasksManager;
        // admincentralEventBus.addHandler(MessageEvent.class, this);

        /*
         * shell.setIndication(
         * ShellAppType.PULSE,
         * tasksManager.getNumberOfUnclearedMessagesForUser(MgnlContext.getUser().getName()));
         */
    }

    public View start() {
        view.setListener(this);
        initView();
        return view;
    }

    private void initView() {
        container = createMessageDataSource();
        view.setDataSource(container);
        view.refresh();
        for (Status status : Status.values()) {
            doTasksStatusUpdate(status, 0);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private HierarchicalContainer createMessageDataSource() {
        container = new HierarchicalContainer();
        container.addContainerProperty(NEW_PROPERTY_ID, Boolean.class, true);
        container.addContainerProperty(TASK_PROPERTY_ID, String.class, null);
        container.addContainerProperty(STATUS_PROPERTY_ID, String.class, null);
        container.addContainerProperty(SENDER_PROPERTY_ID, String.class, null);
        container.addContainerProperty(SENT_TO_PROPERTY_ID, String.class, null);
        container.addContainerProperty(ASSIGNED_TO_PROPERTY_ID, String.class, null);
        container.addContainerProperty(DATE_PROPERTY_ID, Date.class, null);

        createSuperItems();

        /*
         * for (Message message : messagesManager.getMessagesForUser(MgnlContext.getUser().getName())) {
         * addMessageAsItem(message);
         * }
         */

        container.addContainerFilter(sectionFilter);

        return container;
    }

    private void createSuperItems() {
        // TODO iterate over task status
        /*
         * for (MessageType type : MessageType.values()) {
         * Object itemId = getSuperItem(type);
         * Item item = container.addItem(itemId);
         * item.getItemProperty(STATUS_PROPERTY_ID).setValue(type);
         * container.setChildrenAllowed(itemId, true);
         * }
         */
    }

    private void clearSuperItemFromMessages() {
        for (Object itemId : container.getItemIds()) {
            container.setParent(itemId, null);
        }
    }

    private Object getSuperItem(MessageType type) {
        return GROUP_PLACEHOLDER_ITEMID + type;
    }

    /*
     * Sets the grouping of messages
     */
    @Override
    public void setGrouping(boolean checked) {
        grouping = checked;

        clearSuperItemFromMessages();
        container.removeContainerFilter(sectionFilter);

        if (checked) {
            buildTree();
        }

        container.addContainerFilter(sectionFilter);
    }

    /**
     * Return list of child items.
     * 
     * @param itemId parent itemId
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
                // TODO assign to status type
                MessageType type = (MessageType) item.getItemProperty(STATUS_PROPERTY_ID).getValue();
                Object parentItemId = getSuperItem(type);
                Item parentItem = container.getItem(parentItemId);
                if (parentItem != null) {
                    container.setParent(itemId, parentItemId);
                }
            }
        }
    }

    private void addTaskAsItem(Task task) {
        // filter out local messages that have id == null

        final Item item = container.addItem(task.getId());
        container.setChildrenAllowed(task.getId(), false);
        assignPropertiesFromTask(task, item);
    }

    private void assignPropertiesFromTask(Task task, final Item item) {
        if (item != null && task != null) {

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
                final MessageType type = (MessageType) item.getItemProperty(STATUS_PROPERTY_ID).getValue();

                System.out.println("TODO filter tasks...");
                switch (category) {
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
        listener.openMessage(itemId);
        // messagesManager.clearMessage(MgnlContext.getUser().getName(), messageId);
    }

    @Override
    public void deleteItems(final Set<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        final String userName = MgnlContext.getUser().getName();
        int messagesDeleted = 0;

        System.out.println("TODO deleting tasks...");
        shell.updateShellAppIndication(ShellAppType.PULSE, -messagesDeleted);
        /*
         * Refreshes the view to display the updated underlying data.
         */
        initView();
    }

    private void doTasksStatusUpdate(final Task.Status status, int decrementOrIncrement) {

        shell.updateShellAppIndication(ShellAppType.PULSE, decrementOrIncrement);

        int count = 0;
        final String userName = MgnlContext.getUser().getName();

        System.out.println("TODO update tasks...");

    }

    /**
     * Listener interface used to call back to parent presenter.
     */
    public interface Listener {
        public void openMessage(String messageId);
    }

    public int getNumberOfPendingTasksForCurrentUser() {
        // TODO implement
        return 0;
    }
}
