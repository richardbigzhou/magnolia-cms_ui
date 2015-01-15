/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.contentapp.movedialog.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.contentapp.movedialog.MoveActionCallback;
import info.magnolia.ui.framework.action.AbstractMultiItemAction;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.tree.MoveHandler;
import info.magnolia.ui.workbench.tree.MoveLocation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Named;

import com.vaadin.data.Item;

/**
 * Action that moves a node.
 *
 * @see MoveNodeActionDefinition
 */
public class MoveNodeAction extends AbstractMultiItemAction<MoveNodeActionDefinition> {

    /**
     * The item where the items should be moved relative to.
     */
    private final JcrNodeAdapter targetItem;

    protected final EventBus admincentralEventBus;

    private MoveActionCallback callback;

    private MoveLocation moveLocation = MoveLocation.BEFORE;

    private MoveHandler moveHandler;

    public MoveNodeAction(
            MoveNodeActionDefinition definition,
            List<JcrItemAdapter> items,
            JcrNodeAdapter targetItem,
            @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus,
            UiContext uiContext,
            MoveActionCallback callback, MoveHandler moveHandler) {
        super(definition, items, uiContext);
        this.targetItem = targetItem;
        this.admincentralEventBus = admincentralEventBus;
        this.callback = callback;
        this.moveHandler = moveHandler;
    }

    @Override
    public void execute() throws ActionExecutionException {
        Item firstItem = getItems().get(0);
        AbstractJcrAdapter itemAdapter = null;
        if (firstItem instanceof AbstractJcrAdapter) {
            itemAdapter = (AbstractJcrAdapter) firstItem;
        }
        super.execute();
        if (itemAdapter != null) {
            // we need to fire 2 events to ensure that both nodes are reloaded/updated (specially when moving properties)
            // source item
            JcrItemId itemIdOfChangedItem = itemAdapter.getItemId();
            admincentralEventBus.fireEvent(new ContentChangedEvent(itemIdOfChangedItem));
            // target item
            itemIdOfChangedItem = targetItem.getItemId();
            admincentralEventBus.fireEvent(new ContentChangedEvent(itemIdOfChangedItem));

            callback.onMovePerformed(targetItem, moveLocation);
        }
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws Exception {
        moveLocation = getDefinition().getMoveLocation();
        if (!moveHandler.moveItem(item, targetItem, moveLocation)) {
            callback.onMoveCancelled();
            throw new IllegalArgumentException("Move operation was not completed due to failed move validation.");
        }
    }

    @Override
    protected String getSuccessMessage() {
        return getDefinition().getSuccessMessage();
    }

    @Override
    protected String getFailureMessage() {
        return getDefinition().getFailureMessage();
    }

    @Override
    protected List<JcrItemAdapter> getSortedItems(Comparator<JcrItemAdapter> comparator) {
        final List<JcrItemAdapter> sortedItems = super.getSortedItems(comparator);
        if (MoveLocation.AFTER.equals(getDefinition().getMoveLocation())) {
            // moving multiple items after another one would implicitly revert the order of those items - this can be avoided by reverting the collection
            Collections.reverse(sortedItems);
        }
        return sortedItems;
    }
}
