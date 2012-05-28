/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.magnoliashell;

import info.magnolia.ui.widget.magnoliashell.demo.WidgetTestApplication;

import java.io.File;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.vaadin.terminal.gwt.server.ApplicationServlet;

/**
 * Simple test server for the GWT hosted mode development. 
 * Saves a lot of time against m2e wtp-integration.  
 * 
 * @author apchelintcev
 *
 */
public class TestServer {

    private static final int PORT = 9998;

    public static void main(String[] args) throws Exception {
        Server server = new Server();

        final Connector connector = new SelectChannelConnector();

        connector.setPort(PORT);
        server.setConnectors(new Connector[] { connector });

        final WebAppContext context = new WebAppContext();
        final ServletHolder servletHolder = new ServletHolder(ApplicationServlet.class);
        servletHolder.setInitParameter("widgetset", "info.magnolia.ui.widget.magnoliashell.demo.ShellTestAppWidgetSet");
        servletHolder.setInitParameter("application", WidgetTestApplication.class.getName());

        File file = new File("./target/testwebapp");
        context.setWar(file.getPath());
        context.setContextPath("/");

        context.addServlet(servletHolder, "/*");
        server.setHandler(context);

        server.start();
        server.join();
    }

}
