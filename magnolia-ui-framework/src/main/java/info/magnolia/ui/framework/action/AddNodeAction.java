/**
 * This file Copyright (c) 2010-2016 Magnolia International
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

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;

/**
 * Action for adding a new node.
 *
 * @see AddNodeActionDefinition
 */
public class AddNodeAction extends AbstractRepositoryAction<AddNodeActionDefinition> {

    public AddNodeAction(AddNodeActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus) {
        super(definition, item, eventBus);
    }

    @Override
    protected void onExecute(JcrItemAdapter item) throws RepositoryException {
        if (item.getJcrItem().isNode()) {
            Node node = (Node) item.getJcrItem();
            String baseName = getDefinition().getBaseName();
            if (StringUtils.isBlank(baseName)) {
                baseName = AbstractRepositoryAction.DEFAULT_NEW_ITEM_NAME;
            }

            String name = getUniqueNewItemName(node, baseName);
            Node newNode = node.addNode(name, getDefinition().getNodeType());
            // Resolve item id for the newly created node and pass it on as a modified one
            JcrItemId itemId = JcrItemUtil.getItemId(newNode);
            setItemIdOfChangedItem(itemId);
            setItemContentChanged(true);
        }
    }
}
