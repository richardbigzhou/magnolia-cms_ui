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
package info.magnolia.ui.vaadin.layout;

import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector.ThumbnailLayoutState;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutClientRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutServerRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.shared.ThumbnailData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Ordered;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;

/**
 * Lazy layout of asset thumbnails.
 */
public class LazyThumbnailLayout extends AbstractComponent implements Container.Viewer {

    private final List<ThumbnailSelectionListener> selectionListeners = new ArrayList<LazyThumbnailLayout.ThumbnailSelectionListener>();

    private final List<ThumbnailDblClickListener> dblClickListeners = new ArrayList<LazyThumbnailLayout.ThumbnailDblClickListener>();

    private final List<ThumbnailRightClickListener> rightClickListeners = new ArrayList<LazyThumbnailLayout.ThumbnailRightClickListener>();

    private Ordered container;

    // Maps thumbnailId to itemId
    private final BiMap<String, String> mapper = HashBiMap.create();
    private final AtomicInteger counter = new AtomicInteger();

    private String selectedItemId;

    private final ThumbnailLayoutServerRpc rpcHandler = new ThumbnailLayoutServerRpc() {

        @Override
        public void onThumbnailSelected(String id) {
            final Object itemId = mapper.get(id);
            if (itemId != null) {
                LazyThumbnailLayout.this.onThumbnailSelected(itemId);
            }
        }

        @Override
        public void onThumbnailDoubleClicked(String id) {
            final Object itemId = mapper.get(id);
            if (itemId != null) {
                LazyThumbnailLayout.this.onThumbnailDoubleClicked(itemId);
            }
        }

        @Override
        public void onThumbnailRightClicked(String id, int clickX, int clickY) {
            final Object itemId = mapper.get(id);
            if (itemId != null) {
                LazyThumbnailLayout.this.onThumbnailRightClicked(itemId, clickX, clickY);
            }
        }

        @Override
        public void loadThumbnails(int amount) {
            getRpcProxy(ThumbnailLayoutClientRpc.class).addThumbnails(fetchThumbnails(amount));
            getRpcProxy(ThumbnailLayoutClientRpc.class).setSelected(mapper.inverse().get(selectedItemId));
        }

        @Override
        public void clearThumbnails() {
            LazyThumbnailLayout.this.clear();
        }

    };

    public LazyThumbnailLayout() {
        setImmediate(true);
        registerRpc(rpcHandler);
    }

    private void onThumbnailDoubleClicked(Object itemId) {
        for (final ThumbnailDblClickListener listener : dblClickListeners) {
            listener.onThumbnailDblClicked(String.valueOf(itemId));
        }
    }

    private void onThumbnailRightClicked(Object itemId, int clickX, int clickY) {
        for (final ThumbnailRightClickListener listener : rightClickListeners) {
            listener.onThumbnailRightClicked(String.valueOf(itemId), clickX, clickY);
        }
    }

    private void onThumbnailSelected(Object itemId) {
        for (final ThumbnailSelectionListener listener : selectionListeners) {
            listener.onThumbnailSelected(String.valueOf(itemId));
        }
    }

    private List<ThumbnailData> fetchThumbnails(int amount) {
        List<ThumbnailData> thumbnails = new ArrayList<ThumbnailData>();
        Object id = mapper.get(getState().lastQueried);
        if (id == null) {
            id = container.firstItemId();
        }
        int i = 0;
        while (id != null && i < amount) {
            Object resource = container.getContainerProperty(id, "thumbnail").getValue();
            boolean isRealResource = resource instanceof Resource;
            String thumbnailId = mapItemIdToThumbnailId((String) id);
            String iconFontId = isRealResource ? null : String.valueOf(resource);
            if (isRealResource) {
                setResource(thumbnailId, (Resource) resource);
            }
            thumbnails.add(new ThumbnailData(thumbnailId, iconFontId, isRealResource));
            id = container.nextItemId(id);
            ++i;
        }
        getState().lastQueried = StringUtils.defaultString(mapItemIdToThumbnailId((String) id), "null");
        return thumbnails;
    }

    /**
     * Adds the itemId to the internal mapping or if it's already mapped returns the existing key.
     */
    private String mapItemIdToThumbnailId(String itemId) {
        if (itemId == null) {
            return null;
        }
        String thumbnailId = mapper.inverse().get(itemId);
        if (thumbnailId != null) {
            return thumbnailId;
        }
        thumbnailId = String.valueOf(counter.incrementAndGet());
        mapper.put(thumbnailId, itemId);
        return thumbnailId;
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

    public void clear() {
        getState().resources.clear();
        getState().lastQueried = null;
        counter.set(0);
        mapper.clear();
    }

    public void refresh() {
        if (getState(false).thumbnailAmount > 0) {
            clear();
        }
        if (container != null) {
            setThumbnailAmount(container.size());
        }
    }

    public void addThumbnailSelectionListener(final ThumbnailSelectionListener listener) {
        this.selectionListeners.add(listener);
    }

    public void addDoubleClickListener(final ThumbnailDblClickListener listener) {
        this.dblClickListeners.add(listener);
    }

    public void addRightClickListener(final ThumbnailRightClickListener listener) {
        this.rightClickListeners.add(listener);
    }

    @Override
    public void setContainerDataSource(Container newDataSource) {
        if (newDataSource instanceof Ordered) {
            this.container = (Ordered) newDataSource;
            refresh();
        } else {
            throw new IllegalArgumentException("Container must be ordered.");
        }
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

    public void setSelectedItemId(String selectedItemId) {
        this.selectedItemId = selectedItemId;
    }

    /**
     * Listener interface for thumbnail selection.
     */
    public interface ThumbnailSelectionListener {
        void onThumbnailSelected(String itemId);
    }

    /**
     * Listener for thumbnail double clicks.
     */
    public interface ThumbnailDblClickListener {
        void onThumbnailDblClicked(String itemId);
    }

    /**
     * Listener for thumbnail right clicks.
     */
    public interface ThumbnailRightClickListener {
        void onThumbnailRightClicked(String itemId, int clickX, int clickY);
    }

    /**
     * Interface for the providers of the actual thumbnails.
     */
    public interface LazyThumbnailProvider {

        void refresh();

        int getThumbnailsAmount();

        List<Resource> getThumbnails(int amount);

    }

}
