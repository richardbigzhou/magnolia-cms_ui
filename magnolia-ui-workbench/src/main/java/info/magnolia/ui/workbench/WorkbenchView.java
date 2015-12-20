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

import info.magnolia.ui.api.view.View;
import info.magnolia.ui.workbench.definition.ContentPresenterDefinition;

/**
 * WorkbenchView.
 */
public interface WorkbenchView extends View {
    /**
     * Listener interface for events concerning the workbench.
     */
    interface Listener {

        void onSearchQueryChange(String searchExpression);

        /**
         * @deprecated since 5.4 - the functionality provided by this method is now a responsibility of {@link info.magnolia.ui.workbench.contenttool.search.SearchContentToolPresenter}.
         */
        @Deprecated
        void onSearch(String searchExpression);

        void onViewTypeChanged(String viewType);
    }

    void setListener(Listener listener);

    /**
     * Updates the search box with given search query.
     */
    void setSearchQuery(String query);

    /**
     * Use this method to add sub views hosted by this view.
     */
    void addContentView(String type, ContentView view, ContentPresenterDefinition contentViewDefintion);

    /**
     * Use this method to add content tools.
     */
    void addContentTool(View view);

    void setViewType(String type);

    /**
     * Use this method to add a status bar to this sub app view.
     */
    void setStatusBarView(StatusBarView statusBar);

    ContentView getSelectedView();

    /**
     * Whether the user can select more items.
     */
    void setMultiselect(boolean multiselect);

}
