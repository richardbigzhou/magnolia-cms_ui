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
package info.magnolia.ui.workbench.thumbnail.data;

import info.magnolia.ui.vaadin.layout.data.ThumbnailContainer;

import java.util.Collection;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Abstract implementation of {@link ThumbnailContainer} which delegates most of the Vaadin {@link Container.Indexed}
 * operations to some other container.
 *
 * @param <T> type of the delegate container, restricted to {@link Container.Indexed}
 */
public abstract class DelegatingThumbnailContainer<T extends Container.Indexed> implements Container.Indexed, ThumbnailContainer {

    public T getDelegate() {
        return delegate;
    }

    private T delegate;

    public DelegatingThumbnailContainer(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public int indexOfId(Object itemId) {
        return delegate.indexOfId(itemId);
    }

    @Override
    public Object getIdByIndex(int index) {
        return delegate.getIdByIndex(index);
    }

    @Override
    public List<?> getItemIds(int startIndex, int numberOfItems) {
        return delegate.getItemIds(startIndex, numberOfItems);
    }

    @Override
    public Object addItemAt(int index) throws UnsupportedOperationException {
        return delegate.addItemAt(index);
    }

    @Override
    public Item addItemAt(int index, Object newItemId) throws UnsupportedOperationException {
        return delegate.addItemAt(index, newItemId);
    }

    @Override
    public Object nextItemId(Object itemId) {
        return delegate.nextItemId(itemId);
    }

    @Override
    public Object prevItemId(Object itemId) {
        return delegate.prevItemId(itemId);
    }

    @Override
    public Object firstItemId() {
        return delegate.firstItemId();
    }

    @Override
    public Object lastItemId() {
        return delegate.lastItemId();
    }

    @Override
    public boolean isFirstId(Object itemId) {
        return delegate.isFirstId(itemId);
    }

    @Override
    public boolean isLastId(Object itemId) {
        return delegate.isLastId(itemId);
    }

    @Override
    public Object addItemAfter(Object previousItemId) throws UnsupportedOperationException {
        return delegate.addItemAfter(previousItemId);
    }

    @Override
    public Item addItemAfter(Object previousItemId, Object newItemId) throws UnsupportedOperationException {
        return delegate.addItemAfter(previousItemId, newItemId);
    }

    @Override
    public Item getItem(Object itemId) {
        return delegate.getItem(itemId);
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        return delegate.getContainerPropertyIds();
    }

    @Override
    public Collection<?> getItemIds() {
        return delegate.getItemIds();
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return delegate.getContainerProperty(itemId, propertyId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return delegate.getType(propertyId);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean containsId(Object itemId) {
        return delegate.containsId(itemId);
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        return delegate.addItem(itemId);
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        return delegate.addItem();
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return delegate.removeItem(itemId);
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        return delegate.addContainerProperty(propertyId, type, defaultValue);
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        return delegate.removeContainerProperty(propertyId);
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        return delegate.removeAllItems();
    }
}
