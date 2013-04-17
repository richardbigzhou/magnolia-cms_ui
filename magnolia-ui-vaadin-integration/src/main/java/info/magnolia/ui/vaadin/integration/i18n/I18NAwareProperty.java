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
package info.magnolia.ui.vaadin.integration.i18n;


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

    private String i18NPropertyName;

    private String type;

    private String defaultValue;

    private Locale locale;

    private JcrItemNodeAdapter parentNodeAdapter;

    public I18NAwareProperty(String baseName, String type, String defaultValue, JcrItemNodeAdapter parentNodeAdapter) {
        super();
        this.type = type;
        this.defaultValue = defaultValue;
        this.parentNodeAdapter = parentNodeAdapter;
        this.i18NPropertyName = baseName;
        this.basePropertyName = baseName;
    }

    public void setI18NPropertyName(String i18NPropertyName) {
        this.i18NPropertyName = i18NPropertyName;
        fireValueChange();
    }

    @Override
    public String getValue() {
        return getOrCreateProperty().getValue();
    }

    @Override
    public void setValue(String newValue) throws ReadOnlyException {
        if (newValue != null && !newValue.isEmpty()) {
            getOrCreateProperty().setValue(newValue);
        } else {
            parentNodeAdapter.removeItemProperty(getLocalizedPropertyName());
        }
    }

    @Override
    public Class<? extends String> getType() {
        return String.class;
    }

    protected String getLocalizedPropertyName() {
        return i18NPropertyName;
    }

    protected DefaultProperty<String> getOrCreateProperty() {
        String propertyName = getLocalizedPropertyName();
        DefaultProperty<String> property = (DefaultProperty<String>) parentNodeAdapter.getItemProperty(propertyName);
        if (property == null) {
            property = DefaultPropertyUtil.newDefaultProperty(propertyName, type, defaultValue);
            parentNodeAdapter.addItemProperty(propertyName, property);
        }
        return property;
    }

    public String getBasePropertyName() {
        return basePropertyName;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
