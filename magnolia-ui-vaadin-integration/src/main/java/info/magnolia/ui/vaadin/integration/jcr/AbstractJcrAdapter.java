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
import info.magnolia.jcr.util.PropertyUtil;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;


/**
 * Common base for {JcrItemAdapter} implementation.
 */
public abstract class AbstractJcrAdapter implements Property.ValueChangeListener, JcrItemAdapter {

    private static final Logger log = LoggerFactory.getLogger(AbstractJcrAdapter.class);

    static final String UN_IDENTIFIED = "?";

    private boolean isNode;

    private String jcrNodeIdentifier;

    private String jcrWorkspace;

    private String jcrPath;

    public AbstractJcrAdapter(Item jcrItem) {
        setCommonAttributes(jcrItem);
    }

    /**
     * Init common Item attributes.
     */
    protected void setCommonAttributes(Item jcrItem) {
        String nodeIdentifier = null;
        String workspace = null;
        String path = null;
        try {
            isNode = jcrItem.isNode();
            Node node = isNode ? ((Node) jcrItem) : jcrItem.getParent();
            nodeIdentifier = node.getIdentifier();
            workspace = node.getSession().getWorkspace().getName();
            path = jcrItem.getPath();
        } catch (RepositoryException e) {
            log.error("Couldn't retrieve identifier of jcr property", e);
            path = UN_IDENTIFIED;
            workspace = UN_IDENTIFIED;
            nodeIdentifier = UN_IDENTIFIED;
        }
        this.jcrPath = path;
        this.jcrNodeIdentifier = nodeIdentifier;
        this.jcrWorkspace = workspace;
    }

    @Override
    public boolean isNode() {
        return isNode;
    }

    @Override
    public String getNodeIdentifier() {
        return jcrNodeIdentifier;
    }

    @Override
    public String getWorkspace() {
        return jcrWorkspace;
    }

    @Override
    public String getPath() {
        return jcrPath;
    }

    protected void setPath(String path) {
        this.jcrPath = path;
    }

    /**
     * @return The represented JCR Item, or null in case of {RepositoryException}.
     */
    @Override
    public javax.jcr.Item getJcrItem() {
        try {
            return MgnlContext.getJCRSession(jcrWorkspace).getItem(jcrPath);
        } catch (RepositoryException re) {
            log.warn("Not able to retrieve the JcrItem ", re.getMessage());
            return null;
        }
    }

    /**
     * Path is used as itemId for the vaadin containers.
     * 
     * @return the vaadin itemId.
     */
    @Override
    public Object getItemId() {
        return getPath();
    }

    /**
     * Listener to DefaultProperty value change event. Get this event when a property has changed,
     * and propagate this Vaadin Property value change to the corresponding JCR property.
     */
    @Override
    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property instanceof DefaultProperty) {
            String name = ((DefaultProperty) property).getPropertyName();
            Object value = property.getValue();
            // always the case, even from properties because parent node is then returned
            Node node = (getJcrItem() instanceof Node) ? (Node) getJcrItem() : null;
            try {
                if (node != null && node.hasProperty(name)) {
                    log.debug("Update existing property: {} with value: {}.", name, value);
                    PropertyUtil.getProperty(node, name).setValue((String) value);
                } else {
                    addItemProperty(name, property);
                }
            } catch (RepositoryException e) {
                log.error("", e);
            }
        }
    }

}