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
package info.magnolia.pages.app.editor.event;

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;

/**
 * Event to notify the system that the move has been started or stopped.
 * Event holds contextual information about the events cause:
 * <pre>
 *  <ul>
 *      <li>Whether it was a server or client side action causing it.</li>
 *      <li>Whether the move was stopped or started.</li>
 *  </ul>
 *
 *  fired by: {@link info.magnolia.pages.app.action.MoveComponentAction}
 *  handler registered in:
 *  <ul>
 *      <li>{@link info.magnolia.pages.app.editor.PagesEditorSubApp#bindHandlers}</li>
 *      <li>{@link info.magnolia.pages.app.editor.PageEditorPresenter#registerHandlers}</li>
 *  </ul>
 * </pre>
 */
public class ComponentMoveEvent implements Event<ComponentMoveEvent.Handler> {

    private boolean start;

    private boolean serverSide;

    public ComponentMoveEvent(boolean start, boolean serverSide) {
        this.start = start;
        this.serverSide = serverSide;
    }

    public boolean isStart() {
        return start;
    }

    public boolean isServerSide() {
        return serverSide;
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onMove(this);
    }

    /**
     * Handler.
     */
    public static interface Handler extends EventHandler {
        void onMove(ComponentMoveEvent event);
    }

}
