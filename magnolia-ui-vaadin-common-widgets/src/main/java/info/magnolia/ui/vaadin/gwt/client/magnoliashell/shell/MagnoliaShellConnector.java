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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppNavigationEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.VShellMessage.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector.ViewportConnector;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget.PreloaderCallback;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;
import info.magnolia.ui.vaadin.magnoliashell.BaseMagnoliaShell;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.Connector;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.shared.ui.Connect;


/**
 * MagnoliaShellConnector.
 */
@Connect(BaseMagnoliaShell.class)
public class MagnoliaShellConnector extends AbstractLayoutConnector implements MagnoliaShellView.Presenter {

    private ShellServerRpc rpc = RpcProxy.create(ShellServerRpc.class, this);

    private MagnoliaShellView view;

    private EventBus eventBus = new SimpleEventBus();

    @Override
    protected void init() {
        super.init();
        addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                MagnoliaShellState state = getState();
                Iterator<Entry<ShellAppType, Integer>> it = getState().indications.entrySet().iterator();
                while (it.hasNext()) {
                    final Entry<ShellAppType, Integer> entry = it.next();
                    view.setShellAppIndication(entry.getKey(), entry.getValue());
                }
                final Connector active = state.activeViewport;
                if (active != null) {
                    view.setActiveViewport(((ViewportConnector)active).getWidget());    
                }
                
            }
        });

        registerRpc(ShellClientRpc.class, new ShellClientRpc() {

            @Override
            public void navigate(String appId, String subAppId, String param) {
                view.navigate(appId, subAppId, param);
            }

            @Override
            public void activeViewportChanged(Connector viewport) {
                view.setActiveViewport(((ViewportConnector)viewport).getWidget());
            }

            @Override
            public void showMessage(String type, String topic, String msg, String id) {
                view.showMessage(MessageType.valueOf(type), topic, msg, id);
            }

            @Override
            public void hideAllMessages() {
                view.hideAllMessages();
            }
        });
        
        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() { 
            @Override
            public void onElementResize(ElementResizeEvent e) {
                view.updateShellDivet();
            }
        });
    }
    
    @Override
    public void updateCaption(ComponentConnector connector) {}

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        List<ComponentConnector> children = getChildComponents();
        for (ComponentConnector connector : children) {
            final ViewportConnector vc = (ViewportConnector)connector;
            view.updateViewport(vc.getWidget(), vc.getType());
        }
    }

    @Override
    protected Widget createWidget() {
        this.view = new MagnoliaShellViewImpl(eventBus);
        this.view.setPresenter(this);
        return view.asWidget();
    }

    @Override
    protected SharedState createState() {
        return super.createState();
    }

    @Override
    public MagnoliaShellState getState() {
        return (MagnoliaShellState) super.getState();
    }
    
    @Override
    public void activateApp(Fragment fragment) {
        rpc.activateRunningApp(fragment);
    }

    @Override
    public void activateShellApp(Fragment f) {
        rpc.activateShellApp(f);
    }

    @Override
    public void updateViewportLayout(ViewportWidget activeViewport) {
        // client.runDescendentsLayout(viewport);
    }

    @Override
    public void closeCurrentApp() {
        rpc.closeCurrentApp();
    }

    @Override
    public void closeCurrentShellApp() {
        rpc.closeCurrentShellApp();
    }

    @Override
    public void removeMessage(String id) {
        rpc.removeMessage(id);
    }

    @Override
    public boolean isAppRegistered(String appName) {
        return getState().registeredAppNames.contains(appName);
    }

    @Override
    public boolean isAppRunning(String appName) {
        return getState().runningAppNames.contains(appName);
    }

    @Override
    public void handleHistoryChange(String fragmentStr) {
        final Fragment f = Fragment.fromFragment(fragmentStr);
        if (f.getAppViewportType() == ViewportType.SHELL_APP) {
            eventBus.fireEvent(new ShellAppNavigationEvent(ShellAppType.resolveType(f.getAppId()), f.getParameter()));
        } else {
            final String appId = f.getAppId();
            if (isAppRegistered(appId)) {
                if (!isAppRunning(appId)) {
                    view.showAppPreloader(appId, new PreloaderCallback() {
                        @Override
                        public void onPreloaderShown(String appName) {
                            activateApp(f);
                        }
                    });
                } else {
                    activateApp(f);
                }
            } else {
                goToAppLauncher(f.getParameter());
            }
        }
    }

    @Override
    public Widget getShellAppWidget(ShellAppType type) {
        final Connector c = getState().shellApps.get(type);
        return c != null ? ((ComponentConnector)c).getWidget() : null; 
    }
    
    private void goToAppLauncher(String parameter) {
        eventBus.fireEvent(new ShellAppNavigationEvent(ShellAppType.APPLAUNCHER, parameter));
    }
}
