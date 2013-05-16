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
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.api.ModelConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

/**
 * Abstract implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Node}. Implements {Property.ValueChangeListener} in order to inform/change JCR
 * property when a Vaadin property has changed. Access JCR repository for all read Jcr Property.
 */
public abstract class AbstractJcrNodeAdapter extends AbstractJcrAdapter implements JcrItemNodeAdapter {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrNodeAdapter.class);

    private String nodeIdentifier;

    private String primaryNodeType;

    private final Map<String, JcrItemNodeAdapter> children = new HashMap<String, JcrItemNodeAdapter>();

    private final Map<String, JcrItemNodeAdapter> removedChildren = new HashMap<String, JcrItemNodeAdapter>();

    private JcrItemNodeAdapter parent;

    private String nodeName = null;

    public AbstractJcrNodeAdapter(Node jcrNode) {
        super(jcrNode);
    }

    @Override
    protected void initCommonAttributes(Item jcrItem) {
        super.initCommonAttributes(jcrItem);
        Node node = (Node) jcrItem;
        try {
            nodeIdentifier = node.getIdentifier();
            if (StringUtils.isBlank(primaryNodeType)) {
                primaryNodeType = node.getPrimaryNodeType().getName();
            }
        } catch (RepositoryException e) {
            log.error("Could not retrieve identifier or primaryNodeType name of JCR Node.", e);
            nodeIdentifier = UNIDENTIFIED;
            primaryNodeType = UNIDENTIFIED;
        }
    }

    @Override
    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    protected void setPrimaryNodeTypeName(String primaryNodeTypeName) {
        this.primaryNodeType = primaryNodeTypeName;
    }

    @Override
    public String getPrimaryNodeTypeName() {
        return primaryNodeType;
    }

    protected Map<String, JcrItemNodeAdapter> getRemovedChildren() {
        return removedChildren;
    }

    /**
     * @return Corresponding node or null if not existing.
     */
    @Override
    public Node getNodeFromRepository() {
        return (Node) getJcrItem();
    }

    /**
     * Add a new JCR Property.
     */
    @Override
    public boolean addItemProperty(Object id, Property property) {
        // add PropertyChange Listener
        addListenerIfNotYetSet(property, this);

        log.debug("Add new Property Item name " + id + " with value " + property.getValue());
        try {
            if (!getNodeFromRepository().hasProperty((String) id)) {
                // Create Property.
                getNodeFromRepository().setProperty((String) id, (String) property.getValue());
                return true;
            } else {
                log.warn("Property " + id + " already exist.do nothing");
                return false;
            }
        } catch (RepositoryException e) {
            log.error("", e);
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
        int type = PropertyType.STRING;
        try {
            final Node jcrNode = getNodeFromRepository();
            if (!jcrNode.hasProperty((String) id)) {
                if (ModelConstants.JCR_NAME.equals(id)) {
                    value = jcrNode.getName();
                } else {
                    return null;
                }
            } else {
                javax.jcr.Property property = jcrNode.getProperty(String.valueOf(id));
                value = PropertyUtil.getValueObject(property.getValue());
                type = property.getType();
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        DefaultProperty property = DefaultPropertyUtil.newDefaultProperty((String) id, type, value);
        // add PropertyChange Listener
        property.addValueChangeListener(this);
        getChangedProperties().put((String) id, property);
        return property;
    }

    /**
     * Add a {(ValueChangeEvent}. listener if not yet set.
     */
    protected void addListenerIfNotYetSet(Property property, Property.ValueChangeListener listener) {
        // add PropertyChange Listener
        if (!((DefaultProperty) property).getListeners(ValueChangeEvent.class).contains(listener)) {
            ((DefaultProperty) property).addValueChangeListener(listener);
        }
    }

    /**
     * Gets the JCR Node and updates its properties and children. Update will create new properties,
     * set new values and remove those requested for removal. Children will also be added, updated
     * or removed.
     */
    @Override
    public Node getNode() {
        Node node = null;
        try {
            // get Node from repository
            node = (Node) getJcrItem();

            // Update Node properties and children
            updateProperties(node);
            updateChildren(node);

        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return node;
    }

    /**
     * Updates and removes children based on the {@link #children} and {@link #removedChildren} maps.
     */
    private void updateChildren(Node node) throws RepositoryException {
        if (!children.isEmpty()) {
            for (JcrItemNodeAdapter child : children.values()) {
                // Update child node as well
                child.getNode();
            }
        }
        // Remove child node if needed
        if (!removedChildren.isEmpty()) {
            for (JcrItemNodeAdapter removedChild : removedChildren.values()) {
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

                    // make sure new path is clear
                    jcrName = Path.getValidatedLabel(jcrName);
                    jcrName = Path.getUniqueLabel(node.getSession(), node.getParent().getPath(), jcrName);
                    String newPath = NodeUtil.combinePathAndName(node.getParent().getPath(), jcrName);
                    node.getSession().move(node.getPath(), newPath);
                    setPath(node.getPath());
                }
            } catch (RepositoryException e) {
                    log.error("Could not rename JCR Node.", e);
                }
        } else if (propertyId != null && !propertyId.isEmpty()) {
            if (property.getValue() != null && StringUtils.isNotEmpty(property.getValue().toString())) {
                try {
                    if (!node.hasProperty(propertyId)) {
                        addListenerIfNotYetSet(property, this);
                    }
                    PropertyUtil.setProperty(node, propertyId, property.getValue());
                } catch (RepositoryException e) {
                    log.error("Could not set JCR Property", e);
                }
            } else {
                removeItemProperty(propertyId);
                log.debug("Property '{}' has a null value: Will be removed", propertyId);
            }
        }
    }

    @Override
    public JcrItemNodeAdapter getChild(String id) {
        return children.get(id);
    }

    @Override
    public Map<String, JcrItemNodeAdapter> getChildren() {
        return children;
    }

    /**
     * Add a Item in the child list. Remove this item from the removedChildren list if present.
     */
    @Override
    public JcrItemNodeAdapter addChild(JcrItemNodeAdapter child) {
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
     * Add the removed child in the removedChildren map.
     */
    @Override
    public boolean removeChild(JcrItemNodeAdapter toRemove) {
        removedChildren.put(toRemove.getNodeName(), toRemove);
        return children.remove(toRemove.getNodeName()) != null;
    }

    /**
     * Return the parent Item or Null if not yet set.
     */
    @Override
    public JcrItemNodeAdapter getParent() {
        return parent;
    }

    @Override
    public void setParent(JcrItemNodeAdapter parent) {
        this.parent = parent;
    }

    @Override
    public String getNodeName() {
        return this.nodeName;
    }

    protected void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}
