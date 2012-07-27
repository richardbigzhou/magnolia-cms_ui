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

import java.util.Collection;
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

/**
 * Base implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Node}.
 * Implements {Property.ValueChangeListener} in order to inform/change JCR property when a
 * Vaadim property has changed.
 *
 * Jcr properties are read from Repository as long as they are not modified.
 *
 * Jcr properties are updated or created if they:
 *   Previously existed and where modified.
 *   Newly created and set (an empty created property is not stored into Jcr repository)
 *
 * Create a JcrNodeAdapter:
 *   Just create a new JcrNodeAdapter with the related Jcr Node as parameter.
 *
 * Properties:
 *   getItemProperty(Object id) will return the current stored JCR property if not yet modified
 *     or the modified one.
 *     If the property do not exist null will be returned.
 *     In this case we have to create a new Property and attach this property to the JcrNodeAdapter
 *       property p = DefaultPropertyUtil.newDefaultProperty(...)
 *      jcrNodeAdapter.addItemProperty(...)
 *
 */
public class JcrNodeAdapter extends JcrAbstractNodeAdapter  {
    // Init
    private static final Logger log = LoggerFactory.getLogger(JcrNodeAdapter.class);

    public JcrNodeAdapter(Node jcrNode) {
        super(jcrNode);
        try {
            setNodeName(jcrNode.getName());
        }
        catch (RepositoryException e) {
            log.error("Could not access the Node name.... Should never happen",e);
        }
    }

    /**
     * Get Vaadin Property from a Jcr Property.
     * If the Property was already modify, get this Property from the local changedProperties map.
     * Else:
     *   If the corresponding Jcr property don't exist, create a empty Vaadin Property.
     *   If the corresponding Jcr property already exist, create a corresponding Vaadin Property.
     */
    @Override
    public Property getItemProperty(Object id) {
        DefaultProperty property = null;

        if(changedProperties.containsKey(id)) {
            property = (DefaultProperty) changedProperties.get(id);
        }
        else {
            property = (DefaultProperty) super.getItemProperty(id);
        }
        return property;
    }

    @Override
    public Collection<?> getItemPropertyIds() {
        return Collections.unmodifiableCollection(changedProperties.keySet());
    }

    @Override
    public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
        log.debug("Adding new Property Item named [{}] with value [{}]", id, property.getValue());

        // add PropertyChange Listener
        if(!((DefaultProperty)property).getListeners(ValueChangeEvent.class).contains(this)) {
            ((DefaultProperty)property).addListener(this);
        }

        //Store Property.
        changedProperties.put((String) id, property);

        return true;
    }

    /**
     * Remove a property from an Item.
     * If the property was already modified, remove it for the changedProperties Map and
     * add it to the removedProperties Map.
     * Else fill the removedProperties Map with the retrieved property.
     */
    @Override
    public boolean removeItemProperty(Object id){
        boolean res = false;
        if(changedProperties.containsKey(id)) {
            removedProperties.put((String)id, changedProperties.remove(id));
            res = true;
        }else if(jcrItemHasProperty((String) id)){
            removedProperties.put((String)id, super.getItemProperty(id));
            res = true;
        } else {
            res = false;
        }
        return res;
    }


    @Override
    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if(property instanceof DefaultProperty) {
            String name = ((DefaultProperty)property).getPropertyName();
            addItemProperty(name, property);
        }
    }


    private boolean jcrItemHasProperty(String propertyName) {
        try {
            return ((Node) getJcrItem()).hasProperty((String) propertyName);
        }
        catch (RepositoryException e) {
            log.error("", e);
            return false;
        }
    }
}
