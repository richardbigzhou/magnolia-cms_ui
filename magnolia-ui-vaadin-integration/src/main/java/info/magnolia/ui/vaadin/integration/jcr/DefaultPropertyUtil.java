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

import java.math.BigDecimal;
import java.util.Date;

import javax.jcr.PropertyType;

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
    public static DefaultProperty newDefaultProperty(String name, String fieldType, String defaultValue) throws NumberFormatException{
        Object value = null;
        try {
            value = createTypedValue(name, fieldType, defaultValue);
        }catch(Exception e) {
            log.error("Exception during Value creation", e);
            value = "";
        }
        return new DefaultProperty(name, value);
    }

    /**
     * Create a custom Field Object based on the Type and defaultValue.
     * If the Type is nor defined, used he default one (String).
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws: In case of the default value could not be parsed to the desired class.
     */
    public static Object createTypedValue(String name, String fieldType, String defaultValue) throws NumberFormatException, InstantiationException, IllegalAccessException{
        if (StringUtils.isNotEmpty(fieldType)) {
            int valueType = PropertyType.valueFromName(fieldType);
            defaultValue = StringUtils.isNotBlank(defaultValue)?defaultValue:"";
            switch (valueType) {
                case PropertyType.STRING:
                    return (StringUtils.isNotBlank(defaultValue)?defaultValue:"");
                case PropertyType.LONG:
                    return (StringUtils.isNotBlank(defaultValue)?Long.valueOf(defaultValue):Long.class.newInstance());
                case PropertyType.DOUBLE:
                    return (StringUtils.isNotBlank(defaultValue)?Double.valueOf(defaultValue):Double.class.newInstance());
                case PropertyType.DATE:
                    // TODO ehe Check if this is Util!!
                    return null;
                case PropertyType.BOOLEAN:
                    return (StringUtils.isNotBlank(defaultValue)?Boolean.valueOf(defaultValue):Boolean.class.newInstance());
                case PropertyType.DECIMAL:
                    return (StringUtils.isNotBlank(defaultValue)?BigDecimal.valueOf(Double.parseDouble(defaultValue)):BigDecimal.class.newInstance());
                default: {
                    String msg = "Unsupported property type " + PropertyType.nameFromValue(valueType);
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
            }
        } else {
            return String.valueOf(StringUtils.isNotBlank(defaultValue)?defaultValue:"");
        }
    }

    /**
     * Return the related Class for a desired Type.
     * @throws IllegalArgumentException if the Type is null or not supported.
     */
    public static Class<?> getFieldTypeClass(String fieldType) {
        if (StringUtils.isNotEmpty(fieldType)) {
            int valueType = PropertyType.valueFromName(fieldType);
            switch (valueType) {
                case PropertyType.STRING:
                    return String.class;
                case PropertyType.LONG:
                    return Long.class;
                case PropertyType.DOUBLE:
                    return Double.class;
                case PropertyType.DATE:
                    // TODO we use Date here instead of Calendar simply because the vaadin DateField uses Date not Calendar
                    return Date.class;
                case PropertyType.BOOLEAN:
                    return Boolean.class;
                case PropertyType.DECIMAL:
                    return BigDecimal.class;
                default:
                    throw new IllegalArgumentException("Unsupported property type " + PropertyType.nameFromValue(valueType));
            }
        } else {
            throw new IllegalArgumentException("Unsupported property type null");
        }
    }
}
