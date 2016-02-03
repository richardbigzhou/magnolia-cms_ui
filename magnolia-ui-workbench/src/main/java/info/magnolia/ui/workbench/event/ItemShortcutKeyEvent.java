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
package info.magnolia.ui.workbench.event;

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

/**
 * This event is fired when a registered key shortcut is pressed while an item is selected (i.e. a row in the data grid within the workbench representing
 * either a {@link javax.jcr.Node} or a {@link javax.jcr.Property}).
 */
public class ItemShortcutKeyEvent implements Event<ItemShortcutKeyEvent.Handler> {

    /**
     * Handles {@link ItemShortcutKeyEvent} events.
     */
    public interface Handler extends EventHandler {

        void onItemShortcutKeyEvent(ItemShortcutKeyEvent event);
    }

    private String workspace;

    private final JcrItemAdapter item;

    private final int keyCode;

    private final int[] modifierKeys;

    public ItemShortcutKeyEvent(String workspace, JcrItemAdapter item, int keyCode, int... modifierKeys) {
        this.workspace = workspace;
        this.item = item;
        this.keyCode = keyCode;
        this.modifierKeys = modifierKeys;
    }

    public String getWorkspace() {
        return workspace;
    }

    public JcrItemAdapter getItem() {
        return item;
    }

    public String getItemId() {
        return item != null ? item.getItemId() : null;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int[] getModifierKeys() {
        return modifierKeys;
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onItemShortcutKeyEvent(this);
    }

}
