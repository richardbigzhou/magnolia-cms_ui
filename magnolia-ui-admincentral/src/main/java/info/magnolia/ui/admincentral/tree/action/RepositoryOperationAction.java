/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.cms.core.Path;
import info.magnolia.ui.admincentral.event.ContentChangedEvent;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.model.action.ActionBase;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.action.ActionExecutionException;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A repository operation action which saves the changes and informs the event bus.
 *
 * @param <D> The {@link ActionDefinition} used by the action.
 */
public abstract class RepositoryOperationAction<D extends ActionDefinition> extends ActionBase<D> {

    protected final Item item;

    private final EventBus eventBus;

    public RepositoryOperationAction(D definition, Item item, EventBus eventBus) {
        super(definition);
        this.item = item;
        this.eventBus = eventBus;
    }

    /**
     * Executes the defined action on the passed in item. When successful, it will fire a {@link ContentChangedEvent}.
     */
    @Override
    public void execute() throws ActionExecutionException {
        try {
            Session session = item.getSession();
            onExecute(item);
            session.save();
            eventBus.fireEvent(new ContentChangedEvent(session.getWorkspace().getName(), getItemPath()));
        } catch (RepositoryException e) {
            throw new ActionExecutionException("Can't execute repository operation.\n" + e.getMessage(), e);
        }
    }

    /**
     * Get Item Path.
     * Used by subclass to define the item path (for a deleted Item, should be the parent).
     */
    protected String getItemPath() throws RepositoryException {
        return item.getPath();
    }

    protected abstract void onExecute(Item item) throws RepositoryException;

    protected String getUniqueNewItemName(final Item item) throws RepositoryException, ItemNotFoundException, AccessDeniedException {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        return Path.getUniqueLabel(item.getSession(), item.getPath(), "untitled");
    }

}
