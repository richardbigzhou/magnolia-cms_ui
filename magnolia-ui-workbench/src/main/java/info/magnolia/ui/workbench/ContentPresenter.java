/**
 * This file Copyright (c) 2011-2016 Magnolia International
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
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;

import java.util.List;


/**
 * The ContentPresenter is the presenter class for a {@link ContentView}, it represents a view type in the workbench.
 * Magnolia provides default implementations of this interface for displaying data as a tree, list or thumbnails.<br>
 * <br>
 * Implementations of this interface are responsible for configuring and populating content views according to a workbench definition (defines e.g. workspace, node-types).<br>
 * <br>
 * Content presenters are configured using the <code>implentationClass</code> property of a {@link info.magnolia.ui.workbench.definition.ContentPresenterDefinition},
 * and by registering such definition in the workbench's contentViews node in configuration.
 */
public interface ContentPresenter {

    /**
     * Initializes the presenter with the workbench definition, eventBus and viewType name.
     * 
     * @param workbenchDefinition the workbench definition that defines which data to present
     * @param eventBus the event bus to fire e.g. selection events on
     * @param viewTypeName the view type as defined in the presenter definition
     * @return the content view
     */
    ContentView start(WorkbenchDefinition workbenchDefinition, EventBus eventBus, String viewTypeName);

    /**
     * Refreshes the data container and view.
     */
    void refresh();

    /**
     * @return the selected item ids in the content view.
     */
    List<String> getSelectedItemIds();

    /**
     * Sets the selected item ids for this presenter to react on, e.g. with keyboard shortcuts.
     */
    void setSelectedItemIds(List<String> itemId);

    /**
     * Selects the given items in the content view.
     */
    void select(List<String> itemIds);

    /**
     * Make sure the given items are visible in the content view.
     */
    void expand(String itemId);
}
