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
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.contenttool.ConfiguredContentToolDefinition;
import info.magnolia.ui.workbench.contenttool.ContentToolDefinition;
import info.magnolia.ui.workbench.contenttool.ContentToolPresenter;
import info.magnolia.ui.workbench.contenttool.search.SearchContentToolPresenter;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.QueryStatementChangedEvent;
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

/**
 * The WorkbenchPresenter is responsible for creating, configuring and updating the workbench view, as well as handling its interaction.
 */
public class WorkbenchPresenter implements WorkbenchView.Listener {

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


        // add content tools
        final List<ContentToolDefinition> contentTools = this.workbenchDefinition.getContentTools();
        for (ContentToolDefinition toolDefinition : contentTools) {
            Class<? extends ContentToolPresenter> presenterClass = toolDefinition.getPresenterClass();
            if (presenterClass != null) {
                ContentToolPresenter contentToolPresenter = componentProvider.newInstance(presenterClass, toolDefinition, eventBus, this);
                View contentToolView = contentToolPresenter.start();

                if ((toolDefinition instanceof ConfiguredContentToolDefinition) && (view instanceof WorkbenchViewImpl)) {
                    final ConfiguredContentToolDefinition configuredContentToolDefinition = (ConfiguredContentToolDefinition) toolDefinition;
                    // the following methods should become the parts of WorkbenchView/ContentTool inetrfaces as a part of MGNLUI-3709
                    ((WorkbenchViewImpl)view).addContentTool(contentToolView, configuredContentToolDefinition.getAlignment(), configuredContentToolDefinition.getExpandRatio());
                }
            }
        }

        if (hasViewType(ListPresenterDefinition.VIEW_TYPE) && hasViewType(SearchPresenterDefinition.VIEW_TYPE)) {
            // always include search component in the toolbar search component
            addSearchContentTool();
        }

        // add status bar
        view.setStatusBarView(statusBarPresenter.start(eventBus, activePresenter));

        view.setMultiselect(!workbenchDefinition.isDialogWorkbench());

        view.setListener(this);
        return view;
    }

    /**
     * Returns the default item id of this workbench.
     * @deprecated since 5.4.3. Method isn't used anymore.
     */
    @Deprecated
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

    protected void addSearchContentTool() {
        SearchContentToolPresenter searchPresenter = componentProvider.newInstance(SearchContentToolPresenter.class, this, eventBus);
        View searchView = searchPresenter.start();
        ((WorkbenchViewImpl) this.view).addContentTool(searchView, ContentToolDefinition.Alignment.RIGHT, 0);
    }


    /**
     * Search logic is now implemented in a corresponding tool presenter.
     *
     * @see SearchContentToolPresenter
     */
    @Override
    public void onSearch(final String searchExpression) {
        // deprecated, does nothing
    }

    @Override
    public void onViewTypeChanged(final String viewType) {
        setViewType(viewType);
        eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    @Override
    public void onSearchQueryChange(String searchQuery) {
        eventBus.fireEvent(new QueryStatementChangedEvent(searchQuery));
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
        return this.eventBus;
    }

    protected final WorkbenchDefinition getWorkbenchDefinition() {
        return workbenchDefinition;
    }
}
