/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.security.app.dialog.action;

import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.security.app.util.UsersWorkspaceUtil;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.DuplicateNodeAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that creates a new node by duplicating an existing one.
 *
 * @see DuplicateUserActionDefinition
 */
public class DuplicateUserAction extends DuplicateNodeAction {

    private static final Logger log = LoggerFactory.getLogger(DuplicateUserAction.class);

    public DuplicateUserAction(DuplicateUserActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition, item, eventBus);
    }

    @Override
    protected void onExecute(JcrItemAdapter item) throws RepositoryException {
        Item jcrItem = item.getJcrItem();
        if (jcrItem.isNode()) {
            Node node = (Node) jcrItem;
            Node parentNode = node.getParent();

            // Generate name and path of the new node
            String newName = getUniqueNewItemName(parentNode, node.getName());
            String newPath = Path.getAbsolutePath(parentNode.getPath(), newName);

            // Duplicate node
            node.getSession().getWorkspace().copy(node.getPath(), newPath);

            // Update metadata
            Node duplicateNode = node.getSession().getNode(newPath);

            UsersWorkspaceUtil.updateAcls(duplicateNode, node.getPath());

            activatableUpdate(duplicateNode, MgnlContext.getUser().getName());
            // Set item of the new node for the ContentChangedEvent
            JcrItemId itemId = JcrItemUtil.getItemId(duplicateNode);
            setItemIdOfChangedItem(itemId);

            log.debug("Created a copy of {} with new path {}", node.getPath(), newPath);
        }
    }
    private void activatableUpdate(Node node, String userName) throws RepositoryException {
        if (NodeUtil.isNodeType(node, NodeTypes.Activatable.NAME) ||
                (RepositoryConstants.CONFIG.equals(node.getSession().getWorkspace().getName()) && NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME))) {
            NodeTypes.Activatable.update(node, userName, false);
        }

        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            activatableUpdate(nodeIterator.nextNode(), userName);
        }
    }

}
