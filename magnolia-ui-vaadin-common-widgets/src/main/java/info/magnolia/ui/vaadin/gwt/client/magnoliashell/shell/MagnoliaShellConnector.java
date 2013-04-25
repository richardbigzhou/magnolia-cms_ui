/**
 * This file Copyright (c) 2010-2013 Magnolia International
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

import info.magnolia.ui.vaadin.gwt.client.dialog.connector.OverlayConnector;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppActivatedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector.ViewportConnector;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget.PreloaderCallback;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.magnoliashell.MagnoliaShell;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.Util;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.AbstractLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.Connector;
import com.vaadin.shared.ui.Connect;

/**
 * MagnoliaShellConnector.
 */
@Connect(MagnoliaShell.class)
public class MagnoliaShellConnector extends AbstractLayoutConnector implements MagnoliaShellView.Presenter {

    private final ShellAppActivatedEvent.Handler navigationHandler = new ShellAppActivatedEvent.Handler() {
        @Override
        public void onShellAppActivated(final ShellAppActivatedEvent event) {
            view.showShellApp(event.getType());
            activateShellApp(Fragment.fromString("shell:" + event.getType().name().toLowerCase() + ":" + event.getToken()));
        }

    };

    private ShellServerRpc rpc = RpcProxy.create(ShellServerRpc.class, this);

    private MagnoliaShellView view;

    private EventBus eventBus = new SimpleEventBus();

    private boolean isHistoryInitialized = false;

    @Override
    protected void init() {
        super.init();
        eventBus.addHandler(ShellAppActivatedEvent.TYPE, navigationHandler);
        addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                MagnoliaShellState state = getState();
                Iterator<Entry<ShellAppType, Integer>> it = state.indications.entrySet().iterator();
                while (it.hasNext()) {
                    final Entry<ShellAppType, Integer> entry = it.next();
                    view.setShellAppIndication(entry.getKey(), entry.getValue());
                }

                if (!isHistoryInitialized) {
                    isHistoryInitialized = true;
                    History.fireCurrentHistoryState();
                }
            }
        });

        addStateChangeHandler("overlays", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {

                MagnoliaShellState state = getState();
                Iterator<Connector> it = state.overlays.iterator();

                // Check for overlays that have not yet been added.
                while (it.hasNext()) {
                    final Connector overlay = it.next();
                    // Test if overlay is attached.
                    Widget overlayWidget = ((ComponentConnector) overlay).getWidget();

                    if (overlayWidget.getParent() == null) {
                        // Add the widget
                        ComponentConnector overlayParent = (ComponentConnector) (((OverlayConnector) overlay).getState().overlayParent);

                        Widget parentWidget = overlayParent.getWidget();
                        view.openOverlayOnWidget(overlayWidget, parentWidget);
                    }
                }

            }
        });

        registerRpc(ShellClientRpc.class, new ShellClientRpc() {

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

        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Fragment newFragment = Fragment.fromString(event.getValue());
                changeAppFromFragment(newFragment);
                view.setActiveViewport(newFragment.isApp());
            }
        });
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        List<ComponentConnector> children = getChildComponents();
        for (ComponentConnector connector : children) {
            if (connector instanceof ViewportConnector) {
                final ViewportConnector vc = (ViewportConnector) connector;
                view.updateViewport(vc.getWidget(), vc.getType());
            }
        }
    }

    @Override
    protected Widget createWidget() {
        this.view = new MagnoliaShellViewImpl(eventBus);
        this.view.setPresenter(this);
        return view.asWidget();
    }

    @Override
    public MagnoliaShellState getState() {
        return (MagnoliaShellState) super.getState();
    }

    @Override
    public void activateApp(Fragment f) {
        rpc.activateApp(f);
    }

    @Override
    public void activateShellApp(Fragment f) {
        rpc.activateShellApp(f);
    }

    @Override
    public void updateViewportLayout(ViewportWidget activeViewport) {
        getLayoutManager().setNeedsMeasure(Util.findConnectorFor(activeViewport));
        getLayoutManager().layoutNow();
    }

    @Override
    public void closeCurrentApp() {
        rpc.stopCurrentApp();
    }

    @Override
    public void closeCurrentShellApp() {
        rpc.stopCurrentShellApp();
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

    public void changeAppFromFragment(final Fragment f) {
        if (f.isShellApp()) {
            eventBus.fireEvent(new ShellAppActivatedEvent(f.resolveShellAppType(), f.getParameter()));
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
        return c != null ? ((ComponentConnector) c).getWidget() : null;
    }

    private void goToAppLauncher(String parameter) {
        eventBus.fireEvent(new ShellAppActivatedEvent(ShellAppType.APPLAUNCHER, parameter));
    }

}
