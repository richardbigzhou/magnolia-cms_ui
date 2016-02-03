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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.ui.admincentral.shellapp.pulse.message.data.MessageConstants;
import info.magnolia.ui.admincentral.shellapp.pulse.task.data.TaskConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsIterableContainingInOrder;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class PulseMatchers {

    /**
     * Checks whether the provided list of Vaadin items is of expected size and contains valid message items
     * (i.e. contain properties described in {@link MessageConstants}).
     */
    public static Matcher<Iterable<? extends Item>> containsAmountOfMessageItems(int size) {
        final List<Matcher<? super Item>> matchers = Lists.newArrayListWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            matchers.add(isMessageItem());
        }
        return new IsIterableContainingInOrder<Item>(matchers);
    }

    /**
     * Checks whether the provided list of Vaadin items is of expected size and contains valid task items
     * (i.e. contain properties described in {@link TaskConstants}).
     */
    public static Matcher<Iterable<? extends Item>> containsAmountOfTaskItems(int size) {
        final List<Matcher<? super Item>> matchers = Lists.newArrayListWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            matchers.add(isTaskItem());
        }
        return new IsIterableContainingInOrder<Item>(matchers);
    }

    /**
     * Checks whether the provided list of Vaadin items has a certain order of item ids (values of
     * <b>id</b> property).
     */
    public static Matcher<Iterable<? extends Item>> containsIdsInOrder(Object... ids) {
        final List<Matcher<? super Item>> itemMatchers = Lists.newLinkedList();
        for (Object id : ids) {
            itemMatchers.add(ItemWithId.itemWithId(id));
        }
        return new IsIterableContainingInOrder<Item>(itemMatchers);
    }

    private static class ItemWithId<T extends Item> extends TypeSafeMatcher<T> {

        private Object id;

        public ItemWithId(Object id) {
            this.id = id;
        }

        @Override
        protected boolean matchesSafely(T item) {
            if (item != null) {
                final Property<?> idProperty = item.getItemProperty("id");
                if (idProperty != null) {
                    return (String.valueOf(id).equals(idProperty.getValue()));
                }
            }
            return false;
        }

        @Override
        protected void describeMismatchSafely(T item, Description mismatchDescription) {
            if (item == null) {
                mismatchDescription.appendText("item is null");
            } else {
                final Property<?> idProperty = item.getItemProperty("id");
                if (idProperty == null) {
                    mismatchDescription.appendText("item without property <id>");
                } else {
                    mismatchDescription.appendText(String.format("item with <id> = %s", id));
                }
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format("item with <id> = %s", id));
        }

        @Factory
        public static <T extends Item> ItemWithId<T> itemWithId(Object id) {
            return new ItemWithId<T>(id);
        }
    }

    private static <T extends Item> IsItemWithProperties<T> isTaskItem() {
        return new IsItemWithProperties<T>(new String[]{
                TaskConstants.ID,
                TaskConstants.STATUS_PROPERTY_ID,
                TaskConstants.NEW_PROPERTY_ID,
                TaskConstants.TASK_PROPERTY_ID,
                TaskConstants.SENDER_PROPERTY_ID,
                TaskConstants.LAST_CHANGE_PROPERTY_ID,
                TaskConstants.ASSIGNED_TO_PROPERTY_ID});
    }

    private static <T extends Item> IsItemWithProperties<T> isMessageItem() {
        return new IsItemWithProperties<T>(new String[]{
                MessageConstants.NEW_PROPERTY_ID,
                MessageConstants.SUBJECT_PROPERTY_ID,
                MessageConstants.TYPE_PROPERTY_ID,
                MessageConstants.TEXT_PROPERTY_ID,
                MessageConstants.SENDER_PROPERTY_ID,
                MessageConstants.DATE_PROPERTY_ID});
    }

    private static class IsItemWithProperties<T extends Item> extends TypeSafeMatcher<T> {

        private String[] properties;

        public IsItemWithProperties(String[] properties) {
            this.properties = properties;
        }

        private Collection<String> getMissingPropertyIds(final T item) {
            return Collections2.filter(Arrays.asList(properties), new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return item.getItemProperty(input) == null;
                }
            });
        }

        @Override
        protected boolean matchesSafely(T item) {
            return item != null && getMissingPropertyIds(item).isEmpty();
        }

        @Override
        protected void describeMismatchSafely(T item, Description mismatchDescription) {
            super.describeMismatchSafely(item, mismatchDescription);
            if (item == null) {
                mismatchDescription.appendText("NULL");
            } else {
                final Collection<String> missingPids = getMissingPropertyIds(item);
                mismatchDescription.appendText(" item with missing properties: " + Arrays.toString(missingPids.toArray()));
            }
        }

        @Override
        public void describeTo(Description description) {
            StringBuilder sb = new StringBuilder("item with properties: ").append(Arrays.toString(properties));
            description.appendText(sb.toString());
        }

    }
}
