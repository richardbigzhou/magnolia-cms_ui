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

import info.magnolia.cms.util.DateUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jcr.Binary;
import javax.jcr.PropertyType;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
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
     * Create a new DefaultProperty.
     * If fieldType is define, create a Typed Value.
     * If defaultValue is defined, initialize the Field with the default value.
     * If fieldType is not defined, create a String Value.
     */
    public static DefaultProperty newDefaultProperty(String name, String fieldType, String defaultValue) throws NumberFormatException {
        Object value = null;
        try {
            value = createTypedValue(fieldType, defaultValue);
        } catch (Exception e) {
            log.error("Exception during Value creation", e);
            value = "";
        }
        return new DefaultProperty(name, value, getFieldTypeClass(fieldType));
    }

    /**
     * Create a custom Field Object based on the Type and defaultValue.
     * If the fieldType is null, the defaultValue will be returned as String or null.
     * If the defaultValue is null, null will be returned.
     * 
     * @throws NumberFormatException
     *             In case of the default value could not be parsed to the desired class.
     */
    public static Object createTypedValue(String fieldType, String defaultValue) throws NumberFormatException {
        if (StringUtils.isBlank(fieldType)) {
            return defaultValue;
        }
        else if (defaultValue != null) {
            int valueType = PropertyType.valueFromName(fieldType);
            switch (valueType) {
                case PropertyType.STRING:
                    return defaultValue;
                case PropertyType.BINARY:
                    return null;
                case PropertyType.LONG:
                    return Long.decode(defaultValue);
                case PropertyType.DOUBLE:
                    return Double.valueOf(defaultValue);
                case PropertyType.DATE:
                    try {
                        return new SimpleDateFormat(DateUtil.YYYY_MM_DD).parse(defaultValue);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                case PropertyType.BOOLEAN:
                    return BooleanUtils.toBoolean(defaultValue);
                case PropertyType.DECIMAL:
                    return BigDecimal.valueOf(Long.decode(defaultValue));
                default: {
                    String msg = "Unsupported property type " + PropertyType.nameFromValue(valueType);
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        return null;
    }

    /**
     * Return the related Class for a desired Type.
     * If no fieldType is defined, the default is String.
     * 
     * @throws IllegalArgumentException
     *             if the Type is not supported.
     */
    public static Class<?> getFieldTypeClass(String fieldType) {
        if (StringUtils.isNotEmpty(fieldType)) {
            int valueType = PropertyType.valueFromName(fieldType);
            switch (valueType) {
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
                    throw new IllegalArgumentException("Unsupported property type " + PropertyType.nameFromValue(valueType));
            }
        } else {
            return String.class;
        }
    }
}