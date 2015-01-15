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
package info.magnolia.ui.admincentral.shellapp.pulse.task;

import static info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListView.GROUP_PLACEHOLDER_ITEMID;

import info.magnolia.task.Task;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListContainer;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * The tasks container instantiates and manages an {@link HierarchicalContainer} with tasks.
 */
public class TasksContainer extends AbstractPulseListContainer<Task> {

    private static final Logger log = LoggerFactory.getLogger(TasksContainer.class);

    public static final String NEW_PROPERTY_ID = "new";
    public static final String TASK_PROPERTY_ID = "task";
    public static final String STATUS_PROPERTY_ID = "status";
    public static final String SENDER_PROPERTY_ID = "sender";
    public static final String LAST_CHANGE_PROPERTY_ID = "date";
    public static final String SENT_TO_PROPERTY_ID = "sentTo";
    public static final String ASSIGNED_TO_PROPERTY_ID = "assignedTo";


    /*
     * This filter hides grouping titles when
     * grouping is not on or group would be empty
     */
    private Container.Filter sectionFilter = new Container.Filter() {

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

    @Override
    public HierarchicalContainer createDataSource(Collection<Task> tasks) {
        container = new HierarchicalContainer();
        container.addContainerProperty(NEW_PROPERTY_ID, Boolean.class, true);
        container.addContainerProperty(TASK_PROPERTY_ID, String.class, null);
        container.addContainerProperty(STATUS_PROPERTY_ID, Task.Status.class, Task.Status.Created);
        container.addContainerProperty(SENDER_PROPERTY_ID, String.class, null);
        container.addContainerProperty(ASSIGNED_TO_PROPERTY_ID, String.class, null);
        container.addContainerProperty(SENT_TO_PROPERTY_ID, String.class, null);
        container.addContainerProperty(LAST_CHANGE_PROPERTY_ID, Date.class, null);

        createSuperItems();

        for (Task task : tasks) {
            addBeanAsItem(task);
        }

        container.addContainerFilter(getSectionFilter());

        return container;
    }

    @Override
    public void addBeanAsItem(Task task) {
        log.debug("Adding task {}", task);
        final String taskId = task.getId();
        final Item item = container.addItem(taskId);
        container.setChildrenAllowed(taskId, false);
        assignPropertiesFromBean(task, item);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void assignPropertiesFromBean(Task task, final Item item) {
        if (item != null && task != null) {
            item.getItemProperty(NEW_PROPERTY_ID).setValue(task.getStatus() == Task.Status.Created);
            item.getItemProperty(TASK_PROPERTY_ID).setValue(getItemTitle(task));
            item.getItemProperty(SENDER_PROPERTY_ID).setValue(task.getRequestor());
            item.getItemProperty(LAST_CHANGE_PROPERTY_ID).setValue(task.getModificationDate());
            item.getItemProperty(STATUS_PROPERTY_ID).setValue(task.getStatus());
            item.getItemProperty(ASSIGNED_TO_PROPERTY_ID).setValue(StringUtils.defaultString(task.getActorId()));

            String sentTo = "";
            if (task.getGroupIds() != null && task.getGroupIds().size() > 0) {
                sentTo += StringUtils.join(task.getGroupIds(), ",");
            }
            if (task.getActorIds() != null && task.getActorIds().size() > 0) {
                sentTo += StringUtils.join(task.getActorIds(), ",");
            }
            item.getItemProperty(SENT_TO_PROPERTY_ID).setValue(sentTo);
        }
    }

    @Override
    protected void createSuperItems() {
        for (Task.Status status : Task.Status.values()) {
            Object itemId = getSuperItem(status);
            Item item = container.addItem(itemId);
            item.getItemProperty(STATUS_PROPERTY_ID).setValue(status);
            container.setChildrenAllowed(itemId, true);
        }

    }

    @Override
    protected void clearSuperItems() {
        for (Object itemId : container.getItemIds()) {
            container.setParent(itemId, null);
        }
    }

    private Object getSuperItem(Task.Status status) {
        return GROUP_PLACEHOLDER_ITEMID + status;
    }

    /*
     * Assign messages under correct parents so that
     * grouping works.
     */
    @Override
    public void buildTree() {
        for (Object itemId : container.getItemIds()) {
            // Skip super items
            if (!itemId.toString().startsWith(GROUP_PLACEHOLDER_ITEMID)) {
                Item item = container.getItem(itemId);

                Task.Status status = (Task.Status) item.getItemProperty(STATUS_PROPERTY_ID).getValue();
                Object parentItemId = getSuperItem(status);
                Item parentItem = container.getItem(parentItemId);
                if (parentItem != null) {
                    container.setParent(itemId, parentItemId);
                }
            }
        }
    }

    /*
    * Default visibility for testing purposes only.
    */
    private String getItemTitle(final Task task) {
        return listener.getItemTitle(task);
    }

    @Override
    protected void applyCategoryFilter(final PulseItemCategory category) {
        final Container.Filter filter = new Container.Filter() {

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
                final Task.Status type = (Task.Status) item.getItemProperty(STATUS_PROPERTY_ID).getValue();

                switch (category) {
                case UNCLAIMED:
                    return type == Task.Status.Created;
                case ONGOING:
                    return type == Task.Status.InProgress;
                case DONE:
                    return type == Task.Status.Resolved;
                case FAILED:
                    return type == Task.Status.Failed;
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
    protected Container.Filter getSectionFilter() {
        return sectionFilter;
    }

}
