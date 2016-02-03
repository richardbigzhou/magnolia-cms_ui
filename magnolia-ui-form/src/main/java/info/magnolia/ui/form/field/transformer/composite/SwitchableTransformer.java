/**
 * This file Copyright (c) 2013-2016 Magnolia International
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

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.PropertysetItem;

/**
 * Default switchable field {@link info.magnolia.ui.form.field.transformer.Transformer} implementation storing and retrieving SwitchableField informations as {@link PropertysetItem}.<br>
 * Storing strategy: <br>
 * - property (definition.getName()) : contain the last selected field name <br>
 * - property (propertyPrefix + first field name): contain the value of the first field <br>
 * - property (propertyPrefix + second field name): contain the value of the second field <br>
 * ...<br>
 */
public class SwitchableTransformer extends CompositeTransformer {

    public SwitchableTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName) {
        super(relatedFormItem, definition, type, fieldsName);
    }

    @Override
    public void writeToItem(PropertysetItem newValues) {
        super.writeToItem(newValues);
        String propertyName = definePropertyName();

        // Add the select property value (select property name == field name)
        if (newValues.getItemProperty(propertyName) != null) {
            relatedFormItem.addItemProperty(propertyName, newValues.getItemProperty(propertyName));
        }
        // As parent implementation will create a property called propertyPrefix+definition.getName()
        // representing the select property name with a propertyPrefix, we have to remove this property.
        if (relatedFormItem.getItemProperty(definition.getName() + propertyName) != null) {
            relatedFormItem.removeItemProperty(definition.getName() + propertyName);
        }
    }

    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem newValues = super.readFromItem();
        String propertyName = definePropertyName();
        if (relatedFormItem.getItemProperty(propertyName) != null) {
            newValues.addItemProperty(propertyName, relatedFormItem.getItemProperty(propertyName));
        }
        return newValues;
    }
}
