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
package info.magnolia.security.app.dialog.field;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;

/**
 * Abstract super class for field builder that provide fields for editing ACLs.
 *
 * @param <D> definition type
 */
public abstract class AbstractAccessFieldFactory<D extends FieldDefinition> extends AbstractFieldFactory<D, Object> {

    protected AbstractAccessFieldFactory(D definition, Item relatedFieldItem) {
        super(definition, relatedFieldItem);
    }

    protected AbstractJcrNodeAdapter getOrAddAclItem(JcrNodeAdapter roleItem, String aclName) throws RepositoryException {
        AbstractJcrNodeAdapter aclItem = roleItem.getChild(aclName);
        if (aclItem == null) {
            Node roleNode = roleItem.getJcrItem();
            if (roleNode.hasNode(aclName)) {
                Node aclNode = roleNode.getNode(aclName);
                aclItem = new JcrNodeAdapter(aclNode);
                roleItem.addChild(aclItem);
            } else {
                aclItem = new JcrNewNodeAdapter(roleNode, NodeTypes.ContentNode.NAME);
                aclItem.setNodeName(aclName);
                roleItem.addChild(aclItem);
            }
        }
        return aclItem;
    }

    protected JcrNewNodeAdapter addAclEntryItem(AbstractJcrNodeAdapter aclItem) throws RepositoryException {
        JcrNewNodeAdapter newItem = new JcrNewNodeAdapter(aclItem.getJcrItem(), NodeTypes.ContentNode.NAME);
        newItem.setNodeName(getUniqueNodeNameForChild(aclItem));
        aclItem.addChild(newItem);
        return newItem;
    }

    protected String getUniqueNodeNameForChild(AbstractJcrNodeAdapter parentItem) throws RepositoryException {

        // The adapter cannot handle more than one unnamed child, see MGNLUI-1459, so we have to generate unique ones

        Node parentNode = null;
        if (!(parentItem instanceof JcrNewNodeAdapter)) {
            parentNode = parentItem.getJcrItem();
        }

        int newNodeName = 0;
        while (true) {
            if (parentItem.getChild(String.valueOf(newNodeName)) != null) {
                newNodeName++;
                continue;
            }
            if (parentNode != null && parentNode.hasNode(String.valueOf(newNodeName))) {
                newNodeName++;
                continue;
            }
            break;
        }

        return String.valueOf(newNodeName);
    }
}
