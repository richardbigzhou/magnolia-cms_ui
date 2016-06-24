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
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.factory.AbstractFieldFactory;
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
 * @param <T> the field type
 */
public abstract class AbstractAccessFieldFactory<D extends FieldDefinition, T extends AccessControlList> extends AbstractFieldFactory<D, T> {

    protected AbstractAccessFieldFactory(D definition, Item relatedFieldItem, UiContext uiContext, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, relatedFieldItem, uiContext, i18nAuthoringSupport);
    }

    /**
     * @deprecated since 5.4.7 - use {@link #AbstractAccessFieldFactory(FieldDefinition, Item, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    protected AbstractAccessFieldFactory(D definition, Item relatedFieldItem) {
        this(definition, relatedFieldItem, null, Components.getComponent(I18NAuthoringSupport.class));
    }

    /**
     * @deprecated since 5.4.8 - won't use anymore.
     */
    @Deprecated
    protected AbstractJcrNodeAdapter getOrAddAclItem(JcrNodeAdapter roleItem, String aclName) throws RepositoryException {
        AbstractJcrNodeAdapter aclItem = roleItem.getChild(aclName);
        if (aclItem == null) {
            Node roleNode = roleItem.getJcrItem();
            if (roleItem instanceof JcrNewNodeAdapter || !roleNode.hasNode(aclName)) {
                aclItem = new JcrNewNodeAdapter(roleNode, NodeTypes.ContentNode.NAME);
                aclItem.setNodeName(aclName);
                roleItem.addChild(aclItem);
            } else {
                Node aclNode = roleNode.getNode(aclName);
                aclItem = new JcrNodeAdapter(aclNode);
                roleItem.addChild(aclItem);
            }
        }
        return aclItem;
    }

    /**
     * @deprecated since 5.4.8 - won't use anymore.
     */
    @Deprecated
    protected JcrNewNodeAdapter addAclEntryItem(AbstractJcrNodeAdapter aclItem) throws RepositoryException {
        JcrNewNodeAdapter newItem = new JcrNewNodeAdapter(aclItem.getJcrItem(), NodeTypes.ContentNode.NAME);
        newItem.setNodeName(getUniqueNodeNameForChild(aclItem));
        aclItem.addChild(newItem);
        return newItem;
    }

    /**
     * @deprecated since 5.4.8 - won't use anymore.
     */
    @Deprecated
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
