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
package info.magnolia.ui.form.field.property.composite;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.property.AbstractBaseHandler;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * Simple implementation storing and retrieving properties defined under an Item as {@link PropertysetItem} element.<br>
 * Storage strategy: <br>
 * - getValue(): <br>
 * -- iterate the fieldsName property and retrieve all stored property.<br>
 * -- Fulfill the {@link PropertysetItem}.<br>
 * - setValue(): <br>
 * -- iterate the incoming {@link PropertysetItem}.<br>
 * -- if the related parent item do not contain this property, add it.<br>
 */
public class SimplePropertyCompositeHandler extends AbstractBaseHandler<PropertysetItem> implements PropertyHandler<PropertysetItem> {

    protected List<String> fieldsName;
    private String propertyPrefix;

    public SimplePropertyCompositeHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider, List<String> fieldsName) {
        super(parent, definition, componentProvider);
        this.fieldsName = fieldsName;
        this.propertyPrefix = createPropertyPrefix(definition);
    }

    /**
     * @return propertyPrefix used to prefix the property name.
     */
    protected String createPropertyPrefix(ConfiguredFieldDefinition definition) {
        return definition.getName() + "_";
    }

    @Override
    public void writeToDataSourceItem(PropertysetItem newValues) {
        // Get iterator.
        Iterator<?> propertyNames = newValues.getItemPropertyIds().iterator();

        while (propertyNames.hasNext()) {
            String propertyName = (String) propertyNames.next();
            String compositePropertyName = propertyPrefix + propertyName;
            Property<?> property = parent.getItemProperty(compositePropertyName);
            if (property == null) {
                parent.addItemProperty(compositePropertyName, newValues.getItemProperty(propertyName));
            }
        }
    }

    @Override
    public PropertysetItem readFromDataSourceItem() {
        PropertysetItem newValues = new PropertysetItem();
        if (!(parent instanceof JcrNewNodeAdapter)) {
            for (String propertyName : fieldsName) {
                if (parent.getItemProperty(propertyPrefix + propertyName) != null) {
                    newValues.addItemProperty(propertyName, parent.getItemProperty(propertyPrefix + propertyName));
                }
            }
        }
        return newValues;
    }

}
