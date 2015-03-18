/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.AdmincentralModule;
import info.magnolia.ui.admincentral.shellapp.ShellApp;
import info.magnolia.ui.admincentral.shellapp.ShellAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pulse shell app.
 */
public final class PulseShellApp implements ShellApp {

    private static final Logger log = LoggerFactory.getLogger(PulseShellApp.class);

    private PulsePresenter presenter;

    private AdmincentralModule admincentralModule;

    private ComponentProvider componentProvider;

    @Inject
    public PulseShellApp(AdmincentralModule admincentralModule, ComponentProvider componentProvider) {
        this.admincentralModule = admincentralModule;
        this.componentProvider = componentProvider;
    }

    @Override
    public View start(ShellAppContext context) {
        this.presenter = componentProvider.newInstance(PulsePresenter.class, admincentralModule.getPulse());
        return presenter.start();
    }

    @Override
    public void locationChanged(Location location) {
        displayView(location);
    }

    private void displayView(Location location) {
        // this bit is used to open a message detail directly when clicking on the error band link
        if ("pulse".equals(location.getAppName()) && location.getParameter().contains("messages")) {
            String[] params = location.getParameter().split("/");
            if (params.length == 2) {
                String messageId = params[1];
                presenter.openItem("messages", messageId);
            } else {
                log.warn("Got a request to open a message detail but found no message id in the location parameters. Location was [{}]", location);
            }
        } else {
            // bit of a hack, since opening a message above will cause another location change we need to avoid displaying the list view again
            if (!presenter.isDisplayingDetailView()) {
                presenter.showList();
            }
        }
    }

}
