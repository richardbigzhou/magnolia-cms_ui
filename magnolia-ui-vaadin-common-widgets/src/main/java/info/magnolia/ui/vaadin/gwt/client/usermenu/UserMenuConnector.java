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
package info.magnolia.ui.vaadin.gwt.client.usermenu;

import info.magnolia.ui.vaadin.usermenu.UserMenu;

import org.vaadin.peter.contextmenu.client.ContextMenuServerRpc;
import org.vaadin.peter.contextmenu.client.ContextMenuState;
import org.vaadin.peter.contextmenu.client.ContextMenuWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Replaces {@link UserMenuConnector}. Registers {@link UserMenuClientRpc} and calculates the coordinates of the widget based
 * on {@link #extensionTarget}s position.
 * It does not register a {@link com.google.gwt.event.dom.client.ContextMenuHandler}. To open the widget you will have to open
 * it from server side using {@link info.magnolia.ui.vaadin.gwt.client.usermenu.UserMenuClientRpc#showContextMenu()}.
 */
@Connect(UserMenu.class)
public class UserMenuConnector extends AbstractExtensionConnector {

    protected ContextMenuWidget widget;

    protected Widget extensionTarget;

    protected ContextMenuServerRpc clientToServerRPC = RpcProxy.create(
            ContextMenuServerRpc.class, this);

    protected CloseHandler<PopupPanel> contextMenuCloseHandler = new CloseHandler<PopupPanel>() {
        @Override
        public void onClose(CloseEvent<PopupPanel> popupPanelCloseEvent) {
            clientToServerRPC.contextMenuClosed();
        }
    };

    protected UserMenuClientRpc serverToClientRPC = new UserMenuClientRpc() {

        @Override
        public void showContextMenu() {
            Widget clickTargetWidget = extensionTarget;

            int targetW = clickTargetWidget.getOffsetWidth();
            int x = clickTargetWidget.getAbsoluteLeft() + targetW;
            int y = clickTargetWidget.getAbsoluteTop() + clickTargetWidget.getOffsetHeight();

            widget.showContextMenu(x, y);

        }
    };

    @Override
    protected void init() {
        widget = GWT.create(ContextMenuWidget.class);
        widget.addCloseHandler(contextMenuCloseHandler);
        registerRpc(UserMenuClientRpc.class, serverToClientRPC);
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        widget.clearItems();

        for (ContextMenuState.ContextMenuItemState rootItem : getState().getRootItems()) {
            widget.addRootMenuItem(rootItem, this);
        }
    }

    @Override
    public ContextMenuState getState() {
        return (ContextMenuState) super.getState();
    }


    @Override
    protected void extend(ServerConnector extensionTarget) {
        this.extensionTarget = ((ComponentConnector) extensionTarget)
                .getWidget();
    }
}
