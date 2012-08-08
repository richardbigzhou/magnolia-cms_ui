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
import info.magnolia.ui.widget.actionbar.ActionbarView;

import com.vaadin.data.Item;
import com.vaadin.ui.ComponentContainer;


/**
 * Implementations of this interface are responsible for building a workbench and handling the UI
 * actions associated with it.
 */
public interface ContentWorkbenchView extends ComponentContainer, View {

    /**
     * TODO dlipp - ActionBarPresenter should be a proper type as well.
     */
    public interface Listener {
    }

    void setListener(final Listener listener);

    void setGridType(final ContentView.ViewType type);

    /**
     * Causes a view refresh only if the current node exists in the repository.
     */
    void refreshItem(final Item item);

    /**
     * TODO review the for two methods to perform the view refresh. Had to add this one to refresh
     * the view in case of item deletion. Refreshes the view.
     */
    void refresh();

    /**
     * Use this method to add sub views hosted by this view.
     */
    void addContentView(final ViewType type, final ContentView view);

    /**
     * Use this method to add an action bar to this sub app view.
     */
    void setActionbarView(final ActionbarView actionbar);
}
