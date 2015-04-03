/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.vaadin.overlay;

import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.vaadin.gwt.client.dialog.connector.OverlayState;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.OverlayClientRpc;
import info.magnolia.ui.vaadin.gwt.client.dialog.rpc.OverlayServerRpc;

import com.vaadin.ui.AbstractSingleComponentContainer;
import com.vaadin.ui.Component;

/**
 * A Single component container that includes a "glass" or "curtain" which dims out and prevents interaction on the elements
 * below it. It is different than a Vaadin Window in that ONLY the component that it is attached to receives the modal glass.
 * It is only modal within the component that it is added to.
 * Positioning of the glass and component depends on one of the parents having css position set to relative or absolute.
 */
public class Overlay extends AbstractSingleComponentContainer {

    /**
     * Timeout listener interface.
     */
    public interface Listener {
        void onOverlayClosed();
    };

    private Listener listener;

    public Overlay(final Component content, final Component overlayParent, final OverlayLayer.ModalityDomain modalityDomain, final OverlayLayer.ModalityLevel modalityLevel) {
        setImmediate(true);

        content.addStyleName("overlay-child");
        setContent(content);
        getState().overlayContent = content;
        getState().overlayParent = overlayParent;

        this.addStyleName("overlay");
        this.addStyleName("open");

        // Set css classes of Modal
        this.addStyleName(modalityDomain.getCssClass());
        this.addStyleName(modalityLevel.getCssClass());

        registerRpc(new OverlayServerRpc() {
            @Override
            public void onClosed() {
                if (listener != null) {
                    listener.onOverlayClosed();
                }
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected OverlayState getState() {
        return (OverlayState) super.getState();
    }

    public void setCloseTimeout(int closeTimeout) {
        getState().closeTimeout = closeTimeout;
    }

    public void close() {
        getRpcProxy(OverlayClientRpc.class).close();
    }

    @Override
    public void detach() {
        if (getParent() != null) {
            super.detach();
        }
    }
}
