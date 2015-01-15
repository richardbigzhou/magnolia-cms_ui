/**
 * This file Copyright (c) 2012-2015 Magnolia International
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

import static info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListView.GROUP_PLACEHOLDER_ITEMID;

import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.list.AbstractPulseListContainer;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;

import java.util.Collection;
import java.util.Date;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * The messages container instantiates and manages an {@link HierarchicalContainer} with messages.
 */
public class MessagesContainer extends AbstractPulseListContainer<Message> {

    public static final String NEW_PROPERTY_ID = "new";
    public static final String TYPE_PROPERTY_ID = "type";
    public static final String SUBJECT_PROPERTY_ID = "subject";
    public static final String TEXT_PROPERTY_ID = "text";
    public static final String SENDER_PROPERTY_ID = "sender";
    public static final String DATE_PROPERTY_ID = "date";
    public static final String QUICKDO_PROPERTY_ID = "quickdo";

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
            return TYPE_PROPERTY_ID.equals(propertyId);
        }

        private boolean isTypeGroupEmpty(Object typeId) {
            return container.getChildren(typeId) == null || container.getChildren(typeId).isEmpty();
        }

    };
    @Override
    public HierarchicalContainer createDataSource(Collection<Message> messages) {
        container = new HierarchicalContainer();
        container.addContainerProperty(NEW_PROPERTY_ID, Boolean.class, true);
        container.addContainerProperty(SUBJECT_PROPERTY_ID, String.class, null);
        container.addContainerProperty(TYPE_PROPERTY_ID, MessageType.class, MessageType.UNKNOWN);
        container.addContainerProperty(TEXT_PROPERTY_ID, String.class, null);
        container.addContainerProperty(SENDER_PROPERTY_ID, String.class, null);
        container.addContainerProperty(DATE_PROPERTY_ID, Date.class, null);
        container.addContainerProperty(QUICKDO_PROPERTY_ID, String.class, null);

        createSuperItems();

        for (Message message : messages) {
            addBeanAsItem(message);
        }

        container.addContainerFilter(getSectionFilter());

        return container;
    }

    @Override
    public void addBeanAsItem(Message message) {
        // filter out local messages that have id == null
        if (message.getId() != null) {
            final Item item = container.addItem(message.getId());
            container.setChildrenAllowed(message.getId(), false);
            assignPropertiesFromBean(message, item);
        }
    }

    @Override
    public void assignPropertiesFromBean(Message message, Item item) {
        if (item != null && message != null) {
            item.getItemProperty(NEW_PROPERTY_ID).setValue(!message.isCleared());
            item.getItemProperty(TYPE_PROPERTY_ID).setValue(message.getType());
            item.getItemProperty(SENDER_PROPERTY_ID).setValue(message.getSender());
            item.getItemProperty(SUBJECT_PROPERTY_ID).setValue(message.getSubject());
            item.getItemProperty(TEXT_PROPERTY_ID).setValue((message.getMessage()));
            item.getItemProperty(DATE_PROPERTY_ID).setValue(new Date(message.getTimestamp()));
        }
    }

    @Override
    protected void createSuperItems() {
        for (MessageType type : MessageType.values()) {
            Object itemId = getSuperItem(type);
            Item item = container.addItem(itemId);
            item.getItemProperty(TYPE_PROPERTY_ID).setValue(type);
            container.setChildrenAllowed(itemId, true);
        }
    }

    @Override
    protected void clearSuperItems() {
        for (Object itemId : container.getItemIds()) {
            container.setParent(itemId, null);
        }
    }

    private Object getSuperItem(MessageType type) {
        return GROUP_PLACEHOLDER_ITEMID + type;
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
                MessageType type = (MessageType) item.getItemProperty(TYPE_PROPERTY_ID).getValue();
                Object parentItemId = getSuperItem(type);
                Item parentItem = container.getItem(parentItemId);
                if (parentItem != null) {
                    container.setParent(itemId, parentItemId);
                }
            }
        }
    }

    @Override
    protected Container.Filter getSectionFilter() {
        return sectionFilter;
    }

    @Override
    protected void applyCategoryFilter(final PulseItemCategory category) {

        final Container.Filter filter = new Container.Filter() {

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
                final MessageType type = (MessageType) item.getItemProperty(TYPE_PROPERTY_ID).getValue();

                switch (category) {
                case PROBLEM:
                    return type == MessageType.ERROR || type == MessageType.WARNING;
                case INFO:
                    return type == MessageType.INFO;
                default:
                    return true;
                }
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return TYPE_PROPERTY_ID.equals(propertyId);
            }

        };
        container.addContainerFilter(filter);
    }

}
