/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.vaadin.integration.jcr;

import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;

/**
 * Basic implementation of {@link com.vaadin.data.Property}.
 *
 * TODO dlipp - this impl is not depending on jcr, so it could/should be located in a different package.
 *
 * @param <T> generic type of the value.
 */
public class DefaultProperty<T> extends AbstractProperty<T> {

    private T value;
    private final Class<T> type;
    private String propertyName;

    /**
     * Constructor which reads the type from the value.
     * Do not use this constructor. As null values are actually wanted to not propagate empty fields to jcr.
     * Resolving the class type from the value can cause problems with converters.
     * @throws IllegalArgumentException if value is null.
     */
    @Deprecated
    public DefaultProperty(String propertyName, T value) {
        this.propertyName = propertyName;
        if (value == null) {
            throw new IllegalArgumentException("Null value passed to wrong constructor. This has been deprecated.");
        }
        this.value = value;
        this.type = (Class<T>) value.getClass();
    }

    public DefaultProperty(String propertyName, T value, Class<T> type) {
        this.propertyName = propertyName;
        this.value = value;
        this.type = type;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T newValue) throws ReadOnlyException, ConversionException {
        if (isReadOnly()) {
            throw new ReadOnlyException("Property is readonly: Can not update value: " + String.valueOf(newValue));
        }
        if (newValue != null && !getType().isAssignableFrom(newValue.getClass())) {
           throw new ConversionException("Cannot convert " + newValue.getClass() + " to " + getType());
        }
        value = newValue;
        fireValueChange();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    @Override
    public String toString() {
        T value = getValue();
        return value != null ? value.toString() : "";
    }
}
