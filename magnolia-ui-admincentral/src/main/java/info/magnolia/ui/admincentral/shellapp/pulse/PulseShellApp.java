/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import info.magnolia.ui.admincentral.shellapp.ShellApp;
import info.magnolia.ui.admincentral.shellapp.ShellAppContext;
import info.magnolia.ui.admincentral.shellapp.pulse.message.MessagePresenter;
import info.magnolia.ui.admincentral.shellapp.pulse.message.PulseMessagesPresenter;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.framework.shell.ShellImpl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pulse shell app.
 */
public final class PulseShellApp implements ShellApp, PulseMessagesPresenter.Listener, MessagePresenter.Listener {

    private static final Logger log = LoggerFactory.getLogger(PulseShellApp.class);

    private PulseView pulseView;
    private PulseMessagesPresenter messages;
    private MessagePresenter messagePresenter;
    private ShellImpl shell;

    private enum PulseViewType {
        LIST, DETAIL
    }

    /*
     * we keep the current message view type as we need it to know in order to refresh the list (i.e. call showList()) or not. We don't want to refresh the list of messages when we're showing a message detail
     * but we need to do it when e.g. the list view is the current one and a new message arrives. Not doing so would display the new message not sorted correctly and with title and text displayed as null.
     */
    private PulseViewType currentViewType = PulseViewType.LIST;

    @Inject
    public PulseShellApp(PulseView pulseView, PulseMessagesPresenter messages, MessagePresenter messagePresenter, ShellImpl shell) {
        this.pulseView = pulseView;
        this.messages = messages;
        this.messagePresenter = messagePresenter;
        this.shell = shell;
        messages.setListener(this);
        messagePresenter.setListener(this);
    }

    @Override
    public View start(ShellAppContext context) {
        pulseView.setPulseView(messages.start());
        return pulseView;
    }

    @Override
    public void locationChanged(Location location) {
        if ("pulse".equals(location.getAppName()) && location.getParameter().contains("messages")) {
            String[] params = location.getParameter().split("/");
            if (params.length == 2) {
                String messageId = params[1];
                openMessage(messageId);
            } else {
                log.warn("Got a request to open a message detail but found no message id in the location parameters. Location was [{}]", location);
            }
        } else {
            shell.hideAllMessages();
            if (currentViewType == PulseViewType.LIST) {
                showList();
            }
        }
    }

    @Override
    public void openMessage(String messageId) {
        pulseView.setPulseView(messagePresenter.start(messageId));
        currentViewType = PulseViewType.DETAIL;
    }

    @Override
    public void showList() {
        pulseView.setPulseView(messages.start());
        currentViewType = PulseViewType.LIST;
    }
}
