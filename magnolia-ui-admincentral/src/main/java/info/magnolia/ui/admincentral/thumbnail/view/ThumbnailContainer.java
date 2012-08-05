/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.thumbnail.view;

import info.magnolia.ui.admincentral.thumbnail.view.ThumbnailContainer.ThumbnailItem;
import info.magnolia.ui.model.thumbnail.ThumbnailProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;

/**
 * Container that provides thumbnails lazily.
 */
public class ThumbnailContainer extends AbstractInMemoryContainer<String, Resource, ThumbnailItem> implements Container.Ordered {
    
    private ThumbnailProvider thumbnailProvider;

    private String workspaceName = "";

    private int thumbnailWidth = 0;

    private int thumbnailHeight = 0;

    public ThumbnailContainer(ThumbnailProvider thumbnailProvider, List<String> uuids) {
        super();
        this.thumbnailProvider = thumbnailProvider;
        getAllItemIds().addAll(uuids);
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        return Arrays.asList("thumbnail");
    }

    @Override
    public ThumbnailContainerProperty getContainerProperty(Object itemId, Object propertyId) {
        if ("thumbnail".equals(propertyId)) {
            return new ThumbnailContainerProperty(String.valueOf(itemId));
        }
        return null;
    }

    @Override
    public Class<?> getType(Object propertyId) {
        if ("thumbnail".equals(propertyId)) {
            return Resource.class;
        }
        return null;
    }
    
    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ThumbnailItem getUnfilteredItem(Object itemId) {
        return new ThumbnailItem(String.valueOf(itemId));
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    /**
     * ThumbnailContainer property.
     */
    public class ThumbnailContainerProperty extends AbstractProperty {

        private String itemId;

        public ThumbnailContainerProperty(final String itemId) {
            this.itemId = itemId;
        }

        @Override
        public Resource getValue() {
            return new ExternalResource(thumbnailProvider.getPath(itemId, getWorkspaceName(), getThumbnailWidth(), getThumbnailHeight()));
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
            if (!(newValue instanceof Resource)) {
                throw new IllegalArgumentException("Only accepts resources!");
            }

        }

        @Override
        public Class<Resource> getType() {
            return Resource.class;
        }

    }

    /**
     * Thumbnail Item.
     */
    public class ThumbnailItem implements Item {

        private String id;

        public ThumbnailItem(final String id) {
            this.id = id;
        }

        @Override
        public Property getItemProperty(Object id) {
            if ("thumbnail".equals(id)) {
                return new ThumbnailContainerProperty(this.id);
            }
            return null;
        }

        @Override
        public Collection<?> getItemPropertyIds() {
            return Arrays.asList("thumbnail");
        }

        @Override
        public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

    }
}
