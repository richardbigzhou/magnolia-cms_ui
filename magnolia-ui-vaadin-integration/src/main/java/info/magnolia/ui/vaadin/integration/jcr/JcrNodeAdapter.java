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

import info.magnolia.cms.core.MetaData;
import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;

/**
 * Base implementation of an {@link com.vaadin.data.Item} wrapping/representing a {@link javax.jcr.Node}.
 * Implements {Property.ValueChangeListener} in order to inform/change JCR property when a
 * Vaadim property has changed.
 *
 * Jcr properties are read from Repository as long as they are not modified.
 *
 * Jcr properties are updated or created if they:
 *   Previously existed and where modified.
 *   Newly created and set (an empty created property is not stored into Jcr repository)
 */
public class JcrNodeAdapter extends JcrAbstractNodeAdapter  {
    // Init
    private static final Logger log = LoggerFactory.getLogger(JcrNodeAdapter.class);
    protected Map<String, Property> changedProperties = new HashMap<String,Property>();
    protected Map<String, Property> removedProperties = new HashMap<String,Property>();

    public JcrNodeAdapter(Node jcrNode) {
        super(jcrNode);
    }

    /**
     * Get Vaadin Property from a Jcr Property.
     * If the Property was already modify, get this Property from the local changedProperties map.
     * Else:
     *   If the corresponding Jcr property don't exist, create a empty Vaadin Property.
     *   If the corresponding Jcr property already exist, create a corresponding Vaadin Property.
     */
    @Override
    public Property getItemProperty(Object id) {
        DefaultProperty property = null;

        if(changedProperties.containsKey(id)) {
            property = (DefaultProperty) changedProperties.get(id);
        }
        else {
            property = (DefaultProperty) super.getItemProperty(id);
        }
        return property;
    }

    @Override
    public Collection<?> getItemPropertyIds() {
        return Collections.unmodifiableCollection(changedProperties.keySet());
    }

    @Override
    public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
        log.debug("Adding new Property Item named [{}] with value [{}]", id, property.getValue());

        // add PropertyChange Listener
        if(!((DefaultProperty)property).getListeners(ValueChangeEvent.class).contains(this)) {
            ((DefaultProperty)property).addListener(this);
        }

        //Store Property.
        changedProperties.put((String) id, property);

        return true;
    }

    /**
     * Remove a property from an Item.
     * If the property was already modified, remove it for the changedProperties Map and
     * add it to the removedProperties Map.
     * Else fill the removedProperties Map with the retrieved property.
     */
    @Override
    public boolean removeItemProperty(Object id){
        boolean res = false;
        if(changedProperties.containsKey(id)) {
            removedProperties.put((String)id, changedProperties.remove(id));
            res = true;
        }else if(jcrItemHasProperty((String) id)){
            removedProperties.put((String)id, super.getItemProperty(id));
            res = true;
        } else {
            res = false;
        }
        return res;
    }

    /**
     * Get the referenced node and update the property.
     * Update property will:
     *  remove existing JCR property if requested
     *  add newly and setted property
     *  update existing modified property.
     */
    public Node getNode() {
        Node node = null;
        try {
            node =  getNodeFromRepository();
            // Update property
            updateProperty(node);

            return node;
        }
        catch (LoginException e) {
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

    /**
     * Update or remove property.
     * Property wit flag saveInfo to false will not be updated.
     * Property can refer to node property (like name, title) or
     * node.MetaData property like (MetaData/template).
     * Also handle the specific case of node renaming.
     *  If property JCR_NAME is present, Rename the node.
     */
    protected void updateProperty(Node node) throws RepositoryException {
      //Update property
        for(Entry<String, Property> entry: changedProperties.entrySet()) {
            //Check saveInfo Flag
            if(!((DefaultProperty)entry.getValue()).isSaveInfo() || ((DefaultProperty)entry.getValue()).isReadOnly()) {
                continue;
            }
            // JCRNAME has change --> perform the renaming and continue
            if(entry.getKey().equals(JCR_NAME) && (entry.getValue() !=null && !entry.getValue().toString().isEmpty())) {
               node.getSession().move(node.getPath(), NodeUtil.combinePathAndName(node.getParent().getPath(), entry.getValue().getValue().toString()));
               setPath(node.getPath());
               continue;
            }
            // Check if the field is refereeing to MetaData Property
            if(entry.getKey().startsWith(MetaData.DEFAULT_META_NODE)) {
                PropertyUtil.setProperty(node.getNode(MetaData.DEFAULT_META_NODE), StringUtils.removeStart(entry.getKey(),MetaData.DEFAULT_META_NODE+"/"), entry.getValue().getValue());
            } else {
                PropertyUtil.setProperty(node, entry.getKey(), entry.getValue().getValue());
            }
        }
        // Remove Property
        for(Entry<String, Property> entry: removedProperties.entrySet()) {
            if(node.hasProperty(entry.getKey())) {
                node.getProperty(entry.getKey()).remove();
            }
        }
    }

    private boolean jcrItemHasProperty(String propertyName) {
        try {
            return ((Node) getJcrItem()).hasProperty((String) propertyName);
        }
        catch (RepositoryException e) {
            log.error("", e);
            return false;
        }
    }

}
