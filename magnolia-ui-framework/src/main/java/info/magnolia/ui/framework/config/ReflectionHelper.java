/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.framework.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Some utility methods related to reflection.
 */
public class ReflectionHelper {

    public static Map<Method, Annotation> findConfigAnnotatedMethods(Class<?> clazz) {
        final Map<Method, Annotation> annotatedMethods = new HashMap<Method,Annotation>();
        Class<?> targetClass = clazz;
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (!Modifier.isStatic(declaredMethod.getModifiers()) && !isOverridden(declaredMethod, clazz)) {
                Annotation[] declaredAnnotations = declaredMethod.getDeclaredAnnotations();
                if (declaredAnnotations != null) {
                    for (Annotation annotation : declaredAnnotations) {
                        if (annotation.annotationType().isAnnotationPresent(Definition.class)) {
                            annotatedMethods.put(declaredMethod, annotation);
                        }
                    }
                }
            }
        }
        return annotatedMethods;
    }

    public static boolean isOverridden(Method method, Class<?> clazz) {
        if (method.getDeclaringClass().equals(clazz)) {
            return false;
        }
        int modifiers = method.getModifiers();
        if (Modifier.isPrivate(modifiers)) {
            return false;
        }
        Class<?> targetClass = clazz;
        while (targetClass != null && !targetClass.equals(method.getDeclaringClass())) {
            for (Method declaredMethod : targetClass.getDeclaredMethods()) {
                if (overrides(declaredMethod, method)) {
                    return true;
                }
            }
            targetClass = clazz.getSuperclass();
        }
        return false;
    }

    /**
     * Returns true if a method overrides a method in a super class.
     */
    public static boolean overrides(Method method, Method methodInSuperclass) {
        if (!hasSameSignatures(method, methodInSuperclass)) {
            return false;
        }

        // See JLS section 8.4.8.1
        int modifiers = methodInSuperclass.getModifiers();
        if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
            return true;
        }
        if (Modifier.isPrivate(modifiers)) {
            return false;
        }
        // methodInSuperclass must be package-private
        return method.getDeclaringClass().getPackage().equals(methodInSuperclass.getDeclaringClass().getPackage());
    }

    public static boolean hasSameSignatures(Method lhs, Method rhs) {
        return lhs.getName().equals(rhs.getName()) && Arrays.equals(lhs.getParameterTypes(), rhs.getParameterTypes());
    }

    public static void makeAccessible(AccessibleObject accessibleObject) {
        if (!accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
    }


    // TODO the return here is total bogus
    public  static <T> T rethrowInvocationTargetException(InvocationTargetException e) throws InvocationTargetException {
        Throwable targetException = e.getTargetException();
        if (targetException instanceof Error) {
            throw (Error) targetException;
        }
        if (targetException instanceof RuntimeException) {
            throw (RuntimeException) targetException;
        }
        throw e;

    }
}
