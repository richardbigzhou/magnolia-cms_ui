/**
 * This file Copyright (c) 2014-2015 Magnolia International
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

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * This delegating {@link info.magnolia.ui.form.field.transformer.Transformer Transformer} is dedicated to the {@link info.magnolia.ui.form.field.CompositeField CompositeField} and {@link info.magnolia.ui.form.field.SwitchableField SwitchableField};
 * it simply delegates property handling to the configured sub-fields.
 * <p>
 * Therefore, sub-fields use their own transformers to store the field value; e.g. with {@link BasicTransformer} properties are named after their respective {@link info.magnolia.ui.form.field.definition.FieldDefinition#getName()}.
 */
public class DelegatingCompositeFieldTransformer extends BasicTransformer<PropertysetItem> {
    private static final Logger log = LoggerFactory.getLogger(DelegatingCompositeFieldTransformer.class);
    protected List<String> fieldsName;
    private PropertysetItem items;

    /**
     * @deprecated since 5.4.1 - use {@link #DelegatingCompositeFieldTransformer(Item, ConfiguredFieldDefinition, Class, List, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public DelegatingCompositeFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName) {
        this(relatedFormItem, definition, type, fieldsName, Components.getComponent(I18NAuthoringSupport.class));
    }

    @Inject
    public DelegatingCompositeFieldTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<PropertysetItem> type, List<String> fieldsName, I18NAuthoringSupport i18NAuthoringSupport) {
        super(relatedFormItem, definition, type, i18NAuthoringSupport);
        this.fieldsName = fieldsName;
    }
    /**
     * This transformer's write implementation is empty. We do not need to write to the item as this is delegated to the sub-fields.
     */
    @Override
    public void writeToItem(PropertysetItem newValue) {
        log.debug("CALL writeToItem");
    }

    /**
     * Returns a representation of the child items as a {@link PropertysetItem};
     * this is merely a map whose keys are the configured names of the sub-fields, and whose values are the child items, wrapped as {@link ObjectProperty ObjectProperties}.
     */
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