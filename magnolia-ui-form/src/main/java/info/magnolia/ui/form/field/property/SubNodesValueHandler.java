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
package info.magnolia.ui.form.field.property;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sub Nodes implementation of {@link MultiValueHandler}.<br>
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
 */
public class SubNodesValueHandler implements MultiValueHandler {

    private static final Logger log = LoggerFactory.getLogger(SubNodesValueHandler.class);
    private JcrNodeAdapter root;
    private String subNodeName;
    private int valueItemNameSize = 20;

    @Inject
    public SubNodesValueHandler(JcrNodeAdapter root, String subNodeName) {
        this.root = root;
        this.subNodeName = subNodeName;
    }

    /**
     * Create a new set of valueItems.<br>
     * - from the 'root' get or create childItem (childItem will contains the list of valueItem) <br>
     * - if the childItem has already a set of valueItem, remove them. <br>
     * - for every newValue element, create a valueItem linked to childItem.
     */
    @Override
    public void setValue(List<String> newValue) {
        // Get the child Item that contains the list of children Item Value
        JcrNodeAdapter childItem = getOrCreateChildNode();
        // Remove all current children Item Value
        detachChildren(childItem);
        // Get the child Node
        Node childNode = childItem.getJcrItem();
        for (String value : newValue) {
            // For each value, create a Child Item Value
            createAndAddValueItem(childItem, childNode, value);
        }
        // Attach the child item to the root item
        if (childItem.getChildren() != null && !childItem.getChildren().isEmpty()) {
            root.addChild(childItem);
        } else {
            root.removeChild(childItem);
        }
    }

    @Override
    public List<String> getValue() {
        LinkedList<String> res = new LinkedList<String>();
        try {
            // Get the child Item that contains the list of children Item Value
            JcrNodeAdapter childItem = getOrCreateChildNode();
            // Get the child Node
            Node childNode = childItem.getJcrItem();
            if (!(childItem instanceof JcrNewNodeAdapter) && childNode.hasNodes()) {
                NodeIterator iterator = childNode.getNodes();
                while (iterator.hasNext()) {
                    Node valueNode = iterator.nextNode();
                    if (valueNode.hasProperty(subNodeName)) {
                        res.add(valueNode.getProperty(subNodeName).getString());
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Could get or create related items", e);
        }
        return res;
    }

    /**
     * Create a valueItem. <br>
     * Note that the <b>valueItem name is equal to the 20 first chars of the related value</b>.
     */
    @SuppressWarnings("unchecked")
    private void createAndAddValueItem(JcrNodeAdapter childItem, Node childNode, String value) {
        JcrNodeAdapter valueItem = new JcrNewNodeAdapter(childNode, NodeTypes.Content.NAME, StringUtils.left(value, valueItemNameSize));
        childItem.addChild(valueItem);
        valueItem.addItemProperty(subNodeName, new DefaultProperty(String.class, value));
    }

    private void detachChildren(JcrNodeAdapter childItem) {
        try {
            Node childNode = childItem.getJcrItem();
            if (!(childItem instanceof JcrNewNodeAdapter) && childNode.hasNodes()) {
                NodeIterator iterator = childNode.getNodes();
                while (iterator.hasNext()) {
                    JcrNodeAdapter toRemove = new JcrNodeAdapter(iterator.nextNode());
                    childItem.removeChild(toRemove);
                }
            }
        } catch (RepositoryException e) {
            log.error("Could remove children", e);
        }
    }

    /**
     * Retrieve or create the Child Node containing the values.
     */
    private JcrNodeAdapter getOrCreateChildNode() {
        JcrNodeAdapter child = null;
        try {
            Node node = root.getJcrItem();
            if (node.hasNode(subNodeName) && !(root instanceof JcrNewNodeAdapter)) {
                child = new JcrNodeAdapter(node.getNode(subNodeName));
                child.setParent(root);
            } else {
                child = new JcrNewNodeAdapter(node, NodeTypes.Content.NAME, subNodeName);
                child.setParent(root);
            }
        } catch (RepositoryException e) {
            log.error("Could get or create item", e);
        }
        return child;
    }

}
