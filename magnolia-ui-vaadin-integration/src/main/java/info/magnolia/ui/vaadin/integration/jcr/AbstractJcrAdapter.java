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

import info.magnolia.context.MgnlContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

/**
 * Common base for {@link JcrItemAdapter} implementation.
 */
public abstract class AbstractJcrAdapter implements Property.ValueChangeListener, JcrItemAdapter {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrAdapter.class);

    static final String UNIDENTIFIED = "?";

    private boolean isNode;

    private String workspace;

    private String path;

    private final Map<String, Property> changedProperties = new HashMap<String, Property>();

    private final Map<String, Property> removedProperties = new HashMap<String, Property>();

    public AbstractJcrAdapter(Item jcrItem) {
        initCommonAttributes(jcrItem);
    }

    /**
     * Init common Item attributes.
     */
    protected void initCommonAttributes(Item jcrItem) {
        isNode = jcrItem.isNode();
        try {
            workspace = jcrItem.getSession().getWorkspace().getName();
            path = jcrItem.getPath();
        } catch (RepositoryException e) {
            log.error("Could not retrieve workspace or path of JCR Item.", e);
            path = UNIDENTIFIED;
            workspace = UNIDENTIFIED;
        }
    }

    @Override
    public boolean isNode() {
        return isNode;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public String getPath() {
        return path;
    }

    protected void setPath(String path) {
        this.path = path;
    }

    /** @return The represented JCR Item, or null in case of {@link RepositoryException}. */
    @Override
    public javax.jcr.Item getJcrItem() {
        try {
            return MgnlContext.getJCRSession(workspace).getItem(path);
        } catch (RepositoryException re) {
            log.warn("Not able to retrieve the JcrItem ", re.getMessage());
            return null;
        }
    }

    // ABSTRACT IMPLEMENTATION OF PROPERTY CHANGES

    public boolean hasChangedProperties() {
        return changedProperties.size() > 0;
    }

    protected Map<String, Property> getChangedProperties() {
        return changedProperties;
    }

    protected Map<String, Property> getRemovedProperties() {
        return removedProperties;
    }

    /**
     * Listener to DefaultProperty value change event. Get this event when a property has changed, and propagate this
     * Vaadin Property value change to the corresponding JCR property.
     */
    @Override
    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property instanceof DefaultProperty) {
            String propertyId = ((DefaultProperty) property).getPropertyName();
            getChangedProperties().put(propertyId, property);
        }
    }

    /**
     * Updates and removes properties on the JCR Item represented by this adapter, based on the {@link #changedProperties} and {@link #removedProperties} maps. Read-only properties will not be updated.
     */
    public void updateProperties() throws RepositoryException {
        updateProperties(getJcrItem());
    }

    /**
     * Updates and removes properties on given item, based on the {@link #changedProperties} and {@link #removedProperties} maps. Read-only properties will not be updated.
     */
    public void updateProperties(Item item) throws RepositoryException {
        for (Entry<String, Property> entry : changedProperties.entrySet()) {
            if (entry.getValue().isReadOnly()) {
                continue;
            }
            updateProperty(item, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Performs update of an Item based on given vaadin Property. Note that this should not persist changes into JCR.
     * Implementation should simply make sure that updated propertyIds are mapped to the correct actions (jcrName
     * property should be handled in a specific way).
     */
    abstract protected void updateProperty(Item item, String propertyId, Property property);

}