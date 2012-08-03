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

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractInMemoryContainer;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.terminal.Resource;

/**
 * Container that provides thumbnails lazily.
 */
public class ThumbnailContainer extends AbstractInMemoryContainer<String, Resource, ThumbnailItem> {
    
    private ThumbnailProvider thumbnailProvider;
    
    public ThumbnailContainer() {
        super.addContainerProperty("thumbnail", Resource.class, null);
    }
    
    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
        return Arrays.asList("thumbnail");
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
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
    protected ThumbnailItem getUnfilteredItem(Object itemId) {
        return null;
    }

    /**
     * ThumbnailContainer property.
     */
    public class ThumbnailContainerProperty extends AbstractProperty {

        private String itemId;
        
        @Override
        public Object getValue() {
            return null;
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

        @Override
        public Property getItemProperty(Object id) {
            return null;
        }

        @Override
        public Collection<?> getItemPropertyIds() {
            return null;
        }

        @Override
        public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
            return false;
        }

        @Override
        public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
            return false;
        }
        
    }
}
