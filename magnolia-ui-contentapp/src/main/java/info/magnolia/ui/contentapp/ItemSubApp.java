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
package info.magnolia.ui.contentapp;

import info.magnolia.ui.contentapp.location.ItemLocation;
import info.magnolia.ui.contentapp.workbench.ItemWorkbenchPresenter;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.vaadin.view.View;

import javax.inject.Inject;

/**
 * Base implementation of an item subApp. Provides sensible implementation for services shared by all item subApps.
 * Implementers of this class represent a tab for viewing and editing items typically opened from an {@link ContentSubApp}.
 * Subclasses can augment the default behavior and perform additional tasks by overriding the following methods:
 * <ul>
 * <li>{@link #onSubAppStart()}
 * <li>{@link #locationChanged(Location)}
 * </ul>
 *
 * Currently lacking listeners for {@link info.magnolia.ui.framework.event.ContentChangedEvent}.
 * Currently lacking handling of locationChanged. Related to MGNLUI-154
 *
 * @see ItemWorkbenchPresenter
 * @see WorkbenchSubAppView
 * @see ItemLocation
 */
public class ItemSubApp extends BaseSubApp {

    private final ItemWorkbenchPresenter workbench;

    private String caption;

    @Inject
    protected ItemSubApp(final SubAppContext subAppContext, final WorkbenchSubAppView view, ItemWorkbenchPresenter workbench) {
        super(subAppContext, view);
        this.workbench = workbench;
    }

    /**
     * Performs some routine tasks needed by all item subApps before the view is displayed.
     * The tasks are:
     * <ul>
     * <li>setting the current location
     * <li>setting the workbench view
     * <li>calling {@link #onSubAppStart()} a hook-up method subclasses can override to perform additional work.
     * </ul>
     */
    @Override
    public View start(final Location location) {
        ItemLocation l = ItemLocation.wrap(location);
        super.start(l);
        this.caption = l.getNodePath();
        getView().setWorkbenchView(workbench.start(l.getNodePath(), l.getViewType()));
        return getView();
    }

    /**
     * Wraps the current DefaultLocation in a ItemLocation. Providing getter and setters for used parameters.
     */
    @Override
    public ItemLocation getCurrentLocation() {
        return ItemLocation.wrap(super.getCurrentLocation());
    }

    @Override
    public WorkbenchSubAppView getView() {
        return (WorkbenchSubAppView) super.getView();
    }

    @Override
    public boolean supportsLocation(Location location) {
        ItemLocation itemLocation = ItemLocation.wrap(location);
        String currentPath = getCurrentLocation().getNodePath();
        return currentPath.equals(itemLocation.getNodePath());
    }

    @Override
    public void locationChanged(Location location) {
        ItemLocation itemLocation = ItemLocation.wrap(location);
        // getView().setWorkbenchView(workbench.start(itemLocation.getNodePath()));
        super.locationChanged(location);
    }

    private boolean hasLocationChanged(ItemLocation location) {
        return getCurrentLocation().getViewType() != location.getViewType();
    }

    @Override
    public String getCaption() {
        return caption;
    }

}
