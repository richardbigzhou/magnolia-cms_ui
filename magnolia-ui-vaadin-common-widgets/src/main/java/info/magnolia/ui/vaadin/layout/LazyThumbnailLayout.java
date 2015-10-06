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
package info.magnolia.ui.vaadin.layout;

import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector.ThumbnailLayoutState;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutClientRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutServerRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.shared.ThumbnailData;
import info.magnolia.ui.vaadin.gwt.shared.Range;
import info.magnolia.ui.vaadin.layout.data.PagingThumbnailContainer;
import info.magnolia.ui.vaadin.layout.data.ThumbnailContainer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Ordered;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;

/**
 * Lazy layout of asset thumbnails.
 */
public class LazyThumbnailLayout extends AbstractComponent implements Container.Viewer, Container.ItemSetChangeListener {

    private static Logger log = LoggerFactory.getLogger(LazyThumbnailLayout.class);

    private final List<ThumbnailSelectionListener> selectionListeners = new ArrayList<>();

    private final List<ThumbnailDblClickListener> dblClickListeners = new ArrayList<>();

    private final List<ThumbnailRightClickListener> rightClickListeners = new ArrayList<>();

    private final Set<Object> selectedIds = new HashSet<>();

    private DataProviderKeyMapper mapper = new DataProviderKeyMapper();

    private ThumbnailContainer container;

    private ThumbnailLayoutClientRpc clientRpc;

    private final ThumbnailLayoutServerRpc rpcHandler = new ThumbnailLayoutServerRpc() {

        @Override
        public void loadThumbnails(int startFrom, int length, int cachedFirst, int cachedLast) {
            final Range rangeToLoad = Range.withLength(startFrom, length);
            final Range cachedRange = Range.between(cachedFirst, cachedLast);
            final Range activeRange = cachedRange.isEmpty() ? rangeToLoad : rangeToLoad.combineWith(cachedRange);

            mapper.setActiveRange(activeRange);
            clientRpc.addThumbnails(fetchThumbnails(rangeToLoad), rangeToLoad.getStart());
        }

        @Override
        public void onThumbnailSelected(int index, boolean isMetaKeyPressed, boolean isShiftKeyPressed) {
            handleSelectionAtIndex(index, isMetaKeyPressed || isShiftKeyPressed);
            fireSelectionChange();
        }

        @Override
        public void onThumbnailDoubleClicked(int index) {
            final Object itemId = mapper.itemIdAtIndex(index);
            if (itemId != null) {
                LazyThumbnailLayout.this.onThumbnailDoubleClicked(itemId);
            }
        }

        @Override
        public void onThumbnailRightClicked(int index, int clickX, int clickY) {
            final Object itemId = mapper.itemIdAtIndex(index);
            if (itemId != null) {
                LazyThumbnailLayout.this.onThumbnailRightClicked(itemId, clickX, clickY);
            }
        }

        @Override
        public void updateOffset(int currentThumbnailOffset) {
            getState(false).offset = currentThumbnailOffset;
        }

        @Override
        public void setScaleRatio(float ratio) {
            getState(false).scaleRatio = ratio;
        }

    };

    private void handleSelectionAtIndex(int index, boolean isMultiple) {
        if (isMultiple) {
            getState().selection.toggleMultiSelection(index);
        } else {
            getState().selection.toggleSelection(index);
        }

        updateSelectedIds();
    }

    private void fireSelectionChange() {
        if (selectedIds.size() == 1) {
            this.onThumbnailSelected(selectedIds.iterator().next());
        } else {
            this.onThumbnailsSelected(selectedIds);
        }
    }

    private void updateSelectedIds() {
        selectedIds.clear();
        selectedIds.addAll(Lists.transform(getState().selection.selectedIndices, new Function<Integer, Object>() {
            @Override
            public Object apply(Integer input) {
                return container.getIdByIndex(input.intValue());
            }
        }));
    }

    public LazyThumbnailLayout() {
        setImmediate(true);
        registerRpc(rpcHandler);
        clientRpc = getRpcProxy(ThumbnailLayoutClientRpc.class);
    }

    private void onThumbnailDoubleClicked(Object itemId) {
        for (final ThumbnailDblClickListener listener : dblClickListeners) {
            listener.onThumbnailDblClicked(itemId);
        }
    }

    private void onThumbnailRightClicked(Object itemId, int clickX, int clickY) {
        for (final ThumbnailRightClickListener listener : rightClickListeners) {
            listener.onThumbnailRightClicked(itemId, clickX, clickY);
        }
    }

    private void onThumbnailsSelected(Set<Object> ids) {
        for (final ThumbnailSelectionListener listener : selectionListeners) {
            listener.onThumbnailsSelected(ids);
        }
    }

    /**
     * @deprecated since 5.3.9 - more generic {@link #onThumbnailsSelected(java.util.Set)} should be used instead.
     */
    @Deprecated
    private void onThumbnailSelected(Object itemId) {
        for (final ThumbnailSelectionListener listener : selectionListeners) {
            listener.onThumbnailSelected(itemId);
        }
    }

    private List<ThumbnailData> fetchThumbnails(Range range) {
        final List<ThumbnailData> thumbnails = new ArrayList<>(range.length());
        for (int i = range.getStart(); i < range.getEnd(); ++i) {
            final Object id = mapper.itemIdAtIndex(i);
            final Object resource = container.getThumbnailProperty(id).getValue();

            boolean isRealResource = resource instanceof Resource;
            String thumbnailId = mapper.getKey(id);
            String iconFontId = isRealResource ? null : String.valueOf(resource);
            if (isRealResource) {
                setResource(thumbnailId, (Resource) resource);
            }
            thumbnails.add(new ThumbnailData(thumbnailId, iconFontId, isRealResource));
        }
        return thumbnails;
    }

    private void setThumbnailAmount(int thumbnailAmount) {
        getState().thumbnailAmount = Math.max(thumbnailAmount, 0);
    }

    public void setThumbnailSize(int width, int height) {
        getState().size.height = height;
        getState().size.width = width;
    }

    public int getThumbnailWidth() {
        return getState(false).size.width;
    }

    public int getThumbnailHeight() {
        return getState(false).size.height;
    }

    public void refresh() {
        if (getState(false).thumbnailAmount > 0) {
            getState().resources.clear();
            mapper.clearAll();
        }

        if (container != null) {
            setThumbnailAmount(container.size());

            if (getState().offset > container.size()) {
                getState().offset = 0;
            }
        }

        synchroniseSelection();
        clientRpc.refresh();
    }

    public void addThumbnailSelectionListener(final ThumbnailSelectionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Selection listener cannot be null!");
        }
        this.selectionListeners.add(listener);
    }

    public void addDoubleClickListener(final ThumbnailDblClickListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Double click listener cannot be null!");
        }
        this.dblClickListeners.add(listener);
    }

    public void addRightClickListener(final ThumbnailRightClickListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Right click listener cannot be null!");
        }
        this.rightClickListeners.add(listener);
    }

    @Override
    public void setContainerDataSource(Container newDataSource) {
        if (!(newDataSource instanceof ThumbnailContainer)) {
            throw new IllegalArgumentException("Container must implement info.magnolia.ui.vaadin.layout.data.ThumbnailContainer...");
        }

        if (this.container instanceof Container.ItemSetChangeNotifier) {
            ((Container.ItemSetChangeNotifier) this.container).removeItemSetChangeListener(this);
        }

        this.container = (ThumbnailContainer) newDataSource;

        if (this.container instanceof Container.ItemSetChangeNotifier) {
            ((Container.ItemSetChangeNotifier) this.container).addItemSetChangeListener(this);
        }

        refresh();

    }

    @Override
    public Ordered getContainerDataSource() {
        return container;
    }

    @Override
    protected ThumbnailLayoutState getState() {
        return (ThumbnailLayoutState) super.getState();
    }

    @Override
    protected ThumbnailLayoutState getState(boolean markAsDirty) {
        return (ThumbnailLayoutState) super.getState(markAsDirty);
    }

    public void setSelectedItemId(Object selectedItemId) {
        if (selectedItemId == null) {
            this.getState().selection.selectedIndices.clear();
        } else {
            this.getState().selection.toggleSelection(-1);
            this.getState().selection.toggleSelection(container.indexOfId(selectedItemId));
            updateSelectedIds();
        }
    }

    @Override
    public void containerItemSetChange(Container.ItemSetChangeEvent event) {
        refresh();
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        getState().isFirstUpdate &= initial;
    }

    /**
     * Since the item set changed - the indices in the state might now point to the different items.
     * Since we know which items to select via {@code selectedIds}, we can update the indices in state as well.
     */
    private void synchroniseSelection() {
        final List<Integer> formerSelectedIndices = getState().selection.selectedIndices;
        getState().selection.toggleSelection(-1);

        for (Object id : formerSelectedIndices) {
            if (getContainerDataSource().containsId(id)) {
                handleSelectionAtIndex(container.indexOfId(id), true);
            }
        }

        updateSelectedIds();
        fireSelectionChange();
    }

    /**
     * Maps item ids, indices and client-side keys to each other.
     * Highly inspired by Vaadin analogous class used in Grid component implementation
     * (introduced in Vaadin 7.4).
     */
    private class DataProviderKeyMapper implements Serializable {

        private final BiMap<Integer, Object> indexToItemId = HashBiMap.create();

        private final BiMap<Object, String> itemIdToKey = HashBiMap.create();

        private Range activeRange = Range.withLength(0, 0);

        private long rollingIndex = 0;

        private DataProviderKeyMapper() {
        }

        void setActiveRange(Range newActiveRange) {

            /**
             * First update container's page size if needed - in order to avoid multiple queries to
             * the datasource.
             */
            if (container instanceof PagingThumbnailContainer) {
                ((PagingThumbnailContainer) container).setPageSize(newActiveRange.length());
            }

            final Range[] removed = activeRange.partitionWith(newActiveRange);
            final Range[] added = newActiveRange.partitionWith(activeRange);

            removeActiveThumbnails(removed[0]);
            removeActiveThumbnails(removed[2]);
            addActiveThumbnails(added[0]);
            addActiveThumbnails(added[2]);

            log.debug("Former active: {}, New Active: {}, idx-id: {}, id-key: {}. Removed: {} and {}, Added: {} and {}",
                    activeRange,
                    newActiveRange,
                    indexToItemId.size(),
                    itemIdToKey.size(),
                    removed[0],
                    removed[2],
                    added[0],
                    added[2]);

            activeRange = newActiveRange;

        }

        private void removeActiveThumbnails(final Range deprecated) {
            for (int i = deprecated.getStart(); i < deprecated.getEnd(); i++) {
                final Object itemId = indexToItemId.get(i);

                itemIdToKey.remove(itemId);
                indexToItemId.remove(i);
            }
        }

        private void addActiveThumbnails(Range added) {
            if (added.isEmpty()) {
                return;
            }

            List<?> newItemIds = container.getItemIds(added.getStart(), added.length());
            Integer index = added.getStart();
            for (Object itemId : newItemIds) {
                if (!indexToItemId.containsKey(index)) {
                    if (!itemIdToKey.containsKey(itemId)) {
                        itemIdToKey.put(itemId, nextKey());
                    }

                    indexToItemId.forcePut(index, itemId);
                }
                index++;
            }
        }

        private String nextKey() {
            return String.valueOf(rollingIndex++);
        }

        String getKey(Object itemId) {
            String key = itemIdToKey.get(itemId);
            if (key == null) {
                key = nextKey();
                itemIdToKey.put(itemId, key);
            }
            return key;
        }

        public Object getItemId(String key) throws IllegalStateException {
            Object itemId = itemIdToKey.inverse().get(key);
            if (itemId != null) {
                return itemId;
            } else {
                throw new IllegalStateException("No item id for key " + key + " found.");
            }
        }

        public Collection<Object> getItemIds(Collection<String> keys)
                throws IllegalStateException {
            if (keys == null) {
                throw new IllegalArgumentException("keys may not be null");
            }

            final List<Object> itemIds = new ArrayList<>(keys.size());
            for (String key : keys) {
                itemIds.add(getItemId(key));
            }
            return itemIds;
        }

        Object itemIdAtIndex(int index) {
            return indexToItemId.get(index);
        }

        int indexOf(Object itemId) {
            return indexToItemId.inverse().get(itemId);
        }

        public void clearAll() {
            indexToItemId.clear();
            itemIdToKey.clear();
            rollingIndex = 0;
            activeRange = Range.withLength(0, 0);
        }
    }

    /**
     * Listener interface for thumbnail selection.
     */
    public interface ThumbnailSelectionListener {

        /**
         * @deprecated since 5.3.9 - more generic {@link #onThumbnailsSelected(java.util.Set)} should be used.
         */
        @Deprecated
        void onThumbnailSelected(Object itemId);

        void onThumbnailsSelected(Set<Object> ids);

    }

    /**
     * Listener for thumbnail double clicks.
     */
    public interface ThumbnailDblClickListener {
        void onThumbnailDblClicked(Object itemId);
    }

    /**
     * Listener for thumbnail right clicks.
     */
    public interface ThumbnailRightClickListener {
        void onThumbnailRightClicked(Object itemId, int clickX, int clickY);
    }
}
