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
package info.magnolia.ui.form.field.property.basic;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.property.AbstractBaseHandler;
import info.magnolia.ui.form.field.property.PropertyHandler;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * .
 * 
 * @param <T>
 */
public class BasicPropertyHandler<T> extends AbstractBaseHandler<T> implements PropertyHandler<T> {

    private Class<T> fieldType;


    @Inject
    public BasicPropertyHandler(Item parent, ConfiguredFieldDefinition definition, String fieldTypeName) {
        super(parent, definition, null);
        this.fieldType = getClassForName(fieldTypeName);
    }

    /**
     * Update the related {@link Property} with the new value.<br>
     * The related {@link Property} is created by a previous call to readFromDataSourceItem().
     */
    @Override
    public void writeToDataSourceItem(T newValue) {
        Property<T> p = (Property<T>) getOrCreateProperty(fieldType, "", newValue);
        p.setValue(newValue);
    }

    /**
     * Get a property from the current Item.<br>
     * - if the field is i18n-aware - create a special property that would delegate the values to the proper localized properties. Otherwise - follow the default pattern.<br>
     * - else if the property already exists, return this property. If the property does not exist, create a new property based on the defined type, default value, and saveInfo.
     */
    @Override
    public T readFromDataSourceItem() {
        String defaultValue = definition.getDefaultValue();
        Property<T> p = (Property<T>) getOrCreateProperty(fieldType, defaultValue, null);
        return p.getValue();
    }


    @SuppressWarnings("unchecked")
    private Class<T> getClassForName(String fieldTypeName) {
        try {
            return (Class<T>) Class.forName(fieldTypeName);
        } catch (ClassNotFoundException e) {
            return (Class<T>) String.class;
        }
    }
}
