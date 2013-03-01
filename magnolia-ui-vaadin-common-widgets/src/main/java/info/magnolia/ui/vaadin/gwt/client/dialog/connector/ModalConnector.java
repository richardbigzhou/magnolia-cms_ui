/**
 * This file Copyright (c) 2013 Magnolia International
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

import info.magnolia.ui.vaadin.dialog.Modal;
import info.magnolia.ui.vaadin.gwt.client.dialog.widget.ModalWidget;

import com.google.gwt.user.client.Timer;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for Modal - Takes care of unregistering the widget at the proper time - so the modal dissapears when it is ready.
 */
@Connect(Modal.class)
public class ModalConnector extends AbstractSingleComponentContainerConnector {

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
    }

    @Override
    public ModalWidget getWidget() {
        return (ModalWidget) super.getWidget();
    }

    @Override
    protected ModalWidget createWidget() {
        return new ModalWidget(this);
    }


    @Override
    public void onUnregister() {
        // Delay the destruction of the connector till the widget is ready.
        new Timer() {

            @Override
            public void run() {
                ModalConnector.super.onUnregister();
            }
        }.schedule(300);
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent e) {
        if (!e.getOldChildren().isEmpty()) {
            final ComponentConnector oldContent = e.getOldChildren().get(0);
            getWidget().remove(oldContent.getWidget());
        }

        if (getContent() != null) {
            getWidget().setWidget(getContent().getWidget());
        }
    }

    @Override
    public ModalState getState() {
        return (ModalState) super.getState();
    }
}
