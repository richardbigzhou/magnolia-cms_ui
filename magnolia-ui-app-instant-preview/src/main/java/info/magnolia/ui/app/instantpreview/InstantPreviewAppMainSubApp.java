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
package info.magnolia.ui.app.instantpreview;

import info.magnolia.ui.framework.app.AbstractSubApp;
import info.magnolia.ui.framework.app.SubAppContext;
import info.magnolia.ui.framework.instantpreview.InstantPreviewDispatcher;
import info.magnolia.ui.framework.location.Location;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.view.View;

import javax.inject.Inject;

/**
 * Sub app for the main tab in the instant preview app.
 */
public class InstantPreviewAppMainSubApp extends AbstractSubApp implements InstantPreviewView.Listener {

    private SubAppContext appContext;
    private InstantPreviewView view;
    private InstantPreviewDispatcher dispatcher;

    @Inject
    public InstantPreviewAppMainSubApp(SubAppContext appContext, InstantPreviewView view, InstantPreviewDispatcher dispatcher) {
        super(appContext, view);
        this.appContext = appContext;
        this.view = view;
        this.dispatcher = dispatcher;
        view.setListener(this);
    }

    @Override
    public String getCaption() {
        return "Instant Preview";
    }

    @Override
    public View start(Location location) {
        return view;
    }

    @Override
    public String shareSession() {
        return dispatcher.share();

    }

    @Override
    public void unshareSession(String hostId) {
        dispatcher.unshare(hostId);
    }

    @Override
    public void joinSession(String hostId) {
        dispatcher.subscribeTo(hostId);
    }

    @Override
    public void leaveSession(String hostId) {
        dispatcher.unsubscribeFrom(hostId);
    }

    @Override
    public void sendLocalMessage(String msg, MessageType type) {
        Message message = new Message();
        message.setMessage(msg);
        message.setType(type);
        message.setSubject(getCaption());
        appContext.getAppContext().sendLocalMessage(message);
    }

}
