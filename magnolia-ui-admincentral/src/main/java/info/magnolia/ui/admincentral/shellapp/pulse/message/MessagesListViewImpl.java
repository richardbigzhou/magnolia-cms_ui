/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListView;
import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageConstants;
import info.magnolia.ui.api.message.MessageType;
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
 * Implementation of {@link MessagesListView}.
 */
public final class MessagesListViewImpl extends AbstractPulseListView implements MessagesListView {

    private static final String[] order = new String[] { MessageConstants.NEW_PROPERTY_ID, MessageConstants.TYPE_PROPERTY_ID, MessageConstants.TEXT_PROPERTY_ID, MessageConstants.SENDER_PROPERTY_ID, MessageConstants.DATE_PROPERTY_ID };

    @Inject
    public MessagesListViewImpl(SimpleTranslator i18n) {
        super(i18n, order,
                new String[] { i18n.translate("pulse.items.new"), i18n.translate("pulse.items.type"), i18n.translate("pulse.messages.text"), i18n.translate("pulse.items.sender"), i18n.translate("pulse.items.date") },
                i18n.translate("pulse.messages.empty"),
                PulseItemCategory.ALL_MESSAGES, PulseItemCategory.INFO, PulseItemCategory.PROBLEM);
        constructTable();
    }

    private void constructTable() {
        final Table table = getItemTable();
        table.setCacheRate(1);
        table.addGeneratedColumn(MessageConstants.NEW_PROPERTY_ID, new PulseNewItemColumnGenerator());
        table.setColumnWidth(MessageConstants.NEW_PROPERTY_ID, 100);
        table.addGeneratedColumn(MessageConstants.TYPE_PROPERTY_ID, new MessageTypeColumnGenerator());
        table.setColumnWidth(MessageConstants.TYPE_PROPERTY_ID, 50);
        table.addGeneratedColumn(MessageConstants.TEXT_PROPERTY_ID, new MessageSubjectColumnGenerator());
        table.setColumnWidth(MessageConstants.TEXT_PROPERTY_ID, 450);
        table.addGeneratedColumn(MessageConstants.DATE_PROPERTY_ID, new DateColumnFormatter(null));
        table.setColumnWidth(MessageConstants.DATE_PROPERTY_ID, 150);
        getItemTable().setSortAscending(false);

        // tooltips
        table.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {

            @Override
            public String generateDescription(Component source, Object itemId, Object propertyId) {
                if (MessageConstants.TEXT_PROPERTY_ID.equals(propertyId)) {
                    String subject = (String) ((AbstractSelect) source).getContainerProperty(itemId, MessageConstants.SUBJECT_PROPERTY_ID).getValue();
                    return StringEscapeUtils.escapeXml(subject);
                }
                return null;
            }
        });
    }

    @Override
    public void setDataSource(Container dataSource) {
        super.setDataSource(dataSource);
        getItemTable().setSortContainerPropertyId(MessageConstants.DATE_PROPERTY_ID);
    }

    @Override
    protected GeneratedRow generateGroupingRow(Item item) {
        GeneratedRow row = new GeneratedRow();
        MessageType messageType = (MessageType) item.getItemProperty(MessageConstants.TYPE_PROPERTY_ID).getValue();

        String key;
        switch (messageType) {
        case ERROR:
            key = "pulse.messages.errors";
            break;
        case WARNING:
            key = "pulse.messages.warnings";
            break;
        case INFO:
            key = "pulse.messages.info";
            break;
        default:
            return null;
        }

        row.setText("", "", getI18n().translate(key));
        return row;
    }

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for the subject cells in the messages list view.
     */
    // default visibility for tests.
    class MessageSubjectColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {

            String subject = (String) source.getContainerProperty(itemId, MessageConstants.SUBJECT_PROPERTY_ID).getValue();
            String text = (String) source.getContainerProperty(itemId, MessageConstants.TEXT_PROPERTY_ID).getValue();

            if (StringUtils.isNotBlank(subject) && StringUtils.isNotBlank(text)) {

                // prepare title and comment texts
                subject = StringEscapeUtils.escapeXml(subject);
                text = StringEscapeUtils.escapeXml(text);

                return String.format("<div class=\"message-subject-text\">"
                        + "<strong>%s</strong><br/>"
                        + "%s</div>",
                        StringUtils.abbreviate(subject, 70),
                        StringUtils.abbreviate(text, 70));
            }
            return null;
        }
    }

    /**
     * The Vaadin {@link Table.ColumnGenerator ColumnGenerator} for the type cells in the messages list view.
     */
    private class MessageTypeColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            MessageType messageType = (MessageType) source.getContainerProperty(itemId, MessageConstants.TYPE_PROPERTY_ID).getValue();

            String level, shape = "circle", mark;
            switch (messageType) {
            case INFO:
                level = "info-icon";
                mark = "icon-info_mark";
                break;
            case WARNING:
                level = "warning-icon";
                mark = "icon-warning-mark";
                break;
            case ERROR:
                level = "error-icon";
                shape = "triangle";
                mark = "icon-error-mark";
                break;
            default:
                return null;
            }

            return String.format("<span class=\"composite-icon %1$s\">"
                    + "<span class=\"icon icon-shape-%2$s-plus\"></span>"
                    + "<span class=\"icon icon-shape-%2$s\"></span>"
                    + "<span class=\"icon %3$s\"></span>"
                    + "</span>",
                    level, shape, mark);
        }
    }

}
