/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.ModelConstants;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;

/**
 * Used to create a new Node based on an Vaadin Item. This node adapter uses the parent node to
 * initialize the global properties (workspace, path....). No references is made to an existing JCR
 * node (except for the parent of the node to create). However, after applying changes to the
 * item, the reference will be held to the newly created node.
 */
public class JcrNewNodeAdapter extends JcrNodeAdapter {

    private static final Logger log = LoggerFactory.getLogger(JcrNewNodeAdapter.class);
    /**
     * Whether changes were already applied to the node.
     */
    private boolean appliedChanges = false;

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
     * Returns item property of a new node.
     */
    @Override
    public Property getItemProperty(Object propertyId) {
        // If changes were already applied, behave like a JcrNodeAdapter
        if (appliedChanges) {
            return super.getItemProperty(propertyId);
        }
        return getChangedProperties().get(propertyId);
    }

    /**
     * Create a new subNode of the parent Node or return the existing one if already created.
     *
     * If called a second time, apply changes of {@link JcrNodeAdapter} will be called.
     */
    @Override
    public Node applyChanges() throws RepositoryException {
        // If changes were already applied, behave like a JcrNodeAdapter
        if (appliedChanges) {
            return super.applyChanges();
        }

        Node parent = getJcrItem();

        // Create a Node Name if not defined
        if (StringUtils.isBlank(getNodeName())) {
            setNodeName(getUniqueNewItemName(parent));
        }

        // Create the new node
        Node node = parent.addNode(getNodeName(), getPrimaryNodeTypeName());
        log.debug("create a new node for parent " + parent.getPath() + " with name " + getNodeName());

        // set mgnl:created & mgnl:createdBy
        NodeTypes.Created.set(node);
        // Update properties
        updateProperties(node);

        // Update child nodes
        if (!getChildren().isEmpty()) {
            for (AbstractJcrNodeAdapter child : getChildren().values()) {
                if (child instanceof JcrNewNodeAdapter) {
                    // Set parent node (parent could be newly created)
                    child.initCommonAttributes(node);
                }
                child.applyChanges();
            }
        }

        // Update itemId to new node
        setItemId(node.getIdentifier());
        // Update parent
        if (!appliedChanges) {
            setParent(new JcrNodeAdapter(parent));
        }

        appliedChanges = true;

        return node;
    }

    /**
     * Create a new unique nodeName. If JCR_NAME if part of the properties, use this property as
     * desired nodeName.
     */
    private String getUniqueNewItemName(final Item item) throws RepositoryException {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        String nodeName = "";
        if (getChangedProperties().containsKey(ModelConstants.JCR_NAME)) {
            nodeName = getChangedProperties().get(ModelConstants.JCR_NAME).toString();
            getChangedProperties().remove(ModelConstants.JCR_NAME);
            nodeName = Path.getValidatedLabel(nodeName);
        }

        return Path.getUniqueLabel(item.getSession(), item.getPath(), nodeName);
    }
}
