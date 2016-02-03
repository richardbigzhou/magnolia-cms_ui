/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.pulse.item;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;

/**
 * Nested Property for a map on a bean. Allows accessing map values in a dotted notation,
 * e.g. "address.street".
 *
 * Multiple level of nesting is not supported.
 *
 * @param <T> Type of the nested Property. Resolved in {@link #initialize(Class, String)}.
 */
public class NestedMapProperty<T> extends AbstractProperty<T> {

    private final Object instance;
    private String propertyName;
    private Class<? extends T> type;
    private Map map;

    public NestedMapProperty(Object instance, String propertyName) {
        this.instance = instance;
        this.propertyName = propertyName;

        initialize(instance.getClass(), propertyName);
    }

    /**
     * Gets the value stored in the Property. The value is read from the map .
     *
     * @return the value of the Property
     */
    @Override
    public T getValue() {
        return (T) map.get(propertyName);
    }

    /**
     * Sets the value of the property. The new value must be assignable to the
     * type of this property.
     *
     * @param newValue
     *            the New value of the property.
     * @throws <code>Property.ReadOnlyException</code> if the object is in
     *         read-only mode.
     */
    @Override
    public void setValue(T newValue) throws ReadOnlyException {
        // Checks the mode
        if (isReadOnly()) {
            throw new Property.ReadOnlyException();
        }
        map.put(propertyName, newValue);
        fireValueChange();
    }

    @Override
    public Class<? extends T> getType() {
        return type;
    }

    private void initialize(Class<?> beanClass, String propertyName)
            throws IllegalArgumentException {

        Class<?> propertyClass = beanClass;
        String[] simplePropertyNames = propertyName.split("\\.");

        if (propertyName.endsWith(".") || simplePropertyNames.length != 2) {
            throw new IllegalArgumentException("Invalid property name '"
                    + propertyName + "'");
        }

        String simplePropertyName = simplePropertyNames[0].trim();
        this.propertyName = simplePropertyNames[1].trim();

        try {
            Method getMethod = initGetterMethod(
                    simplePropertyName, propertyClass);
            propertyClass = getMethod.getReturnType();

            if (propertyClass.isAssignableFrom(Map.class)) {
                this.map = ((Map) getMethod.invoke(instance));

                Object object = map.get(this.propertyName);
                if (object == null) {
                    throw new IllegalArgumentException("Value '"
                            + propertyName + "' is null.");
                }
                this.type = (Class<? extends T>) convertPrimitiveType(object.getClass());
            }
            else {
                throw new IllegalArgumentException("Field '"
                        + simplePropertyName + "' is not a Map.");
            }

        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No getter defined for '"
                    + simplePropertyName + "'.");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Could not invoke getter defined for '"
                    + simplePropertyName + "'.");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access getter defined for '"
                    + simplePropertyName + "'.");
        }


    }

    /**
     * Find a getter method for a property (getXyz(), isXyz() or areXyz()).
     *
     * @param propertyName
     *            name of the property
     * @param beanClass
     *            class in which to look for the getter methods
     * @return Method
     * @throws NoSuchMethodException
     *             if no getter found
     */
    static Method initGetterMethod(String propertyName, final Class<?> beanClass)
            throws NoSuchMethodException {
        propertyName = propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);

        Method getMethod = null;
        try {
            getMethod = beanClass.getMethod("get" + propertyName,
                    new Class[] {});
        } catch (final java.lang.NoSuchMethodException ignored) {
            try {
                getMethod = beanClass.getMethod("is" + propertyName,
                        new Class[] {});
            } catch (final java.lang.NoSuchMethodException ignoredAsWell) {
                getMethod = beanClass.getMethod("are" + propertyName,
                        new Class[] {});
            }
        }
        return getMethod;
    }

    static Class<?> convertPrimitiveType(Class<?> type) {
        // Gets the return type from get method
        if (type.isPrimitive()) {
            if (type.equals(Boolean.TYPE)) {
                type = Boolean.class;
            } else if (type.equals(Integer.TYPE)) {
                type = Integer.class;
            } else if (type.equals(Float.TYPE)) {
                type = Float.class;
            } else if (type.equals(Double.TYPE)) {
                type = Double.class;
            } else if (type.equals(Byte.TYPE)) {
                type = Byte.class;
            } else if (type.equals(Character.TYPE)) {
                type = Character.class;
            } else if (type.equals(Short.TYPE)) {
                type = Short.class;
            } else if (type.equals(Long.TYPE)) {
                type = Long.class;
            }
        }
        return type;
    }


}
