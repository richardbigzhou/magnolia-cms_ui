/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.form.field.factory;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.form.fieldtype.registry.FieldTypeDefinitionRegistry;
import info.magnolia.ui.form.validator.registry.FieldValidatorFactoryFactory;

import java.io.Serializable;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating FieldFactory instances using an internal set of mappings connecting a {@link info.magnolia.ui.form.field.definition.FieldDefinition} class with a {@link FieldFactory} class.
 *
 * @see FieldDefinition
 * @see FieldFactory
 */
public class FieldFactoryFactory implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(FieldFactoryFactory.class);

    private final ComponentProvider componentProvider;
    private final FieldTypeDefinitionRegistry fieldTypeDefinitionRegistry;
    private final FieldValidatorFactoryFactory fieldValidatorFactoryFactory;
    private I18NAuthoringSupport i18NAuthoringSupport;
    private final UiContext uiContext;

    @Inject
    public FieldFactoryFactory(ComponentProvider componentProvider, FieldTypeDefinitionRegistry fieldTypeDefinitionRegistry, FieldValidatorFactoryFactory fieldValidatorFactoryFactory, UiContext uiContext, I18NAuthoringSupport i18NAuthoringSupport) {
        this.uiContext = uiContext;
        this.componentProvider = componentProvider;
        this.fieldTypeDefinitionRegistry = fieldTypeDefinitionRegistry;
        this.fieldValidatorFactoryFactory = fieldValidatorFactoryFactory;
        this.i18NAuthoringSupport = i18NAuthoringSupport;
    }

    /**
     * @deprecated since 5.4.1 - use {@link #FieldFactoryFactory(ComponentProvider, FieldTypeDefinitionRegistry, FieldValidatorFactoryFactory, UiContext, I18NAuthoringSupport)} instead.
     */
    @Deprecated
    public FieldFactoryFactory(ComponentProvider componentProvider, FieldTypeDefinitionRegistry fieldTypeDefinitionRegistry, FieldValidatorFactoryFactory fieldValidatorFactoryFactory) {
        this(componentProvider, fieldTypeDefinitionRegistry, fieldValidatorFactoryFactory, componentProvider.getComponent(UiContext.class), componentProvider.getComponent(I18NAuthoringSupport.class));
    }

    /**
     * Based on the {@link FieldDefinition}, get the related {@link FieldFactory} initialized with the input parameters. <br>
     * 
     * @param definition
     * @param parameters
     * @return related {@link FieldFactory} or null in case of exception or if no related factory is defined.
     */
    public FieldFactory createFieldFactory(FieldDefinition definition, Object... parameters) {

        FieldTypeDefinition fieldTypeDefinition;
        try {
            fieldTypeDefinition = fieldTypeDefinitionRegistry.getByDefinition(definition.getClass());
            if (fieldTypeDefinition == null) {
                return null;
            }
        } catch (RegistrationException e) {
            log.error("No field type definition found for " + definition.getName(), e);
            return null;
        }

        Class<? extends FieldFactory> factoryClass = fieldTypeDefinition.getFactoryClass();
        if (factoryClass == null) {
            log.warn("No factory class set for definition class [{}]. Please check your configuration.", definition.getClass().getName());
            return null;
        }

        Object[] combinedParameters = new Object[parameters.length + 1];
        combinedParameters[0] = definition;
        System.arraycopy(parameters, 0, combinedParameters, 1, parameters.length);

        FieldFactory fieldFactory = componentProvider.newInstance(factoryClass, combinedParameters);
        fieldFactory.setFieldValidatorFactoryFactory(fieldValidatorFactoryFactory);

        if (fieldFactory instanceof AbstractFieldFactory && uiContext instanceof SubAppContext) {
            final AbstractFieldFactory abstractFieldFactory = (AbstractFieldFactory) fieldFactory;
            final Locale authoringLocale = ((SubAppContext)uiContext).getAuthoringLocale();
            abstractFieldFactory.setLocale(authoringLocale == null ? i18NAuthoringSupport.getDefaultLocale() : authoringLocale);
        }

        return fieldFactory;
    }
}
