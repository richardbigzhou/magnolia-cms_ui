/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.contentapp.detail;

import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;

import org.apache.commons.lang3.StringUtils;

/**
 * ItemLocation used in implementers of {@link info.magnolia.ui.contentapp.detail.DetailSubApp}.
 * Extends the Default Location by adding fields for :
 * <ul>
 * <li>the nodePath (some/node/path)</li>
 * <li>the {@link DetailView.ViewType} (viewType)</li>
 * <li>the node version (version)</li>
 * </ul>
 * <p>
 * {@code appType:appName:subAppId;some/node/path:viewType:version}
 */
public class DetailLocation extends DefaultLocation {

    private DetailView.ViewType viewType;
    private String nodePath;
    private String version;
    // Position of the parameter based on the ':' used as separator.
    private final static int NODE_PATH_PARAM_POSITION = 0;
    private final static int VIEW_TYPE_PARAM_POSITION = 1;
    private final static int VERSION_PARAM_POSITION = 2;

    public DetailLocation(String appName, String subAppId, String parameter) {
        super(LOCATION_TYPE_APP, appName, subAppId, parameter);

        setNodePath(extractNodePath(parameter));
        setViewType(extractViewType(parameter));
        setVersion(extractVersion(parameter));
    }

    public DetailLocation(String appName, String subAppId, DetailView.ViewType viewType, String nodePath, String version) {
        super(LOCATION_TYPE_APP, appName, subAppId);

        setNodePath(nodePath);
        setViewType(viewType);
        setVersion(version);
        updateParameter();
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

    public DetailView.ViewType getViewType() {
        return viewType;
    }

    public void setViewType(DetailView.ViewType viewType) {
        this.viewType = viewType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean hasVersion() {
        return StringUtils.isNotBlank(version);
    }

    /**
     * Extract the Node path from the parameter.
     *
     * @param parameter some/node/path:viewType:version
     * @return some/node/path
     */
    private String extractNodePath(String parameter) {
        return getParameter(parameter, NODE_PATH_PARAM_POSITION);
    }

    /**
     * Extract the viewType from the parameter.
     *
     * @param parameter some/node/path:viewType:version
     * @return viewType
     */
    private DetailView.ViewType extractViewType(String parameter) {
        String action = getParameter(parameter, VIEW_TYPE_PARAM_POSITION);
        return DetailView.ViewType.fromString(action);
    }

    /**
     * Extract the Node Version from the parameter.
     *
     * @param parameter some/node/path:viewType:version
     * @return version
     */
    private String extractVersion(String parameter) {
        return getParameter(parameter, VERSION_PARAM_POSITION);
    }

    protected String getParameter(String parameter, int position) {
        String arguments[] = StringUtils.split(parameter, ':');
        if (position <= arguments.length - 1) {
            return arguments[position];
        }
        return "";
    }

    protected void updateParameter() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodePath);
        sb.append(":");
        sb.append(viewType.getText());
        if (StringUtils.isNotBlank(version)) {
            sb.append(":");
            sb.append(version);
        }
        super.setParameter(sb.toString());
    }

    public static DetailLocation wrap(Location location) {
        return new DetailLocation(location.getAppName(), location.getSubAppId(), location.getParameter());
    }

    public void updateNodePath(String newNodePath) {
        setNodePath(newNodePath);
        updateParameter();
    }

    public void updateViewtype(DetailView.ViewType newViewType) {
        setViewType(newViewType);
        updateParameter();
    }

    public void updateVersion(String newVersion) {
        setVersion(newVersion);
        updateParameter();
    }

}
