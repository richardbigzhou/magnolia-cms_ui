/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.workbench.event;

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

/**
 * This event is fired when an item is selected (i.e. a row in the data grid within the workbench
 * representing either a {@link javax.jcr.Node} or a {@link javax.jcr.Property}).
 */
public class ItemSelectedEvent implements Event<ItemSelectedEvent.Handler> {

    /**
     * Handles {@link ItemSelectedEvent} events.
     */
    public interface Handler extends EventHandler {

        void onItemSelected(ItemSelectedEvent event);
    }

    private final String workspace;

    private final JcrItemAdapter item;

    public ItemSelectedEvent(String workspace, JcrItemAdapter item) {
        this.workspace = workspace;
        this.item = item;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getPath() {
        return item != null ? item.getItemId() : null;
    }

    public JcrItemAdapter getItem() {
        return item;
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onItemSelected(this);
    }
}
