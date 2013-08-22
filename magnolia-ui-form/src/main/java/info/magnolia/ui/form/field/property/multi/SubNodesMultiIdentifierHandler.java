/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.form.field.property.multi;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Set;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Sub Nodes implementation of {@link ListHandler}.<br>
 * Store the list of values as subNodes.<br>
 * Node structure:
 * <ul>
 * <li>root
 * <ul>
 * <li>childNode (nodeName = subNodeName)
 * <ul>
 * <li>valueNode1 (nodeName = 20 first char of the related list value) <br>
 * valueNode1.listValue (propertyName = subNodeName)</li>
 * </ul>
 * <ul>
 * <li>valueNode2 (nodeName = 20 first char of the related list value) <br>
 * valueNode2.listValue (propertyName = subNodeName)</li>
 * </ul>
 * </li>
 * </ul>
 * </ul>
 * 
 * @param <T> type of the element list.
 */
public class SubNodesMultiIdentifierHandler<T> extends SubNodesMultiHandler<Property<T>> {

    private static final Logger log = LoggerFactory.getLogger(SubNodesMultiIdentifierHandler.class);
    private String subNodeName;
    private int valueItemNameSize = 20;

    @Inject
    public SubNodesMultiIdentifierHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider) {
        super(parent, definition, componentProvider);
        this.subNodeName = definition.getName();
    }

    @Override
    protected JcrNodeAdapter getRootItem() {
        JcrNodeAdapter res = null;
        try {
            res = getOrCreateChildNode(subNodeName, NodeTypes.Content.NAME);
        } catch (RepositoryException re) {
            log.warn("Not able to retrieve or create a sub node for the parent node {}", ((JcrNodeAdapter) parent).getItemId());
        }
        return res;
    }

    @Override
    protected String createChildItemName(Set<String> childNames, Object value, JcrNodeAdapter rootItem) {
        return Path.getValidatedLabel(StringUtils.left(value.toString(), valueItemNameSize));
    }

    @Override
    protected void handleRootitemAndParent(JcrNodeAdapter rootItem) {
        // Attach the child item to the root item
        if (rootItem.getChildren() != null && !rootItem.getChildren().isEmpty()) {
            ((JcrNodeAdapter) parent).addChild(rootItem);
        } else {
            ((JcrNodeAdapter) parent).removeChild(rootItem);
        }
    }

    /**
     * Return a null predicate.
     */
    @Override
    protected Predicate createPredicateToEvaluateChildNode() {
        return null;
    }

}

