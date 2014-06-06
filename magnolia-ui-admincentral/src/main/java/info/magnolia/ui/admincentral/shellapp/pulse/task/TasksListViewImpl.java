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

import static info.magnolia.ui.admincentral.shellapp.pulse.task.TasksContainer.*;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.task.Task.Status;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListFooter;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.workbench.column.DateColumnFormatter;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.GeneratedRow;

/**
 * Implementation of {@link TasksListView}.
 */
public final class TasksListViewImpl extends AbstractPulseListView implements TasksListView {

    private static final String[] order = new String[] { NEW_PROPERTY_ID, TASK_PROPERTY_ID, STATUS_PROPERTY_ID, SENDER_PROPERTY_ID, SENT_TO_PROPERTY_ID, ASSIGNED_TO_PROPERTY_ID, LAST_CHANGE_PROPERTY_ID };

    @Inject
    public TasksListViewImpl(Shell shell, SimpleTranslator i18n) {
        super(shell, i18n, order,
                new String[] { i18n.translate("pulse.items.new"), i18n.translate("pulse.tasks.task"), i18n.translate("pulse.tasks.status"), i18n.translate("pulse.items.sender"), i18n.translate("pulse.tasks.sentTo"), i18n.translate("pulse.tasks.assignedTo"), i18n.translate("pulse.tasks.lastChange") },
                i18n.translate("pulse.tasks.empty"),
                PulseItemCategory.UNCLAIMED, PulseItemCategory.ONGOING, PulseItemCategory.DONE, PulseItemCategory.FAILED, PulseItemCategory.ALL_TASKS);
        constructTable();
        setFooter(PulseListFooter.createTasksFooter(getItemTable(), i18n));
    }

    private void constructTable() {
        getItemTable().addGeneratedColumn(NEW_PROPERTY_ID, newTaskColumnGenerator);
        getItemTable().setColumnWidth(NEW_PROPERTY_ID, 100);
        getItemTable().addGeneratedColumn(TASK_PROPERTY_ID, taskColumnGenerator);
        getItemTable().setColumnWidth(TASK_PROPERTY_ID, 220);
        getItemTable().addGeneratedColumn(STATUS_PROPERTY_ID, taskStatusColumnGenerator);
        getItemTable().setColumnWidth(STATUS_PROPERTY_ID, 80);
        getItemTable().addGeneratedColumn(SENT_TO_PROPERTY_ID, sentToColumnGenerator);
        getItemTable().setColumnWidth(SENT_TO_PROPERTY_ID, 100);
        getItemTable().addGeneratedColumn(LAST_CHANGE_PROPERTY_ID, new DateColumnFormatter(null));
        getItemTable().setColumnWidth(LAST_CHANGE_PROPERTY_ID, 140);

        getItemTable().setSortContainerPropertyId(LAST_CHANGE_PROPERTY_ID);
        getItemTable().setSortAscending(false);
    }

    @Override
    protected GeneratedRow generateGroupingRow(Item item) {
        /*
         * When sorting by type special items are inserted into Container to
         * acts as a placeholder for grouping sub section. This row
         * generator must render those special items.
         */
        Property<Status> property = item.getItemProperty(STATUS_PROPERTY_ID);
        GeneratedRow generated = new GeneratedRow();

        switch (property.getValue()) {
        case Created:
            generated.setText("", "", getI18n().translate("pulse.tasks.unclaimed"));
            break;
        case InProgress:
            generated.setText("", "", getI18n().translate("pulse.tasks.ongoing"));
            break;
        case Resolved:
            generated.setText("", "", getI18n().translate("pulse.tasks.done"));
            break;
        case Failed:
            generated.setText("", "", getI18n().translate("pulse.tasks.failed"));
            break;
        }
        return generated;
    }

    private Table.ColumnGenerator newTaskColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (NEW_PROPERTY_ID.equals(columnId)) {
                final Property<Boolean> newProperty = source.getContainerProperty(itemId, columnId);
                final Boolean isNew = newProperty != null && newProperty.getValue();
                if (isNew) {
                    final Label newTask = new Label();
                    newTask.setSizeUndefined();
                    newTask.addStyleName("icon-tick");
                    newTask.addStyleName("new-message");
                    return newTask;
                }
            }
            return null;
        }
    };

    private Table.ColumnGenerator taskStatusColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (STATUS_PROPERTY_ID.equals(columnId)) {
                final Property<Status> status = source.getContainerProperty(itemId, columnId);
                Label label = new Label();
                switch (status.getValue()) {
                case Created:
                    label.setValue(getI18n().translate("pulse.tasks.unclaimed"));
                    break;
                case InProgress:
                    label.setValue(getI18n().translate("pulse.tasks.ongoing"));
                    break;
                case Resolved:
                    label.setValue(getI18n().translate("pulse.tasks.done"));
                    break;
                case Failed:
                    label.setValue(getI18n().translate("pulse.tasks.failed"));
                    break;
                default:
                    break;
                }
                return label;
            }
            return null;
        }
    };

    /**
     * default visibility for test purposes only.
     */
    Table.ColumnGenerator taskColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (TASK_PROPERTY_ID.equals(columnId)) {
                final Property<String> text = source.getContainerProperty(itemId, columnId);
                return new TaskCellComponent(itemId, text.getValue());
            }
            return null;
        }
    };

    private Table.ColumnGenerator sentToColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (SENT_TO_PROPERTY_ID.equals(columnId)) {
                final Property<String> sendTo = source.getContainerProperty(itemId, columnId);
                return new SentToCellComponent(itemId, sendTo.getValue());
            }
            return null;
        }
    };

    /**
     * TaskCellComponent. Default visibility is for testing purposes only.
     */
    final class TaskCellComponent extends CustomComponent {
        private CssLayout root = new CssLayout();
        private final Label label = new Label();

        public TaskCellComponent(final Object itemId, final String text) {
            final Label icon = new Label();
            icon.setSizeUndefined();
            icon.addStyleName("icon");
            icon.addStyleName("message-type");
            icon.addStyleName("icon-work-item");

            String[] parts = text.split("\\|");
            String title = StringEscapeUtils.escapeXml(parts[0]);
            String comment = getI18n().translate("pulse.tasks.nocomment");
            if (parts.length == 2) {
                comment = StringEscapeUtils.escapeXml(parts[1]);
            }

            label.setContentMode(ContentMode.HTML);
            label.addStyleName("title");
            label.setValue("<strong>" + StringUtils.abbreviate(title, 28) + "</strong><div class=\"comment\">" + StringUtils.abbreviate(comment, 28) + "</div>");

            root.addComponent(icon);
            root.addComponent(label);

            addStyleName("task");

            setCompositionRoot(root);

            // tooltip
            setDescription(title);

            root.addLayoutClickListener(new LayoutClickListener() {

                @Override
                public void layoutClick(LayoutClickEvent event) {
                    onItemClicked(event, itemId);
                }
            });
        }

        public String getValue() {
            return label.getValue();
        }
    }

    /**
     * SentToCellComponent.
     */
    private final class SentToCellComponent extends CustomComponent {
        private CssLayout root = new CssLayout();

        public SentToCellComponent(final Object itemId, final String text) {
            final Label icon = new Label();
            icon.setSizeUndefined();
            icon.addStyleName("icon");
            icon.addStyleName("icon-user-group");
            icon.addStyleName("sentToIcon");

            String[] parts = text.split("\\|");
            String groups = parts[0];
            String users = "";
            if (parts.length == 2) {
                users = parts[1];
            }

            final Label groupLabel = new Label();
            groupLabel.addStyleName("sentTo");
            groupLabel.addStyleName("groups");
            groupLabel.setValue(StringUtils.abbreviate(groups, 12));

            final Label userLabel = new Label();
            userLabel.addStyleName("sentTo");
            userLabel.setValue(StringUtils.abbreviate(users, 12));

            root.addComponent(groupLabel);
            root.addComponent(icon);
            root.addComponent(userLabel);

            addStyleName("task");

            setCompositionRoot(root);

            // tooltip
            setDescription(groups + " " + users);

            root.addLayoutClickListener(new LayoutClickListener() {

                @Override
                public void layoutClick(LayoutClickEvent event) {
                    onItemClicked(event, itemId);
                }
            });
        }
    }

    @Override
    public void setTaskListener(TasksListView.Listener listener) {
        getFooter().setTasksListener(listener);
    }
}
