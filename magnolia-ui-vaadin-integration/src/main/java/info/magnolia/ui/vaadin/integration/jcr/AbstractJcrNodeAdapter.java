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

import info.magnolia.cms.core.MetaData;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;


/**
 * Abstract implementation of an {@link com.vaadin.data.Item} wrapping/representing a
 * {@link javax.jcr.Node}. Implements {Property.ValueChangeListener} in order to inform/change JCR
 * property when a Vaadin property has changed. Access JCR repository for all read Jcr Property.
 */
public abstract class AbstractJcrNodeAdapter extends AbstractJcrAdapter implements Property.ValueChangeListener, JcrItemNodeAdapter {

    // Init
    private static final Logger log = LoggerFactory.getLogger(AbstractJcrNodeAdapter.class);

    private String primaryNodeTypeName;

    private final Map<String, Property> changedProperties = new HashMap<String, Property>();

    private final Map<String, Property> removedProperties = new HashMap<String, Property>();

    private final Map<String, JcrItemNodeAdapter> children = new HashMap<String, JcrItemNodeAdapter>();

    private final Map<String, JcrItemNodeAdapter> removedChildren = new HashMap<String, JcrItemNodeAdapter>();

    private JcrItemNodeAdapter parent;

    private String nodeName = null;

    public AbstractJcrNodeAdapter(Node jcrNode) {
        super(jcrNode);
        setPrimaryNodeTypeName(jcrNode);
    }

    /**
     * Set propertyNodeType.
     */
    private void setPrimaryNodeTypeName(Node jcrNode) {
        String primaryNodeTypeName = null;
        try {
            primaryNodeTypeName = jcrNode.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr node", e);
            primaryNodeTypeName = UN_IDENTIFIED;
        }
        this.primaryNodeTypeName = primaryNodeTypeName;
    }

    protected void setPrimaryNodeTypeName(String primaryNodeTypeName) {
        this.primaryNodeTypeName = primaryNodeTypeName;
    }

    @Override
    public String getPrimaryNodeTypeName() {
        return primaryNodeTypeName;
    }

    protected Map<String, Property> getChangedProperties() {
        return changedProperties;
    }

    protected Map<String, Property> getRemovedProperties() {
        return removedProperties;
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
            }
            else {
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
     * refer to the JCR Node name null if the property doesn't exist yet.
     */
    @Override
    public Property getItemProperty(Object id) {
        Object value;
        try {
            final Node jcrNode = getNodeFromRepository();
            if (!jcrNode.hasProperty((String) id)) {
                if (JCR_NAME.equals(id)) {
                    value = jcrNode.getName();
                }
                else {
                    return null;
                }
            }
            else {
                value = PropertyUtil.getPropertyValueObject(jcrNode, (String) id);
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        DefaultProperty property = new DefaultProperty((String) id, value);
        // add PropertyChange Listener
        property.addListener(this);
        return property;
    }

    /**
     * Listener to DefaultProperty value change event. Get this event when a property has changed,
     * and propagate this Vaadin Property value change to the corresponding JCR property.
     */
    @Override
    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property instanceof DefaultProperty) {
            String name = ((DefaultProperty) property).getPropertyName();
            Object value = property.getValue();
            try {
                if (getNodeFromRepository().hasProperty(name)) {
                    log.debug("Update existing property: {} with value: {}.", name, value);
                    PropertyUtil.getProperty(getNodeFromRepository(), name).setValue((String) value);
                }
                else {
                    addItemProperty(name, property);
                }
            } catch (RepositoryException e) {
                log.error("", e);
            }
        }
    }

    /**
     * Add a {(ValueChangeEvent}. listener if not yet set.
     */
    protected void addListenerIfNotYetSet(Property property, Property.ValueChangeListener listener) {
        // add PropertyChange Listener
        if (!((DefaultProperty) property).getListeners(ValueChangeEvent.class).contains(listener)) {
            ((DefaultProperty) property).addListener(listener);
        }
    }

    /**
     * Get the referenced node and update the property. Update property will: remove existing JCR
     * property if requested add newly and setted property update existing modified property. In
     * addition defined children nodes are updated or removed.
     */
    @Override
    public Node getNode() {
        Node node = null;
        try {
            node = getNodeFromRepository();
            // Update Node property
            updateProperty(node);
            // Update Child Nodes
            if (!children.isEmpty()) {
                for (JcrItemNodeAdapter child : children.values()) {
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
            return node;
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    /**
     * Update or remove property. Property wit flag saveInfo to false will not be updated. Property
     * can refer to node property (like name, title) or node.MetaData property like
     * (MetaData/template). Also handle the specific case of node renaming. If property JCR_NAME is
     * present, Rename the node.
     */
    protected void updateProperty(Node node) throws RepositoryException {
        // Update property
        for (Entry<String, Property> entry : changedProperties.entrySet()) {
            // Check saveInfo Flag
            if (entry.getValue().isReadOnly()) {
                continue;
            }
            // JCRNAME has change --> perform the renaming and continue
            if (entry.getKey().equals(JCR_NAME) && (entry.getValue() != null && !entry.getValue().toString().isEmpty())) {
                node.getSession().move(
                    node.getPath(),
                    NodeUtil.combinePathAndName(node.getParent().getPath(), entry.getValue().getValue().toString()));
                setPath(node.getPath());
                continue;
            }
            // Check if the field is refereeing to MetaData Property
            if (entry.getKey().startsWith(MetaData.DEFAULT_META_NODE)) {
                PropertyUtil.setProperty(
                    node.getNode(MetaData.DEFAULT_META_NODE),
                    StringUtils.removeStart(entry.getKey(), MetaData.DEFAULT_META_NODE + "/"),
                    entry.getValue().getValue());
            }
            else if (entry.getValue().getValue() != null) {
                PropertyUtil.setProperty(node, entry.getKey(), entry.getValue().getValue());
            }
        }
        // Remove Property
        for (Entry<String, Property> entry : removedProperties.entrySet()) {
            if (node.hasProperty(entry.getKey())) {
                node.getProperty(entry.getKey()).remove();
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
