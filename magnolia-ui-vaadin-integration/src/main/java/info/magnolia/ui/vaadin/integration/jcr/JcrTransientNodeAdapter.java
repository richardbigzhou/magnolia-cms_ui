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
import info.magnolia.jcr.RuntimeRepositoryException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

/**
 * Implementation of an {@link com.vaadin.data.Item} wrapping/representing a <strong>transient</strong> {@link javax.jcr.Node}.
 * Implements {Property.ValueChangeListener} in order to inform/change JCR property when a
 * Vaadin property has changed.
 * <p>The special property {@value #JCR_NAME} is reserved and can only be used to set the new node name. If not found, the default name (that is the relative path)
 * of the underlying transient node is used (which is likely to be something like <code>untitled</code>).
 */
@SuppressWarnings("serial")
public class JcrTransientNodeAdapter extends JcrNodeAdapter {

    protected static final String JCR_NAME = "jcrName";

    private static final Logger log = LoggerFactory.getLogger(JcrTransientNodeAdapter.class);

    private Map<String, Property> properties = new HashMap<String,Property>();

    /**
     * Will throw an {@link IllegalArgumentException} if the node is <strong>not transient</strong>, that is if calling {@code node.isNew()} returns <code>false</code>.
     */
    public JcrTransientNodeAdapter(final Node jcrNode) throws RepositoryException {
       super(jcrNode);

       if(!jcrNode.isNew()) {
            throw new IllegalArgumentException(jcrNode.getPath() + " is not a transient node, that is one which temporarily lives in JCR session's transient storage and has not yet been saved.");
       }
    }

    @Override
    public Property getItemProperty(Object id) {
        Object value = "";
        if(properties.containsKey(id)) {
            value = properties.get(id);
        }
        DefaultProperty property = new DefaultProperty((String)id, value);
        // add PropertyChange Listener
        property.addListener(this);
        return property;
    }

    @Override
    public Collection<?> getItemPropertyIds() {
        return Collections.unmodifiableCollection(properties.keySet());
    }

    @Override
    public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
        // add PropertyChange Listener
        ((DefaultProperty)property).addListener(this);

        log.debug("Adding new Property Item named [{}] with value [{}]", id, property.getValue());

        //Store Property.
        properties.put((String) id, property);
        return true;
    }

    @Override
    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        properties.remove(id);
        return true;
    }
    /**
     * Builds the javax.jcr.Node with the Vaadin properties stored so far and attach the <strong>unsaved</strong> node to the current JCR session.
     */
    @Override
    public Node getNode() {

        try {
            String newNodeRelPath = StringUtils.substringAfter(getItemId(), "/");
            if(properties.containsKey(JCR_NAME)) {
                if(newNodeRelPath.contains("/")) {
                    newNodeRelPath = StringUtils.substringBefore(newNodeRelPath, "/") + "/"+ properties.get(JCR_NAME).getValue().toString();
                } else {
                    newNodeRelPath = properties.get(JCR_NAME).getValue().toString();
                }
            }
            log.debug("Path to be saved is [{}]", newNodeRelPath);
            final Session session = MgnlContext.getJCRSession(getWorkspace());
            final Node unsavedNode = session.getRootNode().addNode(newNodeRelPath, getPrimaryNodeTypeName());

            for(Entry<String, Property> entry: properties.entrySet()) {
                //TODO fgrilli: check the value type and create the correct jcr object for the value
                if(JCR_NAME.equals(entry.getKey())) {
                    continue;
                }
                unsavedNode.setProperty(entry.getKey(), entry.getValue().toString());
            }
            return unsavedNode;

        } catch (LoginException e) {
            throw new RuntimeRepositoryException(e);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    @Override
    public void valueChange(ValueChangeEvent event) {
        Property property = event.getProperty();
        if(property instanceof DefaultProperty) {
            String name = ((DefaultProperty)property).getPropertyName();
            addItemProperty(name, property);
        }
    }
}
