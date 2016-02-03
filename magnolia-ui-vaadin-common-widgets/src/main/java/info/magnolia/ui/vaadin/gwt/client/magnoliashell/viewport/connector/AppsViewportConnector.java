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
package info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.connector;

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.ShellState;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ActivateAppEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.AppRequestedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.CurrentAppCloseEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.FullScreenEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.HideShellAppsEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppStartingEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.AppsTransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.AppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.tabsheet.connector.MagnoliaTabSheetConnector;
import info.magnolia.ui.vaadin.magnoliashell.viewport.AppsViewport;

import java.util.Iterator;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.Util;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;

/**
 * Client-side connector for {@link AppsViewport}.
 */
@Connect(AppsViewport.class)
public class AppsViewportConnector extends ViewportConnector implements AppsViewportWidget.Listener {

    private static final Logger log = Logger.getLogger(AppsViewportConnector.class.getName());

    @Override
    protected void init() {
        super.init();
        getWidget().setTransitionDelegate(new AppsTransitionDelegate(getWidget()));
        addStateChangeHandler("activeComponent", new StateChangeEvent.StateChangeHandler() {
            @Override
            public void onStateChanged(StateChangeEvent event) {
                final ComponentConnector newActiveComponent = (ComponentConnector) getState().activeComponent;
                if (newActiveComponent != null && getWidget().getVisibleChild() != newActiveComponent) {
                    getWidget().showChild(newActiveComponent.getWidget());
                    newActiveComponent.getWidget().getElement().getStyle().clearOpacity();
                } else if (newActiveComponent != null) {
                    log.warning("Switching to 'APP STARTED' state since the active component did not change");
                    ShellState.get().setAppStarted();
                }
            }
        });
    }

    private boolean isAppRunning(String appName) {
        return getState().runningAppNames.contains(appName);
    }

    private boolean isAppRegistered(String appName) {
        return getState().registeredAppNames.contains(appName);
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        super.setEventBus(eventBus);
        eventBus.addHandler(AppRequestedEvent.TYPE, new AppRequestedEvent.Handler() {
            @Override
            public void onAppRequested(AppRequestedEvent event) {
                getWidget().setCurtainVisible(false);
                final String appName = event.getAppName();
                if (isAppRegistered(appName)) {
                    if (!isAppRunning(appName)) {
                        ShellState.get().setAppStarting();
                        getWidget().showAppPreloader(appName);
                    } else {
                        final Widget appWidget = resolveAppWidgetHack(appName);
                        if (appWidget != null && appWidget != getWidget().getVisibleChild()) {
                            ShellState.get().setAppStarting();
                            getWidget().showChild(appWidget);
                        }
                    }
                }
            }
        });

        eventBus.addHandler(ShellAppStartingEvent.TYPE, new ShellAppStartingEvent.Handler() {
            @Override
            public void onShellAppStarting(ShellAppStartingEvent event) {
                int widgetCount = getWidget().getWidgetCount();
                if (getWidget().isAppClosing()) {
                    --widgetCount;
                }
                getWidget().setCurtainVisible(widgetCount > 0);
            }
        });

        eventBus.addHandler(HideShellAppsEvent.TYPE, new HideShellAppsEvent.Handler() {
            @Override
            public void onHideShellApps(HideShellAppsEvent event) {
                getWidget().setCurtainVisible(false);
            }
        });

        eventBus.addHandler(FullScreenEvent.TYPE, new FullScreenEvent.Handler() {
            @Override
            public void onChangeFullScreen(FullScreenEvent event) {
                getWidget().setFullScreen(event.getIsFullScreen());
            }
        });
    }

    @Override
    public AppViewportState getState() {
        return (AppViewportState)super.getState();
    }

    @Override
    public void onConnectorHierarchyChange(final ConnectorHierarchyChangeEvent event) {
        if (getWidget().hasPreloader()) {
            new Timer() {
                @Override
                public void run() {
                    getWidget().removePreloader();
                }
            }.schedule(500);
        }
        AppsViewportConnector.super.onConnectorHierarchyChange(event);
    }

    @Override
    public AppsViewportWidget getWidget() {
        return (AppsViewportWidget) super.getWidget();
    }

    @Override
    protected AppsViewportWidget createWidget() {
        return new AppsViewportWidget(this);
    }

    /**
     * Leave this empty so the viewport doesn't actually center out the children.
     */
    @Override
    protected void alignContent(Element e, LayoutManager layoutManager) {}

    @Override
    public void closeCurrentApp() {
        eventBus.fireEvent(new CurrentAppCloseEvent());
    }

    @Override
    public void activateApp(Widget appWidget) {
        String appName = resolveAppNameHack(appWidget);
        eventBus.fireEvent(new ActivateAppEvent(appName));
    }

    // TODO: Ideally AppsViewport shouldn't need to know about MagnoliaTabSheetConnector - improvement would be to have a mapping from connectors to app names. See MGNLUI-1679.
    private String resolveAppNameHack(Widget appWidget) {
        return ((MagnoliaTabSheetConnector) Util.findConnectorFor(appWidget)).getState().name;
    }

    private Widget resolveAppWidgetHack(String appName) {
        final Iterator<Widget> it = getWidget().iterator();
        while (it.hasNext()) {
            final Widget widget = it.next();
            if (appName.equals(((MagnoliaTabSheetConnector) Util.findConnectorFor(widget)).getState().name)) {
                return widget;
            }
        }
        return null;
    }
}
