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

import info.magnolia.objectfactory.Components;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Script;

/**
 * Special type of Groovy script that .
 */
public abstract class DefinitionBuilderGroovyScript extends Script {

    private static Logger log = LoggerFactory.getLogger(DefinitionBuilderGroovyScript.class);

    public static final String VALUE_PROPERTY = "value";

    public void executeScript(String idBase) {
        try {
            /**
             * Invoke run() in order to evaluate all the script fields.
             */
            run();

            /**
             * Locate and bind all the methods annotated with builder-related annotations.
             */
            Map<Method, Annotation> annotatedMethods = ReflectionHelper.findConfigAnnotatedMethods(getClass());
            final Iterator<Map.Entry<Method, Annotation>> it = annotatedMethods.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Method, Annotation> entry = it.next();
                final Definition definitionAnnotation = entry.getValue().annotationType().getAnnotation(Definition.class);
                // TODO inject component provider instead
                DefinitionProviderRegistration op = Components.newInstance(definitionAnnotation.type());
                final String id = String.format("%s/%s", idBase, getDefinitionId(entry.getValue()));
                op.register(new ScriptDefinitionProvider(this, entry.getKey(), id));
            }
        } catch (Exception e) {
            log.error("Failed to evaluate and bind definition building methods {}", e.getMessage(), e);
        }
    }

    /**
     * Extract the 'value' property from the annotation.
     * TODO This is conventional unfortunately.
     */
    protected String getDefinitionId(Annotation key) {
        String id;
        try {
            id = (String) key.annotationType().getDeclaredMethod(VALUE_PROPERTY).invoke(key, null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            id = null;
        }
        return id;
    }

    /**
     * Binds a script method to a generic definition provider.
     */
    private static class ScriptDefinitionProvider implements DefinitionProvider<Object> {

        private Object source;

        private Method method;

        private String id;

        public ScriptDefinitionProvider(Object source, Method method, String id) {
            this.source = source;
            this.method = method;
            this.id = id;
        }

        @Override
        public Object getDefinition() {
            try {
                return invokeMethod();
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("Failed to invoke script method", e);
                return null;
            }
        }

        private Object invokeMethod() throws InvocationTargetException, IllegalAccessException {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final List<Object> args = new LinkedList<Object>();
            if (parameterTypes != null) {
                for (Class<?> parameterType : parameterTypes) {
                    // TODO inject component provider instead
                    args.add(Components.getComponentProvider().newInstance(parameterType, id));
                }
            }
            return method.invoke(source, args.toArray());
        }

        @Override
        public String getId() {
            return id;
        }
    }

}

