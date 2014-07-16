/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.framework.app.stub;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.api.message.MessageType;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.framework.app.BaseApp;
import info.magnolia.ui.framework.app.DefaultAppView;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * FailedToStartAppStub.
 * TODO: Add proper JavaDoc.
 */
public class FailedToStartAppStub extends BaseApp {

    private Exception relatedException;

    @Inject
    public FailedToStartAppStub(final AppContext appContext, Exception relatedException, @Named(AdmincentralEventBus.NAME) EventBus adminCentralEventBus, final AppController appController, final Shell shell) {
        super(appContext, new DefaultAppView());
        this.relatedException = relatedException;
        adminCentralEventBus.addHandler(LocationChangedEvent.class, new LocationChangedEvent.Handler() {
            @Override
            public void onLocationChanged(LocationChangedEvent event) {
                //appController.stopApp(appContext.getName());
                //((ShellImpl)shell).getMagnoliaShell().hideAllMessages();
            }
        });
    }

    @Override
    public void start(Location location) {
        String appName = appContext.getName();
        String message = String.format("%s app failed to start", appName);

        Message error = new Message(MessageType.ERROR, message, relatedException.getMessage());
        ((DefaultAppView)getView()).asVaadinComponent().addComponent(new StubView("icon-warning").asVaadinComponent());

        appContext.sendLocalMessage(error);
    }
}

