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
import info.magnolia.ui.imageprovider.ImageProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import info.magnolia.ui.workbench.event.ViewTypeChangedEvent;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.search.SearchPresenter;
import info.magnolia.ui.workbench.search.SearchPresenterDefinition;
import info.magnolia.ui.workbench.tree.TreePresenter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The WorkbenchPresenter is responsible for creating, configuring and updating the workbench view, as well as handling its interaction.
 */
public class WorkbenchPresenter implements WorkbenchView.Listener {

    private static final Logger log = LoggerFactory.getLogger(WorkbenchPresenter.class);

    private final WorkbenchView view;

    private final ComponentProvider componentProvider;

    private final Map<String, ContentPresenter> contentPresenters = new LinkedHashMap<String, ContentPresenter>();

    private ContentPresenter activePresenter;

    private final WorkbenchStatusBarPresenter statusBarPresenter;

    private WorkbenchDefinition workbenchDefinition;

    private EventBus eventBus;

    @Inject
    public WorkbenchPresenter(WorkbenchView view, ComponentProvider componentProvider, WorkbenchStatusBarPresenter statusBarPresenter) {
        this.view = view;
        this.componentProvider = componentProvider;
        this.statusBarPresenter = statusBarPresenter;
    }

    public WorkbenchView start(WorkbenchDefinition workbenchDefinition, ImageProviderDefinition imageProviderDefinition, EventBus eventBus) {
        this.workbenchDefinition = workbenchDefinition;
        this.eventBus = eventBus;

        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName() + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        // add content views
        for (final ContentPresenterDefinition presenterDefinition : workbenchDefinition.getContentViews()) {

            Class<? extends ContentPresenter> presenterClass = presenterDefinition.getImplementationClass();
            ContentPresenter presenter = null;
            if (presenterClass != null) {
                if (imageProviderDefinition != null) {
                    ImageProvider imageProvider = componentProvider.newInstance(imageProviderDefinition.getImageProviderClass(), imageProviderDefinition);
                    presenter = componentProvider.newInstance(presenterClass, imageProvider);
                } else {
                    presenter = componentProvider.newInstance(presenterClass);
                }
                contentPresenters.put(presenterDefinition.getViewType(), presenter);
                ContentView contentView = presenter.start(workbenchDefinition, eventBus, presenterDefinition.getViewType(), view.getshortcutActionManager());
                if (presenterDefinition.isActive()) {
                    activePresenter = presenter;
                    try {
                        String workbenchRootItemId = JcrItemUtil.getItemId(workbenchDefinition.getWorkspace(), workbenchDefinition.getPath());
                        List<String> ids = new ArrayList<String>(1);
                        ids.add(workbenchRootItemId);
                        activePresenter.setSelectedItemIds(ids);
                    } catch (RepositoryException e) {
                        log.error("Could not find workbench root node", e);
                    }
                }
                view.addContentView(presenterDefinition.getViewType(), contentView, presenterDefinition);

                if (presenter instanceof TreePresenter && workbenchDefinition.isDialogWorkbench()) {
                    ((TreePresenter) presenter).disableDragAndDrop();
                }
            } else {
                throw new RuntimeException("The provided view type [" + presenterDefinition.getViewType() + "] is not valid.");
            }

        }

        // add status bar
        view.setStatusBarView(statusBarPresenter.start(eventBus, workbenchDefinition));

        view.setListener(this);
        return view;
    }

    @Override
    public void onSearch(final String searchExpression) {
        if (StringUtils.isNotBlank(searchExpression)) {
            eventBus.fireEvent(new SearchEvent(searchExpression));
        } else {
            // if search expression is empty switch to list view
            onViewTypeChanged(ListPresenterDefinition.VIEW_TYPE);
        }
    }

    @Override
    public void onViewTypeChanged(final String viewType) {
        setViewType(viewType);
        eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    private void setViewType(String viewType) {
        ContentPresenter oldPresenter = activePresenter;
        List<String> itemIds = oldPresenter.getSelectedItemIds();

        activePresenter = contentPresenters.get(viewType);
        activePresenter.refresh();
        view.setViewType(viewType);

        // make sure selection is kept when switching views
        if (itemIds != null) {
            select(itemIds);
        }
    }

    public String getWorkspace() {
        return workbenchDefinition.getWorkspace();
    }

    public List<String> getSelectedIds() {
        return activePresenter.getSelectedItemIds();
    }

    public void expand(String itemId) {
        activePresenter.expand(itemId);
    }

    public void select(String itemId) {
        List<String> ids = new ArrayList<String>(1);
        ids.add(itemId);
        select(ids);
    }

    public void select(List<String> itemIds) {
        try {
            // restore selection
            Set<JcrItemAdapter> items = new LinkedHashSet<JcrItemAdapter>();
            List<String> selectedIds = new ArrayList<String>();
            boolean rootHasBeenSelected = false;
            for (String itemId : itemIds) {
                if (JcrItemUtil.itemExists(getWorkspace(), itemId)) {
                    selectedIds.add(itemId);

                    Item jcrItem = JcrItemUtil.getJcrItem(getWorkspace(), itemId);
                    JcrItemAdapter itemAdapter;
                    if (jcrItem.isNode()) {
                        itemAdapter = new JcrNodeAdapter((Node) jcrItem);
                    } else {
                        itemAdapter = new JcrPropertyAdapter((Property) jcrItem);
                    }
                    items.add(itemAdapter);

                } else {
                    log.info("Trying to re-sync workbench with no longer existing path {} at workspace {}. Will reset path to its configured root {}.",
                            new Object[] { itemId, workbenchDefinition.getWorkspace(), workbenchDefinition.getPath() });
                    String workbenchRootItemId = JcrItemUtil.getItemId(workbenchDefinition.getWorkspace(), workbenchDefinition.getPath());
                    if (!rootHasBeenSelected && !selectedIds.contains(workbenchRootItemId)) {
                        // adding workbenchRootItemID for non-existent items, but just once
                        selectedIds.add(workbenchRootItemId);
                        rootHasBeenSelected = true;
                    }
                }
            }
            activePresenter.setSelectedItemIds(selectedIds);
            activePresenter.select(selectedIds);
            // Only send event if items are not empty (do exist)
            if (!items.isEmpty()) {
                eventBus.fireEvent(new SelectionChangedEvent(workbenchDefinition.getWorkspace(), items));
            }

        } catch (RepositoryException e) {
            log.warn("Unable to get node or property [{}] for selection", itemIds, e);
        }
    }

    public void refresh() {
        activePresenter.refresh();
    }

    public String getDefaultViewType() {
        for (ContentPresenterDefinition definition : this.workbenchDefinition.getContentViews()) {
            if (definition.isActive()) {
                return definition.getViewType();
            }
        }
        return this.workbenchDefinition.getContentViews().get(0).getViewType();
    }

    public boolean hasViewType(String viewType) {
        return contentPresenters.containsKey(viewType);
    }

    public void resynch(final List<String> itemIds, final String viewType, final String query) {
        setViewType(viewType);
        select(itemIds);

        if (SearchPresenterDefinition.VIEW_TYPE.equals(viewType)) {
            doSearch(query);
            // update search field and focus it
            view.setSearchQuery(query);
        }
    }

    public void doSearch(String searchExpression) {
        // firing new search forces search view as new view type
        if (activePresenter != contentPresenters.get(SearchPresenterDefinition.VIEW_TYPE)) {
            setViewType(SearchPresenterDefinition.VIEW_TYPE);
        }
        final SearchPresenter searchPresenter = (SearchPresenter) activePresenter;
        if (StringUtils.isBlank(searchExpression)) {
            searchPresenter.clear();
        } else {
            searchPresenter.search(searchExpression);
        }
    }
}
