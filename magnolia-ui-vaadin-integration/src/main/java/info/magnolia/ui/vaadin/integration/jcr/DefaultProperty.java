/**
 * This file Copyright (c) 2012-2016 Magnolia International
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

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;

/**
 * Basic implementation of {@link com.vaadin.data.Property}.
 *
 * @param <T> generic type of the value.
 */
public class DefaultProperty<T> extends ObjectProperty<T> {

    @SuppressWarnings("unchecked")
    // the cast is safe, because an object of type T has class Class<T>
    public DefaultProperty(T value) {
        this((value != null ? (Class<T>) value.getClass() : null), value);
    }

    /**
     * Creates a typed DefaultProperty based on the properties name, it's type value and the actual class type.
     * Value can be null.
     */
    public DefaultProperty(Class<T> type, T value) {
        super(value, type);
    }

    @Override
    public void setValue(T newValue) throws ReadOnlyException, ConversionException {
        if (isReadOnly()) {
            throw new ReadOnlyException("Property is readonly: Can not update value: " + String.valueOf(newValue));
        }
        if (newValue != null && !getType().isAssignableFrom(newValue.getClass())) {
            throw new ConversionException("Cannot convert " + newValue.getClass() + " to " + getType());
        }
        super.setValue(newValue);
    }

    @Override
    public String toString() {
        T value = getValue();
        return value != null ? value.toString() : "";
    }
}
