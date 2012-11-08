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
package info.magnolia.ui.admincentral.app.content;

import info.magnolia.ui.admincentral.actionbar.ActionbarPresenter;
import info.magnolia.ui.admincentral.app.content.location.ContentLocation;
import info.magnolia.ui.admincentral.content.view.ContentView.ViewType;
import info.magnolia.ui.admincentral.event.ItemSelectedEvent;
import info.magnolia.ui.admincentral.event.SearchEvent;
import info.magnolia.ui.admincentral.event.ViewTypeChangedEvent;
import info.magnolia.ui.admincentral.workbench.ContentWorkbenchPresenter;
import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;

import javax.inject.Named;

/**
 * Abstract class providing a sensible implementation for services shared by all content subapps.
 * Out-of-the-box it will handle the following
 * <ul>
 * <li>location updates when switching views, selecting items or performing searches: see {@link #locationChanged(Location)}}
 * <li>restoring the workbench app status when i.e. coming from a bookmark: see {@link #start(Location)}
 * </ul>
 * In order to perform those tasks this class registers non-overridable handlers for the following events:
 * <ul>
 * <li> {@link ItemSelectedEvent}
 * <li> {@link ViewTypeChangedEvent}
 * <li> {@link SearchEvent}
 * </ul>
 * Subclasses can augment the default behavior and perform additional tasks by overriding the following methods:
 * <ul>
 * <li>{@link #onSubAppStart()}
 * <li>{@link #locationChanged(Location)}
 * <li>{@link #updateActionbar(ActionbarPresenter)}
 * </ul>
 *
 * @see ContentWorkbenchPresenter
 * @see WorkbenchSubAppView
 * @see AbstractContentApp
 * @see ContentLocation
 */
public abstract class AbstractContentSubApp extends AbstractSubApp {

    private ContentWorkbenchPresenter workbench;

    public AbstractContentSubApp(final SubAppContext subAppContext, final WorkbenchSubAppView view, final ContentWorkbenchPresenter workbench, final @Named("subapp") EventBus subAppEventBus) {

        super(subAppContext, view);
        if(subAppContext == null || view == null || workbench == null || subAppEventBus == null) {
            throw new IllegalArgumentException("Constructor does not allow for null args. Found AppContext = " + subAppContext + ", WorkbenchSubAppView = " + view + ", ContentWorkbenchPresenter = " + workbench + ", EventBus = " + subAppEventBus);
        }
        this.workbench = workbench;
        registerSubAppEventsHandlers(subAppEventBus, this);
    }

    /**
     * Performs some routine tasks needed by all content subapps before the view is displayed.
     * The tasks are:
     * <ul>
     * <li>setting the current location
     * <li>setting the workbench view
     * <li>restoring the workbench status: see {@link #restoreWorkbench(ContentLocation)}
     * <li>calling {@link #onSubAppStart()} a hook-up method subclasses can override to perform additional work.
     * </ul>
     */
    @Override
    public final View start(final Location location) {
        ContentLocation l = ContentLocation.wrap(location);
        super.start(l);
        getView().setWorkbenchView(workbench.start());
        restoreWorkbench(l);
        return getView();
    }

    /**
     * Restores the workbench status based on the information available in the location object. This is used e.g. when starting a subapp based on a
     * bookmark. I.e. given a bookmark containing the following URI fragment
     * <p>
     * {@code
     *   #app:myapp:main;/foo/bar:list
     * }
     * <p>
     * this method will select the path <code>/foo/bar</code> in the workspace used by the app, set the view type as <code>list</code> and finally update the available actions.
     * <p>
     * In case of a search view the URI fragment will look similar to the following one
     * {@code
     *   #app:myapp:main;/:search:qux
     * }
     * <p>
     * then this method will select the root path, set the view type as <code>search</code>, perform a search for "qux" in the workspace used by the app and finally update the available actions.
     * @see AbstractContentSubApp#updateActionbar(ActionbarPresenter)
     * @see AbstractContentSubApp#start(Location)
     * @see Location
     */
    protected final void restoreWorkbench(final ContentLocation location) {
        String path = location.getNodePath();
        ViewType viewType = location.getViewType();
        String query = location.getQuery();
        getWorkbench().resynch(path, viewType, query);
        updateActionbar(getWorkbench().getActionbarPresenter());
    }

    /**
     * Updates the actions available in the workbench's actionbar.
     * Depending on the selected item or on other conditions specific to a concrete app, certain actions will be enabled or disabled.
     * By default if no path is selected in the workbench, namely root is selected, "delete" and "edit" actions are not available.
     * If some path other than root is selected, "edit" and "delete" actions become available.
     * @see #restoreWorkbench(ContentLocation)
     * @see #locationChanged(Location)
     * @see ActionbarPresenter
     */
    protected void updateActionbar(final ActionbarPresenter actionbar) {

        if (getWorkbench().getSelectedItemId() == null || "/".equals(getWorkbench().getSelectedItemId())) {
            actionbar.disable("delete");
            actionbar.disable("edit");
        } else {
            actionbar.enable("delete");
            actionbar.enable("edit");
        }
    }

    protected final ContentWorkbenchPresenter getWorkbench() {
        return workbench;
    }

    @Override
    public final WorkbenchSubAppView getView() {
        return (WorkbenchSubAppView) super.getView();
    }

    /**
     * The default implementation selects the path in the current workspace and updates the available actions in the actionbar.
     */
    @Override
    public void locationChanged(final Location location) {
        super.locationChanged(location);
        ContentLocation contentLocation = ContentLocation.wrap(location);
        String selectedItemPath = contentLocation.getNodePath();
        if (selectedItemPath != null) {
            getWorkbench().selectPath(selectedItemPath);
        }
        updateActionbar(getWorkbench().getActionbarPresenter());
    }

    /**
     * Wraps the current DefaultLocation in a ContentLocation. Providing getter and setters for used parameters.
     * @return
     */
    @Override
    public ContentLocation getCurrentLocation() {
        return ContentLocation.wrap(super.getCurrentLocation());
    }

    /*
     * Registers general purpose handlers for the following events:
     * <ul>
     * <li> {@link ItemSelectedEvent}
     * <li> {@link ViewTypeChangedEvent}
     * <li> {@link SearchEvent}
     * </ul>
     */
    private void registerSubAppEventsHandlers(final EventBus subAppEventBus, final AbstractContentSubApp subApp) {
        final ActionbarPresenter actionbar = subApp.getWorkbench().getActionbarPresenter();
        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                ContentLocation location = getCurrentLocation();
                location.updateNodePath(event.getPath());
                currentLocation = location;
                getAppContext().setSubAppLocation(subApp, location);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(ViewTypeChangedEvent.class, new ViewTypeChangedEvent.Handler() {

            @Override
            public void onViewChanged(ViewTypeChangedEvent event) {
                ContentLocation location = getCurrentLocation();
                location.updateViewType(event.getViewType());
                currentLocation = location;
                getAppContext().setSubAppLocation(subApp, currentLocation);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {

            @Override
            public void onSearch(SearchEvent event) {
                ContentLocation location = getCurrentLocation();
                location.updateQuery(event.getSearchExpression());
                currentLocation = location;
                getAppContext().setSubAppLocation(subApp, currentLocation);
                updateActionbar(actionbar);
            }
        });
    }


}
