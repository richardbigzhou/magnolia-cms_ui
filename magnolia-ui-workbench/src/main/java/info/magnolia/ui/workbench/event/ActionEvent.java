/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Event fired when the inplace editing field value was edited.
 */
public class ActionEvent implements Event<ActionEvent.Handler> {

    private final String actionName;
    private final Set<Object> itemIds = new HashSet<Object>();
    private final Object[] parameters;

    /**
     * Instantiates a new item edited event.
     * 
     * @param actionName the action name
     * @param itemId the item id
     * @param parameters the parameters
     */
    public ActionEvent(String actionName, Object itemId, Object... parameters) {
        this.actionName = actionName;
        this.itemIds.add(itemId);
        this.parameters = parameters;
    }

    /**
     * Instantiates a new item edited event.
     * 
     * @param actionName the action name
     * @param itemIds the item ids
     * @param parameters the parameters
     */
    public ActionEvent(String actionName, Set<Object> itemIds, Object... parameters) {
        this.actionName = actionName;
        this.itemIds.addAll(itemIds);
        this.parameters = parameters;
    }

    /**
     * @return the actionName
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * @return the itemId
     */
    public Set<Object> getItemIds() {
        return itemIds;
    }

    /**
     * @return the parameters
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Event listener that should react to item edited events.
     */
    public interface Handler extends EventHandler {

        void onAction(ActionEvent event);
    }

    /**
     * Event notifier that should register item edited events.
     */
    public interface Notifier extends Serializable {

        void addActionListener(ActionEvent.Handler listener);

        void removeActionListener(ActionEvent.Handler listener);
    }

    @Override
    public void dispatch(Handler handler) {
        handler.onAction(this);
    }
}
