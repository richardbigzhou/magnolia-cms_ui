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
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.api.ModelConstants;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;

/**
 * Represents a JCR property as a Vaadin data Item with three fixed properties for its name, type and value.
 */
public class JcrPropertyAdapter extends AbstractJcrAdapter {

    public static final String VALUE_PROPERTY = "value";

    public static final String TYPE_PROPERTY = "type";

    public static final Set<String> PROPERTY_IDS = Collections.unmodifiableSet(new HashSet<String>() {{
        add(ModelConstants.JCR_NAME);
        add(TYPE_PROPERTY);
        add(VALUE_PROPERTY);
    }});

    private static final Logger log = LoggerFactory.getLogger(JcrPropertyAdapter.class);

    public JcrPropertyAdapter(javax.jcr.Property jcrProperty) {
        super(jcrProperty);
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public javax.jcr.Property getJcrItem() {
        return (javax.jcr.Property)super.getJcrItem();
    }

    @Override
    public javax.jcr.Property applyChanges() throws RepositoryException {
        javax.jcr.Property jcrItem = getJcrItem();
        super.updateProperties(jcrItem);
        // We fetch it again from the JCR session in case the name changed
        return getJcrItem();
    }

    @Override
    public Property getItemProperty(Object id) {
        if (getChangedProperties().containsKey(id)) {
            return getChangedProperties().get(id);
        }
        Object value;
        int type = PropertyType.STRING;
        try {
            javax.jcr.Property jcrProperty = getJcrItem();
            if (ModelConstants.JCR_NAME.equals(id)) {
                value = jcrProperty.getName();
            } else if (VALUE_PROPERTY.equals(id)) {
                value = PropertyUtil.getPropertyValueObject(jcrProperty.getParent(), String.valueOf(jcrProperty.getName()));
                type = jcrProperty.getType();
            } else if (TYPE_PROPERTY.equals(id)) {
                value = PropertyType.nameFromValue(jcrProperty.getType());
            } else {
                return null;
            }
        } catch (RepositoryException re) {
            log.error("Could not get property for " + id, re);
            throw new RuntimeRepositoryException(re);
        }

        DefaultProperty property = new DefaultProperty(value.getClass(), value);
        getChangedProperties().put((String) id, property);
        return property;
    }

    @Override
    public Collection<?> getItemPropertyIds() {
        return PROPERTY_IDS;
    }

    /**
     * Updates one of the three supported properties or does nothing if the given propertyId is something else.
     */
    @Override
    protected void updateProperty(Item item, String propertyId, Property property) {
        if (!(item instanceof javax.jcr.Property)) {
            return;
        }
        javax.jcr.Property jcrProperty = (javax.jcr.Property) item;

        if (ModelConstants.JCR_NAME.equals(propertyId)) {
            String jcrName = (String) property.getValue();
            if (jcrName != null && !jcrName.isEmpty()) {
                try {
                    if (!(jcrName.equals(jcrProperty.getName()))) {

                        // make sure new path is available
                        jcrName = Path.getUniqueLabel(jcrProperty.getSession(), jcrProperty.getParent().getPath(), jcrName);

                        // rename the node
                        javax.jcr.Property newProperty = PropertyUtil.renameProperty(jcrProperty, jcrName);

                        // update internal state
                        setItemId(JcrItemUtil.getItemId(newProperty));
                    }
                } catch (RepositoryException e) {
                    log.error("Could not rename JCR property", e);
                }
            }
        } else if (VALUE_PROPERTY.equals(propertyId)) {
            if (property.getValue() != null) {
                try {
                    ValueFactory valueFactory = jcrProperty.getSession().getValueFactory();

                    Value newValue = PropertyUtil.createValue(property.getValue(), valueFactory);
                    jcrProperty.setValue(newValue);
                } catch (RepositoryException e) {
                    log.error("Could not set JCR property", e);
                }
            }
        } else if (TYPE_PROPERTY.equals(propertyId)) {
            if (property.getValue() != null) {

                // get new type from string
                int newType;
                try {
                    newType = PropertyType.valueFromName(property.getValue().toString());
                } catch (IllegalArgumentException e) {
                    log.warn("Could not set new type for JCR property, unknown type string", e);
                    return;
                }

                // set old value with new type
                try {
                    String valueString = jcrProperty.getValue().getString();
                    ValueFactory valueFactory = jcrProperty.getSession().getValueFactory();

                    Value newValue = PropertyUtil.createValue(valueString, newType, valueFactory);
                    jcrProperty.setValue(newValue);
                } catch (RepositoryException e) {
                    log.error("Could not set new type for JCR property", e);
                }
            }
        }
    }

    @Override
    public boolean addItemProperty(Object id, Property property) {
        throw new UnsupportedOperationException("addItemProperty");
    }

    @Override
    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("removeItemProperty");
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
