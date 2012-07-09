/**
 * This file Copyright (c) 2010-2011 Magnolia International
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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.icepush.servlet.MainServlet;
import org.vaadin.artur.icepush.ICEPush;
import org.vaadin.artur.icepush.JavascriptProvider;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

/**
 * Modified version of the {@link org.vaadin.artur.icepush.ICEPushServlet}.
 */
@SuppressWarnings("serial")
public class MagnoliaIcePushServlet extends ApplicationServlet {

    private MainServlet ICEPushServlet;
    
    private JavascriptProvider javascriptProvider;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            super.init(servletConfig);
        } catch (ServletException e) {
            if (e.getMessage().equals("Application not specified in servlet parameters")) {
                // Ignore if application is not specified to allow the same
                // servlet to be used for only push in portals
            } else {
                throw e;
            }
        }

        this.ICEPushServlet = new MainServlet(servletConfig.getServletContext());

        try {
            String codeBasePath = servletConfig.getServletContext().getContextPath();
            String codeLocation = servletConfig.getInitParameter("codeBasePath");
            if (codeLocation != null)
                codeBasePath += codeLocation;
            if (codeBasePath.endsWith("/"))
                codeBasePath = codeBasePath.substring(0, codeBasePath.length() - 2);
            this.javascriptProvider = new JavascriptProvider(codeBasePath);
            ICEPush.setCodeJavascriptLocation(this.javascriptProvider.getCodeLocation());
        } catch (IOException e) {
            throw new ServletException("Error initializing JavascriptProvider", e);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.equals("/" + this.javascriptProvider.getCodeName())) {
            // Serve icepush.js
            serveIcePushCode(response);
            return;
        }

        if (request.getRequestURI().endsWith(".icepush")) {
            // Push request
            try {
                this.ICEPushServlet.service(request, response);
            } catch (ServletException e) {
                throw e;
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            // Vaadin request
            super.service(request, response);
        }
    }

    private void serveIcePushCode(HttpServletResponse response) throws IOException {
        String icepushJavscript = this.javascriptProvider.getJavaScript();
        response.setHeader("Content-Type", "text/javascript");
        response.getOutputStream().write(icepushJavscript.getBytes());
    }

    @Override
    public void destroy() {
        super.destroy();
        this.ICEPushServlet.shutdown();
    }
}
