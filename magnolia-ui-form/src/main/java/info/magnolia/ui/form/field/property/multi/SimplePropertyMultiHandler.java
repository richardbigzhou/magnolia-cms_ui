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

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.property.BaseHandler;
import info.magnolia.ui.form.field.property.PropertyHandler;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * Simple implementation that store/retrieve the properties contained in a {@link PropertysetItem} as normal Jcr property.<br>
 * Storage strategy: <br>
 * - iterate the List<String> fieldsName and retrieve or store the property.<br>
 * -- the property name is a composition of the result of the created propertyPrefix 'qqq_' and <br>
 * -- the name of the property coming from List<String> fieldsName 'name' <br>
 * -- 'qqq_name'
 */
public class SimplePropertyMultiHandler extends BaseHandler implements PropertyHandler<PropertysetItem> {

    private List<String> fieldsName;
    private String propertyPrefix;

    public SimplePropertyMultiHandler(Item parent, ConfiguredFieldDefinition definition, ComponentProvider componentProvider, List<String> fieldsName) {
        super(parent, definition, componentProvider);
        this.fieldsName = fieldsName;
        this.propertyPrefix = getPropertyPrefix(definition);
    }

    protected String getPropertyPrefix(ConfiguredFieldDefinition definition) {
        return definition.getName() + "_";
    }

    @Override
    public void setValue(PropertysetItem newValues) {

        Iterator<?> propertyNames = newValues.getItemPropertyIds().iterator();

        while (propertyNames.hasNext()) {
            String propertyName = (String) propertyNames.next();
            String compositePropertyName = propertyPrefix + propertyName;
            Property<Object> property = parent.getItemProperty(compositePropertyName);
            if (property == null) {
                parent.addItemProperty(compositePropertyName, new DefaultProperty(newValues.getItemProperty(propertyName).getValue()));
            }
        }
    }

    @Override
    public PropertysetItem getValue() {
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
