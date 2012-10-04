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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract class providing common services to content subapps.
 * TODO fgrilli write detailed javadoc.
 * TODO fgrilli review the TokenElementType and all the methods for manipulating the location.
 *
 */
public abstract class AbstractContentSubApp extends AbstractSubApp {

    public static final String MAIN_SUBAPP_ID = "main";

    /**
     * Token type element. I.e.
     * A token here is the URI fragment part made up by zero or more parameters.
     * In this case we will have
     * {@code
     *   #app:<appName>:<subAppId>:<selectedPathToken>:<viewTypeToken>[;<queryToken>]
     * }
     */
    protected enum TokenElementType { PATH, VIEW, QUERY }


    private DefaultLocation currentLocation;
    private ContentWorkbenchPresenter workbench;
    private ContentAppView view;
    private static String appName;

    public AbstractContentSubApp(final AppContext appContext, final ContentAppView view, final ContentWorkbenchPresenter workbench, final @Named("subapp") EventBus subAppEventBus) {
        AbstractContentSubApp.appName = appContext.getName();
        this.view = view;
        this.workbench = workbench;
        registerSubAppEventsHandlers(appContext, subAppEventBus, this);
    }

    @Override
    public View start(final Location location) {
        currentLocation = (DefaultLocation)location;
        view.setWorkbenchView(workbench.start());
        restoreWorkbench(location);
        return view;
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
     * @see AbstractContentSubApp#updateActionbar()
     * @see AbstractContentSubApp#start(Location)
     * @see Location
     */
    protected final void restoreWorkbench(final Location location) {
        String path = getSelectedItemPath(location);
        String viewType = getSelectedView(location);
        String query = getQuery(location);
        getWorkbench().resynch(path, ViewType.fromString(viewType), query);
        updateActionbar(getWorkbench().getActionbarPresenter());
    }

    /**
     * This method updates the actions available in the workbench's actionbar.
     * Depending on the selected item or on other conditions specific to a concrete app, certain actions will be enabled or disabled.
     * By default if no path is selected in the workbench, namely root is selected, "delete" and "edit" actions are not available.
     * If some path other than root is selected, "edit" and "delete" actions become available.
     * @see #restoreWorkbench(Location)
     * @see #locationChanged(Location)
     * @see ActionbarPresenter
     */
    protected void updateActionbar(final ActionbarPresenter actionbar) {
        // actions disabled based on selection
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

    /**
     * @return the app name as returned by {@link AppContext#getName()}.
     */
    public static final String getAppName() {
        return appName;
    }

    /**
     * The default implementation selects the path in the current workspace and updates the available actions.
     */
    @Override
    public void locationChanged(final Location location) {
        String selectedItemPath = getSelectedItemPath(location);
        if (selectedItemPath != null) {
            getWorkbench().selectPath(selectedItemPath);
        }
        updateActionbar(getWorkbench().getActionbarPresenter());
    }

    /**
     * Location token handling, format is {@code main:<selectedItemPath>:<viewType>[;<query>] } where <code>query</code> is present only if viewType is {@link ViewType#SEARCH}.
     * @return <code>true</code> if this location has tokens (the part after main) and subapp id is <code>main</code>.
     */
    public static final boolean supportsLocation(Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 1 && MAIN_SUBAPP_ID.equals(parts.get(0));
    }

    /**
     * Creates a location for the current subapp given the current location, the passed parameter and its type.
     */
    public static final DefaultLocation createLocation(final String parameter, final DefaultLocation currentLocation, final TokenElementType type) {
        DefaultLocation location = createLocation();
        if (StringUtils.isNotBlank(parameter) && currentLocation != null && type != null) {
            String token = location.getToken();
            token = replaceLocationToken(currentLocation, parameter, type);
            return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, getAppName(), token);
        }
        return location;
    }

    /**
     * Creates a default location for the current subapp whose token has the form <code>main:/:tree</code>.
     */
    public static final DefaultLocation createLocation() {
        String token = MAIN_SUBAPP_ID +":/:" + ViewType.TREE.getText();
        return new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, getAppName(), token);
    }

    public static final String getSubAppId(final Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.get(0);
    }

    /**
     * @return the selected item path as string or <code>/</code> (root) if none is present in the location.
     */
    public static final String getSelectedItemPath(final Location location) {
        List<String> parts = parseLocationToken(location);
        return parts.size() >= 2 ? parts.get(1) : "/";
    }

    /**
     * @return the selected {@link ViewType} as string or {@link ViewType#TREE} if none is present in the location.
     */
    public static final String getSelectedView(final Location location) {
        List<String> parts = parseLocationToken(location);
        if(parts.size() >= 3) {
            if(parts.get(2).indexOf(';') == -1) {
                return parts.get(2);
            } else {
                return parts.get(2).split(";")[0];
            }
        }
        return ViewType.TREE.getText();
    }
    /**
     * @return the query if present and if the view is search, else an empty string.
     */
    public static final String getQuery(final Location location) {
        List<String> parts = parseLocationToken(location);
        if(parts.size() >= 3) {
            if(parts.get(2).indexOf(';') == -1) {
                return "";
            } else {
                String[] view = parts.get(2).split(";");
                return ViewType.SEARCH.getText().equals(view[0]) ? view[1] : "";
            }
        }
        return "";
    }

    private static List<String> parseLocationToken(final Location location) {

        ArrayList<String> parts = new ArrayList<String>();

        DefaultLocation l = (DefaultLocation) location;
        String token = l.getToken();

        // "main"
        int i = token.indexOf(':');
        if (i == -1) {
            if (!MAIN_SUBAPP_ID.equals(token)) {
                return new ArrayList<String>();
            }
            parts.add(token);
            return parts;
        }

        String subAppName = token.substring(0, i);
        if (!MAIN_SUBAPP_ID.equals(subAppName)) {
            return new ArrayList<String>();
        }
        parts.add(subAppName);
        token = token.substring(i + 1);

        // selectedItemPath
        if (token.length() > 0 && token.indexOf(':') == -1) {
            parts.add(token);
        } else {
            // viewType and, if view type == search, its related query
            String[] tokenParts = token.split(":");
            for(String part: tokenParts) {
                parts.add(part);
            }
        }
        return parts;
    }

    private static String replaceLocationToken(final DefaultLocation location, final String tokenPartToReplace, final TokenElementType type) {
        String newToken = null;
        String query = getQuery(location);

        switch(type) {
        case PATH :
            newToken = location.getToken().replaceFirst(getSelectedItemPath(location), tokenPartToReplace);
            break;
        case VIEW :
            if(StringUtils.isNotEmpty(query)) {
                //here we need Pattern.quote() as the query might contain special chars such as the wildcard *, which in regex has a different meaning
                //and would prevent the replace method from working properly.
                newToken = location.getToken().replaceFirst(getSelectedView(location) + ";" + Pattern.quote(query), tokenPartToReplace);
            } else {
                newToken = location.getToken().replaceFirst(getSelectedView(location), tokenPartToReplace);
            }
            break;
        case QUERY :
            if(StringUtils.isNotEmpty(query)) {
                newToken = location.getToken().replaceFirst(query, tokenPartToReplace);
            } else {
                newToken = location.getToken().replaceFirst(getSelectedView(location), getSelectedView(location) + ";" + tokenPartToReplace);
            }
            break;
        }
        return newToken == null? location.getToken() : newToken;
    }

    /*
     * By default registers general purpose handlers for the following events:
     * <ul>
     * <li> {@link ItemSelectedEvent}
     * <li> {@link ViewTypeChangedEvent}
     * <li> {@link SearchEvent}
     * </ul>
     */
    private void registerSubAppEventsHandlers(final AppContext appContext, final EventBus subAppEventBus, final AbstractContentSubApp subapp) {
        final ActionbarPresenter actionbar = subapp.getWorkbench().getActionbarPresenter();
        subAppEventBus.addHandler(ItemSelectedEvent.class, new ItemSelectedEvent.Handler() {

            @Override
            public void onItemSelected(ItemSelectedEvent event) {
                currentLocation = createLocation(event.getPath(), currentLocation, TokenElementType.PATH);
                appContext.setSubAppLocation(subapp, currentLocation);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(ViewTypeChangedEvent.class, new ViewTypeChangedEvent.Handler() {

            @Override
            public void onViewChanged(ViewTypeChangedEvent event) {
                currentLocation = createLocation(event.getViewType().getText(), currentLocation, TokenElementType.VIEW);
                appContext.setSubAppLocation(subapp, currentLocation);
                updateActionbar(actionbar);
            }
        });

        subAppEventBus.addHandler(SearchEvent.class, new SearchEvent.Handler() {

            @Override
            public void onSearch(SearchEvent event) {
                currentLocation = createLocation(event.getSearchExpression(), currentLocation, TokenElementType.QUERY);
                appContext.setSubAppLocation(subapp, currentLocation);
                updateActionbar(actionbar);
            }
        });
    }

}
