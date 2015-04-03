/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.form.field.transformer.composite;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * Default composite field {@link info.magnolia.ui.form.field.transformer.Transformer} implementation storing and retrieving properties defined under an Item as {@link PropertysetItem} element.<br>
 * Storage strategy: <br>
 * - getValue(): <br>
 * -- iterate the fieldsName property and retrieve all stored property.<br>
 * -- Fulfill the {@link PropertysetItem}.<br>
 * - setValue(): <br>
 * -- iterate the incoming {@link PropertysetItem}.<br>
 * -- if the related parent item do not contain this property, add it.<br>
 */
public class CompositeTransformer extends BasicTransformer<PropertysetItem> {

    protected List<String> fieldsName;
    protected String propertyPrefix;

    public CompositeTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName) {
        super(relatedFormItem, definition, type);
        this.fieldsName = fieldsName;
        this.propertyPrefix = createPropertyPrefix(definition);
    }

    /**
     * @return propertyPrefix used to prefix the property name.
     */
    protected String createPropertyPrefix(ConfiguredFieldDefinition definition) {
        return definition.getName();
    }

    @Override
    public void writeToItem(PropertysetItem newValues) {
        // Get iterator.
        Iterator<?> propertyNames = newValues.getItemPropertyIds().iterator();

        while (propertyNames.hasNext()) {
            String propertyName = (String) propertyNames.next();
            String compositePropertyName = getCompositePropertyName(propertyName);
            Property<?> property = relatedFormItem.getItemProperty(compositePropertyName);
            if (property == null && newValues.getItemProperty(propertyName) != null) {
                relatedFormItem.addItemProperty(compositePropertyName, newValues.getItemProperty(propertyName));
            }
        }
    }

    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem newValues = new PropertysetItem();
        for (String propertyName : fieldsName) {
            String compositePropertyName = getCompositePropertyName(propertyName);
            if (relatedFormItem.getItemProperty(compositePropertyName) != null) {
                newValues.addItemProperty(propertyName, relatedFormItem.getItemProperty(compositePropertyName));
            }
        }
        return newValues;
    }

    protected String getCompositePropertyName(String propertyName) {
        propertyName = propertyPrefix + propertyName;
        if (hasI18NSupport()) {
            propertyName = propertyName + StringUtils.difference(basePropertyName, i18NPropertyName);
        }
        return propertyName;
    }
}
