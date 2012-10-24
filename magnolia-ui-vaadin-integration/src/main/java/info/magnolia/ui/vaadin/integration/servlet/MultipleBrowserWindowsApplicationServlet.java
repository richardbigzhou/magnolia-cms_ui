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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import com.vaadin.terminal.gwt.server.CommunicationManager;
import com.vaadin.ui.Window;


/**
 * Vaadin ApplicationServlet that supports running a unique Application per browser window. The
 * applications are identified in the URIs used by UIDL requests and file downloads. The browser
 * keeps track of which application is being used for the browser window (or tab) using the
 * document.name property.
 */
public class MultipleBrowserWindowsApplicationServlet extends MagnoliaIcePushServlet {

    private static final String ATTRIBUTE_APPLICATION_ID = MultipleBrowserWindowsApplicationServlet.class.getName() + ".applicationId";

    private static final String ATTRIBUTE_FORCE_APPLICATION_ID = MultipleBrowserWindowsApplicationServlet.class.getName() + ".forceApplicationId";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String caption;

    private String theme;

    // Implementations of Window and Application that are used only to provide the kick start page
    // with the details it
    // needs. They're reused between requests too reduce overhead.
    private final Window kickStartWindow = new Window() {

        @Override
        public String getTheme() {
            return theme;
        }

        @Override
        public String getCaption() {
            return caption;
        }
    };

    private final Application kickStartApplication = new Application() {

        @Override
        public void init() {
        }

        @Override
        public Window getMainWindow() {
            return kickStartWindow;
        }

        @Override
        public String getTheme() {
            return theme;
        }
    };

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.caption = servletConfig.getInitParameter("caption");
        this.theme = servletConfig.getInitParameter("theme");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // For normal requests (not ajax, not file upload, etc) we serve the browser a customized
        // kick start page. If
        // there's an application id in the uri then we let it pass through because it's probably a
        // request for
        // downloading an application resource.
        if (getRequestType(request) == RequestType.OTHER && getApplicationIdFromRequestPath(request) == null) {

            // Generate an application id that the client may or may not decide to use
            String applicationId = generateNewApplicationId();

            logger.debug("Suggesting application id: " + applicationId + " for request to " + request.getRequestURI());

            // If the 'restartApplication' parameter is present we force the client to start using
            // this application id
            boolean forceApplicationId = request.getParameter("restartApplication") != null;

            writeCustomAjaxPage(request, response, applicationId, forceApplicationId);

            return;
        }

        logger.debug("Serving request for " + request.getRequestURI());

        super.service(request, response);
    }

    private void writeCustomAjaxPage(HttpServletRequest request, HttpServletResponse response, String applicationId, boolean forceApplicationId) throws IOException, ServletException {

        // Set the arguments as request attributes so they can be found later on when writing the
        // custom script
        request.setAttribute(ATTRIBUTE_APPLICATION_ID, applicationId);
        request.setAttribute(ATTRIBUTE_FORCE_APPLICATION_ID, forceApplicationId);

        writeAjaxPage(request, response, kickStartWindow, kickStartApplication);
    }

    @Override
    protected void writeAjaxPageHtmlVaadinScripts(Window window, String themeName, Application application, BufferedWriter page, String appUrl,
        String themeUri, String appId, HttpServletRequest request) throws ServletException, IOException {
        super.writeAjaxPageHtmlVaadinScripts(window, themeName, application, page, appUrl, themeUri, appId, request);

        String applicationId = (String) request.getAttribute(ATTRIBUTE_APPLICATION_ID);
        boolean forceApplicationId = (Boolean) request.getAttribute(ATTRIBUTE_FORCE_APPLICATION_ID);

        page.write("<script type=\"text/javascript\">\n");
        page.write("//<![CDATA[\n");
        page.write("document.write(\"<script language='javascript' src='../VAADIN/js/jquery-1.7.2.min.js'><\\/script>\");\n");
        page.write("document.write(\"<script language='javascript' src='../VAADIN/js/jquery.transition.js'><\\/script>\");\n");
        if (forceApplicationId) {
            page.write("  window.name = \"" + applicationId + "\";\n");
        } else {
            page.write("  if (!window.name) {\n");
            page.write("    window.name = \"" + applicationId + "\";\n");
            page.write("  }\n");
        }

        page.write("  vaadin.vaadinConfigurations[\"" + appId + "\"][\"appUri\"] += \"/\" + window.name;\n");
        page.write("//]]>\n</script>\n");
    }

    @Override
    protected void writeAjaxPageHtmlHeader(BufferedWriter page, String title, String themeUri, HttpServletRequest request)
        throws IOException {
        super.writeAjaxPageHtmlHeader(page, title, themeUri, request);
        page.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />");

        // Add a CSS file based on browser in order to normalize the weight of the DinWebPro font.
        /*
        page.write("<script type=\"text/javascript\">\n");
        page.write("//<![CDATA[\n");
        page.write("if (navigator.userAgent.toLowerCase().indexOf('webkit')>-1 || navigator.appName == 'Microsoft Internet Explorer') {\n");
        page.write("    document.write(\"<link rel='stylesheet' type='text/css' href='../VAADIN/themes/admincentraltheme/css-conditional/fonts-heavy.css' \\/>\");\n");
        page.write("} else {\n");
        page.write("    document.write(\"<link rel='stylesheet' type='text/css' href='../VAADIN/themes/admincentraltheme/css-conditional/fonts-default.css' \\/>\");\n");
        page.write("}\n");
        page.write("//]]>\n</script>\n");
        */

        page.write("<script type=\"text/javascript\">\n");
        page.write("//<![CDATA[\n");
        page.write("document.write(\"<script language='javascript' src='../VAADIN/js/conditional-css-include.js'><\\/script>\");\n");
        page.write("//]]>\n</script>\n");
    }

    @Override
    protected URL getApplicationUrl(HttpServletRequest request) throws MalformedURLException {

        String path = getContextPath(request) + getServletPath(request);
        String applicationId = getApplicationIdFromRequestPath(request);
        if (applicationId != null) {
            path = path + "/" + applicationId;
        }

        final URL reqURL = new URL(
            (request.isSecure() ? "https://" : "http://")
                + request.getServerName()
                + ((request.isSecure() && request.getServerPort() == 443)
                    || (!request.isSecure() && request
                        .getServerPort() == 80) ? "" : ":"
                    + request.getServerPort())
                + path);
        return reqURL;
    }

    private String getApplicationIdFromRequestPath(HttpServletRequest request) {
        String pathWithinServletMapping = getPathWithinServletMapping(request);
        if (pathWithinServletMapping.startsWith("/")) {
            pathWithinServletMapping = pathWithinServletMapping.substring(1);
        }

        int i = pathWithinServletMapping.indexOf('/');
        if (i != -1) {
            pathWithinServletMapping = pathWithinServletMapping.substring(0, i);
        }

        if (pathWithinServletMapping.length() == 0) {
            return null;
        }

        return pathWithinServletMapping;
    }

    private String getPathAfterApplicationId(HttpServletRequest request) {
        String pathWithinServletMapping = getPathWithinServletMapping(request);
        if (pathWithinServletMapping.startsWith("/")) {
            pathWithinServletMapping = pathWithinServletMapping.substring(1);
        }
        int i = pathWithinServletMapping.indexOf('/');
        if (i == -1) {
            return null;
        }
        return pathWithinServletMapping.substring(i);
    }

    protected String generateNewApplicationId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public CommunicationManager createCommunicationManager(Application application) {
        return new CustomCommunicationManager(application);
    }

    private class CustomCommunicationManager extends CommunicationManager {

        public CustomCommunicationManager(Application application) {
            super(application);
        }

        @Override
        public void handleUidlRequest(HttpServletRequest request, HttpServletResponse response, AbstractApplicationServlet applicationServlet,
            Window window) throws IOException, ServletException, InvalidUIDLSecurityKeyException {

            // Terminal is normally set on the first request after starting the application and
            // before serving the kick
            // start page. Because we don't go down that code path for the first request we need to
            // do it here so it
            // happens on the first UIDL request.
            if (window.getTerminal() == null) {
                window.setTerminal(MultipleBrowserWindowsApplicationServlet.this.getApplicationContext(request.getSession()).getBrowser());
            }

            super.handleUidlRequest(request, response, applicationServlet, window);
        }

        @Override
        protected DownloadStream handleURI(Window window, Request request, Response response, final Callback callback) {

            return super.handleURI(window, request, response, new Callback() {

                @Override
                public void criticalNotification(Request request, Response response, String cap, String msg, String details, String outOfSyncURL) throws IOException {
                    callback.criticalNotification(request, response, cap, msg, details, outOfSyncURL);
                }

                @Override
                public String getRequestPathInfo(Request request) {

                    // We override the behavior here to return the part of the request uri after the
                    // application id,
                    // otherwise it would have returned it with the application id and the resource
                    // would not be found.
                    return getPathAfterApplicationId((HttpServletRequest) request.getWrappedRequest());
                }

                @Override
                public InputStream getThemeResourceAsStream(String themeName, String resource) throws IOException {
                    return callback.getThemeResourceAsStream(themeName, resource);
                }
            });
        }
    }

    @Override
    protected String getRequestPathInfo(HttpServletRequest request) {
        return getPathAfterApplicationId(request);
    }

    private static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

    private static final String INCLUDE_REQUEST_URI_ATTRIBUTE = "javax.servlet.include.request_uri";

    private static final String INCLUDE_CONTEXT_PATH_ATTRIBUTE = "javax.servlet.include.context_path";

    private static final String INCLUDE_SERVLET_PATH_ATTRIBUTE = "javax.servlet.include.servlet_path";

    /**
     * Returns "" if the whole path has been matched by the servlet path.
     */
    private String getPathWithinServletMapping(HttpServletRequest request) {
        String pathWithinApplication = getPathWithinApplication(request);
        String servletPath = getServletPath(request);
        if (pathWithinApplication.startsWith(servletPath)) {
            pathWithinApplication = pathWithinApplication.substring(servletPath.length());
        }
        return pathWithinApplication;
    }

    private String getPathWithinApplication(HttpServletRequest request) {
        String pathWithinApplication = getRequestUri(request);
        String contextPath = getContextPath(request);
        if (pathWithinApplication.startsWith(contextPath)) {
            pathWithinApplication = pathWithinApplication.substring(contextPath.length());
        }
        return pathWithinApplication;
    }

    /**
     * Returns the request uri for the request. If the request is an include it will return the uri
     * being included. The returned uri is not decoded.
     */
    private String getRequestUri(HttpServletRequest request) {
        if (request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE) != null) {
            return (String) request.getAttribute(INCLUDE_REQUEST_URI_ATTRIBUTE);
        }
        return cleanRequestUri(decodeUri(request, request.getRequestURI()));
    }

    /**
     * The servlet container is supposed to not include the part of the request uri following a
     * semicolon but some containers (Jetty) leaves it in. This method removes it if it's there.
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
     * Returns the decoded context path or empty if running as the root context. The context path
     * always starts with a slash and never ends with a trailing slash.
     */
    private String getContextPath(HttpServletRequest request) {
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
     * Returns the part of the request uri used to match the servlet being called. The servlet path
     * has already been decoded by the servlet container.
     */
    private String getServletPath(HttpServletRequest request) {
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
