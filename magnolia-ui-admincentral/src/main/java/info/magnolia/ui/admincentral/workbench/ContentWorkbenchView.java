/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.admincentral.workbench;

import info.magnolia.ui.admincentral.content.view.ContentView;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.vaadin.actionbar.ActionbarView;

import com.vaadin.ui.ComponentContainer;


/**
 * Implementations of this interface are responsible for building a workbench and handling the UI
 * actions associated with it.
 */
public interface ContentWorkbenchView extends ComponentContainer, View {
    /**
     * Listener interface for events concerning the workbench.
     */
    interface Listener {
        void onSearch(String searchExpression);

        void onViewTypeChanged(ViewType viewType);
    }

    void setListener(Listener listener);

    void setViewType(ContentView.ViewType type);


    /**
     * Refreshes the current view.
     */
    void refresh();

    /**
     * Use this method to add sub views hosted by this view.
     */
    void addContentView(ViewType type, ContentView view);

    /**
     * Use this method to add an action bar to this sub app view.
     */
    void setActionbarView(ActionbarView actionbar);

    void selectPath(String path);
    /**
     * Synchronize the view status to reflect the information extracted from the Location token,
     * i.e. selected path, view type and optional query (in case of a 'search' view).
     */
    void resynch(String path, ViewType viewType, String query);

    ContentView getSelectedView();

}
