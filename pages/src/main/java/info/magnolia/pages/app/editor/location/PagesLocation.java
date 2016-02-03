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
package info.magnolia.pages.app.editor.location;

import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;

/**
 * PagesLocation.
 */
public class PagesLocation extends DefaultLocation {

    public static final String APP_ID = "pages";
    public static final String SUB_APP_ID = "editor";

    private String nodePath;

    private String mode;

    public PagesLocation(String parameter) {
        super(LOCATION_TYPE_APP, APP_ID, SUB_APP_ID, parameter);

        this.nodePath = extractNodePath(parameter);
        this.mode = extractMode(parameter);
    }

    private String extractNodePath(String parameter) {
        int i = parameter.indexOf(':');
        return i != -1 ? parameter.substring(0, i) : parameter;
    }

    private String extractMode(String parameter) {
        int i = parameter.indexOf(':');
        return i != -1 ? parameter.substring(i + 1) : parameter;
    }

    public String getNodePath() {
        return nodePath;
    }

    public String getMode() {
        return mode;
    }

    public static PagesLocation wrap(Location location) {
        return new PagesLocation(location.getParameter());
    }
}
