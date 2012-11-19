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
package info.magnolia.ui.app.contacts.action;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.admincentral.tree.action.RepositoryOperationAction;
import info.magnolia.ui.framework.event.EventBus;

/**
 * Creates a folder either as a child if the node given is itself a folder or as a child of the nearest ancestor that
 * is a folder.
 */
public class AddFolderAction extends RepositoryOperationAction<AddFolderActionDefinition> {

    public AddFolderAction(AddFolderActionDefinition definition, Item item, @Named("admincentral") EventBus eventBus) {
        super(definition, item, eventBus);
    }

    @Override
    protected void onExecute(Item item) throws RepositoryException {

        Node node = (Node) item;

        node = findAncestorOfType(node, "mgnl:folder");
        if (node == null) {
            node = (Node) item.getAncestor(0);
        }

        String name = Path.getUniqueLabel(node.getSession(), node.getPath(), "untitled");
        Node newNode = node.addNode(name, "mgnl:folder");
        NodeTypes.CreatedMixin.setCreation(newNode);
    }

    private Node findAncestorOfType(Node node, String nodeType) throws RepositoryException {
        while (node.getDepth() > 0) {
            if (NodeUtil.isNodeType(node, nodeType)) {
                return node;
            }
            node = node.getParent();
        }
        return null;
    }
}
