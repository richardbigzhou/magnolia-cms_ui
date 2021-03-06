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

import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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

    /**
     * @deprecated since 5.4.2 - use {@link #SwitchableTransformer(Item, ConfiguredFieldDefinition, Class, List, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public SwitchableTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName) {
        super(relatedFormItem, definition, type, fieldsName);
    }

    public SwitchableTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName, I18NAuthoringSupport i18NAuthoringSupport) {
        super(relatedFormItem, definition, type, fieldsName, i18NAuthoringSupport);
    }

    @Override
    public void writeToItem(PropertysetItem newValues) {
        // Alter newsValues to clear all data that not belong to selections
        String currentSelection = (String) newValues.getItemProperty(propertyPrefix).getValue();

        // Get iterator.
        Collection<String> propertyNames = (Collection<String>) newValues.getItemPropertyIds();
        for (String propertyName : propertyNames) {

            String compositePropertyName = getCompositePropertyName(propertyName);
            if (!propertyName.equals(currentSelection) && !propertyName.equals(propertyPrefix)) {
                relatedFormItem.removeItemProperty(compositePropertyName);
            } else {
                if (newValues.getItemProperty(propertyName) != null) {
                    relatedFormItem.addItemProperty(compositePropertyName, newValues.getItemProperty(propertyName));
                }
            }
        }
    }

    @Override
    public PropertysetItem readFromItem() {
        return super.readFromItem();
    }

    @Override
    protected String getCompositePropertyName(String propertyName) {
        if (StringUtils.equals(propertyName, propertyPrefix)) {
            propertyName = StringUtils.EMPTY;
        }
        return super.getCompositePropertyName(propertyName);
    }
}
