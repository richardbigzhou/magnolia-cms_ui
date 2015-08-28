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
package info.magnolia.ui.form.field.transformer.basic;

import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.UndefinedPropertyType;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.util.Locale;

import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Basic implementation of a {@link Transformer}.<br>
 * This transformer is used for most of the basic fields (textBox, Date, ...).<br>
 * His responsibility is to: <br>
 * - retrieve or create a basic property from the related item <br>
 * - update the item property value in case of changes performed on the related field.
 *
 * @param <T>
 */
public class BasicTransformer<T> implements Transformer<T> {
    private static final Logger log = LoggerFactory.getLogger(BasicTransformer.class);

    protected final ConfiguredFieldDefinition definition;

    protected Item relatedFormItem;

    protected String basePropertyName;

    /**
     * @deprecated since 5.4.2 - should not be used any longer, alter {@link #basePropertyName} in {@link #setLocale(Locale)} method if needed.
     */
    @Deprecated
    protected String i18NPropertyName;
    private Locale locale;
    protected Class<T> type;

    private boolean isReadOnly = false;

    private I18NAuthoringSupport i18NAuthoringSupport;

    @Inject
    public BasicTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<T> type, I18NAuthoringSupport i18NAuthoringSupport) {
        this.definition = definition;
        this.relatedFormItem = relatedFormItem;
        this.i18NAuthoringSupport = i18NAuthoringSupport;
        this.basePropertyName = definition.getName();

        setType(type);
    }

    /**
     * @deprecated since 5.4.2 - use {@link #BasicTransformer(Item, ConfiguredFieldDefinition, Class, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public BasicTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<T> type) {
        this(relatedFormItem, definition, type, Components.getComponent(I18NAuthoringSupport.class));
    }

    public Item getRelatedFormItem() {
        return relatedFormItem;
    }

    @Override
    public void writeToItem(T newValue) {
        Property<T> p = getOrCreateProperty(type);
        p.setValue(newValue);
    }

    @Override
    public T readFromItem() {
        return getOrCreateProperty(type).getValue();
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
     * If the returned property is not of the desired type, cast this property to the proper type.
     *
     * @param <T>
     */
    protected <T> Property<T> getOrCreateProperty(Class<T> type) {
        return getOrCreateProperty(type, true);
    }

    /**
     * If the desired property (propertyName) already exist in the JcrNodeAdapter, return this property<br>
     * else create a new {@link Property}.<br>
     * If 'checkTypes' is set to true and if the returned property is not of the desired type, cast this property to the proper type.
     */
    protected <T> Property<T> getOrCreateProperty(Class<T> type, boolean checkTypes) {
        String propertyName = definePropertyName();
        Property<T> property = relatedFormItem.getItemProperty(propertyName);

        if (property == null) {
            property = initializeDefaultProperty(type, null);
        } else if (checkTypes && !type.isAssignableFrom(property.getType())) {
            // solve MGNLUI-2494
            // as we have type inconsistency (type of the jcr value is diff. of the definition one), try to convert the jcr type to the type coming from the definition.
            // get the value as String
            String stringValue = ((property.getValue() != null && StringUtils.isNotBlank(property.getValue().toString()))
                    ? property.getValue().toString()
                    : null);
            T value = null;
            try {
                // Convert the String value to the desired type.
                value = (T) DefaultPropertyUtil.createTypedValue(type, stringValue);
            } catch (Exception e) {
                // Ignore. In case of exception, set a null value.
            }
            if (!ObjectUtils.equals(value, property.getValue())) {
                property = initializeDefaultProperty(type, value);
            }
        }

        this.isReadOnly |= property.isReadOnly();
        return property;
    }

    private <T> Property<T> initializeDefaultProperty(Class<T> type, T value) {
        final Property<T> property = new DefaultProperty<>(type, value);
        property.setReadOnly(isReadOnly());
        // This will replace the previous property (with the wrong type) with the new one (correctly typed).
        relatedFormItem.addItemProperty(definePropertyName(), property);
        return property;
    }

    /**
     * Based on the i18n information, define the property name to use.
     */
    protected String definePropertyName() {
        final String propertyName = deriveLocaleAwareName(this.basePropertyName);
        this.i18NPropertyName = propertyName;
        return propertyName;
    }

    protected String deriveLocaleAwareName(String baseName) {
        if (hasI18NSupport() && locale != null && !i18NAuthoringSupport.isDefaultLocale(locale, relatedFormItem)) {
            return i18NAuthoringSupport.deriveLocalisedPropertyName(baseName, locale);
        }
        return baseName;
    }

    protected I18NAuthoringSupport getI18NAuthoringSupport() {
        return i18NAuthoringSupport;
    }

    // //////

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setI18NPropertyName(String i18nPropertyName) {
        log.warn("BasicTransformer.setI18NPropertyName() is deprecated since 5.4.2 without replacement, override BasicTransformer.definePropertyName() and construct the locale-specific property name there if needed.");
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

    @Override
    public boolean isReadOnly() {
        return this.isReadOnly || definition.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }
}
