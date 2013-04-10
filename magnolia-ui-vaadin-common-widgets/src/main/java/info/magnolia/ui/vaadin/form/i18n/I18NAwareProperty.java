/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.ui.vaadin.form.i18n;


import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemNodeAdapter;

import java.util.Locale;

import com.vaadin.data.util.AbstractProperty;

/**
 * Simple Property that manages one or more localized JCR properties internally. Depending on the Locale set
 * it delegates the value read/write to the corresponding JCR property.
 */
public class I18NAwareProperty extends AbstractProperty<String> {

    private String basePropertyName;
    private I18nContentSupport i18nSupport;
    private JcrItemNodeAdapter parentNodeAdapter;
    private DefaultProperty<String> currentProperty;

    public I18NAwareProperty(String baseName, JcrItemNodeAdapter parentNodeAdapter, I18nContentSupport i18nSupport) {
        super();
        this.i18nSupport = i18nSupport;
        this.basePropertyName = baseName;
        this.parentNodeAdapter = parentNodeAdapter;
        this.currentProperty = getOrCreateProperty();
    }

    @Override
    public String getValue() {
        return currentProperty.getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException {
        currentProperty.setValue(newValue);
    }

    @Override
    public Class<? extends String> getType() {
        return String.class;
    }

    public void setLocale(Locale newLocale) {
        i18nSupport.setLocale(newLocale);
        currentProperty = getOrCreateProperty();
        fireValueChange();
    }

    /**
     * Handle i18n definition.
     * If i18n is set to true, prefix the property name by the current language
     * (fr_, de_) if the current language is not the default one.
     */
    protected String getPropertyName() {
        boolean isFallbackLanguage = i18nSupport.getFallbackLocale().equals(i18nSupport.getLocale());
        if (!isFallbackLanguage) {
            return basePropertyName + "_" + i18nSupport.getLocale().toString();
        }
        return basePropertyName;
    }

    protected DefaultProperty<String> getOrCreateProperty() {
        String propertyName = getPropertyName();
        DefaultProperty<String> property = (DefaultProperty<String>) parentNodeAdapter.getItemProperty(propertyName);
        if (property == null) {
            property = DefaultPropertyUtil.newDefaultProperty(propertyName, "String", "");
        }
        return property;
    }
}
