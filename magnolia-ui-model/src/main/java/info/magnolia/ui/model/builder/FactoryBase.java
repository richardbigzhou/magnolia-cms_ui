/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.model.builder;

import info.magnolia.objectfactory.ComponentProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * A base class for implementing factories which instantiate implementations based on definition objects.
 *
 * @param <D> definition parent type
 * @param <I> implementation parent type
 */
public abstract class FactoryBase<D, I> {

    private ComponentProvider componentProvider;

    private Map<Class<? extends D>, Class<? extends I>> mapping = new HashMap<Class<? extends D>, Class<? extends I>>();

    protected FactoryBase(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    protected void addMapping(Class<? extends D> definitionClass, Class<? extends I> implementationClass) {
        mapping.put(definitionClass, implementationClass);
    }

    /**
     * Creates an instance of the implementation configured for the given definition. The parameters are made
     * available for injection when the instance is created. The definition object given is also available for
     * injection.
     */
    protected I create(D definition, Object... parameters) {

        Class<? extends I> implementationClass = resolveImplementationClass(definition);
        if (implementationClass != null) {

            // TODO: check whether this is satisfying enough - check TODO in FactoryBaseTest.Impl for details.
            Object[] combinedParameters = new Object[parameters.length + 1];
            combinedParameters[0] = definition;
            System.arraycopy(parameters, 0, combinedParameters, 1, parameters.length);

            return componentProvider.newInstance(implementationClass, combinedParameters);
        }
        return null;
    }

    private Class<? extends I> resolveImplementationClass(D definition) {
        final Class<?> definitionClass = definition.getClass();
        if (mapping.containsKey(definitionClass)) {
            return mapping.get(definitionClass);
        }
        for (Class<? extends D> keyClass : mapping.keySet()) {
            if (keyClass.isInstance(definition)) {
                return mapping.get(keyClass);
            }
        }
        return null;
    }

}
