/**
 * This file Copyright (c) 2011-2015 Magnolia International
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
package info.magnolia.ui.framework.action;

import info.magnolia.cms.core.Path;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A repository operation action which saves the changes and informs the event bus.
 *
 * @param <D> The {@link ActionDefinition} used by the action.
 */
public abstract class AbstractRepositoryAction<D extends ActionDefinition> extends AbstractAction<D> {

    public static final String DEFAULT_NEW_ITEM_NAME = "untitled";

    protected final JcrItemAdapter item;

    private final EventBus eventBus;

    /**
     * Holds the itemId to use for the ContentChangedEvent sent after the action is performed. If the action deletes
     * an item it should set this to the itemId of its parent. If it adds an item it should set this to the itemId
     * of the new item.
     *
     * @see ContentChangedEvent
     */
    private JcrItemId itemIdOfChangedItem;

    private boolean itemContentChanged;

    protected AbstractRepositoryAction(D definition, JcrItemAdapter item, EventBus eventBus) {
        super(definition);
        this.item = item;
        this.eventBus = eventBus;
    }

    protected void setItemIdOfChangedItem(JcrItemId itemIdOfChangedItem) {
        this.itemIdOfChangedItem = itemIdOfChangedItem;
    }

    protected void setItemContentChanged(boolean itemContentChanged) {
        this.itemContentChanged = itemContentChanged;
    }

    /**
     * Executes the defined action on the passed in item. When successful, it will fire a {@link ContentChangedEvent}.
     */
    @Override
    public void execute() throws ActionExecutionException {
        try {
            Session session = item.getJcrItem().getSession();
            onExecute(item);
            session.save();

            // If the subclass set it to null this means no change was performed so we won't send an event
            if (itemIdOfChangedItem != null) {
                eventBus.fireEvent(new ContentChangedEvent(itemIdOfChangedItem, itemContentChanged));
            }

        } catch (RepositoryException e) {
            throw new ActionExecutionException("Can't execute repository operation.\n" + e.getMessage(), e);
        }
    }

    protected abstract void onExecute(JcrItemAdapter item) throws RepositoryException;

    protected String getUniqueNewItemName(Node parent) throws RepositoryException {
        return getUniqueNewItemName(parent, DEFAULT_NEW_ITEM_NAME);
    }

    protected String getUniqueNewItemName(Node parent, String name) throws RepositoryException {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        return Path.getUniqueLabel(parent.getSession(), parent.getPath(), name);
    }
}
