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

import static info.magnolia.ui.admincentral.shellapp.pulse.task.data.TaskConstants.*;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.task.Task.Status;
import info.magnolia.ui.admincentral.shellapp.pulse.data.PulseConstants;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListFooter;
import info.magnolia.ui.workbench.column.DateColumnFormatter;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.GeneratedRow;

/**
 * Implementation of {@link TasksListView}.
 */
public final class TasksListViewImpl extends AbstractPulseListView implements TasksListView {

    private static final String[] order = new String[]{NEW_PROPERTY_ID, TASK_PROPERTY_ID, STATUS_PROPERTY_ID, SENDER_PROPERTY_ID, SENT_TO_PROPERTY_ID, ASSIGNED_TO_PROPERTY_ID, LAST_CHANGE_PROPERTY_ID};

    @Inject
    public TasksListViewImpl(SimpleTranslator i18n) {
        super(i18n, order,
                new String[]{i18n.translate("pulse.items.new"), i18n.translate("pulse.tasks.task"), i18n.translate("pulse.tasks.status"), i18n.translate("pulse.items.sender"), i18n.translate("pulse.tasks.sentTo"), i18n.translate("pulse.tasks.assignedTo"), i18n.translate("pulse.tasks.lastChange")},
                i18n.translate("pulse.tasks.empty"),
                PulseItemCategory.UNCLAIMED, PulseItemCategory.ONGOING, PulseItemCategory.DONE, PulseItemCategory.FAILED, PulseItemCategory.ALL_TASKS);
        setFooter(new PulseListFooter(getItemTable(), i18n, true));
        constructTable();
    }

    private void constructTable() {
        Table table = getItemTable();
        table.setCacheRate(1);
        table.addGeneratedColumn(NEW_PROPERTY_ID, new PulseNewItemColumnGenerator());
        table.setColumnWidth(NEW_PROPERTY_ID, 100);
        table.addGeneratedColumn(TASK_PROPERTY_ID, new TaskSubjectColumnGenerator());
        table.setColumnWidth(TASK_PROPERTY_ID, 220);
        table.addGeneratedColumn(STATUS_PROPERTY_ID, new TaskStatusColumnGenerator());
        table.setColumnWidth(STATUS_PROPERTY_ID, 80);
        table.addGeneratedColumn(SENT_TO_PROPERTY_ID, new SentToColumnGenerator());
        table.setColumnWidth(SENT_TO_PROPERTY_ID, 100);
        table.addGeneratedColumn(LAST_CHANGE_PROPERTY_ID, new DateColumnFormatter(null));
        table.setColumnWidth(LAST_CHANGE_PROPERTY_ID, 140);
        table.setSortAscending(false);

        // tooltips
        table.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                if (String.valueOf(itemId).startsWith(PulseConstants.GROUP_PLACEHOLDER_ITEMID)) {
                    return null;
                }

                if (TASK_PROPERTY_ID.equals(propertyId)) {
                    String task = (String) ((AbstractSelect) source).getContainerProperty(itemId, TASK_PROPERTY_ID).getValue();
                    if (StringUtils.isNotBlank(task)) {
                        // title only
                        String title = task.split("\\|")[0];
                        return StringEscapeUtils.escapeXml(title);
                    }

                } else if (SENT_TO_PROPERTY_ID.equals(propertyId)) {
                    String sentTo = (String) ((AbstractSelect) source).getContainerProperty(itemId, SENT_TO_PROPERTY_ID).getValue();
                    if (StringUtils.isNotBlank(sentTo)) {
                        // prepare group/user labels
                        String[] parts = sentTo.split("\\|");
                        String groups = parts[0];
                        String users = "";
                        if (parts.length == 2) {
                            users = parts[1];
                        }
                        return groups + " " + users;
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void setDataSource(Container dataSource) {
        super.setDataSource(dataSource);
        getItemTable().setSortContainerPropertyId(LAST_CHANGE_PROPERTY_ID);
    }

    @Override
    protected GeneratedRow generateGroupingRow(Item item) {
        /*
         * When sorting by type special items are inserted into Container to
         * acts as a placeholder for grouping sub section. This row
         * generator must render those special items.
         */
        Status status = (Status) item.getItemProperty(STATUS_PROPERTY_ID).getValue();
        GeneratedRow row = new GeneratedRow();
        String key = getI18nKeyForStatus(status);
        if (StringUtils.isNotBlank(key)) {
            row.setText("", "", getI18n().translate(key));
        }
        return row;
    }

    private String getI18nKeyForStatus(Status status) {
        switch (status) {
        case Created:
            return "pulse.tasks.unclaimed";
        case InProgress:
            return "pulse.tasks.ongoing";
        case Resolved:
            return "pulse.tasks.done";
        case Failed:
            return "pulse.tasks.failed";
        default:
            break;
        }
        return null;
    }

    @Override
    public void setTaskListener(TasksListView.Listener listener) {
        getFooter().setTasksListener(listener);
    }

    /**
     * The task subject {@link Table.ColumnGenerator ColumnGenerator} resolves the task title and comment in an abbreviated and escaped form.
     */
    // default visibility for tests.
    class TaskSubjectColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            String text = (String) source.getContainerProperty(itemId, TASK_PROPERTY_ID).getValue();
            if (StringUtils.isNotBlank(text)) {

                // prepare title and comment texts
                String[] parts = text.split("\\|");
                String title = StringEscapeUtils.escapeXml(parts[0]);
                String comment = getI18n().translate("pulse.tasks.nocomment");
                if (parts.length == 2) {
                    comment = StringEscapeUtils.escapeXml(parts[1]);
                }

                return String.format("<div class=\"task\"><span class=\"icon %s message-type\"></span>"
                        + "<span class=\"title\"><strong>%s</strong>"
                        + "<span class=\"comment\">%s</span></span></div>",
                        "icon-work-item",
                        StringUtils.abbreviate(title, 28),
                        StringUtils.abbreviate(comment, 28));
            }
            return null;
        }
    }

    /**
     * The task status {@link Table.ColumnGenerator ColumnGenerator} resolves the i18n key for the given status and returns its translation.
     */
    private class TaskStatusColumnGenerator implements Table.ColumnGenerator {
        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            Status status = (Status) source.getContainerProperty(itemId, STATUS_PROPERTY_ID).getValue();
            String key = getI18nKeyForStatus(status);
            if (key != null) {
                return getI18n().translate(key);
            }
            return null;
        }
    }

    /**
     * The "sent-to" {@link Table.ColumnGenerator ColumnGenerator} resolves task recipients (groups, users) in an abbreviated form.
     */
    private class SentToColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            String sentTo = (String) source.getContainerProperty(itemId, SENT_TO_PROPERTY_ID).getValue();
            if (StringUtils.isNotBlank(sentTo)) {

                // prepare group/user labels
                String[] parts = sentTo.split("\\|");
                String groups = parts[0];
                String users = "";
                if (parts.length == 2) {
                    users = parts[1];
                }

                StringBuilder cell = new StringBuilder();
                cell.append("<div class=\"task\">");

                if (StringUtils.isNotBlank(groups)) {
                    cell.append("<span class=\"sentTo groups\">")
                            .append(StringUtils.abbreviate(groups, 12))
                            .append("</span>")
                            .append("<span class=\"icon icon-user-group sentToIcon\">");
                }
                if (StringUtils.isNotBlank(users)) {
                    cell.append("<span class=\"sentTo\">")
                            .append(StringUtils.abbreviate(users, 12))
                            .append("</span>");
                }

                cell.append("</div>");
                return cell.toString();
            }
            return null;
        }
    }
}
