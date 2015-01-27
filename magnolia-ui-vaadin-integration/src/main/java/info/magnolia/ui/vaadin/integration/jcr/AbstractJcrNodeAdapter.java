/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;

/**
 * Abstract implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Node}. Implements {Property.ValueChangeListener} in order to inform/change JCR
 * property when a Vaadin property has changed. Access JCR repository for all read Jcr Property.
 */
public abstract class AbstractJcrNodeAdapter extends AbstractJcrAdapter {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrNodeAdapter.class);

    private String primaryNodeType;

    private final Map<String, AbstractJcrNodeAdapter> children = new LinkedHashMap<String, AbstractJcrNodeAdapter>();

    private final Map<String, AbstractJcrNodeAdapter> removedChildren = new HashMap<String, AbstractJcrNodeAdapter>();

    private AbstractJcrNodeAdapter parent;

    private String nodeName;

    private boolean childItemChanges = false;

    public AbstractJcrNodeAdapter(Node jcrNode) {
        super(jcrNode);
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    protected void initCommonAttributes(Item jcrItem) {
        super.initCommonAttributes(jcrItem);
        Node node = (Node) jcrItem;
        try {
            if (StringUtils.isBlank(primaryNodeType)) {
                primaryNodeType = node.getPrimaryNodeType().getName();
            }
        } catch (RepositoryException e) {
            log.error("Could not determine primaryNodeType name of JCR node", e);
            primaryNodeType = UNIDENTIFIED;
        }
    }

    protected void setPrimaryNodeTypeName(String primaryNodeTypeName) {
        this.primaryNodeType = primaryNodeTypeName;
    }

    /**
     * Return the Primary node type Name. This Node type is defined based on the related JCR Node.
     * In case of new Node, the Type is passed during the construction of the new Item or if not
     * defined, the Type is equivalent to the Parent Node Type.
     */
    public String getPrimaryNodeTypeName() {
        return primaryNodeType;
    }

    protected Map<String, AbstractJcrNodeAdapter> getRemovedChildren() {
        return removedChildren;
    }

    /**
     * Return the corresponding node directly from the JCR repository. <b> The returned Node does
     * not contains all changes done on the current Item, but it's a representation of the current
     * stored Jcr node. </b> To get the Jcr Node including the changes done on the current Item, use
     * applyChanges().
     */
    @Override
    public Node getJcrItem() {
        return (Node)super.getJcrItem();
    }

    /**
     * Add a new JCR Property.
     */
    @Override
    public boolean addItemProperty(Object id, Property property) {
        // REMOVE ME: Never called as overrides by sub class.
        log.debug("Add new Property Item name " + id + " with value " + property.getValue());
        try {
            Node node = getJcrItem();
            String propertyName = (String) id;
            if (!node.hasProperty(propertyName)) {
                // Create Property.
                node.setProperty(propertyName, (String) property.getValue());
                getChangedProperties().put((String)id, property);
                return true;
            } else {
                log.warn("Property " + id + " already exist.");
                return false;
            }
        } catch (RepositoryException e) {
            log.error("Unable to add JCR property", e);
            return false;
        }
    }

    /**
     * @return the property if it already exist on the JCR Item a new property if this property
     *         refer to the JCR Node name null if the property doesn't exist yet.
     */
    @Override
    public Property getItemProperty(Object id) {
        Object value;
        Class type = String.class;
        try {
            final Node jcrNode = getJcrItem();
            if (!jcrNode.hasProperty((String) id)) {
                if (ModelConstants.JCR_NAME.equals(id)) {
                    value = jcrNode.getName();
                } else {
                    return null;
                }
            } else {
                value = PropertyUtil.getPropertyValueObject(jcrNode, String.valueOf(id));
                type = value.getClass();
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        DefaultProperty property = new DefaultProperty(type, value);
        getChangedProperties().put((String) id, property);
        return property;
    }

    /**
     * Returns the JCR Node represented by this Item with changes applied. Updates both properties and child nodes. Will
     * create new properties, set new values and remove those requested for removal. Child nodes will also be added,
     * updated or removed.
     */
    @Override
    public Node applyChanges() throws RepositoryException {
        // get Node from repository
        Node node = getJcrItem();

        // Update Node properties and children
        updateProperties(node);
        updateChildren(node);

        return node;
    }

    /**
     * Updates and removes children based on the {@link #children} and {@link #removedChildren} maps.
     *
     * TODO: Has been made public as of MGNLUI-3124 resolution. Needs further API improvement (e.g. no-arg version or possibly some other better way to update the JCR node internals).
     */
    public void updateChildren(Node node) throws RepositoryException {
        if (!children.isEmpty()) {
            for (AbstractJcrNodeAdapter child : children.values()) {
                // Update child node as well
                child.applyChanges();
            }
        }
        // Remove child node if needed
        if (!removedChildren.isEmpty()) {
            for (AbstractJcrNodeAdapter removedChild : removedChildren.values()) {
                if (node.hasNode(removedChild.getNodeName())) {
                    node.getNode(removedChild.getNodeName()).remove();
                }
            }
        }
    }

    @Override
    public void updateProperties(Item item) throws RepositoryException {
        super.updateProperties(item);
        if (item instanceof Node) {
            // Remove Properties
            Node node = (Node) item;
            for (Entry<String, Property> entry : getRemovedProperties().entrySet()) {
                if (node.hasProperty(entry.getKey())) {
                    node.getProperty(entry.getKey()).remove();
                }
            }
            getRemovedProperties().clear();
        }
    }

    /**
     * Update or remove property. Property with flag saveInfo to false will not be updated. Property
     * can refer to node property (like name, title) or node.MetaData property like
     * (MetaData/template). Also handle the specific case of node renaming. If property JCR_NAME is
     * present, Rename the node.
     * In case the value has changed to null, it will be removed. When being called from {@link #updateProperties}
     * we have to make sure it is removed before running in here as {@link #removeItemProperty(java.lang.Object)}
     * is manipulating the {@link #changedProperties} list directly.
     */
    @Override
    protected void updateProperty(Item item, String propertyId, Property property) {
        if (!(item instanceof Node)) {
            return;
        }
        Node node = (Node) item;
        if (ModelConstants.JCR_NAME.equals(propertyId)) {
            String jcrName = (String) property.getValue();
            try {
                if (jcrName != null && !jcrName.isEmpty() && !jcrName.equals(node.getName())) {

                    // make sure new path is available
                    jcrName = Path.getValidatedLabel(jcrName);
                    jcrName = Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), jcrName);

                    NodeUtil.renameNode(node, jcrName);

                    setItemId(JcrItemUtil.getItemId(node));
                }
            } catch (RepositoryException e) {
                log.error("Could not rename JCR Node.", e);
            }
        } else if (propertyId != null && !propertyId.isEmpty()) {
            if (property.getValue() != null) {
                try {
                    PropertyUtil.setProperty(node, propertyId, property.getValue());
                } catch (RepositoryException e) {
                    log.error("Could not set JCR Property {}", propertyId, e);
                }
            } else {
                removeItemProperty(propertyId);
                log.debug("Property '{}' has a null value: Will be removed", propertyId);
            }
        }
    }

    /**
     * @param nodeName name of the child node
     * @return child if part of the children, or null if not defined.
     */
    public AbstractJcrNodeAdapter getChild(String nodeName) {
        return children.get(nodeName);
    }

    public Map<String, AbstractJcrNodeAdapter> getChildren() {
        return children;
    }

    /**
     * Add a child adapter to the current Item. <b>Only Child Nodes part of this Map will
     * be persisted into Jcr.</b>
     */
    public AbstractJcrNodeAdapter addChild(AbstractJcrNodeAdapter child) {
        childItemChanges = true;
        if (removedChildren.containsKey(child.getNodeName())) {
            removedChildren.remove(child.getNodeName());
        }
        child.setParent(this);
        if (children.containsKey(child.getNodeName())) {
            children.remove(child.getNodeName());
        }
        return children.put(child.getNodeName(), child);
    }

    /**
     * Remove a child Node from the child list. <b>When removing an JcrNodeAdapter, this child
     * will be added to the Remove Child List even if this Item was not part of the current children
     * list. All Item part from the removed list are removed from the Jcr repository.</b>
     */
    public boolean removeChild(AbstractJcrNodeAdapter toRemove) {
        childItemChanges = true;
        removedChildren.put(toRemove.getNodeName(), toRemove);
        return children.remove(toRemove.getNodeName()) != null;
    }

    /**
     * Return the current Parent Item (If Item is a child). Parent is set by calling addChild(...
     */
    public AbstractJcrNodeAdapter getParent() {
        return parent;
    }

    public void setParent(AbstractJcrNodeAdapter parent) {
        this.parent = parent;
    }

    /**
     * Return the current Node Name. For new Item, this is the name set in the new Item constructor
     * or null if not yet defined.
     */
    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * @return true if an {@link com.vaadin.data.Item} was added or removed, false otherwise.
     */
    public boolean hasChildItemChanges() {
        return childItemChanges;
    }
}
