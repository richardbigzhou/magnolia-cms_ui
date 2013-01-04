/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell;

import java.io.Serializable;

/**
 * Helper class for holding the parsed info from the fragment.
 */
public class Fragment implements Serializable {

    private ViewportType appViewportType = ViewportType.SHELL_APP;

    private String appId = "";

    private String subAppId = "";

    private String parameter = "";

    public Fragment() {
    }

    public static Fragment fromString(final String fragment) {
        Fragment dto = new Fragment();
        String type = extractAppType(fragment);
        if (type.equals("shell")) {
            dto.appViewportType = ViewportType.SHELL_APP;
            dto.appId = ShellAppType.getTypeByFragmentId(extractAppId(fragment));
            dto.subAppId = extractSubAppId(fragment);
            dto.parameter = extractSubAppId(fragment);
        } else if (type.equals("app")) {
            dto.appViewportType = ViewportType.APP;
            dto.appId = extractAppId(fragment);
            dto.subAppId = extractSubAppId(fragment);
            dto.parameter = extractParameter(fragment);
        }
        return dto;
    }

    public ShellAppType resolveShellAppType() {
        if (isApp()) {
            return null;
        }
        try {
            return ShellAppType.valueOf(appId.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ShellAppType.APPLAUNCHER;
        }
    }
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public void setSubAppId(String subAppId) {
        this.subAppId = subAppId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppViewportType(ViewportType type) {
        this.appViewportType = type;
    }

    public ViewportType getAppViewportType() {
        return appViewportType;
    }

    public String getAppId() {
        return appId;
    }

    public String getSubAppId() {
        return subAppId;
    }

    public String getParameter() {
        return parameter;
    }

    // These methods are duplicated from info.magnolia.ui.framework.location.DefaultLocation

    public String toFragment() {
        return toString();
    }

    @Override
    public String toString() {
        return appViewportType.getFragmentPrefix() + appId + ":" + subAppId + ";" + parameter;
    }

    public static String extractAppType(String fragment) {
        int i = fragment.indexOf(':');
        return i != -1 ? fragment.substring(0, i) : fragment;
    }

    public static String extractAppId(String fragment) {
        fragment = removeParameter(fragment);
        int i = fragment.indexOf(':');
        if (i == -1) {
            return "";
        }
        int j = fragment.indexOf(':', i + 1);
        return j != -1 ? fragment.substring(i + 1, j) : fragment.substring(i + 1);
    }

    public static String extractSubAppId(String fragment) {
        fragment = removeParameter(fragment);

        int i = fragment.indexOf(':');
        if (i == -1) {
            return "";
        }
        int j = fragment.indexOf(':', i + 1);
        if (j == -1) {
            return "";
        }
        return fragment.substring(j + 1);
    }

    public static String extractParameter(String fragment) {
        int i = fragment.indexOf(';');
        if (i == -1) {
            return "";
        }
        return fragment.substring(i + 1);
    }

    private static String removeParameter(String fragment) {
        int i = fragment.indexOf(';');
        return i != -1 ? fragment.substring(0, i) : fragment;
    }

    public boolean isShellApp() {
        return appViewportType == ViewportType.SHELL_APP;
    }
    
    public boolean isApp() {
        return !isShellApp();
    }
}
