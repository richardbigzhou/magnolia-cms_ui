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

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.dialog.registry.DialogDefinitionProvider;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;

import javax.inject.Inject;

/**
 * Registers a generic definition provider into a {@link DialogDefinitionRegistry}.
 */
public class DialogDefinitionProviderRegistration implements DefinitionProviderRegistration {

    private DialogDefinitionRegistry dialogDefinitionRegistry;

    @Inject
    public DialogDefinitionProviderRegistration(DialogDefinitionRegistry dialogDefinitionRegistry) {
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
    }

    @Override
    public void register(final DefinitionProvider definitionProvider) {
        dialogDefinitionRegistry.register(new DialogDefinitionProvider() {

            @Override
            public String getId() {
                return definitionProvider.getId();
            }

            @Override
            public FormDialogDefinition getDialogDefinition() throws RegistrationException {
                return (FormDialogDefinition)definitionProvider.getDefinition();
            }

            @Override
            public Class<? extends FormDialogPresenter> getPresenterClass() throws RegistrationException {
                return getDialogDefinition().getPresenterClass();
            }
        });
    }
}
