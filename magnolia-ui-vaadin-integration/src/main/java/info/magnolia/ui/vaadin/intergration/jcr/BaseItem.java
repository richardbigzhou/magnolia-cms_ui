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
package info.magnolia.ui.vaadin.intergration.jcr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Base implementation of an {@link com.vaadin.data.Item} based on a {@link javax.jcr.Node}.
 */
public class BaseItem implements Item {

    static final String UN_IDENTIFIED = "?";

    private static final Logger log = LoggerFactory.getLogger(BaseItem.class);

    private final String jcrIdentifier;

    private HashMap<Object, Property> properties = new LinkedHashMap<Object, Property>();

    public BaseItem(javax.jcr.Node jcrNode) {
        String identifier;
        try {
            identifier = jcrNode.getIdentifier();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr node", e);
            identifier = UN_IDENTIFIED;
        }
        jcrIdentifier = identifier;
    }

    @Override
    public Property getItemProperty(Object id) {
        return properties.get(id);
    }

    @Override
    public Collection<Object> getItemPropertyIds() {
       return Collections.unmodifiableCollection(properties.keySet());
    }

    @Override
    public boolean addItemProperty(Object id, Property property) {
        properties.put(id, property);
        return true;
    }

    @Override
    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        properties.remove(id);
        return true;
    }

    /**
     * Getter - required in order to be able to retrieve the underlying Node from jcr.
     *
     * @return the jcr identifier
     */
    public String getIdentifier() {
        return jcrIdentifier;
    }
}
