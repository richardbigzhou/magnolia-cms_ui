/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ActivateAppEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.AppRequestedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.CurrentAppCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.FullScreenEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.HideShellAppsEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.HideShellAppsRequestedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppRequestedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppStartedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppStartingEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppsHiddenEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellClientRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shell.rpc.ShellServerRpc;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.shellmessage.ShellMessageWidget.MessageType;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector.ViewportConnector;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;
import info.magnolia.ui.vaadin.magnoliashell.MagnoliaShell;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.core.client.Scheduler;
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
import com.vaadin.client.ui.nativebutton.NativeButtonConnector;
import com.vaadin.shared.ui.Connect;

/**
 * MagnoliaShellConnector.
 */
@Connect(MagnoliaShell.class)
public class MagnoliaShellConnector extends AbstractLayoutConnector implements MagnoliaShellView.Presenter {

    private ShellServerRpc rpc = RpcProxy.create(ShellServerRpc.class, this);
    private MagnoliaShellView view;
    private EventBus eventBus = new SimpleEventBus();
    private Fragment lastHandledFragment;
    private boolean isHistoryInitialized = false;

    @Override
    protected void init() {
        super.init();
        addStateChangeHandler(new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                MagnoliaShellState state = getState();
                Iterator<Entry<ShellAppType, Integer>> it = state.indications.entrySet().iterator();
                while (it.hasNext()) {
                    final Entry<ShellAppType, Integer> entry = it.next();
                    view.setShellAppIndication(entry.getKey(), entry.getValue());
                }
            }
        });

        addStateChangeHandler("uriFragment", new StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent stateChangeEvent) {
                Fragment f = getState().uriFragment;
                if (f != null) {
                    History.newItem(f.toFragment(), !f.isSameApp(lastHandledFragment));
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

            @Override
            public void setFullScreen(boolean isFullScreen) {
                MagnoliaShellConnector.this.doFullScreen(isFullScreen);
            }
        });

        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                view.updateShellDivet();
            }
        });

        eventBus.addHandler(CurrentAppCloseEvent.TYPE, new CurrentAppCloseEvent.Handler() {
            @Override
            public void onViewportClose(CurrentAppCloseEvent event) {
                closeCurrentApp();
            }
        });

        eventBus.addHandler(ShellAppStartingEvent.TYPE, new ShellAppStartingEvent.Handler() {
            @Override
            public void onShellAppStarting(ShellAppStartingEvent event) {
                view.onShellAppStarting(event.getType());
            }
        });

        eventBus.addHandler(ShellAppStartedEvent.TYPE, new ShellAppStartedEvent.Handler() {
            @Override
            public void onShellAppStarted(ShellAppStartedEvent event) {
                lastHandledFragment = Fragment.fromString("shell:" + event.getType().name().toLowerCase() + ":" + "");
                activateShellApp(lastHandledFragment);
            }
        });

        eventBus.addHandler(HideShellAppsRequestedEvent.TYPE, new HideShellAppsRequestedEvent.Handler() {
            @Override
            public void onHideShellAppsRequest(HideShellAppsRequestedEvent event) {
                onHideShellAppsRequested();
            }
        });

        eventBus.addHandler(ShellAppsHiddenEvent.TYPE, new ShellAppsHiddenEvent.Handler() {
            @Override
            public void onShellAppsHidden(ShellAppsHiddenEvent event) {
                rpc.stopCurrentShellApp();
            }
        });

        eventBus.addHandler(ActivateAppEvent.TYPE, new ActivateAppEvent.Handler() {
            @Override
            public void onActivateApp(ActivateAppEvent event) {
                rpc.activateApp(Fragment.fromString("app:" + event.getName()));
            }
        });

        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Fragment newFragment = Fragment.fromString(event.getValue());
                if (newFragment.isShellApp()) {
                    showShellApp(newFragment.resolveShellAppType());
                } else {
                    loadApp(newFragment.getAppName());
                    rpc.activateApp(newFragment);
                }
                lastHandledFragment = newFragment;
            }
        });
    }

    @Override
    public void activateShellApp(Fragment f) {
        rpc.activateShellApp(f);
    }

    @Override
    public void closeCurrentApp() {
        rpc.stopCurrentApp();
    }

    @Override
    public void removeMessage(String id) {
        rpc.removeMessage(id);
    }

    @Override
    public void loadApp(String appName) {
        view.onAppStarting();
        eventBus.fireEvent(new AppRequestedEvent(appName));
    }

    @Override
    public void showShellApp(ShellAppType shellAppType) {
        eventBus.fireEvent(new ShellAppRequestedEvent(shellAppType));
    }

    private void doFullScreen(boolean isFullScreen) {
        eventBus.fireEvent(new FullScreenEvent(isFullScreen));
    }

    @Override
    public void onHideShellAppsRequested() {
        ViewportWidget viewportWidget =
                (ViewportWidget) ((ComponentConnector)getState().viewports.get(ViewportType.APP)).getWidget();
        if (viewportWidget.getWidgetCount() > 0) {
            view.onAppStarting();
            eventBus.fireEvent(new HideShellAppsEvent());
        } else {
            showShellApp(ShellAppType.APPLAUNCHER);
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {}

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        List<ComponentConnector> children = getChildComponents();
        for (ComponentConnector connector : children) {
            if (connector instanceof ViewportConnector) {
                final ViewportConnector vc = (ViewportConnector) connector;
                view.updateViewport(vc.getWidget(), vc.getType());
                vc.setEventBus(eventBus);
            } else if (connector instanceof OverlayConnector) {
                if (!view.hasOverlay(connector.getWidget())) {
                    final OverlayConnector oc = (OverlayConnector) connector;
                    ComponentConnector overlayParent = (ComponentConnector) oc.getState().overlayParent;
                    Widget parentWidget = overlayParent.getWidget();
                    view.openOverlayOnWidget(oc.getWidget(), parentWidget);
                }
            } else if (connector instanceof NativeButtonConnector) {
                view.setUserMenu(connector.getWidget());
            }
        }

        List<ComponentConnector> oldChildren = event.getOldChildren();
        oldChildren.removeAll(children);
        for (ComponentConnector cc : oldChildren) {
            cc.getWidget().removeFromParent();
        }
    }

    @Override
    protected Widget createWidget() {
        this.view = new MagnoliaShellViewImpl();
        this.view.setPresenter(this);
        return view.asWidget();
    }

    @Override
    public MagnoliaShellState getState() {
        return (MagnoliaShellState) super.getState();
    }

    @Override
    public void updateViewportLayout(ViewportWidget activeViewport) {
        getLayoutManager().setNeedsMeasure(Util.findConnectorFor(activeViewport));
        getLayoutManager().layoutNow();
    }


    @Override
    public void initHistory() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if (!isHistoryInitialized) {
                    isHistoryInitialized = true;
                    History.fireCurrentHistoryState();
                }
            }
        });
    }

}
