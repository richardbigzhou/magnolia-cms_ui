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
package info.magnolia.ui.vaadin.layout;

import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector.ThumbnailLayoutState;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutClientRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutServerRpc;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Ordered;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;

/**
 * Lazy layout of asset thumbnails.
 *
 */
public class LazyThumbnailLayout extends AbstractComponent implements Container.Viewer {

    private final List<ThumbnailSelectionListener> selectionListeners = new ArrayList<LazyThumbnailLayout.ThumbnailSelectionListener>();

    private final List<ThumbnailDblClickListener> dblClickListeners = new ArrayList<LazyThumbnailLayout.ThumbnailDblClickListener>();

    private Ordered container;

    private Object lastQueried = null;

    private final KeyMapper<Object> mapper = new KeyMapper<Object>();

    private final ThumbnailLayoutServerRpc rpcHandler = new ThumbnailLayoutServerRpc() {
        @Override
        public void onThumbnailSelected(String id) {
            final Object itemId = mapper.get(id);
            if (itemId != null) {
                select(itemId);
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
        public void loadThumbnails(int amount) {
            getRpcProxy(ThumbnailLayoutClientRpc.class).addThumbnails(fetchThumbnails(amount));
        }

        @Override
        public void clear() {
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

    private void select(Object itemId) {
        for (final ThumbnailSelectionListener listener : selectionListeners) {
            listener.onThumbnailSelected(String.valueOf(itemId));
        }
    }

    private List<String> fetchThumbnails(int amount) {
        List<String> thumbnails = new ArrayList<String>();
        Object id = lastQueried;
        if (id == null) {
            id = container.firstItemId();
        }
        int i = 0;
        while (id != null && i < amount) {
            Resource resource = (Resource) container.getContainerProperty(id, "thumbnail").getValue();
            setResource(mapper.key(id), resource);
            thumbnails.add(mapper.key(id));
            id = container.nextItemId(id);
            ++i;
        }
        lastQueried = id;
        return thumbnails;
    }

    private void setThumbnailAmount(int thumbnailAmount) {
        getState().thumbnailsAmount = Math.max(thumbnailAmount, 0);
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
        getRpcProxy(ThumbnailLayoutClientRpc.class).clear();
        for (final String key : getState().resources.keySet()) {
            setResource(key, null);
        }
        lastQueried = null;
        mapper.removeAll();

    }


    public void refresh() {
        clear();
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

    @Override
    public void setContainerDataSource(Container newDataSource) {
        if (newDataSource instanceof Ordered) {
            this.container = (Ordered) newDataSource;
            refresh();
            markAsDirty();
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
        return (ThumbnailLayoutState)super.getState();
    }
    
    @Override
    protected ThumbnailLayoutState getState(boolean markAsDirty) {
        return (ThumbnailLayoutState)super.getState(markAsDirty);
    }
    
    /**
     * Listener interface for thumbnail selection.
     */
    public interface ThumbnailSelectionListener {
        void onThumbnailSelected(String thumbnailId);
    }

    /**
     * Listener for thumbnail double clicks.
     */
    public interface ThumbnailDblClickListener {
        void onThumbnailDblClicked(String thumbnailId);
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
