/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.dialog.registry;

import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

/**
 * Maintains a registry of dialog providers registered by id.
 */
@Singleton
public class DialogDefinitionRegistry {

    private final RegistryMap<String, DialogDefinitionProvider> registry = new RegistryMap<String, DialogDefinitionProvider>() {

        @Override
        protected String keyFromValue(DialogDefinitionProvider value) {
            return value.getId();
        }
    };

    public FormDialogDefinition getDialogDefinition(String id) throws RegistrationException {
        return getProvider(id).getDialogDefinition();
    }

    public Class<? extends FormDialogPresenter> getPresenterClass(String id) throws RegistrationException {
        return getProvider(id).getPresenterClass();
    }

    private DialogDefinitionProvider getProvider(String id) throws RegistrationException {
        try {
            return registry.getRequired(id);
        } catch (RegistrationException e) {
            throw new RegistrationException("No dialog definition registered for id: " + id, e);
        }
    }

    public void register(DialogDefinitionProvider provider) {
        registry.put(provider);
    }

    public Set<String> unregisterAndRegister(Set<String> registeredIds, List<DialogDefinitionProvider> providers) {
        return registry.removeAndPutAll(registeredIds, providers);
    }
}
