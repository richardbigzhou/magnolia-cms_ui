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

import info.magnolia.cms.i18n.MessagesUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Message category navigation component in Pulse.
 */
public final class PulseMessageCategoryNavigator extends CssLayout {

    private CheckBox groupByTypeCheckBox;

    private Map<MessageCategory, MessageCategoryTab> messageCategoryTabs = new HashMap<MessageCategory, MessageCategoryTab>();

    public PulseMessageCategoryNavigator() {
        super();
        setStyleName("navigator");
        construct();
    }

    private void construct() {
        setSizeUndefined();
        for (final MessageCategory category : MessageCategory.values()) {
            MessageCategoryTab tab = new MessageCategoryTab(category);
            if (category == MessageCategory.ALL) {
                tab.setActive(true);
            }
            messageCategoryTabs.put(category, tab);
            addComponent(tab);
        }

        groupByTypeCheckBox = new CheckBox(MessagesUtil.get("pulse.messages.groupby"));
        groupByTypeCheckBox.addStyleName("navigator-grouping");
        groupByTypeCheckBox.setImmediate(true);
        addComponent(groupByTypeCheckBox);

    }

    public void addGroupingListener(ValueChangeListener listener) {
        groupByTypeCheckBox.addValueChangeListener(listener);
    }

    public void showGroupByType(boolean show) {
        groupByTypeCheckBox.setVisible(show);
    }

    /**
     * Enumeration for the category types.
     */
    public enum MessageCategory {
        ALL(MessagesUtil.get("pulse.messages.all")),
        WORK_ITEM(MessagesUtil.get("pulse.messages.workitems")),
        PROBLEM(MessagesUtil.get("pulse.messages.problems")),
        INFO(MessagesUtil.get("pulse.messages.info"));

        private String caption;

        private MessageCategory(final String caption) {
            this.caption = caption;
        }

        public String getCaption() {
            return caption;
        }
    }

    /**
     * Category changed event.
     */
    public static class CategoryChangedEvent extends Component.Event {

        public static final java.lang.reflect.Method MESSAGE_CATEGORY_CHANGED;

        static {
            try {
                MESSAGE_CATEGORY_CHANGED = MessageCategoryChangedListener.class.getDeclaredMethod("messageCategoryChanged", new Class[]{CategoryChangedEvent.class});
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        private final MessageCategory category;

        public CategoryChangedEvent(Component source, MessageCategory category) {
            super(source);
            this.category = category;
        }

        public MessageCategory getCategory() {
            return category;
        }
    }

    /**
     * MessageCategoryChangedListener.
     */
    public interface MessageCategoryChangedListener {

        public void messageCategoryChanged(final CategoryChangedEvent event);
    }

    public void addCategoryChangeListener(final MessageCategoryChangedListener listener) {
        addListener("category_changed", CategoryChangedEvent.class, listener, CategoryChangedEvent.MESSAGE_CATEGORY_CHANGED);
    }

    private void fireCategoryChangedEvent(MessageCategory category) {
        Iterator<Component> iterator = iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (component instanceof MessageCategoryTab) {
                MessageCategoryTab button = (MessageCategoryTab) component;
                button.setActive(button.category == category);
            }
        }
        fireEvent(new CategoryChangedEvent(this, category));
    }

    /**
     * Message category button.
     */
    public class MessageCategoryTab extends HorizontalLayout {

        private final MessageCategory category;
        private final Label categoryLabel;
        private final Label badge;

        public MessageCategoryTab(MessageCategory category) {
            super();
            this.category = category;
            this.addStyleName("navigator-tab");
            this.setSizeUndefined();

            categoryLabel = new Label(category.getCaption());
            categoryLabel.addStyleName("category");

            badge = new Label();
            badge.addStyleName("badge");
            badge.setVisible(false);

            this.addComponent(categoryLabel);
            this.addComponent(badge);
            this.addLayoutClickListener(new LayoutClickListener() {

                @Override
                public void layoutClick(LayoutClickEvent event) {
                    fireCategoryChangedEvent(MessageCategoryTab.this.category);
                }
            });

        }

        public void setActive(boolean active) {
            if (active) {
                addStyleName("active");
            } else {
                removeStyleName("active");
            }
        }

        public void updateMessagesCount(int count) {
            if (count <= 0) {
                badge.setVisible(false);
            } else {
                String countAsString = String.valueOf(count);
                if (count > 99) {
                    countAsString = "99+";
                }
                badge.setValue(countAsString);
                badge.setVisible(true);
            }
        }
    }

    public void updateCategoryBadgeCount(MessageCategory category, int count) {
        messageCategoryTabs.get(category).updateMessagesCount(count);
    }
}
