/**
 * This file Copyright (c) 2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.data;

import info.magnolia.ui.vaadin.gwt.shared.Range;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.lazyquerycontainer.Query;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Skeleton {@link Query} implementation for accessing Pulse-related objects via corresponding services.
 * Converts objects into {@link PropertysetItem}s, supporting sorting and grouping by entity types.
 *
 * @param <T> type of queried objects (e.g. {@link info.magnolia.ui.api.message.Message})
 * @param <ET> enumeration type corresponding to the queried objects (e.g. {@link info.magnolia.ui.api.message.MessageType})
 */
public abstract class LazyPulseQuery<ET, T> implements Query {

    private static Logger log = LoggerFactory.getLogger(LazyPulseQuery.class);

    private final PulseQueryDefinition<ET> queryDefinition;

    private final Map<ET, Range> amountPerType = Maps.newLinkedHashMap();

    private long size = -1;

    private final Function<T, Item> objectToItem = new Function<T, Item>() {
        @Nullable
        @Override
        public Item apply(T object) {
            final Item item = constructItem();
            mapObjectToItem(object, item);
            return item;
        }
    };

    protected abstract void mapObjectToItem(T object, Item item);

    protected abstract long getEntriesAmount(List<ET> types);

    protected abstract List<T> getEntries(List<ET> types, int limit, int offset);

    protected abstract T createGroupingEntry(ET type);

    public LazyPulseQuery(PulseQueryDefinition<ET> queryDefinition) {
        this.queryDefinition = queryDefinition;
    }

    @Override
    public int size() {
        if (size >= 0) {
            return Long.valueOf(size).intValue();
        }

        if (!getQueryDefinition().isGroupingByType()) {
            size = getEntriesAmount(getQueryDefinition().types());
        } else {
            calculateMessagesPerType();

            size = 0;
            final Iterator<Map.Entry<ET, Range>> it = amountPerType.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<ET, Range> entry = it.next();
                size += entry.getValue().length();
            }
        }

        return Long.valueOf(size).intValue();
    }

    @Override
    public List<Item> loadItems(int startIndex, int count) {
        long timeMs = System.currentTimeMillis();
        List<Item> transform = Lists.transform(getObjects(startIndex, count), objectToItem);
        log.debug("Loaded {} items from {} in {}ms", count, startIndex, System.currentTimeMillis() - timeMs);
        return transform;
    }

    @Override
    public Item constructItem() {
        return new PropertysetItem();
    }

    protected PulseQueryDefinition<ET> getQueryDefinition() {
        return queryDefinition;
    }

    protected List<T> getObjects(int startIndex, int count) {
        // Querying messages as is
        if (!getQueryDefinition().isGroupingByType()) {
            return getEntries(getQueryDefinition().types(), count, startIndex);
            // Have to query messages per type, maybe as a result of two queries - if the span spans across several message type ranges
        } else {
            if (size < 0) {
                // Technically #size() will be always called before #getObjects(). Maybe except for the test cases.
                // simply call #size() to trigger all the necessary calculations in such case.
                size();
            }
            Range queriedRange = Range.withLength(startIndex, count);
            final ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
            final Iterator<Map.Entry<ET, Range>> it = amountPerType.entrySet().iterator();
            while (it.hasNext() && !queriedRange.isEmpty()) {
                final Map.Entry<ET, Range> entry = it.next();
                final Range typeRange = entry.getValue();
                if (typeRange.intersects(queriedRange)) {
                    final Range[] partition = queriedRange.partitionWith(typeRange);
                    final Range rangeOfMessageTypeToLoad = partition[1].expand(typeRange.getStart(), -typeRange.getStart());
                    List<T> messagesOfType = getEntriesOfType(entry.getKey(), rangeOfMessageTypeToLoad);
                    listBuilder.addAll(messagesOfType);
                    queriedRange = partition[2];
                }
            }

            return listBuilder.build();
        }
    }

    protected void calculateMessagesPerType() {
        amountPerType.clear();
        int offset = 0;
        for (ET type : getQueryDefinition().types()) {
            long amount = getEntriesAmount(ImmutableList.of(type));
            if (amount > 0) {
                amountPerType.put(type, Range.withLength(offset, Long.valueOf(amount + 1).intValue()));
                offset += amount + 1;
            }
        }
    }

    /**
     * Aggregates query sorting criteria into a map with property names as keys, and {@code boolean} values where
     * {@code true} stands for <b>ascending</b> sorting direction.
     */
    protected Map<String, Boolean> getSortCriteria() {
        final Map<String, Boolean> result = Maps.newHashMap();
        Object[] sortPropertyIds = getQueryDefinition().getSortPropertyIds();
        for (int i = 0; i < sortPropertyIds.length; ++i) {
            result.put((String) sortPropertyIds[i], getQueryDefinition().getSortPropertyAscendingStates()[i]);
        }
        return result;
    }

    protected List<T> getEntriesOfType(ET type, Range range) {
        final ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
        Range rangeToQuery = range;
        if (rangeToQuery.getStart() == 0) {
            listBuilder.add(createGroupingEntry(type));
            rangeToQuery = rangeToQuery.expand(0, -1);
        } else {
            rangeToQuery = rangeToQuery.expand(1, -1);
        }

        if (!rangeToQuery.isEmpty()) {
            listBuilder.addAll(getEntries(ImmutableList.of(type), rangeToQuery.length(), rangeToQuery.getStart()));
        }

        return listBuilder.build();
    }

    @Override
    public void saveItems(List<Item> addedItems, List<Item> modifiedItems, List<Item> removedItems) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteAllItems() {
        throw new UnsupportedOperationException();
    }
}
