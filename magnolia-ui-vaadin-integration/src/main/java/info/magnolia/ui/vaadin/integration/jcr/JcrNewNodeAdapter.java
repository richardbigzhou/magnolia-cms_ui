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
package info.magnolia.ui.vaadin.integration.jcr;

import info.magnolia.cms.core.Path;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.ui.api.ModelConstants;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;

/**
 * Used to create a new Node based on an Vaadin Item. This node adapter uses the parent node to
 * initialize the global properties (workspace, path....). No references is made to an existing JCR
 * node (except for the parent of the node to create).
 */
public class JcrNewNodeAdapter extends JcrNodeAdapter {

    private static final Logger log = LoggerFactory.getLogger(JcrNewNodeAdapter.class);

    /**
     * @param parentNode Parent of the node to create.
     * @param nodeType Type node to create.
     */
    public JcrNewNodeAdapter(Node parentNode, String nodeType) {
        this(parentNode, nodeType, null);
    }

    /**
     * @param parentNode Parent of the node to create.
     * @param nodeType Type node to create.
     * @param nodeName Name of the new node.
     */
    public JcrNewNodeAdapter(Node parentNode, String nodeType, String nodeName) {
        super(parentNode);
        setPrimaryNodeTypeName(nodeType);
        setNodeName(nodeName);
    }

    /**
     * Create pure Vaadin Property fully decoupled for Jcr.
     */
    @Override
    public Property getItemProperty(Object propertyId) {
        DefaultProperty property = null;

        if (getChangedProperties().containsKey(propertyId)) {
            property = (DefaultProperty) getChangedProperties().get(propertyId);
        }

        return property;
    }

    /**
     * Create a new subNode of the parent Node or return the existing one if already created. In
     * case of exception return null.
     */
    @Override
    public Node getNode() {
        try {
            if (getNodeName() != null && getParentNode().hasNode(getNodeName())) {
                return getParentNode().getNode(getNodeName());
            }
        } catch (RepositoryException re) {
            log.warn("Exception during access of the newly created node " + getNodeName(), re);
            return null;
        }

        return createNode();
    }

    /**
     * Return the parent Node.
     */
    public Node getParentNode() {
        return (Node) getJcrItem();
    }

    /**
     * Create a new node linked o the parent node used to initialized this NewNodeAdapter.
     */
    private Node createNode() {
        Node node = null;
        try {
            Node parent = getParentNode();

            // Create a Node Name if not defined
            if (StringUtils.isBlank(getNodeName())) {
                setNodeName(getUniqueNewItemName(parent));
            }

            node = parent.addNode(getNodeName(), getPrimaryNodeTypeName());

            log.debug("create a new node for parent " + parent.getPath() + " with nodeId " + getNodeName());
            // Update property
            updateProperties(node);
            // Update child nodes
            if (!getChildren().isEmpty()) {
                for (JcrItemNodeAdapter child : getChildren().values()) {
                    if (child instanceof JcrNewNodeAdapter) {
                        // Set parent node (parent could be newly created)
                        ((AbstractJcrAdapter) child).initCommonAttributes(node);
                    }
                    child.getNode();
                }
            }

            return node;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * Create a new unique nodeName. If JCR_NAME if part of the properties, use this property as
     * desired nodeName.
     */
    private String getUniqueNewItemName(final Item item) throws RepositoryException, ItemNotFoundException, AccessDeniedException {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        String nodeName = "";
        if (getChangedProperties().containsKey(ModelConstants.JCR_NAME)) {
            nodeName = getChangedProperties().get(ModelConstants.JCR_NAME).toString();
            getChangedProperties().remove(ModelConstants.JCR_NAME);
            nodeName = StringUtils.isNotBlank(nodeName) ? nodeName : "untitled";
            nodeName = Path.getValidatedLabel(nodeName);
        }

        return Path.getUniqueLabel(item.getSession(), item.getPath(), nodeName);
    }
}