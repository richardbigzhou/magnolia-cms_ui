/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.admincentral.app.content.location;

import info.magnolia.ui.admincentral.content.item.ItemView;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;

/**
 * ItemLocation.
 */
public class ItemLocation extends DefaultLocation {

    private ItemView.ViewType viewType;
    private String nodePath;

    public ItemLocation(String appId, String subAppId, String parameter) {
        super(LOCATION_TYPE_APP, appId, subAppId, parameter);

        setNodePath(extractNodePath(parameter));
        setViewType(extractViewType(parameter));
    }

    public String getNodePath() {
        return nodePath;
    }


    /**
     * If the node path is empty, assume root path.
     */
    private void setNodePath(String nodePath) {
        this.nodePath = (nodePath == null || nodePath.isEmpty()) ?  "/" : nodePath;
    }

    public ItemView.ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ItemView.ViewType viewType) {
        this.viewType = viewType;
    }

    private String extractNodePath(String parameter) {
        int i = parameter.indexOf(':');
        return i != -1 ? parameter.substring(0, i) : parameter;
    }

    private ItemView.ViewType extractViewType(String parameter) {
        String action = "";
        // nodePath
        int i = parameter.indexOf(':');
        if (i != -1) {
            // view
            int j = parameter.indexOf(':', i + 1);
            action = (j != -1) ? parameter.substring(i + 1, j) : parameter.substring(i + 1);
        }
        return ItemView.ViewType.fromString(action);
    }

    protected void updateParameter() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodePath);
        sb.append(":");
        sb.append(viewType.getText());
        super.setParameter(sb.toString());
    }

    public static ItemLocation wrap(Location location) {
        DefaultLocation l = (DefaultLocation) location;
        return new ItemLocation(l.getAppId(), l.getSubAppId(), l.getParameter());
    }

    public void updateNodePath(String newNodePath) {
        setNodePath(newNodePath);
        updateParameter();
    }

    public void updateAction(ItemView.ViewType newViewType) {
        setViewType(newViewType);
        updateParameter();
    }

}
