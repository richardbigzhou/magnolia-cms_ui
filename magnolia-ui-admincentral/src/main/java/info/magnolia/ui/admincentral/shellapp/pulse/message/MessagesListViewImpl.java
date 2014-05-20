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
package info.magnolia.ui.admincentral.shellapp.pulse.message;

import static info.magnolia.ui.admincentral.shellapp.pulse.message.MessagesContainer.*;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListView;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.PulseListFooter;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.vaadin.icon.ErrorIcon;
import info.magnolia.ui.vaadin.icon.InfoIcon;
import info.magnolia.ui.vaadin.icon.WarningIcon;
import info.magnolia.ui.workbench.column.DateColumnFormatter;

import javax.inject.Inject;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.GeneratedRow;
import com.vaadin.ui.TreeTable;

/**
 * Implementation of {@link MessagesListView}.
 */
public final class MessagesListViewImpl extends AbstractPulseListView implements MessagesListView {

    private static final String[] order = new String[] { NEW_PROPERTY_ID, TYPE_PROPERTY_ID, TEXT_PROPERTY_ID, SENDER_PROPERTY_ID, DATE_PROPERTY_ID };

    private static Table.ColumnGenerator newMessageColumnGenerator = new Table.ColumnGenerator() {

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
     * default visibility is for testing purposes.
     */
    static Table.ColumnGenerator textColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (TEXT_PROPERTY_ID.equals(columnId)) {
                final Property<String> textProperty = source.getContainerProperty(itemId, columnId);
                final Property<String> subjectProperty = source.getContainerProperty(itemId, SUBJECT_PROPERTY_ID);

                final Label textLabel = new Label();
                textLabel.setSizeUndefined();
                textLabel.addStyleName("message-subject-text");
                textLabel.setContentMode(ContentMode.HTML);

                final String subject = StringEscapeUtils.escapeXml(subjectProperty.getValue());
                final String text = StringEscapeUtils.escapeXml(textProperty.getValue());
                textLabel.setValue("<strong>" + StringUtils.abbreviate(subject, 70) + "</strong><div>" + StringUtils.abbreviate(text, 70) + "</div>");

                // tooltip
                textLabel.setDescription(subject);
                return textLabel;

            }
            return null;
        }
    };

    private static Table.ColumnGenerator typeColumnGenerator = new Table.ColumnGenerator() {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            if (TYPE_PROPERTY_ID.equals(columnId)) {
                final Property<MessageType> typeProperty = source.getContainerProperty(itemId, columnId);
                final MessageType messageType = typeProperty.getValue();

                switch (messageType) {
                case INFO:
                    return new InfoIcon();

                case WARNING:
                    return new WarningIcon();

                case ERROR:
                    return new ErrorIcon();

                }
            }
            return null;
        }
    };

    @Inject
    public MessagesListViewImpl(Shell shell, SimpleTranslator i18n) {
        super(shell, i18n, order,
                new String[] { i18n.translate("pulse.items.new"), i18n.translate("pulse.items.type"), i18n.translate("pulse.messages.text"), i18n.translate("pulse.items.sender"), i18n.translate("pulse.items.date") },
                i18n.translate("pulse.messages.empty"),
                PulseItemCategory.ALL_MESSAGES, PulseItemCategory.INFO, PulseItemCategory.PROBLEM);
        buildTable(getItemTable());
        setFooter(PulseListFooter.createMessagesFooter(getItemTable(), i18n));
    }

    private void buildTable(TreeTable itemTable) {
        itemTable.addGeneratedColumn(NEW_PROPERTY_ID, newMessageColumnGenerator);
        itemTable.setColumnWidth(NEW_PROPERTY_ID, 100);
        itemTable.addGeneratedColumn(TYPE_PROPERTY_ID, typeColumnGenerator);
        itemTable.setColumnWidth(TYPE_PROPERTY_ID, 50);
        itemTable.addGeneratedColumn(TEXT_PROPERTY_ID, textColumnGenerator);
        itemTable.setColumnWidth(TEXT_PROPERTY_ID, 450);
        itemTable.addGeneratedColumn(DATE_PROPERTY_ID, new DateColumnFormatter(null));
        itemTable.setColumnWidth(DATE_PROPERTY_ID, 150);
        itemTable.setSortContainerPropertyId(DATE_PROPERTY_ID);
        itemTable.setSortAscending(false);
    }

    @Override
    protected GeneratedRow generateGroupingRow(Item item) {
        GeneratedRow generated = new GeneratedRow();
        Property<MessageType> property = item.getItemProperty(TYPE_PROPERTY_ID);
        switch (property.getValue()) {
        case ERROR:
            generated.setText("", "", getI18n().translate("pulse.messages.errors"));
            break;
        case WARNING:
            generated.setText("", "", getI18n().translate("pulse.messages.warnings"));
            break;
        case INFO:
            generated.setText("", "", getI18n().translate("pulse.messages.info"));
            break;
        }
        return generated;
    }

}
