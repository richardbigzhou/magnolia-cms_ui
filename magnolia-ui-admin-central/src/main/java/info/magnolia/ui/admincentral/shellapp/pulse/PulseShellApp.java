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

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.framework.app.ShellApp;
import info.magnolia.ui.framework.app.ShellAppContext;
import info.magnolia.ui.framework.app.ShellView;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Activity for pulse.
 *
 * @version $Id$
 */
public class PulseShellApp implements ShellApp, PulseView.Presenter {
    
    private PulseView pulseView;

    private MessagesManager messagesManager;
    private MagnoliaShell shell;
    private ShellAppContext context;

    @Inject
    public PulseShellApp(PulseView pulseView, MessagesManager messagesManager, MagnoliaShell shell) {
        this.pulseView = pulseView;
        this.messagesManager = messagesManager;
        this.shell = shell;
    }

    @Override
    public ShellView start(ShellAppContext context) {
        this.context = context;
        pulseView.setPresenter(this);

        for (Message message : messagesManager.getMessagesForUser(MgnlContext.getUser().getName())) {
            pulseView.addMessage(message);
        }

        // TODO -tobias- Calling this here results in an NPE because MagnoliaShell doesn't have an Application instance to synchronize on yet
//        shell.updateShellAppIndication(VMainLauncher.ShellAppType.PULSE, messagesManager.getNumberOfUnclearedMessagesForUser(MgnlContext.getUser().getName()));

        messagesManager.registerMessagesListener(MgnlContext.getUser().getName(), new MessagesManager.MessageListener() {

            @Override
            public void messageSent(Message message) {
                pulseView.addMessage(message);
                shell.updateShellAppIndication(VMainLauncher.ShellAppType.PULSE, 1);
            }

            @Override
            public void messageCleared(Message message) {
                pulseView.updateMessage(message);
                shell.updateShellAppIndication(VMainLauncher.ShellAppType.PULSE, -1);
            }
        });

        return pulseView;
    }

    @Override
    public void locationChanged(Location location) {
        DefaultLocation pulsePlace = (DefaultLocation) location;
        List<String> pathParams = parsePathParamsFromToken(pulsePlace.getToken());
        if (pathParams.size() > 0) {
            final String tabName = pathParams.remove(0);
            final String displayedTabId = pulseView.setCurrentPulseTab(tabName, pathParams);
        }
//        pulsePlace.setCurrentPulseTab(displayedTabId);
    }

    private List<String> parsePathParamsFromToken(String token) {
        final List<String> result = new ArrayList<String>();
        result.add(token);
        return result;
    }

    @Override
    public void onPulseTabChanged(String tabId) {
        context.setAppLocation(new DefaultLocation("shell", "pulse", tabId));
    }
}
