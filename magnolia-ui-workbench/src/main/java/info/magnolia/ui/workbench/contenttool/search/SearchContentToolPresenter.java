/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.workbench.contenttool.search;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.ContentPresenter;
import info.magnolia.ui.workbench.WorkbenchPresenter;
import info.magnolia.ui.workbench.contenttool.ContentToolPresenter;
import info.magnolia.ui.workbench.event.ViewTypeChangedEvent;
import info.magnolia.ui.workbench.list.ListPresenterDefinition;
import info.magnolia.ui.workbench.search.SearchPresenter;
import info.magnolia.ui.workbench.search.SearchPresenterDefinition;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Search content tool presenter.
 */
public class SearchContentToolPresenter implements ContentToolPresenter, SearchContentToolView.Listener {

    private static final Logger log = LoggerFactory.getLogger(SearchContentToolPresenter.class);

    private SearchContentToolView view;
    private WorkbenchPresenter workbenchPresenter;

    @Inject
    public SearchContentToolPresenter(SearchContentToolView view) {
        this.view = view;
    }

    @Override
    public View start(final WorkbenchPresenter workbenchPresenter, ContentPresenter contentPresenter, ContentConnector contentConnector, EventBus eventBus) {
        this.workbenchPresenter = workbenchPresenter;
        view.setListener(this);

        eventBus.addHandler(ViewTypeChangedEvent.class, new ViewTypeChangedEvent.Handler() {
            @Override
            public void onViewChanged(ViewTypeChangedEvent event) {
                // clear text field when switching between different view types
                // but do not clear it when user is performing search
                if (event.getViewType().equals(SearchPresenterDefinition.VIEW_TYPE)) {
                    return;
                }
                view.clearSearchQuery();
            }
        });

        return view;
    }

    @Override
    public void onSearch(String searchExpression) {
        if (workbenchPresenter.hasViewType(SearchPresenterDefinition.VIEW_TYPE)) {
                doSearch(searchExpression);
        } else {
            log.warn("Workbench view triggered search although the search view type is not configured in this workbench {}", this);
        }
    }

    public void doSearch(String searchExpression) {
        if (StringUtils.isBlank(searchExpression)) {
            // in case text field was empty
            workbenchPresenter.onViewTypeChanged(ListPresenterDefinition.VIEW_TYPE);
            return;
        }
        if (!workbenchPresenter.isActivePresenter(SearchPresenter.class)) {
            // we have to switch view type
            workbenchPresenter.onViewTypeChanged(SearchPresenterDefinition.VIEW_TYPE);
        }
        // perform the search
        final SearchPresenter searchPresenter = (SearchPresenter) workbenchPresenter.getActivePresenter();
        searchPresenter.search(searchExpression);
    }
}
