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
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
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

    protected ContentConnector contentConnector;

    @Inject
    public WorkbenchPresenter(WorkbenchView view, ComponentProvider componentProvider, WorkbenchStatusBarPresenter statusBarPresenter, ContentConnector contentConnector) {
        this.view = view;
        this.componentProvider = componentProvider;
        this.statusBarPresenter = statusBarPresenter;
        this.contentConnector = contentConnector;
    }

    public WorkbenchView start(WorkbenchDefinition workbenchDefinition, ImageProviderDefinition imageProviderDefinition, EventBus eventBus) {
        this.workbenchDefinition = workbenchDefinition;
        this.eventBus = eventBus;

        sanityCheck(workbenchDefinition);

        // find default viewType
        String defaultViewType = getDefaultViewType();

        // add content views
        for (final ContentPresenterDefinition presenterDefinition : workbenchDefinition.getContentViews()) {
            ContentPresenter presenter;
            Class<? extends ContentPresenter> presenterClass = presenterDefinition.getImplementationClass();
            if (presenterClass != null) {
                presenter = componentProvider.newInstance(presenterClass);
                contentPresenters.put(presenterDefinition.getViewType(), presenter);
                ContentView contentView = presenter.start(workbenchDefinition, eventBus, presenterDefinition.getViewType(), contentConnector);

                // use new #addContentView (doesn't require the content-view definition anymore)
                if (view instanceof WorkbenchViewImpl) {
                    ((WorkbenchViewImpl) view).addContentView(presenterDefinition.getViewType(), contentView, presenterDefinition.getIcon());
                } else {
                    // will be deprecated
                    view.addContentView(presenterDefinition.getViewType(), contentView, presenterDefinition);
                }

                if (presenterDefinition.getViewType().equals(defaultViewType)) {
                    activePresenter = presenter;
                    setViewType(presenterDefinition.getViewType());
                }

                if (presenter instanceof TreePresenter && workbenchDefinition.isDialogWorkbench()) {
                    ((TreePresenter) presenter).disableDragAndDrop();
                }
            } else {
                throw new RuntimeException("The provided view type [" + presenterDefinition.getViewType() + "] is not valid.");
            }

        }

        // add status bar
        view.setStatusBarView(statusBarPresenter.start(eventBus, activePresenter));

        view.setMultiselect(!workbenchDefinition.isDialogWorkbench());

        view.setListener(this);
        return view;
    }

    public Object resolveWorkbenchRoot() {
        return contentConnector.getDefaultItemId();
    }

    protected void sanityCheck(WorkbenchDefinition workbenchDefinition) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Failed to init a workbench: WorkbenchDefinition is null.");
        } else if (workbenchDefinition.getContentViews() == null || workbenchDefinition.getContentViews().size() == 0) {
            throw new IllegalArgumentException("Failed to init a workbench: No content-view is configured.");
        }
    }




    @Override
    public void onSearch(final String searchExpression) {
        if (hasViewType(SearchPresenterDefinition.VIEW_TYPE)) {
            if (StringUtils.isNotBlank(searchExpression)) {
                eventBus.fireEvent(new SearchEvent(searchExpression));
            } else {
                // if search expression is empty switch to list view
                onViewTypeChanged(ListPresenterDefinition.VIEW_TYPE);
            }
        } else {
            log.warn("Workbench view triggered search although the search view type is not configured in this workbench {}", this);
        }
    }

    @Override
    public void onViewTypeChanged(final String viewType) {
        setViewType(viewType);
        eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    private void setViewType(String viewType) {
        ContentPresenter oldPresenter = activePresenter;
        List<Object> itemIds = oldPresenter == null ? null : oldPresenter.getSelectedItemIds();

        activePresenter = contentPresenters.get(viewType);
        activePresenter.refresh();
        view.setViewType(viewType);

        statusBarPresenter.setActivePresenter(activePresenter);

        // make sure selection is kept when switching views
        if (itemIds != null) {
            select(itemIds);
        }
    }

    public List<Object> getSelectedIds() {
        return activePresenter.getSelectedItemIds();
    }

    public void expand(Object itemId) {
        activePresenter.expand(itemId);
    }

    public void select(Object itemId) {
        List<Object> ids = new ArrayList<Object>(1);
        ids.add(itemId);
        select(ids);
    }

    public void select(List<Object> itemIds) {
        final List<Object> selectedIds = filterExistingItems(itemIds);
        // restore selection
        if (selectedIds.isEmpty()) {
            Object workbenchRootItemId = contentConnector.getDefaultItemId();
            if (workbenchRootItemId != null) {
                selectedIds.add(workbenchRootItemId);
            }
        }

        activePresenter.setSelectedItemIds(selectedIds);
        activePresenter.select(selectedIds);
        // Only send event if items are not empty (do exist)
        eventBus.fireEvent(new SelectionChangedEvent(new HashSet<Object>(selectedIds)));
    }

    protected List<Object> filterExistingItems(List<Object> itemIds) {
        List<Object> filteredIds = new ArrayList<Object>();
        Iterator<Object> it = itemIds.iterator();
        while (it.hasNext()) {
            Object itemId = it.next();
            if (contentConnector.canHandleItem(itemId) && contentConnector.getItem(itemId) != null) {
                filteredIds.add(itemId);
            }
        }
        return filteredIds;
    }

    public void refresh() {
        activePresenter.refresh();
        statusBarPresenter.refresh();
    }

    public String getDefaultViewType() {
        for (ContentPresenterDefinition definition : workbenchDefinition.getContentViews()) {
            if (definition.isActive()) {
                return definition.getViewType();
            }
        }
        return workbenchDefinition.getContentViews().get(0).getViewType();
    }

    public boolean hasViewType(String viewType) {
        return contentPresenters.containsKey(viewType);
    }

    public void resynch(final List<Object> itemIds, final String viewType, final String query) {
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

    // exposed only for extending classes. these methods should not be public!
    protected final ContentPresenter getActivePresenter() {
        return activePresenter;
    }

    protected final EventBus getEventBus() {
        return eventBus;
    }

    protected final WorkbenchDefinition getWorkbenchDefinition() {
        return workbenchDefinition;
    }
}
