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
package info.magnolia.ui.framework.i18n;


import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.api.i18n.I18NAwareProperty;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;

import java.util.Locale;

import com.vaadin.data.util.AbstractProperty;

/**
 * Simple Property that manages one or more localized JCR properties internally. Depending on the Locale set
 * it delegates the value read/write to the corresponding JCR property.
 * @param <T> property type.
 */
public class I18NAwarePropertyImpl<T> extends AbstractProperty<T> implements I18NAwareProperty<T> {

    private String basePropertyName;

    private String i18NPropertyName;

    private T defaultValue;

    private Locale locale;

    private Class<T> type;

    private AbstractJcrNodeAdapter parentNodeAdapter;

    public I18NAwarePropertyImpl(String baseName, Class<T> type, AbstractJcrNodeAdapter parentNodeAdapter) {
        this(baseName, type, parentNodeAdapter, null);
    }

    public I18NAwarePropertyImpl(String baseName, Class<T> type, AbstractJcrNodeAdapter parentNodeAdapter, T defaultValue) {
        super();
        this.type = type;
        this.parentNodeAdapter = parentNodeAdapter;
        this.i18NPropertyName = baseName;
        this.basePropertyName = baseName;
        setDefaultValue(defaultValue);
    }

    @Override
    public void setI18NPropertyName(String i18NPropertyName) {
        this.i18NPropertyName = i18NPropertyName;
        fireValueChange();
    }

    @Override
    public T getValue() {
        return getOrCreateProperty().getValue();
    }

    @Override
    public void setValue(T newValue) throws ReadOnlyException {
        getOrCreateProperty().setValue(newValue);
    }

    @Override
    public Class<? extends T> getType() {
        return type;
    }

    @Override
    public String getLocalizedPropertyName() {
        return i18NPropertyName;
    }

    protected DefaultProperty<T> getOrCreateProperty() {
        String propertyName = getLocalizedPropertyName();
        DefaultProperty<T> property = (DefaultProperty<T>) parentNodeAdapter.getItemProperty(propertyName);
        if (property == null) {
            property = DefaultPropertyUtil.newDefaultProperty(PropertyUtil.getJCRPropertyType(defaultValue), defaultValue);
            parentNodeAdapter.addItemProperty(propertyName, property);
        }
        return property;
    }

    @Override
    public String getBasePropertyName() {
        return basePropertyName;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }
}
