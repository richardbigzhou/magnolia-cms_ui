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
package info.magnolia.ui.vaadin.integration.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility for using paths as returned by the {@link HttpServletRequest} class.
 *
 * @version $Id$
 */
public class RequestPathUtil {

    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

    public static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";
    public static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";
    public static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";

    /**
     * Returns "" if the whole path has been matched by the servlet path.
     */
    public static String getPathWithinServletMapping(HttpServletRequest request) {
        String pathWithinApplication = getPathWithinApplication(request);
        String servletPath = getServletPath(request);
        if (pathWithinApplication.startsWith(servletPath)) {
            pathWithinApplication = pathWithinApplication.substring(servletPath.length());
        }
        return pathWithinApplication;
    }

    public static String getPathWithinApplication(HttpServletRequest request) {
        String pathWithinApplication = getRequestUri(request);
        String contextPath = getContextPath(request);
        if (pathWithinApplication.startsWith(contextPath)) {
            pathWithinApplication = pathWithinApplication.substring(contextPath.length());
        }
        return pathWithinApplication;
    }

    /**
     * Returns the request uri for the request. If the request is an include it will return the uri being included. The
     * returned uri is not decoded.
     */
    public static String getRequestUri(HttpServletRequest request) {
        if (request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null) {
            return (String) request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE);
        }
        return cleanRequestUri(decodeUri(request, request.getRequestURI()));
    }

    /**
     * The servlet container is supposed to not include the part of the request uri following a semicolon but some
     * containers (Jetty) leaves it in. This method removes it if it's there.
     *
     * @param uri a decoded request uri
     */
    private static String cleanRequestUri(String uri) {
        int i = uri.indexOf(';');
        if (i != -1) {
            uri = uri.substring(0, i);
        }
        return uri;
    }

    /**
     * Returns the decoded context path or empty if running as the root context. The context path always starts with a
     * slash and never ends with a trailing slash.
     */
    public static String getContextPath(HttpServletRequest request) {
        String contextPath = (String) request.getAttribute(INCLUDE_CONTEXT_PATH_ATTRIBUTE);
        if (contextPath == null) {
            contextPath = request.getContextPath();
        }
        if ("/".equals(contextPath)) {
            // Special case for includes in Jetty
            contextPath = "";
        }
        return decodeUri(request, contextPath);
    }

    /**
     * Returns the part of the request uri used to match the servlet being called. The servlet path has already been
     * decoded by the servlet container.
     */
    public static String getServletPath(HttpServletRequest request) {
        String servletPath = (String) request.getAttribute(INCLUDE_SERVLET_PATH_ATTRIBUTE);
        if (servletPath == null) {
            servletPath = request.getServletPath();
        }
        if (servletPath.endsWith("/")) {
            servletPath = servletPath.substring(servletPath.length() - 1);
        }
        return servletPath;
    }

    private static String decodeUri(HttpServletRequest request, String uri) {
        try {
            return URLDecoder.decode(uri, determineEncoding(request));
        } catch (UnsupportedEncodingException e) {
            return URLDecoder.decode(uri);
        }
    }

    private static String determineEncoding(HttpServletRequest request) {
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = DEFAULT_CHARACTER_ENCODING;
        }
        return characterEncoding;
    }
}
