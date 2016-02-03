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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;

/**
 * SingleProperty implementation of {@link info.magnolia.ui.form.field.transformer.Transformer}.<br>
 * Store the list of values in a single property as a concatenation of string with ',' as separator.<br>
 * Retrieve the single property as a List of String.
 * <b>This handler is implemented for backward capability with Magnolia 4.x. <br>
 * As for Magnolia 4.x, the current implementation only support a list of String</b>
 */
public class MultiValueJSONTransformer extends BasicTransformer<PropertysetItem> {

    @Inject
    public MultiValueJSONTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type) {
        super(relatedFormItem, definition, type);
    }

    @Override
    public void writeToItem(PropertysetItem newValue) {
        Property<String> property = getOrCreateProperty(String.class);
        property.setValue(StringUtils.join(removeComma(newValue), ","));
    }

    @Override
    public PropertysetItem readFromItem() {
        PropertysetItem newValues = new PropertysetItem();
        List<String> list = new ArrayList<String>();
        Property<String> property = getOrCreateProperty(String.class);
        String value = property.getValue();
        if (StringUtils.isNotBlank(value)) {
            list = Arrays.asList(value.split(","));
        }
        int position = 0;
        for (String element : list) {
            newValues.addItemProperty(position, new DefaultProperty(element));
            position += 1;
        }
        return newValues;
    }

    /**
     * Simple utility method that remove the , for every elements of a String List.
     */
    private List<String> removeComma(PropertysetItem newValue) {
        List<String> res = new ArrayList<String>();
        if (newValue != null) {
            Iterator<?> it = newValue.getItemPropertyIds().iterator();
            while (it.hasNext()) {
                String element = newValue.getItemProperty(it.next()).getValue().toString();
                res.add(StringUtils.replace(element, ",", " "));
            }
        }
        return res;
    }

}
