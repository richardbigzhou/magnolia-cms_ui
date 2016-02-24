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

import info.magnolia.cms.util.DateUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.PropertyType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Property Utility Class.
 *
 * Allows the creation of custom Value Object.
 */
public class DefaultPropertyUtil {

    private static final Logger log = LoggerFactory.getLogger(DefaultPropertyUtil.class);

    /**
     * Create a DefaultProperty and set the defaultValue after conversion.
     */
    public static <T> DefaultProperty<T> newDefaultProperty(Class<T> type, String defaultValue) {
        Object value = null;
        try {
            value = createTypedValue(type, defaultValue);
        } catch (Exception e) {
            log.error("Exception during Value creation", e);
        }
        return new DefaultProperty<T>(type, (T) value);
    }

    /**
     * Create a new DefaultProperty by passing the value as a String.
     * If fieldType is defined, create a Typed Value.
     * If fieldType is not defined, create a String Value.
     * If stringValue is defined, create a typed value based on fieldType.
     *
     * @deprecated since 5.1. use {@link DefaultPropertyUtil#newDefaultProperty(Class, String)} instead.
     */
    @Deprecated
    public static DefaultProperty newDefaultProperty(String fieldType, String stringValue) throws NumberFormatException {
        Object value = null;
        try {
            value = createTypedValue(fieldType, stringValue);
        } catch (Exception e) {
            log.error("Exception during Value creation", e);
        }
        return new DefaultProperty(getFieldTypeClass(fieldType), value);
    }

    /**
     * Create a DefaultProperty based on types defined in {@link PropertyType}.
     *
     * @deprecated since 5.1. use {@link DefaultPropertyUtil#newDefaultProperty(Class, String)} instead.
     */
    @Deprecated
    public static DefaultProperty newDefaultProperty(int fieldType, Object value) throws NumberFormatException {
        return new DefaultProperty(getFieldTypeClass(fieldType), value);
    }

    /**
     * Create a custom Field Object based on the Type and defaultValue.
     * If the fieldType is null, the defaultValue will be returned as String or null.
     * If the defaultValue is null, null will be returned.
     *
     * @throws NumberFormatException In case of the default value could not be parsed to the desired class.
     * @deprecated since 5.1. use {@link DefaultPropertyUtil#createTypedValue(Class, String)} instead.
     */
    @Deprecated
    public static Object createTypedValue(String fieldType, String defaultValue) throws NumberFormatException {
        if (StringUtils.isBlank(fieldType)) {
            return defaultValue;
        } else if (defaultValue != null) {
            Class<?> type = getFieldTypeClass(fieldType);
            return createTypedValue(type, defaultValue);
        }
        return null;
    }

    /**
     * Create a custom Field Object based on the Type and defaultValue.
     * If the fieldType is null, the defaultValue will be returned as String or null.
     * If the defaultValue is null, null will be returned.
     *
     * @throws NumberFormatException In case of the default value could not be parsed to the desired class.
     */
    public static Object createTypedValue(Class<?> type, String defaultValue) throws NumberFormatException {
        if (StringUtils.isBlank(defaultValue)) {
            return defaultValue;
        } else if (defaultValue != null) {
            if (type.getName().equals(String.class.getName())) {
                return defaultValue;
            } else if (type.getName().equals(Long.class.getName())) {
                return Long.decode(defaultValue);
            } else if (type.isAssignableFrom(Binary.class)) {
                return null;
            } else if (type.getName().equals(Double.class.getName())) {
                return Double.valueOf(defaultValue);
            } else if (type.getName().equals(Date.class.getName())) {
                try {
                    return new SimpleDateFormat(DateUtil.YYYY_MM_DD).parse(defaultValue);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            } else if (type.getName().equals(Boolean.class.getName())) {
                return BooleanUtils.toBoolean(defaultValue);
            } else if (type.getName().equals(BigDecimal.class.getName())) {
                return BigDecimal.valueOf(Long.decode(defaultValue));
            } else if (type.isAssignableFrom(List.class)) {
                return Arrays.asList(defaultValue.split(","));
            } else {
                throw new IllegalArgumentException("Unsupported property type " + type.getName());
            }
        }
        return null;
    }

    /**
     * DefaultPropertyUtil mainly provides string-based conversion to JCR common property types.
     * In some cases, client may want to know in advance if this will apply to a given type, to implement alternative
     * strategies of creating/parsing property values.
     *
     * @see #createTypedValue(Class, String)
     */
    public static boolean canConvertStringValue(Class<?> type) {
        // basically mirroring conditions in impl above (as fishy as it is)
        if (type.getName().equals(String.class.getName())
                || type.getName().equals(Long.class.getName())
                || type.isAssignableFrom(Binary.class)
                || type.getName().equals(Double.class.getName())
                || type.getName().equals(Date.class.getName())
                || type.getName().equals(Boolean.class.getName())
                || type.getName().equals(BigDecimal.class.getName())
                || type.isAssignableFrom(List.class)) {
            return true;
        }
        return false;
    }

    /**
     * Return the related Class for a desired Type by String. Using {@link PropertyType} to read the type from the String.
     * If no fieldType is defined, the default is String.
     *
     * @throws IllegalArgumentException if the Type is not supported.
     */
    public static Class<?> getFieldTypeClass(String fieldType) {
        if (StringUtils.isNotEmpty(fieldType)) {
            int valueType = PropertyType.valueFromName(fieldType);
            return getFieldTypeClass(valueType);
        } else {
            return String.class;
        }
    }

    /**
     * Return the related Class for a desired Type.
     * If no fieldType is defined, the default is String.
     *
     * @throws IllegalArgumentException if the Type is not supported.
     */
    public static Class<?> getFieldTypeClass(int fieldType) {
        if (fieldType > 0) {
            switch (fieldType) {
            case PropertyType.STRING:
                return String.class;
            case PropertyType.BINARY:
                return Binary.class;
            case PropertyType.LONG:
                return Long.class;
            case PropertyType.DOUBLE:
                return Double.class;
            case PropertyType.DATE:
                // we use Date here instead of Calendar simply because the vaadin DateField uses Date not Calendar
                return Date.class;
            case PropertyType.BOOLEAN:
                return Boolean.class;
            case PropertyType.DECIMAL:
                return BigDecimal.class;
            default:
                throw new IllegalArgumentException("Unsupported property type " + PropertyType.nameFromValue(fieldType));
            }
        } else {
            return String.class;
        }
    }
}
