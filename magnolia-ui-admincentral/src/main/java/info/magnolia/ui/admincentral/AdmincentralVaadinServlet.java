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
package info.magnolia.ui.admincentral;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.shared.ApplicationConstants;

/**
 * The AdmincentralVaadinServlet.
 */
public class AdmincentralVaadinServlet extends VaadinServlet {

    private static final Logger log = LoggerFactory.getLogger(AdmincentralVaadinServlet.class);
    /**
     * URL param forcing restart of vaadin application.
     */
    public static final String RESTART_APPLICATION_PARAM = "?restartApplication";

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event) {
                event.getSession().addBootstrapListener(new BootstrapListener() {

                    @Override
                    public void modifyBootstrapPage(BootstrapPageResponse response) {
                        response.getDocument().head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />");
                    }

                    @Override
                    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
                    }
                });
            }
        });
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            super.service(request, response);
        } catch (Exception e) {
            log.error("An internal error has occurred in the VaadinServlet.", e);
            writeServerErrorPage(request, response, e);
        }
    }

    @Override
    protected void criticalNotification(VaadinServletRequest request, HttpServletResponse response, String caption, String message, String details, String url) throws IOException {
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

            StringBuilder output = new StringBuilder();

            output.append("<style>a {color: inherit; text-decoration:none;}" +
                    "html, body {height:100%; margin:0;}" +
                    ".error-message {color:#fff; padding:24px; line-height:1.3; overflow-x:hidden; overflow-y:auto;}" +
                    "h2 {font-size:5em; font-family:DINWebPro, sans-serif; font-weight: normal; margin:0;}" +
                    ".v-button-link {font-size: 2em;} .v-button-link .v-button-caption {text-decoration:none;}" +
                    "#stacktrace {font-family: monospace; display:none; color:#3e5900;}" +
                    ".viewerror {color:#aabf2f;} .v-button-link:hover, .v-button-link:focus {color:#93bac6;}</style>");
            output.append("<div class=\"v-magnolia-shell\" style=\"height:100%;\">" +
                    "<div id=\"main-launcher\"><a href=\"" + url + "\"><img id=\"logo\" src=\"./../VAADIN/themes/admincentraltheme/img/logo-magnolia.svg\"></a></div>" +
                    "<div class=\"error-message v-shell-viewport-slot\">" +
                    "<h2>Whoops!</h2>" +
                    "<p>The server has encountered an internal error, we just need to restart the application.<br/>" +
                    "If you keep experiencing difficulties, please contact your server administrator.<br/></p>" +
                    "<p>We apologize for the inconvenience.</p>" +
                    "<div class=\"v-button v-widget link v-button-link\" tabindex=\"0\" role=\"button\"><a href=\"" + url + "\">[<span class=\"v-button-caption\">Click here to restart the application</span>]</a></div><br/>" +
                    "<div class=\"v-button v-widget link v-button-link viewerror\" tabindex=\"0\" role=\"button\" onclick=\"var st=document.getElementById('stacktrace');st.style.display=(st.style.display=='block')?'none':'block';\">[<span class=\"v-button-caption\">Or view the error's stack trace</span>]</div>");
            output.append(getStackTrace(e));
            output.append("</div></div>");

            if (request.getMethod().equalsIgnoreCase("GET")) {
                // prepend document head if this is the first vaadin request
                output.insert(0, "<html><head><meta charset=\"UTF-8\"/><meta content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" name=\"viewport\"/><title>Magnolia 5.0</title><link rel=\"stylesheet\" type=\"text/css\" href=\"./../VAADIN/themes/admincentral/styles.css\"/></head><body>");
            }

            // make sure vaadin writes this output in the document rather than treat it as a UIDL response
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeResponse(response, "text/html; charset=UTF-8", output.toString());
        }
    }

    private String getStackTrace(Throwable e) {
        final StringBuilder result = new StringBuilder("<p id=\"stacktrace\">");
        result.append(e.toString());

        // add each element of the stack trace
        for (StackTraceElement element : e.getStackTrace()) {
            result.append("<br/>");
            result.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;at ");
            result.append(element);
        }
        result.append("</p>");
        return result.toString();
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

        if (pathInfo.startsWith(prefix)) {
            return true;
        }

        return false;
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
}
