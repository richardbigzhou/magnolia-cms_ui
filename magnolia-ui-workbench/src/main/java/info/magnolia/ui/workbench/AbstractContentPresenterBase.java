/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemRightClickedEvent;
import info.magnolia.ui.workbench.event.ItemShortcutKeyEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Item;

/**
 * Abstract JCR-agnostic {@link ContentPresenter} implementation.
 */
public abstract class AbstractContentPresenterBase implements ContentPresenter, ContentView.Listener {

    private static final Logger log = LoggerFactory.getLogger(AbstractContentPresenter.class);

    protected static final String ICON_PROPERTY = "icon-node-data";

    protected static final String ICON_TRASH = "icon-trash";

    protected EventBus eventBus;

    protected WorkbenchDefinition workbenchDefinition;

    private List<Object> selectedItemIds = new ArrayList<Object>();

    protected String viewTypeName;

    protected ContentConnector contentConnector;

    private final ComponentProvider componentProvider;

    protected Container container;

    public AbstractContentPresenterBase(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    protected ComponentProvider getComponentProvider() {
        return componentProvider;
    }

    @Override
    public ContentView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName, ContentConnector contentConnector) {
        this.workbenchDefinition = workbenchDefinition;
        this.eventBus = eventBus;
        this.viewTypeName = viewTypeName;
        this.contentConnector = contentConnector;
        return null;
    }

    @Override
    public List<Object> getSelectedItemIds() {
        return this.selectedItemIds;
    }

    public Object getSelectedItemId() {
        return selectedItemIds.isEmpty() ? null : selectedItemIds.get(0);
    }

    @Override
    public void setSelectedItemIds(List<Object> selectedItemIds) {
        this.selectedItemIds = selectedItemIds;
    }

    // CONTENT VIEW LISTENER

    @Override
    public void onItemSelection(Set<Object> itemIds) {
        Object rootItemId = contentConnector.getDefaultItemId();
        if (itemIds == null || itemIds.isEmpty()) {
            log.debug("Got null com.vaadin.data.Item. ItemSelectedEvent will be fired with null path.");
            setSelectedItemIds(Lists.newArrayList(rootItemId));
        } else {
            List<Object> selectedIds = new ArrayList<>(itemIds.size());
            boolean isMultipleSelection = itemIds.size() > 1;

            for (Object id : itemIds) {
                if (isMultipleSelection && rootItemId.equals(id)) {
                    // in a multiple selection done via checkbox the root path is always added to the selection, just skip it
                    continue;
                }
                selectedIds.add(id);
            }

            setSelectedItemIds(selectedIds);
            log.debug("com.vaadin.data.Item at {} was selected. Firing ItemSelectedEvent...", selectedIds);
        }
        eventBus.fireEvent(new SelectionChangedEvent(Collections.unmodifiableSet(new HashSet<>(selectedItemIds))));

    }

    @Override
    public void onDoubleClick(Object itemId) {
        if (itemId != null) {
            try {
                List<Object> ids = new ArrayList<Object>(1);
                ids.add(itemId);
                setSelectedItemIds(ids);
                log.debug("com.vaadin.data.Item at {} was double clicked. Firing ItemDoubleClickedEvent...", getSelectedItemId());
                eventBus.fireEvent(new ItemDoubleClickedEvent(getSelectedItemId()));
            } catch (Exception e) {
                log.error("An error occurred while double clicking on a row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }

    @Override
    public void onRightClick(Object itemId, int clickX, int clickY) {
        if (itemId != null) {
            try {
                // if the right-clicket item is not yet selected
                if (!selectedItemIds.contains(itemId)) {
                    List<Object> ids = new ArrayList<Object>(1);
                    ids.add((Object) itemId);
                    setSelectedItemIds(ids);
                    select(ids);
                }
                log.debug("com.vaadin.data.Item at {} was right clicked. Firing ItemRightClickedEvent...", itemId);
                eventBus.fireEvent(new ItemRightClickedEvent(itemId, clickX, clickY));
            } catch (Exception e) {
                log.error("An error occurred while right clicking on a row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }

    @Override
    public void onShortcutKey(int keyCode, int[] modifierKeys) {
        Item item;

        if (selectedItemIds.size() == 1) {
            try {
                // item = getSelectedItemId();
                log.debug("com.vaadin.data.Item at {} was keyboard clicked. Firing ItemShortcutKeyEvent...", getSelectedItemId());
                eventBus.fireEvent(new ItemShortcutKeyEvent(getSelectedItemId(), keyCode, modifierKeys));
            } catch (Exception e) {
                log.error("An error occurred while a key was pressed with a selected row in the data grid", e);
            }
        } else {
            log.warn("Got null com.vaadin.data.Item. No event will be fired.");
        }
    }

    protected Iterator<ColumnDefinition> getColumnsIterator() {
        Iterator<ContentPresenterDefinition> viewsIterator = workbenchDefinition.getContentViews().iterator();
        while (viewsIterator.hasNext()) {
            ContentPresenterDefinition contentView = viewsIterator.next();
            String viewType = contentView.getViewType();
            if (viewType != null && viewType.equals(viewTypeName)) {
                return getAvailableColumns(contentView.getColumns()).iterator();
            }
        }
        return null;
    }

    @Override
    public abstract String getIcon(Item item);

    protected List<ColumnDefinition> getAvailableColumns(final List<ColumnDefinition> allColumns) {
        final List<ColumnDefinition> availableColumns = new ArrayList<ColumnDefinition>();
        Iterator<ColumnDefinition> it = allColumns.iterator();
        while (it.hasNext()) {
            ColumnDefinition column = it.next();
            if (column.isEnabled() && (column.getRuleClass() == null || componentProvider.newInstance(column.getRuleClass(), column).isAvailable())) {
                availableColumns.add(column);
            }
        }
        return availableColumns;
    }

    @Override
    public void select(List<Object> itemIds) {
    }

    @Override
    public void expand(Object itemId) {
    }

    protected abstract Container initializeContainer();

    protected ContentPresenterDefinition getPresenterDefinition() {
        Iterator<ContentPresenterDefinition> viewsIterator = workbenchDefinition.getContentViews().iterator();
        while (viewsIterator.hasNext()) {
            ContentPresenterDefinition contentPresenterDefinition = viewsIterator.next();
            String viewType = contentPresenterDefinition.getViewType();
            if (viewType != null && viewType.equals(viewTypeName)) {
                return contentPresenterDefinition;
            }
        }
        return null;
    }
}