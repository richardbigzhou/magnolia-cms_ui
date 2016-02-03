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
package info.magnolia.ui.admincentral.shellapp.pulse.item.registry;

import info.magnolia.config.registry.AbstractRegistry;
import info.magnolia.config.registry.DefinitionMetadataBuilder;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.DefinitionProviderWrapper;
import info.magnolia.config.registry.DefinitionType;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.shellapp.pulse.item.definition.ItemViewDefinition;

import javax.inject.Singleton;

/**
 * Maintains a registry of item view providers registered by id.
 */
@Singleton
public class ItemViewDefinitionRegistry extends AbstractRegistry<ItemViewDefinition> {
    public static final DefinitionType TYPE = new DefinitionType() {
        @Override
        public String name() {
            return "messageView";
        }

        @Override
        public Class baseClass() {
            return ItemViewDefinition.class;
        }
    };

    @Override
    public DefinitionType type() {
        return TYPE;
    }

    @Override
    public DefinitionMetadataBuilder newMetadataBuilder() {
        return DefinitionMetadataBuilder.usingModuleAndRelativePathAsId();
    }

    public ItemViewDefinition get(String id) throws RegistrationException {
        return getProvider(id).get();
    }

    @Override
    protected DefinitionProvider<ItemViewDefinition> onRegister(final DefinitionProvider<ItemViewDefinition> provider) {
        final DefinitionProvider<ItemViewDefinition> wrappedProvider = super.onRegister(provider);
        return new DefinitionProviderWrapper<ItemViewDefinition>(wrappedProvider) {
            @Override
            public ItemViewDefinition get() {
                final ItemViewDefinition td = super.get();
                final String referenceString = wrappedProvider.getMetadata().getReferenceId();
                td.setId(referenceString);
                return td;
            }
        };
    }
}
