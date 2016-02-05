/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.form.validator.definition;

import info.magnolia.ui.form.definition.AbstractFormKeyGenerator;
import info.magnolia.ui.form.definition.ConfiguredTabDefinition;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;

import java.lang.reflect.AnnotatedElement;
import java.util.List;


/**
 * Generates a key in the form <code> [app-name | dialog-name].tab-name.field-name.validation.[name of getter or field annotated with {@link info.magnolia.i18nsystem.I18nText}]</code>.
 */
public class FieldValidatorDefinitionKeyGenerator extends AbstractFormKeyGenerator<FieldValidatorDefinition> {

    private static final String VALIDATORS = "validators";
    private static final String VALIDATION = "validation";

    @Override
    protected void keysFor(List<String> keys, FieldValidatorDefinition object, AnnotatedElement el) {
        ConfiguredFieldDefinition fieldDefinition = getParentViaCast(object);
        ConfiguredTabDefinition tabDefinition = getConfiguredTabDefinition(fieldDefinition);
        final String rawIdOrName = getIdOrNameForUnknownRoot(object, false);
        final String idOrName = keyify(rawIdOrName);
        final String validatorName = getValidatorName(object);
        final String suffix = fieldOrGetterName(el);
        addKey(keys, idOrName, tabDefinition.getName(), fieldDefinition.getName(), VALIDATION, suffix);
        addKey(keys, idOrName, fieldDefinition.getName(), VALIDATION, suffix);
        addKey(keys, fieldDefinition.getName(), VALIDATION, suffix);
        if (validatorName != null) {
            addKey(keys, VALIDATORS, validatorName, suffix);
        }
    }

    /**
     * Get by recursion the parent tab definition from a field definition.
     */
    private ConfiguredTabDefinition getConfiguredTabDefinition(ConfiguredFieldDefinition fieldDefinition) {
        Object def = getParentViaCast(fieldDefinition);
        if (def instanceof ConfiguredTabDefinition) {
            return (ConfiguredTabDefinition) def;
        } else if (def instanceof ConfiguredFieldDefinition) {
            return getConfiguredTabDefinition((ConfiguredFieldDefinition) def);
        }
        return null;
    }

    private String getValidatorName(FieldValidatorDefinition definition) {
        if (definition instanceof ConfiguredFieldValidatorDefinition) {
            return ((ConfiguredFieldValidatorDefinition) definition).getName();
        }
        return null;
    }
}
