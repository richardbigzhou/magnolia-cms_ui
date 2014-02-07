/**
 * This file Copyright (c) 2013 Magnolia International
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
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.ItemDoubleClickedEvent;
import info.magnolia.ui.workbench.event.ItemRightClickedEvent;
import info.magnolia.ui.workbench.event.ItemShortcutKeyEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 04/02/14
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractContentPresenterBase<IDTYPE> implements ContentPresenter<IDTYPE>, ContentView.Listener {

    private static final Logger log = LoggerFactory.getLogger(AbstractContentPresenter.class);

    protected static final String ICON_PROPERTY = "icon-node-data";

    protected static final String ICON_TRASH = "icon-trash";

    protected EventBus eventBus;

    protected WorkbenchDefinition workbenchDefinition;

    private List<IDTYPE> selectedItemIds = new ArrayList<IDTYPE>();

    protected String viewTypeName;

    private final ComponentProvider componentProvider;

    public AbstractContentPresenterBase(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    protected ComponentProvider getComponentProvider() {
        return componentProvider;
    }

    @Override
    public ContentView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName) {
        this.workbenchDefinition = workbenchDefinition;
        this.eventBus = eventBus;
        this.viewTypeName = viewTypeName;
        return null;
    }

    @Override
    public List<IDTYPE> getSelectedItemIds() {
        return this.selectedItemIds;
    }

    public IDTYPE getSelectedItemId() {
        return selectedItemIds.isEmpty() ? null : selectedItemIds.get(0);
    }

    @Override
    public void setSelectedItemIds(List<IDTYPE> selectedItemIds) {
        this.selectedItemIds = selectedItemIds;
    }

    // CONTENT VIEW LISTENER

    @Override
    public void onItemSelection(Set<Object> itemIds) {
        IDTYPE rootItemId = resolveWorkbenchRootId();
        if (itemIds == null || itemIds.isEmpty()) {
            log.debug("Got null com.vaadin.data.Item. ItemSelectedEvent will be fired with null path.");
            List<IDTYPE> ids = new ArrayList<IDTYPE>(1);

            ids.add(rootItemId);
            setSelectedItemIds(ids);
        } else {
            Iterator<Object> itemIdIt = itemIds.iterator();
            while (itemIdIt.hasNext()) {
                IDTYPE item = (IDTYPE) itemIdIt.next();
                // if the selection is done by clicking the checkbox, the root item is added to the set - so it has to be ignored
                // but only if there is any other item in the set
                // TODO MGNLUI-1521
                try {
                    if (rootItemId.equals(item) && itemIds.size() > 1) {
                        itemIdIt.remove();
                    }
                } catch (Exception e) {
                    log.error("An error occurred while selecting a row in the data grid", e);
                }
            }

            List<IDTYPE> selectedIds = new ArrayList<IDTYPE>(itemIds.size());
            for (Object id : itemIds) {
                selectedIds.add((IDTYPE) id);
            }

            setSelectedItemIds(new ArrayList<IDTYPE>(selectedIds));
            log.debug("com.vaadin.data.Item at {} was selected. Firing ItemSelectedEvent...", itemIds.toArray());
        }
        eventBus.fireEvent(new SelectionChangedEvent(itemIds));

    }

    protected abstract IDTYPE resolveWorkbenchRootId();

    @Override
    public void onDoubleClick(Object itemId) {
        if (itemId != null) {
            try {
                List<IDTYPE> ids = new ArrayList<IDTYPE>(1);
                ids.add((IDTYPE) itemId);
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

    protected abstract String getItemId(Item item);

    @Override
    public void onRightClick(Object itemId, int clickX, int clickY) {
        if (itemId != null) {
            try {
                // if the right-clicket item is not yet selected
                if (!selectedItemIds.contains(itemId)) {
                    List<IDTYPE> ids = new ArrayList<IDTYPE>(1);
                    ids.add((IDTYPE) itemId);
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
                item = getContainer().getItem(getSelectedItemId());
                // item = getSelectedItemId();
                log.debug("com.vaadin.data.Item at {} was keyboard clicked. Firing ItemShortcutKeyEvent...", getSelectedItemId());
                eventBus.fireEvent(new ItemShortcutKeyEvent(item, keyCode, modifierKeys));
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
    public void select(List<IDTYPE> itemIds) {}

    @Override
    public void expand(String itemId) {}

    protected abstract Container getContainer();
}