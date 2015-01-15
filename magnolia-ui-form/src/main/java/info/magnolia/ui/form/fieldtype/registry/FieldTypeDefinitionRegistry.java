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
package info.magnolia.ui.form.fieldtype.registry;

import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import net.sf.cglib.proxy.Enhancer;

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

/**
 * FieldTypeDefinitionRegistry.
 */
@Singleton
public class FieldTypeDefinitionRegistry {

    private final RegistryMap<String, FieldTypeDefinitionProvider> registry = new RegistryMap<String, FieldTypeDefinitionProvider>() {

        @Override
        protected String keyFromValue(FieldTypeDefinitionProvider value) {
            return value.getId();
        }
    };

    public FieldTypeDefinition get(String id) throws RegistrationException {
        FieldTypeDefinitionProvider provider;
        try {
            provider = registry.getRequired(id);
        } catch (RegistrationException e) {
            throw new RegistrationException("No fieldTypeProvider registered for id: " + id, e);
        }
        return provider.getFieldTypeDefinition();
    }

    public FieldTypeDefinition getByDefinition(Class<? extends FieldDefinition> definitionClass) throws RegistrationException {
        // TODO hack: i18nizer proxies definition classes, so we have to unwrap them.
        while (Enhancer.isEnhanced(definitionClass)) {
            definitionClass = (Class<? extends FieldDefinition>)definitionClass.getSuperclass();
        }
        if (definitionClass.equals(ConfiguredFieldDefinition.class)) {
            // FIXME MGNLUI-829 Working around side effect of extend=override, can't do anything with ConfiguredFieldDefinition.
            return null;
        }
        // TODO end hack.

        for (FieldTypeDefinitionProvider provider : registry.values()) {
            if (definitionClass.equals(provider.getFieldTypeDefinition().getDefinitionClass())) {
                return provider.getFieldTypeDefinition();
            }
        }
        throw new RegistrationException("Could not find fieldType for definition " + definitionClass.getName());
    }

    public void register(FieldTypeDefinitionProvider provider) {
        registry.put(provider);
    }

    public Set<String> unregisterAndRegister(Set<String> registeredIds, List<FieldTypeDefinitionProvider> providers) {
        return registry.removeAndPutAll(registeredIds, providers);
    }
}
