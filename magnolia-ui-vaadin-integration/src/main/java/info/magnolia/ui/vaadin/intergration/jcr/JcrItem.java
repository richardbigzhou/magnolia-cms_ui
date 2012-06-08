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

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.PropertyUtil;

import java.util.Collection;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Base implementation of an {@link com.vaadin.data.Item} based on a {@link javax.jcr.Node}.
 */
public class JcrItem implements Item {

    static final String UN_IDENTIFIED = "?";

    private static final Logger log = LoggerFactory.getLogger(JcrItem.class);

    private final String jcrIdentifier;
    private final String jcrWorkspace;

    public JcrItem(Node jcrNode) {
        String identifier;
        String workspace;
        try {
            identifier = jcrNode.getIdentifier();
            workspace = jcrNode.getSession().getWorkspace().getName();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr node", e);
            identifier = UN_IDENTIFIED;
            workspace = UN_IDENTIFIED;
        }
        jcrIdentifier = identifier;
        jcrWorkspace = workspace;
    }

    @Override
    public Property getItemProperty(Object id) {
        Object value;
        try {
            value = PropertyUtil.getProperty(getNode(), (String) id).getString();
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        return new BaseProperty(value);
    }

    @Override
    public Collection<Object> getItemPropertyIds() {
        // TODO dlipp - not clear where these could be retrieved from...
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addItemProperty(Object id, Property property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Getter - required in order to be able to retrieve the underlying Node from jcr.
     *
     * @return the jcr identifier
     */
    public String getIdentifier() {
        return jcrIdentifier;
    }

    public Node getNode() throws RepositoryException{
        return MgnlContext.getJCRSession(jcrWorkspace).getNodeByIdentifier(jcrIdentifier);
    }
}
