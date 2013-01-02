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

import info.magnolia.ui.vaadin.gwt.client.layout.VLazyThumbnailLayout;
import info.magnolia.ui.vaadin.integration.serializer.ResourceSerializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gson.GsonBuilder;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Ordered;
import com.vaadin.terminal.KeyMapper;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;

/**
 * Lazy layout of asset thumbnails.
 * 
 */
@ClientWidget(VLazyThumbnailLayout.class)
public class LazyThumbnailLayout extends AbstractComponent implements ServerSideHandler, Container.Viewer {

    private int thumbnailsAmount = 0;

    private int thumbnailWidth = 0;

    private int thumbnailHeight = 0;

    private List<ThumbnailSelectionListener> selectionListeners = new ArrayList<LazyThumbnailLayout.ThumbnailSelectionListener>();

    private List<ThumbnailDblClickListener> dblClickListeners = new ArrayList<LazyThumbnailLayout.ThumbnailDblClickListener>();

    private Ordered container;

    private Object lastQueried = null;

    private KeyMapper mapper = new KeyMapper();

    private ServerSideProxy proxy = new ServerSideProxy(this) {
        {
            register("loadThumbnails", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceSerializer());
                    int amount = (Integer) params[0];
                    final List<Thumbnail> resources = fetchThumbnails(amount);
                    final String gson = gsonBuilder.create().toJson(resources);
                    proxy.call("addThumbnails", gson);
                }
            });

            register("thumbnailSelected", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String key = String.valueOf(params[0]);
                    final Object itemId = mapper.get(key);
                    if (itemId != null) {
                        select(itemId);
                    }
                }
            });

            register("thumbnailDoubleClicked", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String key = String.valueOf(params[0]);
                    final Object itemId = mapper.get(key);
                    if (itemId != null) {
                        onThumbnailDoubleClicked(itemId);
                    }
                }
            });

            register("clear", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    clearSelf();
                }
            });
        }
    };

    public LazyThumbnailLayout() {
        setImmediate(true);
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

    private List<Thumbnail> fetchThumbnails(int amount) {
        List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
        Object id = lastQueried;
        if (id == null) {
            id = container.firstItemId();
        }
        int i = 0;
        while (id != null && i < amount) {
            Resource resource = (Resource) container.getContainerProperty(id, "thumbnail").getValue();
            thumbnails.add(new Thumbnail(mapper.key(id), resource));
            id = container.nextItemId(id);
            ++i;
        }
        lastQueried = id;
        return thumbnails;
    }

    private void setThumbnailAmount(int thumbnailAmount) {
        this.thumbnailsAmount = thumbnailAmount;
        proxy.callOnce("setThumbnailAmount", Math.max(thumbnailAmount, 0));
    }

    public void setThumbnailSize(int width, int height) {
        this.thumbnailWidth = width;
        this.thumbnailHeight = height;
        proxy.callOnce("setThumbnailSize", width, height);
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        proxy.paintContent(target);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
    }

    @Override
    public Object[] initRequestFromClient() {
        refresh();
        proxy.callOnce("setThumbnailSize", thumbnailWidth, thumbnailHeight);
        proxy.callOnce("setThumbnailAmount", thumbnailsAmount);
        return new Object[] {};
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unknown client side call: " + method);
    }

    public void clear() {
        clearSelf();
        proxy.callOnce("clear");
    }

    private void clearSelf() {
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
            requestRepaint();
        } else {
            throw new IllegalArgumentException("Container must be ordered.");
        }
    }

    @Override
    public Ordered getContainerDataSource() {
        return container;
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

    /**
     * DTO object for thumbnails.
     */
    public static class Thumbnail implements Serializable {

        private final String id;

        private Resource resource;

        public Thumbnail(String id, Resource resource) {
            this.id = id;
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }

        public String getId() {
            return id;
        }
    }

}
