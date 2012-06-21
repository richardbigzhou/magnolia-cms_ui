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

import java.util.Collection;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;


/**
 * Base implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Property}.
 */

public class JcrPropertyAdapter extends JcrAbstractAdapter {

    private static final Logger log = LoggerFactory.getLogger(JcrPropertyAdapter.class);


    public JcrPropertyAdapter(javax.jcr.Property jcrProperty) {
        super(jcrProperty);
    }


    public javax.jcr.Property getProperty() throws RepositoryException {
        return (javax.jcr.Property) getJcrItem();
    }

    @Override
    public Property getItemProperty(Object id) {
        Object value = null;
        try {
            value = getProperty().getString();
        }catch (RepositoryException re) {
            log.error("Could not get property for "+id,re);
            throw new RuntimeRepositoryException(re);
        }
        return new DefaultProperty((String)id, value);
    }

    @Override
    public Collection< ? > getItemPropertyIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addItemProperty(Object id, Property property) {
        try {
            getProperty().setValue((String)property.getValue());
        }catch (RepositoryException re) {
            log.error("Could not get property for "+id,re);
            throw new RuntimeRepositoryException(re);
        }
        return true;
    }

    @Override
    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        try {
            getProperty().remove();
        }catch (RepositoryException re) {
            log.error("Could not get property for "+id,re);
            throw new RuntimeRepositoryException(re);
        }
        return true;
    }

}
