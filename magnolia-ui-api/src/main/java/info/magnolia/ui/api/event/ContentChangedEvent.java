/**
 * This file Copyright (c) 2011-2013 Magnolia International
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
package info.magnolia.ui.api.event;

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;

/**
 * Event fired when content is changed in a workspace. Holds an itemId of an item that is related to the changes made.
 * If the change was a deletion the itemId is the parent of the deleted item. If something was added the itemId is for
 * the new item.
 */
public class ContentChangedEvent implements Event<ContentChangedEvent.Handler> {

    /**
     * Handles {@link ContentChangedEvent} events.
     */
    public interface Handler extends EventHandler {

        void onContentChanged(ContentChangedEvent event);
    }

    private String workspace;

    private String itemId;

    /**
     * Whether the content changed is a property.
     */
    private boolean propertyChange = false;

    public ContentChangedEvent(String workspace, String itemId) {
        this.workspace = workspace;
        this.itemId = itemId;
    }

    public ContentChangedEvent(String workspace, String itemId, boolean isProperty) {
        this.workspace = workspace;
        this.itemId = itemId;
        this.propertyChange = isProperty;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getItemId() {
        return itemId;
    }

    public boolean isPropertyChange() {
        return propertyChange;
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onContentChanged(this);
    }
}
