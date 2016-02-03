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

import java.util.Collection;
import java.util.Collections;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;

/**
 * Base implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Node}.
 * Implements {@link com.vaadin.data.Property.ValueChangeListener} in order to inform/change JCR property when a
 * Vaadin property has changed.
 * <pre>
 *  <p>
 *      Jcr properties are read from Repository as long as they are not modified.
 *  <p>
 *  Jcr properties are updated or created if they:
 *  <ul>
 *      <li>Previously existed and where modified.
 *      <li>Newly created and set (an empty created property is not stored into Jcr repository)
 *  </ul>
 *  <p>
 *      Create a JcrNodeAdapter:
 *  <ul>
 *      <li>Just create a new JcrNodeAdapter with the related Jcr Node as parameter.
 *  </ul>
 *  <p>
 *      Properties:
 *  <ul>
 *      <li>getItemProperty(Object id) will return the current stored JCR property if not yet modified or the modified one.
 *      <li>If the property do not exist null will be returned.
 *      <li>In this case we have to create a new Property and attach this property to the JcrNodeAdapter, i.e.
 *  <p>
 *  <code>
 *      property p = DefaultPropertyUtil.newDefaultProperty(...)
 *      jcrNodeAdapter.addItemProperty(...)
 *  </code>
 *  </ul>
 * </pre>
 */
public class JcrNodeAdapter extends AbstractJcrNodeAdapter {

    private static final Logger log = LoggerFactory.getLogger(JcrNodeAdapter.class);

    public JcrNodeAdapter(Node jcrNode) {
        super(jcrNode);
        try {
            setNodeName(jcrNode.getName());
        } catch (RepositoryException e) {
            log.error("Could not access the node name", e);
        }
    }

    /**
     * Get Vaadin Property from a Jcr Property.
     * If the Property was already modified, get this Property from the local changedProperties map - else
     * delegate to super implementation.
     *
     * @param propertyId id of the property to be retrieved
     * @return the Property with the provided propertyId
     */
    @Override
    public Property getItemProperty(Object propertyId) {
        // since we add props as strings, we need to look for them in same fashion
        propertyId = propertyId.toString();
        return getChangedProperties().containsKey(propertyId) ? getChangedProperties().get(propertyId) : super.getItemProperty(propertyId);
    }

    @Override
    public Collection<?> getItemPropertyIds() {
        return Collections.unmodifiableCollection(getChangedProperties().keySet());
    }

    @Override
    public boolean addItemProperty(Object propertyId, Property property) throws UnsupportedOperationException {
        log.debug("Adding new Property Item named [{}] with value [{}]", propertyId, property.getValue());

        // Store Property.
        getChangedProperties().put(propertyId.toString(), property);

        return true;
    }

    /**
     * Remove a property from an Item.
     * If the property was already modified, remove it for the changedProperties Map and
     * add it to the removedProperties Map.
     * Else fill the removedProperties Map with the retrieved property.
     */
    @Override
    public boolean removeItemProperty(Object id) {
        boolean res = false;
        if (getChangedProperties().containsKey(id)) {
            getRemovedProperties().put((String) id, getChangedProperties().get(id));
            res = true;
        } else if (jcrItemHasProperty((String) id)) {
            getRemovedProperties().put((String) id, super.getItemProperty(id));
            res = true;
        }
        return res;
    }

    private boolean jcrItemHasProperty(String propertyName) {
        try {
            return getJcrItem().hasProperty(propertyName);
        } catch (RepositoryException e) {
            log.error("Could not determine if property [{}] exists", propertyName, e);
            return false;
        }
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
