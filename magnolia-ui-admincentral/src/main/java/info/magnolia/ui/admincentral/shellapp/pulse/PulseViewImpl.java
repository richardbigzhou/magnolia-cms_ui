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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategory;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategoryNavigator;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategoryNavigator.CategoryChangedEvent;
import info.magnolia.ui.admincentral.shellapp.pulse.item.detail.PulseItemCategoryNavigator.ItemCategoryChangedListener;
import info.magnolia.ui.api.view.View;

import javax.inject.Inject;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Default view implementation for Pulse.
 */
public final class PulseViewImpl implements PulseView {

    private final VerticalLayout layout = new VerticalLayout();

    private final PulseItemCategoryNavigator navigator;

    private Listener listener;

    @Inject
    public PulseViewImpl(final SimpleTranslator i18n) {
        layout.addStyleName("v-pulse");
        layout.setHeight(100, Unit.PERCENTAGE);
        layout.setWidth("900px");
        navigator = new PulseItemCategoryNavigator(i18n, false, true, PulseItemCategory.TASKS, PulseItemCategory.MESSAGES);
        navigator.addCategoryChangeListener(new ItemCategoryChangedListener() {

            @Override
            public void itemCategoryChanged(CategoryChangedEvent event) {
                final PulseItemCategory category = event.getCategory();
                listener.onCategoryChange(category);
            }
        });
        navigator.setHeight("25px");
        layout.addComponentAsFirst(navigator);
    }

    @Override
    public void setPulseSubView(View view) {
        if (layout.getComponentCount() == 2) {
            Component oldView = layout.getComponent(1);
            layout.removeComponent(oldView);
        }
        layout.addComponent(view.asVaadinComponent(), 1);
        layout.setExpandRatio(view.asVaadinComponent(), 1);
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void updateCategoryBadgeCount(PulseItemCategory category, int count) {
        navigator.updateCategoryBadgeCount(category, count);
    }

    @Override
    public void setTabActive(PulseItemCategory category) {
        navigator.setActive(category);
    }

}
