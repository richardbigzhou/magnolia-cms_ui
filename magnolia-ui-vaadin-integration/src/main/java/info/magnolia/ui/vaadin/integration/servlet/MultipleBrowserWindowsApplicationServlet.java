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
import java.net.MalformedURLException;
import java.net.URL;
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
import com.vaadin.terminal.gwt.server.AbstractCommunicationManager;
import com.vaadin.terminal.gwt.server.CommunicationManager;
import com.vaadin.ui.Window;

/**
 * Vaadin ApplicationServlet that supports running a unique Application per browser window. The applications are
 * identified in the URIs used by UIDL requests and file downloads. The browser keeps track of which application is
 * being used for the browser window (or tab) using the document.name property.
 */
public class MultipleBrowserWindowsApplicationServlet extends MagnoliaIcePushServlet {

    private static final String ATTRIBUTE_APPLICATION_ID = MultipleBrowserWindowsApplicationServlet.class.getName() + ".applicationId";
    private static final String ATTRIBUTE_FORCE_APPLICATION_ID = MultipleBrowserWindowsApplicationServlet.class.getName() + ".forceApplicationId";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String caption;
    private String theme;

    // Implementations of Window and Application that are used only to provide the kick start page with the details it
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

        // For normal requests (not ajax, not file upload, etc) we serve the browser a customized kick start page. If
        // there's an application id in the uri then we let it pass through because it's probably a request for
        // downloading an application resource.
        if (getRequestType(request) == RequestType.OTHER && getApplicationIdFromRequestPath(request) == null) {

            // Generate an application id that the client may or may not decide to use
            String applicationId = generateNewApplicationId();

            logger.debug("Suggesting application id: " + applicationId + " for request to " + request.getRequestURI());

            // If the 'restartApplication' parameter is present we force the client to start using this application id
            boolean forceApplicationId = request.getParameter("restartApplication") != null;

            writeCustomAjaxPage(request, response, applicationId, forceApplicationId);

            return;
        }

        logger.debug("Serving request for " + request.getRequestURI());

        super.service(request, response);
    }

    private void writeCustomAjaxPage(HttpServletRequest request, HttpServletResponse response, String applicationId, boolean forceApplicationId) throws IOException, ServletException {

        // Set the arguments as request attributes so they can be found later on when writing the custom script
        request.setAttribute(ATTRIBUTE_APPLICATION_ID, applicationId);
        request.setAttribute(ATTRIBUTE_FORCE_APPLICATION_ID, forceApplicationId);

        writeAjaxPage(request, response, kickStartWindow, kickStartApplication);
    }

    @Override
    protected void writeAjaxPageHtmlVaadinScripts(Window window, String themeName, Application application, BufferedWriter page, String appUrl, String themeUri, String appId, HttpServletRequest request) throws ServletException, IOException {
        super.writeAjaxPageHtmlVaadinScripts(window, themeName, application, page, appUrl, themeUri, appId, request);

        String applicationId = (String) request.getAttribute(ATTRIBUTE_APPLICATION_ID);
        boolean forceApplicationId = (Boolean) request.getAttribute(ATTRIBUTE_FORCE_APPLICATION_ID);

        page.write("<script type=\"text/javascript\">\n");
        page.write("//<![CDATA[\n");

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
    protected URL getApplicationUrl(HttpServletRequest request) throws MalformedURLException {

        String path = RequestPathUtil.getContextPath(request) + RequestPathUtil.getServletPath(request);
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
        String pathWithinServletMapping = RequestPathUtil.getPathWithinServletMapping(request);
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

    private String getPathAfterApplicationId(AbstractCommunicationManager.Request request) {
        String pathWithinServletMapping = RequestPathUtil.getPathWithinServletMapping((HttpServletRequest) request.getWrappedRequest());
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
        public void handleUidlRequest(HttpServletRequest request, HttpServletResponse response, AbstractApplicationServlet applicationServlet, Window window) throws IOException, ServletException, InvalidUIDLSecurityKeyException {

            // Terminal is normally set on the first request after starting the application and before serving the kick
            // start page. Because we don't go down that code path for the first request we need to do it here so it
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

                    // We override the behavior here to return the part of the request uri after the application id,
                    // otherwise it would have returned it with the application id and the resource would not be found.
                    return getPathAfterApplicationId(request);
                }

                @Override
                public InputStream getThemeResourceAsStream(String themeName, String resource) throws IOException {
                    return callback.getThemeResourceAsStream(themeName, resource);
                }
            });
        }
    }
}
