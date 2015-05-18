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
package info.magnolia.ui.admincentral.shellapp.pulse.item.detail;

import info.magnolia.i18nsystem.SimpleTranslator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Generic item category navigation component in Pulse. An item can be e.g. a Task, a Message, etc.
 */
public final class PulseItemCategoryNavigator extends CssLayout {

    private CheckBox groupByCheckBox;

    private Map<PulseItemCategory, ItemCategoryTab> itemCategoryTabs = new HashMap<PulseItemCategory, ItemCategoryTab>();

    private final SimpleTranslator i18n;

    private boolean isTopRow;

    private boolean showGroupBy;

    /**
     * @deprecated since 5.4 use the plain constructor {@link #PulseItemCategoryNavigator(SimpleTranslator, boolean, boolean, PulseItemCategory...)} instead.
     */
    @Deprecated
    public static PulseItemCategoryNavigator createTopRowNavigator(SimpleTranslator i18n, PulseItemCategory... categories) {
        return new PulseItemCategoryNavigator(i18n, false, true, categories);
    }

    /**
     * @deprecated since 5.4 use the plain constructor {@link #PulseItemCategoryNavigator(SimpleTranslator, boolean, boolean, PulseItemCategory...)} instead.
     */
    @Deprecated
    public static PulseItemCategoryNavigator createSubRowNavigator(SimpleTranslator i18n, PulseItemCategory... categories) {
        return new PulseItemCategoryNavigator(i18n, true, false, categories);
    }

    public PulseItemCategoryNavigator(SimpleTranslator i18n, boolean showGroupBy, boolean isTopRow, PulseItemCategory... categories) {
        super();
        this.i18n = i18n;
        setStyleName("navigator");
        this.isTopRow = isTopRow;
        this.showGroupBy = showGroupBy;
        construct(categories);
    }

    private void construct(PulseItemCategory... categories) {
        setSizeUndefined();

        for (final PulseItemCategory category : categories) {
            ItemCategoryTab tab = new ItemCategoryTab(category);
            if (category == PulseItemCategory.ALL_MESSAGES || category == PulseItemCategory.UNCLAIMED || category == PulseItemCategory.TASKS) {
                tab.setActive(true);
            }
            itemCategoryTabs.put(category, tab);
            addComponent(tab);
        }
        if (isTopRow) {
            addStyleName("top-row");
        }

        initCheckbox(categories);
    }

    private void initCheckbox(PulseItemCategory... categories) {
        final String caption = i18n.translate("pulse.items.groupby");
        groupByCheckBox = new CheckBox(StringUtils.abbreviate(caption, 15));
        groupByCheckBox.addStyleName("navigator-grouping");
        groupByCheckBox.setImmediate(true);
        groupByCheckBox.setVisible(false);
        // tooltip
        groupByCheckBox.setDescription(caption);

        if (showGroupBy) {
            addComponent(groupByCheckBox);
            for (final PulseItemCategory category : categories) {
                if (category == PulseItemCategory.ALL_MESSAGES) {
                    enableGroupBy(true);
                }
            }
        }
    }

    public void addGroupingListener(ValueChangeListener listener) {
        groupByCheckBox.addValueChangeListener(listener);
    }

    public void enableGroupBy(boolean enable) {
        groupByCheckBox.setVisible(enable);
    }

    /**
     * Category changed event.
     */
    public static class CategoryChangedEvent extends Component.Event {

        public static final java.lang.reflect.Method ITEM_CATEGORY_CHANGED;

        static {
            try {
                ITEM_CATEGORY_CHANGED = ItemCategoryChangedListener.class.getDeclaredMethod("itemCategoryChanged", new Class[]{CategoryChangedEvent.class});
            } catch (final java.lang.NoSuchMethodException e) {
                throw new java.lang.RuntimeException(e);
            }
        }

        private final PulseItemCategory category;

        public CategoryChangedEvent(Component source, PulseItemCategory category) {
            super(source);
            this.category = category;
        }

        public PulseItemCategory getCategory() {
            return category;
        }
    }

    /**
     * ItemCategoryChangedListener.
     */
    public interface ItemCategoryChangedListener {

        public void itemCategoryChanged(final CategoryChangedEvent event);
    }

    public void addCategoryChangeListener(final ItemCategoryChangedListener listener) {
        addListener("category_changed", CategoryChangedEvent.class, listener, CategoryChangedEvent.ITEM_CATEGORY_CHANGED);
    }

    private void fireCategoryChangedEvent(PulseItemCategory category) {
        Iterator<Component> iterator = iterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (component instanceof ItemCategoryTab) {
                ItemCategoryTab button = (ItemCategoryTab) component;
                button.setActive(button.category == category);
            }
        }
        fireEvent(new CategoryChangedEvent(this, category));
    }

    /**
     * Item category button.
     */
    public class ItemCategoryTab extends HorizontalLayout {

        private final PulseItemCategory category;
        private final Label categoryLabel;
        private final Label badge;

        public ItemCategoryTab(PulseItemCategory category) {
            super();
            this.category = category;
            this.addStyleName("navigator-tab");
            this.setSizeUndefined();

            categoryLabel = new Label(i18n.translate(category.getKey()));
            categoryLabel.addStyleName("category");

            badge = new Label();
            badge.addStyleName("badge");
            if (category == PulseItemCategory.ONGOING) {
                badge.addStyleName("empty-circle-gray");
            }
            badge.setVisible(false);

            this.addComponent(categoryLabel);
            this.addComponent(badge);
            this.addLayoutClickListener(new LayoutClickListener() {

                @Override
                public void layoutClick(LayoutClickEvent event) {
                    fireCategoryChangedEvent(ItemCategoryTab.this.category);
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

        public void updateItemsCount(int count) {
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

        public String getLabel() {
            return categoryLabel.getValue();
        }
    }

    public void updateCategoryBadgeCount(PulseItemCategory category, int count) {
        itemCategoryTabs.get(category).updateItemsCount(count);
    }

    /**
     * Sets the passed category as selected and un-select all the others.
     */
    public void setActive(PulseItemCategory category) {
        for (ItemCategoryTab tab : itemCategoryTabs.values()) {
            tab.setActive(false);
        }
        itemCategoryTabs.get(category).setActive(true);
    }
}
