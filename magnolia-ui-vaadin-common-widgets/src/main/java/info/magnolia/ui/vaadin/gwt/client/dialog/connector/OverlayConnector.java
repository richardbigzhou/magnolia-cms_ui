/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.dialog.connector;

import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.OverlayClientRpc;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.OverlayServerRpc;
import info.magnolia.ui.vaadin.gwt.client.dialog.widget.OverlayWidget;
import info.magnolia.ui.vaadin.overlay.Overlay;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Client-side connector for Overlay component.
 */
@Connect(Overlay.class)
public class OverlayConnector extends AbstractSingleComponentContainerConnector {

    public static final int OVERLAY_CLOSE_ANIMATION_DURATION_MSEC = 500;

    private OverlayServerRpc rpc = RpcProxy.create(OverlayServerRpc.class, this);

    private Timer automaticRemovalTimer = new Timer() {
        @Override
        public void run() {
            removeSelf();
        }
    };

    @Override
    protected void init() {
        super.init();
        registerRpc(OverlayClientRpc.class, new OverlayClientRpc() {
            @Override
            public void close() {
                removeSelf();
            }
        });
    }

    public void removeSelf() {
        final Object lock = new Object();
        getWidget().addStyleName("close");
        getConnection().suspendReponseHandling(lock);
        final Widget w = getWidget();
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                getConnection().resumeResponseHandling(lock);
                w.removeFromParent();
                rpc.onClosed();
                return false;
            }
        }, OVERLAY_CLOSE_ANIMATION_DURATION_MSEC);
    }

    @Override
    public void onStateChanged(StateChangeEvent event) {
        super.onStateChanged(event);
        if (event.hasPropertyChanged("closeTimeout")) {
            int timeout = getState().closeTimeout;
            if (timeout < 0) {
                automaticRemovalTimer.cancel();
            } else {
                automaticRemovalTimer.schedule(timeout * 1000);
            }
        }
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
    }

    @Override
    public OverlayWidget getWidget() {
        return (OverlayWidget) super.getWidget();
    }

    @Override
    public void onConnectorHierarchyChange(final ConnectorHierarchyChangeEvent e) {
        if (getContent() != null) {
            getWidget().setWidget(getContent().getWidget());
        }
    }

    @Override
    public OverlayState getState() {
        return (OverlayState) super.getState();
    }
}
