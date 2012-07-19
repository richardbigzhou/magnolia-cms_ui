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
import com.google.gwt.user.client.History;
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
public class VAppLauncher extends Composite implements Paintable, ClientSideHandler, VAppLauncherView.Presenter {

    private ClientSideProxy proxy = new ClientSideProxy(this) {
        {

            register("addSection", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final VAppSectionJSO section = VAppSectionJSO.parse(String.valueOf(params[0]));
                    view.addAppSection(section);
                }
            });

            register("addAppTile", new Method() {
                @Override
                public void invoke(String methodName, Object[] params) {
                    final VAppTileJSO appTile = VAppTileJSO.parse(String.valueOf(params[0]));
                    final String categoryId = String.valueOf(params[1]);
                    view.addAppThumbnail(appTile, categoryId);
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

    private final EventBus internalEventBus = new SimpleEventBus();
    
    public VAppLauncher() {
        super();
        this.view = new VAppLauncherViewImpl(internalEventBus);
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
        History.newItem("app:" + appId, true);
    }

}
