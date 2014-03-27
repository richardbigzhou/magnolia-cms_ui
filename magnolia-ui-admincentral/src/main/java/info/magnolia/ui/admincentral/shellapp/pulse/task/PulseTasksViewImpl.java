/**
 * This file Copyright (c) 2012-2014 Magnolia International
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

import static info.magnolia.ui.admincentral.shellapp.pulse.task.PulseTasksPresenter.*;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.shellapp.pulse.item.AbstractPulseItemView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.ItemCategory;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.workbench.column.DateColumnFormatter;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.GeneratedRow;

/**
 * Implementation of {@link PulseTasksView}.
 */
public final class PulseTasksViewImpl extends AbstractPulseItemView implements PulseTasksView {

    private static final String[] order = new String[] { NEW_PROPERTY_ID, TASK_PROPERTY_ID, STATUS_PROPERTY_ID, SENDER_PROPERTY_ID, SENT_TO_PROPERTY_ID, ASSIGNED_TO_PROPERTY_ID, DATE_PROPERTY_ID };

    @Inject
    public PulseTasksViewImpl(Shell shell, SimpleTranslator i18n) {
        super(shell, i18n, order,
                new String[] { i18n.translate("pulse.items.new"), i18n.translate("pulse.tasks.task"), i18n.translate("pulse.tasks.status"), i18n.translate("pulse.items.sender"), i18n.translate("pulse.tasks.sentTo"), i18n.translate("pulse.tasks.assignedTo"), i18n.translate("pulse.items.date") },
                ItemCategory.ALL, ItemCategory.PENDING, ItemCategory.ONGOING, ItemCategory.DONE);

        constructTable();
    }

    private void constructTable() {
        getItemTable().addGeneratedColumn(NEW_PROPERTY_ID, newTaskColumnGenerator);
        getItemTable().setColumnWidth(NEW_PROPERTY_ID, 100);
        getItemTable().addGeneratedColumn(TASK_PROPERTY_ID, taskColumnGenerator);
        getItemTable().setColumnWidth(TASK_PROPERTY_ID, 50);
        getItemTable().addGeneratedColumn(STATUS_PROPERTY_ID, taskColumnGenerator);
        getItemTable().setColumnWidth(STATUS_PROPERTY_ID, 450);
        getItemTable().addGeneratedColumn(DATE_PROPERTY_ID, new DateColumnFormatter(null));
        getItemTable().setColumnWidth(DATE_PROPERTY_ID, 150);

        getItemTable().setSortContainerPropertyId(DATE_PROPERTY_ID);
        getItemTable().setSortAscending(false);

    }

    @Override
    protected GeneratedRow generateGroupingRow(Item item) {
        /*
         * When sorting by type special items are inserted into Container to
         * acts as a placeholder for grouping sub section. This row
         * generator must render those special items.
         */
        Property<?> property = item.getItemProperty(STATUS_PROPERTY_ID);
        GeneratedRow generated = new GeneratedRow();

        // TODO return correct generated row
        return generated;
    }

    private Table.ColumnGenerator newTaskColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (NEW_PROPERTY_ID.equals(columnId)) {
                final Property<Boolean> newProperty = source.getContainerProperty(itemId, columnId);
                final Boolean isNew = newProperty != null && newProperty.getValue();
                if (isNew) {
                    final Label newMessage = new Label();
                    newMessage.setSizeUndefined();
                    newMessage.addStyleName("icon-tick");
                    newMessage.addStyleName("new-message");
                    return newMessage;
                }
            }
            return null;
        }
    };

    /**
     * a description of the work item and the content item it affects. It consists of:
     * an icon representing the work flow (not the work item itself).
     * This is typically the same icon as used by the action, which triggered the work flow, but this must be configurable.
     * a title naming the type of the work item
     * It should summarize the step in the larger work flow, which the work item represents.
     * a summary of the affected content item or items
     * typically shows the visible name of the content item (e.g. "Magnolia 6 features" for a page, "Andreas Weder" for a contact)
     * if several items are affected, the number and type of affected items (e.g. "5 contacts", "3 articles").
     * for content items hosted on sites, this would also show the name of the sites affected by the work flow.
     */
    private Table.ColumnGenerator taskColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (TASK_PROPERTY_ID.equals(columnId)) {

                final Label messageTypeIcon = new Label();
                messageTypeIcon.setSizeUndefined();
                messageTypeIcon.addStyleName("icon");
                messageTypeIcon.addStyleName("message-type");
                messageTypeIcon.addStyleName("icon-work-item");
                return messageTypeIcon;

            }
            return null;
        }
    };
}
