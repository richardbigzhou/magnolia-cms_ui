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
package info.magnolia.ui.form.field.property.list;

import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Dedicated {@link PropertysetItem} implementation of {@link SubNodesListHandler}.<br>
 * This implementation store/retrieve the {@link PropertysetItem} property under the child node.<br>
 * A sub node is created for every {@link PropertysetItem} element of the List.
 * 
 * @param <T>.
 */
public class SubNodesListPropertysetItemHandler<T> extends SubNodesListHandler<PropertysetItem> {

    public SubNodesListPropertysetItemHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider) {
        super(parent, definition, componentProvider);
    }

    @Override
    protected PropertysetItem getValueFromChildNode(Node child) {
        PropertysetItem newValues = new PropertysetItem();
        try {

            PropertyIterator iterator = new JCRMgnlPropertiesFilteringNodeWrapper(child).getProperties();
            while (iterator.hasNext()) {
                Property jcrPorperty = iterator.nextProperty();
                Object propertyObject = PropertyUtil.getPropertyValueObject(child, jcrPorperty.getName());
                DefaultProperty newProperty = new DefaultProperty(propertyObject);

                newValues.addItemProperty(jcrPorperty.getName(), newProperty);
            }
        } catch (RepositoryException re) {
            // TODO log.
        }
        return newValues;
    }

    @Override
    protected void setChildItemValue(JcrNodeAdapter childItem, PropertysetItem newValues) {

        Iterator<?> propertyNames = newValues.getItemPropertyIds().iterator();
        while (propertyNames.hasNext()) {
            String propertyName = (String) propertyNames.next();
            com.vaadin.data.Property<Object> storedProperty = childItem.getItemProperty(propertyName);

            if (storedProperty != null) {
                storedProperty.setValue(newValues.getItemProperty(propertyName).getValue());
            } else {
                storedProperty = newValues.getItemProperty(propertyName);
                childItem.addItemProperty(propertyName, storedProperty);
            }
        }
    }
}
