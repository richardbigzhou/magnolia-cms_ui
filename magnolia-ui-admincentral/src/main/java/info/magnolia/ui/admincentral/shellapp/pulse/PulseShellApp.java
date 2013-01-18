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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.framework.app.ShellApp;
import info.magnolia.ui.framework.app.ShellAppContext;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Pulse shell app.
 */
public class PulseShellApp implements ShellApp, PulseView.Presenter {

    private PulseView pulseView;

    private ShellAppContext context;
    private MagnoliaShell shell;

    @Inject
    public PulseShellApp(PulseView pulseView, MagnoliaShell shell) {
        this.pulseView = pulseView;
        this.shell = shell;
        shell.registerShellApp(ShellAppType.PULSE, pulseView.asVaadinComponent());
    }

    @Override
    public View start(ShellAppContext context) {
        this.context = context;
        pulseView.setPresenter(this);

        // Causes a Guice error - probably the messagesManager is not ready yet or something.
        // shell.setIndicationOnPulseToNumberOfUnclearedMessagesForCurrentUser();

        return pulseView;
    }

    @Override
    public void locationChanged(Location location) {
        List<String> pathParams = parsePathParamsFromToken(location.getParameter());
        if (pathParams.size() > 0) {
            final String tabName = pathParams.remove(0);
            pulseView.setCurrentPulseTab(tabName, pathParams);
        }
        shell.hideAllMessages();
    }

    private List<String> parsePathParamsFromToken(String token) {
        return new ArrayList<String>(Arrays.asList(token.split("/")));
    }

    @Override
    public void onPulseTabChanged(String tabId) {
        context.setAppLocation(new DefaultLocation(Location.LOCATION_TYPE_SHELL_APP, "pulse", "", tabId));
    }
}
