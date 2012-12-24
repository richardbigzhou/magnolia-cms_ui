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
package info.magnolia.ui.framework.location;

import org.apache.commons.lang.StringUtils;

import java.util.StringTokenizer;

/**
 * Default location implementation.<p>
 * {@code appType:appId:subAppId;some/parameter}
 */
public class DefaultLocation implements Location {

    private String appType;
    
    private String appId;
    
    private String subAppId;

    private String parameter;

    public DefaultLocation() {}
    
    public DefaultLocation(String appType, String appId) {
        this.appType = appType;
        this.appId = appId;
    }

    public DefaultLocation(String appType, String appId, String subAppId) {
        this.appType = appType;
        this.appId = appId;
        this.subAppId = subAppId;
    }

    public DefaultLocation(String appType, String appId, String subAppId, String parameter) {
        this.appType = appType;
        this.appId = appId;
        this.subAppId = subAppId;
        this.parameter = parameter;
    }

    public DefaultLocation(String fragment) {
        parseLocation(fragment);
    }

    private void parseLocation(String fragment) {
        String[] split = StringUtils.split(";");
        setAppParams(split[0]);
        this.parameter = split[1];
    }

    private void setAppParams(String appParams) {
        StringTokenizer tokenizer = new StringTokenizer(appParams, ":");
        this.appType = (tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "";
        this.appId = (tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "";
        this.subAppId = (tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "";
    }

    @Override
    public String getAppType() {
        return appType;
    }

    @Override
    public String getAppId() {
        return appId;
    }

    @Override
    public String getSubAppId() {
        return subAppId;
    }

    @Override
    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public void setSubAppId(String subAppId) {
        this.subAppId = subAppId;
    }
    
    public void setAppType(String appType) {
        this.appType = appType;
    }
    
    public void setAppId(String appId) {
        this.appId = appId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        DefaultLocation that = (DefaultLocation) o;

        if (appType != null ? !appType.equals(that.appType) : that.appType != null) {
            return false;
        }
        if (appId != null ? !appId.equals(that.appId) : that.appId != null) {
            return false;
        }
        if (subAppId != null ? !subAppId.equals(that.subAppId) : that.subAppId != null) {
            return false;
        }
        if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = appType != null ? appType.hashCode() : 0;
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        result = 31 * result + (subAppId != null ? subAppId.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (appType != null && appType.length() != 0) {
            sb.append(appType);
            if (appId != null && appId.length() != 0) {
                sb.append(":").append(appId);
            }
            if (subAppId != null && subAppId.length() != 0) {
                sb.append(":").append(subAppId);
            }
            if (parameter != null && parameter.length() != 0) {
                sb.append(";").append(parameter);
            }
        }
        return sb.toString();
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

}
