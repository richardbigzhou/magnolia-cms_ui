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
package info.magnolia.ui.form.field.transformer.multi;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * Multi values properties implementation of {@link info.magnolia.ui.form.field.transformer.Transformer}.<br>
 * Store the list of values as a {@link javax.jcr.Value[]} property.<br>
 * Retrieve the {@link javax.jcr.Value[]} property value as a list.
 */

public class MultiValueTransformer extends BasicTransformer<PropertysetItem> {

    public MultiValueTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
        super(relatedFormItem, definition, type);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void writeToItem(PropertysetItem newValue) {
        Property<List> property = getOrCreateProperty(List.class);

        List<Object> propertyValue = new LinkedList<Object>();
        if (newValue != null) {
            Iterator<?> it = newValue.getItemPropertyIds().iterator();
            while (it.hasNext()) {
                propertyValue.add(newValue.getItemProperty(it.next()).getValue());
            }
        }
        property.setValue(propertyValue);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem newValues = new PropertysetItem();
        Property<List> property = getOrCreateProperty(List.class);
        if (property.getValue() != null) {
            List<?> values = property.getValue();
            int position = 0;
            for (Object o : values) {
                newValues.addItemProperty(position, new DefaultProperty(o));
                position += 1;
            }
        }

        return newValues;
    }

}
