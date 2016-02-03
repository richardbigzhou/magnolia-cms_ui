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
package info.magnolia.ui.contentapp.movedialog.action;

import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.contentapp.movedialog.MoveActionCallback;
import info.magnolia.ui.framework.action.AbstractMultiItemAction;
import info.magnolia.ui.framework.action.MoveLocation;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.List;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Action that moves a node.
 *
 * @see MoveNodeActionDefinition
 */
public class MoveNodeAction extends AbstractMultiItemAction<MoveNodeActionDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The item where the items should be moved relative to.
     */
    private final JcrNodeAdapter targetItem;

    protected final EventBus admincentralEventBus;

    private MoveActionCallback callback;

    private MoveLocation moveLocation = MoveLocation.BEFORE;

    public MoveNodeAction(
            MoveNodeActionDefinition definition,
            List<JcrItemAdapter> items,
            JcrNodeAdapter targetItem,
            @Named(AdmincentralEventBus.NAME) EventBus admincentralEventBus,
            UiContext uiContext,
            MoveActionCallback callback) {
        super(definition, items, uiContext);
        this.targetItem = targetItem;
        this.admincentralEventBus = admincentralEventBus;
        this.callback = callback;
    }

    @Override
    public void execute() throws ActionExecutionException {
        super.execute();
        Item firstItem = getItems().get(0);
        if (firstItem instanceof JcrNodeAdapter) {
            JcrNodeAdapter nodeAdapter = (JcrNodeAdapter) firstItem;
            String itemIdOfChangedItem;
            try {
                itemIdOfChangedItem = JcrItemUtil.getItemId(nodeAdapter.getJcrItem());
                admincentralEventBus.fireEvent(new ContentChangedEvent(nodeAdapter.getWorkspace(), itemIdOfChangedItem));
                callback.onMovePerformed(targetItem, moveLocation);
            } catch (RepositoryException e) {
                callback.onMoveCancelled();
            }
        }
    }

    @Override
    protected void executeOnItem(JcrItemAdapter item) throws Exception {
        if (basicMoveCheck(item.getJcrItem(), targetItem.getJcrItem())) {
            moveLocation = getDefinition().getMoveLocation();

            Node source = (Node) item.getJcrItem();
            Node target = targetItem.getJcrItem();

            switch (moveLocation) {
                case INSIDE:
                    NodeUtil.moveNode(source, target);
                    break;
                case BEFORE:
                    NodeUtil.moveNodeBefore(source, target);
                    break;
                case AFTER:
                    NodeUtil.moveNodeAfter(source, target);
                    break;
            }
            Session session = source.getSession();
            session.save();
        } else {
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

    /**
     * Perform basic check.
     */
    private boolean basicMoveCheck(javax.jcr.Item source, javax.jcr.Item target) throws RepositoryException {
        if (!target.isNode() || !source.isNode()) {
            return false;
        }
        if (target.getPath().equals(source.getPath())) {
            return false;
        }
        return !NodeUtil.isSame((Node) target, source.getParent());
    }
}
