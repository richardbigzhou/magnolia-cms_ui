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

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.PropertyUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

/**
 * Abstract implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Node}.
 * Implements {Property.ValueChangeListener} in order to inform/change JCR property when a
 * Vaadim property has changed.
 * Access JCR repository for all read Jcr Property.
 */
public abstract class JcrAbstractNodeAdapter extends JcrAbstractAdapter implements  Property.ValueChangeListener {
    // Init
    private static final Logger log = LoggerFactory.getLogger(JcrAbstractNodeAdapter.class);
    private String primaryNodeTypeName;


    public JcrAbstractNodeAdapter(Node jcrNode) {
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

    public String getPrimaryNodeTypeName() {
        return primaryNodeTypeName;
    }

    /**
     * @return: Corresponding node or null if not existing.
     */
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

        log.debug("Add new Property Item name "+id+" with value "+property.getValue());
        try {
            if(!getNodeFromRepository().hasProperty((String) id)) {
                //Create Property.
                getNodeFromRepository().setProperty((String) id, (String)property.getValue());
                return true;
            } else {
                log.warn("Property "+id+" already exist.do nothing");
                return false;
            }
        }
        catch (RepositoryException e) {
            log.error("",e);
            return false;
        }
    }

    /**
     * @return Property: New Empty property if the related Jcr Node does not
     * yet have this property. Otherwise create a Vaadin Property based on the Jcr Property.
     */
    @Override
    public Property getItemProperty(Object id) {
        Object value;
        try {
            if(!this.getNodeFromRepository().hasProperty((String) id)) {
                value = new String("");
                if(JCR_NAME.equals(id)) {
                    value = getNodeFromRepository().getName();
                }
            } else {
                value = PropertyUtil.getProperty(getNodeFromRepository(), (String) id).getString();
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        DefaultProperty property = new DefaultProperty((String)id, value);
        // add PropertyChange Listener
        property.addListener(this);
        return property;
    }

    /**
     * Listener to DefaultProperty value change event.
     * Get this event when a property has changed, and
     * propagate this VaadimProperty value change to the corresponding
     * JCR property.
     */
    @Override
    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if(property instanceof DefaultProperty) {
            String name = ((DefaultProperty)property).getPropertyName();
            Object value = property.getValue();
            try {
                if(getNodeFromRepository().hasProperty(name)) {
                    log.debug("Update existing propertie: "+name+ " with value: "+value);
                    PropertyUtil.getProperty(getNodeFromRepository(), name).setValue((String)value);
                }else {
                    addItemProperty(name,property);
                }
            }
            catch (RepositoryException e) {
                log.error("",e);
            }
        }
    }

    /**
     * Add a {(ValueChangeEvent}. listener if not yet set.
     */
    protected void addListenerIfNotYetSet(Property property, Property.ValueChangeListener listener) {
        // add PropertyChange Listener
        if(!((DefaultProperty)property).getListeners(ValueChangeEvent.class).contains(listener)) {
            ((DefaultProperty)property).addListener(listener);
        }
    }

}
