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

import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;


/**
 * Base implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Property}.
 */
public class JcrPropertyAdapter extends AbstractJcrAdapter {

    public static final String VALUE_PROPERTY = "value";

    public static final String TYPE_PROPERTY = "type";

    // Init
    private static final Logger log = LoggerFactory.getLogger(JcrPropertyAdapter.class);

    private String jcrPropertyName;

    public JcrPropertyAdapter(javax.jcr.Property jcrProperty) {
        super(jcrProperty);
        setPropertyName(jcrProperty);
    }

    /**
     * Set PropertyName.
     */
    private void setPropertyName(javax.jcr.Property jcrProperty) {
        String propertyIdentifier = null;
        try {
            propertyIdentifier = jcrProperty.getName();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr property", e);
            propertyIdentifier = UNIDENTIFIED;
        }
        this.jcrPropertyName = propertyIdentifier;
    }

    public String getPropertyName() {
        return jcrPropertyName;
    }

    public javax.jcr.Property getProperty() throws RepositoryException {
        return (javax.jcr.Property) getJcrItem();
    }

    @Override
    public Property getItemProperty(Object id) {
        Object value = null;
        try {
            if (JCR_NAME.equals(id)) {
                value = getProperty().getName();
            } else if (VALUE_PROPERTY.equals(id)) {
                value = getProperty().getString();
            } else if (TYPE_PROPERTY.equals(id)) {
                value = PropertyType.nameFromValue(getProperty().getType());
            } else {
                value = null;
            }
        } catch (RepositoryException re) {
            log.error("Could not get property for " + id, re);
            throw new RuntimeRepositoryException(re);
        }
        DefaultProperty property = new DefaultProperty((String) id, value);
        property.addListener(this);
        return property;
    }

    @Override
    public Collection< ? > getItemPropertyIds() {
        throw new UnsupportedOperationException();
    }

    /**
     * JcrPropertyAdapter custom logic to update one single vaadin property. If updated propertyId is
     * {@link info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter#JCR_NAME}, then rename JCR Property. If propertyId
     * is {@link #VALUE_PROPERTY}, set new property value. If propertyId is {@link #TYPE_PROPERTY}, set new property
     * type. Otherwise, do nothing.
     */
    @Override
    protected void updateProperty(Item item, String propertyId, Property property) {
        if (!(item instanceof javax.jcr.Property)) {
            return;
        }
        javax.jcr.Property jcrProperty = (javax.jcr.Property) item;

        // JCR_NAME name then move this Node
        if (JCR_NAME.equals(propertyId)) {
            String jcrName = (String) property.getValue();
            if (jcrName != null && !jcrName.isEmpty()) {
                try {
                    // Never rename property to same name, otherwise PropertyUtil would delete it.
                    boolean isNameUnchanged = (jcrName.equals(jcrProperty.getName()));
                    if (isNameUnchanged) {
                        return;
                    }
                    PropertyUtil.renameProperty(jcrProperty, jcrName);
                } catch (RepositoryException e) {
                    log.error("Could not rename JCR Property.", e);
                }
            }
        } else if (VALUE_PROPERTY.equals(propertyId)) {
            if (property.getValue() != null) {
                try {
                    String valueString = (String) property.getValue();
                    int valueType = jcrProperty.getType();
                    ValueFactory valueFactory = jcrProperty.getSession().getValueFactory();

                    Value newValue = PropertyUtil.createValue(valueString, valueType, valueFactory);
                    jcrProperty.setValue(newValue);
                } catch (RepositoryException e) {
                    log.error("Could not set JCR Property", e);
                }
            }
        } else if (TYPE_PROPERTY.equals(propertyId)) {
            if (property.getValue() != null) {
                // get new type from string
                int newType;
                try {
                    newType = PropertyType.valueFromName(property.getValue().toString());
                } catch (IllegalArgumentException e) {
                    log.warn("Could not set new type for JCR Property, unknown type string.", e);
                    return;
                }

                // set old value with new type
                try {
                    String valueString = jcrProperty.getValue().getString();
                    ValueFactory valueFactory = jcrProperty.getSession().getValueFactory();

                    Value newValue = PropertyUtil.createValue(valueString, newType, valueFactory);
                    jcrProperty.setValue(newValue);
                } catch (RepositoryException e) {
                    log.error("Could not set new type for JCR Property.", e);
                }

            }
        }
    }

    @Override
    public boolean addItemProperty(Object id, Property property) {
        try {
            getProperty().setValue((String) property.getValue());
        } catch (RepositoryException re) {
            throw new RuntimeRepositoryException(re);
        }
        return true;
    }

    @Override
    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        try {
            getProperty().remove();
        } catch (RepositoryException re) {
            throw new RuntimeRepositoryException(re);
        }
        return true;
    }

}
