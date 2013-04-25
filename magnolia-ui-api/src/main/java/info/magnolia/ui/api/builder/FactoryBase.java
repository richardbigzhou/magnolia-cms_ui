/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.api.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.objectfactory.ComponentProvider;

/**
 * FactoryBase.
 * @param <D>
 * @param <I>
 */
public abstract class FactoryBase<D, I> {

    private static final Logger log = LoggerFactory.getLogger(FactoryBase.class);
    
    protected ComponentProvider componentProvider;
    
    public FactoryBase(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }
    
    /**
     * Creates an instance of the implementation configured for the given definition. The parameters are made
     * available for injection when the instance is created. The definition object given is also available for
     * injection.
     * Returns <code>null</code> if no match is found (most likely a configuration error).
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
        log.warn("No matching implementation class was found for definition class [{}]. Please check your configuration.", definition.getClass().getName());
        return null;
    }
    
    protected abstract Class<? extends I> resolveImplementationClass(D definition);
    
}
