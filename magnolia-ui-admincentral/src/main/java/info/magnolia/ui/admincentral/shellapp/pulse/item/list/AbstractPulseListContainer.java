/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.item.list;

import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;

/**
 * Abstract container for items displayed in pulse.
 *
 * @param <T> the bean added to the container.
 */
public abstract class AbstractPulseListContainer<T> {

    protected boolean grouping = false;
    protected HierarchicalContainer container;
    protected Listener listener;

    public abstract HierarchicalContainer createDataSource(Collection<T> items);

    public abstract void addBeanAsItem(T bean);

    public abstract void buildTree();

    public abstract void assignPropertiesFromBean(T bean, final Item item);

    protected abstract void createSuperItems();

    protected abstract void clearSuperItems();

    protected abstract Container.Filter getSectionFilter();

    protected abstract void applyCategoryFilter(final PulseItemCategory category);

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Item getItem(Object itemId) {
        return container.getItem(itemId);
    }

    /*
     * Sets the grouping of messages
     */
    public void setGrouping(boolean checked) {
        grouping = checked;

        clearSuperItems();
        container.removeContainerFilter(getSectionFilter());

        if (checked) {
            buildTree();
        }

        container.addContainerFilter(getSectionFilter());
    }

    public boolean isGrouping() {
        return grouping;
    }

    public void filterByItemCategory(PulseItemCategory category) {
        if (container != null) {
            container.removeAllContainerFilters();
            container.addContainerFilter(getSectionFilter());
            applyCategoryFilter(category);
        }
    }

    /**
     * Return list of child items.
     */
    public Collection<?> getGroup(Object itemId) {
        return container.getChildren(itemId);
    }

    /**
     * Return parent itemId for an item.
     */
    public Object getParent(Object itemId) {
        return container.getParent(itemId);
    }

    /**
     * Listener for calling back into parent presenter.
     */
    public interface Listener<T> {
        String getItemTitle(T item);
    }
}
