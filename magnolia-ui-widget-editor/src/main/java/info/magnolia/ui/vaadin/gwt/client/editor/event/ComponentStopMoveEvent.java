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
package info.magnolia.ui.vaadin.gwt.client.editor.event;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event to notify the system that the move has been stopped.
 * Event holds contextual information about the stop:
 * <pre>
 *  <ul>
 *      <li>Whether it was a server or client side action causing it.</li>
 *      <li>Whether the move was cancelled (by dropping it outside the drop zone, hitting ESC, "Cancel move" by action bar
 *      or stop the drag of the component).</li>
 *  </ul>

 *  fired by:
 *  <ul>
 *      <li>{@link info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent#onMoveStop} to notify about a sort</li>
 *      <li>{@link info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent#onMoveCancel()} when cancelled from clientside</li>
 *      <li>{@link info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorClientRpc#cancelMoveComponent} when cancelled from serverside</li>
 *  </ul>
 *  handler registered in:
 *  <ul>
 *      <li>info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent#doStartMove(boolean) for notifying the source component</li>
 *      <li>{@link info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent#registerMoveTarget(boolean)} by all move target components.</li>
 *      <li>{@link info.magnolia.ui.vaadin.gwt.client.connector.PageEditorConnector} to reset the {@link info.magnolia.ui.vaadin.gwt.client.editor.model.Model}
 *      and notify the server when client side action.</li>
 *  </ul>
 * </pre>
 */
public class ComponentStopMoveEvent extends GwtEvent<ComponentStopMoveEvent.ComponentStopMoveEventHandler> {

    public static Type<ComponentStopMoveEventHandler> TYPE = new Type<ComponentStopMoveEventHandler>();

    private MgnlComponent component;

    private boolean serverSide;

    public ComponentStopMoveEvent(MgnlComponent component, boolean serverSide) {
        this.component = component;
        this.serverSide = serverSide;
    }

    @Override
    public Type<ComponentStopMoveEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ComponentStopMoveEventHandler handler) {
        handler.onStop(this);
    }

    public MgnlComponent getTargetComponent() {
        return component;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    /**
     * Handler for {@link ComponentStopMoveEvent}.
     */
    public static interface ComponentStopMoveEventHandler extends EventHandler {
        void onStop(ComponentStopMoveEvent componentMoveEvent);
    }
}
