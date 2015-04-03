/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.workbench;

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.Set;

import javax.inject.Inject;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * The browser features a status bar at the bottom with selected path and item count information.
 */
public class WorkbenchStatusBarPresenter {

    private final StatusBarView view;

    private ContentConnector contentConnector;

    private EventBus eventBus;

    private final Label selectionLabel = new Label();

    private ContentPresenter activeContentPresenter;

    private boolean rootIsSelected;

    private SimpleTranslator i18n;

    @Inject
    public WorkbenchStatusBarPresenter(StatusBarView view, ContentConnector contentConnector, SimpleTranslator i18n) {
        this.view = view;
        this.contentConnector = contentConnector;
        this.i18n = i18n;
    }

    private void bindHandlers() {
        eventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                setSelectedItems(event.getItemIds());
            }
        });
    }

    public StatusBarView start(EventBus eventBus, ContentPresenter activeContentPresenter) {

        this.eventBus = eventBus;
        this.activeContentPresenter = activeContentPresenter;

        view.addComponent(selectionLabel, Alignment.TOP_LEFT);
        ((HorizontalLayout) view).setExpandRatio(selectionLabel, 1);

        bindHandlers();

        refresh();

        return view;
    }

    public void setSelectedItems(Set<Object> itemIds) {
        if (!itemIds.isEmpty()) {
            Object id = itemIds.iterator().next();
            rootIsSelected = id.equals(contentConnector.getDefaultItemId());
            setSelectedItem(id, itemIds.size());
        } else {
            rootIsSelected = true;
            setSelectedItem(contentConnector.getDefaultItemId(), itemIds.size());
        }
    }

    public void setSelectedItem(Object itemId, int totalSelected) {
        // selection might contain the configured root path (by default '/') but we don't want to count that
        if (rootIsSelected && totalSelected > 0) {
            totalSelected--;
        }
        if (totalSelected == 1) {
            String newValue = contentConnector.getItemUrlFragment(itemId);
            selectionLabel.setValue(newValue);
            selectionLabel.setDescription(newValue);
        } else {
            String selected = i18n.translate("ui-contentapp.statusbar.selected", totalSelected);
            selectionLabel.setValue(selected);
            selectionLabel.setDescription(selected);
        }
    }

    public void refresh() {
        // active presenter can be null initially when there are multiple browser subapps
        if (activeContentPresenter == null) {
            return;
        }

        int selected = activeContentPresenter.getSelectedItemIds().size();
        if (selected == 1) {
            setSelectedItem(activeContentPresenter.getSelectedItemIds().get(0), selected);
        } else {
            setSelectedItem(null, selected);
        }
    }

    public void setActivePresenter(ContentPresenter activePresenter) {
        this.activeContentPresenter = activePresenter;
    }

}
