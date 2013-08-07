/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.form.field.property.multi;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.property.BaseHandler;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * Simple implementation that store/retrieve the properties contained in a {@link PropertysetItem} as a sub Item (equivalent to a Child Node).<br>
 * This sub Item will contain the property set.<br>
 * Storage strategy: <br>
 * - parent (Item)<br>
 * -- child (definition.getName())<br>
 * --- property 1<br>
 * --- property 2 ....<br>
 * property 1, 2, ... are used to populate the {@link PropertysetItem}.
 */
public class SubNodesMultiHandler extends BaseHandler implements PropertyHandler<PropertysetItem> {

    private static final Logger log = LoggerFactory.getLogger(SubNodesMultiHandler.class);
    private List<String> fieldsName;

    @Inject
    public SubNodesMultiHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider, List<String> fieldsName) {
        super(parent, definition, componentProvider);
        this.fieldsName = fieldsName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(PropertysetItem newValues) {
        try {
            // Get the child Item that contains the properties
            JcrNodeAdapter childItem = getOrCreateChildNode(definition.getName(), NodeTypes.Content.NAME);
            if (childItem == null) {
                return;
            }
            Iterator<?> propertyNames = newValues.getItemPropertyIds().iterator();
            while (propertyNames.hasNext()) {
                String propertyName = (String) propertyNames.next();
                Property<Object> storedProperty = childItem.getItemProperty(propertyName);

                if (storedProperty != null) {
                    storedProperty.setValue(newValues.getItemProperty(propertyName).getValue());
                } else {
                    storedProperty = newValues.getItemProperty(propertyName);
                    childItem.addItemProperty(propertyName, storedProperty);
                }
            }
            // Attach the child item to the root item
            if (childItem.getItemPropertyIds() != null && !childItem.getItemPropertyIds().isEmpty()) {
                ((JcrNodeAdapter) parent).addChild(childItem);
            } else {
                ((JcrNodeAdapter) parent).removeChild(childItem);
            }
        } catch (RepositoryException re) {
            log.error("Could not store the values.", re);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public PropertysetItem getValue() {
        PropertysetItem newValues = new PropertysetItem();
        try {
            // Get the child Item that contains the list of children Item Value
            JcrNodeAdapter childItem = getOrCreateChildNode(definition.getName(), NodeTypes.Content.NAME);
            if (childItem == null) {
                return newValues;
            } else if (!(childItem instanceof JcrNewNodeAdapter)) {
                // Get the child Node
                Node childNode = childItem.getJcrItem();
                for (String propertyName : fieldsName) {
                    if (childNode.hasProperty(propertyName)) {
                        Object propertyObject = PropertyUtil.getPropertyValueObject(childNode, propertyName);
                        newValues.addItemProperty(propertyName, new DefaultProperty(propertyObject.getClass(), propertyObject));
                    }
                }
            }

        } catch (RepositoryException e) {
            log.error("Could get stored values", e);
        }
        return newValues;
    }

}
