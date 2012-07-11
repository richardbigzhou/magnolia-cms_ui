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
package info.magnolia.ui.vaadin.integration.widget.client.applauncher;

import org.vaadin.rpc.client.ClientSideHandler;
import org.vaadin.rpc.client.ClientSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side impl of AppLauncher.
 * 
 */
@SuppressWarnings("serial")
public class VAppLaucnher extends Composite implements Paintable, ClientSideHandler, VAppLauncherView.Presenter {

    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {

            register("addGroup", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String caption = String.valueOf(params[0]);
                    final String color = String.valueOf(params[1]);
                    view.addPermanentAppGroup(caption, color);
                }
            });

            register("addMinorGroup", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String caption = String.valueOf(params[0]);
                    final String color = String.valueOf(params[1]);
                    view.addTemporaryAppGroup(caption, color);
                }
            });

            register("addAppThumbnail", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String caption = String.valueOf(params[0]);
                    final String iconStyle = String.valueOf(params[1]);
                    final String categoryId = String.valueOf(params[2]);
                    view.addAppThumbnail(caption, iconStyle, categoryId);
                }
            });
            
            register("setAppActive", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final String appName = String.valueOf(params[0]);
                    final boolean isActive = (Boolean)params[1];
                    view.setAppActive(appName, isActive);
                }
            });
        }
    };
    
    private final VAppLauncherView view;

    private final EventBus eventBus = new SimpleEventBus();
    
    public VAppLaucnher() {
        super();
        this.view = new VAppLauncherViewImpl(eventBus);
        this.view.setPresenter(this);
        initWidget(view.asWidget());
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        proxy.update(this, uidl, client);
    }

    @Override
    public boolean initWidget(Object[] params) {
        return false;
    }

    @Override
    public void handleCallFromServer(String method, Object[] params) {
        GWT.log("Unknown method call from server: " + method);
    }

    @Override
    public void activateApp(String appId) {
        proxy.call("appActivated", appId);
    }

}
