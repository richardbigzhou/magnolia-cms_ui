/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.admincentral;

import info.magnolia.cms.util.ServletUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.communication.ServletBootstrapHandler;
import com.vaadin.shared.ApplicationConstants;

/**
 * The AdmincentralVaadinServlet.
 */
public class AdmincentralVaadinServlet extends VaadinServlet {

    private static final Logger log = LoggerFactory.getLogger(AdmincentralVaadinServlet.class);

    private static final String ERROR_PAGE_STYLE = "<style>a {color: inherit; text-decoration:none;}" +
            "html, body {height:100%; margin:0;}" +
            ".error-message {color:#fff; font-family: Verdana, sans-serif; padding:24px; line-height:1.3; overflow-x:hidden; overflow-y:auto;}" +
            "h2 {font-size:5em; font-family:DINWebPro, sans-serif; font-weight: normal; margin:0;}" +
            ".v-button-link {font-size: 2em;} .v-button-link .v-button-caption {text-decoration:none;}" +
            "#stacktrace {font-family: monospace; display:none; color:#3e5900;}" +
            ".viewerror {color:#aabf2f;} .v-button-link:hover, .v-button-link:focus {color:#93bac6;}</style>";

    /**
     * URL param forcing restart of vaadin application.
     */
    public static final String RESTART_APPLICATION_PARAM = "?restartApplication";

    private UIProvider admincentralUiProvider;

    @Inject
    public AdmincentralVaadinServlet(UIProvider admincentralUiProvider) {
        this.admincentralUiProvider = admincentralUiProvider;
    }

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event) {
                event.getSession().addBootstrapListener(new BootstrapListener() {

                    @Override
                    public void modifyBootstrapPage(BootstrapPageResponse response) {
                        Element ieMode = response.getDocument().head().getElementsByAttributeValue("http-equiv", "X-UA-Compatible").first();
                        if (ieMode != null) {
                            ieMode.attr("content", "IE=9;chrome=1");
                        }
                        response.getDocument().head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />");
                    }

                    @Override
                    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
                    }
                });

                // Set up and configure UIProvider for the admincentral
                if (admincentralUiProvider != null) {
                    event.getSession().addUIProvider(admincentralUiProvider);
                } else {
                    log.error("Could not inject AdmincentralUIProvider.");
                }
            }
        });
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestURI = ServletUtil.stripPathParameters(request.getRequestURI());
            if (requestURI != null && requestURI.endsWith("undefined.cache.js")) {
                writeUnsupportedBrowserPage(request, response);
            } else {
                super.service(request, response);
            }
        } catch (Exception e) {
            log.error("An internal error has occurred in the VaadinServlet.", e);
            writeServerErrorPage(request, response, e);
        }
    }

    @Override
    protected void criticalNotification(VaadinServletRequest request, VaadinServletResponse response, String caption, String message, String details, String url) throws IOException {
        // invoking critical notifications only for UIDL requests, otherwise we let it fall back to writing the error page.
        if (isUidlRequest(request)) {
            super.criticalNotification(request, response, caption, message, details, url);
        }
    }

    private void writeServerErrorPage(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        if (!isUidlRequest(request)) {
            // Create an HTML response with the error

            // compute restart application URL at previous location
            String url = request.getRequestURL().toString() + RESTART_APPLICATION_PARAM;
            String fragment = request.getParameter("v-loc");
            if (fragment != null && fragment.indexOf("#") != -1) {
                url += fragment.substring(fragment.indexOf("#"));
            }

            final StringBuilder output = new StringBuilder();
            output.append(ERROR_PAGE_STYLE)
                    .append("<div class=\"v-magnolia-shell\" style=\"height:100%;\">")
                    .append("<div id=\"main-launcher\"><a href=\"" + url + "\"><img id=\"logo\" src=\"./../VAADIN/themes/admincentraltheme/img/logo-magnolia.svg\" /></a></div>")
                    .append("<div class=\"error-message v-shell-viewport-slot\">")

                    .append("<h2>Whoops!</h2>")
                    .append("<p>The server has encountered an internal error.</p>")

                    .append("<div class=\"v-button v-widget link v-button-link\" tabindex=\"0\" role=\"button\">")
                    .append("<a href=\"" + url + "\">[<span class=\"v-button-caption\">Click here to attempt to recover from this</span>]</a></div>")

                    .append("<p>We apologize for any inconvenience caused.</p>")
                    .append("<p>If you keep experiencing difficulties, please contact your system administrator.</p>")
                    .append("<p>Please check your log files for the complete stack trace.</p>");

            output.append("</div></div>");

            // prepend document head if this is the first vaadin request
            output.insert(0, "<html><head><meta charset=\"UTF-8\"/><meta content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" name=\"viewport\"/><title>Magnolia 5</title><link rel=\"stylesheet\" type=\"text/css\" href=\"./../VAADIN/themes/admincentral/styles.css\"/></head><body>");
            output.append("</body></html>");

            // make sure vaadin writes this output in the document rather than treat it as a UIDL response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeResponse(response, "text/html; charset=UTF-8", output.toString());
        }
    }

    private void writeUnsupportedBrowserPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // build message
        final StringBuilder output = new StringBuilder();
        output.append(ERROR_PAGE_STYLE)
                .append("<div class=\"v-magnolia-shell\" style=\"height:100%;\">")
                .append("<div id=\"main-launcher\"><a href=\"#\"><img id=\"logo\" src=\"./../VAADIN/themes/admincentraltheme/img/logo-magnolia.svg\"></a></div>")
                .append("<div class=\"error-message v-shell-viewport-slot\">")
                .append("<h2>Sorry.</h2>")
                .append("<p>You're trying to use Magnolia 5 on a browser we currently do not support.</p>")
                .append("<p>Please log in using either Firefox, Chrome, Safari or IE8+.<br />")
                .append("We apologize for any inconvenience caused.</p>")
                .append("</div></div>");

        // wrap as JS response
        output.replace(0, output.length(), output.toString().replaceAll("\\\"", "\\\\\\\""));
        output.insert(0, "document.body.innerHTML = \"");
        output.append("\";");

        writeResponse(response, "text/javascript; charset=UTF-8", output.toString());
    }

    /**
     * same as {@link com.vaadin.server.ServletPortletHelper#isUIDLRequest}.
     */
    private boolean isUidlRequest(HttpServletRequest request) {
        String prefix = ApplicationConstants.UIDL_PATH + '/';
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            return false;
        }

        if (!prefix.startsWith("/")) {
            prefix = '/' + prefix;
        }

        return pathInfo.startsWith(prefix);
    }

    /**
     * same as {@link VaadinServlet#writeResponse}.
     */
    private void writeResponse(HttpServletResponse response,
            String contentType, String output) throws IOException {
        response.setContentType(contentType);
        final ServletOutputStream out = response.getOutputStream();
        // Set the response type
        final PrintWriter outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
        outWriter.print(output);
        outWriter.flush();
        outWriter.close();
        out.flush();
    }

    @Override
    protected VaadinServletService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException {
        VaadinServletService service = new VaadinServletService(this, deploymentConfiguration) {

            @Override
            protected List<RequestHandler> createRequestHandlers() throws ServiceException {
                List<RequestHandler> handlers = super.createRequestHandlers();
                for (int i = 0; i < handlers.size(); i++) {
                    RequestHandler handler = handlers.get(i);
                    if (handler instanceof ServletBootstrapHandler) {
                        handlers.set(i, new ServletBootstrapHandler() {

                            @Override
                            protected String getServiceUrl(BootstrapContext context) {

                                // We replace the default ServletBootstrapHandler with our own so that we can specify
                                // the serviceUrl explicitly. It's otherwise left empty making the client determine it
                                // using location.href. The client does not play well with this and appends paths after
                                // the JSESSIONID instead of in front of it. This results in a problem loading resources
                                // specified using @JavaScript and @StyleSheet.
                                //
                                // see com.vaadin.client.ApplicationConfiguration.loadFromDOM()
                                // see MGNLUI-2291
                                // see http://dev.vaadin.com/ticket/10974

                                return ServletUtil.getOriginalRequestURI(((VaadinServletRequest)context.getRequest()).getHttpServletRequest());
                            }
                        });
                        break;
                    }
                }
                return handlers;
            }
        };
        service.init();
        return service;
    }
}
