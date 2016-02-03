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
package info.magnolia.ui.form.field.transformer.basic;

import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.UndefinedPropertyType;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;

import java.util.Locale;

import javax.inject.Inject;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Basic implementation of a {@link Transformer}.<br>
 * This handler is used for most of the basic fields (textBox, Date, ...).<br>
 * His responsibility is to: <br>
 * - retrieve or create a basic property from the related item <br>
 * - update the item property value in case of changes performed on the related field.
 *
 * @param <T>
 */
public class BasicTransformer<T> implements Transformer<T> {

    protected Item relatedFormItem;
    protected final ConfiguredFieldDefinition definition;

    protected String basePropertyName;
    protected String i18NPropertyName;
    private Locale locale;
    protected Class<T> type;

    @Inject
    public BasicTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<T> type) {
        this.definition = definition;
        this.relatedFormItem = relatedFormItem;
        this.basePropertyName = definition.getName();
        if (hasI18NSupport()) {
            this.i18NPropertyName = this.basePropertyName;
        }
        setType(type);
    }

    @Override
    public void writeToItem(T newValue) {
        Property<T> p = getOrCreateProperty(type);
        p.setValue(newValue);
    }

    @Override
    public T readFromItem() {
        String defaultValue = definition.getDefaultValue();
        Property<T> p = getOrCreateProperty(type);
        if (definition.isReadOnly()) {
            p.setReadOnly(true);
        }
        return p.getValue();
    }

    /**
     * If the value type is not initialize by the field factory ({@link UndefinedPropertyType}), check if the property already exist in the Item.<br>
     * If the Item has already this property, return the property value type.<br>
     * Else return the default type 'String'
     */
    protected void setType(Class<T> typeFromDefinition) {
        if (typeFromDefinition.isAssignableFrom(UndefinedPropertyType.class)) {
            String propertyName = definePropertyName();
            Property<T> property = relatedFormItem.getItemProperty(propertyName);
            if (property != null) {
                this.type = (Class<T>) property.getType();
            } else {
                this.type = (Class<T>) String.class;
            }
        } else {
            this.type = typeFromDefinition;
        }
    }

    /**
     * If the desired property (propertyName) already exist in the JcrNodeAdapter, return this property<br>
     * else create a new {@link Property}.<br>
     * If the defaultValueString is empty or null, return a typed null value property.
     *
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    protected <T> Property<T> getOrCreateProperty(Class<T> type) {
        String propertyName = definePropertyName();
        Property<T> property = relatedFormItem.getItemProperty(propertyName);

        if (property == null) {
            property = new DefaultProperty<T>(type, null);
            relatedFormItem.addItemProperty(propertyName, property);
        }
        return property;
    }

    /**
     * Based on the i18n information, define the property name to use.
     */
    protected String definePropertyName() {
        String propertyName = this.basePropertyName;

        if (hasI18NSupport()) {
            propertyName = this.i18NPropertyName;
        }
        return propertyName;
    }

    // //////
    // I18N support
    // /////

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setI18NPropertyName(String i18nPropertyName) {
        this.i18NPropertyName = i18nPropertyName;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public String getBasePropertyName() {
        return basePropertyName;
    }

    @Override
    public boolean hasI18NSupport() {
        return definition.isI18n();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}
