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

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event used to notify the system, that a move has started. Not used for DnD events.
 * Initiated by a client side move, when clicking on the move button on the {@link info.magnolia.ui.vaadin.gwt.client.widget.controlbar.ComponentBar}.
 * The event will cause a server round trip to notify the server which will in return start the move on client side.
 *
 * @see info.magnolia.ui.vaadin.gwt.client.rpc.PageEditorClientRpc
 * <pre>
 *  <ul>
 *      <li>fired by {@link info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent#onMoveStart(boolean)}</li>
 *      <li>handled by {@link info.magnolia.ui.vaadin.gwt.client.connector.PageEditorConnector}</li>
 *  </ul>
 * </pre>
 */
public class ComponentStartMoveEvent extends GwtEvent<ComponentStartMoveEvent.CompnentStartMoveEventHandler> {

    public static Type<CompnentStartMoveEventHandler> TYPE = new Type<CompnentStartMoveEventHandler>();

    @Override
    public Type<CompnentStartMoveEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CompnentStartMoveEventHandler handler) {
        handler.onStart(this);
    }

    /**
     * Handler for {@link ComponentStartMoveEvent}.
     */
    public static interface CompnentStartMoveEventHandler extends EventHandler {
        void onStart(ComponentStartMoveEvent componentStartMoveEvent);
    }
}
