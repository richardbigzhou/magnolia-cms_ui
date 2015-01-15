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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;

/**
 * Common base for {@link JcrItemAdapter} implementation.
 */
public abstract class AbstractJcrAdapter implements JcrItemAdapter {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrAdapter.class);

    protected static final String UNIDENTIFIED = "?";

    private JcrItemId itemId;

    private final Map<String, Property> changedProperties = new HashMap<String, Property>();

    private final Map<String, Property> removedProperties = new HashMap<String, Property>();

    public AbstractJcrAdapter(Item jcrItem) {
        initCommonAttributes(jcrItem);
    }

    /**
     * Init common Item attributes.
     */
    protected void initCommonAttributes(Item jcrItem) {
        try {
            setItemId(JcrItemUtil.getItemId(jcrItem));
        } catch (RepositoryException e) {
            log.error("Could not retrieve workspace or path of JCR Item.", e);
            setItemId(new JcrItemId(UNIDENTIFIED, UNIDENTIFIED));
        }
    }

    @Override
    public String getWorkspace() {
        return itemId.getWorkspace();
    }

    @Override
    public JcrItemId getItemId() {
        return itemId;
    }

    public void setItemId(JcrItemId itemId) {
        this.itemId = itemId;
    }

    @Override
    public javax.jcr.Item getJcrItem() {
        try {
            return JcrItemUtil.getJcrItem(itemId);
        } catch (RepositoryException re) {
            log.warn("Not able to retrieve the JcrItem ", re.getMessage());
            return null;
        }
    }

    @Override
    public boolean hasChangedProperties() {
        return changedProperties.size() > 0;
    }

    // ABSTRACT IMPLEMENTATION OF PROPERTY CHANGES

    protected Map<String, Property> getChangedProperties() {
        return changedProperties;
    }

    protected Map<String, Property> getRemovedProperties() {
        return removedProperties;
    }

    /**
     * Updates and removes properties on given item, based on the {@link #changedProperties} and {@link #removedProperties} maps. Read-only properties will not be updated and null valued properties will get removed.
     */
    protected void updateProperties(Item item) throws RepositoryException {
        for (Entry<String, Property> entry : changedProperties.entrySet()) {
            if (entry.getValue().isReadOnly()) {
                continue;
            }
            updateProperty(item, entry.getKey(), entry.getValue());
            if (ModelConstants.JCR_NAME.equals(entry.getKey())) {
                // As the item name has changed - the item must be refreshed to prevent an attempt to change property on an invalid old name.
                item = getJcrItem();
            }
        }
    }

    /**
     * Performs update of an Item based on given vaadin Property. Note that this should not persist changes into JCR.
     * Implementation should simply make sure that updated propertyIds are mapped to the correct actions (jcrName
     * property should be handled in a specific way).
     */
    protected abstract void updateProperty(Item item, String propertyId, Property property);

}
