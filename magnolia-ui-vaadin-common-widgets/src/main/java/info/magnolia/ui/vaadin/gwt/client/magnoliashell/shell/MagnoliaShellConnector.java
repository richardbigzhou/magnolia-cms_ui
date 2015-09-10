/**
 * This file Copyright (c) 2010-2015 Magnolia International
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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.ShellState;
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
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.Fragment;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ViewportType;
import info.magnolia.ui.vaadin.magnoliashell.MagnoliaShell;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger log = Logger.getLogger(MagnoliaShellConnector.class.getName());

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

        /**
         * Fired when the transition that reveals a shell app has just started.
         */
        eventBus.addHandler(ShellAppStartingEvent.TYPE, new ShellAppStartingEvent.Handler() {
            @Override
            public void onShellAppStarting(ShellAppStartingEvent event) {
                ShellState.get().setShellAppStarting();
                view.onShellAppStarting(event.getType());
            }
        });

        /**
         * Fired when the transition that reveals a shell app has just finished.
         */
        eventBus.addHandler(ShellAppStartedEvent.TYPE, new ShellAppStartedEvent.Handler() {
            @Override
            public void onShellAppStarted(ShellAppStartedEvent event) {
                final String currentHistoryToken = History.getToken();
                final Fragment fragment = Fragment.fromString(currentHistoryToken);
                String newShellAppName = event.getType().name();
                ShellState.get().setShellAppStarted();
                if (currentHistoryToken.isEmpty() || !fragment.isShellApp() || !fragment.getAppName().equalsIgnoreCase(newShellAppName)) {
                    final Fragment newFragment = Fragment.fromString("shell:" + newShellAppName.toLowerCase() + ":");
                    History.newItem(newFragment.toFragment(), false);
                    lastHandledFragment = newFragment;
                    activateShellApp(newFragment);
                }
            }
        });


        /**
         * Fired when the shell app icon was clicked twice, or area outside of a shell app was clicked.
         */
        eventBus.addHandler(HideShellAppsRequestedEvent.TYPE, new HideShellAppsRequestedEvent.Handler() {
            @Override
            public void onHideShellAppsRequest(HideShellAppsRequestedEvent event) {
                if (ShellState.get().isShellAppStarted() || ShellState.get().isShellAppStarting()) {
                    onHideShellAppsRequested();
                }
            }
        });

        /**
         * Fired when the shell app viewport is completely hidden.
         */
        eventBus.addHandler(ShellAppsHiddenEvent.TYPE, new ShellAppsHiddenEvent.Handler() {
            @Override
            public void onShellAppsHidden(ShellAppsHiddenEvent event) {
                    rpc.stopCurrentShellApp();
            }
        });

        /**
         * This one is only fired after swipe/keyboard navigation.
         */
        eventBus.addHandler(ActivateAppEvent.TYPE, new ActivateAppEvent.Handler() {
            @Override
            public void onActivateApp(ActivateAppEvent event) {
                if (!ShellState.get().isShellAppStarting()) {
                    log.log(Level.WARNING, "Starting from swipe/keyboard: " + event.getName());
                    ShellState.get().setAppStarting();
                    rpc.activateApp(Fragment.fromString("app:" + event.getName()));
                }
            }
        });

        /**
         * Handles the address bar navigation.
         */
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final Fragment newFragment = Fragment.fromString(event.getValue());

                if (newFragment.equals(lastHandledFragment)) {
                    return;
                }

                if (newFragment.isShellApp()) {
                    if (!getConnection().hasActiveRequest() || !ShellState.get().isAppStarting()) {
                        doShowShellApp(newFragment.resolveShellAppType());
                    }
                } else {

                    if (lastHandledFragment != null) {
                        log.warning("App navigation from " +lastHandledFragment.toFragment()+ " to " + newFragment.toFragment() + (!newFragment.sameSubApp(lastHandledFragment) ? "" : "not") +", request will %s be sent");
                    }

                    // The fragment of the app that was last displayed in the viewport.
                    final Fragment latestLoadedAppLocation = getState().currentAppUriFragment;

                    /**
                     * The new location points to the same app as before, means probably we have returned from the server roundtrip and app
                     * state/location was refined. Either was - we should mark the state as 'app started'.
                     */
                    if (newFragment.isSameApp(latestLoadedAppLocation)) {
                        log.warning("Switching to 'APP STARTED' state since the updated app is already active");
                        ShellState.get().setAppStarted();
                    }

                    /**
                     * We either have no active request -> the location change came directly from address bar, so we have to navigate,
                     * or the new app is requested, so we notify the server about it.
                     */

                    if (!getConnection().hasActiveRequest() || !newFragment.isSameApp(lastHandledFragment)) {
                        loadApp(newFragment.getAppName());
                    }

                    if (ShellState.get().isAppStarting() || !getConnection().hasActiveRequest()) {
                        rpc.activateApp(newFragment);
                    }
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
        ShellState.get().setAppClosing();
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

    private void doShowShellApp(ShellAppType shellAppType) {
        eventBus.fireEvent(new ShellAppRequestedEvent(shellAppType));
    }

    private void doFullScreen(boolean isFullScreen) {
        eventBus.fireEvent(new FullScreenEvent(isFullScreen));
    }

    @Override
    public void onHideShellAppsRequested() {
        final AppsViewportWidget appViewportWidget = (AppsViewportWidget) ((ComponentConnector)getState().viewports.get(ViewportType.APP)).getWidget();

        // If no app is active, then show or keep the applauncher.
        Widget currentApp = appViewportWidget.getCurrentApp();
        if (currentApp != null) {
            view.onAppStarting();
            ShellState.get().setShellAppClosing();
            eventBus.fireEvent(new HideShellAppsEvent());
        } else {
            doShowShellApp(ShellAppType.APPLAUNCHER);
        }
    }

    @Override
    public void showShellApp(ShellAppType type) {
        // We don't trigger the shell apps via trinity icons and/or 1-3 buttons
        // if there is a request being processed because it will cause another request (fired after transition is done)
        // and eventually could lead to the location change race (so called Disco App effect).
        if (!getConnection().hasActiveRequest()) {
            doShowShellApp(type);
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
