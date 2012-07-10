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
package info.magnolia.ui.app.messages;

import java.util.Date;
import javax.inject.Inject;

import info.magnolia.ui.framework.app.AppContext;
import info.magnolia.ui.framework.app.SubApp;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.view.View;

/**
 * Sub app for the main tab in the message app.
 */
public class MessagesAppMainSubApp implements SubApp, MessagesView.Listener {

    private AppContext appContext;
    private MessagesView view;

    @Inject
    public MessagesAppMainSubApp(AppContext appContext, MessagesView view) {
        this.appContext = appContext;
        this.view = view;
    }

    @Override
    public String getCaption() {
        return "Messages";
    }

    @Override
    public View start() {
        return view;
    }

    @Override
    public void showConfirmationMessage(String message) {
        appContext.showConfirmationMessage(message);
    }

    @Override
    public void handleLocalMessage(MessageType type, String subject, String message) {
        final Message msg = new Message();
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setType(type);
        msg.setTimestamp(new Date().getTime());
        appContext.sendLocalMessage(msg);
    }

    @Override
    public void handleGlobalMessage(MessageType type, String subject, String message) {
        final Message msg = new Message();
        msg.setSubject(subject);
        msg.setMessage(message);
        msg.setType(type);
        msg.setTimestamp(new Date().getTime());
        appContext.broadcastMessage(msg);
    }
}
