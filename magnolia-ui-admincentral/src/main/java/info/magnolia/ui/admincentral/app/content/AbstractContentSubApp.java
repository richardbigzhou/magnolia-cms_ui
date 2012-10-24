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
import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;
import org.apache.commons.lang.StringUtils;

import javax.inject.Named;
import java.util.regex.Pattern;

/**
 * Abstract class providing a sensible implementation for services shared by all content subapps.
 * Out-of-the-box it will handle the following
 * <ul>
 * <li>location updates when switching views, selecting items or performing searches: see {@link #locationChanged(Location)} and {@link #createLocation()}
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
 * A number of static utility methods for dealing with the {@link Location} object is also provided.
 *
 * @see ContentWorkbenchPresenter
 * @see ContentAppView
 * @see AppContext
 * @see AbstractContentApp
 * @see DefaultLocation
 */
public abstract class AbstractContentSubApp extends AbstractSubApp {

    private ContentWorkbenchPresenter workbench;

    public AbstractContentSubApp(final AppContext appContext, final ContentAppView view, final ContentWorkbenchPresenter workbench, final @Named("subapp") EventBus subAppEventBus) {

        super(appContext, view, subAppEventBus);
        if(appContext == null || view == null || workbench == null || subAppEventBus == null) {
            throw new IllegalArgumentException("Constructor does not allow for null args. Found AppContext = " + appContext + ", ContentAppView = " + view + ", ContentWorkbenchPresenter = " + workbench + ", EventBus = " + subAppEventBus);
        }
        this.workbench = workbench;
        registerSubAppEventsHandlers(appContext, subAppEventBus, this);
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
        ContentLocation l = ContentLocation.wrap((DefaultLocation)location);
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
     *   #app:myapp:main:/foo/bar:list
     * }
     * <p>
     * this method will select the path <code>/foo/bar</code> in the workspace used by the app, set the view type as <code>list</code> and finally update the available actions.
     * <p>
     * In case of a search view the URI fragment will look similar to the following one
     * {@code
     *   #app:myapp:main:/:search;qux
     * }
     * <p>
     * then this method will select the root path, set the view type as <code>search</code>, perform a search for "qux" in the workspace used by the app and finally update the available actions.
     * @see AbstractContentSubApp#updateActionbar(ActionbarPresenter)
     * @see AbstractContentSubApp#start(Location)
     * @see Location
     */
    protected final void restoreWorkbench(final ContentLocation location) {
        String path = location.getNodePath();
        ViewType viewType = location.getView();
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
    public final ContentAppView getView() {
        return (ContentAppView) super.getView();
    }

    /**
     * The default implementation selects the path in the current workspace and updates the available actions in the actionbar.
     */
    @Override
    public void locationChanged(final Location location) {
        ContentLocation contentLocation = (ContentLocation) location;
        String selectedItemPath = contentLocation.getNodePath();
        if (selectedItemPath != null) {
            getWorkbench().selectPath(selectedItemPath);
        }
        updateActionbar(getWorkbench().getActionbarPresenter());
    }


    //Some of the following class members have default visibility scope for the sake of testability.
    /**
     * Token type element.
     * A token here is the URI fragment part made up by zero or more elements.
     * In this case we will have
     * {@code
     *   #app:<appName>:<subAppId>:<selectedPathToken>:<viewTypeToken>[;<queryToken>]
     * }
     */
    enum TokenElementType { PATH, VIEW, QUERY }


    /*
    * Creates a location for the current subapp given the current location, the passed parameter and its type.
    */
    protected final DefaultLocation createLocation(final String parameter, final ContentLocation currentLocation, final TokenElementType type) {
        DefaultLocation location = createLocation();
        if (currentLocation != null && type != null) {
            String token = location.getParameter();
            token = replaceLocationToken(currentLocation, parameter, type);
            return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, getAppName(), getSubAppId(), token);
        }
        return location;
    }


    /*
    * If type is PATH or VIEW and token to replace is null/empty it returns the current token. Only in case of QUERY the token to replace can be null/empty
    */
    String replaceLocationToken(final ContentLocation location, final String tokenPartToReplace, final TokenElementType type) {
        String newToken = null;
        String query = location.getQuery();
        String viewType = location.getView().getText();

        switch(type) {
            case PATH:
                if(StringUtils.isNotBlank(tokenPartToReplace)) {
                    newToken = location.getParameter().replaceFirst(location.getNodePath(), tokenPartToReplace);
                }
                break;
            case VIEW :
                if(StringUtils.isNotBlank(tokenPartToReplace)) {
                    if(StringUtils.isNotEmpty(query)) {
                        //here we need Pattern.quote() as the query might contain special chars such as the wildcard *, which in regex has a different meaning
                        //and would prevent the replace method from working properly.
                        newToken = location.getParameter().replaceFirst(viewType + ";" + Pattern.quote(query), tokenPartToReplace);
                    } else {
                        newToken = location.getParameter().replaceFirst(viewType, tokenPartToReplace);
                    }}
                break;
            case QUERY :
                //searchbox can be emptied after having performed a query. This means that we must keep the view and discard the query only
                if(StringUtils.isNotEmpty(query)) {
                    newToken = location.getParameter().replaceFirst(StringUtils.isBlank(tokenPartToReplace) ? (";" + Pattern.quote(query)): Pattern.quote(query), tokenPartToReplace);
                } else {
                    newToken = location.getParameter().replaceFirst(viewType, StringUtils.isBlank(tokenPartToReplace) ? viewType : (viewType + ";" + tokenPartToReplace));
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown token type " + type);
        }
        return newToken == null? location.getParameter() : newToken;
    }

    @Override
    public ContentLocation getCurrentLocation() {
        return (ContentLocation) super.getCurrentLocation();
    }

    /*
     * Registers general purpose handlers for the following events:
     * <ul>
     * <li> {@link ItemSelectedEvent}
     * <li> {@link ViewTypeChangedEvent}
     * <li> {@link SearchEvent}
     * </ul>
     */
    private void registerSubAppEventsHandlers(final AppContext appContext, final EventBus subAppEventBus, final AbstractContentSubApp subApp) {
        final ActionbarPresenter actionbar = subApp.getWorkbench().getActionbarPresenter();
        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                currentLocation = createLocation(event.getPath(), getCurrentLocation(), TokenElementType.PATH);
                appContext.setSubAppLocation(subApp, currentLocation);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(ViewTypeChangedEvent.class, new ViewTypeChangedEvent.Handler() {

            @Override
            public void onViewChanged(ViewTypeChangedEvent event) {
                currentLocation = createLocation(event.getViewType().getText(), getCurrentLocation(), TokenElementType.VIEW);
                appContext.setSubAppLocation(subApp, currentLocation);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {

            @Override
            public void onSearch(SearchEvent event) {
                currentLocation = createLocation(event.getSearchExpression(), getCurrentLocation(), TokenElementType.QUERY);
                appContext.setSubAppLocation(subApp, currentLocation);
                updateActionbar(actionbar);
            }
        });
    }


}
