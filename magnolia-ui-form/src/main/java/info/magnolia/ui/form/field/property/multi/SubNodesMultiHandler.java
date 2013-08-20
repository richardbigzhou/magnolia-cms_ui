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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.property.AbstractBaseHandler;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;

/**
 * Generic List Sub Nodes {@link info.magnolia.ui.form.field.property.PropertyHandler} implementation.<br>
 * Structure: <br>
 * - root node <br>
 * -- child1 <br>
 * --- property1<br>
 * -- child2 <br>
 * --- property2 ...<br>
 * 
 * @param <T> type of the element list.
 */
public class SubNodesMultiHandler<T> extends AbstractBaseHandler<List<T>> implements PropertyHandler<List<T>> {

    private static final Logger log = LoggerFactory.getLogger(SubNodesMultiHandler.class);

    private String childNodeType = NodeTypes.Content.NAME;
    private String childValuePropertyName;

    public SubNodesMultiHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider) {
        super(parent, definition, componentProvider);
        this.childValuePropertyName = definition.getName();
    }

    /**
     * No I18N Support implemented for subNode.
     */
    @Override
    public boolean hasI18NSupport() {
        return false;
    }

    /**
     * Retrieve a list of values based on the sub nodes.<br>
     * - get a list of childNodes to handle <br>
     * - for each childNode retrieve the value to set to the list <br>
     * If no childNodes are present, return an empty List.
     */
    @Override
    public List<T> readFromDataSourceItem() {
        LinkedList<T> res = new LinkedList<T>();
        JcrNodeAdapter rootItem = getRootItem();
        // Get a list of childNodes
        List<Node> childNodes = getStoredChildNodes((JcrNodeAdapter) rootItem);
        for (Node child : childNodes) {
            T value = getValueFromChildNode(child);
            if (value != null) {
                res.add(value);
            }
        }
        return res;
    }

    /**
     * Define the root Item use in order to set the List element as SubNodes.
     */
    protected JcrNodeAdapter getRootItem() {
        return (JcrNodeAdapter) parent;
    }

    /**
     * Get all childNodes of parent passing the {@link Predicate} created by {@link SubNodesMultiHandler#createPredicateToEvaluateChildNode()} or <br>
     * with type {@link NodeTypes.ContentNode.NAME} if the {@link Predicate} is null.
     */
    protected List<Node> getStoredChildNodes(JcrNodeAdapter parent) {
        try {
            if (!(parent instanceof JcrNewNodeAdapter) && parent.getJcrItem().hasNodes()) {
                Predicate predicate = createPredicateToEvaluateChildNode();
                if (predicate != null) {
                    return NodeUtil.asList(NodeUtil.getNodes(parent.getJcrItem(), predicate));
                } else {
                    return NodeUtil.asList(NodeUtil.getNodes(parent.getJcrItem(), childNodeType));
                }
            }
        } catch (RepositoryException re) {
            log.warn("Not able to access the Child Nodes of the following Node Identifier {}", parent.getItemId(), re);
        }
        return new ArrayList<Node>();
    }

    /**
     * Only return child node that have a number name's.
     */
    protected Predicate createPredicateToEvaluateChildNode() {

        return new Predicate() {
            @Override
            public boolean evaluate(Object node) {
                if (node instanceof Node) {
                    try {
                        return ((Node) node).getName().matches("[0-9]+");
                    } catch (RepositoryException e) {
                        return false;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Return a specific value from the child node.
     */
    protected T getValueFromChildNode(Node child) {
        try {
            if (child.hasProperty(childValuePropertyName)) {
                return (T) PropertyUtil.getPropertyValueObject(child, childValuePropertyName);
            }
        } catch (RepositoryException re) {
            log.warn("Not able to access the Child Nodes property of the following Child Node Name {}", NodeUtil.getName(child), re);
        }
        return null;
    }

    /**
     * Create new Child Items based on the newValues. <br>
     * - on the rootItem, create or update childItems based on the newValues (one ChildItem per new Value).
     * - remove the no more existing child from the source Item.
     */
    @Override
    public void writeToDataSourceItem(List<T> newValue) {
        // Get root Item
        JcrNodeAdapter rootItem = getRootItem();
        rootItem.getChildren().clear();
        // Add childItems to the rootItem
        setNewChildItem(rootItem, newValue);
        // Remove all no more existing children
        detachNoMoreExistingChildren(rootItem);
        // Attach or Detach rootItem from parent
        handleRootitemAndParent(rootItem);
    }

    protected void setNewChildItem(JcrNodeAdapter rootItem, List<T> newValue) {
        // Used to build the ChildItemName;
        Set<String> childNames = new HashSet<String>();
        Node rootNode = rootItem.getJcrItem();
        try {
            for (T value : newValue) {
                // Create the child Item Name
                String childName = createChildItemName(childNames, value, rootItem);
                // Get or create the childItem
                JcrNodeAdapter childItem = initializeChildItem(rootItem, rootNode, childName);
                // Set the Value to the ChildItem
                setChildItemValue(childItem, value);
            }
        } catch (Exception e) {
            log.warn("Not able to create a Child Item for {} ", rootItem.getItemId(), e);
        }
    }

    /**
     * Set the value as property to the childItem.
     */
    protected void setChildItemValue(JcrNodeAdapter childItem, T value) {
        childItem.addItemProperty(childValuePropertyName, new DefaultProperty<T>(value));
    }

    /**
     * Create a Child Item.<br>
     * - if the related node already has a Child Node called 'childName', initialize the ChildItem based on this child Node.<br>
     * - else create a new JcrNodeAdapter.
     */
    protected JcrNodeAdapter initializeChildItem(JcrNodeAdapter rootItem, Node rootNode, String childName) throws PathNotFoundException, RepositoryException {
        JcrNodeAdapter childItem = null;
        if (!(rootItem instanceof JcrNewNodeAdapter) && rootNode.hasNode(childName)) {
            childItem = new JcrNodeAdapter(rootNode.getNode(childName));
        } else {
            childItem = new JcrNewNodeAdapter(rootNode, childNodeType, childName);
        }
        rootItem.addChild(childItem);
        return childItem;
    }

    /**
     * If values are already stored, remove the no more existing one.
     */
    private void detachNoMoreExistingChildren(JcrNodeAdapter rootItem) {
        try {
            List<Node> children = getStoredChildNodes(rootItem);
            for (Node child : children) {
                if (rootItem.getChild(child.getName()) == null) {
                    JcrNodeAdapter toRemove = new JcrNodeAdapter(child);
                    rootItem.removeChild(toRemove);
                }
            }
        } catch (RepositoryException e) {
            log.error("Could remove children", e);
        }
    }

    /**
     * Handle the relation between parent and rootItem.<br>
     * Typically, if rootItem would be a child of parentItem: <br>
     * <p>
     * if (childItem.getChildren() != null && !childItem.getChildren().isEmpty()) { ((JcrNodeAdapter) parent).addChild(childItem); } else { ((JcrNodeAdapter) parent).removeChild(childItem); }
     * </p>
     */
    protected void handleRootitemAndParent(JcrNodeAdapter rootItem) {
        // In our case, do nothing as childItem is already the parent.
    }

    /**
     * Basic Implementation that create child Nodes with increasing number as Name.
     */
    protected String createChildItemName(Set<String> childNames, Object value, JcrNodeAdapter rootItem) {
        int nb = 0;
        String name = "00";
        DecimalFormat df = new DecimalFormat("00");
        while (childNames.contains(name)) {
            nb += 1;
            name = df.format(nb);
        }
        childNames.add(name);
        return name;
    }

    public void setChildValuePropertyName(String newName) {
        this.childValuePropertyName = newName;
    }

}
