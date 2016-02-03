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

import org.apache.commons.lang3.StringUtils;

/**
 * ContentLocation used in ContentSubApps. Extends the Default Location by adding fields for the nodePath, viewType and query.
 */
public class BrowserLocation extends DefaultLocation {

    private String nodePath;
    private String viewType;
    private String query;

    public BrowserLocation(String appName, String subAppId, String parameter) {
        super(LOCATION_TYPE_APP, appName, subAppId, parameter);

        parameter = StringUtils.defaultString(parameter);
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

    public String getViewType() {
        return viewType;
    }

    private void setViewType(String viewType) {
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

    private String extractView(String parameter) {
        // first param is path
        int i = parameter.indexOf(':');
        if (i != -1) {
            // isolate view type parameter, there can be more
            int j = parameter.indexOf(':', i + 1);
            String view = (j != -1) ? parameter.substring(i + 1, j) : parameter.substring(i + 1);
            if (StringUtils.isNotBlank(view)) {
                return view;
            }
        }
        return null;
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
        sb.append(viewType);
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

    public void updateViewType(String newViewType) {
        setViewType(newViewType);
        updateParameter();
    }

    public void updateQuery(String newQuery) {
        setQuery(newQuery);
        updateParameter();
    }
}
