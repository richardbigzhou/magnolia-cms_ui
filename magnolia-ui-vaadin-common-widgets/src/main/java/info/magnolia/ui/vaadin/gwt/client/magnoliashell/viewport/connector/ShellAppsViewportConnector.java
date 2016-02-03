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

import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.AppRequestedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.HideShellAppsEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.HideShellAppsRequestedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppRequestedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppStartedEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppStartingEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.event.ShellAppsHiddenEvent;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.ShellAppsTransitionDelegate;
import info.magnolia.ui.vaadin.gwt.client.magnoliashell.viewport.widget.ShellAppsViewportWidget;
import info.magnolia.ui.vaadin.gwt.client.shared.magnoliashell.ShellAppType;
import info.magnolia.ui.vaadin.magnoliashell.viewport.ShellAppsViewport;

import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.Util;
import com.vaadin.shared.ui.Connect;

/**
 * ShellAppsViewportConnector.
 */
@Connect(ShellAppsViewport.class)
public class ShellAppsViewportConnector extends ViewportConnector implements ShellAppsViewportWidget.Listener {

    public boolean locked = false;

    @Override
    protected void init() {
        super.init();
        getWidget().setTransitionDelegate(new ShellAppsTransitionDelegate(getWidget(), getConnection()));
        getConnection().addHandler(ApplicationConnection.ResponseHandlingStartedEvent.TYPE, new ApplicationConnection.CommunicationHandler() {
            @Override
            public void onRequestStarting(ApplicationConnection.RequestStartingEvent e) {
                locked = true;
            }

            @Override
            public void onResponseHandlingStarted(ApplicationConnection.ResponseHandlingStartedEvent e) {
                locked = false;
            }

            @Override
            public void onResponseHandlingEnded(ApplicationConnection.ResponseHandlingEndedEvent e) {
            }
        });
    }

    public void showShellApp(ShellAppType type) {
        ComponentConnector shellAppConnector = (ComponentConnector) getState().shellApps.get(type);
        Widget w = shellAppConnector.getWidget();
        getWidget().showChild(w);
        if (!getWidget().isActive()) {
            getWidget().setActive(true);
        }
        getLayoutManager().setNeedsMeasure(shellAppConnector);
        eventBus.fireEvent(new ShellAppStartingEvent(type));
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        super.setEventBus(eventBus);
        eventBus.addHandler(ShellAppRequestedEvent.TYPE, new ShellAppRequestedEvent.Handler() {
            @Override
            public void onShellAppRequested(ShellAppRequestedEvent event) {
                if (!getWidget().isActive() || !getConnection().hasActiveRequest()) {
                    showShellApp(event.getType());
                }
            }
        });

        eventBus.addHandler(HideShellAppsEvent.TYPE, new HideShellAppsEvent.Handler() {
            @Override
            public void onHideShellApps(HideShellAppsEvent event) {
                getWidget().setActive(false);
            }
        });

        eventBus.addHandler(AppRequestedEvent.TYPE, new AppRequestedEvent.Handler() {
            @Override
            public void onAppRequested(AppRequestedEvent event) {
                getWidget().setActiveNoTransition(false);
            }
        });
    }

    @Override
    public ShellAppsViewportWidget getWidget() {
        return (ShellAppsViewportWidget)super.getWidget();
    }

    @Override
    protected ShellAppsViewportWidget createWidget() {
        return new ShellAppsViewportWidget(this);
    }

    @Override
    public ShellAppViewportState getState() {
        return (ShellAppViewportState) super.getState();
    }

    @Override
    public void onShellAppLoaded(Widget shellAppWidget) {
        ComponentConnector shellAppConnector = Util.findConnectorFor(shellAppWidget);
        eventBus.fireEvent(new ShellAppStartedEvent(getState().getShellAppType(shellAppConnector)));
    }

    @Override
    public void outerContentClicked() {
        eventBus.fireEvent(new HideShellAppsRequestedEvent());
    }

    @Override
    public void onShellAppsHidden() {
        eventBus.fireEvent(new ShellAppsHiddenEvent());
    }

}
