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

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.CategoryDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.PulseListDefinition;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.CategoryItem;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseDetailPresenter;
import info.magnolia.ui.api.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract presenter for items displayed in pulse.
 *
 * @param <T> typed parameter for the items.
 */
public abstract class AbstractPulseListPresenter<T> implements PulseListPresenter, PulseDetailPresenter.Listener, PulseListView.Listener {

    protected AbstractPulseListContainer<T> container;
    protected Listener listener;

    protected AbstractPulseListPresenter(AbstractPulseListContainer<T> container) {
        this.container = container;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void showList() {
        listener.showList();
    }

    /**
     * Return parent itemId for an item.
     */
    @Override
    public Object getParent(Object itemId) {
        return container.getParent(itemId);
    }

    /**
     * Return list of child items.
     */
    @Override
    public Collection<?> getGroup(Object itemId) {
        return container.getGroup(itemId);
    }

    @Override
    public void setGrouping(boolean checked) {
        container.setGrouping(checked);
    }

    @Override
    public void filterByItemCategory(CategoryItem category) {
        container.filterByItemCategory(category);
    }

    @Override
    public abstract CategoryItem getCategory();

    @Override
    public abstract View openItem(String itemId) throws RegistrationException;

    @Override
    public abstract int getPendingItemCount();

    protected CategoryItem findCategoryByMappedStatus(String mappedStatus) {
        for (CategoryDefinition definition : getDefinition().getTabs()) {
            if (definition.getMappedStatus() != null && definition.getMappedStatus().contains(mappedStatus)) {
                return new CategoryItem(definition.getName(), definition.getLabel());
            }
        }
        return null;
    }

    protected abstract PulseListDefinition getDefinition();

    protected CategoryItem[] getCategories() {
        List<CategoryItem> categoryItems = new ArrayList<CategoryItem>();
        for (CategoryDefinition cd : getDefinition().getTabs()) {
            CategoryItem categoryItem = new CategoryItem(cd.getName(), cd.getLabel());
            categoryItem.setStatuses(cd.getMappedStatus());
            categoryItem.setActivate(getDefinition().getDefaultTab() != null && getDefinition().getDefaultTab().equals(cd.getName()));
            categoryItems.add(categoryItem);
        }
        return categoryItems.toArray(new CategoryItem[0]);
    }
}
