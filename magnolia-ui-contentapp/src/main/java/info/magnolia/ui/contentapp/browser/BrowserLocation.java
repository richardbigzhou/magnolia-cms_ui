/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.contentapp.browser;

import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.workbench.ContentView;

/**
 * ContentLocation used in ContentSubApps. Extends the Default Location by adding fields for the nodePath, viewType and query.
 */
public class BrowserLocation extends DefaultLocation {

    private String nodePath;
    private ContentView.ViewType viewType;
    private String query;

    public BrowserLocation(String appName, String subAppId, String parameter) {
        super(LOCATION_TYPE_APP, appName, subAppId, parameter);

        setNodePath(extractNodePath(parameter));
        setViewType(extractView(parameter));
        setQuery(extractQuery(parameter));
    }

    public String getNodePath() {
        return nodePath;
    }

    /**
     * If the node path is empty, assume root path.
     */
    private void setNodePath(String nodePath) {
        this.nodePath = (nodePath == null || nodePath.isEmpty()) ? "/" : nodePath;
    }

    public ContentView.ViewType getViewType() {
        return viewType;
    }

    private void setViewType(ContentView.ViewType viewType) {
        this.viewType = viewType;
    }

    public String getQuery() {
        return query;
    }

    private void setQuery(String query) {
        this.query = query;
    }

    private String extractNodePath(String parameter) {
        int i = parameter.indexOf(':');
        return i != -1 ? parameter.substring(0, i) : parameter;
    }

    private ContentView.ViewType extractView(String parameter) {
        String view = "";
        // nodePath
        int i = parameter.indexOf(':');
        if (i != -1) {
            // view
            int j = parameter.indexOf(':', i + 1);
            view = (j != -1) ? parameter.substring(i + 1, j) : parameter.substring(i + 1);
        }
        return ContentView.ViewType.fromString(view);
    }

    public static String extractQuery(String fragment) {
        // nodePath
        int i = fragment.indexOf(':');
        if (i == -1) {
            return "";
        }
        // view
        int j = fragment.indexOf(':', i + 1);
        if (j == -1) {
            return "";
        }
        // query
        return fragment.substring(j + 1);
    }

    protected void updateParameter() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodePath);
        sb.append(":");
        sb.append(viewType.getText());
        sb.append(":");
        sb.append(query);
        super.setParameter(sb.toString());
    }

    public static BrowserLocation wrap(Location location) {
        return new BrowserLocation(location.getAppName(), location.getSubAppId(), location.getParameter());
    }

    public void updateNodePath(String newNodePath) {
        setNodePath(newNodePath);
        updateParameter();
    }

    public void updateViewType(ContentView.ViewType newViewType) {
        setViewType(newViewType);
        updateParameter();
    }

    public void updateQuery(String newQuery) {
        setQuery(newQuery);
        updateParameter();
    }
}
