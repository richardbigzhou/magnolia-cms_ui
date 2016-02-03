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
package info.magnolia.ui.api.location;

import info.magnolia.context.MgnlContext;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default location implementation. Follows the pattern: {@code appType:appName:subAppId;some/parameter}.
 */
public class DefaultLocation implements Location {

    private static Logger log = LoggerFactory.getLogger(DefaultLocation.class);

    private String appType;

    private String appName;

    private String subAppId;

    private String parameter;

    public DefaultLocation() {
    }

    public DefaultLocation(String appType, String appName) {
        this(appType, appName, "");
    }

    public DefaultLocation(String appType, String appName, String subAppId) {
        this(appType, appName, subAppId, "");
    }

    public DefaultLocation(String appType, String appName, String subAppId, String parameter) {
        this.appType = decodeFragment(appType);
        this.appName = decodeFragment(appName);
        this.subAppId = decodeFragment(subAppId);
        this.parameter = decodeFragment(parameter);
    }

    /**
     * @throws IllegalArgumentException if the passed fragment is null or empty.
     */
    public DefaultLocation(String fragment) {
        if (StringUtils.isBlank(fragment)) {
            throw new IllegalArgumentException("Fragment cannot be empty or null");
        }
        parseLocation(fragment);
    }

    private void parseLocation(String fragment) {
        String[] split = StringUtils.split(fragment, ";");
        setAppParams(split[0]);
        if (split.length == 2) {
            this.parameter = decodeFragment(split[1]);
        }
    }

    private void setAppParams(String appParams) {
        StringTokenizer tokenizer = new StringTokenizer(appParams, ":");
        this.appType = decodeFragment((tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "");
        this.appName = decodeFragment((tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "");
        this.subAppId = decodeFragment((tokenizer.hasMoreTokens()) ? tokenizer.nextToken() : "");
    }

    @Override
    public String getAppType() {
        return appType;
    }

    @Override
    public String getAppName() {
        return appName;
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
        this.parameter = decodeFragment(parameter);
    }

    public void setSubAppId(String subAppId) {
        this.subAppId = decodeFragment(subAppId);
    }

    public void setAppType(String appType) {
        this.appType = decodeFragment(appType);
    }

    public void setAppName(String appName) {
        this.appName = decodeFragment(appName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || !(o instanceof  DefaultLocation)) {
            return false;
        }

        DefaultLocation that = (DefaultLocation) o;

        if (appType != null ? !appType.equals(that.appType) : that.appType != null) {
            return false;
        }
        if (appName != null ? !appName.equals(that.appName) : that.appName != null) {
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
        result = 31 * result + (appName != null ? appName.hashCode() : 0);
        result = 31 * result + (subAppId != null ? subAppId.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (appType != null && appType.length() != 0) {
            sb.append(appType);
            if (appName != null && appName.length() != 0) {
                sb.append(":").append(appName);
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

    public static String extractAppName(String fragment) {
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

    /**
     * Decodes <code>application/x-www-form-urlencoded</code> fragment string using a specified encoding scheme if necessary.
     */
    public static String decodeFragment(String fragment, String encoding) {
        if (fragment == null) {
            return fragment;
        }

        if (fragment.indexOf('%') > -1) {
            try {
                fragment = URLDecoder.decode(fragment, encoding);
            } catch (UnsupportedEncodingException e) {
                log.error("Error decoding fragment '" + fragment + "' with encoding '" + encoding + "'", e);
            }
        }

        return fragment;
    }

    /**
     * Decodes a fragment using the character encoding from the {@link info.magnolia.cms.core.AggregationState}.
     */
    public static String decodeFragment(String fragment) {
        return decodeFragment(fragment, MgnlContext.getAggregationState().getCharacterEncoding());
    }

}
