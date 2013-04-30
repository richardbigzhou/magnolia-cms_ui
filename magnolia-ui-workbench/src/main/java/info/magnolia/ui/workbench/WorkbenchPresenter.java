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

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.workbench.event.SearchEvent;
import info.magnolia.ui.workbench.event.ViewTypeChangedEvent;
import info.magnolia.ui.workbench.search.SearchView;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;

/**
 * TODO: Add JavaDoc for WorkbenchPresenter.
 */
public class WorkbenchPresenter implements WorkbenchView.Listener {

    private static final Logger log = LoggerFactory.getLogger(WorkbenchPresenter.class);

    private final WorkbenchView view;

    private final ContentPresenter contentPresenter;

    private final WorkbenchStatusBarPresenter statusBarPresenter;

    private WorkbenchDefinition workbenchDefinition;

    private EventBus eventBus;

    @Inject
    public WorkbenchPresenter(WorkbenchView view, ContentPresenter contentPresenter, WorkbenchStatusBarPresenter statusBarPresenter) {
        this.view = view;
        this.contentPresenter = contentPresenter;
        this.statusBarPresenter = statusBarPresenter;
    }

    public WorkbenchView start(WorkbenchDefinition workbenchDefinition, ImageProviderDefinition imageProviderDefinition, EventBus eventBus) {
        this.workbenchDefinition = workbenchDefinition;
        this.eventBus = eventBus;
        contentPresenter.start(view, workbenchDefinition, imageProviderDefinition, eventBus);

        if (view.getSelectedView() != null && view.getSelectedView().getContainer() != null) {
            view.getSelectedView().getContainer().addItemSetChangeListener(new ItemSetChangeListener() {

                @Override
                public void containerItemSetChange(ItemSetChangeEvent event) {
                    statusBarPresenter.setItemCount(event.getContainer().size());
                }
            });
        }

        view.setStatusBarView(statusBarPresenter.start(eventBus));
        view.setListener(this);
        return view;
    }

    @Override
    public void onSearch(final String searchExpression) {
        eventBus.fireEvent(new SearchEvent(searchExpression));
    }

    @Override
    public void onViewTypeChanged(final ContentView.ViewType viewType) {
        eventBus.fireEvent(new ViewTypeChangedEvent(viewType));
    }

    public String getSelectedId() {
        return contentPresenter.getSelectedItemPath();
    }

    public String getWorkspace() {
        return workbenchDefinition.getWorkspace();
    }

    public void selectPath(String path) {
        view.selectPath(path);
        contentPresenter.setSelectedItemPath(path);
    }

    public void refresh() {
        view.refresh();
    }

    public ContentView.ViewType getDefaultViewType() {
        for (ContentViewDefinition definition : this.workbenchDefinition.getContentViews()) {
            if (definition.isActive()) {
                return definition.getViewType();
            }
        }
        return this.workbenchDefinition.getContentViews().get(0).getViewType();
    }

    public void resynch(final String path, final ContentView.ViewType viewType, final String query) {
        view.setViewType(viewType);

        if (viewType == ContentView.ViewType.SEARCH) {
            doSearch(query);
            // update search field and focus it
            view.setSearchQuery(query);
        }

        // restore selection
        boolean itemExists = itemExists(path);
        if (!itemExists) {
            log.info("Trying to re-sync workbench with no longer existing path {} at workspace {}. Will reset path to its configured root {}.",
                    new Object[] { path, workbenchDefinition.getWorkspace(), workbenchDefinition.getPath() });
        }
        view.selectPath(itemExists ? path : workbenchDefinition.getPath());
    }

    public void doSearch(String searchExpression) {
        // firing new search forces search view as new view type
        if (view.getSelectedView().getViewType() != ContentView.ViewType.SEARCH) {
            view.setViewType(ContentView.ViewType.SEARCH);
        }
        final SearchView searchView = (SearchView) view.getSelectedView();
        if (StringUtils.isBlank(searchExpression)) {
            searchView.clear();
        } else {
            searchView.search(searchExpression);
        }
    }

    private boolean itemExists(String path) {
        try {
            return StringUtils.isNotBlank(path) && MgnlContext.getJCRSession(workbenchDefinition.getWorkspace()).itemExists(path);
        } catch (RepositoryException e) {
            log.warn("", e);
        }
        return false;
    }
}
