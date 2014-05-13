/**
 * This file Copyright (c) 2014 Magnolia International
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
import info.magnolia.ui.form.field.transformer.I18nTransformerDelegator;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * The {@link DelegatingCompositeFieldTransformer#readFromItem()} returns an {@link PropertysetItem} that contains in this case:<br>
 * - as key, the embedded field name <br>
 * - as values the 'relatedFormItem' items wrapped into an {@link ObjectProperty}. <br>
 * SubField uses then their {@link ConfiguredFieldDefinition#getName()} to define the name of the property used store the field value.
 * //TODO add comments about the empty write.
 */
public class DelegatingCompositeFieldTransformer extends BasicTransformer<PropertysetItem> implements I18nTransformerDelegator {
    private static final Logger log = LoggerFactory.getLogger(DelegatingCompositeFieldTransformer.class);
    protected List<String> fieldsName;
    private PropertysetItem items;

    @Inject
    public DelegatingCompositeFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName) {
        super(relatedFormItem, definition, type);
        this.fieldsName = fieldsName;
    }

    @Override
    public void writeToItem(PropertysetItem newValue) {
        // No need to write to the Item as this is done on the Item passed to the subFields.
        log.debug("CALL writeToItem");
    }

    @Override
    public PropertysetItem readFromItem() {
        // Only read it once
        if (items != null) {
            return items;
        }
        items = new PropertysetItem();
        for (String fieldName : fieldsName) {
            items.addItemProperty(fieldName, new ObjectProperty<Item>(relatedFormItem));
        }
        return items;
    }
}