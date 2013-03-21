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
package info.magnolia.ui.admincentral.tree.action;

import info.magnolia.cms.core.Path;
import info.magnolia.event.EventBus;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.framework.event.AdminCentralEventBusConfigurer;

import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DuplicateNodeAction. Create a Copy of the selected Node. Use
 * {@link Path#getUniqueLabel} to create the new node Name.
 */
public class DuplicateNodeAction extends RepositoryOperationAction<DuplicateNodeActionDefinition> {

    private static final Logger log = LoggerFactory.getLogger(DuplicateNodeAction.class);

    /**
     * Instantiates a new duplicate node action.
     */
    public DuplicateNodeAction(DuplicateNodeActionDefinition definition, Item item, @Named(AdminCentralEventBusConfigurer.EVENT_BUS_NAME) EventBus eventBus) {
        super(definition, item, eventBus);
    }


    @Override
    protected void onExecute(Item item) throws RepositoryException {
        Node node = (Node) item;
        String newName = Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), node.getName());
        String newPath = Path.getAbsolutePath(node.getParent().getPath(), newName);
        // Duplicate Node
        node.getSession().getWorkspace().copy(node.getPath(), newPath);
        log.debug("Create a copy of {} with new Path {}", node.getPath(), newPath);
        // Update dates.
        Node duplicateNode = node.getSession().getNode(newPath);
        NodeTypes.LastModified.update(duplicateNode);
    }

}
