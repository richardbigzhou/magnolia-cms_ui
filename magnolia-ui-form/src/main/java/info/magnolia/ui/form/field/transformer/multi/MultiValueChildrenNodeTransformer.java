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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.NullItem;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.commons.predicate.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * Sub Nodes implementation of {@link info.magnolia.ui.form.field.transformer.Transformer} storing and retrieving properties (as {@link PropertysetItem}) displayed in MultiField.<br>
 * Storage strategy: <br>
 * - root node (relatedFormItem)<br>
 * -- child node 1 (used to store the first value of the MultiField as a property)<br>
 * --- property1 (store the first value of the MultiField)<br>
 * -- child node 2 (used to store the second value of the MultiField as a property)<br>
 * --- property2 (store the second value of the MultiField)<br>
 * ...<br>
 * Each element of the MultiField is stored in a property located in a child node of the root node. <br>
 * Child node name : Incremental number (00, 01,....) <br>
 * Property name : field name <br>
 * Override {@link MultiValueChildrenNodeTransformer#createChildItemName(Set, Object, JcrNodeAdapter)} to define the child node name.<br>
 * Override {@link MultiValueChildrenNodeTransformer#setChildValuePropertyName(String)} to change the property name used to store the MultiField value element.
 */
public class MultiValueChildrenNodeTransformer extends BasicTransformer<PropertysetItem> {

    private static final Logger log = LoggerFactory.getLogger(MultiValueChildrenNodeTransformer.class);

    protected String childNodeType = NodeTypes.ContentNode.NAME;
    private String childValuePropertyName;

    public MultiValueChildrenNodeTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
        super(relatedFormItem, definition, type);
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
     * - for each childNode retrieve the value to set to the {@link PropertysetItem} <br>
     * If no childNodes are present, return an empty {@link PropertysetItem}.
     */
    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem newValues = new PropertysetItem();
        JcrNodeAdapter rootItem = getRootItem();
        // Get a list of childNodes
        List<Node> childNodes = getStoredChildNodes(rootItem);
        int position = 0;
        for (Node child : childNodes) {
            Object value = getValueFromChildNode(child);
            if (value != null) {
                newValues.addItemProperty(position, new DefaultProperty(value));
                position += 1;
            }
        }
        return newValues;
    }


    /**
     * Create new Child Items based on the newValues. <br>
     * - on the rootItem, create or update childItems based on the newValues (one ChildItem per new Value).
     * - remove the no more existing child from the source Item.
     */
    @Override
    public void writeToItem(PropertysetItem newValue) {
        // Get root Item
        JcrNodeAdapter rootItem = getRootItem();
        if (rootItem == null) {
            // nothing to write yet, someone just clicked add and then clicked away to other field
            return;
        }
        rootItem.getChildren().clear();
        // Add childItems to the rootItem
        setNewChildItem(rootItem, newValue);
        // Remove all no more existing children
        detachNoMoreExistingChildren(rootItem);
        // Attach or Detach rootItem from parent
        handleRootitemAndParent(rootItem);
    }

    /**
     * Define the root Item used in order to set the SubNodes list.
     */
    protected JcrNodeAdapter getRootItem() {
        return (JcrNodeAdapter) relatedFormItem;
    }

    /**
     * Get all childNodes of parent passing the {@link Predicate} created by {@link MultiValueChildrenNodeTransformer#createPredicateToEvaluateChildNode()} or <br>
     * with type {@link NodeTypes.ContentNode.NAME} if the {@link Predicate} is null.
     */
    protected List<Node> getStoredChildNodes(JcrNodeAdapter parent) {
        List<Node> res = new ArrayList<Node>();
        try {
            if (parent != null && !(parent instanceof JcrNewNodeAdapter) && parent.getJcrItem().hasNodes()) {
                Predicate predicate = createPredicateToEvaluateChildNode();
                if (predicate != null) {
                    res = NodeUtil.asList(NodeUtil.getNodes(parent.getJcrItem(), predicate));
                } else {
                    res = NodeUtil.asList(NodeUtil.getNodes(parent.getJcrItem(), childNodeType));
                }
            }
        } catch (RepositoryException re) {
            log.warn("Not able to access the Child Nodes of the following Node Identifier {}", parent.getItemId(), re);
        }
        return res;
    }

    /**
     * Create a {@link Predicate} used to evaluate the child node of the root to handle.<br>
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
    protected Object getValueFromChildNode(Node child) {
        try {
            if (child.hasProperty(childValuePropertyName)) {
                return PropertyUtil.getPropertyValueObject(child, childValuePropertyName);
            }
        } catch (RepositoryException re) {
            log.warn("Not able to access the Child Nodes property of the following Child Node Name {}", NodeUtil.getName(child), re);
        }
        return null;
    }


    protected void setNewChildItem(JcrNodeAdapter rootItem, PropertysetItem newValue) {
        // Used to build the ChildItemName;
        Set<String> childNames = new HashSet<String>();
        Node rootNode = rootItem.getJcrItem();
        try {
            Iterator<?> it = newValue.getItemPropertyIds().iterator();
            while (it.hasNext()) {
                Property<?> p = newValue.getItemProperty(it.next());
                // Do not handle null values
                if (p == null || p.getValue() == null) {
                    continue;
                }
                Object value = p.getValue();
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
    protected void setChildItemValue(JcrNodeAdapter childItem, Object value) {
        childItem.addItemProperty(childValuePropertyName, new DefaultProperty(value));
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
                } else {
                    detachNoMoreExistingChildren((JcrNodeAdapter) rootItem.getChild(child.getName()));
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

    /**
     * Retrieve or create a child node as {@link JcrNodeAdapter}. Method will return null for any none JcrNodeAdapter releated form items.
     */
    protected JcrNodeAdapter getOrCreateChildNode(String childNodeName, String childNodeType) throws RepositoryException {
        JcrNodeAdapter child = null;
        if (relatedFormItem instanceof NullItem) {
            return null;
        }
        if (!(relatedFormItem instanceof JcrNodeAdapter)) {
            log.warn("Detected attempt to retrieve a Jcr Item from a Non Jcr Item Adapter. Will return null.");
            return null;
        }
        Node node = ((JcrNodeAdapter) relatedFormItem).getJcrItem();
        if (node.hasNode(childNodeName) && !(relatedFormItem instanceof JcrNewNodeAdapter)) {
            child = new JcrNodeAdapter(node.getNode(childNodeName));
            child.setParent(((JcrNodeAdapter) relatedFormItem));
        } else {
            child = new JcrNewNodeAdapter(node, childNodeType, childNodeName);
            child.setParent(((JcrNodeAdapter) relatedFormItem));
        }
        return child;
    }
}
