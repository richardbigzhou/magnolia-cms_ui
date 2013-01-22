/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse;

import java.util.Iterator;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Message category navigation component in Pulse.
 */
public class PulseMessageCategoryNavigator extends CssLayout {

    private CheckBox groupByTypeCheckBox;

    public PulseMessageCategoryNavigator() {
        super();
        setStyleName("navigator");
        construct();
    }

    private void construct() {
        for (final MessageCategory category : MessageCategory.values()) {
            MessageCategoryButton button = new MessageCategoryButton(category);
            if (category.equals(MessageCategory.ALL)) {
                button.setActive(true);
            }
            addComponent(button);
        }

        groupByTypeCheckBox = new CheckBox("group by type");
        groupByTypeCheckBox.addStyleName("navigator-grouping");
        groupByTypeCheckBox.setImmediate(true);
        addComponent(groupByTypeCheckBox);
    }

    public void addGroupingListener(ValueChangeListener listener) {
        groupByTypeCheckBox.addListener(listener);
    }

    /**
     * Enumeration for the category types.
     */
    public enum MessageCategory {
        ALL("All messages"),
        WORK_ITEM("Work items"),
        PROBLEM("Problems"),
        INFO("Info");

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
        Iterator<Component> iterator = getComponentIterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (component instanceof MessageCategoryButton) {
                MessageCategoryButton button = (MessageCategoryButton) component;
                button.setActive(button.category == category);
            }
        }
        fireEvent(new CategoryChangedEvent(this, category));
    }

    /**
     * Message category button.
     */
    public class MessageCategoryButton extends NativeButton {

        private final MessageCategory category;

        public MessageCategoryButton(MessageCategory category) {
            super();
            setStyleName(BaseTheme.BUTTON_LINK);
            addStyleName("navigator-button");
            this.category = category;
            this.setCaption(category.getCaption());
            addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    fireCategoryChangedEvent(MessageCategoryButton.this.category);
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
    }
}
