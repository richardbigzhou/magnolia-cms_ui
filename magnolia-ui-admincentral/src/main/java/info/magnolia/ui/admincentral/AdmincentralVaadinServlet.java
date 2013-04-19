/**
 * This file Copyright (c) 2013 Magnolia International
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

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.JsonPaintTarget;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.shared.ApplicationConstants;

/**
 * The AdmincentralVaadinServlet.
 */
public class AdmincentralVaadinServlet extends VaadinServlet {

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
    protected void criticalNotification(VaadinServletRequest request, HttpServletResponse response, String caption, String message, String details, String url) throws IOException {

        if (isUidlRequest(request)) {
            // override default error UIDL response with the RPC call to show an error message instead.

            if (caption != null) {
                caption = "\"" + JsonPaintTarget.escapeJSON(caption) + "\"";
            }
            if (details != null) {
                if (message == null) {
                    message = details;
                } else {
                    message += "<br/><br/>" + details;
                }
            }
            if (message != null) {
                message = "\"" + JsonPaintTarget.escapeJSON(message) + "\"";
            }
            if (url != null) {
                url = "\"" + JsonPaintTarget.escapeJSON(url) + "\"";
            }

            String outrpc = "for(;;);[{\"changes\":[], \"rpc\" : ["
                    + "[\"1\",\"info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc\",\"showMessage\","
                    + "[\"ERROR\"," + caption + "," + message + ",null]]]}]";
            writeResponse(response, "application/json; charset=UTF-8", outrpc);

        } else {
            super.criticalNotification(request, response, caption, message, details, url);
        }
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
